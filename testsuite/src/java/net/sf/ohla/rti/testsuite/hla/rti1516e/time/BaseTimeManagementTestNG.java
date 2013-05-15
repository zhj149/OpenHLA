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

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import hla.rti1516e.LogicalTimeFactoryFactory;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.time.HLAinteger64Interval;
import hla.rti1516e.time.HLAinteger64Time;
import hla.rti1516e.time.HLAinteger64TimeFactory;

public abstract class BaseTimeManagementTestNG
  extends BaseTestNG<TimeManagementFederateAmbassador>
{
  protected final HLAinteger64TimeFactory logicalTimeFactory =
    LogicalTimeFactoryFactory.getLogicalTimeFactory(HLAinteger64TimeFactory.class);

  protected final HLAinteger64Time initial = logicalTimeFactory.makeInitial();

  protected final HLAinteger64Time two = logicalTimeFactory.makeTime(2L);
  protected final HLAinteger64Time three = logicalTimeFactory.makeTime(3L);
  protected final HLAinteger64Time four = logicalTimeFactory.makeTime(4L);
  protected final HLAinteger64Time five = logicalTimeFactory.makeTime(5L);
  protected final HLAinteger64Time six = logicalTimeFactory.makeTime(6L);
  protected final HLAinteger64Time seven = logicalTimeFactory.makeTime(7L);
  protected final HLAinteger64Time eight = logicalTimeFactory.makeTime(8L);
  protected final HLAinteger64Time nine = logicalTimeFactory.makeTime(9L);
  protected final HLAinteger64Time ten = logicalTimeFactory.makeTime(10L);
  protected final HLAinteger64Time fifteen = logicalTimeFactory.makeTime(15L);
  protected final HLAinteger64Time twenty = logicalTimeFactory.makeTime(20L);
  protected final HLAinteger64Time thirty = logicalTimeFactory.makeTime(30L);
  protected final HLAinteger64Time oneHundred = logicalTimeFactory.makeTime(100L);

  protected final HLAinteger64Interval lookahead1 = logicalTimeFactory.makeInterval(1L);
  protected final HLAinteger64Interval lookahead2 = logicalTimeFactory.makeInterval(2L);

  protected BaseTimeManagementTestNG(String federationName)
  {
    super(federationName);
  }

  protected BaseTimeManagementTestNG(int rtiAmbassadorCount, String federationName)
  {
    super(rtiAmbassadorCount, federationName);
  }

  @BeforeClass
  public void baseTimeSetup()
    throws Exception
  {
    connect();
    createFederationExecution(HLAinteger64TimeFactory.NAME);
    joinFederationExecution();
  }

  @AfterClass
  public void baseTimeTeardown()
    throws Exception
  {
    resignFederationExecution(ResignAction.UNCONDITIONALLY_DIVEST_ATTRIBUTES);
    destroyFederationExecution();
    disconnect();
  }

  protected TimeManagementFederateAmbassador createFederateAmbassador(RTIambassador rtiAmbassador)
  {
    return new TimeManagementFederateAmbassador(rtiAmbassador);
  }
}
