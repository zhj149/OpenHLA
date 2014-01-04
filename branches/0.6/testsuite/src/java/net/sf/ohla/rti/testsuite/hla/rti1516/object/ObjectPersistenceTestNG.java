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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ObjectInstanceNameInUse;
import hla.rti1516.OrderType;
import hla.rti1516.RTIambassador;
import hla.rti1516.ResignAction;

@Test
public class ObjectPersistenceTestNG
  extends BaseTestNG<ObjectPersistenceTestNG.TestFederateAmbassador>
{
  private static final String FEDERATION_NAME = ObjectPersistenceTestNG.class.getSimpleName();
  private static final String SAVE_NAME = FEDERATION_NAME + UUID.randomUUID();

  private ObjectClassHandle testObjectClassHandle;
  private ObjectClassHandle testObjectClassHandle2;

  private ObjectInstanceHandle testObjectInstanceHandle;
  private ObjectInstanceHandle testObjectInstanceHandle2;
  private ObjectInstanceHandle testObjectInstanceHandle3;

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
    AttributeHandle attributeHandle1 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE1);
    AttributeHandle attributeHandle2 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE2);
    AttributeHandle attributeHandle3 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE3);
    AttributeHandleSet testObjectAttributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    testObjectAttributeHandles.add(attributeHandle1);
    testObjectAttributeHandles.add(attributeHandle2);
    testObjectAttributeHandles.add(attributeHandle3);

    testObjectClassHandle2 = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT2);
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

    rtiAmbassadors.get(0).reserveObjectInstanceName(TEST_OBJECT);
    federateAmbassadors.get(0).checkObjectInstanceNameReserved(TEST_OBJECT);
    testObjectInstanceHandle3 = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle, TEST_OBJECT);

    testObjectInstanceName = rtiAmbassadors.get(0).getObjectInstanceName(testObjectInstanceHandle);
    testObjectInstanceName2 = rtiAmbassadors.get(0).getObjectInstanceName(testObjectInstanceHandle2);

    // ensure the objects arrive
    //
    federateAmbassadors.get(1).checkObjectInstanceHandle(testObjectInstanceHandle);
    federateAmbassadors.get(1).checkObjectInstanceHandle(testObjectInstanceHandle2);
    federateAmbassadors.get(2).checkObjectInstanceHandle(testObjectInstanceHandle2);
    federateAmbassadors.get(1).checkObjectInstanceHandle(testObjectInstanceHandle3);

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

    resignFederationExecution(ResignAction.UNCONDITIONALLY_DIVEST_ATTRIBUTES);
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
    resignFederationExecution(ResignAction.UNCONDITIONALLY_DIVEST_ATTRIBUTES);
    destroyFederationExecution();
  }

  @Test
  public void testGetObjectInstanceHandle()
    throws Exception
  {
    assert testObjectInstanceHandle.equals(rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName));
    assert testObjectInstanceHandle.equals(rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName));

    assert testObjectInstanceHandle2.equals(rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName2));
    assert testObjectInstanceHandle2.equals(rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName2));
    assert testObjectInstanceHandle2.equals(rtiAmbassadors.get(2).getObjectInstanceHandle(testObjectInstanceName2));
  }

  @Test
  public void testGetObjectInstanceName()
    throws Exception
  {
    assert testObjectInstanceName.equals(rtiAmbassadors.get(0).getObjectInstanceName(testObjectInstanceHandle));
    assert testObjectInstanceName.equals(rtiAmbassadors.get(1).getObjectInstanceName(testObjectInstanceHandle));

    assert testObjectInstanceName2.equals(rtiAmbassadors.get(0).getObjectInstanceName(testObjectInstanceHandle2));
    assert testObjectInstanceName2.equals(rtiAmbassadors.get(1).getObjectInstanceName(testObjectInstanceHandle2));
    assert testObjectInstanceName2.equals(rtiAmbassadors.get(2).getObjectInstanceName(testObjectInstanceHandle2));
  }

  @Test
  public void testKnownObjectClassHandle()
    throws Exception
  {
    assert testObjectClassHandle.equals(rtiAmbassadors.get(0).getKnownObjectClassHandle(testObjectInstanceHandle));
    assert testObjectClassHandle.equals(rtiAmbassadors.get(1).getKnownObjectClassHandle(testObjectInstanceHandle));
    assert testObjectClassHandle.equals(rtiAmbassadors.get(1).getKnownObjectClassHandle(testObjectInstanceHandle2));

    assert testObjectClassHandle2.equals(rtiAmbassadors.get(0).getKnownObjectClassHandle(testObjectInstanceHandle2));
    assert testObjectClassHandle2.equals(rtiAmbassadors.get(2).getKnownObjectClassHandle(testObjectInstanceHandle2));
  }

  @Test
  public void testReserveObjectInstanceNameAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).reserveObjectInstanceName(TEST_OBJECT);
    federateAmbassadors.get(0).checkObjectInstanceNameNotReserved(TEST_OBJECT);
  }

  @Test(expectedExceptions = ObjectInstanceNameInUse.class)
  public void testRegisterObjectInstanceByNameAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle, TEST_OBJECT);
  }

  protected TestFederateAmbassador createFederateAmbassador(RTIambassador rtiAmbassador)
  {
    return new TestFederateAmbassador(rtiAmbassador);
  }

  public static class TestFederateAmbassador
    extends BaseFederateAmbassador
  {
    private final Set<String> successfullyReservedObjectInstanceNames = new HashSet<String>();
    private final Set<String> unsuccessfullyReservedObjectInstanceNames = new HashSet<String>();

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
          return !successfullyReservedObjectInstanceNames.contains(objectInstanceName);
        }
      });

      assert successfullyReservedObjectInstanceNames.contains(objectInstanceName);
    }

    public void checkObjectInstanceNameNotReserved(final String objectInstanceName)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return !unsuccessfullyReservedObjectInstanceNames.contains(objectInstanceName);
        }
      });

      assert unsuccessfullyReservedObjectInstanceNames.contains(objectInstanceName);
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
    public void reset()
    {
      super.reset();

      successfullyReservedObjectInstanceNames.clear();
      unsuccessfullyReservedObjectInstanceNames.clear();

      objectInstances.clear();
    }

    @Override
    public void objectInstanceNameReservationSucceeded(String name)
      throws FederateInternalError
    {
      successfullyReservedObjectInstanceNames.add(name);
    }

    @Override
    public void objectInstanceNameReservationFailed(String name)
      throws FederateInternalError
    {
      unsuccessfullyReservedObjectInstanceNames.add(name);
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
    public void removeObjectInstance(
      ObjectInstanceHandle objectInstanceHandle, byte[] tag, OrderType sentOrderType)
      throws FederateInternalError
    {
      objectInstances.get(objectInstanceHandle).setRemoved(tag);
    }
  }
}
