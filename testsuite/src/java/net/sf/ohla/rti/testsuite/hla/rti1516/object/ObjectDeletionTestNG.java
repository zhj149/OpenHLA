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

package net.sf.ohla.rti.testsuite.hla.rti1516.object;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti1516.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti1516.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.DeletePrivilegeNotHeld;
import hla.rti1516.FederateInternalError;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ObjectInstanceNotKnown;
import hla.rti1516.OrderType;
import hla.rti1516.RTIambassador;
import hla.rti1516.ResignAction;

@Test
public class ObjectDeletionTestNG
  extends BaseTestNG<ObjectDeletionTestNG.TestFederateAmbassador>
{
  private static final String FEDERATION_NAME = ObjectDeletionTestNG.class.getSimpleName();

  private ObjectInstanceHandle testObjectInstanceHandle;
  private ObjectInstanceHandle testObjectInstanceHandle2;

  public ObjectDeletionTestNG()
  {
    super(4, FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    createFederationExecution();
    joinFederationExecution();

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

    rtiAmbassadors.get(0).publishObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(0).publishObjectClassAttributes(testObjectClassHandle2, testObjectAttributeHandles2);

    rtiAmbassadors.get(1).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(2).subscribeObjectClassAttributes(testObjectClassHandle2, testObjectAttributeHandles2);

    testObjectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);
    testObjectInstanceHandle2 = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle2);

    // ensure the objects arrive
    //
    federateAmbassadors.get(1).checkObjectInstanceHandle(testObjectInstanceHandle);
    federateAmbassadors.get(1).checkObjectInstanceHandle(testObjectInstanceHandle2);
    federateAmbassadors.get(2).checkObjectInstanceHandle(testObjectInstanceHandle2);

    synchronize(SYNCHRONIZATION_POINT_SETUP_COMPLETE, federateAmbassadors);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    // the first RTI has been resigned already
    //
    rtiAmbassadors.remove(0);

    resignFederationExecution(ResignAction.UNCONDITIONALLY_DIVEST_ATTRIBUTES);
    destroyFederationExecution();
  }

  @Test
  public void testDeleteObjectInstance()
    throws Exception
  {
    rtiAmbassadors.get(0).deleteObjectInstance(testObjectInstanceHandle2, TAG);

    federateAmbassadors.get(1).checkRemoved(testObjectInstanceHandle2, TAG);
    federateAmbassadors.get(2).checkRemoved(testObjectInstanceHandle2, TAG);
  }

  @Test(expectedExceptions = ObjectInstanceNotKnown.class)
  public void testDeleteObjectInstanceWithNullObjectInstanceHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).deleteObjectInstance(null, TAG);
  }

  @Test(expectedExceptions = DeletePrivilegeNotHeld.class)
  public void testDeleteUnownedObjectInstance()
    throws Exception
  {
    rtiAmbassadors.get(1).deleteObjectInstance(testObjectInstanceHandle, TAG);
  }

  @Test(expectedExceptions = ObjectInstanceNotKnown.class)
  public void testDeleteUnknownObject()
    throws Exception
  {
    rtiAmbassadors.get(3).deleteObjectInstance(testObjectInstanceHandle, TAG);
  }

  @Test(dependsOnMethods = {"testDeleteObjectInstance", "testDeleteUnownedObjectInstance"})
  public void testDeleteObjectInstanceByResigning()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.DELETE_OBJECTS);

    federateAmbassadors.get(1).checkRemoved(testObjectInstanceHandle, null);
  }

  protected TestFederateAmbassador createFederateAmbassador(RTIambassador rtiAmbassador)
  {
    return new TestFederateAmbassador(rtiAmbassador);
  }

  public static class TestFederateAmbassador
    extends BaseFederateAmbassador
  {
    private final Map<ObjectInstanceHandle, TestObjectInstance> objectInstances =
      new HashMap<ObjectInstanceHandle, TestObjectInstance>();

    public TestFederateAmbassador(RTIambassador rtiAmbassador)
    {
      super(rtiAmbassador);
    }

    public void checkObjectInstanceHandle(final ObjectInstanceHandle objectInstanceHandle)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return !objectInstances.containsKey(objectInstanceHandle); } });

      assert objectInstances.containsKey(objectInstanceHandle);
    }

    public void checkRemoved(final ObjectInstanceHandle objectInstanceHandle, byte[] tag)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return !objectInstances.get(objectInstanceHandle).isRemoved(); } });

      TestObjectInstance objectInstance = objectInstances.get(objectInstanceHandle);
      assert objectInstance != null;
      assert objectInstance.isRemoved();
      assert Arrays.equals(tag, objectInstance.getTag());
    }

    @Override
    public void discoverObjectInstance(
      ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle, String objectInstanceName)
      throws FederateInternalError
    {
      objectInstances.put(objectInstanceHandle, new TestObjectInstance(
        objectInstanceHandle, objectClassHandle, objectInstanceName));
    }

    @Override
    public void removeObjectInstance(ObjectInstanceHandle objectInstanceHandle, byte[] tag, OrderType sentOrderType)
      throws ObjectInstanceNotKnown, FederateInternalError
    {
      objectInstances.get(objectInstanceHandle).setRemoved(tag);
    }
  }
}
