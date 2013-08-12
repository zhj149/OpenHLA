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

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.LogicalTimeAlreadyPassed;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ParameterHandle;
import hla.rti1516.ParameterHandleValueMap;

@Test
public class TimeAdvanceRequestAvailableTestNG
  extends BaseTimeManagementTestNG
{
  private static final String FEDERATION_NAME = "OHLA IEEE 1516 Time Advance Request Available Test Federation";

  private InteractionClassHandle testInteractionClassHandle;

  private ParameterHandleValueMap testParameterValues;

  private ObjectInstanceHandle testObjectInstanceHandle;
  private AttributeHandleValueMap testAttributeValues;

  public TimeAdvanceRequestAvailableTestNG()
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

    testInteractionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);

    ParameterHandle parameterHandle1 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle, PARAMETER1);
    ParameterHandle parameterHandle2 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle, PARAMETER2);
    ParameterHandle parameterHandle3 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle, PARAMETER3);

    testParameterValues = rtiAmbassadors.get(0).getParameterHandleValueMapFactory().create(3);
    testParameterValues.put(parameterHandle1, PARAMETER1_VALUE.getBytes());
    testParameterValues.put(parameterHandle2, PARAMETER2_VALUE.getBytes());
    testParameterValues.put(parameterHandle3, PARAMETER3_VALUE.getBytes());

    rtiAmbassadors.get(0).publishInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(1).subscribeInteractionClass(testInteractionClassHandle);

    ObjectClassHandle testObjectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    AttributeHandle attributeHandle1 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE1);
    AttributeHandle attributeHandle2 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE2);
    AttributeHandle attributeHandle3 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE3);

    AttributeHandleSet testObjectAttributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    testObjectAttributeHandles.add(attributeHandle1);
    testObjectAttributeHandles.add(attributeHandle2);
    testObjectAttributeHandles.add(attributeHandle3);

    rtiAmbassadors.get(0).publishObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(1).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);

    testObjectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);

    federateAmbassadors.get(1).checkObjectInstanceHandle(testObjectInstanceHandle);

    testAttributeValues = rtiAmbassadors.get(0).getAttributeHandleValueMapFactory().create(3);
    testAttributeValues.put(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());
    testAttributeValues.put(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
    testAttributeValues.put(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());

    synchronize(SYNCHRONIZATION_POINT_SETUP_COMPLETE, federateAmbassadors);

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
    federateAmbassadors.get(1).checkParameterValues(testParameterValues, five);

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
    rtiAmbassadors.get(0).timeAdvanceRequest(five);
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