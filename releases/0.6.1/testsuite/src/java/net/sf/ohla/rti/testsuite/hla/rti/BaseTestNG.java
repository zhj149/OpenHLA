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

import net.sf.ohla.rti.hla.rti.Integer64TimeFactory;
import net.sf.ohla.rti.hla.rti.Integer64TimeIntervalFactory;

import org.testng.annotations.BeforeClass;

import hla.rti.CouldNotOpenFED;
import hla.rti.ErrorReadingFED;
import hla.rti.FederateAlreadyExecutionMember;
import hla.rti.FederateAmbassador;
import hla.rti.FederateNotExecutionMember;
import hla.rti.FederateOwnsAttributes;
import hla.rti.FederatesCurrentlyJoined;
import hla.rti.FederationExecutionAlreadyExists;
import hla.rti.FederationExecutionDoesNotExist;
import hla.rti.InvalidResignAction;
import hla.rti.MobileFederateServices;
import hla.rti.RTIinternalError;
import hla.rti.ResignAction;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hla.rti.jlc.RTIambassadorEx;
import hla.rti.jlc.RtiFactory;
import hla.rti.jlc.RtiFactoryFactory;

public abstract class BaseTestNG<FA extends FederateAmbassador>
  implements TestConstants
{
  protected final int rtiAmbassadorCount;
  protected final String federationExecutionName;

  protected final List<RTIambassadorEx> rtiAmbassadors;
  protected final List<Integer> federateHandles;
  protected final List<FA> federateAmbassadors;

  protected URL fed;
  protected URL badFED;

  protected RtiFactory rtiFactory;

  protected final MobileFederateServices mobileFederateServices =
    new MobileFederateServices(new Integer64TimeFactory(), new Integer64TimeIntervalFactory());

  protected BaseTestNG()
  {
    this(1, null);
  }

  protected BaseTestNG(String federationExecutionName)
  {
    this(1, federationExecutionName);
  }

  protected BaseTestNG(int rtiAmbassadorCount, String federationExecutionName)
  {
    this.rtiAmbassadorCount = rtiAmbassadorCount;
    this.federationExecutionName = federationExecutionName;

    rtiAmbassadors = new ArrayList<>(rtiAmbassadorCount);
    federateHandles = new ArrayList<>(rtiAmbassadorCount);
    federateAmbassadors = new ArrayList<>(rtiAmbassadorCount);
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
  }

  protected void createFederationExecution()
    throws CouldNotOpenFED, RTIinternalError, FederationExecutionAlreadyExists, ErrorReadingFED
  {
    RTIambassadorEx rtiAmbassador = rtiFactory.createRtiAmbassador();
    rtiAmbassador.createFederationExecution(federationExecutionName, fed);
  }

  protected void destroyFederationExecution()
    throws FederationExecutionDoesNotExist, RTIinternalError, InterruptedException
  {
    destroyFederationExecution(federationExecutionName);
  }

  protected void destroyFederationExecution(String federationExecutionName)
    throws FederationExecutionDoesNotExist, RTIinternalError, InterruptedException
  {
    destroyFederationExecution(federationExecutionName, 10);
  }

  protected void destroyFederationExecution(String federationExecutionName, int attempts)
    throws FederationExecutionDoesNotExist, RTIinternalError, InterruptedException
  {
    RTIambassadorEx rtiAmbassador = rtiFactory.createRtiAmbassador();
    boolean done = false;
    for(; !done && attempts > 0; attempts--)
    {
      try
      {
        rtiAmbassador.destroyFederationExecution(federationExecutionName);

        done = true;
      }
      catch (FederatesCurrentlyJoined fcj)
      {
        Thread.sleep(100);
      }
    }
  }

  protected void joinFederationExecution()
    throws FederationExecutionDoesNotExist, RestoreInProgress, SaveInProgress, RTIinternalError,
           FederateAlreadyExecutionMember
  {
    for (int count = rtiAmbassadorCount; count > 0; count--)
    {
      RTIambassadorEx rtiAmbassador = rtiFactory.createRtiAmbassador();
      rtiAmbassadors.add(rtiAmbassador);

      FA federateAmbassador = createFederateAmbassador(rtiAmbassador);
      federateAmbassadors.add(federateAmbassador);
    }

    switch (rtiAmbassadors.size())
    {
      case 4:
        federateHandles.add(0, rtiAmbassadors.get(3).joinFederationExecution(
          FEDERATE_TYPE_4, federationExecutionName, federateAmbassadors.get(3), mobileFederateServices));
      case 3:
        federateHandles.add(0, rtiAmbassadors.get(2).joinFederationExecution(
          FEDERATE_TYPE_3, federationExecutionName, federateAmbassadors.get(2), mobileFederateServices));
      case 2:
        federateHandles.add(0, rtiAmbassadors.get(1).joinFederationExecution(
          FEDERATE_TYPE_2, federationExecutionName, federateAmbassadors.get(1), mobileFederateServices));
      case 1:
        federateHandles.add(0, rtiAmbassadors.get(0).joinFederationExecution(
          FEDERATE_TYPE_1, federationExecutionName, federateAmbassadors.get(0), mobileFederateServices));
        break;
      default:
      {
        int i = 0;
        for (RTIambassadorEx rtiAmbassador : rtiAmbassadors)
        {
          federateHandles.add(rtiAmbassador.joinFederationExecution(
            FEDERATE_TYPE_1, federationExecutionName, federateAmbassadors.get(i++), mobileFederateServices));
        }
      }
    }
  }

  protected void resignFederationExecution()
    throws FederateNotExecutionMember, FederateOwnsAttributes, InvalidResignAction, RTIinternalError
  {
    resignFederationExecution(ResignAction.NO_ACTION);
  }

  protected void resignFederationExecution(int resignAction)
    throws FederateNotExecutionMember, FederateOwnsAttributes, InvalidResignAction, RTIinternalError
  {
    for (RTIambassadorEx rtiAmbassador : rtiAmbassadors)
    {
      rtiAmbassador.resignFederationExecution(resignAction);
    }
    rtiAmbassadors.clear();

    federateHandles.clear();
    federateAmbassadors.clear();
  }

  protected void synchronize(
    String synchronizationPointLabel, List<? extends BaseFederateAmbassador> federateAmbassadors)
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

  protected abstract FA createFederateAmbassador(RTIambassadorEx rtiAmbassador);
}
