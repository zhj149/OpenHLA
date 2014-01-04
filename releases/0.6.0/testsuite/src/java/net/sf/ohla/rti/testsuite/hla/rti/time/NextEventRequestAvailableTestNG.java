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

import org.testng.annotations.Test;

import hla.rti.EventRetractionHandle;

@Test
public class NextEventRequestAvailableTestNG
  extends BaseNextEventRequestTestNG
{
  private static final String FEDERATION_NAME = NextEventRequestAvailableTestNG.class.getSimpleName();

  public NextEventRequestAvailableTestNG()
    throws Exception
  {
    super(FEDERATION_NAME);
  }

  @Test
  public void testNextEventRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(0).nextEventRequestAvailable(two);
    rtiAmbassadors.get(1).nextEventRequestAvailable(five);
    rtiAmbassadors.get(2).nextEventRequestAvailable(five);
    rtiAmbassadors.get(3).nextEventRequestAvailable(five);
    rtiAmbassadors.get(4).nextEventRequestAvailable(five);

    // federate 0 will be granted an advance to 2 because GALT is past 2
    //
    federateAmbassadors.get(0).checkTimeAdvanceGrant(two);

    // federates 1 and 2 will be granted an advance to 3 because that is their next message time
    //
    federateAmbassadors.get(1).checkTimeAdvanceGrant(three);
    federateAmbassadors.get(2).checkTimeAdvanceGrant(three);

    // verify their received interactions
    //
    federateAmbassadors.get(1).checkReceivedInteraction(testParameterValues, three);
    federateAmbassadors.get(2).checkReceivedInteraction(testParameterValues, three);

    federateAmbassadors.get(1).reset();
    federateAmbassadors.get(2).reset();

    // send another message at time 3
    //
    EventRetractionHandle testEventRetractionHandle2 =
      rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle2, testParameterValues, TAG, three);
    assert testEventRetractionHandle2 != null;

    // advance federate 0 to 3
    //
    rtiAmbassadors.get(0).nextEventRequestAvailable(three);

    // try to go to 5 again
    //
    rtiAmbassadors.get(1).nextEventRequestAvailable(five);
    rtiAmbassadors.get(2).nextEventRequestAvailable(five);

    // federates 1 and 2 will be re-granted an advance to 3 because that is their next message time
    //
    federateAmbassadors.get(1).checkTimeAdvanceGrant(three);
    federateAmbassadors.get(2).checkTimeAdvanceGrant(three);

    // verify their received interactions
    //
    federateAmbassadors.get(1).checkReceivedInteraction(testParameterValues, three);
    federateAmbassadors.get(2).checkReceivedInteraction(testParameterValues, three);

    // advance federates 1 and 2 to 5
    //
    rtiAmbassadors.get(1).nextEventRequestAvailable(five);
    rtiAmbassadors.get(2).nextEventRequestAvailable(five);

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
    federateAmbassadors.get(3).checkReceivedInteraction(testParameterValues, four);
    federateAmbassadors.get(4).checkReceivedInteraction(testParameterValues, four);

    federateAmbassadors.get(3).reset();
    federateAmbassadors.get(4).reset();

    // send another message at time 4
    //
    rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle3, testParameterValues, TAG, four);

    // advance federate 0 to 4
    //
    rtiAmbassadors.get(0).nextEventRequestAvailable(four);

    // federate 0 will be granted an advance to 4 because GALT is past 4
    //
    federateAmbassadors.get(0).checkTimeAdvanceGrant(four);

    // try to go to 5 again
    //
    rtiAmbassadors.get(3).nextEventRequestAvailable(five);
    rtiAmbassadors.get(4).nextEventRequestAvailable(five);

    // federates 3 and 4 will be re-granted an advance to 4 because that is their next message time
    //
    federateAmbassadors.get(3).checkTimeAdvanceGrant(four);
    federateAmbassadors.get(4).checkTimeAdvanceGrant(four);

    // verify their received interactions
    //
    federateAmbassadors.get(3).checkReceivedInteraction(testParameterValues, four);
    federateAmbassadors.get(4).checkReceivedInteraction(testParameterValues, four);

    // advance federates 0, 3 and 4 to 5
    //
    rtiAmbassadors.get(0).nextEventRequestAvailable(five);
    rtiAmbassadors.get(3).nextEventRequestAvailable(five);
    rtiAmbassadors.get(4).nextEventRequestAvailable(five);

    // all should be at 5 now
    //
    federateAmbassadors.get(0).checkTimeAdvanceGrant(five);
    federateAmbassadors.get(1).checkTimeAdvanceGrant(five);
    federateAmbassadors.get(2).checkTimeAdvanceGrant(five);
    federateAmbassadors.get(3).checkTimeAdvanceGrant(five);
    federateAmbassadors.get(4).checkTimeAdvanceGrant(five);
  }
}
