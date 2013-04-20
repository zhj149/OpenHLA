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
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseTestNG;
import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseFederateAmbassador;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.exceptions.DeletePrivilegeNotHeld;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.ObjectInstanceNotKnown;

@Test
public class ObjectDeletionTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = "OHLA Object Deletion Test Federation";

  private final List<FederateHandle> federateHandles = new ArrayList<FederateHandle>(4);
  private final List<TestFederateAmbassador> federateAmbassadors = new ArrayList<TestFederateAmbassador>(4);

  private ObjectInstanceHandle testObjectInstanceHandle;
  private ObjectInstanceHandle testObjectInstanceHandle2;

  public ObjectDeletionTestNG()
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
    rtiAmbassadors.get(3).connect(federateAmbassadors.get(3), CallbackModel.HLA_EVOKED);

    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);

    federateHandles.add(rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE_1, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(1).joinFederationExecution(FEDERATE_TYPE_1, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(2).joinFederationExecution(FEDERATE_TYPE_1, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(3).joinFederationExecution(FEDERATE_TYPE_1, FEDERATION_NAME));

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

    rtiAmbassadors.remove(0).disconnect();

    resignFederationExecution(ResignAction.UNCONDITIONALLY_DIVEST_ATTRIBUTES);

    destroyFederationExecution(FEDERATION_NAME);

    disconnect();
  }

  @Test
  public void testDeleteObjectInstance()
    throws Exception
  {
    rtiAmbassadors.get(0).deleteObjectInstance(testObjectInstanceHandle2, TAG);

    federateAmbassadors.get(1).checkRemoved(testObjectInstanceHandle2, TAG, federateHandles.get(0));
    federateAmbassadors.get(2).checkRemoved(testObjectInstanceHandle2, TAG, federateHandles.get(0));
  }

  @Test(expectedExceptions = {ObjectInstanceNotKnown.class})
  public void testDeleteObjectInstanceWithNullObjectInstanceHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).deleteObjectInstance(null, TAG);
  }

  @Test(expectedExceptions = {DeletePrivilegeNotHeld.class})
  public void testDeleteUnownedObjectInstance()
    throws Exception
  {
    rtiAmbassadors.get(1).deleteObjectInstance(testObjectInstanceHandle, TAG);
  }

  @Test(expectedExceptions = {ObjectInstanceNotKnown.class})
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

    federateAmbassadors.get(1).checkRemoved(testObjectInstanceHandle, null, federateHandles.get(0));
  }

  private static class TestFederateAmbassador
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

    public void checkRemoved(final ObjectInstanceHandle objectInstanceHandle, byte[] tag, FederateHandle federateHandle)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return !objectInstances.get(objectInstanceHandle).isRemoved(); } });

      TestObjectInstance objectInstance = objectInstances.get(objectInstanceHandle);
      assert objectInstance != null;
      assert objectInstance.isRemoved();
      assert Arrays.equals(tag, objectInstance.getTag());
      assert objectInstance.getDeletingFederateHandle().equals(federateHandle);
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
    public void removeObjectInstance(
      ObjectInstanceHandle objectInstanceHandle, byte[] tag, OrderType sentOrderType, SupplementalRemoveInfo removeInfo)
      throws FederateInternalError
    {
      objectInstances.get(objectInstanceHandle).setRemoved(tag, removeInfo.getProducingFederate());
    }
  }
}
