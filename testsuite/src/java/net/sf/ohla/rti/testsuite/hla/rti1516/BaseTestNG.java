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

package net.sf.ohla.rti.testsuite.hla.rti1516;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import net.sf.ohla.rti.hla.rti1516.Integer64TimeFactory;
import net.sf.ohla.rti.hla.rti1516.Integer64TimeIntervalFactory;

import org.testng.annotations.BeforeClass;

import hla.rti1516.CouldNotOpenFDD;
import hla.rti1516.ErrorReadingFDD;
import hla.rti1516.FederateAlreadyExecutionMember;
import hla.rti1516.FederateAmbassador;
import hla.rti1516.FederateHandle;
import hla.rti1516.FederateNotExecutionMember;
import hla.rti1516.FederateOwnsAttributes;
import hla.rti1516.FederatesCurrentlyJoined;
import hla.rti1516.FederationExecutionAlreadyExists;
import hla.rti1516.FederationExecutionDoesNotExist;
import hla.rti1516.MobileFederateServices;
import hla.rti1516.OwnershipAcquisitionPending;
import hla.rti1516.RTIambassador;
import hla.rti1516.RTIinternalError;
import hla.rti1516.ResignAction;
import hla.rti1516.RestoreInProgress;
import hla.rti1516.SaveInProgress;
import hla.rti1516.jlc.RtiFactory;
import hla.rti1516.jlc.RtiFactoryFactory;

public abstract class BaseTestNG<FA extends FederateAmbassador>
  implements TestConstants
{
  protected final int rtiAmbassadorCount;

  protected final String federationExecutionName;

  protected final List<RTIambassador> rtiAmbassadors;
  protected final List<FederateHandle> federateHandles;
  protected final List<FA> federateAmbassadors;

  protected URL fdd;
  protected URL badFDD;

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
    fdd = Thread.currentThread().getContextClassLoader().getResource(FDD);
    assert fdd != null : "could not locate: " + FDD;

    badFDD = Thread.currentThread().getContextClassLoader().getResource(BAD_FDD);
    assert badFDD != null : "could not locate: " + BAD_FDD;

    rtiFactory = RtiFactoryFactory.getRtiFactory();
  }

  protected void createFederationExecution()
    throws ErrorReadingFDD, CouldNotOpenFDD, FederationExecutionAlreadyExists, RTIinternalError
  {
    RTIambassador rtiAmbassador = rtiFactory.getRtiAmbassador();
    rtiAmbassador.createFederationExecution(federationExecutionName, fdd);
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
    RTIambassador rtiAmbassador = rtiFactory.getRtiAmbassador();
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
    throws FederationExecutionDoesNotExist, RestoreInProgress, SaveInProgress,
           RTIinternalError, FederateAlreadyExecutionMember
  {
    for (int count = rtiAmbassadorCount; count > 0; count--)
    {
      RTIambassador rtiAmbassador = rtiFactory.getRtiAmbassador();
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
        for (RTIambassador rtiAmbassador : rtiAmbassadors)
        {
          federateHandles.add(rtiAmbassador.joinFederationExecution(
            FEDERATE_TYPE_1, federationExecutionName, federateAmbassadors.get(i++), mobileFederateServices));
        }
      }
    }
  }

  protected void resignFederationExecution()
    throws FederateNotExecutionMember, FederateOwnsAttributes, OwnershipAcquisitionPending, RTIinternalError
  {
    resignFederationExecution(ResignAction.NO_ACTION);
  }

  protected void resignFederationExecution(ResignAction resignAction)
    throws FederateNotExecutionMember, FederateOwnsAttributes, OwnershipAcquisitionPending, RTIinternalError
  {
    for (RTIambassador rtiAmbassador : rtiAmbassadors)
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

  protected abstract FA createFederateAmbassador(RTIambassador rtiAmbassador);
}
