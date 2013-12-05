/*
 * Copyright (c) 2005-2011, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.testsuite.hla.rti1516;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import hla.rti1516.CouldNotInitiateRestore;
import hla.rti1516.FederateHandle;
import hla.rti1516.FederateHandleRestoreStatusPair;
import hla.rti1516.FederateHandleSaveStatusPair;
import hla.rti1516.FederateInternalError;
import hla.rti1516.FederateNotExecutionMember;
import hla.rti1516.LogicalTime;
import hla.rti1516.RTIambassador;
import hla.rti1516.RTIinternalError;
import hla.rti1516.RestoreFailureReason;
import hla.rti1516.RestoreInProgress;
import hla.rti1516.RestoreStatus;
import hla.rti1516.SaveFailureReason;
import hla.rti1516.SaveInProgress;
import hla.rti1516.SaveStatus;
import hla.rti1516.SpecifiedSaveLabelDoesNotExist;
import hla.rti1516.jlc.NullFederateAmbassador;

public class BaseFederateAmbassador
  extends NullFederateAmbassador
{
  protected final RTIambassador rtiAmbassador;

  protected final Set<String> announcedSynchronizationPoints = new HashSet<String>();
  protected final Set<String> synchronizedSynchronizationPoints = new HashSet<String>();

  protected final Map<String, LogicalTime> successfullyInitiatedFederateSaves = new HashMap<String, LogicalTime>();

  protected final Set<String> successfulFederationSaves = new HashSet<String>();
  protected final Map<String, SaveFailureReason> unsuccessfulFederationSaves = new HashMap<String, SaveFailureReason>();

  protected final Map<FederateHandle, SaveStatus> saveStatusResponse = new HashMap<FederateHandle, SaveStatus>();

  protected final Set<String> successfulFederationRestoreRequests = new HashSet<String>();
  protected final Set<String> unsuccessfulFederationRestoreRequests = new HashSet<String>();

  protected final Map<String, FederateHandle> successfullyInitiatedFederateRestores =
    new HashMap<String, FederateHandle>();

  protected final Set<String> successfulFederationRestores = new HashSet<String>();
  protected final Map<String, RestoreFailureReason> unsuccessfulFederationRestores =
    new HashMap<String, RestoreFailureReason>();

  protected final Map<FederateHandle, RestoreStatus> restoreStatusResponse =
    new HashMap<FederateHandle, RestoreStatus>();

  protected String currentSaveLabel;

  protected boolean federateRestoreBegun;
  protected String currentRestoreLabel;

  public BaseFederateAmbassador(RTIambassador rtiAmbassador)
  {
    this.rtiAmbassador = rtiAmbassador;
  }

  protected void evokeCallbackWhile(Callable<Boolean> test)
    throws Exception
  {
    evokeCallbackWhile(test, 5);
  }

  protected void evokeCallbackWhile(Callable<Boolean> test, double minimumTime)
    throws Exception
  {
    evokeCallbackWhile(test, 5, minimumTime);
  }

  protected void evokeCallbackWhile(Callable<Boolean> test, int count)
    throws Exception
  {
    evokeCallbackWhile(test, count, 1.0);
  }

  protected void evokeCallbackWhile(Callable<Boolean> test, int count, double minimumTime)
    throws Exception
  {
    for (; count > 0 && test.call(); count--)
    {
      rtiAmbassador.evokeCallback(minimumTime);
    }
  }

  protected void doEvokeCallbackWhile(Callable<Boolean> test)
    throws Exception
  {
    doEvokeCallbackWhile(test, 5);
  }

  protected void doEvokeCallbackWhile(Callable<Boolean> test, double minimumTime)
    throws Exception
  {
    doEvokeCallbackWhile(test, 5, minimumTime);
  }

  protected void doEvokeCallbackWhile(Callable<Boolean> test, int count)
    throws Exception
  {
    doEvokeCallbackWhile(test, count, 1.0);
  }

  protected void doEvokeCallbackWhile(Callable<Boolean> test, int count, double minimumTime)
    throws Exception
  {
    do
    {
      rtiAmbassador.evokeCallback(minimumTime);
    } while (--count > 0 && test.call());
  }

  public void reset()
  {
    announcedSynchronizationPoints.clear();
    synchronizedSynchronizationPoints.clear();

    successfullyInitiatedFederateSaves.clear();

    successfulFederationSaves.clear();
    unsuccessfulFederationSaves.clear();

    saveStatusResponse.clear();

    successfulFederationRestoreRequests.clear();
    unsuccessfulFederationRestoreRequests.clear();

    successfullyInitiatedFederateRestores.clear();

    successfulFederationRestores.clear();
    unsuccessfulFederationRestores.clear();

    restoreStatusResponse.clear();

    currentSaveLabel = null;

    federateRestoreBegun = false;

    currentRestoreLabel = null;
  }

  public void registerSynchronizationPoint(String synchronizationPointLabel)
    throws FederateNotExecutionMember, RestoreInProgress, SaveInProgress, RTIinternalError
  {
    rtiAmbassador.registerFederationSynchronizationPoint(synchronizationPointLabel, null);
  }

  public void waitForAnnounceSynchronizationPoint(final String synchronizationPointLabel)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return !announcedSynchronizationPoints.contains(synchronizationPointLabel);
      }
    });
  }

  public void waitForFederationSynchronized(final String synchronizationPointLabel)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return !synchronizedSynchronizationPoints.contains(synchronizationPointLabel);
      }
    });
  }

  @Override
  public void announceSynchronizationPoint(String synchronizationPointLabel, byte[] tag)
    throws FederateInternalError
  {
    assert !announcedSynchronizationPoints.contains(synchronizationPointLabel);

    announcedSynchronizationPoints.add(synchronizationPointLabel);
    try
    {
      rtiAmbassador.synchronizationPointAchieved(synchronizationPointLabel);
    }
    catch (Throwable t)
    {
      throw new FederateInternalError(t.getMessage(), t);
    }
  }

  @Override
  public void federationSynchronized(String synchronizationPointLabel)
    throws FederateInternalError
  {
    assert announcedSynchronizationPoints.contains(synchronizationPointLabel);
    assert !synchronizedSynchronizationPoints.contains(synchronizationPointLabel);

    synchronizedSynchronizationPoints.add(synchronizationPointLabel);
  }

  public void checkInitiateFederateSave(String label)
    throws Exception
  {
    checkInitiateFederateSave(label, null);
  }

  public void checkInitiateFederateSave(final String label, LogicalTime time)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return !successfullyInitiatedFederateSaves.containsKey(label);
      }
    });

    assert successfullyInitiatedFederateSaves.containsKey(label);
    assert time == null || time.equals(successfullyInitiatedFederateSaves.get(label));
  }

  public void checkFederationSaved(final String label)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return !successfulFederationSaves.contains(label);
      }
    });

    assert successfulFederationSaves.contains(label);
  }

  public void checkFederationNotSaved(final String label, SaveFailureReason reason)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return !unsuccessfulFederationSaves.containsKey(label);
      }
    });

    assert unsuccessfulFederationSaves.containsKey(label);
    assert reason == unsuccessfulFederationSaves.get(label);
  }

  public void checkFederationSaveStatus(final Map<FederateHandle, SaveStatus> saveStatusResponse)
    throws Exception
  {
    doEvokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
        throws FederateNotExecutionMember, RestoreInProgress, RTIinternalError
      {
        boolean done = BaseFederateAmbassador.this.saveStatusResponse.equals(saveStatusResponse);
        if (!done)
        {
          rtiAmbassador.queryFederationSaveStatus();
        }
        return !done;
      }
    });

    assert this.saveStatusResponse.equals(saveStatusResponse);

    this.saveStatusResponse.clear();
  }

  public void checkRequestFederationRestoreSucceeded(final String label)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return !successfulFederationRestoreRequests.contains(label);
      }
    });

    assert successfulFederationRestoreRequests.contains(label);
  }

  public void checkRequestFederationRestoreFailed(final String label)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return !unsuccessfulFederationRestoreRequests.contains(label);
      }
    });

    assert unsuccessfulFederationRestoreRequests.contains(label);
  }

  public void checkFederateRestoreBegun()
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return !federateRestoreBegun;
      }
    });
  }

  public void checkInitiateFederateRestore(final String label, final FederateHandle federateHandle)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return !federateHandle.equals(successfullyInitiatedFederateRestores.get(label));
      }
    });

    assert federateHandle.equals(successfullyInitiatedFederateRestores.get(label));
  }

  public void checkFederationRestored(final String label)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return !successfulFederationRestores.contains(label);
      }
    });

    assert successfulFederationRestores.contains(label);
  }

  public void checkFederationNotRestored(final String label, RestoreFailureReason reason)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return !unsuccessfulFederationRestores.containsKey(label);
      }
    });

    assert unsuccessfulFederationRestores.containsKey(label);
    assert reason == unsuccessfulFederationRestores.get(label);
  }

  public void checkFederationRestoreStatus(final Map<FederateHandle, RestoreStatus> restoreStatusResponse)
    throws Exception
  {
    doEvokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
        throws FederateNotExecutionMember, SaveInProgress, RTIinternalError
      {
        boolean done = BaseFederateAmbassador.this.restoreStatusResponse.equals(restoreStatusResponse);
        if (!done)
        {
          rtiAmbassador.queryFederationRestoreStatus();
        }
        return !done;
      }
    });

    assert BaseFederateAmbassador.this.restoreStatusResponse.equals(restoreStatusResponse);

    this.restoreStatusResponse.clear();
  }

  @Override
  public void initiateFederateSave(String label)
    throws FederateInternalError
  {
    assert currentSaveLabel == null;

    currentSaveLabel = label;

    successfullyInitiatedFederateSaves.put(label, null);
  }

  @Override
  public void initiateFederateSave(String label, LogicalTime time)
    throws FederateInternalError
  {
    assert currentSaveLabel == null;

    currentSaveLabel = label;

    successfullyInitiatedFederateSaves.put(label, time);
  }

  @Override
  public void federationSaved()
    throws FederateInternalError
  {
    assert !successfulFederationSaves.contains(currentSaveLabel);
    assert !unsuccessfulFederationSaves.containsKey(currentSaveLabel);

    successfulFederationSaves.add(currentSaveLabel);

    currentSaveLabel = null;
  }

  @Override
  public void federationNotSaved(SaveFailureReason reason)
    throws FederateInternalError
  {
    assert !successfulFederationSaves.contains(currentSaveLabel);
    assert !unsuccessfulFederationSaves.containsKey(currentSaveLabel);

    unsuccessfulFederationSaves.put(currentSaveLabel, reason);

    currentSaveLabel = null;
  }

  @Override
  public void federationSaveStatusResponse(FederateHandleSaveStatusPair[] response)
    throws FederateInternalError
  {
    saveStatusResponse.putAll(toMap(response));
  }

  @Override
  public void requestFederationRestoreSucceeded(String label)
    throws FederateInternalError
  {
    assert !successfulFederationRestoreRequests.contains(label);
    assert !unsuccessfulFederationRestoreRequests.contains(label);

    successfulFederationRestoreRequests.add(label);
  }

  @Override
  public void requestFederationRestoreFailed(String label)
    throws FederateInternalError
  {
    assert !successfulFederationRestoreRequests.contains(label);
    assert !unsuccessfulFederationRestoreRequests.contains(label);

    unsuccessfulFederationRestoreRequests.add(label);
  }

  @Override
  public void federationRestoreBegun()
    throws FederateInternalError
  {
    assert !federateRestoreBegun;

    federateRestoreBegun = true;
  }

  @Override
  public void initiateFederateRestore(String label, FederateHandle federateHandle)
    throws SpecifiedSaveLabelDoesNotExist, CouldNotInitiateRestore, FederateInternalError
  {
    assert currentRestoreLabel == null;

    currentRestoreLabel = label;

    successfullyInitiatedFederateRestores.put(label, federateHandle);
  }

  @Override
  public void federationRestored()
    throws FederateInternalError
  {
    assert !successfulFederationRestores.contains(currentRestoreLabel);
    assert !unsuccessfulFederationRestores.containsKey(currentRestoreLabel);

    successfulFederationRestores.add(currentRestoreLabel);

    currentRestoreLabel = null;
    federateRestoreBegun = false;
  }

  @Override
  public void federationNotRestored(RestoreFailureReason reason)
    throws FederateInternalError
  {
    assert !successfulFederationRestores.contains(currentRestoreLabel);
    assert !unsuccessfulFederationRestores.containsKey(currentRestoreLabel);

    unsuccessfulFederationRestores.put(currentRestoreLabel, reason);

    currentRestoreLabel = null;
    federateRestoreBegun = false;
  }

  @Override
  public void federationRestoreStatusResponse(FederateHandleRestoreStatusPair[] response)
    throws FederateInternalError
  {
    restoreStatusResponse.putAll(toMap(response));
  }

  private static Map<FederateHandle, SaveStatus> toMap(FederateHandleSaveStatusPair[] response)
  {
    Map<FederateHandle, SaveStatus> saveStatusResponse = new HashMap<FederateHandle, SaveStatus>();
    for (FederateHandleSaveStatusPair federateHandleSaveStatusPair : response)
    {
      saveStatusResponse.put(federateHandleSaveStatusPair.handle, federateHandleSaveStatusPair.status);
    }
    return saveStatusResponse;
  }

  private static Map<FederateHandle, RestoreStatus> toMap(FederateHandleRestoreStatusPair[] response)
  {
    Map<FederateHandle, RestoreStatus> restoreStatusResponse = new HashMap<FederateHandle, RestoreStatus>();
    for (FederateHandleRestoreStatusPair federateRestoreStatus : response)
    {
      restoreStatusResponse.put(federateRestoreStatus.handle, federateRestoreStatus.status);
    }
    return restoreStatusResponse;
  }

  private static boolean equals(
    Map<FederateHandle, FederateHandleRestoreStatusPair> lhs, Map<FederateHandle, FederateHandleRestoreStatusPair> rhs)
  {
    boolean equals;
    if (equals = lhs.size() == rhs.size())
    {
      for (Map.Entry<FederateHandle, FederateHandleRestoreStatusPair> lhsEntry : lhs.entrySet())
      {
        FederateHandleRestoreStatusPair rhsFederateRestoreStatus = rhs.get(lhsEntry.getKey());
        if (equals = rhsFederateRestoreStatus != null)
        {
          equals = lhsEntry.getValue().handle.equals(rhsFederateRestoreStatus.handle) &&
                   lhsEntry.getValue().status == rhsFederateRestoreStatus.status;
        }
      }
    }
    return equals;
  }
}
