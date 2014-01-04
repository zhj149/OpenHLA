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

import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.MessageRetractionReturn;
import hla.rti1516e.OrderType;

@Test
public class NextMessageRequestAvailableTestNG
  extends BaseNextMessageRequestTestNG
{
  private static final String FEDERATION_NAME = NextMessageRequestAvailableTestNG.class.getSimpleName();

  public NextMessageRequestAvailableTestNG()
    throws Exception
  {
    super(FEDERATION_NAME);
  }

  @Test
  public void testNextMessageRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(0).nextMessageRequestAvailable(two);
    rtiAmbassadors.get(1).nextMessageRequestAvailable(five);
    rtiAmbassadors.get(2).nextMessageRequestAvailable(five);
    rtiAmbassadors.get(3).nextMessageRequestAvailable(five);
    rtiAmbassadors.get(4).nextMessageRequestAvailable(five);

    // federate 0 will be granted an advance to 2 because GALT is past 2
    //
    federateAmbassadors.get(0).checkTimeAdvanceGrant(two);

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

    federateAmbassadors.get(1).reset();
    federateAmbassadors.get(2).reset();

    // send another message at time 3
    //
    MessageRetractionReturn testInteractionMessageRetraction2 =
      rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle2, testParameterValues, TAG, three);
    assert testInteractionMessageRetraction2.retractionHandleIsValid;
    MessageRetractionHandle testInteractionMessageRetractionHandle2 = testInteractionMessageRetraction2.handle;

    // advance federate 0 to 3
    //
    rtiAmbassadors.get(0).nextMessageRequestAvailable(three);

    // try to go to 5 again
    //
    rtiAmbassadors.get(1).nextMessageRequestAvailable(five);
    rtiAmbassadors.get(2).nextMessageRequestAvailable(five);

    // federates 1 and 2 will be re-granted an advance to 3 because that is their next message time
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

    // advance federates 1 and 2 to 5
    //
    rtiAmbassadors.get(1).nextMessageRequestAvailable(five);
    rtiAmbassadors.get(2).nextMessageRequestAvailable(five);

    // federate 0 will be granted an advance to 3 because GALT is past 3
    //
    federateAmbassadors.get(0).checkTimeAdvanceGrant(three);

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

    federateAmbassadors.get(3).reset();
    federateAmbassadors.get(4).reset();

    // send another message at time 4
    //
    MessageRetractionReturn testInteractionMessageRetraction3 =
      rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle3, testParameterValues, TAG, four);
    MessageRetractionHandle testInteractionMessageRetractionHandle3 = testInteractionMessageRetraction3.handle;

    // advance federate 0 to 4
    //
    rtiAmbassadors.get(0).nextMessageRequestAvailable(four);

    // federate 0 will be granted an advance to 4 because GALT is past 4
    //
    federateAmbassadors.get(0).checkTimeAdvanceGrant(four);

    // try to go to 5 again
    //
    rtiAmbassadors.get(3).nextMessageRequestAvailable(five);
    rtiAmbassadors.get(4).nextMessageRequestAvailable(five);

    // federates 3 and 4 will be re-granted an advance to 4 because that is their next message time
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

    // advance federates 0, 3 and 4 to 5
    //
    rtiAmbassadors.get(0).nextMessageRequestAvailable(five);
    rtiAmbassadors.get(3).nextMessageRequestAvailable(five);
    rtiAmbassadors.get(4).nextMessageRequestAvailable(five);

    // all should be at 5 now
    //
    federateAmbassadors.get(0).checkTimeAdvanceGrant(five);
    federateAmbassadors.get(1).checkTimeAdvanceGrant(five);
    federateAmbassadors.get(2).checkTimeAdvanceGrant(five);
    federateAmbassadors.get(3).checkTimeAdvanceGrant(five);
    federateAmbassadors.get(4).checkTimeAdvanceGrant(five);
  }
}
