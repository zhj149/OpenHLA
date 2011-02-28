/*
 * Copyright (c) 2005-2011, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.testsuite.hla.rti1516e.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.LockSupport;

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.ObjectClassNotDefined;
import hla.rti1516e.exceptions.ObjectClassNotPublished;
import hla.rti1516e.exceptions.ObjectInstanceNameInUse;
import hla.rti1516e.exceptions.ObjectInstanceNameNotReserved;

@Test
public class ObjectRegistrationTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = "OHLA Object Registration Test Federation";

  private final List<FederateHandle> federateHandles = new ArrayList<FederateHandle>(5);
  private final List<TestFederateAmbassador> federateAmbassadors = new ArrayList<TestFederateAmbassador>(5);

  private ObjectClassHandle testObjectClassHandle;
  private AttributeHandleSet testObjectAttributeHandles;

  private ObjectClassHandle testObjectClassHandle2;
  private AttributeHandleSet testObjectAttributeHandles2;

  private ObjectInstanceHandle objectInstanceHandle1;
  private ObjectInstanceHandle objectInstanceHandle2;
  private ObjectInstanceHandle objectInstanceHandle3;

  public ObjectRegistrationTestNG()
  {
    super(5);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(0)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(1)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(2)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(3)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(4)));

    rtiAmbassadors.get(0).connect(federateAmbassadors.get(0), CallbackModel.HLA_EVOKED);
    rtiAmbassadors.get(1).connect(federateAmbassadors.get(1), CallbackModel.HLA_EVOKED);
    rtiAmbassadors.get(2).connect(federateAmbassadors.get(2), CallbackModel.HLA_EVOKED);
    rtiAmbassadors.get(3).connect(federateAmbassadors.get(3), CallbackModel.HLA_EVOKED);
    rtiAmbassadors.get(4).connect(federateAmbassadors.get(4), CallbackModel.HLA_EVOKED);

    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);

    federateHandles.add(rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(1).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(2).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(3).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(4).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME));

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
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.UNCONDITIONALLY_DIVEST_ATTRIBUTES);
    rtiAmbassadors.get(1).resignFederationExecution(ResignAction.UNCONDITIONALLY_DIVEST_ATTRIBUTES);
    rtiAmbassadors.get(2).resignFederationExecution(ResignAction.UNCONDITIONALLY_DIVEST_ATTRIBUTES);
    rtiAmbassadors.get(3).resignFederationExecution(ResignAction.UNCONDITIONALLY_DIVEST_ATTRIBUTES);
    rtiAmbassadors.get(4).resignFederationExecution(ResignAction.UNCONDITIONALLY_DIVEST_ATTRIBUTES);

    // this is necessary to ensure the federates is actually resigned
    //
    LockSupport.parkUntil(System.currentTimeMillis() + 1000);

    rtiAmbassadors.get(0).destroyFederationExecution(FEDERATION_NAME);

    rtiAmbassadors.get(0).disconnect();
    rtiAmbassadors.get(1).disconnect();
    rtiAmbassadors.get(2).disconnect();
    rtiAmbassadors.get(3).disconnect();
    rtiAmbassadors.get(4).disconnect();
  }

  @Test
  public void testRegisterObjectInstance()
    throws Exception
  {
    objectInstanceHandle1 = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);

    federateAmbassadors.get(2).checkObjectInstanceHandle(objectInstanceHandle1);
    federateAmbassadors.get(2).checkObjectClassHandle(objectInstanceHandle1, testObjectClassHandle);
    federateAmbassadors.get(2).checkProducingFederateHandle(objectInstanceHandle1, federateHandles.get(0));

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
    federateAmbassadors.get(0).checkProducingFederateHandle(objectInstanceHandle2, federateHandles.get(1));

    federateAmbassadors.get(2).checkObjectInstanceHandle(objectInstanceHandle2);
    federateAmbassadors.get(2).checkObjectClassHandle(objectInstanceHandle2, testObjectClassHandle2);
    federateAmbassadors.get(2).checkProducingFederateHandle(objectInstanceHandle2, federateHandles.get(1));

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
    federateAmbassadors.get(2).checkProducingFederateHandle(objectInstanceHandle3, federateHandles.get(0));

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
    federateAmbassadors.get(3).checkProducingFederateHandle(objectInstanceHandle1, federateHandles.get(0));
    federateAmbassadors.get(3).checkProducingFederateHandle(objectInstanceHandle2, federateHandles.get(1));
    federateAmbassadors.get(3).checkProducingFederateHandle(objectInstanceHandle3, federateHandles.get(0));

    federateAmbassadors.get(4).checkObjectInstanceHandle(objectInstanceHandle2);
    federateAmbassadors.get(4).checkObjectClassHandle(objectInstanceHandle2, testObjectClassHandle2);
    federateAmbassadors.get(4).checkProducingFederateHandle(objectInstanceHandle2, federateHandles.get(1));

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

  private static class TestFederateAmbassador
    extends NullFederateAmbassador
  {
    private final RTIambassador rtiAmbassador;

    private final Set<String> reservedObjectInstanceNames = new HashSet<String>();

    private final Map<ObjectInstanceHandle, TestObjectInstance> objectInstances =
      new HashMap<ObjectInstanceHandle, TestObjectInstance>();

    public TestFederateAmbassador(RTIambassador rtiAmbassador)
    {
      this.rtiAmbassador = rtiAmbassador;
    }

    public void checkObjectInstanceNameReserved(String objectInstanceName)
      throws Exception
    {
      for (int i = 0; i < 5 && !reservedObjectInstanceNames.contains(objectInstanceName); i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert reservedObjectInstanceNames.contains(objectInstanceName);
    }

    public void checkObjectInstanceHandle(ObjectInstanceHandle objectInstanceHandle)
      throws Exception
    {
      for (int i = 0; i < 5 && !objectInstances.containsKey(objectInstanceHandle); i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
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

    public void checkProducingFederateHandle(ObjectInstanceHandle objectInstanceHandle, FederateHandle federateHandle)
      throws Exception
    {
      assert objectInstances.get(objectInstanceHandle).getProducingFederateHandle().equals(federateHandle);
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
        objectInstanceHandle, objectClassHandle, objectInstanceName, null));
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
  }
}
