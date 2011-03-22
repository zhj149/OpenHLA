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

package net.sf.ohla.rti.testsuite.hla.rti;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import hla.rti.FederateNotExecutionMember;
import hla.rti.FederateOwnsAttributes;
import hla.rti.FederatesCurrentlyJoined;
import hla.rti.FederationExecutionDoesNotExist;
import hla.rti.InvalidResignAction;
import hla.rti.RTIinternalError;
import hla.rti.jlc.RTIambassadorEx;
import hla.rti.jlc.RtiFactory;
import hla.rti.jlc.RtiFactoryFactory;

public abstract class BaseTestNG
  implements TestConstants
{
  protected final int rtiAmbassadorCount;
  protected final List<RTIambassadorEx> rtiAmbassadors;

  protected URL fed;
  protected URL badFED;

  protected RtiFactory rtiFactory;

  protected BaseTestNG()
  {
    this(1);
  }

  protected BaseTestNG(int rtiAmbassadorCount)
  {
    this.rtiAmbassadorCount = rtiAmbassadorCount;

    rtiAmbassadors = new ArrayList<RTIambassadorEx>(rtiAmbassadorCount);
  }

  @BeforeClass
  public final void baseSetup()
    throws Exception
  {
    fed = Thread.currentThread().getContextClassLoader().getResource(FED);
    assert fed != null : "could not locate: " + FED;

    badFED = Thread.currentThread().getContextClassLoader().getResource(BAD_FED);
    assert badFED != null : "could not locate: " + BAD_FED;

    rtiFactory = RtiFactoryFactory.getRtiFactory();
    for (int count = rtiAmbassadorCount; count >= 1; count--)
    {
      rtiAmbassadors.add(rtiFactory.createRtiAmbassador());
    }
  }

  @AfterClass
  public final void baseTeardown()
    throws Exception
  {
  }

  protected void setupComplete(List<? extends BaseFederateAmbassador> federateAmbassadors)
    throws Exception
  {
    federateAmbassadors.get(0).setupComplete();

    for (BaseFederateAmbassador federateAmbassador : federateAmbassadors)
    {
      federateAmbassador.waitForSetupCompleteAnnounced();
    }

    for (BaseFederateAmbassador federateAmbassador : federateAmbassadors)
    {
      federateAmbassador.waitForSetupComplete();
    }
  }

  protected void resignFederationExecution(int resignAction)
    throws FederateNotExecutionMember, FederateOwnsAttributes, RTIinternalError, InvalidResignAction
  {
    for (RTIambassadorEx rtiAmbassador : rtiAmbassadors)
    {
      rtiAmbassador.resignFederationExecution(resignAction);
    }
  }

  protected void destroyFederationExecution(String federationExecutionName)
    throws FederationExecutionDoesNotExist, RTIinternalError, InterruptedException
  {
    destroyFederationExecution(federationExecutionName, 10);
  }

  protected void destroyFederationExecution(String federationExecutionName, int attempts)
    throws FederationExecutionDoesNotExist, RTIinternalError, InterruptedException
  {
    boolean done = false;
    for(; !done && attempts > 0; attempts--)
    {
      try
      {
        rtiAmbassadors.get(0).destroyFederationExecution(federationExecutionName);

        done = true;
      }
      catch (FederatesCurrentlyJoined fcj)
      {
        Thread.sleep(100);
      }
    }
  }
}
