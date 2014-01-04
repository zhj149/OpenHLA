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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandle;
import net.sf.ohla.rti.messages.FederationExecutionMessage;
import net.sf.ohla.rti.messages.Message;
import net.sf.ohla.rti.messages.MessageFactory;
import net.sf.ohla.rti.messages.callbacks.FederationSaved;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import hla.rti1516e.FederateHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.SaveFailureReason;
import hla.rti1516e.SaveStatus;
import hla.rti1516e.exceptions.CouldNotEncode;

public class FederationExecutionSave
{
  public static final String FEDERATION_EXECUTION_MESSAGES = "Federation_Execution_Messages";

  public static final String SAVE_FILE_EXTENSION = ".save";

  private final FederationExecution federationExecution;

  private final String label;
  private final LogicalTime saveTime;

  private final RandomAccessFile saveFile;

  private final File federationExecutionMessagesFile;
  private final DataOutputStream federationExecutionMessagesDataOutputStream;

  private final Map<FederateHandle, FederateProxySave> federateSaves = new HashMap<FederateHandle, FederateProxySave>();

  private final Set<FederateHandle> instructedToSave = new HashSet<FederateHandle>();
  private final Set<FederateHandle> saving = new HashSet<FederateHandle>();
  private final Set<FederateHandle> waitingForFederationToSave = new HashSet<FederateHandle>();

  private final Map<FederateHandle, SaveFailureReason> failed = new HashMap<FederateHandle, SaveFailureReason>();

  private SaveFailureReason saveFailureReason;

  private int federationExecutionMessageCount;

  public FederationExecutionSave(FederationExecution federationExecution, String label)
    throws IOException
  {
    this(federationExecution, label, null);
  }

  public FederationExecutionSave(FederationExecution federationExecution, String label, LogicalTime saveTime)
    throws IOException
  {
    this.federationExecution = federationExecution;
    this.label = label;
    this.saveTime = saveTime;

    federationExecution.getSaveDirectory().mkdirs();

    File file = new File(federationExecution.getSaveDirectory(), label + SAVE_FILE_EXTENSION);

    // TODO: check for existence of file and other stuff

    saveFile = new RandomAccessFile(file, "rw");

    saveFile.writeInt(federationExecution.getFederates().size());
    for (FederateProxy federate : federationExecution.getFederates().values())
    {
      ((IEEE1516eFederateHandle) federate.getFederateHandle()).writeTo(saveFile);
      saveFile.writeUTF(federate.getFederateName());
      saveFile.writeUTF(federate.getFederateType());
    }

    federationExecutionMessagesFile = File.createTempFile(FEDERATION_EXECUTION_MESSAGES, "save");
    DataOutputStream federationExecutionMessagesDataOutputStream = new DataOutputStream(
      new FileOutputStream(federationExecutionMessagesFile));

    // save space for an uncompressed int at the head of the file
    //
    federationExecutionMessagesDataOutputStream.writeInt(0);
    federationExecutionMessagesDataOutputStream.flush();

    // start the zipped portion after the uncompressed number has been written at the front
    //
    this.federationExecutionMessagesDataOutputStream = new DataOutputStream(
      new GZIPOutputStream(federationExecutionMessagesDataOutputStream));
  }

  public String getLabel()
  {
    return label;
  }

  public LogicalTime getSaveTime()
  {
    return saveTime;
  }

  public FederateProxySave getFederateSave(FederateHandle federateHandle)
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

  public FederateProxySave instructedToSave(FederateProxy federateProxy)
    throws IOException
  {
    instructedToSave.add(federateProxy.getFederateHandle());

    FederateProxySave federateProxySave = new FederateProxySave(federateProxy);
    federateSaves.put(federateProxy.getFederateHandle(), federateProxySave);
    return federateProxySave;
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

    if (instructedToSave.isEmpty())
    {
      // all federates have begun saving, no more messages will be coming in

      try
      {
        federationExecutionMessagesDataOutputStream.close();

        // write the number of messages at the front of the file (uncompressed)
        //
        RandomAccessFile raf = new RandomAccessFile(federationExecutionMessagesFile, "rw");
        raf.writeInt(federationExecutionMessageCount);
        raf.close();
      }
      catch (IOException ioe)
      {
        ioe.printStackTrace();

        // TODO: fail the save
      }
    }
  }

