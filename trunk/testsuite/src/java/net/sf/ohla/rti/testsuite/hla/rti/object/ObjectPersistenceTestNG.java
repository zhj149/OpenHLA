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
import java.util.UUID;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.AttributeHandleSet;
import hla.rti.ObjectAlreadyRegistered;
import hla.rti.ResignAction;
import hla.rti.jlc.RTIambassadorEx;

@Test
public class ObjectPersistenceTestNG
  extends BaseTestNG<ObjectPersistenceTestNG.TestFederateAmbassador>
{
  private static final String FEDERATION_NAME = ObjectPersistenceTestNG.class.getSimpleName();
  private static final String SAVE_NAME = FEDERATION_NAME + UUID.randomUUID();

  private int testObjectClassHandle;
  private int testObjectClassHandle2;

  private String testObjectInstanceName;
  private String testObjectInstanceName2;

  public ObjectPersistenceTestNG()
  {
    super(4, FEDERATION_NAME);
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
    AttributeHandleSet testObjectAttributeHandles = rtiFactory.createAttributeHandleSet();
    testObjectAttributeHandles.add(attributeHandle1);
    testObjectAttributeHandles.add(attributeHandle2);
    testObjectAttributeHandles.add(attributeHandle3);

    testObjectClassHandle2 = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT2);
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

    int testObjectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);
    int testObjectInstanceHandle2 = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle2);
    int testObjectInstanceHandle3 = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle, TEST_OBJECT);

    testObjectInstanceName = rtiAmbassadors.get(0).getObjectInstanceName(testObjectInstanceHandle);
    testObjectInstanceName2 = rtiAmbassadors.get(0).getObjectInstanceName(testObjectInstanceHandle2);

    // ensure the objects arrive
    //
    federateAmbassadors.get(1).checkObjectInstanceName(testObjectInstanceName);
    federateAmbassadors.get(1).checkObjectInstanceName(testObjectInstanceName2);
    federateAmbassadors.get(2).checkObjectInstanceName(testObjectInstanceName2);
    federateAmbassadors.get(1).checkObjectInstanceName(TEST_OBJECT);

    synchronize(SYNCHRONIZATION_POINT_SETUP_COMPLETE, federateAmbassadors);

    rtiAmbassadors.get(0).requestFederationSave(SAVE_NAME);

    federateAmbassadors.get(0).checkInitiateFederateSave(SAVE_NAME);
    federateAmbassadors.get(1).checkInitiateFederateSave(SAVE_NAME);
    federateAmbassadors.get(2).checkInitiateFederateSave(SAVE_NAME);
    federateAmbassadors.get(3).checkInitiateFederateSave(SAVE_NAME);

    rtiAmbassadors.get(0).federateSaveBegun();
    rtiAmbassadors.get(1).federateSaveBegun();
    rtiAmbassadors.get(2).federateSaveBegun();
    rtiAmbassadors.get(3).federateSaveBegun();

    rtiAmbassadors.get(0).federateSaveComplete();
    rtiAmbassadors.get(1).federateSaveComplete();
    rtiAmbassadors.get(2).federateSaveComplete();
    rtiAmbassadors.get(3).federateSaveComplete();

    federateAmbassadors.get(0).checkFederationSaved(SAVE_NAME);
    federateAmbassadors.get(1).checkFederationSaved(SAVE_NAME);
    federateAmbassadors.get(2).checkFederationSaved(SAVE_NAME);
    federateAmbassadors.get(3).checkFederationSaved(SAVE_NAME);

    resignFederationExecution(ResignAction.RELEASE_ATTRIBUTES);
    destroyFederationExecution();

    createFederationExecution();
    joinFederationExecution();

    rtiAmbassadors.get(0).requestFederationRestore(SAVE_NAME);

    federateAmbassadors.get(0).checkRequestFederationRestoreSucceeded(SAVE_NAME);

    federateAmbassadors.get(0).checkInitiateFederateRestore(SAVE_NAME, federateHandles.get(0));
    federateAmbassadors.get(1).checkInitiateFederateRestore(SAVE_NAME, federateHandles.get(1));
    federateAmbassadors.get(2).checkInitiateFederateRestore(SAVE_NAME, federateHandles.get(2));
    federateAmbassadors.get(3).checkInitiateFederateRestore(SAVE_NAME, federateHandles.get(3));

    rtiAmbassadors.get(0).federateRestoreComplete();
    rtiAmbassadors.get(1).federateRestoreComplete();
    rtiAmbassadors.get(2).federateRestoreComplete();
    rtiAmbassadors.get(3).federateRestoreComplete();

    federateAmbassadors.get(0).checkFederationRestored(SAVE_NAME);
    federateAmbassadors.get(1).checkFederationRestored(SAVE_NAME);
    federateAmbassadors.get(2).checkFederationRestored(SAVE_NAME);
    federateAmbassadors.get(3).checkFederationRestored(SAVE_NAME);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    resignFederationExecution(ResignAction.RELEASE_ATTRIBUTES);
    destroyFederationExecution();
  }

  @Test
  public void testGetObjectInstanceHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName);
    rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName);
    rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName2);
    rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName2);
    rtiAmbassadors.get(2).getObjectInstanceHandle(testObjectInstanceName2);
    rtiAmbassadors.get(0).getObjectInstanceHandle(TEST_OBJECT);
    rtiAmbassadors.get(1).getObjectInstanceHandle(TEST_OBJECT);
  }

  @Test
  public void testGetObjectInstanceName()
    throws Exception
  {
    assert testObjectInstanceName.equals(rtiAmbassadors.get(0).getObjectInstanceName(
      rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName)));
    assert testObjectInstanceName.equals(rtiAmbassadors.get(1).getObjectInstanceName(
      rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName)));

    assert testObjectInstanceName2.equals(rtiAmbassadors.get(0).getObjectInstanceName(
      rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName2)));
    assert testObjectInstanceName2.equals(rtiAmbassadors.get(1).getObjectInstanceName(
      rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName2)));
    assert testObjectInstanceName2.equals(rtiAmbassadors.get(2).getObjectInstanceName(
      rtiAmbassadors.get(2).getObjectInstanceHandle(testObjectInstanceName2)));

    assert TEST_OBJECT.equals(rtiAmbassadors.get(0).getObjectInstanceName(
      rtiAmbassadors.get(0).getObjectInstanceHandle(TEST_OBJECT)));
    assert TEST_OBJECT.equals(rtiAmbassadors.get(1).getObjectInstanceName(
      rtiAmbassadors.get(1).getObjectInstanceHandle(TEST_OBJECT)));
  }

  @Test
  public void testGetObjectClassHandle()
    throws Exception
  {
    assert testObjectClassHandle == rtiAmbassadors.get(0).getObjectClass(
      rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName));
    assert testObjectClassHandle == rtiAmbassadors.get(1).getObjectClass(
      rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName));
    assert testObjectClassHandle == rtiAmbassadors.get(1).getObjectClass(
      rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName2));
    assert testObjectClassHandle == rtiAmbassadors.get(0).getObjectClass(
      rtiAmbassadors.get(0).getObjectInstanceHandle(TEST_OBJECT));
    assert testObjectClassHandle == rtiAmbassadors.get(1).getObjectClass(
      rtiAmbassadors.get(1).getObjectInstanceHandle(TEST_OBJECT));

    assert testObjectClassHandle2 == rtiAmbassadors.get(0).getObjectClass(
      rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName2));
    assert testObjectClassHandle2 == rtiAmbassadors.get(2).getObjectClass(
      rtiAmbassadors.get(2).getObjectInstanceHandle(testObjectInstanceName2));
  }

  @Test(expectedExceptions = ObjectAlreadyRegistered.class)
  public void testRegisterObjectInstanceByNameAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle, TEST_OBJECT);
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
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return !objectInstances.containsKey(objectInstanceName);
        }
      });

      assert objectInstances.containsKey(objectInstanceName);
    }

    @Override
    public void reset()
    {
      super.reset();

      objectInstances.clear();
    }

    @Override
    public void discoverObjectInstance(int objectInstanceHandle, int objectClassHandle, String objectInstanceName)
    {
      objectInstances.put(objectInstanceName, new TestObjectInstance(
        objectInstanceHandle, objectClassHandle, objectInstanceName));
    }
  }
}
