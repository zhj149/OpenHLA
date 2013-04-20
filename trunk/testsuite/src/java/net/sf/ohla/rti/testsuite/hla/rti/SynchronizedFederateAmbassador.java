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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import hla.rti.FederateInternalError;
import hla.rti.FederateNotExecutionMember;
import hla.rti.RTIinternalError;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hla.rti.jlc.RTIambassadorEx;

public class SynchronizedFederateAmbassador
  extends BaseFederateAmbassador
{
  private final Set<String> announcedSynchronizationPoints = new HashSet<String>();
  private final Set<String> synchronizedSynchronizationPoints = new HashSet<String>();

  public SynchronizedFederateAmbassador(RTIambassadorEx rtiAmbassador)
  {
    super(rtiAmbassador);
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
  {
    assert announcedSynchronizationPoints.contains(synchronizationPointLabel);
    assert !synchronizedSynchronizationPoints.contains(synchronizationPointLabel);

    synchronizedSynchronizationPoints.add(synchronizationPointLabel);
  }
}
