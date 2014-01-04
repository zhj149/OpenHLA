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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.AttributeHandleSet;
import hla.rti.ObjectAlreadyRegistered;
import hla.rti.ObjectClassNotDefined;
import hla.rti.ObjectClassNotPublished;
import hla.rti.RTIinternalError;
import hla.rti.ResignAction;
import hla.rti.jlc.RTIambassadorEx;

@Test
public class ObjectRegistrationTestNG
  extends BaseTestNG<ObjectRegistrationTestNG.TestFederateAmbassador>
{
  private static final String FEDERATION_NAME = ObjectRegistrationTestNG.class.getSimpleName();

  private int testObjectClassHandle;
  private AttributeHandleSet testObjectAttributeHandles;

  private int testObjectClassHandle2;
  private AttributeHandleSet testObjectAttributeHandles2;

  private String objectInstanceName1;
  private String objectInstanceName2;
  private String objectInstanceName3;

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
    int attributeHandle1 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE1, testObjectClassHandle);
    int attributeHandle2 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE2, testObjectClassHandle);
    int attributeHandle3 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE3, testObjectClassHandle);
    testObjectAttributeHandles = rtiFactory.createAttributeHandleSet();
    testObjectAttributeHandles.add(attributeHandle1);
    testObjectAttributeHandles.add(attributeHandle2);
    testObjectAttributeHandles.add(attributeHandle3);

    testObjectClassHandle2 = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT2);
    int attributeHandle4 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE4, testObjectClassHandle2);
    int attributeHandle5 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE5, testObjectClassHandle2);
    int attributeHandle6 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE6, testObjectClassHandle2);
    testObjectAttributeHandles2 = rtiFactory.createAttributeHandleSet();
    testObjectAttributeHandles2.add(attributeHandle1);
    testObjectAttributeHandles2.add(attributeHandle2);
    testObjectAttributeHandles2.add(attributeHandle3);
    testObjectAttributeHandles2.add(attributeHandle4);
    testObjectAttributeHandles2.add(attributeHandle5);
    testObjectAttributeHandles2.add(attributeHandle6);

    rtiAmbassadors.get(0).publishObjectClass(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(1).publishObjectClass(testObjectClassHandle2, testObjectAttributeHandles2);

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
    resignFederationExecution(ResignAction.RELEASE_ATTRIBUTES);
    destroyFederationExecution();
  }

  @Test
  public void testRegisterObjectInstance()
    throws Exception
  {
    int objectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);
    objectInstanceName1 = rtiAmbassadors.get(0).getObjectInstanceName(objectInstanceHandle);

    assert testObjectClassHandle == rtiAmbassadors.get(0).getObjectClass(objectInstanceHandle);

    federateAmbassadors.get(2).checkObjectInstanceName(objectInstanceName1);
    federateAmbassadors.get(2).checkObjectClassHandle(objectInstanceName1, testObjectClassHandle);
  }

  @Test
  public void testRegisterObjectInstanceChild()
    throws Exception
  {
    int objectInstanceHandle1 = rtiAmbassadors.get(1).registerObjectInstance(testObjectClassHandle2);
    objectInstanceName2 = rtiAmbassadors.get(1).getObjectInstanceName(objectInstanceHandle1);

    federateAmbassadors.get(0).checkObjectInstanceName(objectInstanceName2);
    federateAmbassadors.get(0).checkObjectClassHandle(objectInstanceName2, testObjectClassHandle);
    int objectInstanceHandle0 = rtiAmbassadors.get(1).getObjectInstanceHandle(objectInstanceName2);

    federateAmbassadors.get(2).checkObjectInstanceName(objectInstanceName2);
    federateAmbassadors.get(2).checkObjectClassHandle(objectInstanceName2, testObjectClassHandle2);
    int objectInstanceHandle2 = rtiAmbassadors.get(2).getObjectInstanceHandle(objectInstanceName2);

    assert testObjectClassHandle == rtiAmbassadors.get(0).getObjectClass(objectInstanceHandle0);
    assert testObjectClassHandle2 == rtiAmbassadors.get(1).getObjectClass(objectInstanceHandle1);
    assert testObjectClassHandle2 == rtiAmbassadors.get(2).getObjectClass(objectInstanceHandle2);
  }

  @Test
  public void testRegisterObjectInstanceByName()
    throws Exception
  {
    int objectInstanceHandle0 = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle, TEST_OBJECT);
    objectInstanceName3 = rtiAmbassadors.get(0).getObjectInstanceName(objectInstanceHandle0);

    federateAmbassadors.get(2).checkObjectInstanceName(objectInstanceName3);
    federateAmbassadors.get(2).checkObjectClassHandle(objectInstanceName3, testObjectClassHandle);
    int objectInstanceHandle2 = rtiAmbassadors.get(2).getObjectInstanceHandle(objectInstanceName3);

    assert testObjectClassHandle == rtiAmbassadors.get(0).getObjectClass(objectInstanceHandle0);
    assert testObjectClassHandle == rtiAmbassadors.get(2).getObjectClass(objectInstanceHandle2);
  }

  @Test(dependsOnMethods = "testRegisterObjectInstanceByName", expectedExceptions = ObjectAlreadyRegistered.class)
  public void testRegisterObjectInstanceByNameAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle, TEST_OBJECT);
  }

  @Test(dependsOnMethods = {"testRegisterObjectInstance", "testRegisterObjectInstanceChild", "testRegisterObjectInstanceByName"})
  public void testLateSubscribe()
    throws Exception
  {
    rtiAmbassadors.get(3).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(4).subscribeObjectClassAttributes(testObjectClassHandle2, testObjectAttributeHandles2);

    federateAmbassadors.get(3).checkObjectInstanceName(objectInstanceName1);
    federateAmbassadors.get(3).checkObjectInstanceName(objectInstanceName2);
    federateAmbassadors.get(3).checkObjectInstanceName(objectInstanceName3);
    federateAmbassadors.get(3).checkObjectClassHandle(objectInstanceName1, testObjectClassHandle);
    federateAmbassadors.get(3).checkObjectClassHandle(objectInstanceName2, testObjectClassHandle);
    federateAmbassadors.get(3).checkObjectClassHandle(objectInstanceName3, testObjectClassHandle);
    assert testObjectClassHandle == rtiAmbassadors.get(3).getObjectClass(
      rtiAmbassadors.get(3).getObjectInstanceHandle(objectInstanceName1));
    assert testObjectClassHandle == rtiAmbassadors.get(3).getObjectClass(
      rtiAmbassadors.get(3).getObjectInstanceHandle(objectInstanceName2));
    assert testObjectClassHandle == rtiAmbassadors.get(3).getObjectClass(
      rtiAmbassadors.get(3).getObjectInstanceHandle(objectInstanceName3));

    federateAmbassadors.get(4).checkObjectInstanceName(objectInstanceName2);
    federateAmbassadors.get(4).checkObjectClassHandle(objectInstanceName2, testObjectClassHandle2);
    assert testObjectClassHandle2 == rtiAmbassadors.get(4).getObjectClass(
      rtiAmbassadors.get(4).getObjectInstanceHandle(objectInstanceName2));
  }

  @Test(expectedExceptions = ObjectClassNotDefined.class)
  public void testRegisterObjectInstanceWithNullObjectClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).registerObjectInstance(-1);
  }

  @Test(expectedExceptions = ObjectClassNotDefined.class)
  public void testRegisterObjectInstanceByNameWithNullObjectClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).registerObjectInstance(-1, "xxx");
  }

  @Test(expectedExceptions = RTIinternalError.class)
  public void testRegisterObjectInstanceByNameWithNullObjectInstanceName()
    throws Exception
  {
    rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle, null);
  }

  @Test(expectedExceptions = RTIinternalError.class)
  public void testRegisterObjectInstanceByNameWithEmptyObjectInstanceName()
    throws Exception
  {
    rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle, "");
  }

  @Test(expectedExceptions = ObjectClassNotPublished.class)
  public void testRegisterObjectInstanceOfUnpublishedObjectClass()
    throws Exception
  {
    rtiAmbassadors.get(2).registerObjectInstance(testObjectClassHandle);
  }

  @Test(expectedExceptions = ObjectClassNotPublished.class)
  public void testRegisterObjectInstanceByNameOfUnpublishedObjectClass()
    throws Exception
  {
    rtiAmbassadors.get(2).registerObjectInstance(testObjectClassHandle, "yyy");
  }

  protected TestFederateAmbassador createFederateAmbassador(RTIambassadorEx rtiAmbassador)
  {
    return new TestFederateAmbassador(rtiAmbassador);
  }

  public static class TestFederateAmbassador
    extends BaseFederateAmbassador
  {
    private final Map<String, TestObjectInstance> objectInstances = new HashMap<String, TestObjectInstance>();

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

    public void checkObjectClassHandle(String objectInstanceName, int objectClassHandle)
      throws Exception
    {
      assert objectInstances.get(objectInstanceName).getObjectClassHandle() == objectClassHandle;
    }

    @Override
    public void discoverObjectInstance(int objectInstanceHandle, int objectClassHandle, String objectInstanceName)
    {
      objectInstances.put(objectInstanceName, new TestObjectInstance(
        objectInstanceHandle, objectClassHandle, objectInstanceName));
    }
  }
}
