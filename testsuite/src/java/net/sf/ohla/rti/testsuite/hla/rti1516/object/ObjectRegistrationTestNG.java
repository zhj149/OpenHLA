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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti1516.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti1516.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.FederateInternalError;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectClassNotDefined;
import hla.rti1516.ObjectClassNotPublished;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ObjectInstanceNameInUse;
import hla.rti1516.ObjectInstanceNameNotReserved;
import hla.rti1516.RTIambassador;
import hla.rti1516.ResignAction;

@Test
public class ObjectRegistrationTestNG
  extends BaseTestNG<ObjectRegistrationTestNG.TestFederateAmbassador>
{
  private static final String FEDERATION_NAME = ObjectRegistrationTestNG.class.getSimpleName();

  private ObjectClassHandle testObjectClassHandle;
  private AttributeHandleSet testObjectAttributeHandles;

  private ObjectClassHandle testObjectClassHandle2;
  private AttributeHandleSet testObjectAttributeHandles2;

  private ObjectInstanceHandle objectInstanceHandle1;
  private ObjectInstanceHandle objectInstanceHandle2;
  private ObjectInstanceHandle objectInstanceHandle3;

  public ObjectRegistrationTestNG()
  {
    super(5, FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    createFederationExecution();
    joinFederationExecution();

    testObjectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    AttributeHandle attributeHandle1 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE1);
    AttributeHandle attributeHandle2 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE2);
    AttributeHandle attributeHandle3 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE3);
    testObjectAttributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    testObjectAttributeHandles.add(attributeHandle1);
    testObjectAttributeHandles.add(attributeHandle2);
    testObjectAttributeHandles.add(attributeHandle3);

    testObjectClassHandle2 = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT2);
    AttributeHandle attributeHandle4 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle2, ATTRIBUTE4);
    AttributeHandle attributeHandle5 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle2, ATTRIBUTE5);
    AttributeHandle attributeHandle6 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle2, ATTRIBUTE6);
    testObjectAttributeHandles2 = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    testObjectAttributeHandles2.add(attributeHandle1);
    testObjectAttributeHandles2.add(attributeHandle2);
    testObjectAttributeHandles2.add(attributeHandle3);
    testObjectAttributeHandles2.add(attributeHandle4);
    testObjectAttributeHandles2.add(attributeHandle5);
    testObjectAttributeHandles2.add(attributeHandle6);

    rtiAmbassadors.get(0).publishObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(1).publishObjectClassAttributes(testObjectClassHandle2, testObjectAttributeHandles2);

    rtiAmbassadors.get(0).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(1).subscribeObjectClassAttributes(testObjectClassHandle2, testObjectAttributeHandles2);
    rtiAmbassadors.get(2).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(2).subscribeObjectClassAttributes(testObjectClassHandle2, testObjectAttributeHandles2);

    synchronize(SYNCHRONIZATION_POINT_SETUP_COMPLETE, federateAmbassadors);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    resignFederationExecution(ResignAction.UNCONDITIONALLY_DIVEST_ATTRIBUTES);
    destroyFederationExecution();
  }

  @Test
  public void testRegisterObjectInstance()
    throws Exception
  {
    objectInstanceHandle1 = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);

    federateAmbassadors.get(2).checkObjectInstanceHandle(objectInstanceHandle1);
    federateAmbassadors.get(2).checkObjectClassHandle(objectInstanceHandle1, testObjectClassHandle);

    assert testObjectClassHandle.equals(rtiAmbassadors.get(0).getKnownObjectClassHandle(objectInstanceHandle1));

    String objectInstanceName = rtiAmbassadors.get(0).getObjectInstanceName(objectInstanceHandle1);
    assert objectInstanceHandle1.equals(rtiAmbassadors.get(0).getObjectInstanceHandle(objectInstanceName));

    assert objectInstanceName.equals(rtiAmbassadors.get(2).getObjectInstanceName(objectInstanceHandle1));
    assert objectInstanceHandle1.equals(rtiAmbassadors.get(2).getObjectInstanceHandle(objectInstanceName));
  }

  @Test
  public void testRegisterObjectInstanceChild()
    throws Exception
  {
    objectInstanceHandle2 = rtiAmbassadors.get(1).registerObjectInstance(testObjectClassHandle2);

    federateAmbassadors.get(0).checkObjectInstanceHandle(objectInstanceHandle2);
    federateAmbassadors.get(0).checkObjectClassHandle(objectInstanceHandle2, testObjectClassHandle);

    federateAmbassadors.get(2).checkObjectInstanceHandle(objectInstanceHandle2);
    federateAmbassadors.get(2).checkObjectClassHandle(objectInstanceHandle2, testObjectClassHandle2);

    assert testObjectClassHandle.equals(rtiAmbassadors.get(0).getKnownObjectClassHandle(objectInstanceHandle2));
    assert testObjectClassHandle2.equals(rtiAmbassadors.get(1).getKnownObjectClassHandle(objectInstanceHandle2));
    assert testObjectClassHandle2.equals(rtiAmbassadors.get(2).getKnownObjectClassHandle(objectInstanceHandle2));

    String objectInstanceName = rtiAmbassadors.get(0).getObjectInstanceName(objectInstanceHandle2);
    assert objectInstanceHandle2.equals(rtiAmbassadors.get(0).getObjectInstanceHandle(objectInstanceName));

    assert objectInstanceName.equals(rtiAmbassadors.get(1).getObjectInstanceName(objectInstanceHandle2));
    assert objectInstanceHandle2.equals(rtiAmbassadors.get(1).getObjectInstanceHandle(objectInstanceName));

    assert objectInstanceName.equals(rtiAmbassadors.get(2).getObjectInstanceName(objectInstanceHandle2));
    assert objectInstanceHandle2.equals(rtiAmbassadors.get(2).getObjectInstanceHandle(objectInstanceName));
  }

  @Test
  public void testRegisterObjectInstanceByName()
    throws Exception
  {
    rtiAmbassadors.get(0).reserveObjectInstanceName(TEST_OBJECT);
    federateAmbassadors.get(0).checkObjectInstanceNameReserved(TEST_OBJECT);

    objectInstanceHandle3 = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle, TEST_OBJECT);

    federateAmbassadors.get(2).checkObjectInstanceHandle(objectInstanceHandle3);
    federateAmbassadors.get(2).checkObjectInstanceName(objectInstanceHandle3, TEST_OBJECT);

    assert testObjectClassHandle.equals(rtiAmbassadors.get(0).getKnownObjectClassHandle(objectInstanceHandle3));
    assert testObjectClassHandle.equals(rtiAmbassadors.get(2).getKnownObjectClassHandle(objectInstanceHandle3));

    assert TEST_OBJECT.equals(rtiAmbassadors.get(0).getObjectInstanceName(objectInstanceHandle3));
    assert objectInstanceHandle3.equals(rtiAmbassadors.get(0).getObjectInstanceHandle(TEST_OBJECT));

    assert TEST_OBJECT.equals(rtiAmbassadors.get(2).getObjectInstanceName(objectInstanceHandle3));
    assert objectInstanceHandle3.equals(rtiAmbassadors.get(2).getObjectInstanceHandle(TEST_OBJECT));
  }

  @Test(dependsOnMethods = {"testRegisterObjectInstanceByName"}, expectedExceptions = {ObjectInstanceNameInUse.class})
  public void testRegisterObjectInstanceByNameAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle, TEST_OBJECT);
  }

  @Test(expectedExceptions = {ObjectInstanceNameNotReserved.class})
  public void testRegisterObjectInstanceWithUnreservedName()
    throws Exception
  {
    rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle, "xxx");
  }

  @Test(dependsOnMethods = {"testRegisterObjectInstance", "testRegisterObjectInstanceChild", "testRegisterObjectInstanceByName"})
  public void testLateSubscribe()
    throws Exception
  {
    rtiAmbassadors.get(3).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(4).subscribeObjectClassAttributes(testObjectClassHandle2, testObjectAttributeHandles2);

    federateAmbassadors.get(3).checkObjectInstanceHandle(objectInstanceHandle1);
    federateAmbassadors.get(3).checkObjectInstanceHandle(objectInstanceHandle2);
    federateAmbassadors.get(3).checkObjectInstanceHandle(objectInstanceHandle3);
    federateAmbassadors.get(3).checkObjectClassHandle(objectInstanceHandle1, testObjectClassHandle);
    federateAmbassadors.get(3).checkObjectClassHandle(objectInstanceHandle2, testObjectClassHandle);
    federateAmbassadors.get(3).checkObjectClassHandle(objectInstanceHandle3, testObjectClassHandle);

    federateAmbassadors.get(4).checkObjectInstanceHandle(objectInstanceHandle2);
    federateAmbassadors.get(4).checkObjectClassHandle(objectInstanceHandle2, testObjectClassHandle2);

    federateAmbassadors.get(3).checkObjectInstanceName(objectInstanceHandle3, TEST_OBJECT);
  }

  @Test(expectedExceptions = {ObjectClassNotDefined.class})
  public void testRegisterObjectInstanceWithNullObjectClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).registerObjectInstance(null);
  }

  @Test(expectedExceptions = {ObjectClassNotDefined.class})
  public void testRegisterObjectInstanceByNameWithNullObjectClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).registerObjectInstance(null, TEST_OBJECT);
  }

  @Test(expectedExceptions = {ObjectInstanceNameNotReserved.class})
  public void testRegisterObjectInstanceByNameWithNullObjectInstanceName()
    throws Exception
  {
    rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle, null);
  }

  @Test(expectedExceptions = {ObjectInstanceNameNotReserved.class})
  public void testRegisterObjectInstanceByNameWithEmptyObjectInstanceName()
    throws Exception
  {
    rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle, "");
  }

  @Test(expectedExceptions = {ObjectClassNotPublished.class})
  public void testRegisterObjectInstanceOfUnpublishedObjectClass()
    throws Exception
  {
    rtiAmbassadors.get(2).registerObjectInstance(testObjectClassHandle);
  }

  @Test(expectedExceptions = {ObjectClassNotPublished.class})
  public void testRegisterObjectInstanceByNameOfUnpublishedObjectClass()
    throws Exception
  {
    rtiAmbassadors.get(2).registerObjectInstance(testObjectClassHandle, TEST_OBJECT);
  }

  protected TestFederateAmbassador createFederateAmbassador(RTIambassador rtiAmbassador)
  {
    return new TestFederateAmbassador(rtiAmbassador);
  }

  public static class TestFederateAmbassador
    extends BaseFederateAmbassador
  {
    private final Set<String> reservedObjectInstanceNames = new HashSet<String>();

    private final Map<ObjectInstanceHandle, TestObjectInstance> objectInstances =
      new HashMap<ObjectInstanceHandle, TestObjectInstance>();

    public TestFederateAmbassador(RTIambassador rtiAmbassador)
    {
      super(rtiAmbassador);
    }

    public void checkObjectInstanceNameReserved(final String objectInstanceName)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return !reservedObjectInstanceNames.contains(objectInstanceName);
        }
      });

      assert reservedObjectInstanceNames.contains(objectInstanceName);
    }

    public void checkObjectInstanceHandle(final ObjectInstanceHandle objectInstanceHandle)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return !objectInstances.containsKey(objectInstanceHandle);
        }
      });

      assert objectInstances.containsKey(objectInstanceHandle);
    }

    public void checkObjectClassHandle(ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle)
      throws Exception
    {
      assert objectInstances.get(objectInstanceHandle).getObjectClassHandle().equals(objectClassHandle);
    }

    public void checkObjectInstanceName(ObjectInstanceHandle objectInstanceHandle, String objectInstanceName)
      throws Exception
    {
      assert objectInstances.get(objectInstanceHandle).getObjectInstanceName().equals(objectInstanceName);
    }

    @Override
    public void objectInstanceNameReservationSucceeded(String name)
      throws FederateInternalError
    {
      reservedObjectInstanceNames.add(name);
    }

    @Override
    public void discoverObjectInstance(
      ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle, String objectInstanceName)
      throws FederateInternalError
    {
      objectInstances.put(objectInstanceHandle, new TestObjectInstance(
        objectInstanceHandle, objectClassHandle, objectInstanceName));
    }
  }
}
