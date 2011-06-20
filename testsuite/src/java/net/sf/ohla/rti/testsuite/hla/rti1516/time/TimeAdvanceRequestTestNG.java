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

import hla.rti1516.InteractionClassHandle;
import hla.rti1516.ParameterHandle;
import hla.rti1516.ParameterHandleValueMap;

@Test
public class TimeAdvanceRequestTestNG
  extends BaseTimeManagementTestNG
{
  private static final String FEDERATION_NAME = "OHLA IEEE 1516 Time Advance Request Test Federation";

  private ParameterHandleValueMap testParameterValues;

  public TimeAdvanceRequestTestNG()
    throws Exception
  {
    super(2, FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);
    rtiAmbassadors.get(1).enableTimeRegulation(lookahead1);

    federateAmbassadors.get(0).checkTimeRegulationEnabled(initial);
    federateAmbassadors.get(1).checkTimeRegulationEnabled(initial);

    rtiAmbassadors.get(0).enableTimeConstrained();
    rtiAmbassadors.get(1).enableTimeConstrained();

    federateAmbassadors.get(0).checkTimeConstrainedEnabled(initial);
    federateAmbassadors.get(1).checkTimeConstrainedEnabled(initial);

    InteractionClassHandle testInteractionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);

    ParameterHandle parameterHandle1 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle, PARAMETER1);
    ParameterHandle parameterHandle2 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle, PARAMETER2);
    ParameterHandle parameterHandle3 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle, PARAMETER3);

    testParameterValues = rtiAmbassadors.get(0).getParameterHandleValueMapFactory().create(3);
    testParameterValues.put(parameterHandle1, PARAMETER1_VALUE.getBytes());
    testParameterValues.put(parameterHandle2, PARAMETER2_VALUE.getBytes());
    testParameterValues.put(parameterHandle3, PARAMETER3_VALUE.getBytes());

    rtiAmbassadors.get(0).publishInteractionClass(testInteractionClassHandle);

    rtiAmbassadors.get(1).subscribeInteractionClass(testInteractionClassHandle);

    setupComplete(federateAmbassadors);

    rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle, testParameterValues, TAG, five);
    rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle, testParameterValues, TAG, ten);
  }

  @Test
  public void testTimeAdvanceRequest()
    throws Exception
  {
    // advance federate 0 to 10, this will make both messages available to federate 1
    //
    rtiAmbassadors.get(0).timeAdvanceRequest(ten);

    // advance federate 1 to 4
    //
    rtiAmbassadors.get(1).timeAdvanceRequest(four);
    federateAmbassadors.get(1).checkTimeAdvanceGrant(four);

    // nothing should be received yet
    //
    federateAmbassadors.get(1).checkParameterValuesNotReceived();

    // advance federate 1 to 5
    //
    rtiAmbassadors.get(1).timeAdvanceRequest(five);

    // the first message should be received
    //
    federateAmbassadors.get(1).checkParameterValues(testParameterValues, five);

    federateAmbassadors.get(1).checkTimeAdvanceGrant(five);

    // advance federate 1 to 4
    //
    rtiAmbassadors.get(1).timeAdvanceRequest(nine);
    federateAmbassadors.get(1).checkTimeAdvanceGrant(nine);

    // nothing should be received yet
    //
    federateAmbassadors.get(1).checkParameterValuesNotReceived();

    // advance federate 1 to 10
    //
    rtiAmbassadors.get(1).timeAdvanceRequest(ten);

    // the second message should be received
    //
    federateAmbassadors.get(1).checkParameterValues(testParameterValues, ten);

    federateAmbassadors.get(0).checkTimeAdvanceGrant(ten);
    federateAmbassadors.get(1).checkTimeAdvanceGrant(ten);
  }
}
