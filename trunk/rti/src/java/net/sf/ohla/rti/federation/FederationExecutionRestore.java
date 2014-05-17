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

import java.io.IOException;

import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sf.ohla.rti.util.FederateHandles;
import net.sf.ohla.rti.messages.FederationExecutionMessage;
import net.sf.ohla.rti.messages.Message;
import net.sf.ohla.rti.messages.Messages;
import net.sf.ohla.rti.messages.callbacks.FederationNotRestored;
import net.sf.ohla.rti.messages.callbacks.FederationRestoreBegun;
import net.sf.ohla.rti.messages.callbacks.FederationRestored;
import net.sf.ohla.rti.messages.callbacks.InitiateFederateRestore;
import net.sf.ohla.rti.proto.FederationExecutionSaveProtos.FederationExecutionSaveHeader;
import net.sf.ohla.rti.proto.FederationExecutionSaveProtos.SavedFederationExecutionMessage;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.protobuf.CodedInputStream;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateRestoreStatus;
import hla.rti1516e.RestoreFailureReason;
import hla.rti1516e.RestoreStatus;

public class FederationExecutionRestore
{
  private final FederationExecution federationExecution;

  private final String label;

  private final Path restoreFile;
  private final FileChannel restoreFileChannel;

  /**
   * {@code Map} of the post-restore {@link FederateHandle} to the {@link FederateRestoreMapping}.
   */
  private final Map<FederateHandle, FederateRestoreMapping> federateRestoreMappings = new HashMap<>();

  private final Set<FederateHandle> waitingForFederationToRestore = new HashSet<>();

  public FederationExecutionRestore(
    FederationExecution federationExecution, String label, FederateProxy requestingFederateProxy)
    throws IOException
  {
    this.federationExecution = federationExecution;
    this.label = label;

    restoreFile = federationExecution.getSaveDirectory().resolve(label + FederationExecutionSave.SAVE_FILE_EXTENSION);

    // TODO: check for existence of file and other stuff

    restoreFileChannel = FileChannel.open(restoreFile, StandardOpenOption.READ);

    FederationExecutionSaveHeader federationExecutionSaveHeader =
      FederationExecutionSaveHeader.parseDelimitedFrom(Channels.newInputStream(restoreFileChannel));

    // quick check
    //
    if (federationExecutionSaveHeader.getFederateCount() != federationExecution.getFederates().size())
    {
      // TODO: not enough federates subscribed
    }

    // organize the federate proxy restores by type
    //
    Multimap<String, FederateProxyRestore> federateProxyRestoresByType = HashMultimap.create();
    for (int federateCount = federationExecutionSaveHeader.getFederateCount(); federateCount > 0; --federateCount)
    {
      FederateProxyRestore federateProxyRestore = new FederateProxyRestore(restoreFileChannel);
      federateProxyRestoresByType.put(federateProxyRestore.getFederateType(), federateProxyRestore);
    }

    // organize the federates by type
    //
    Multimap<String, FederateProxy> federatesByType = HashMultimap.create();
    for (FederateProxy federate : federationExecution.getFederates().values())
    {
      federatesByType.put(federate.getFederateType(), federate);
    }

    // ensure that there are the same number of types
    //
    if (federateProxyRestoresByType.keySet().size() != federatesByType.keySet().size())
    {
      // TODO: incorrect number of federate types
    }

    // ensure that there are enough federates of each type and map the saved Federates to connected Federates
    //
    for (Map.Entry<String, Collection<FederateProxyRestore>> entry : federateProxyRestoresByType.asMap().entrySet())
    {
      Collection<FederateProxy> federatesOfType = federatesByType.get(entry.getKey());
      if (federatesOfType == null)
      {
        // TODO: missing federate type
      }
      else if (federatesOfType.size() != entry.getValue().size())
      {
        // TODO: not enough federates of federate type
      }
      else
      {
        // assign the current federates as the saved federates

        Iterator<FederateProxy> i = federatesOfType.iterator();
        Iterator<FederateProxyRestore> j = entry.getValue().iterator();
        while (i.hasNext())
        {
          assert j.hasNext();

          FederateRestoreMapping federateRestoreMapping =
            new FederateRestoreMapping(i.next(), j.next(), requestingFederateProxy);
          federateRestoreMappings.put(federateRestoreMapping.getPostRestoreFederateHandle(), federateRestoreMapping);
        }
        assert !j.hasNext();
      }
    }
  }

  public String getLabel()
  {
    return label;
  }

  public void begin()
  {
    for (FederateRestoreMapping federateRestoreMapping : federateRestoreMappings.values())
    {
      federateRestoreMapping.begin();
    }
  }

  public void initiate()
  {
    try
    {
      for (FederateRestoreMapping federateRestoreMapping : federateRestoreMappings.values())
      {
        federateRestoreMapping.initiate();
      }
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();

      fail(RestoreFailureReason.RTI_UNABLE_TO_RESTORE);
    }
  }

  public void abort()
  {
    fail(RestoreFailureReason.RESTORE_ABORTED);
  }

  public void fail(RestoreFailureReason restoreFailureReason)
  {
    FederationNotRestored federationNotRestored = new FederationNotRestored(restoreFailureReason);

    for (FederateRestoreMapping federateRestoreMapping : federateRestoreMappings.values())
    {
      federateRestoreMapping.getFederateProxy().federationNotRestored(federationNotRestored);
    }
  }

