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

package net.sf.ohla.rti.testsuite.hla.rti.time;

import java.util.ArrayList;
import java.util.List;

import net.sf.ohla.rti.hla.rti.HLA13RTIambassador;
import net.sf.ohla.rti.hla.rti.Integer64Time;
import net.sf.ohla.rti.hla.rti.Integer64TimeInterval;
import net.sf.ohla.rti.testsuite.hla.rti.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import hla.rti.LogicalTime;
import hla.rti.LogicalTimeInterval;
import hla.rti.ResignAction;
import hla.rti.jlc.RTIambassadorEx;

import hla.rti1516e.time.HLAinteger64TimeFactory;

public class BaseTimeManagementTestNG
  extends BaseTestNG
{
  protected final String federationName;

  protected final List<Integer> federateHandles;
  protected final List<TimeManagementFederateAmbassador> federateAmbassadors;

  protected final LogicalTime initial;
  protected final LogicalTime infinity;

  protected final LogicalTime two = new Integer64Time(2L);
  protected final LogicalTime three = new Integer64Time(3L);
  protected final LogicalTime four = new Integer64Time(4L);
  protected final LogicalTime five = new Integer64Time(5L);
  protected final LogicalTime six = new Integer64Time(6L);
  protected final LogicalTime seven = new Integer64Time(7L);
  protected final LogicalTime eight = new Integer64Time(8L);
  protected final LogicalTime nine = new Integer64Time(9L);
  protected final LogicalTime ten = new Integer64Time(10L);
  protected final LogicalTime fifteen = new Integer64Time(15L);
  protected final LogicalTime twenty = new Integer64Time(20L);
  protected final LogicalTime thirty = new Integer64Time(30L);
  protected final LogicalTime oneHundred = new Integer64Time(100L);

  protected final LogicalTimeInterval lookahead1 = new Integer64TimeInterval(1L);
  protected final LogicalTimeInterval lookahead2 = new Integer64TimeInterval(2L);

  protected BaseTimeManagementTestNG(String federationName)
  {
    this.federationName = federationName;

    federateHandles = new ArrayList<Integer>(rtiAmbassadorCount);
    federateAmbassadors = new ArrayList<TimeManagementFederateAmbassador>(rtiAmbassadorCount);

    initial = mobileFederateServices._timeFactory.makeInitial();

    infinity = mobileFederateServices._timeFactory.makeInitial();
    infinity.setFinal();

    System.setProperty(String.format(
      HLA13RTIambassador.OHLA_HLA13_FEDERATION_EXECUTION_LOGICAL_TIME_IMPLEMENTATION_PROPERTY, federationName),
                       HLAinteger64TimeFactory.NAME);
  }

  protected BaseTimeManagementTestNG(int rtiAmbassadorCount, String federationName)
  {
    super(rtiAmbassadorCount);

    this.federationName = federationName;

    federateHandles = new ArrayList<Integer>(rtiAmbassadorCount);
    federateAmbassadors = new ArrayList<TimeManagementFederateAmbassador>(rtiAmbassadorCount);

    initial = mobileFederateServices._timeFactory.makeInitial();

    infinity = mobileFederateServices._timeFactory.makeInitial();
    infinity.setFinal();

    System.setProperty(String.format(
      HLA13RTIambassador.OHLA_HLA13_FEDERATION_EXECUTION_LOGICAL_TIME_IMPLEMENTATION_PROPERTY, federationName),
                       HLAinteger64TimeFactory.NAME);
  }

  @BeforeClass
  public void baseTimeSetup()
    throws Exception
  {
    for (RTIambassadorEx rtiAmbassador : rtiAmbassadors)
    {
      TimeManagementFederateAmbassador federateAmbassador = new TimeManagementFederateAmbassador(rtiAmbassador);
      federateAmbassadors.add(federateAmbassador);
    }

    rtiAmbassadors.get(0).createFederationExecution(federationName, fed);

    for (int i = 0; i < rtiAmbassadorCount; i++)
    {
      federateHandles.add(rtiAmbassadors.get(i).joinFederationExecution(
        FEDERATE_TYPE, federationName, federateAmbassadors.get(i), mobileFederateServices));
    }
  }

  @AfterClass
  public void baseTimeTeardown()
    throws Exception
  {
    resignFederationExecution(ResignAction.RELEASE_ATTRIBUTES);

    destroyFederationExecution(federationName);
  }
}
