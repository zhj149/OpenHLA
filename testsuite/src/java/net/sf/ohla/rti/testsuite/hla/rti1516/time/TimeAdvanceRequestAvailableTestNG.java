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

package net.sf.ohla.rti.testsuite.hla.rti1516.time;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516.LogicalTimeAlreadyPassed;
import hla.rti1516.OrderType;
import hla.rti1516.TransportationType;

@Test
public class TimeAdvanceRequestAvailableTestNG
  extends BaseTimeAdvanceRequestTestNG
{
  private static final String FEDERATION_NAME = TimeAdvanceRequestAvailableTestNG.class.getSimpleName();

  public TimeAdvanceRequestAvailableTestNG()
  {
    super(FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle, testParameterValues, TAG, five);
  }

  @Test
  public void testTimeAdvanceRequestAvailable()
    throws Exception
  {
    // advance federate 0 to 4, his LOTS will be 5 which will release the first message
    //
    rtiAmbassadors.get(0).timeAdvanceRequest(four);

    // advance federate 1 to 5
    //
    rtiAmbassadors.get(1).timeAdvanceRequestAvailable(five);

    // the first message should be received
    //
    federateAmbassadors.get(1).checkParameterValues(
      testInteractionClassHandle, testParameterValues, TAG, OrderType.TIMESTAMP, TransportationType.HLA_RELIABLE,
      five, OrderType.TIMESTAMP, testInteractionMessageRetractionHandle);

    federateAmbassadors.get(0).checkTimeAdvanceGrant(four);
    federateAmbassadors.get(1).checkTimeAdvanceGrant(five);

    // send another message at 5
    //
    rtiAmbassadors.get(0).updateAttributeValues(testObjectInstanceHandle, testAttributeValues, TAG, five);

    rtiAmbassadors.get(0).timeAdvanceRequest(ten);
    rtiAmbassadors.get(1).timeAdvanceRequest(ten);

    // the second message should be received
    //
    federateAmbassadors.get(1).checkAttributeValues(testObjectInstanceHandle, testAttributeValues, five);

    federateAmbassadors.get(0).checkTimeAdvanceGrant(ten);
    federateAmbassadors.get(1).checkTimeAdvanceGrant(ten);
  }

  @Test(dependsOnMethods = { "testTimeAdvanceRequestAvailable" }, expectedExceptions = { LogicalTimeAlreadyPassed.class })
  public void testTimeAdvanceRequestAvailableToLogicalTimeAlreadyPassed()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequestAvailable(five);
  }

  @Test(dependsOnMethods = { "testTimeAdvanceRequestAvailable" })
  public void testTimeAdvanceRequestAvailableToSameTime()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequestAvailable(ten);
    federateAmbassadors.get(0).checkTimeAdvanceGrant(ten);

    assert ten.equals(rtiAmbassadors.get(0).queryLogicalTime());
  }
}
