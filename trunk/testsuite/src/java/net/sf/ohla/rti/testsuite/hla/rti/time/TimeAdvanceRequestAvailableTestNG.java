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

import hla.rti.AttributeHandleSet;
import hla.rti.FederationTimeAlreadyPassed;
import hla.rti.SuppliedAttributes;
import hla.rti.SuppliedParameters;

@Test
public class TimeAdvanceRequestAvailableTestNG
  extends BaseTimeManagementTestNG
{
  private static final String FEDERATION_NAME = "OHLA IEEE 1516e Time Advance Request Available Test Federation";

  private int testInteractionClassHandle;

  private SuppliedParameters testSuppliedParameters;

  private int testObjectInstanceHandle;
  private String testObjectInstanceName;
  private SuppliedAttributes testSuppliedAttributes;

  public TimeAdvanceRequestAvailableTestNG()
    throws Exception
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

    int testObjectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    int attributeHandle1 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE1, testObjectClassHandle);
    int attributeHandle2 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE2, testObjectClassHandle);
    int attributeHandle3 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE3, testObjectClassHandle);

    AttributeHandleSet testObjectAttributeHandles = rtiFactory.createAttributeHandleSet();
    testObjectAttributeHandles.add(attributeHandle1);
    testObjectAttributeHandles.add(attributeHandle2);
    testObjectAttributeHandles.add(attributeHandle3);

    testSuppliedAttributes = rtiFactory.createSuppliedAttributes();

    testSuppliedAttributes.add(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());
    testSuppliedAttributes.add(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
    testSuppliedAttributes.add(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());

    rtiAmbassadors.get(0).publishObjectClass(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(1).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);

    testObjectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);
    testObjectInstanceName = rtiAmbassadors.get(0).getObjectInstanceName(testObjectInstanceHandle);

    federateAmbassadors.get(1).checkObjectInstanceName(testObjectInstanceName);

    setupComplete(federateAmbassadors);

    rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle, testSuppliedParameters, TAG, five);
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
    federateAmbassadors.get(1).checkReceivedInteraction(testSuppliedParameters, five);

    federateAmbassadors.get(0).checkTimeAdvanceGrant(four);
    federateAmbassadors.get(1).checkTimeAdvanceGrant(five);

    // send another message at 5
    //
    rtiAmbassadors.get(0).updateAttributeValues(testObjectInstanceHandle, testSuppliedAttributes, TAG, five);

    rtiAmbassadors.get(0).timeAdvanceRequest(ten);
    rtiAmbassadors.get(1).timeAdvanceRequest(ten);

    // the second message should be received
    //
    federateAmbassadors.get(1).checkReflectedAttributes(testObjectInstanceName, testSuppliedAttributes, five);

    federateAmbassadors.get(0).checkTimeAdvanceGrant(ten);
    federateAmbassadors.get(1).checkTimeAdvanceGrant(ten);
  }

  @Test(dependsOnMethods = { "testTimeAdvanceRequestAvailable" }, expectedExceptions = { FederationTimeAlreadyPassed.class })
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

    assert ten.equals(rtiAmbassadors.get(0).queryFederateTime());
  }
}
