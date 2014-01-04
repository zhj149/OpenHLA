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

import hla.rti.InvalidTransportationHandle;
import hla.rti.NameNotFound;

@Test
public class TransportationSupportTestNG
  extends BaseSupportTestNG
{
  private static final String FEDERATION_NAME = TransportationSupportTestNG.class.getSimpleName();

  public TransportationSupportTestNG()
  {
    super(FEDERATION_NAME);
  }

  @Test
  public void testGetReliableTransportationTypeHandleAndName()
    throws Exception
  {
    int reliableTransportationHandle = rtiAmbassadors.get(0).getTransportationHandle(HLA_RELIABLE);
    assert HLA_RELIABLE.equals(rtiAmbassadors.get(0).getTransportationName(reliableTransportationHandle));
  }

  @Test
  public void testGetBestEffortTransportationTypeHandleAndName()
    throws Exception
  {
    int bestEffortTransportationHandle = rtiAmbassadors.get(0).getTransportationHandle(HLA_BEST_EFFORT);
    assert HLA_BEST_EFFORT.equals(rtiAmbassadors.get(0).getTransportationName(bestEffortTransportationHandle));
  }

  @Test(expectedExceptions = NameNotFound.class)
  public void testGetTransportationHandleOfUnknownTransportationType()
    throws Exception
  {
    rtiAmbassadors.get(0).getTransportationHandle(UNKNOWN_TRANSPORTATION_TYPE);
  }

  @Test(expectedExceptions = NameNotFound.class)
  public void testGetTransportationHandleWithNullTransportationType()
    throws Exception
  {
    rtiAmbassadors.get(0).getTransportationHandle(null);
  }

  @Test(expectedExceptions = InvalidTransportationHandle.class)
  public void testGetTransportationNameOfInvalidTransportationHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getTransportationName(Integer.MIN_VALUE);
  }
}
