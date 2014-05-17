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

import hla.rti.AttributeHandleSet;
import hla.rti.EventRetractionHandle;
import hla.rti.SuppliedAttributes;
import hla.rti.SuppliedParameters;

public abstract class BaseTimeAdvanceRequestTestNG
  extends BaseTimeManagementTestNG
{
  protected int testInteractionClassHandle;
  protected SuppliedParameters testParameterValues;
  protected EventRetractionHandle testInteractionEventRetractionHandle;

  protected int testObjectClassHandle;
  protected String testObjectInstanceName;
  protected SuppliedAttributes testAttributeValues;
  protected EventRetractionHandle testUpdateAttributesEventRetractionHandle;

  protected BaseTimeAdvanceRequestTestNG(String federationName)
  {
    super(2, federationName);
  }

  @BeforeClass
  public void baseTimeAdvanceRequestSetup()
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

    testParameterValues = rtiFactory.createSuppliedParameters();
    testParameterValues.add(parameterHandle1, PARAMETER1_VALUE.getBytes());
    testParameterValues.add(parameterHandle2, PARAMETER2_VALUE.getBytes());
    testParameterValues.add(parameterHandle3, PARAMETER3_VALUE.getBytes());

    rtiAmbassadors.get(0).publishInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(1).subscribeInteractionClass(testInteractionClassHandle);

    testObjectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    int attributeHandle1 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE1, testObjectClassHandle);
    int attributeHandle2 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE2, testObjectClassHandle);
    int attributeHandle3 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE3, testObjectClassHandle);

    AttributeHandleSet testObjectAttributeHandles = rtiFactory.createAttributeHandleSet();
    testObjectAttributeHandles.add(attributeHandle1);
    testObjectAttributeHandles.add(attributeHandle2);
    testObjectAttributeHandles.add(attributeHandle3);

    rtiAmbassadors.get(0).publishObjectClass(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(1).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);

    int testObjectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);
    testObjectInstanceName = rtiAmbassadors.get(0).getObjectInstanceName(testObjectInstanceHandle);

    federateAmbassadors.get(1).checkObjectInstanceName(testObjectInstanceName);

    testAttributeValues = rtiFactory.createSuppliedAttributes();
    testAttributeValues.add(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());
    testAttributeValues.add(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
    testAttributeValues.add(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());

    synchronize(SYNCHRONIZATION_POINT_SETUP_COMPLETE, federateAmbassadors);
  }
}
