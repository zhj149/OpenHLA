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
public class FlushQueueRequestTestNG
  extends BaseTimeManagementTestNG
{
  private static final String FEDERATION_NAME = FlushQueueRequestTestNG.class.getSimpleName();

  private ParameterHandleValueMap testParameterValues;

  public FlushQueueRequestTestNG()
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

    synchronize(SYNCHRONIZATION_POINT_SETUP_COMPLETE, federateAmbassadors);

    rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle, testParameterValues, TAG, five);
    rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle, testParameterValues, TAG, ten);
  }

  @Test
  public void testFlushQueueRequest()
    throws Exception
  {
    rtiAmbassadors.get(1).flushQueueRequest(initial);

    federateAmbassadors.get(1).checkParameterValues(testParameterValues, five);
    federateAmbassadors.get(1).checkParameterValues(testParameterValues, ten);

    federateAmbassadors.get(1).checkTimeAdvanceGrant(initial);
  }
}
