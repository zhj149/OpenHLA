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

import java.util.concurrent.Callable;

import hla.rti1516.FederateInternalError;
import hla.rti1516.FederateNotExecutionMember;
import hla.rti1516.RTIambassador;
import hla.rti1516.RTIinternalError;
import hla.rti1516.RestoreInProgress;
import hla.rti1516.SaveInProgress;
import hla.rti1516.jlc.NullFederateAmbassador;

public class BaseFederateAmbassador
  extends NullFederateAmbassador
{
  public static final String SETUP_COMPLETE = "SETUP_COMPLETE";

  protected final RTIambassador rtiAmbassador;

  private boolean setupCompleteAnnounced;
  private boolean setupComplete;

  public BaseFederateAmbassador(RTIambassador rtiAmbassador)
  {
    this.rtiAmbassador = rtiAmbassador;
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

  protected void evokeCallbackWhile(Callable<Boolean> test)
    throws Exception
  {
    evokeCallbackWhile(test, 5);
  }

  protected void evokeCallbackWhile(Callable<Boolean> test, int count)
    throws Exception
  {
    for (; count > 0 && test.call(); count--)
    {
      rtiAmbassador.evokeCallback(1.0);
    }
  }
}