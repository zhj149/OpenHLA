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

package net.sf.ohla.rti.testsuite.hla.rti.support;

import org.testng.annotations.Test;

import hla.rti.InvalidOrderingHandle;
import hla.rti.NameNotFound;

@Test
public class OrderSupportTestNG
  extends BaseSupportTestNG
{
  private static final String FEDERATION_NAME = "OHLA HLA 1.3 Order Support Test Federation";

  public OrderSupportTestNG()
  {
    super(FEDERATION_NAME);
  }

  @Test
  public void testGetReceiveOrderingHandleAndName()
    throws Exception
  {
    int receiveOrderingHandle = rtiAmbassadors.get(0).getOrderingHandle(RECEIVE);
    assert RECEIVE.equals(rtiAmbassadors.get(0).getOrderingName(receiveOrderingHandle));
  }

  @Test
  public void testGetTimestampOrderTypeAndName()
    throws Exception
  {
    int receiveOrderingHandle = rtiAmbassadors.get(0).getOrderingHandle(TIMESTAMP);
    assert TIMESTAMP.equals(rtiAmbassadors.get(0).getOrderingName(receiveOrderingHandle));
  }

  @Test(expectedExceptions = {NameNotFound.class})
  public void testGetOrderingHandleWithNullOrderingName()
    throws Exception
  {
    rtiAmbassadors.get(0).getOrderingHandle(null);
  }

  @Test(expectedExceptions = {NameNotFound.class})
  public void testGetOrderingHandleOfUnknownOrderingName()
    throws Exception
  {
    rtiAmbassadors.get(0).getOrderingHandle(UNKNOWN_ORDER_TYPE);
  }

  @Test(expectedExceptions = {InvalidOrderingHandle.class})
  public void testGetOrderingNameOfInvalidOrderingHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getOrderingName(Integer.MIN_VALUE);
  }
}
