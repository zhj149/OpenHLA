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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandle;
import net.sf.ohla.rti.messages.callbacks.FederationNotRestored;
import net.sf.ohla.rti.messages.callbacks.FederationRestoreBegun;
import net.sf.ohla.rti.messages.callbacks.InitiateFederateRestore;

import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateRestoreStatus;
import hla.rti1516e.RestoreFailureReason;
import hla.rti1516e.RestoreStatus;
import hla.rti1516e.exceptions.CouldNotDecode;

public class FederationExecutionRestore
{
  private final String label;

  private final RandomAccessFile restoreFile;

  private final Collection<FederateRestoreMapping> federateRestoreMappings = new LinkedList<FederateRestoreMapping>();

  private final Set<FederateHandle> waitingForFederationToRestore = new HashSet<FederateHandle>();

  public FederationExecutionRestore(File directory, String label, Map<FederateHandle, FederateProxy> federates)
    throws IOException
  {
    this.label = label;

    File file = new File(directory, label + FederationExecutionSave.SAVE_FILE_EXTENSION);

    // TODO: check for existence of file and other stuff

    restoreFile = new RandomAccessFile(file, "rw");

    int federatesInSave = restoreFile.readInt();

    // quick check
    //
    if (federatesInSave != federates.size())
    {
      // TODO: not enough federates subscribed
    }

    // organize the federate save headers by type
    //
    Map<String, Collection<FederateSaveHeader>> federateSaveHeadersByType =
      new HashMap<String, Collection<FederateSaveHeader>>();
    for (int i = 0; i < federatesInSave; i++)
    {
      FederateSaveHeader federateSaveHeader = new FederateSaveHeader(restoreFile);

      Collection<FederateSaveHeader> federateSaveHeadersOfType =
        federateSaveHeadersByType.get(federateSaveHeader.getFederateType());
      if (federateSaveHeadersOfType == null)
      {
        federateSaveHeadersOfType = new LinkedList<FederateSaveHeader>();
        federateSaveHeadersByType.put(federateSaveHeader.getFederateType(), federateSaveHeadersOfType);
      }
      federateSaveHeadersOfType.add(federateSaveHeader);
    }

    // organize the federates by type
    //
    Map<String, Collection<FederateProxy>> federatesByType = new HashMap<String, Collection<FederateProxy>>();
    for (FederateProxy federate : federates.values())
    {
      Collection<FederateProxy> federatesOfType = federatesByType.get(federate.getFederateType());
      if (federatesOfType == null)
      {
        federatesOfType = new LinkedList<FederateProxy>();
        federatesByType.put(federate.getFederateType(), federatesOfType);
      }
      federatesOfType.add(federate);
    }

    // ensure that there are the same number of types
    //
    if (federateSaveHeadersByType.keySet().size() != federatesByType.keySet().size())
    {
      // TODO: incorrect number of federate types
    }

    // ensure that there are enough federates of each type and map the saved Federates to connected Federates
    //
    for (Map.Entry<String, Collection<FederateSaveHeader>> entry : federateSaveHeadersByType.entrySet())
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

        Iterator<FederateSaveHeader> i = entry.getValue().iterator();
        Iterator<FederateProxy> j = federatesOfType.iterator();
        while (i.hasNext())
        {
          assert j.hasNext();

          federateRestoreMappings.add(new FederateRestoreMapping(i.next(), j.next()));
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
    for (FederateRestoreMapping federateRestoreMapping : federateRestoreMappings)
    {
      federateRestoreMapping.begin();
    }
  }

  public void initiate()
  {
    try
    {
      // create a FederateRestore for each Federate in the save
      //
      for (FederateRestoreMapping federateRestoreMapping : federateRestoreMappings)
      {
        federateRestoreMapping.initiate(restoreFile);
      }
    }
    catch (IOException ioe)
    {
      fail(RestoreFailureReason.RTI_UNABLE_TO_RESTORE);
    }
    catch (CouldNotDecode couldNotDecode)
    {
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

    for (FederateRestoreMapping federateRestoreMapping : federateRestoreMappings)
    {
      federateRestoreMapping.getFederateProxy().federationNotRestored(federationNotRestored);
    }
  }

  public void updateFederationRestoreStatus(Map<FederateHandle, FederateRestoreStatus> federationRestoreStatus)
  {
    for (FederateRestoreMapping federateRestoreMapping : federateRestoreMappings)
    {
      federationRestoreStatus.put(federateRestoreMapping.getFederateSaveHeader().getFederateHandle(),
                                  federateRestoreMapping.getFederateRestoreStatus());
    }
  }

  public boolean federateRestoreComplete(FederateHandle federateHandle)
  {
    waitingForFederationToRestore.add(federateHandle);

    return waitingForFederationToRestore.size() == federateRestoreMappings.size();
  }

  public void federateRestoreNotComplete(FederateHandle federateHandle)
  {
    fail(RestoreFailureReason.FEDERATE_REPORTED_FAILURE_DURING_RESTORE);
  }

  private class FederateRestoreMapping
  {
    private final FederateSaveHeader federateSaveHeader;
    private final FederateProxy federateProxy;

    private final FederateHandle preRestoreFederateHandle;
    private final String preRestoreFederateName;

    private FederateRestore federateRestore;

    private volatile RestoreStatus restoreStatus = RestoreStatus.NO_RESTORE_IN_PROGRESS;

    public FederateRestoreMapping(FederateSaveHeader federateSaveHeader, FederateProxy federateProxy)
    {
      this.federateSaveHeader = federateSaveHeader;
      this.federateProxy = federateProxy;

      preRestoreFederateHandle = federateProxy.getFederateHandle();
      preRestoreFederateName = federateProxy.getFederateName();
    }

    public FederateSaveHeader getFederateSaveHeader()
    {
      return federateSaveHeader;
    }

    public FederateProxy getFederateProxy()
    {
      return federateProxy;
    }

    public FederateRestoreStatus getFederateRestoreStatus()
    {
      return new FederateRestoreStatus(preRestoreFederateHandle, federateSaveHeader.getFederateHandle(), restoreStatus);
    }

    public void begin()
    {
      // notify the Federate a restore has begun
      //
      federateProxy.federationRestoreBegun(new FederationRestoreBegun());
    }

    public void initiate(RandomAccessFile file)
      throws IOException, CouldNotDecode
    {
      federateRestore = new FederateRestore(file);

      // tell the Federate to initiate the restore
      //
      federateProxy.initiateFederateRestore(new InitiateFederateRestore(
        label, federateSaveHeader.getFederateName(), federateSaveHeader.getFederateHandle()));

      federateRestore.restore(federateProxy);
    }
  }

  private static class FederateSaveHeader
  {
    private final IEEE1516eFederateHandle federateHandle;
    private final String federateName;
    private final String federateType;

    public FederateSaveHeader(RandomAccessFile restoreFile)
      throws IOException
    {
      federateHandle = IEEE1516eFederateHandle.decode(restoreFile);
      federateName = restoreFile.readUTF();
      federateType = restoreFile.readUTF();
    }

    public IEEE1516eFederateHandle getFederateHandle()
    {
      return federateHandle;
    }

    public String getFederateName()
    {
      return federateName;
    }

    public String getFederateType()
    {
      return federateType;
    }
  }
}
