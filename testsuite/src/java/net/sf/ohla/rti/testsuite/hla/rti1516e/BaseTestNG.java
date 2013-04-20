/*
 * Copyright (c) 2005-2010, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.testsuite.hla.rti1516e;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RtiFactory;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.FederateIsExecutionMember;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederateOwnsAttributes;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.InvalidResignAction;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.OwnershipAcquisitionPending;
import hla.rti1516e.exceptions.RTIinternalError;

public abstract class BaseTestNG
  implements TestConstants
{
  protected final int rtiAmbassadorCount;
  protected final List<RTIambassador> rtiAmbassadors;

  protected URL fdd;
  protected URL badFDD;

  protected RtiFactory rtiFactory;

  protected BaseTestNG()
  {
    this(1);
  }

  protected BaseTestNG(int rtiAmbassadorCount)
  {
    this.rtiAmbassadorCount = rtiAmbassadorCount;

    rtiAmbassadors = new ArrayList<RTIambassador>(rtiAmbassadorCount);
  }

  @BeforeClass
  public final void baseSetup()
    throws Exception
  {
    fdd = Thread.currentThread().getContextClassLoader().getResource(FDD);
    assert fdd != null : "could not locate: " + FDD;

    badFDD = Thread.currentThread().getContextClassLoader().getResource(BAD_FDD);
    assert badFDD != null : "could not locate: " + BAD_FDD;

    rtiFactory = RtiFactoryFactory.getRtiFactory();
    for (int count = rtiAmbassadorCount; count > 0; count--)
    {
      rtiAmbassadors.add(rtiFactory.getRtiAmbassador());
    }
  }

  @AfterClass
  public final void baseTeardown()
    throws Exception
  {
  }

  protected void synchronize(String synchronizationPointLabel,
                             List<? extends BaseFederateAmbassador> federateAmbassadors)
    throws Exception
  {
    federateAmbassadors.get(0).registerSynchronizationPoint(synchronizationPointLabel);

    for (BaseFederateAmbassador federateAmbassador : federateAmbassadors)
    {
      federateAmbassador.waitForAnnounceSynchronizationPoint(synchronizationPointLabel);
    }

    for (BaseFederateAmbassador federateAmbassador : federateAmbassadors)
    {
      federateAmbassador.waitForFederationSynchronized(synchronizationPointLabel);
    }
  }

  protected void disconnect()
    throws FederateIsExecutionMember, RTIinternalError, CallNotAllowedFromWithinCallback
  {
    for (RTIambassador rtiAmbassador : rtiAmbassadors)
    {
      rtiAmbassador.disconnect();
    }
  }

  protected void resignFederationExecution(ResignAction resignAction)
    throws FederateNotExecutionMember, FederateOwnsAttributes, OwnershipAcquisitionPending, NotConnected,
           RTIinternalError, CallNotAllowedFromWithinCallback, InvalidResignAction
  {
    for (RTIambassador rtiAmbassador : rtiAmbassadors)
    {
      rtiAmbassador.resignFederationExecution(resignAction);
    }
  }

  protected void destroyFederationExecution(String federationExecutionName)
    throws FederationExecutionDoesNotExist, NotConnected, RTIinternalError, InterruptedException
  {
    destroyFederationExecution(federationExecutionName, 10);
  }

  protected void destroyFederationExecution(String federationExecutionName, int attempts)
    throws FederationExecutionDoesNotExist, NotConnected, RTIinternalError, InterruptedException
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
