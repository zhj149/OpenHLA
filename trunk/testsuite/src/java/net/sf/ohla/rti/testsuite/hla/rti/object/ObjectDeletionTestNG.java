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
import java.util.concurrent.locks.LockSupport;

import net.sf.ohla.rti.testsuite.hla.rti.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.AttributeHandleSet;
import hla.rti.DeletePrivilegeNotHeld;
import hla.rti.FederateInternalError;
import hla.rti.ObjectNotKnown;
import hla.rti.ResignAction;
import hla.rti.jlc.NullFederateAmbassador;
import hla.rti.jlc.RTIambassadorEx;

@Test
public class ObjectDeletionTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = "OHLA HLA 1.3 Object Deletion Test Federation";

  private final List<TestFederateAmbassador> federateAmbassadors = new ArrayList<TestFederateAmbassador>(4);

  private int testObjectInstanceHandle;
  private int testObjectInstanceHandle2;

  private String testObjectInstanceName;
  private String testObjectInstanceName2;

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
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    rtiAmbassadors.get(1).resignFederationExecution(ResignAction.RELEASE_ATTRIBUTES);
    rtiAmbassadors.get(2).resignFederationExecution(ResignAction.RELEASE_ATTRIBUTES);
    rtiAmbassadors.get(3).resignFederationExecution(ResignAction.RELEASE_ATTRIBUTES);

    // this is necessary to ensure the federates is actually resigned
    //
    LockSupport.parkUntil(System.currentTimeMillis() + 1000);

    rtiAmbassadors.get(0).destroyFederationExecution(FEDERATION_NAME);
  }

  @Test
  public void testDeleteObjectInstance()
    throws Exception
  {
    rtiAmbassadors.get(0).deleteObjectInstance(testObjectInstanceHandle2, TAG);

    federateAmbassadors.get(1).checkRemoved(testObjectInstanceName2, TAG);
    federateAmbassadors.get(2).checkRemoved(testObjectInstanceName2, TAG);
  }

  @Test(expectedExceptions = {ObjectNotKnown.class})
  public void testDeleteObjectInstanceWithInvalidObjectInstanceHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).deleteObjectInstance(-1, TAG);
  }

  @Test(expectedExceptions = {DeletePrivilegeNotHeld.class})
  public void testDeleteUnownedObjectInstance()
    throws Exception
  {
    rtiAmbassadors.get(1).deleteObjectInstance(
      rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName), TAG);
  }

  @Test(expectedExceptions = {ObjectNotKnown.class})
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

  private static class TestFederateAmbassador
    extends NullFederateAmbassador
  {
    private final RTIambassadorEx rtiAmbassador;

    private final Map<String, TestObjectInstance> objectInstances =
      new HashMap<String, TestObjectInstance>();
    private final Map<Integer, TestObjectInstance> objectInstancesByHandle =
      new HashMap<Integer, TestObjectInstance>();

    public TestFederateAmbassador(RTIambassadorEx rtiAmbassador)
    {
      this.rtiAmbassador = rtiAmbassador;
    }

    public void checkObjectInstanceName(String objectInstanceName)
      throws Exception
    {
      for (int i = 0; i < 5 && !objectInstances.containsKey(objectInstanceName); i++)
      {
        rtiAmbassador.tick(.01, 1.0);
      }
      assert objectInstances.containsKey(objectInstanceName);
    }

    public void checkRemoved(String objectInstanceName, byte[] tag)
      throws Exception
    {
      for (int i = 0; i < 5 && !objectInstances.get(objectInstanceName).isRemoved(); i++)
      {
        rtiAmbassador.tick(.01, 1.0);
      }
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
