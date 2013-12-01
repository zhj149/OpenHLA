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

import org.testng.annotations.Test;

import hla.rti1516e.OrderType;

@Test
public class NextMessageRequestTestNG
  extends BaseNextMessageRequestTestNG
{
  private static final String FEDERATION_NAME = NextMessageRequestTestNG.class.getSimpleName();

  public NextMessageRequestTestNG()
    throws Exception
  {
    super(FEDERATION_NAME);
  }

  @Test
  public void testNextMessageRequest()
    throws Exception
  {
    rtiAmbassadors.get(0).nextMessageRequest(five);
    rtiAmbassadors.get(1).nextMessageRequest(five);
    rtiAmbassadors.get(2).nextMessageRequest(five);
    rtiAmbassadors.get(3).nextMessageRequest(five);
    rtiAmbassadors.get(4).nextMessageRequest(five);

    // federates 1 and 2 will be granted an advance to 3 because that is their next message time
    //
    federateAmbassadors.get(1).checkTimeAdvanceGrant(three);
    federateAmbassadors.get(2).checkTimeAdvanceGrant(three);

    // verify their received interactions
    //
    federateAmbassadors.get(1).checkParameterValues(
      testInteractionClassHandle2, testParameterValues, TAG, OrderType.TIMESTAMP, reliableTransportationTypeHandle,
      three, OrderType.TIMESTAMP, testInteractionMessageRetractionHandle2, federateHandles.get(0));
    federateAmbassadors.get(2).checkParameterValues(
      testInteractionClassHandle2, testParameterValues, TAG, OrderType.TIMESTAMP, reliableTransportationTypeHandle,
      three, OrderType.TIMESTAMP, testInteractionMessageRetractionHandle2, federateHandles.get(0));

    // federates 3 and 4 will be granted an advance to 4 because when federates 1 and 2 go to 3, their LOTS is
    // 5 (time + lookahead (3 + 2)) which allows federates 3 and 4 to receive their next message
    //
    federateAmbassadors.get(3).checkTimeAdvanceGrant(four);
    federateAmbassadors.get(4).checkTimeAdvanceGrant(four);

    // verify their received interactions
    //
    federateAmbassadors.get(3).checkParameterValues(
      testInteractionClassHandle3, testParameterValues, TAG, OrderType.TIMESTAMP, reliableTransportationTypeHandle,
      four, OrderType.TIMESTAMP, testInteractionMessageRetractionHandle3, federateHandles.get(0));
    federateAmbassadors.get(4).checkParameterValues(
      testInteractionClassHandle3, testParameterValues, TAG, OrderType.TIMESTAMP, reliableTransportationTypeHandle,
      four, OrderType.TIMESTAMP, testInteractionMessageRetractionHandle3, federateHandles.get(0));

    // advance everyone to 5
    //
    rtiAmbassadors.get(1).nextMessageRequest(five);
    rtiAmbassadors.get(2).nextMessageRequest(five);
    rtiAmbassadors.get(3).nextMessageRequest(five);
    rtiAmbassadors.get(4).nextMessageRequest(five);

    // all should be at 5 now
    //
    federateAmbassadors.get(0).checkTimeAdvanceGrant(five);
    federateAmbassadors.get(1).checkTimeAdvanceGrant(five);
    federateAmbassadors.get(2).checkTimeAdvanceGrant(five);
    federateAmbassadors.get(3).checkTimeAdvanceGrant(five);
    federateAmbassadors.get(4).checkTimeAdvanceGrant(five);
  }
}
