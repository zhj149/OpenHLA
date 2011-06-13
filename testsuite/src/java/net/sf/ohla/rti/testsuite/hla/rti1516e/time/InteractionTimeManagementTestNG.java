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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;

@Test
public class InteractionTimeManagementTestNG
  extends BaseTimeManagementTestNG
{
  private static final String FEDERATION_NAME = "OHLA IEEE 1516e Interaction Time Management Test Federation";

  private InteractionClassHandle testInteractionClassHandle;
  private ParameterHandle parameterHandle1;
  private ParameterHandle parameterHandle2;
  private ParameterHandle parameterHandle3;

  public InteractionTimeManagementTestNG()
    throws Exception
  {
    super(3, FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    testInteractionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);

    parameterHandle1 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle, PARAMETER1);
    parameterHandle2 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle, PARAMETER2);
    parameterHandle3 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle, PARAMETER3);

    rtiAmbassadors.get(0).publishInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(0).subscribeInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(1).publishInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(1).subscribeInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(2).publishInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(2).subscribeInteractionClass(testInteractionClassHandle);

    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);
    rtiAmbassadors.get(2).enableTimeRegulation(lookahead1);

    federateAmbassadors.get(0).checkTimeRegulationEnabled(initial);
    federateAmbassadors.get(2).checkTimeRegulationEnabled(initial);

    rtiAmbassadors.get(1).enableTimeConstrained();
    rtiAmbassadors.get(2).enableTimeConstrained();

    federateAmbassadors.get(1).checkTimeConstrainedEnabled(initial);
    federateAmbassadors.get(2).checkTimeConstrainedEnabled(initial);

    setupComplete(federateAmbassadors);
  }

  @Test
  public void testSendInteractionWhileNotTimeAdvancing()
    throws Exception
  {
    ParameterHandleValueMap parameterValues = rtiAmbassadors.get(0).getParameterHandleValueMapFactory().create(1);
    parameterValues.put(parameterHandle1, PARAMETER1_VALUE.getBytes());
    parameterValues.put(parameterHandle2, PARAMETER2_VALUE.getBytes());
    parameterValues.put(parameterHandle3, PARAMETER3_VALUE.getBytes());

    rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle, parameterValues, TAG);

    // the 2 constrained federates will not receive it because they do not have asynchronous delivery enabled and are
    // not in the time advancing state
    //
    federateAmbassadors.get(1).checkParameterValuesNotReceived();
    federateAmbassadors.get(2).checkParameterValuesNotReceived();

    // advance constrained federates so they will be sure to receive the update (because they will be waiting for the
    // remaining regulating federate to advance)
    //
    rtiAmbassadors.get(1).timeAdvanceRequest(ten);
    rtiAmbassadors.get(2).timeAdvanceRequest(ten);

    // parameter values should have been released
    //
    federateAmbassadors.get(1).checkParameterValues(parameterValues, null);
    federateAmbassadors.get(2).checkParameterValues(parameterValues, null);

    // finish time advance
    //
    rtiAmbassadors.get(0).timeAdvanceRequest(ten);

    federateAmbassadors.get(0).checkTimeAdvanceGrant(ten);
    federateAmbassadors.get(1).checkTimeAdvanceGrant(ten);
    federateAmbassadors.get(2).checkTimeAdvanceGrant(ten);
  }

  @Test(dependsOnMethods = {"testSendInteractionWhileNotTimeAdvancing"})
  public void testSendInteractionInFuture()
    throws Exception
  {
    ParameterHandleValueMap parameterValues = rtiAmbassadors.get(0).getParameterHandleValueMapFactory().create(1);
    parameterValues.put(parameterHandle1, PARAMETER1_VALUE.getBytes());
    parameterValues.put(parameterHandle2, PARAMETER2_VALUE.getBytes());
    parameterValues.put(parameterHandle3, PARAMETER3_VALUE.getBytes());

    rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle, parameterValues, TAG, twenty);

    // request advance by regulating-only federate
    //
    rtiAmbassadors.get(0).timeAdvanceRequest(twenty);
    federateAmbassadors.get(0).checkTimeAdvanceGrant(twenty);

    // advance constrained federates
    //
    rtiAmbassadors.get(1).timeAdvanceRequest(fifteen);
    rtiAmbassadors.get(2).timeAdvanceRequest(fifteen);

    federateAmbassadors.get(1).checkTimeAdvanceGrant(fifteen);
    federateAmbassadors.get(2).checkTimeAdvanceGrant(fifteen);

    // the 2 constrained federates will not receive it because they have not yet advanced past 20
    //
    federateAmbassadors.get(1).checkParameterValuesNotReceived();
    federateAmbassadors.get(2).checkParameterValuesNotReceived();

    // advance constrained federates
    //
    rtiAmbassadors.get(1).timeAdvanceRequest(twenty);
    rtiAmbassadors.get(2).timeAdvanceRequest(twenty);

    // parameter values should have been released
    //
    federateAmbassadors.get(1).checkParameterValues(parameterValues, twenty);
    federateAmbassadors.get(2).checkParameterValues(parameterValues, twenty);

    federateAmbassadors.get(1).checkTimeAdvanceGrant(twenty);
    federateAmbassadors.get(2).checkTimeAdvanceGrant(twenty);
  }
}
