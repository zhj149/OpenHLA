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
  public static final String SETUP_COMPLETE = "SETUP_COMPLETE";

  private boolean setupCompleteAnnounced;
  private boolean setupComplete;

  public SynchronizedFederateAmbassador(RTIambassadorEx rtiAmbassador)
  {
    super(rtiAmbassador);
  }

  public void setupComplete()
    throws FederateNotExecutionMember, RestoreInProgress, SaveInProgress, RTIinternalError
  {
    rtiAmbassador.registerFederationSynchronizationPoint(SETUP_COMPLETE, null);
  }

  public void waitForSetupCompleteAnnounced()
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return !setupCompleteAnnounced; } });
  }

  public void waitForSetupComplete()
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return !setupComplete; } });
  }

  @Override
  public void announceSynchronizationPoint(String synchronizationPointLabel, byte[] tag)
    throws FederateInternalError
  {
    if (SETUP_COMPLETE.equals(synchronizationPointLabel))
    {
      setupCompleteAnnounced = true;

      try
      {
        rtiAmbassador.synchronizationPointAchieved(synchronizationPointLabel);
      }
      catch (Throwable t)
      {
        throw new FederateInternalError(t);
      }
    }
  }

  @Override
  public void federationSynchronized(String synchronizationPointLabel)
  {
    if (SETUP_COMPLETE.equals(synchronizationPointLabel))
    {
      setupComplete = true;
    }
  }
}
