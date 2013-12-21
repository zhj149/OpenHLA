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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.AttributeHandleSet;
import hla.rti.DeletePrivilegeNotHeld;
import hla.rti.FederateInternalError;
import hla.rti.ObjectNotKnown;
import hla.rti.ResignAction;
import hla.rti.jlc.RTIambassadorEx;

@Test
public class ObjectDeletionTestNG
  extends BaseTestNG<ObjectDeletionTestNG.TestFederateAmbassador>
{
  private static final String FEDERATION_NAME = ObjectDeletionTestNG.class.getSimpleName();

  private int testObjectInstanceHandle;
  private int testObjectInstanceHandle2;

  private String testObjectInstanceName;
  private String testObjectInstanceName2;

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

    synchronize(SYNCHRONIZATION_POINT_SETUP_COMPLETE, federateAmbassadors);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    // the first RTI has been resigned already
    //
    rtiAmbassadors.remove(0);

    resignFederationExecution(ResignAction.RELEASE_ATTRIBUTES);
    destroyFederationExecution();
  }

  @Test
  public void testDeleteObjectInstance()
    throws Exception
  {
    rtiAmbassadors.get(0).deleteObjectInstance(testObjectInstanceHandle2, TAG);

    federateAmbassadors.get(1).checkRemoved(testObjectInstanceName2, TAG);
    federateAmbassadors.get(2).checkRemoved(testObjectInstanceName2, TAG);
  }

  @Test(expectedExceptions = ObjectNotKnown.class)
  public void testDeleteObjectInstanceWithInvalidObjectInstanceHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).deleteObjectInstance(-1, TAG);
  }

  @Test(expectedExceptions = DeletePrivilegeNotHeld.class)
  public void testDeleteUnownedObjectInstance()
    throws Exception
  {
    rtiAmbassadors.get(1).deleteObjectInstance(
      rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName), TAG);
  }

  @Test(expectedExceptions = ObjectNotKnown.class)
  public void testDeleteUnknownObject()
    throws Exception
  {
    rtiAmbassadors.get(3).deleteObjectInstance(100, TAG);
  }

  @Test(dependsOnMethods = {"testDeleteObjectInstance", "testDeleteUnownedObjectInstance"})
  public void testDeleteObjectInstanceByResigning()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.DELETE_OBJECTS);

    federateAmbassadors.get(1).checkRemoved(testObjectInstanceName, null);
  }

  protected TestFederateAmbassador createFederateAmbassador(RTIambassadorEx rtiAmbassador)
  {
    return new TestFederateAmbassador(rtiAmbassador);
  }

  public static class TestFederateAmbassador
    extends BaseFederateAmbassador
  {
    private final Map<String, TestObjectInstance> objectInstances = new HashMap<String, TestObjectInstance>();
    private final Map<Integer, TestObjectInstance> objectInstancesByHandle = new HashMap<Integer, TestObjectInstance>();

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

    public void checkRemoved(final String objectInstanceName, byte[] tag)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return !objectInstances.get(objectInstanceName).isRemoved(); } });

      TestObjectInstance objectInstance = objectInstances.get(objectInstanceName);
      assert objectInstance.isRemoved();
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
    public void removeObjectInstance(int objectInstanceHandle, byte[] tag)
      throws ObjectNotKnown, FederateInternalError
    {
      objectInstancesByHandle.get(objectInstanceHandle).setRemoved(tag);
    }
  }
}
