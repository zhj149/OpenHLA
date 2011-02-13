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

package net.sf.ohla.rti.testsuite.hla.rti1516e.object;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.AttributeNotOwned;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.ObjectInstanceNotKnown;

@Test
public class ObjectUpdateTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = "OHLA Object Update Test Federation";

  private final List<FederateHandle> federateHandles = new ArrayList<FederateHandle>(4);
  private final List<TestFederateAmbassador> federateAmbassadors = new ArrayList<TestFederateAmbassador>(4);

  private AttributeHandleValueMap testObjectAttributeValues;
  private AttributeHandleValueMap testObjectAttributeValues2;

  private ObjectInstanceHandle testObjectInstanceHandle;
  private ObjectInstanceHandle testObjectInstanceHandle2;

  public ObjectUpdateTestNG()
  {
    super(4);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(0)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(1)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(2)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(3)));

    rtiAmbassadors.get(0).connect(federateAmbassadors.get(0), CallbackModel.HLA_EVOKED);
    rtiAmbassadors.get(1).connect(federateAmbassadors.get(1), CallbackModel.HLA_EVOKED);
    rtiAmbassadors.get(2).connect(federateAmbassadors.get(2), CallbackModel.HLA_EVOKED);
    rtiAmbassadors.get(3).connect(federateAmbassadors.get(2), CallbackModel.HLA_EVOKED);

    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);

    federateHandles.add(rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(1).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(2).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(3).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME));

    ObjectClassHandle testObjectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    AttributeHandle attributeHandle1 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE1);
    AttributeHandle attributeHandle2 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE2);
    AttributeHandle attributeHandle3 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE3);
    AttributeHandleSet testObjectAttributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    testObjectAttributeHandles.add(attributeHandle1);
    testObjectAttributeHandles.add(attributeHandle2);
    testObjectAttributeHandles.add(attributeHandle3);

    ObjectClassHandle testObjectClassHandle2 = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT2);
    AttributeHandle attributeHandle4 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle2, ATTRIBUTE4);
    AttributeHandle attributeHandle5 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle2, ATTRIBUTE5);
    AttributeHandle attributeHandle6 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle2, ATTRIBUTE6);
    AttributeHandleSet testObjectAttributeHandles2 = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    testObjectAttributeHandles2.add(attributeHandle1);
    testObjectAttributeHandles2.add(attributeHandle2);
    testObjectAttributeHandles2.add(attributeHandle3);
    testObjectAttributeHandles2.add(attributeHandle4);
    testObjectAttributeHandles2.add(attributeHandle5);
    testObjectAttributeHandles2.add(attributeHandle6);

    testObjectAttributeValues = rtiAmbassadors.get(0).getAttributeHandleValueMapFactory().create(3);
    testObjectAttributeValues2 = rtiAmbassadors.get(0).getAttributeHandleValueMapFactory().create(3);

    testObjectAttributeValues.put(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());
    testObjectAttributeValues.put(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
    testObjectAttributeValues.put(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());

    testObjectAttributeValues2.put(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());
    testObjectAttributeValues2.put(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
    testObjectAttributeValues2.put(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());
    testObjectAttributeValues2.put(attributeHandle4, ATTRIBUTE4_VALUE.getBytes());
    testObjectAttributeValues2.put(attributeHandle5, ATTRIBUTE5_VALUE.getBytes());
    testObjectAttributeValues2.put(attributeHandle6, ATTRIBUTE6_VALUE.getBytes());

    rtiAmbassadors.get(0).publishObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(0).publishObjectClassAttributes(testObjectClassHandle2, testObjectAttributeHandles2);

    rtiAmbassadors.get(1).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(2).subscribeObjectClassAttributes(testObjectClassHandle2, testObjectAttributeHandles2);

    testObjectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);
    testObjectInstanceHandle2 = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle2);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.UNCONDITIONALLY_DIVEST_ATTRIBUTES);
    rtiAmbassadors.get(1).resignFederationExecution(ResignAction.UNCONDITIONALLY_DIVEST_ATTRIBUTES);
    rtiAmbassadors.get(2).resignFederationExecution(ResignAction.UNCONDITIONALLY_DIVEST_ATTRIBUTES);
    rtiAmbassadors.get(3).resignFederationExecution(ResignAction.UNCONDITIONALLY_DIVEST_ATTRIBUTES);

    // this is necessary to ensure the federates is actually resigned
    //
    LockSupport.parkUntil(System.currentTimeMillis() + 1000);

    rtiAmbassadors.get(0).destroyFederationExecution(FEDERATION_NAME);

    rtiAmbassadors.get(0).disconnect();
    rtiAmbassadors.get(1).disconnect();
    rtiAmbassadors.get(2).disconnect();
    rtiAmbassadors.get(3).disconnect();
  }

  @Test
  public void testUpdateAttributeValues()
    throws Exception
  {
    rtiAmbassadors.get(0).updateAttributeValues(testObjectInstanceHandle2, testObjectAttributeValues2, TAG);

    federateAmbassadors.get(1).checkAttributeValues(
      testObjectInstanceHandle2, testObjectAttributeValues, federateHandles.get(0), TAG);
    federateAmbassadors.get(2).checkAttributeValues(
      testObjectInstanceHandle2, testObjectAttributeValues2, federateHandles.get(0), TAG);
  }

  @Test(expectedExceptions = {ObjectInstanceNotKnown.class})
  public void testUpdateAttributeValuesWithNullObjectInstanceHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).updateAttributeValues(null, testObjectAttributeValues2, null);
  }

  @Test(expectedExceptions = {AttributeNotDefined.class})
  public void testUpdateAttributeValuesWithUndefinedAttributes()
    throws Exception
  {
    rtiAmbassadors.get(0).updateAttributeValues(testObjectInstanceHandle, testObjectAttributeValues2, null);
  }

  @Test(expectedExceptions = {AttributeNotOwned.class})
  public void testUpdateAttributeValuesOfUnownedAttribute()
    throws Exception
  {
    rtiAmbassadors.get(1).updateAttributeValues(testObjectInstanceHandle, testObjectAttributeValues, null);
  }

  @Test(expectedExceptions = {ObjectInstanceNotKnown.class})
  public void testUpdateAttributeValuesOfUnknownObject()
    throws Exception
  {
    rtiAmbassadors.get(3).updateAttributeValues(testObjectInstanceHandle, testObjectAttributeValues, null);
  }

  private static class TestFederateAmbassador
    extends NullFederateAmbassador
  {
    private final RTIambassador rtiAmbassador;

    private final Map<ObjectInstanceHandle, TestObjectInstance> objectInstances =
      new HashMap<ObjectInstanceHandle, TestObjectInstance>();

    public TestFederateAmbassador(RTIambassador rtiAmbassador)
    {
      this.rtiAmbassador = rtiAmbassador;
    }

    public void checkAttributeValues(
      ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues, FederateHandle federateHandle,
      byte[] tag)
      throws Exception
    {
      for (int i = 0; i < 5 && (!objectInstances.containsKey(objectInstanceHandle) || objectInstances.get(objectInstanceHandle).getAttributeValues() == null); i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      TestObjectInstance objectInstance = objectInstances.get(objectInstanceHandle);
      assert objectInstance != null && objectInstance.getAttributeValues() != null &&
             objectInstance.getAttributeValues().equals(attributeValues) &&
             Arrays.equals(tag, objectInstance.getTag()) &&
             objectInstance.getUpdatingFederateHandle().equals(federateHandle);
    }

    @Override
    public void discoverObjectInstance(
      ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle, String objectInstanceName,
      FederateHandle federateHandle)
      throws FederateInternalError
    {
      objectInstances.put(objectInstanceHandle, new TestObjectInstance(
        objectInstanceHandle, objectClassHandle, objectInstanceName, federateHandle));
    }

    @Override
    public void reflectAttributeValues(
      ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues,
      byte[] tag, OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle,
      SupplementalReflectInfo reflectInfo)
      throws FederateInternalError
    {
      objectInstances.get(objectInstanceHandle).setAttributeValues(
        attributeValues, tag, reflectInfo.getProducingFederate());
    }
  }
}
