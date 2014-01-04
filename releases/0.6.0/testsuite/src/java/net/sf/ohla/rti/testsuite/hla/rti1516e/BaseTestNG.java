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

import org.testng.annotations.BeforeClass;

import hla.rti1516e.CallbackModel;
import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RtiFactory;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.exceptions.AlreadyConnected;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.ConnectionFailed;
import hla.rti1516e.exceptions.CouldNotCreateLogicalTimeFactory;
import hla.rti1516e.exceptions.CouldNotOpenFDD;
import hla.rti1516e.exceptions.ErrorReadingFDD;
import hla.rti1516e.exceptions.FederateAlreadyExecutionMember;
import hla.rti1516e.exceptions.FederateIsExecutionMember;
import hla.rti1516e.exceptions.FederateNameAlreadyInUse;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederateOwnsAttributes;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.InconsistentFDD;
import hla.rti1516e.exceptions.InvalidLocalSettingsDesignator;
import hla.rti1516e.exceptions.InvalidResignAction;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.OwnershipAcquisitionPending;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;
import hla.rti1516e.exceptions.UnsupportedCallbackModel;

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

    rtiAmbassadors = new ArrayList<RTIambassador>(rtiAmbassadorCount);
    federateHandles = new ArrayList<FederateHandle>(rtiAmbassadorCount);
    federateAmbassadors = new ArrayList<FA>(rtiAmbassadorCount);
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

  protected void connect()
    throws ConnectionFailed, AlreadyConnected, UnsupportedCallbackModel, InvalidLocalSettingsDesignator,
           RTIinternalError, CallNotAllowedFromWithinCallback
  {
    connect(CallbackModel.HLA_EVOKED);
  }

  protected void connect(CallbackModel callbackModel)
    throws ConnectionFailed, AlreadyConnected, UnsupportedCallbackModel, InvalidLocalSettingsDesignator,
           RTIinternalError, CallNotAllowedFromWithinCallback
  {
    for (RTIambassador rtiAmbassador : rtiAmbassadors)
    {
      FA federateAmbassador = createFederateAmbassador(rtiAmbassador);
      federateAmbassadors.add(federateAmbassador);

      rtiAmbassador.connect(federateAmbassador, callbackModel);
    }
  }

  protected void disconnect()
    throws FederateIsExecutionMember, RTIinternalError, CallNotAllowedFromWithinCallback
  {
    for (RTIambassador rtiAmbassador : rtiAmbassadors)
    {
      rtiAmbassador.disconnect();
    }

    federateAmbassadors.clear();
  }

  protected void createFederationExecution()
    throws ErrorReadingFDD, CouldNotOpenFDD, InconsistentFDD, NotConnected, FederationExecutionAlreadyExists,
           RTIinternalError
  {
    rtiAmbassadors.get(0).createFederationExecution(federationExecutionName, fdd);
  }

  protected void createFederationExecution(String logicalTimeImplementationName)
    throws ErrorReadingFDD, CouldNotOpenFDD, InconsistentFDD, NotConnected, FederationExecutionAlreadyExists,
           CouldNotCreateLogicalTimeFactory, RTIinternalError
  {
    rtiAmbassadors.get(0).createFederationExecution(federationExecutionName, new URL[] { fdd }, logicalTimeImplementationName);
  }

  protected void destroyFederationExecution()
    throws FederationExecutionDoesNotExist, NotConnected, RTIinternalError, InterruptedException
  {
    destroyFederationExecution(federationExecutionName);
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

  protected void joinFederationExecution()
    throws FederationExecutionDoesNotExist, RestoreInProgress, FederateNameAlreadyInUse, SaveInProgress, NotConnected,
           CouldNotCreateLogicalTimeFactory, RTIinternalError, CallNotAllowedFromWithinCallback,
           FederateAlreadyExecutionMember
  {
    switch (rtiAmbassadors.size())
    {
      case 4:
        federateHandles.add(0, rtiAmbassadors.get(3).joinFederationExecution(
          FEDERATE_TYPE_4, FEDERATE_TYPE_4, federationExecutionName));
      case 3:
        federateHandles.add(0, rtiAmbassadors.get(2).joinFederationExecution(
          FEDERATE_TYPE_3, FEDERATE_TYPE_3, federationExecutionName));
      case 2:
        federateHandles.add(0, rtiAmbassadors.get(1).joinFederationExecution(
          FEDERATE_TYPE_2, FEDERATE_TYPE_2, federationExecutionName));
      case 1:
        federateHandles.add(0, rtiAmbassadors.get(0).joinFederationExecution(
          FEDERATE_TYPE_1, FEDERATE_TYPE_1, federationExecutionName));
        break;
      default:
      {
        for (RTIambassador rtiAmbassador : rtiAmbassadors)
        {
          federateHandles.add(rtiAmbassador.joinFederationExecution(FEDERATE_TYPE_1, federationExecutionName));
        }
      }
    }
  }

  protected void resignFederationExecution()
    throws FederateNotExecutionMember, FederateOwnsAttributes, OwnershipAcquisitionPending, NotConnected,
           RTIinternalError, CallNotAllowedFromWithinCallback, InvalidResignAction
  {
    resignFederationExecution(ResignAction.NO_ACTION);
  }

  protected void resignFederationExecution(ResignAction resignAction)
    throws FederateNotExecutionMember, FederateOwnsAttributes, OwnershipAcquisitionPending, NotConnected,
           RTIinternalError, CallNotAllowedFromWithinCallback, InvalidResignAction
  {
    for (RTIambassador rtiAmbassador : rtiAmbassadors)
    {
      rtiAmbassador.resignFederationExecution(resignAction);
    }

    federateHandles.clear();
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

  protected abstract FA createFederateAmbassador(RTIambassador rtiAmbassador);
}
