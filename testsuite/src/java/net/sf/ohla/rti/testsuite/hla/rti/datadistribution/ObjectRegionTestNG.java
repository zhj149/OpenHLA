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

package net.sf.ohla.rti.testsuite.hla.rti.datadistribution;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti.BaseTestNG;
import net.sf.ohla.rti.testsuite.hla.rti.object.TestObjectInstance;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.AttributeHandleSet;
import hla.rti.AttributeNotKnown;
import hla.rti.FederateInternalError;
import hla.rti.FederateOwnsAttributes;
import hla.rti.ObjectNotKnown;
import hla.rti.ReflectedAttributes;
import hla.rti.Region;
import hla.rti.ResignAction;
import hla.rti.SuppliedAttributes;
import hla.rti.jlc.RTIambassadorEx;

@Test
public class ObjectRegionTestNG
  extends BaseTestNG<ObjectRegionTestNG.TestFederateAmbassador>
{
  private static final String FEDERATION_NAME = ObjectRegionTestNG.class.getSimpleName();

  private Region region1;

  private int testObjectClassHandle;
  private int attributeHandle1;
  private int attributeHandle2;
  private int attributeHandle3;

  private int objectInstanceHandle;
  private String objectInstanceName;

  private SuppliedAttributes objectAttributeValues;

  public ObjectRegionTestNG()
  {
    super(3, FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    createFederationExecution();
    joinFederationExecution();

    int routingSpaceHandle = rtiAmbassadors.get(0).getRoutingSpaceHandle(ROUTING_SPACE);

    region1 = rtiAmbassadors.get(0).createRegion(routingSpaceHandle, 1);
    Region region2 = rtiAmbassadors.get(1).createRegion(routingSpaceHandle, 1);

    testObjectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    attributeHandle1 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE1, testObjectClassHandle);
    attributeHandle2 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE2, testObjectClassHandle);
    attributeHandle3 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE3, testObjectClassHandle);
    AttributeHandleSet testObjectAttributeHandles = rtiFactory.createAttributeHandleSet();
    testObjectAttributeHandles.add(attributeHandle1);
    testObjectAttributeHandles.add(attributeHandle2);
    testObjectAttributeHandles.add(attributeHandle3);

    rtiAmbassadors.get(0).publishObjectClass(testObjectClassHandle, testObjectAttributeHandles);

    AttributeHandleSet attributeHandles = rtiFactory.createAttributeHandleSet();
    attributeHandles.add(attributeHandle1);

    rtiAmbassadors.get(1).subscribeObjectClassAttributesWithRegion(testObjectClassHandle, region2, attributeHandles);

    attributeHandles = rtiFactory.createAttributeHandleSet();
    attributeHandles.add(attributeHandle1);
    attributeHandles.add(attributeHandle2);
    attributeHandles.add(attributeHandle3);

    rtiAmbassadors.get(2).subscribeObjectClassAttributes(testObjectClassHandle, attributeHandles);

    objectAttributeValues = rtiFactory.createSuppliedAttributes();

    objectAttributeValues.add(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());
    objectAttributeValues.add(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
    objectAttributeValues.add(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());

    synchronize(SYNCHRONIZATION_POINT_SETUP_COMPLETE, federateAmbassadors);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    resignFederationExecution(ResignAction.RELEASE_ATTRIBUTES);
    destroyFederationExecution();
  }

  @Test
  public void testRegisterObjectInstanceWithRegions()
    throws Exception
  {
    int[] attributeHandles = new int[] { attributeHandle1 };
    Region[] regions = new Region[] { region1 };

    objectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstanceWithRegion(
      testObjectClassHandle, attributeHandles, regions);
    objectInstanceName = rtiAmbassadors.get(0).getObjectInstanceName(objectInstanceHandle);

    federateAmbassadors.get(1).checkObjectInstanceName(objectInstanceName);
    federateAmbassadors.get(2).checkObjectInstanceName(objectInstanceName);
  }

  @Test(dependsOnMethods = "testRegisterObjectInstanceWithRegions")
  public void testUpdateAttributeValues()
    throws Exception
  {
    rtiAmbassadors.get(0).updateAttributeValues(objectInstanceHandle, objectAttributeValues, TAG);

    SuppliedAttributes objectAttributeValues = rtiFactory.createSuppliedAttributes();
    objectAttributeValues.add(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());

    federateAmbassadors.get(1).checkAttributeValues(objectInstanceName, objectAttributeValues, TAG, true);

    objectAttributeValues = rtiFactory.createSuppliedAttributes();
    objectAttributeValues.add(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
    objectAttributeValues.add(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());
    federateAmbassadors.get(2).checkAttributeValues(objectInstanceName, objectAttributeValues, TAG, false);
  }

  protected TestFederateAmbassador createFederateAmbassador(RTIambassadorEx rtiAmbassador)
  {
    return new TestFederateAmbassador(rtiAmbassador);
  }

  public static class TestFederateAmbassador
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

    public void checkAttributeValues(
      final String objectInstanceName, SuppliedAttributes suppliedAttributes, byte[] tag, boolean hasRegions)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return objectInstances.get(objectInstanceName).getReflectedAttributes() == null; } });

      TestObjectInstance objectInstance = objectInstances.get(objectInstanceName);
      assert objectInstance.getReflectedAttributes() != null;
      checkReflectedAttributes(objectInstance.getReflectedAttributes(), suppliedAttributes, hasRegions);
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
      objectInstancesByHandle.get(objectInstanceHandle).setReflectedAttributes(reflectedAttributes, tag, null);
    }
  }
}
