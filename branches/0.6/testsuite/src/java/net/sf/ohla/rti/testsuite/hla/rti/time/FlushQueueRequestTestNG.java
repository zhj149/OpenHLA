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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.EventRetractionHandle;
import hla.rti.SuppliedParameters;

@Test
public class FlushQueueRequestTestNG
  extends BaseTimeManagementTestNG
{
  private static final String FEDERATION_NAME = FlushQueueRequestTestNG.class.getSimpleName();

  private int testInteractionClassHandle;
  private SuppliedParameters testSuppliedParameters;

  private EventRetractionHandle testInteractionEventRetractionHandle1;
  private EventRetractionHandle testInteractionEventRetractionHandle2;

  public FlushQueueRequestTestNG()
  {
    super(2, FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(initial, lookahead1);
    rtiAmbassadors.get(1).enableTimeRegulation(initial, lookahead1);

    federateAmbassadors.get(0).checkTimeRegulationEnabled(initial);
    federateAmbassadors.get(1).checkTimeRegulationEnabled(initial);

    rtiAmbassadors.get(0).enableTimeConstrained();
    rtiAmbassadors.get(1).enableTimeConstrained();

    federateAmbassadors.get(0).checkTimeConstrainedEnabled(initial);
    federateAmbassadors.get(1).checkTimeConstrainedEnabled(initial);

    testInteractionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);

    int parameterHandle1 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER1, testInteractionClassHandle);
    int parameterHandle2 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER2, testInteractionClassHandle);
    int parameterHandle3 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER3, testInteractionClassHandle);

    testSuppliedParameters = rtiFactory.createSuppliedParameters();
    testSuppliedParameters.add(parameterHandle1, PARAMETER1_VALUE.getBytes());
    testSuppliedParameters.add(parameterHandle2, PARAMETER2_VALUE.getBytes());
    testSuppliedParameters.add(parameterHandle3, PARAMETER3_VALUE.getBytes());

    rtiAmbassadors.get(0).publishInteractionClass(testInteractionClassHandle);

    rtiAmbassadors.get(1).subscribeInteractionClass(testInteractionClassHandle);

    synchronize(SYNCHRONIZATION_POINT_SETUP_COMPLETE, federateAmbassadors);

    testInteractionEventRetractionHandle1 =
      rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle, testSuppliedParameters, TAG, five);
    assert testInteractionEventRetractionHandle1 != null;
    testInteractionEventRetractionHandle2 =
      rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle, testSuppliedParameters, TAG, ten);
    assert testInteractionEventRetractionHandle2 != null;
  }

  @Test
  public void testFlushQueueRequest()
    throws Exception
  {
    rtiAmbassadors.get(1).flushQueueRequest(initial);

    federateAmbassadors.get(1).checkReceivedInteraction(testSuppliedParameters, five);
    federateAmbassadors.get(1).checkReceivedInteraction(testSuppliedParameters, ten);

    federateAmbassadors.get(1).checkTimeAdvanceGrant(initial);
  }
}
