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

package net.sf.ohla.rti.testsuite.hla.rti1516e.time;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import hla.rti1516e.CallbackModel;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.LogicalTimeFactoryFactory;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.time.HLAinteger64Time;
import hla.rti1516e.time.HLAinteger64TimeFactory;

public abstract class BaseTimeManagementTestNG
  extends BaseTestNG
{
  protected final String federationName;

  protected final List<FederateHandle> federateHandles;
  protected final List<TimeManagementFederateAmbassador> federateAmbassadors;

  protected final HLAinteger64TimeFactory logicalTimeFactory =
    LogicalTimeFactoryFactory.getLogicalTimeFactory(HLAinteger64TimeFactory.class);

  protected final HLAinteger64Time initial = logicalTimeFactory.makeInitial();

  protected final HLAinteger64Time five = logicalTimeFactory.makeTime(5L);
  protected final HLAinteger64Time ten = logicalTimeFactory.makeTime(10L);
  protected final HLAinteger64Time twenty = logicalTimeFactory.makeTime(20L);
  protected final HLAinteger64Time oneHundred = logicalTimeFactory.makeTime(100L);

  protected BaseTimeManagementTestNG(String federationName)
  {
    this.federationName = federationName;

    federateHandles = new ArrayList<FederateHandle>(rtiAmbassadorCount);
    federateAmbassadors = new ArrayList<TimeManagementFederateAmbassador>(rtiAmbassadorCount);
  }

  protected BaseTimeManagementTestNG(int rtiAmbassadorCount, String federationName)
  {
    super(rtiAmbassadorCount);

    this.federationName = federationName;

    federateHandles = new ArrayList<FederateHandle>(rtiAmbassadorCount);
    federateAmbassadors = new ArrayList<TimeManagementFederateAmbassador>(rtiAmbassadorCount);
  }

  @BeforeClass
  public void baseTimeSetup()
    throws Exception
  {
    for (RTIambassador rtiAmbassador : rtiAmbassadors)
    {
      TimeManagementFederateAmbassador federateAmbassador = new TimeManagementFederateAmbassador(rtiAmbassador);
      federateAmbassadors.add(federateAmbassador);

      rtiAmbassador.connect(federateAmbassador, CallbackModel.HLA_EVOKED);
    }

    rtiAmbassadors.get(0).createFederationExecution(federationName, new URL[] { fdd }, HLAinteger64TimeFactory.NAME);

    for (RTIambassador rtiAmbassador : rtiAmbassadors)
    {
      federateHandles.add(rtiAmbassador.joinFederationExecution(FEDERATE_TYPE, federationName));
    }
  }

  @AfterClass
  public void baseTimeTeardown()
    throws Exception
  {
    resignFederationExecution(ResignAction.NO_ACTION);

    destroyFederationExecution(federationName);
  }
}
