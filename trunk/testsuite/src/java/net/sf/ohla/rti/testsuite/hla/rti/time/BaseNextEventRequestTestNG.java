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

import hla.rti.EventRetractionHandle;
import hla.rti.SuppliedParameters;

public abstract class BaseNextEventRequestTestNG
  extends BaseTimeManagementTestNG
{
  protected int testInteractionClassHandle2;
  protected int testInteractionClassHandle3;

  protected SuppliedParameters testParameterValues;

  protected EventRetractionHandle testInteractionEventRetractionHandle2;
  protected EventRetractionHandle testInteractionEventRetractionHandle3;

  protected BaseNextEventRequestTestNG(String federationName)
  {
    super(5, federationName);
  }

  @BeforeClass
  public void baseNextMessageRequestSetup()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(initial, lookahead1);
    rtiAmbassadors.get(1).enableTimeRegulation(initial, lookahead2);
    rtiAmbassadors.get(2).enableTimeRegulation(initial, lookahead2);
    rtiAmbassadors.get(3).enableTimeRegulation(initial, lookahead1);
    rtiAmbassadors.get(4).enableTimeRegulation(initial, lookahead1);

    federateAmbassadors.get(0).checkTimeRegulationEnabled(initial);
    federateAmbassadors.get(1).checkTimeRegulationEnabled(initial);
    federateAmbassadors.get(2).checkTimeRegulationEnabled(initial);
    federateAmbassadors.get(3).checkTimeRegulationEnabled(initial);
    federateAmbassadors.get(4).checkTimeRegulationEnabled(initial);

    rtiAmbassadors.get(0).enableTimeConstrained();
    rtiAmbassadors.get(1).enableTimeConstrained();
    rtiAmbassadors.get(2).enableTimeConstrained();
    rtiAmbassadors.get(3).enableTimeConstrained();
    rtiAmbassadors.get(4).enableTimeConstrained();

    federateAmbassadors.get(0).checkTimeConstrainedEnabled(initial);
    federateAmbassadors.get(1).checkTimeConstrainedEnabled(initial);
    federateAmbassadors.get(2).checkTimeConstrainedEnabled(initial);
    federateAmbassadors.get(3).checkTimeConstrainedEnabled(initial);
    federateAmbassadors.get(4).checkTimeConstrainedEnabled(initial);

    int testInteractionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
    testInteractionClassHandle2 = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION2);
    testInteractionClassHandle3 = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION3);

    int parameterHandle1 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER1, testInteractionClassHandle);
    int parameterHandle2 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER2, testInteractionClassHandle);
    int parameterHandle3 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER3, testInteractionClassHandle);

    testParameterValues = rtiFactory.createSuppliedParameters();
    testParameterValues.add(parameterHandle1, PARAMETER1_VALUE.getBytes());
    testParameterValues.add(parameterHandle2, PARAMETER2_VALUE.getBytes());
    testParameterValues.add(parameterHandle3, PARAMETER3_VALUE.getBytes());

    rtiAmbassadors.get(0).publishInteractionClass(testInteractionClassHandle2);
    rtiAmbassadors.get(0).publishInteractionClass(testInteractionClassHandle3);

    rtiAmbassadors.get(1).subscribeInteractionClass(testInteractionClassHandle2);
    rtiAmbassadors.get(2).subscribeInteractionClass(testInteractionClassHandle2);

    rtiAmbassadors.get(3).subscribeInteractionClass(testInteractionClassHandle3);
    rtiAmbassadors.get(4).subscribeInteractionClass(testInteractionClassHandle3);

    synchronize(SYNCHRONIZATION_POINT_SETUP_COMPLETE, federateAmbassadors);

    testInteractionEventRetractionHandle2 =
      rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle2, testParameterValues, TAG, three);
    testInteractionEventRetractionHandle3 =
      rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle3, testParameterValues, TAG, four);
  }
}
