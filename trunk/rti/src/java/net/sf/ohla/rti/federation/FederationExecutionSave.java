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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.ohla.rti.util.FederateHandles;
import net.sf.ohla.rti.util.LogicalTimes;
import net.sf.ohla.rti.util.MoreChannels;
import net.sf.ohla.rti.messages.FederationExecutionMessage;
import net.sf.ohla.rti.messages.Message;
import net.sf.ohla.rti.messages.Messages;
import net.sf.ohla.rti.messages.callbacks.FederationSaved;
import net.sf.ohla.rti.proto.FederationExecutionSaveProtos.FederationExecutionSaveHeader;
import net.sf.ohla.rti.proto.FederationExecutionSaveProtos.SavedFederationExecutionMessage;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.SaveFailureReason;
import hla.rti1516e.SaveStatus;

public class FederationExecutionSave
{
  public static final String FEDERATION_EXECUTION_MESSAGES = "Federation_Execution_Messages";

  public static final String SAVE_FILE_EXTENSION = ".save";

  private final FederationExecution federationExecution;

  private final String label;
  private final LogicalTime saveTime;

  private final FileChannel saveFileChannel;
  private final CodedOutputStream saveFileCodedOutputStream;

  private final Path federationExecutionMessagesFile;
  private final FileChannel federationExecutionMessagesFileChannel;
  private final CodedOutputStream federationExecutionMessagesCodedOutputStream;

  private final Map<FederateHandle, FederateProxySave> federateProxySaves = new HashMap<>();

  private final Set<FederateHandle> instructedToSave = new HashSet<>();
  private final Set<FederateHandle> saving = new HashSet<>();
  private final Set<FederateHandle> waitingForFederationToSave = new HashSet<>();

  private final Map<FederateHandle, SaveFailureReason> failed = new HashMap<>();

  private SaveFailureReason saveFailureReason;

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

    // ensure the save directory has been created
    //
    Files.createDirectories(federationExecution.getSaveDirectory());

    Path saveFile = federationExecution.getSaveDirectory().resolve(label + SAVE_FILE_EXTENSION);

    saveFileChannel = FileChannel.open(
      saveFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    saveFileCodedOutputStream = CodedOutputStream.newInstance(Channels.newOutputStream(saveFileChannel));

    FederationExecutionSaveHeader.Builder federationExecutionSaveHeader =
      FederationExecutionSaveHeader.newBuilder();

    federationExecutionSaveHeader.setLabel(label);
    federationExecutionSaveHeader.setFederationExecutionName(federationExecution.getName());
    federationExecutionSaveHeader.setFederateCount(federationExecution.getFederates().size());

    if (saveTime != null)
    {
      federationExecutionSaveHeader.setSaveTime(LogicalTimes.convert(saveTime));
    }

    federationExecutionSaveHeader.setFdd(federationExecution.getFDD().toProto());

    federationExecutionSaveHeader.setRealTime(System.currentTimeMillis());

    saveFileCodedOutputStream.writeMessageNoTag(federationExecutionSaveHeader.build());
    saveFileCodedOutputStream.flush();

    federationExecutionMessagesFile = Files.createTempFile(FEDERATION_EXECUTION_MESSAGES, "save");
    federationExecutionMessagesFileChannel = FileChannel.open(
      federationExecutionMessagesFile, StandardOpenOption.WRITE);
    federationExecutionMessagesCodedOutputStream = CodedOutputStream.newInstance(
      Channels.newOutputStream(federationExecutionMessagesFileChannel));
  }

  public String getLabel()
  {
    return label;
  }

  public LogicalTime getSaveTime()
  {
    return saveTime;
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
    federateProxySaves.put(federateProxy.getFederateHandle(), federateProxySave);
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
    assert instructedToSave.contains(federateHandle);

    instructedToSave.remove(federateHandle);
    saving.add(federateHandle);

    if (instructedToSave.isEmpty())
    {
      // all federates have begun saving, no more messages will be coming in

      try
      {
        federationExecutionMessagesCodedOutputStream.flush();
        federationExecutionMessagesFileChannel.close();
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
    assert saving.contains(federateHandle);

    saving.remove(federateHandle);
    waitingForFederationToSave.add(federateHandle);

    try
    {
      federateProxySaves.get(federateHandle).writeTo(saveFileChannel, saveFileCodedOutputStream);
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();

      // TODO: fail the save
    }

    return instructedToSave.isEmpty() && saving.isEmpty();
  }

  public boolean federateSaveNotComplete(FederateHandle federateHandle)
  {
    saving.remove(federateHandle);
    waitingForFederationToSave.add(federateHandle);

    failed.put(federateHandle, SaveFailureReason.FEDERATE_REPORTED_FAILURE_DURING_SAVE);

    saveFailureReason = SaveFailureReason.FEDERATE_REPORTED_FAILURE_DURING_SAVE;

    return instructedToSave.isEmpty() && saving.isEmpty();
  }

  public void federationSaved(Map<FederateHandle, FederateProxy> federates)
  {
    try (FileChannel saveFileChannel = this.saveFileChannel)
    {
      // write the FederationExecution state
      //
      federationExecution.saveState(saveFileCodedOutputStream);
      saveFileCodedOutputStream.flush();

      // append any FederationExecution messages that were sent during the save
      //
      MoreChannels.transferFromFully(saveFileChannel, federationExecutionMessagesFile);
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();

      // TODO: fail the save
    }

    // notify all federates that the federation has saved successfully
    //
    FederationSaved federationSaved = new FederationSaved();
    for (FederateProxy f : federates.values())
    {
      f.federationSaved(federationSaved);
    }

    // send the messages sent by federates after the save started, but before they were instructed to save
    //
    try (InputStream in = Channels.newInputStream(FileChannel.open(federationExecutionMessagesFile, StandardOpenOption.READ)))
    {
      CodedInputStream federationExecutionMessagesCodedInputStream = CodedInputStream.newInstance(in);

      while (!federationExecutionMessagesCodedInputStream.isAtEnd())
      {
        SavedFederationExecutionMessage savedFederationExecutionMessage =
          federationExecutionMessagesCodedInputStream.readMessage(SavedFederationExecutionMessage.PARSER, null);

        FederateHandle sendingFederateHandle = FederateHandles.convert(
          savedFederationExecutionMessage.getSendingFederateHandle());

        Message message = Messages.parseDelimitedFrom(
          federationExecutionMessagesCodedInputStream,
          savedFederationExecutionMessage.getFederationExecutionMessageType());
        assert message instanceof FederationExecutionMessage;

        ((FederationExecutionMessage) message).execute(
          federationExecution, federationExecution.getFederate(sendingFederateHandle));
      }
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

    try
    {
      Files.delete(federationExecutionMessagesFile);
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

    return instructedToSave.isEmpty() && saving.isEmpty();
  }

  public synchronized void save(
    FederateHandle sendingFederateHandle, FederationExecutionMessage federationExecutionMessage)
    throws IOException
  {
    Message message = (Message) federationExecutionMessage;

    SavedFederationExecutionMessage.Builder savedFederationExecutionMessage =
      SavedFederationExecutionMessage.newBuilder();

    savedFederationExecutionMessage.setSendingFederateHandle(FederateHandles.convert(sendingFederateHandle));
    savedFederationExecutionMessage.setFederationExecutionMessageType(message.getMessageType());

    federationExecutionMessagesCodedOutputStream.writeMessageNoTag(savedFederationExecutionMessage.build());
    federationExecutionMessagesCodedOutputStream.writeMessageNoTag(message.getMessageLite());
  }
}
