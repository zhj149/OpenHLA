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

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;

@Test
public class ObjectTimeManagementTestNG
  extends BaseTimeManagementTestNG
{
  private static final String FEDERATION_NAME = "OHLA IEEE 1516e Object Time Management Test Federation";

  private ObjectInstanceHandle testObjectInstanceHandle;

  private AttributeHandle attributeHandle1;
  private AttributeHandle attributeHandle2;
  private AttributeHandle attributeHandle3;

  public ObjectTimeManagementTestNG()
    throws Exception
  {
    super(3, FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    ObjectClassHandle testObjectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    attributeHandle1 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE1);
    attributeHandle2 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE2);
    attributeHandle3 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE3);

    AttributeHandleSet testObjectAttributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    testObjectAttributeHandles.add(attributeHandle1);
    testObjectAttributeHandles.add(attributeHandle2);
    testObjectAttributeHandles.add(attributeHandle3);

    rtiAmbassadors.get(0).publishObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(0).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(1).publishObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(1).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(2).publishObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(2).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);

    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);
    rtiAmbassadors.get(2).enableTimeRegulation(lookahead1);

    rtiAmbassadors.get(1).enableTimeConstrained();
    rtiAmbassadors.get(2).enableTimeConstrained();

    federateAmbassadors.get(0).checkTimeRegulationEnabled(initial);
    federateAmbassadors.get(2).checkTimeRegulationEnabled(initial);

    federateAmbassadors.get(1).checkTimeConstrainedEnabled(initial);
    federateAmbassadors.get(2).checkTimeConstrainedEnabled(initial);

    testObjectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);

    setupComplete(federateAmbassadors);
  }

  @Test
  public void testUpdateAttributeValuesWhileNotTimeAdvancing()
    throws Exception
  {
    AttributeHandleValueMap attributeValues = rtiAmbassadors.get(0).getAttributeHandleValueMapFactory().create(3);
    attributeValues.put(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());
    attributeValues.put(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
    attributeValues.put(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());

    rtiAmbassadors.get(0).updateAttributeValues(testObjectInstanceHandle, attributeValues, TAG);

    // the 2 constrained federates will not receive it because they do not have asynchronous delivery enabled and are
    // not in the time advancing state
    //
    federateAmbassadors.get(1).checkAttributeValuesNotReceived(testObjectInstanceHandle);
    federateAmbassadors.get(2).checkAttributeValuesNotReceived(testObjectInstanceHandle);

    // advance constrained federates so they will be sure to receive the update (because they will be waiting for the
    // remaining regulating federate to advance)
    //
    rtiAmbassadors.get(1).timeAdvanceRequest(ten);
    rtiAmbassadors.get(2).timeAdvanceRequest(ten);

    // parameter values should have been released
    //
    federateAmbassadors.get(1).checkAttributeValues(testObjectInstanceHandle, attributeValues);
    federateAmbassadors.get(2).checkAttributeValues(testObjectInstanceHandle, attributeValues);

    // finish time advance
    //
    rtiAmbassadors.get(0).timeAdvanceRequest(ten);

    federateAmbassadors.get(0).checkTimeAdvanceGrant(ten);
    federateAmbassadors.get(1).checkTimeAdvanceGrant(ten);
    federateAmbassadors.get(2).checkTimeAdvanceGrant(ten);
  }

  @Test(dependsOnMethods = {"testUpdateAttributeValuesWhileNotTimeAdvancing"})
  public void testUpdateAttributeValuesInFuture()
    throws Exception
  {
    AttributeHandleValueMap attributeValues = rtiAmbassadors.get(0).getAttributeHandleValueMapFactory().create(3);
    attributeValues.put(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());
    attributeValues.put(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
    attributeValues.put(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());

    rtiAmbassadors.get(0).updateAttributeValues(testObjectInstanceHandle, attributeValues, TAG, twenty);

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
    federateAmbassadors.get(1).checkAttributeValuesNotReceived(testObjectInstanceHandle);
    federateAmbassadors.get(2).checkAttributeValuesNotReceived(testObjectInstanceHandle);

    // advance constrained federates
    //
    rtiAmbassadors.get(1).timeAdvanceRequest(twenty);
    rtiAmbassadors.get(2).timeAdvanceRequest(twenty);

    // parameter values should have been released
    //
    federateAmbassadors.get(1).checkAttributeValues(testObjectInstanceHandle, attributeValues);
    federateAmbassadors.get(2).checkAttributeValues(testObjectInstanceHandle, attributeValues);

    federateAmbassadors.get(1).checkTimeAdvanceGrant(twenty);
    federateAmbassadors.get(2).checkTimeAdvanceGrant(twenty);
  }
}
