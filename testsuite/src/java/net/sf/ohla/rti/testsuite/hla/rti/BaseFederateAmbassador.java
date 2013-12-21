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

package net.sf.ohla.rti.testsuite.hla.rti;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.FederateInternalError;
import hla.rti.FederateNotExecutionMember;
import hla.rti.RTIinternalError;
import hla.rti.ReceivedInteraction;
import hla.rti.ReflectedAttributes;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hla.rti.SuppliedAttributes;
import hla.rti.SuppliedParameters;
import hla.rti.jlc.NullFederateAmbassador;
import hla.rti.jlc.RTIambassadorEx;

public class BaseFederateAmbassador
  extends NullFederateAmbassador
{
  protected final RTIambassadorEx rtiAmbassador;

  protected final Set<String> announcedSynchronizationPoints = new HashSet<String>();
  protected final Set<String> synchronizedSynchronizationPoints = new HashSet<String>();

  protected final Set<String> successfullyInitiatedFederateSaves = new HashSet<String>();

  protected final Set<String> successfulFederationSaves = new HashSet<String>();
  protected final Set<String> unsuccessfulFederationSaves = new HashSet<String>();

  protected final Set<String> successfulFederationRestoreRequests = new HashSet<String>();
  protected final Set<String> unsuccessfulFederationRestoreRequests = new HashSet<String>();

  protected final Map<String, Integer> successfullyInitiatedFederateRestores =
    new HashMap<String, Integer>();

  protected final Set<String> successfulFederationRestores = new HashSet<String>();
  protected final Set<String> unsuccessfulFederationRestores =new HashSet<String>();

  protected String currentSaveLabel;

  protected boolean federateRestoreBegun;
  protected String currentRestoreLabel;

  public BaseFederateAmbassador(RTIambassadorEx rtiAmbassador)
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
      rtiAmbassador.tick(minimumTime, minimumTime * 2);
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
      rtiAmbassador.tick(minimumTime, minimumTime * 2);
    } while (--count > 0 && test.call());
  }

  public void reset()
  {
    announcedSynchronizationPoints.clear();
    synchronizedSynchronizationPoints.clear();

    successfullyInitiatedFederateSaves.clear();

    successfulFederationSaves.clear();
    unsuccessfulFederationSaves.clear();

    successfulFederationRestoreRequests.clear();
    unsuccessfulFederationRestoreRequests.clear();

    successfullyInitiatedFederateRestores.clear();

    successfulFederationRestores.clear();
    unsuccessfulFederationRestores.clear();

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

  protected void checkReceivedInteraction(
    ReceivedInteraction receivedInteraction, SuppliedParameters suppliedParameters)
    throws ArrayIndexOutOfBounds
  {
    assert suppliedParameters.size() == receivedInteraction.size();
    for (int i = 0; i < receivedInteraction.size(); i++)
    {
      for (int j = 0; j < suppliedParameters.size(); j++)
      {
        if (receivedInteraction.getParameterHandle(i) ==
            suppliedParameters.getHandle(j))
        {
          assert Arrays.equals(receivedInteraction.getValue(i), suppliedParameters.getValue(j));
        }
      }
    }
  }

  protected void checkReflectedAttributes(
    ReflectedAttributes reflectedAttributes, SuppliedAttributes suppliedAttributes, boolean hasRegions)
    throws ArrayIndexOutOfBounds
  {
    assert suppliedAttributes.size() == reflectedAttributes.size();
    for (int i = 0; i < reflectedAttributes.size(); i++)
    {
      for (int j = 0; j < suppliedAttributes.size(); j++)
      {
        if (reflectedAttributes.getAttributeHandle(i) == suppliedAttributes.getHandle(j))
        {
          assert Arrays.equals(reflectedAttributes.getValue(i), suppliedAttributes.getValue(j));
          assert (hasRegions && reflectedAttributes.getRegion(i) != null) || !hasRegions;
        }
      }
    }
  }

  public void checkInitiateFederateSave(final String label)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return !successfullyInitiatedFederateSaves.contains(label);
      }
    });

    assert successfullyInitiatedFederateSaves.contains(label);
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

  public void checkFederationNotSaved(final String label)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return !unsuccessfulFederationSaves.contains(label);
      }
    });

    assert unsuccessfulFederationSaves.contains(label);
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

  public void checkInitiateFederateRestore(final String label, final Integer federateHandle)
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

  public void checkFederationNotRestored(final String label)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return !unsuccessfulFederationRestores.contains(label);
      }
    });

    assert unsuccessfulFederationRestores.contains(label);
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

    successfullyInitiatedFederateSaves.add(label);
  }

  @Override
  public void federationSaved()
  {
    assert !successfulFederationSaves.contains(currentSaveLabel);
    assert !unsuccessfulFederationSaves.contains(currentSaveLabel);

    successfulFederationSaves.add(currentSaveLabel);

    currentSaveLabel = null;
  }

  @Override
  public void federationNotSaved()
  {
    assert !successfulFederationSaves.contains(currentSaveLabel);
    assert !unsuccessfulFederationSaves.contains(currentSaveLabel);

    unsuccessfulFederationSaves.add(currentSaveLabel);

    currentSaveLabel = null;
  }

  @Override
  public void requestFederationRestoreSucceeded(String label)
  {
    assert !successfulFederationRestoreRequests.contains(label);
    assert !unsuccessfulFederationRestoreRequests.contains(label);

    successfulFederationRestoreRequests.add(label);
  }

  @Override
  public void requestFederationRestoreFailed(String label, String reason)
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
  public void initiateFederateRestore(String label, int federateHandle)
  {
    assert currentRestoreLabel == null;

    currentRestoreLabel = label;

    successfullyInitiatedFederateRestores.put(label, federateHandle);
  }

  @Override
  public void federationRestored()
  {
    assert !successfulFederationRestores.contains(currentRestoreLabel);
    assert !unsuccessfulFederationRestores.contains(currentRestoreLabel);

    successfulFederationRestores.add(currentRestoreLabel);

    currentRestoreLabel = null;
    federateRestoreBegun = false;
  }

  @Override
  public void federationNotRestored()
  {
    assert !successfulFederationRestores.contains(currentRestoreLabel);
    assert !unsuccessfulFederationRestores.contains(currentRestoreLabel);

    unsuccessfulFederationRestores.add(currentRestoreLabel);

    currentRestoreLabel = null;
    federateRestoreBegun = false;
  }
}
