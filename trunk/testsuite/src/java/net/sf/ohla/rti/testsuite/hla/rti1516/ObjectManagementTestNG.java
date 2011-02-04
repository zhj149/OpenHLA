/*
 * Copyright (c) 2006-2007, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.testsuite.hla.rti1516;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.DeletePrivilegeNotHeld;
import hla.rti1516.FederateInternalError;
import hla.rti1516.IllegalName;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectClassNotPublished;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ObjectInstanceNameInUse;
import hla.rti1516.ObjectInstanceNameNotReserved;
import hla.rti1516.ObjectInstanceNotKnown;
import hla.rti1516.OrderType;
import hla.rti1516.ParameterHandle;
import hla.rti1516.ParameterHandleValueMap;
import hla.rti1516.RTIambassador;
import hla.rti1516.ResignAction;
import hla.rti1516.TransportationType;
import hla.rti1516.UnknownName;
import hla.rti1516.jlc.NullFederateAmbassador;

@Test(groups = {"Object Management"})
public class ObjectManagementTestNG
  extends BaseTestNG
{
  protected List<TestFederateAmbassador> federateAmbassadors =
    new ArrayList<TestFederateAmbassador>(3);

  protected ObjectClassHandle testObjectClassHandle;
  protected AttributeHandle attributeHandle1;
  protected AttributeHandle attributeHandle2;
  protected AttributeHandle attributeHandle3;
  protected AttributeHandleSet testObjectAttributeHandles;

  protected ObjectClassHandle testObjectClassHandle2;
  protected AttributeHandle attributeHandle4;
  protected AttributeHandle attributeHandle5;
  protected AttributeHandle attributeHandle6;
  protected AttributeHandleSet testObjectAttributeHandles2;

  protected ObjectInstanceHandle objectInstanceHandle;
  protected ObjectInstanceHandle objectInstanceHandle2;
  protected ObjectInstanceHandle objectInstanceHandleByName;

  protected InteractionClassHandle testInteractionClassHandle;
  protected ParameterHandle parameterHandle1;
  protected ParameterHandle parameterHandle2;
  protected ParameterHandle parameterHandle3;

  protected InteractionClassHandle testInteractionClassHandle2;
  protected ParameterHandle parameterHandle4;
  protected ParameterHandle parameterHandle5;
  protected ParameterHandle parameterHandle6;

  public ObjectManagementTestNG()
  {
    super(3);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);

    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(0)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(1)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(2)));

    rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(0),
      mobileFederateServices);

    testObjectClassHandle =
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    attributeHandle1 = rtiAmbassadors.get(0).getAttributeHandle(
      testObjectClassHandle, ATTRIBUTE1);
    attributeHandle2 = rtiAmbassadors.get(0).getAttributeHandle(
      testObjectClassHandle, ATTRIBUTE2);
    attributeHandle3 = rtiAmbassadors.get(0).getAttributeHandle(
      testObjectClassHandle, ATTRIBUTE3);

    testObjectAttributeHandles =
      rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    testObjectAttributeHandles.add(attributeHandle1);
    testObjectAttributeHandles.add(attributeHandle2);
    testObjectAttributeHandles.add(attributeHandle3);

    testObjectClassHandle2 =
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT2);

    attributeHandle4 = rtiAmbassadors.get(0).getAttributeHandle(
      testObjectClassHandle2, ATTRIBUTE4);
    attributeHandle5 = rtiAmbassadors.get(0).getAttributeHandle(
      testObjectClassHandle2, ATTRIBUTE5);
    attributeHandle6 = rtiAmbassadors.get(0).getAttributeHandle(
      testObjectClassHandle2, ATTRIBUTE6);

    testObjectAttributeHandles2 =
      rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    testObjectAttributeHandles2.add(attributeHandle1);
    testObjectAttributeHandles2.add(attributeHandle2);
    testObjectAttributeHandles2.add(attributeHandle3);
    testObjectAttributeHandles2.add(attributeHandle4);
    testObjectAttributeHandles2.add(attributeHandle5);
    testObjectAttributeHandles2.add(attributeHandle6);

    rtiAmbassadors.get(0).publishObjectClassAttributes(
      testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(0).subscribeObjectClassAttributes(
      testObjectClassHandle, testObjectAttributeHandles);

    testInteractionClassHandle =
      rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
    testInteractionClassHandle2 =
      rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION2);

    parameterHandle1 = rtiAmbassadors.get(0).getParameterHandle(
      testInteractionClassHandle, PARAMETER1);
    parameterHandle2 = rtiAmbassadors.get(0).getParameterHandle(
      testInteractionClassHandle, PARAMETER2);
    parameterHandle3 = rtiAmbassadors.get(0).getParameterHandle(
      testInteractionClassHandle, PARAMETER3);
    parameterHandle4 = rtiAmbassadors.get(0).getParameterHandle(
      testInteractionClassHandle2, PARAMETER4);
    parameterHandle5 = rtiAmbassadors.get(0).getParameterHandle(
      testInteractionClassHandle2, PARAMETER5);
    parameterHandle6 = rtiAmbassadors.get(0).getParameterHandle(
      testInteractionClassHandle2, PARAMETER6);

    rtiAmbassadors.get(0).publishInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(0).subscribeInteractionClass(testInteractionClassHandle);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(1).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(2).resignFederationExecution(ResignAction.NO_ACTION);

    rtiAmbassadors.get(0).destroyFederationExecution(FEDERATION_NAME);
  }

  @Test(expectedExceptions = {IllegalName.class})
  public void testReserveIllegalObjectInstanceName()
    throws Exception
  {
    rtiAmbassadors.get(0).reserveObjectInstanceName(
      "HLAtestReserveIllegalObjectInstanceName");
  }

  @Test
  public void testReserveObjectInstanceName()
    throws Exception
  {
    rtiAmbassadors.get(0).reserveObjectInstanceName(
      "testRegisterObjectInstanceByName");

    federateAmbassadors.get(0).checkObjectInstanceNameReserved(
      "testRegisterObjectInstanceByName");
  }

  @Test(dependsOnMethods = {"testReserveObjectInstanceName"})
  public void testRegisterObjectInstanceByName()
    throws Exception
  {
    objectInstanceHandleByName =
      rtiAmbassadors.get(0).registerObjectInstance(
        testObjectClassHandle, "testRegisterObjectInstanceByName");

    // ensure the same name comes back
    //
    assert "testRegisterObjectInstanceByName".equals(
      rtiAmbassadors.get(0).getObjectInstanceName(objectInstanceHandleByName));
  }

  @Test(dependsOnMethods = {"testRegisterObjectInstanceByName"},
        expectedExceptions = {ObjectInstanceNameInUse.class})
  public void testRegisterObjectInstanceByNameAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).registerObjectInstance(
      testObjectClassHandle, "testRegisterObjectInstanceByName");
  }

  @Test(dependsOnMethods = {"testRegisterObjectInstanceByNameAgain"})
  public void testDeleteObjectInstanceByName()
    throws Exception
  {
    rtiAmbassadors.get(0).deleteObjectInstance(
      objectInstanceHandleByName, null);
  }

  @Test(dependsOnMethods = {"testDeleteObjectInstanceByName"},
        expectedExceptions = {IllegalName.class})
  public void testReserveRetiredObjectInstanceName()
    throws Exception
  {
    rtiAmbassadors.get(0).reserveObjectInstanceName(
      "testRegisterObjectInstanceByName");
  }

  @Test(dependsOnMethods = {"testDeleteObjectInstanceByName"},
        expectedExceptions = {ObjectInstanceNameInUse.class,
          ObjectInstanceNameNotReserved.class})
  public void testRegisterObjectInstanceWithRetiredName()
    throws Exception
  {
    rtiAmbassadors.get(0).registerObjectInstance(
      testObjectClassHandle, "testRegisterObjectInstanceByName");
  }

  @Test(expectedExceptions = {ObjectInstanceNameNotReserved.class})
  public void testRegisterObjectInstanceWithoutReserving()
    throws Exception
  {
    rtiAmbassadors.get(0).registerObjectInstance(
      testObjectClassHandle, "testRegisterObjectInstanceWithoutReserving");
  }

  @Test(expectedExceptions = {ObjectClassNotPublished.class})
  public void testRegisterObjectInstanceOfUnpublishedObjectClass()
    throws Exception
  {
    rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle2);
  }

  @Test(dependsOnMethods = {"testDeleteObjectInstanceByName"},
        expectedExceptions = {ObjectInstanceNotKnown.class})
  public void testDeleteObjectInstanceThatDoesNotExist()
    throws Exception
  {
    rtiAmbassadors.get(0).deleteObjectInstance(
      objectInstanceHandleByName, null);
  }

  @Test
  public void testRegisterObjectFrom1stFederate()
    throws Exception
  {
    objectInstanceHandle =
      rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);
  }

  @Test(dependsOnMethods = {"testRegisterObjectFrom1stFederate"})
  public void testJoin2ndFederate()
    throws Exception
  {
    rtiAmbassadors.get(1).joinFederationExecution(
      FEDERATE_TYPE + "2", FEDERATION_NAME, federateAmbassadors.get(1),
      mobileFederateServices);

    rtiAmbassadors.get(1).publishObjectClassAttributes(
      testObjectClassHandle2, testObjectAttributeHandles2);
    rtiAmbassadors.get(1).subscribeObjectClassAttributes(
      testObjectClassHandle2, testObjectAttributeHandles2);

    rtiAmbassadors.get(1).publishInteractionClass(testInteractionClassHandle2);
    rtiAmbassadors.get(1).subscribeInteractionClass(
      testInteractionClassHandle2);

    objectInstanceHandle2 =
      rtiAmbassadors.get(1).registerObjectInstance(testObjectClassHandle2);

    federateAmbassadors.get(0).checkObjectInstanceHandle(objectInstanceHandle2);
  }

  @Test(dependsOnMethods = {"testJoin2ndFederate"})
  public void testJoin3rdFederate()
    throws Exception
  {
    rtiAmbassadors.get(2).joinFederationExecution(
      FEDERATE_TYPE + "3", FEDERATION_NAME, federateAmbassadors.get(2),
      mobileFederateServices);

    rtiAmbassadors.get(2).subscribeObjectClassAttributes(
      testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(2).subscribeInteractionClass(testInteractionClassHandle);

    federateAmbassadors.get(2).checkObjectInstanceHandle(
      objectInstanceHandle, objectInstanceHandle2);
  }

  @Test(dependsOnMethods = {"testJoin3rdFederate"})
  public void testUpdateObjectFrom1stFederate()
    throws Exception
  {
    AttributeHandleValueMap attributeValues =
      rtiAmbassadors.get(0).getAttributeHandleValueMapFactory().create(3);
    attributeValues.put(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());
    attributeValues.put(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
    attributeValues.put(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());

    rtiAmbassadors.get(0).updateAttributeValues(
      objectInstanceHandle, attributeValues, null);

    federateAmbassadors.get(2).checkAttributeValues(
      objectInstanceHandle, attributeValues);
  }

  @Test(dependsOnMethods = {"testUpdateObjectFrom1stFederate"})
  public void testUpdateObjectFrom2ndFederate()
    throws Exception
  {
    AttributeHandleValueMap attributeValues =
      rtiAmbassadors.get(1).getAttributeHandleValueMapFactory().create(3);
    attributeValues.put(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());
    attributeValues.put(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
    attributeValues.put(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());

    AttributeHandleValueMap attributeValues2 =
      rtiAmbassadors.get(1).getAttributeHandleValueMapFactory().create(6);
    attributeValues2.put(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());
    attributeValues2.put(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
    attributeValues2.put(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());
    attributeValues2.put(attributeHandle4, ATTRIBUTE4_VALUE.getBytes());
    attributeValues2.put(attributeHandle5, ATTRIBUTE5_VALUE.getBytes());
    attributeValues2.put(attributeHandle6, ATTRIBUTE6_VALUE.getBytes());

    rtiAmbassadors.get(1).updateAttributeValues(
      objectInstanceHandle2, attributeValues2, null);

    federateAmbassadors.get(0).checkAttributeValues(
      objectInstanceHandle2, attributeValues);
    federateAmbassadors.get(2).checkAttributeValues(
      objectInstanceHandle2, attributeValues);
  }

  @Test(dependsOnMethods = {"testUpdateObjectFrom2ndFederate"},
        expectedExceptions = {DeletePrivilegeNotHeld.class})
  public void testDeleteUnownedObject()
    throws Exception
  {
    rtiAmbassadors.get(2).deleteObjectInstance(objectInstanceHandle, null);
  }

  @Test(dependsOnMethods = {"testDeleteUnownedObject"})
  public void testDeleteObjectFrom1stFederate()
    throws Exception
  {
    rtiAmbassadors.get(0).deleteObjectInstance(objectInstanceHandle, null);

    federateAmbassadors.get(2).checkForRemovedObjectInstanceHandle(
      objectInstanceHandle);
  }

  @Test(dependsOnMethods = {"testDeleteObjectFrom1stFederate"})
  public void testDeleteObjectFrom2ndFederate()
    throws Exception
  {
    rtiAmbassadors.get(1).deleteObjectInstance(objectInstanceHandle2, null);

    federateAmbassadors.get(0).checkForRemovedObjectInstanceHandle(
      objectInstanceHandle2);
    federateAmbassadors.get(2).checkForRemovedObjectInstanceHandle(
      objectInstanceHandle2);
  }

  @Test(dependsOnMethods = {"testDeleteObjectFrom2ndFederate"},
        expectedExceptions = {ObjectInstanceNotKnown.class})
  public void testDeleteUnknownObject()
    throws Exception
  {
    rtiAmbassadors.get(2).deleteObjectInstance(objectInstanceHandle, null);
  }

  @Test(dependsOnMethods = {"testDeleteUnknownObject"})
  public void testSendInteractionFrom1stFederate()
    throws Exception
  {
    ParameterHandleValueMap parameterValues =
      rtiAmbassadors.get(0).getParameterHandleValueMapFactory().create(3);
    parameterValues.put(parameterHandle1, PARAMETER1_VALUE.getBytes());
    parameterValues.put(parameterHandle2, PARAMETER2_VALUE.getBytes());
    parameterValues.put(parameterHandle3, PARAMETER3_VALUE.getBytes());

    rtiAmbassadors.get(0).sendInteraction(
      testInteractionClassHandle, parameterValues, null);

    federateAmbassadors.get(2).checkParameterValues(parameterValues);
  }

  @Test(dependsOnMethods = {"testSendInteractionFrom1stFederate"})
  public void testSendInteractionFrom2ndFederate()
    throws Exception
  {
    ParameterHandleValueMap parameterValues =
      rtiAmbassadors.get(1).getParameterHandleValueMapFactory().create(3);
    parameterValues.put(parameterHandle1, PARAMETER1_VALUE.getBytes());
    parameterValues.put(parameterHandle2, PARAMETER2_VALUE.getBytes());
    parameterValues.put(parameterHandle3, PARAMETER3_VALUE.getBytes());

    ParameterHandleValueMap parameterValues2 =
      rtiAmbassadors.get(1).getParameterHandleValueMapFactory().create(3);
    parameterValues2.put(parameterHandle1, PARAMETER1_VALUE.getBytes());
    parameterValues2.put(parameterHandle2, PARAMETER2_VALUE.getBytes());
    parameterValues2.put(parameterHandle3, PARAMETER3_VALUE.getBytes());
    parameterValues2.put(parameterHandle4, PARAMETER4_VALUE.getBytes());
    parameterValues2.put(parameterHandle5, PARAMETER5_VALUE.getBytes());
    parameterValues2.put(parameterHandle6, PARAMETER6_VALUE.getBytes());

    rtiAmbassadors.get(1).sendInteraction(
      testInteractionClassHandle2, parameterValues2, null);

    federateAmbassadors.get(0).checkParameterValues(parameterValues);
    federateAmbassadors.get(2).checkParameterValues(parameterValues);
  }

  protected static class TestFederateAmbassador
    extends NullFederateAmbassador
  {
    protected RTIambassador rtiAmbassador;

    protected Set<String> reservedObjectInstanceNames =
      new HashSet<String>();

    protected Set<String> notReservedObjectInstanceNames =
      new HashSet<String>();

    protected Map<ObjectInstanceHandle, ObjectInstance> objectInstances =
      new HashMap<ObjectInstanceHandle, ObjectInstance>();

    protected ParameterHandleValueMap parameterValues;

    public TestFederateAmbassador(RTIambassador rtiAmbassador)
    {
      this.rtiAmbassador = rtiAmbassador;
    }

    public void checkObjectInstanceNameReserved(String objectInstanceName)
      throws Exception
    {
      for (int i = 0;
           i < 5 && !reservedObjectInstanceNames.contains(objectInstanceName);
           i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert reservedObjectInstanceNames.contains(objectInstanceName);
    }

    public void checkObjectInstanceNameNotReserved(String objectInstanceName)
      throws Exception
    {
      for (int i = 0;
           i < 5 &&
           !notReservedObjectInstanceNames.contains(objectInstanceName); i++)
      {
        rtiAmbassador.evokeCallback(0.1);
      }
      assert notReservedObjectInstanceNames.contains(objectInstanceName);
    }

    public void checkObjectInstanceHandle(
      ObjectInstanceHandle objectInstanceHandle)
      throws Exception
    {
      for (int i = 0;
           i < 5 && !objectInstances.containsKey(objectInstanceHandle); i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert objectInstances.containsKey(objectInstanceHandle);
    }

    public void checkObjectInstanceHandle(
      ObjectInstanceHandle objectInstanceHandle1,
      ObjectInstanceHandle objectInstanceHandle2)
      throws Exception
    {
      for (int i = 0;
           i < 5 && (!objectInstances.containsKey(objectInstanceHandle1) ||
                     !objectInstances.containsKey(objectInstanceHandle2)); i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert objectInstances.containsKey(objectInstanceHandle1) &&
             objectInstances.containsKey(objectInstanceHandle2);
    }

    public void checkForRemovedObjectInstanceHandle(
      ObjectInstanceHandle objectInstanceHandle)
      throws Exception
    {
      for (int i = 0;
           i < 5 && !objectInstances.get(objectInstanceHandle).isRemoved();
           i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert objectInstances.get(objectInstanceHandle).isRemoved();
    }

    public void checkAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                                     AttributeHandleValueMap attributeValues)
      throws Exception
    {
      for (int i = 0;
           i < 5 &&
           objectInstances.get(objectInstanceHandle).getAttributeValues() ==
           null;
           i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }

      assert attributeValues.equals(
        objectInstances.get(objectInstanceHandle).getAttributeValues());
    }

    public void checkParameterValues(ParameterHandleValueMap parameterValues)
      throws Exception
    {
      for (int i = 0; i < 5 && this.parameterValues == null; i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert parameterValues.equals(this.parameterValues);
    }

    @Override
    public void objectInstanceNameReservationSucceeded(String name)
      throws UnknownName, FederateInternalError
    {
      reservedObjectInstanceNames.add(name);
    }

    @Override
    public void objectInstanceNameReservationFailed(String name)
      throws UnknownName, FederateInternalError
    {
      notReservedObjectInstanceNames.add(name);
    }

    @Override
    public void discoverObjectInstance(
      ObjectInstanceHandle objectInstanceHandle,
      ObjectClassHandle objectClassHandle,
      String name)
    {
      objectInstances.put(objectInstanceHandle, new ObjectInstance(
        objectInstanceHandle, objectClassHandle, name));
    }

    @Override
    public void reflectAttributeValues(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleValueMap attributeValues,
      byte[] tag, OrderType sentOrderType,
      TransportationType transportationType)
    {
      objectInstances.get(objectInstanceHandle).setAttributeValues(
        attributeValues);
    }

    @Override
    public void removeObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                                     byte[] tag, OrderType sentOrderType)
    {
      objectInstances.get(objectInstanceHandle).setRemoved(true);
    }

    @Override
    public void receiveInteraction(
      InteractionClassHandle interactionClassHandle,
      ParameterHandleValueMap parameterValues,
      byte[] tag, OrderType sentOrderType,
      TransportationType transportationType)
    {
      this.parameterValues = parameterValues;
    }
  }

  protected static class ObjectInstance
  {
    protected ObjectInstanceHandle objectInstanceHandle;
    protected ObjectClassHandle objectClassHandle;
    protected String name;
    protected AttributeHandleValueMap attributeValues;
    protected boolean removed;

    public ObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                          ObjectClassHandle objectClassHandle, String name)
    {
      this.objectInstanceHandle = objectInstanceHandle;
      this.objectClassHandle = objectClassHandle;
      this.name = name;
    }

    public ObjectInstanceHandle getObjectInstanceHandle()
    {
      return objectInstanceHandle;
    }

    public ObjectClassHandle getObjectClassHandle()
    {
      return objectClassHandle;
    }

    public String getName()
    {
      return name;
    }

    public AttributeHandleValueMap getAttributeValues()
    {
      return attributeValues;
    }

    public void setAttributeValues(AttributeHandleValueMap attributeValues)
    {
      this.attributeValues = attributeValues;
    }

    public boolean isRemoved()
    {
      return removed;
    }

    public void setRemoved(boolean removed)
    {
      this.removed = removed;
    }
  }
}
