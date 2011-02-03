/*
 * Copyright (c) 2005-2011, Michael Newcomb
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

package net.sf.ohla.rti1516e.support;

import net.sf.ohla.rti1516e.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.CallbackModel;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.InvalidTransportationName;
import hla.rti1516e.exceptions.InvalidTransportationType;

@Test
public class TransportationSupportTestNG
  extends BaseTestNG
{
  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).connect(new NullFederateAmbassador(), CallbackModel.HLA_EVOKED);
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);
    rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(0).destroyFederationExecution(FEDERATION_NAME);
    rtiAmbassadors.get(0).disconnect();
  }

  @Test
  public void testGetReliableTransportationTypeHandleAndName()
    throws Exception
  {
    TransportationTypeHandle transportationTypeHandle = rtiAmbassadors.get(0).getTransportationTypeHandle(HLA_RELIABLE);
    assert transportationTypeHandle != null;
    assert HLA_RELIABLE.equals(rtiAmbassadors.get(0).getTransportationTypeName(transportationTypeHandle));
  }

  @Test
  public void testGetBestEffortTransportationTypeHandleAndName()
    throws Exception
  {
    TransportationTypeHandle transportationTypeHandle =
      rtiAmbassadors.get(0).getTransportationTypeHandle(HLA_BEST_EFFORT);
    assert transportationTypeHandle != null;
    assert HLA_BEST_EFFORT.equals(rtiAmbassadors.get(0).getTransportationTypeName(transportationTypeHandle));
  }

  @Test(expectedExceptions = {InvalidTransportationName.class})
  public void testGetTransportationTypeOfUnknownTransportationType()
    throws Exception
  {
    rtiAmbassadors.get(0).getTransportationTypeHandle(UNKNOWN_TRANSPORTATION_TYPE);
  }

  @Test(expectedExceptions = {InvalidTransportationName.class})
  public void testGetTransportationTypeOfNullTransportationType()
    throws Exception
  {
    rtiAmbassadors.get(0).getTransportationTypeHandle(null);
  }

  @Test(expectedExceptions = {InvalidTransportationType.class})
  public void testGetTransportationTypeNameOfNullTransportationTypeHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getTransportationTypeName(null);
  }
}
