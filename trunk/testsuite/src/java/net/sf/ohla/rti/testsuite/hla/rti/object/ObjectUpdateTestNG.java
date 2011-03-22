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

package net.sf.ohla.rti.testsuite.hla.rti.object;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.AttributeHandleSet;
import hla.rti.AttributeNotDefined;
import hla.rti.AttributeNotKnown;
import hla.rti.AttributeNotOwned;
import hla.rti.FederateInternalError;
import hla.rti.FederateOwnsAttributes;
import hla.rti.ObjectNotKnown;
import hla.rti.ReflectedAttributes;
import hla.rti.ResignAction;
import hla.rti.SuppliedAttributes;
import hla.rti.jlc.RTIambassadorEx;

@Test
public class ObjectUpdateTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = "OHLA HLA 1.3 Object Update Test Federation";

  private final List<TestFederateAmbassador> federateAmbassadors = new ArrayList<TestFederateAmbassador>(4);

  private SuppliedAttributes testObjectAttributeValues;
  private SuppliedAttributes testObjectAttributeValues2;

  private int testObjectInstanceHandle;
  private int testObjectInstanceHandle2;

  private String testObjectInstanceName;
  private String testObjectInstanceName2;

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

    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fed);

    rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(0));
    rtiAmbassadors.get(1).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(1));
    rtiAmbassadors.get(2).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(2));
    rtiAmbassadors.get(3).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(3));

    int testObjectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    int attributeHandle1 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE1, testObjectClassHandle);
    int attributeHandle2 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE2, testObjectClassHandle);
    int attributeHandle3 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE3, testObjectClassHandle);
    AttributeHandleSet testObjectAttributeHandles = rtiFactory.createAttributeHandleSet();
    testObjectAttributeHandles.add(attributeHandle1);
    testObjectAttributeHandles.add(attributeHandle2);
    testObjectAttributeHandles.add(attributeHandle3);

    int testObjectClassHandle2 = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT2);
    int attributeHandle4 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE4, testObjectClassHandle2);
    int attributeHandle5 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE5, testObjectClassHandle2);
    int attributeHandle6 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE6, testObjectClassHandle2);
    AttributeHandleSet testObjectAttributeHandles2 = rtiFactory.createAttributeHandleSet();
    testObjectAttributeHandles2.add(attributeHandle1);
    testObjectAttributeHandles2.add(attributeHandle2);
    testObjectAttributeHandles2.add(attributeHandle3);
    testObjectAttributeHandles2.add(attributeHandle4);
    testObjectAttributeHandles2.add(attributeHandle5);
    testObjectAttributeHandles2.add(attributeHandle6);

    testObjectAttributeValues = rtiFactory.createSuppliedAttributes();
    testObjectAttributeValues2 = rtiFactory.createSuppliedAttributes();

    testObjectAttributeValues.add(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());
    testObjectAttributeValues.add(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
    testObjectAttributeValues.add(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());

    testObjectAttributeValues2.add(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());
    testObjectAttributeValues2.add(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
    testObjectAttributeValues2.add(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());
    testObjectAttributeValues2.add(attributeHandle4, ATTRIBUTE4_VALUE.getBytes());
    testObjectAttributeValues2.add(attributeHandle5, ATTRIBUTE5_VALUE.getBytes());
    testObjectAttributeValues2.add(attributeHandle6, ATTRIBUTE6_VALUE.getBytes());

    rtiAmbassadors.get(0).publishObjectClass(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(0).publishObjectClass(testObjectClassHandle2, testObjectAttributeHandles2);

    rtiAmbassadors.get(1).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(2).subscribeObjectClassAttributes(testObjectClassHandle2, testObjectAttributeHandles2);

    testObjectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);
    testObjectInstanceHandle2 = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle2);

    testObjectInstanceName = rtiAmbassadors.get(0).getObjectInstanceName(testObjectInstanceHandle);
    testObjectInstanceName2 = rtiAmbassadors.get(0).getObjectInstanceName(testObjectInstanceHandle2);

    // ensure the objects arrive
    //
    federateAmbassadors.get(1).checkObjectInstanceName(testObjectInstanceName);
    federateAmbassadors.get(1).checkObjectInstanceName(testObjectInstanceName2);
    federateAmbassadors.get(2).checkObjectInstanceName(testObjectInstanceName2);

    setupComplete(federateAmbassadors);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    resignFederationExecution(ResignAction.RELEASE_ATTRIBUTES);

    destroyFederationExecution(FEDERATION_NAME);
  }

  @Test
  public void testUpdateAttributeValues()
    throws Exception
  {
    rtiAmbassadors.get(0).updateAttributeValues(testObjectInstanceHandle2, testObjectAttributeValues2, TAG);

    federateAmbassadors.get(1).checkAttributeValues(testObjectInstanceName2, testObjectAttributeValues, TAG);
    federateAmbassadors.get(2).checkAttributeValues(testObjectInstanceName2, testObjectAttributeValues2, TAG);
  }

  @Test(expectedExceptions = {ObjectNotKnown.class})
  public void testUpdateAttributeValuesWithInvalidObjectInstanceHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).updateAttributeValues(-1, testObjectAttributeValues2, null);
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
    rtiAmbassadors.get(1).updateAttributeValues(
      rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName), testObjectAttributeValues, null);
  }

  @Test(expectedExceptions = {ObjectNotKnown.class})
  public void testUpdateAttributeValuesOfUnknownObject()
    throws Exception
  {
    rtiAmbassadors.get(3).updateAttributeValues(100, testObjectAttributeValues, null);
  }

  private static class TestFederateAmbassador
    extends BaseFederateAmbassador
  {
    private final Map<String, TestObjectInstance> objectInstances =
      new HashMap<String, TestObjectInstance>();
    private final Map<Integer, TestObjectInstance> objectInstancesByHandle =
      new HashMap<Integer, TestObjectInstance>();

    public TestFederateAmbassador(RTIambassadorEx rtiAmbassador)
    {
      super(rtiAmbassador);
    }

    public void checkObjectInstanceName(final String objectInstanceName)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return !objectInstances.containsKey(objectInstanceName); } });

      assert objectInstances.containsKey(objectInstanceName);
    }

    public void checkAttributeValues(final String objectInstanceName, SuppliedAttributes suppliedAttributes, byte[] tag)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return objectInstances.get(objectInstanceName).getReflectedAttributes() == null; } });

      TestObjectInstance objectInstance = objectInstances.get(objectInstanceName);
      assert objectInstance.getReflectedAttributes() != null;
      checkReflectedAttributes(objectInstance.getReflectedAttributes(), suppliedAttributes, false);
      assert Arrays.equals(tag, objectInstance.getTag());
    }

    @Override
    public void discoverObjectInstance(int objectInstanceHandle, int objectClassHandle, String objectInstanceName)
    {
      TestObjectInstance objectInstance =
        new TestObjectInstance(objectInstanceHandle, objectClassHandle, objectInstanceName);
      objectInstances.put(objectInstanceName, objectInstance);
      objectInstancesByHandle.put(objectInstanceHandle, objectInstance);
    }

    @Override
    public void reflectAttributeValues(int objectInstanceHandle, ReflectedAttributes reflectedAttributes, byte[] tag)
      throws ObjectNotKnown, AttributeNotKnown, FederateOwnsAttributes, FederateInternalError
    {
      objectInstancesByHandle.get(objectInstanceHandle).setReflectedAttributes(reflectedAttributes, tag);
    }
  }
}
