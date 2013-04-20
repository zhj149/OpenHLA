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

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandle;
import net.sf.ohla.rti.messages.callbacks.FederationNotRestored;
import net.sf.ohla.rti.messages.callbacks.FederationRestoreBegun;
import net.sf.ohla.rti.messages.callbacks.FederationRestored;
import net.sf.ohla.rti.messages.callbacks.InitiateFederateRestore;

import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateRestoreStatus;
import hla.rti1516e.RestoreFailureReason;
import hla.rti1516e.RestoreStatus;
import hla.rti1516e.exceptions.CouldNotDecode;

public class FederationExecutionRestore
{
  private final FederationExecution federationExecution;

  private final String label;

  private final RandomAccessFile restoreFile;

  /**
   * {@code Map} of the post-restore {@link FederateHandle} to the {@link FederateRestoreMapping}.
   */
  private final Map<FederateHandle, FederateRestoreMapping> federateRestoreMappings =
    new HashMap<FederateHandle, FederateRestoreMapping>();

  private final Set<FederateHandle> waitingForFederationToRestore = new HashSet<FederateHandle>();

  public FederationExecutionRestore(FederationExecution federationExecution, String label,
                                    FederateProxy requestingFederateProxy)
    throws IOException
  {
    this.federationExecution = federationExecution;
    this.label = label;

    File file = new File(federationExecution.getSaveDirectory(), label + FederationExecutionSave.SAVE_FILE_EXTENSION);

    // TODO: check for existence of file and other stuff

    restoreFile = new RandomAccessFile(file, "rw");

    int federatesInSave = restoreFile.readInt();

    // quick check
    //
    if (federatesInSave != federationExecution.getFederates().size())
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
    for (FederateProxy federate : federationExecution.getFederates().values())
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
      for (int count = federateRestoreMappings.size(); count > 0; count--)
      {
        FederateHandle federateHandle = IEEE1516eFederateHandle.decode(restoreFile);

        FederateRestoreMapping federateRestoreMapping = federateRestoreMappings.get(federateHandle);
        assert federateRestoreMapping != null;

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
    try
    {
      federationExecution.restoreState(restoreFile);

      int federationExecutionMessageCount = restoreFile.readInt();

      DataInput in = new DataInputStream(new GZIPInputStream(new InputStream()
      {
        public int read()
        throws IOException
        {
          return restoreFile.read();
        }

        @Override
        public int read(byte b[], int off, int len)
        throws IOException
        {
          return restoreFile.read(b, off, len);
        }

        @Override
        public long skip(long n)
        throws IOException
        {
          long skipped;

          long availableToSkip = restoreFile.length() - restoreFile.getFilePointer();
          if (n > availableToSkip)
          {
            skipped = availableToSkip;
          }
          else
          {
            skipped = n;
          }
          restoreFile.seek(restoreFile.getFilePointer() + n);
          return skipped;
        }

        @Override
        public int available()
          throws IOException
        {
          return new Long(restoreFile.length() - restoreFile.getFilePointer()).intValue();
        }

        @Override
        public void close()
          throws IOException
        {
          super.close();
        }
      }));

      for (int i = federationExecutionMessageCount; i > 0; i--)
      {
        FederateHandle sendingFederateHandle = IEEE1516eFederateHandle.decode(in);

        int length = in.readInt();
        byte[] buffer = new byte[length];
        in.readFully(buffer);
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();

      // TODO: fail the restore
    }
    catch (CouldNotDecode couldNotDecode)
    {
      couldNotDecode.printStackTrace();

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
  }

  private class FederateRestoreMapping
  {
    private final FederateSaveHeader federateSaveHeader;
    private final FederateProxy federateProxy;

    private final FederateHandle preRestoreFederateHandle;
    private final String preRestoreFederateName;

    private FederateProxyRestore federateProxyRestore;

    private volatile RestoreStatus restoreStatus;

    public FederateRestoreMapping(FederateSaveHeader federateSaveHeader, FederateProxy federateProxy,
                                  FederateProxy requestingFederateProxy)
    {
      this.federateSaveHeader = federateSaveHeader;
      this.federateProxy = federateProxy;

      preRestoreFederateHandle = federateProxy.getFederateHandle();
      preRestoreFederateName = federateProxy.getFederateName();

      restoreStatus = federateProxy == requestingFederateProxy ?
        RestoreStatus.FEDERATE_WAITING_FOR_RESTORE_TO_BEGIN : RestoreStatus.NO_RESTORE_IN_PROGRESS;
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
      return federateSaveHeader.getFederateHandle();
    }

    public String getPostRestoreFederateName()
    {
      return federateSaveHeader.getFederateName();
    }

    public void begin()
    {
      // notify the Federate a restore has begun
      //
      federateProxy.federationRestoreBegun(new FederationRestoreBegun(
        label, federateSaveHeader.getFederateName(), federateSaveHeader.getFederateHandle()));

      restoreStatus = RestoreStatus.FEDERATE_PREPARED_TO_RESTORE;
    }

    public void initiate(RandomAccessFile file)
      throws IOException, CouldNotDecode
    {
      federateProxyRestore = new FederateProxyRestore(
        federateSaveHeader.getFederateHandle(), federateSaveHeader.getFederateName(),
        federateSaveHeader.getFederateType(), file);

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
