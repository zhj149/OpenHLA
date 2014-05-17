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

package net.sf.ohla.rti.testsuite.hla.rti1516e;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateHandleSaveStatusPair;
import hla.rti1516e.FederateHandleSet;
import hla.rti1516e.FederateRestoreStatus;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.RestoreFailureReason;
import hla.rti1516e.SaveFailureReason;
import hla.rti1516e.SaveStatus;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;

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

  protected final Map<String, FederateNameHandlePair> successfullyInitiatedFederateRestores =
    new HashMap<String, FederateNameHandlePair>();

  protected final Set<String> successfulFederationRestores = new HashSet<String>();
  protected final Map<String, RestoreFailureReason> unsuccessfulFederationRestores =
    new HashMap<String, RestoreFailureReason>();

  protected final Map<FederateHandle, FederateRestoreStatus> restoreStatusResponse =
    new HashMap<FederateHandle, FederateRestoreStatus>();

  protected String currentSaveLabel;

  protected boolean federateRestoreBegun;
  protected String currentRestoreLabel;

  public BaseFederateAmbassador(RTIambassador rtiAmbassador)
  {
    this.rtiAmbassador = rtiAmbassador;
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
    throws FederateNotExecutionMember, RestoreInProgress, SaveInProgress, NotConnected, RTIinternalError
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
        throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError
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

  public void checkInitiateFederateRestore(
    final String label, final String federateName, final FederateHandle federateHandle)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        FederateNameHandlePair federateNameHandlePair = successfullyInitiatedFederateRestores.get(label);
        return federateNameHandlePair == null ||
               !federateName.equals(federateNameHandlePair.federateName) ||
               !federateHandle.equals(federateNameHandlePair.federateHandle);
      }
    });

    assert successfullyInitiatedFederateRestores.containsKey(label);
    assert federateName.equals(successfullyInitiatedFederateRestores.get(label).federateName);
    assert federateHandle.equals(successfullyInitiatedFederateRestores.get(label).federateHandle);
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

  public void checkFederationRestoreStatus(final Map<FederateHandle, FederateRestoreStatus> restoreStatusResponse)
    throws Exception
  {
    doEvokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
        throws FederateNotExecutionMember, SaveInProgress, NotConnected, RTIinternalError
      {
        boolean done = BaseFederateAmbassador.equals(
          BaseFederateAmbassador.this.restoreStatusResponse, restoreStatusResponse);
        if (!done)
        {
          rtiAmbassador.queryFederationRestoreStatus();
        }
        return !done;
      }
    });

    assert equals(this.restoreStatusResponse, restoreStatusResponse);

    this.restoreStatusResponse.clear();
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
  public void federationSynchronized(String synchronizationPointLabel, FederateHandleSet failedToSync)
  {
    assert announcedSynchronizationPoints.contains(synchronizationPointLabel);
    assert !synchronizedSynchronizationPoints.contains(synchronizationPointLabel);

    synchronizedSynchronizationPoints.add(synchronizationPointLabel);
  }

  @Override
  public void initiateFederateSave(String label)
  {
    assert currentSaveLabel == null;

    currentSaveLabel = label;

    successfullyInitiatedFederateSaves.put(label, null);
  }

  @Override
  public void initiateFederateSave(String label, LogicalTime time)
  {
    assert currentSaveLabel == null;

    currentSaveLabel = label;

    successfullyInitiatedFederateSaves.put(label, time);
  }

  @Override
  public void federationSaved()
  {
    assert !successfulFederationSaves.contains(currentSaveLabel);
    assert !unsuccessfulFederationSaves.containsKey(currentSaveLabel);

    successfulFederationSaves.add(currentSaveLabel);

    currentSaveLabel = null;
  }

  @Override
  public void federationNotSaved(SaveFailureReason reason)
  {
    assert !successfulFederationSaves.contains(currentSaveLabel);
    assert !unsuccessfulFederationSaves.containsKey(currentSaveLabel);

    unsuccessfulFederationSaves.put(currentSaveLabel, reason);

    currentSaveLabel = null;
  }

  @Override
  public void federationSaveStatusResponse(FederateHandleSaveStatusPair[] response)
  {
    saveStatusResponse.putAll(toMap(response));
  }

  @Override
  public void requestFederationRestoreSucceeded(String label)
  {
    assert !successfulFederationRestoreRequests.contains(label);
    assert !unsuccessfulFederationRestoreRequests.contains(label);

    successfulFederationRestoreRequests.add(label);
  }

  @Override
  public void requestFederationRestoreFailed(String label)
  {
    assert !successfulFederationRestoreRequests.contains(label);
    assert !unsuccessfulFederationRestoreRequests.contains(label);

    unsuccessfulFederationRestoreRequests.add(label);
  }

  @Override
  public void federationRestoreBegun()
  {
    assert !federateRestoreBegun;

    federateRestoreBegun = true;
  }

  @Override
  public void initiateFederateRestore(String label, String federateName, FederateHandle federateHandle)
  {
    assert currentRestoreLabel == null;

    currentRestoreLabel = label;

    successfullyInitiatedFederateRestores.put(label, new FederateNameHandlePair(federateName, federateHandle));
  }

  @Override
  public void federationRestored()
  {
    assert !successfulFederationRestores.contains(currentRestoreLabel);
    assert !unsuccessfulFederationRestores.containsKey(currentRestoreLabel);

    successfulFederationRestores.add(currentRestoreLabel);

    currentRestoreLabel = null;
    federateRestoreBegun = false;
  }

  @Override
  public void federationNotRestored(RestoreFailureReason reason)
  {
    assert !successfulFederationRestores.contains(currentRestoreLabel);
    assert !unsuccessfulFederationRestores.containsKey(currentRestoreLabel);

    unsuccessfulFederationRestores.put(currentRestoreLabel, reason);

    currentRestoreLabel = null;
    federateRestoreBegun = false;
  }

  @Override
  public void federationRestoreStatusResponse(FederateRestoreStatus[] response)
  {
    restoreStatusResponse.putAll(toMap(response));
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

  private static Map<FederateHandle, SaveStatus> toMap(FederateHandleSaveStatusPair[] response)
  {
    Map<FederateHandle, SaveStatus> saveStatusResponse = new HashMap<FederateHandle, SaveStatus>();
    for (FederateHandleSaveStatusPair federateHandleSaveStatusPair : response)
    {
      saveStatusResponse.put(federateHandleSaveStatusPair.handle, federateHandleSaveStatusPair.status);
    }
    return saveStatusResponse;
  }

  private static Map<FederateHandle, FederateRestoreStatus> toMap(FederateRestoreStatus[] response)
  {
    Map<FederateHandle, FederateRestoreStatus> restoreStatusResponse =
      new HashMap<FederateHandle, FederateRestoreStatus>();
    for (FederateRestoreStatus federateRestoreStatus : response)
    {
      restoreStatusResponse.put(federateRestoreStatus.preRestoreHandle, federateRestoreStatus);
    }
    return restoreStatusResponse;
  }

  private static boolean equals(
    Map<FederateHandle, FederateRestoreStatus> lhs, Map<FederateHandle, FederateRestoreStatus> rhs)
  {
    boolean equals;
    if (equals = lhs.size() == rhs.size())
    {
      for (Map.Entry<FederateHandle, FederateRestoreStatus> lhsEntry : lhs.entrySet())
      {
        FederateRestoreStatus rhsFederateRestoreStatus = rhs.get(lhsEntry.getKey());
        if (equals = rhsFederateRestoreStatus != null)
        {
          equals = lhsEntry.getValue().preRestoreHandle.equals(rhsFederateRestoreStatus.preRestoreHandle) &&
                   (lhsEntry.getValue().postRestoreHandle == null ? rhsFederateRestoreStatus.postRestoreHandle == null :
                    lhsEntry.getValue().postRestoreHandle.equals(rhsFederateRestoreStatus.postRestoreHandle)) &&
                   lhsEntry.getValue().status == rhsFederateRestoreStatus.status;
        }
      }
    }
    return equals;
  }

  private static class FederateNameHandlePair
  {
    public final String federateName;
    public final FederateHandle federateHandle;

    private FederateNameHandlePair(String federateName, FederateHandle federateHandle)
    {
      this.federateName = federateName;
      this.federateHandle = federateHandle;
    }
  }
}