  public boolean federateSaveComplete(FederateHandle federateHandle)
  {
    saving.remove(federateHandle);
    waitingForFederationToSave.add(federateHandle);

    try
    {
      saveFile.writeInt(((IEEE1516eFederateHandle) federateHandle).getHandle());

      federateSaves.get(federateHandle).writeTo(saveFile);
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
    }

    boolean done;
    if (done = waitingForFederationToSave.size() == federateSaves.size())
    {
      try
      {
        federationExecution.saveState(saveFile);

        FileInputStream in = new FileInputStream(federationExecutionMessagesFile);

        long length = federationExecutionMessagesFile.length();
        long position = saveFile.getFilePointer();
        do
        {
          long bytesTransferred = saveFile.getChannel().transferFrom(in.getChannel(), position, length);
          length -= bytesTransferred;
          position += bytesTransferred;
        } while (length > 0);

        in.close();

        saveFile.close();
      }
      catch (IOException ioe)
      {
        ioe.printStackTrace();

        // TODO: fail the save
      }
      catch (CouldNotEncode couldNotEncode)
      {
        couldNotEncode.printStackTrace();

        // TODO: fail the save
      }
    }

    return done;
  }

  public boolean federateSaveNotComplete(FederateHandle federateHandle)
  {
    saving.remove(federateHandle);
    waitingForFederationToSave.add(federateHandle);

    failed.put(federateHandle, SaveFailureReason.FEDERATE_REPORTED_FAILURE_DURING_SAVE);

    saveFailureReason = SaveFailureReason.FEDERATE_REPORTED_FAILURE_DURING_SAVE;

    return saving.isEmpty();
  }

  public void federationSaved(Map<FederateHandle, FederateProxy> federates)
  {
    FederationSaved federationSaved = new FederationSaved();

    for (FederateProxy f : federates.values())
    {
      f.federationSaved(federationSaved);
    }

    // send the messages sent by federates after the save started, but before they were instructed to save
    //
    try
    {
      // read the uncompressed number of messages
      //
      DataInputStream in = new DataInputStream(new FileInputStream(federationExecutionMessagesFile));
      int federationExecutionMessageCount = in.readInt();

      // start reading the compressed part
      //
      in = new DataInputStream(new GZIPInputStream(in));

      for (; federationExecutionMessageCount > 0; federationExecutionMessageCount--)
      {
        FederateHandle sendingFederateHandle = IEEE1516eFederateHandle.decode(in);

        ChannelBuffer buffer = ChannelBuffers.buffer(in.readInt());
        buffer.writeBytes(in, buffer.writableBytes());

        Message message = MessageFactory.createMessage(
          buffer, federationExecution.getTimeManager().getLogicalTimeFactory());
        assert message instanceof FederationExecutionMessage;

        ((FederationExecutionMessage) message).execute(
          federationExecution, federationExecution.getFederate(sendingFederateHandle));
      }

      in.close();

      federationExecutionMessagesFile.delete();
    }
    catch (FileNotFoundException fnfe)
    {
      fnfe.printStackTrace();

      // TODO: ??
    }
    catch (IOException e)
    {
      e.printStackTrace();

      // TODO: ??
    }
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
    ((IEEE1516eFederateHandle) federateHandle).writeTo(federationExecutionMessagesDataOutputStream);

    ChannelBuffer buffer = message.getBuffer();

    int length = buffer.readableBytes();
    federationExecutionMessagesDataOutputStream.writeInt(length);
    buffer.readBytes(federationExecutionMessagesDataOutputStream, length);

    federationExecutionMessageCount++;
  }
}
