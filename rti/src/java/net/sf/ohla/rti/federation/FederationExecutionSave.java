/*
 * Copyright (c) 2006-2007, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.federation;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandle;
import net.sf.ohla.rti.messages.FederationExecutionMessage;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.FederateHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.SaveFailureReason;
import hla.rti1516e.SaveStatus;

public class FederationExecutionSave
{
  public static final String FEDERATION_EXECUTION_MESSAGES = "Federation_Execution_Messages";

  public static final String SAVE_FILE_EXTENSION = ".save";

  private final String label;
  private final LogicalTime saveTime;

  private final RandomAccessFile saveFile;

  private final File federationExecutionMessagesFile;
  private final OutputStream federationExecutionMessagesOutputStream;
  private final DataOutput federationExecutionMessagesDataOutput;

  private final Map<FederateHandle, FederateSave> federateSaves = new HashMap<FederateHandle, FederateSave>();

  private final List<FederationExecutionMessage> federationExecutionMessages = new LinkedList<FederationExecutionMessage>();

  private final Set<FederateHandle> instructedToSave = new HashSet<FederateHandle>();
  private final Set<FederateHandle> saving = new HashSet<FederateHandle>();
  private final Set<FederateHandle> waitingForFederationToSave = new HashSet<FederateHandle>();

  private final Map<FederateHandle, SaveFailureReason> failed = new HashMap<FederateHandle, SaveFailureReason>();

  private SaveFailureReason saveFailureReason;

  public FederationExecutionSave(File directory, String label, Collection<FederateProxy> federates)
    throws IOException
  {
    this(directory, label, federates, null);
  }

  public FederationExecutionSave(File directory, String label, Collection<FederateProxy> federates,
                                 LogicalTime saveTime)
    throws IOException
  {
    this.label = label;
    this.saveTime = saveTime;

    directory.mkdirs();

    File file = new File(directory, label + SAVE_FILE_EXTENSION);

    // TODO: check for existence of file and other stuff

    saveFile = new RandomAccessFile(file, "rw");

    saveFile.writeInt(federates.size());
    for (FederateProxy federate : federates)
    {
      ((IEEE1516eFederateHandle) federate.getFederateHandle()).writeTo(saveFile);
      saveFile.writeUTF(federate.getFederateName());
      saveFile.writeUTF(federate.getFederateType());
    }

    federationExecutionMessagesFile = File.createTempFile(FEDERATION_EXECUTION_MESSAGES, "save");
    federationExecutionMessagesOutputStream = new FileOutputStream(federationExecutionMessagesFile);
    federationExecutionMessagesDataOutput = new DataOutputStream(federationExecutionMessagesOutputStream);
  }

  public String getLabel()
  {
    return label;
  }

  public LogicalTime getSaveTime()
  {
    return saveTime;
  }

  public FederateSave getFederateSave(FederateHandle federateHandle)
  {
    return federateSaves.get(federateHandle);
  }

  public Set<FederateHandle> getInstructedToSave()
  {
    return instructedToSave;
  }

  public boolean hasFailed()
  {
    return !failed.isEmpty();
  }

  public SaveFailureReason getSaveFailureReason()
  {
    return saveFailureReason;
  }

  public FederateSave instructedToSave(FederateProxy federateProxy)
    throws IOException
  {
    instructedToSave.add(federateProxy.getFederateHandle());

    FederateSave federateSave = new FederateSave(federateProxy);
    federateSaves.put(federateProxy.getFederateHandle(), federateSave);
    return federateSave;
  }

  public void updateFederationSaveStatus(Map<FederateHandle, SaveStatus> federationSaveStatus)
  {
    for (FederateHandle federateHandle : instructedToSave)
    {
      federationSaveStatus.put(federateHandle, SaveStatus.FEDERATE_INSTRUCTED_TO_SAVE);
    }

    for (FederateHandle federateHandle : saving)
    {
      federationSaveStatus.put(federateHandle, SaveStatus.FEDERATE_SAVING);
    }

    for (FederateHandle federateHandle : waitingForFederationToSave)
    {
      federationSaveStatus.put(federateHandle, SaveStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_SAVE);
    }
  }

  public void federateSaveBegun(FederateHandle federateHandle)
  {
    instructedToSave.remove(federateHandle);
    saving.add(federateHandle);
  }

  public boolean federateSaveComplete(FederateHandle federateHandle)
  {
    saving.remove(federateHandle);
    waitingForFederationToSave.add(federateHandle);

    try
    {
      federateSaves.get(federateHandle).writeTo(saveFile);
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
    }

    boolean done;
    if (done = saving.isEmpty())
    {
      try
      {
        federationExecutionMessagesOutputStream.close();

        saveFile.writeLong(federationExecutionMessagesFile.length());

        FileInputStream in = new FileInputStream(federationExecutionMessagesFile);
        saveFile.getChannel().transferFrom(in.getChannel(), 0, federationExecutionMessagesFile.length());
        in.close();

        saveFile.close();
      }
      catch (IOException ioe)
      {
        ioe.printStackTrace();

        // TODO: fail the save
      }
    }

    return done;
  }

  public boolean federateSaveNotComplete(FederateHandle federateHandle)
  {
    saving.remove(federateHandle);
    failed.put(federateHandle, SaveFailureReason.FEDERATE_REPORTED_FAILURE_DURING_SAVE);
    waitingForFederationToSave.add(federateHandle);

    saveFailureReason = SaveFailureReason.FEDERATE_REPORTED_FAILURE_DURING_SAVE;

    return saving.isEmpty();
  }

  public boolean federateResigned(FederateHandle federateHandle)
  {
    instructedToSave.remove(federateHandle);
    saving.remove(federateHandle);
    failed.put(federateHandle, SaveFailureReason.FEDERATE_RESIGNED_DURING_SAVE);

    saveFailureReason = SaveFailureReason.FEDERATE_RESIGNED_DURING_SAVE;

    return saving.isEmpty();
  }

  public synchronized void save(FederateHandle federateHandle, FederationExecutionMessage message)
    throws IOException
  {
    ((IEEE1516eFederateHandle) federateHandle).writeTo(federationExecutionMessagesDataOutput);

    ChannelBuffer buffer = message.getBuffer();

    int length = buffer.readableBytes();
    federationExecutionMessagesDataOutput.writeInt(length);
    buffer.readBytes(federationExecutionMessagesOutputStream, length);
  }
}