  public FederateRestoreStatus[] queryFederationRestoreStatus()
  {
    FederateRestoreStatus[] federationRestoreStatus = new FederateRestoreStatus[federateRestoreMappings.size()];

    Iterator<FederateRestoreMapping> federateRestoreMappings = this.federateRestoreMappings.values().iterator();
    for (int i = 0; i < federationRestoreStatus.length; i++)
    {
      federationRestoreStatus[i] = federateRestoreMappings.next().getFederateRestoreStatus();
    }

    return federationRestoreStatus;
  }

  public boolean federateRestoreComplete(FederateHandle federateHandle)
  {
    waitingForFederationToRestore.add(federateHandle);

    FederateRestoreMapping federateRestoreMapping = federateRestoreMappings.get(federateHandle);
    assert federateRestoreMapping != null;

    federateRestoreMapping.federateRestoreComplete();

    return waitingForFederationToRestore.size() == federateRestoreMappings.size();
  }

  public void federateRestoreNotComplete(FederateHandle federateHandle)
  {
    fail(RestoreFailureReason.FEDERATE_REPORTED_FAILURE_DURING_RESTORE);
  }

  public void federationRestored()
  {
    CodedInputStream restoreFileCodedInputStream =
      CodedInputStream.newInstance(Channels.newInputStream(restoreFileChannel));

    try
    {
      federationExecution.restoreState(restoreFileCodedInputStream);
    }
    catch (IOException e)
    {
      e.printStackTrace();

      // TODO: fail the restore
    }

    FederationRestored federationRestored = new FederationRestored();
    for (FederateRestoreMapping federateRestoreMapping : federateRestoreMappings.values())
    {
      federateRestoreMapping.federationRestored(federationRestored);

      federationExecution.getFederates().put(
        federateRestoreMapping.getPostRestoreFederateHandle(), federateRestoreMapping.getFederateProxy());
      federationExecution.getFederatesByName().put(
        federateRestoreMapping.getPostRestoreFederateName(), federateRestoreMapping.getFederateProxy());
    }

    // send the messages sent by federates after the save started, but before they were instructed to save
    //
    try (FileChannel fileChannel = restoreFileChannel)
    {
      while (!restoreFileCodedInputStream.isAtEnd())
      {
        SavedFederationExecutionMessage savedFederationExecutionMessage =
          restoreFileCodedInputStream.readMessage(SavedFederationExecutionMessage.PARSER, null);

        FederateHandle sendingFederateHandle = FederateHandles.convert(
          savedFederationExecutionMessage.getSendingFederateHandle());

        Message message = Messages.parseDelimitedFrom(
          restoreFileCodedInputStream, savedFederationExecutionMessage.getFederationExecutionMessageType());
        assert message instanceof FederationExecutionMessage;

        ((FederationExecutionMessage) message).execute(
          federationExecution, federationExecution.getFederate(sendingFederateHandle));
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();

      // TODO: ??
    }
  }

  private class FederateRestoreMapping
  {
    private final FederateProxy federateProxy;
    private final FederateProxyRestore federateProxyRestore;

    private final FederateHandle preRestoreFederateHandle;
    private final String preRestoreFederateName;

    private volatile RestoreStatus restoreStatus;

    public FederateRestoreMapping(
      FederateProxy federateProxy, FederateProxyRestore federateProxyRestore, FederateProxy requestingFederateProxy)
    {
      this.federateProxy = federateProxy;
      this.federateProxyRestore = federateProxyRestore;

      preRestoreFederateHandle = federateProxy.getFederateHandle();
      preRestoreFederateName = federateProxy.getFederateName();

      restoreStatus = federateProxy == requestingFederateProxy ?
        RestoreStatus.FEDERATE_WAITING_FOR_RESTORE_TO_BEGIN : RestoreStatus.NO_RESTORE_IN_PROGRESS;
    }

    public FederateProxy getFederateProxy()
    {
      return federateProxy;
    }

    public FederateProxyRestore getFederateProxyRestore()
    {
      return federateProxyRestore;
    }

    public FederateRestoreStatus getFederateRestoreStatus()
    {
      return new FederateRestoreStatus(preRestoreFederateHandle, federateProxyRestore.getFederateHandle(), restoreStatus);
    }

    public FederateHandle getPreRestoreFederateHandle()
    {
      return preRestoreFederateHandle;
    }

    public String getPreRestoreFederateName()
    {
      return preRestoreFederateName;
    }

    public FederateHandle getPostRestoreFederateHandle()
    {
      return federateProxyRestore.getFederateHandle();
    }

    public String getPostRestoreFederateName()
    {
      return federateProxyRestore.getFederateName();
    }

    public void begin()
    {
      // notify the Federate a restore has begun
      //
      federateProxy.federationRestoreBegun(new FederationRestoreBegun(
        label, federateProxyRestore.getFederateName(), federateProxyRestore.getFederateHandle()));

      restoreStatus = RestoreStatus.FEDERATE_PREPARED_TO_RESTORE;
    }

    public void initiate()
      throws IOException
    {
      // tell the Federate to initiate the restore
      //
      federateProxy.initiateFederateRestore(new InitiateFederateRestore());

      restoreStatus = RestoreStatus.FEDERATE_RESTORING;

      federateProxyRestore.restore(federateProxy);
    }

    public void federateRestoreComplete()
    {
      restoreStatus = RestoreStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_RESTORE;
    }

    public void federationRestored(FederationRestored federationRestored)
    {
      federateProxy.federationRestored(federationRestored);
    }
  }
}
