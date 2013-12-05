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

package net.sf.ohla.rti.testsuite.hla.rti1516.support;

import org.testng.annotations.Test;

import hla.rti1516.InvalidTransportationName;
import hla.rti1516.InvalidTransportationType;
import hla.rti1516.TransportationType;

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
    assert TransportationType.HLA_RELIABLE == rtiAmbassadors.get(0).getTransportationType(HLA_RELIABLE);
    assert HLA_RELIABLE.equals(rtiAmbassadors.get(0).getTransportationName(TransportationType.HLA_RELIABLE));
  }

  @Test
  public void testGetBestEffortTransportationTypeHandleAndName()
    throws Exception
  {
    assert TransportationType.HLA_BEST_EFFORT == rtiAmbassadors.get(0).getTransportationType(HLA_BEST_EFFORT);
    assert HLA_BEST_EFFORT.equals(rtiAmbassadors.get(0).getTransportationName(TransportationType.HLA_BEST_EFFORT));
  }

  @Test(expectedExceptions = {InvalidTransportationName.class})
  public void testGetTransportationTypeOfUnknownTransportationType()
    throws Exception
  {
    rtiAmbassadors.get(0).getTransportationType(UNKNOWN_TRANSPORTATION_TYPE);
  }

  @Test(expectedExceptions = {InvalidTransportationName.class})
  public void testGetTransportationTypeOfNullTransportationType()
    throws Exception
  {
    rtiAmbassadors.get(0).getTransportationType(null);
  }

  @Test(expectedExceptions = {InvalidTransportationType.class})
  public void testGetTransportationTypeNameOfNullTransportationTypeHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getTransportationName(null);
  }
}
