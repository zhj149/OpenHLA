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
import hla.rti1516e.OrderType;
import hla.rti1516e.ResignAction;
import hla.rti1516e.exceptions.InvalidOrderName;
import hla.rti1516e.exceptions.InvalidOrderType;

@Test
public class OrderSupportTestNG
  extends BaseSupportTestNG
{
  @Test
  public void testGetReceiveOrderTypeAndName()
    throws Exception
  {
    OrderType orderType = rtiAmbassadors.get(0).getOrderType(RECEIVE);
    assert OrderType.RECEIVE == orderType;
    assert RECEIVE.equals(rtiAmbassadors.get(0).getOrderName(orderType));
  }

  @Test
  public void testGetTimestampOrderTypeAndName()
    throws Exception
  {
    OrderType orderType = rtiAmbassadors.get(0).getOrderType(TIMESTAMP);
    assert OrderType.TIMESTAMP == orderType;
    assert TIMESTAMP.equals(rtiAmbassadors.get(0).getOrderName(orderType));
  }

  @Test(expectedExceptions = {InvalidOrderName.class})
  public void testGetOrderTypeOfUnknownOrderName()
    throws Exception
  {
    rtiAmbassadors.get(0).getOrderType(UNKNOWN_ORDER_TYPE);
  }

  @Test(expectedExceptions = {InvalidOrderName.class})
  public void testGetOrderTypeOfNullOrderName()
    throws Exception
  {
    rtiAmbassadors.get(0).getOrderType(null);
  }

  @Test(expectedExceptions = {InvalidOrderType.class})
  public void testGetOrderNameOfNullOrderType()
    throws Exception
  {
    rtiAmbassadors.get(0).getOrderName(null);
  }
}