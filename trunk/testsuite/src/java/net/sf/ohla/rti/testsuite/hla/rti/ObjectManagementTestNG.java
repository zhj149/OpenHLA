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

package net.sf.ohla.rti.testsuite.hla.rti;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.AttributeHandleSet;
import hla.rti.DeletePrivilegeNotHeld;
import hla.rti.ObjectAlreadyRegistered;
import hla.rti.ObjectClassNotPublished;
import hla.rti.ObjectNotKnown;
import hla.rti.RTIinternalError;
import hla.rti.ReceivedInteraction;
import hla.rti.ReflectedAttributes;
import hla.rti.ResignAction;
import hla.rti.SuppliedAttributes;
import hla.rti.SuppliedParameters;
import hla.rti.jlc.NullFederateAmbassador;
import hla.rti.jlc.RTIambassadorEx;

@Test(groups = {"Object Management"})
public class ObjectManagementTestNG
  extends BaseTestNG
{
  protected List<TestFederateAmbassador> federateAmbassadors =
    new ArrayList<TestFederateAmbassador>(3);

  protected int testObjectClassHandle;
  protected int attributeHandle1;
  protected int attributeHandle2;
  protected int attributeHandle3;
  protected AttributeHandleSet testObjectAttributeHandles;

  protected int testObjectClassHandle2;
  protected int attributeHandle4;
  protected int attributeHandle5;
  protected int attributeHandle6;
  protected AttributeHandleSet testObjectAttributeHandles2;

  protected int objectInstanceHandle;
  protected int objectInstanceHandle2;
  protected int objectInstanceHandleByName;

  protected int testInteractionClassHandle;
  protected int parameterHandle1;
  protected int parameterHandle2;
  protected int parameterHandle3;

  protected int testInteractionClassHandle2;
  protected int parameterHandle4;
  protected int parameterHandle5;
  protected int parameterHandle6;

  public ObjectManagementTestNG()
  {
    super(3);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fed);

    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(0)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(1)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(2)));

    rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(0), null);

    testObjectClassHandle =
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    attributeHandle1 = rtiAmbassadors.get(0).getAttributeHandle(
      ATTRIBUTE1, testObjectClassHandle);
    attributeHandle2 = rtiAmbassadors.get(0).getAttributeHandle(
      ATTRIBUTE2, testObjectClassHandle);
    attributeHandle3 = rtiAmbassadors.get(0).getAttributeHandle(
      ATTRIBUTE3, testObjectClassHandle);


    testObjectAttributeHandles = rtiFactory.createAttributeHandleSet();
    testObjectAttributeHandles.add(attributeHandle1);
    testObjectAttributeHandles.add(attributeHandle2);
    testObjectAttributeHandles.add(attributeHandle3);

    testObjectClassHandle2 =
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT2);

    attributeHandle4 = rtiAmbassadors.get(0).getAttributeHandle(
      ATTRIBUTE4, testObjectClassHandle2);
    attributeHandle5 = rtiAmbassadors.get(0).getAttributeHandle(
      ATTRIBUTE5, testObjectClassHandle2);
    attributeHandle6 = rtiAmbassadors.get(0).getAttributeHandle(
      ATTRIBUTE6, testObjectClassHandle2);

    testObjectAttributeHandles2 = rtiFactory.createAttributeHandleSet();
    testObjectAttributeHandles2.add(attributeHandle1);
    testObjectAttributeHandles2.add(attributeHandle2);
    testObjectAttributeHandles2.add(attributeHandle3);
    testObjectAttributeHandles2.add(attributeHandle4);
    testObjectAttributeHandles2.add(attributeHandle5);
    testObjectAttributeHandles2.add(attributeHandle6);

    rtiAmbassadors.get(0).publishObjectClass(
      testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(0).subscribeObjectClassAttributes(
      testObjectClassHandle, testObjectAttributeHandles);

    testInteractionClassHandle =
      rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
    testInteractionClassHandle2 =
      rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION2);

    parameterHandle1 = rtiAmbassadors.get(0).getParameterHandle(
      PARAMETER1, testInteractionClassHandle);
    parameterHandle2 = rtiAmbassadors.get(0).getParameterHandle(
      PARAMETER2, testInteractionClassHandle);
    parameterHandle3 = rtiAmbassadors.get(0).getParameterHandle(
      PARAMETER3, testInteractionClassHandle);
    parameterHandle4 = rtiAmbassadors.get(0).getParameterHandle(
      PARAMETER4, testInteractionClassHandle2);
    parameterHandle5 = rtiAmbassadors.get(0).getParameterHandle(
      PARAMETER5, testInteractionClassHandle2);
    parameterHandle6 = rtiAmbassadors.get(0).getParameterHandle(
      PARAMETER6, testInteractionClassHandle2);

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

  @Test(dependsOnMethods = {"testRegisterObjectInstanceByName"},
        expectedExceptions = {ObjectAlreadyRegistered.class,
          RTIinternalError.class})
  public void testRegisterObjectInstanceWithIllegalName()
    throws Exception
  {
    rtiAmbassadors.get(0).registerObjectInstance(
      testObjectClassHandle, "HLAtestRegisterObjectInstanceByName");
  }

  @Test
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
        expectedExceptions = {ObjectAlreadyRegistered.class,
          RTIinternalError.class})
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
        expectedExceptions = {ObjectAlreadyRegistered.class,
          RTIinternalError.class})
  public void testRegisterObjectInstanceWithRetiredName()
    throws Exception
  {
    rtiAmbassadors.get(0).registerObjectInstance(
      testObjectClassHandle, "testRegisterObjectInstanceByName");
  }

  @Test(expectedExceptions = {ObjectClassNotPublished.class})
  public void testRegisterObjectInstanceOfUnpublishedObjectClass()
    throws Exception
  {
    rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle2);
  }

  @Test(dependsOnMethods = {"testDeleteObjectInstanceByName"},
        expectedExceptions = {ObjectNotKnown.class})
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
      FEDERATE_TYPE + "2", FEDERATION_NAME, federateAmbassadors.get(1), null);

    rtiAmbassadors.get(1).publishObjectClass(
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
      FEDERATE_TYPE + "3", FEDERATION_NAME, federateAmbassadors.get(2), null);

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
    SuppliedAttributes suppliedAttributes =
      rtiFactory.createSuppliedAttributes();
    suppliedAttributes.add(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());
    suppliedAttributes.add(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
    suppliedAttributes.add(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());

    // because of the nature of distributed computing, the subscribe message
    // from federate #3 may not have arrived at federate #1 or #2, so we need
    // to put in an artificual pause because we want the testing to succeed
    //
    rtiAmbassadors.get(0).tick(1.0, 1.0);

    rtiAmbassadors.get(0).updateAttributeValues(
      objectInstanceHandle, suppliedAttributes, null);

    federateAmbassadors.get(2).checkReflectedAttributes(
      objectInstanceHandle, suppliedAttributes);
  }

  @Test(dependsOnMethods = {"testUpdateObjectFrom1stFederate"})
  public void testUpdateObjectFrom2ndFederate()
    throws Exception
  {
    SuppliedAttributes suppliedAttributes =
      rtiFactory.createSuppliedAttributes();
    suppliedAttributes.add(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());
    suppliedAttributes.add(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
    suppliedAttributes.add(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());

    SuppliedAttributes suppliedAttributes2 =
      rtiFactory.createSuppliedAttributes();
    suppliedAttributes2.add(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());
    suppliedAttributes2.add(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
    suppliedAttributes2.add(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());
    suppliedAttributes2.add(attributeHandle4, ATTRIBUTE4_VALUE.getBytes());
    suppliedAttributes2.add(attributeHandle5, ATTRIBUTE5_VALUE.getBytes());
    suppliedAttributes2.add(attributeHandle6, ATTRIBUTE6_VALUE.getBytes());

    rtiAmbassadors.get(1).updateAttributeValues(
      objectInstanceHandle2, suppliedAttributes2, null);

    federateAmbassadors.get(0).checkReflectedAttributes(
      objectInstanceHandle2, suppliedAttributes);
    federateAmbassadors.get(2).checkReflectedAttributes(
      objectInstanceHandle2, suppliedAttributes);
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

    federateAmbassadors.get(2).checkForRemovedlObjectInstanceHandle(
      objectInstanceHandle);
  }

  @Test(dependsOnMethods = {"testDeleteObjectFrom1stFederate"})
  public void testDeleteObjectFrom2ndFederate()
    throws Exception
  {
    rtiAmbassadors.get(1).deleteObjectInstance(objectInstanceHandle2, null);

    federateAmbassadors.get(0).checkForRemovedlObjectInstanceHandle(
      objectInstanceHandle2);
    federateAmbassadors.get(2).checkForRemovedlObjectInstanceHandle(
      objectInstanceHandle2);
  }

  @Test(dependsOnMethods = {"testDeleteObjectFrom2ndFederate"},
        expectedExceptions = {ObjectNotKnown.class})
  public void testDeleteUnknownObject()
    throws Exception
  {
    rtiAmbassadors.get(2).deleteObjectInstance(objectInstanceHandle, null);
  }

  @Test(dependsOnMethods = {"testDeleteUnknownObject"})
  public void testSendInteractionFrom1stFederate()
    throws Exception
  {
    SuppliedParameters suppliedParameters =
      rtiFactory.createSuppliedParameters();
    suppliedParameters.add(parameterHandle1, PARAMETER1_VALUE.getBytes());
    suppliedParameters.add(parameterHandle2, PARAMETER2_VALUE.getBytes());
    suppliedParameters.add(parameterHandle3, PARAMETER3_VALUE.getBytes());

    rtiAmbassadors.get(0).sendInteraction(
      testInteractionClassHandle, suppliedParameters, null);

    federateAmbassadors.get(2).checkReceivedInteraction(suppliedParameters);
  }

  @Test(dependsOnMethods = {"testSendInteractionFrom1stFederate"})
  public void testSendInteractionFrom2ndFederate()
    throws Exception
  {
    SuppliedParameters suppliedParameters =
      rtiFactory.createSuppliedParameters();
    suppliedParameters.add(parameterHandle1, PARAMETER1_VALUE.getBytes());
    suppliedParameters.add(parameterHandle2, PARAMETER2_VALUE.getBytes());
    suppliedParameters.add(parameterHandle3, PARAMETER3_VALUE.getBytes());

    SuppliedParameters suppliedParameters2 =
      rtiFactory.createSuppliedParameters();
    suppliedParameters2.add(parameterHandle1, PARAMETER1_VALUE.getBytes());
    suppliedParameters2.add(parameterHandle2, PARAMETER2_VALUE.getBytes());
    suppliedParameters2.add(parameterHandle3, PARAMETER3_VALUE.getBytes());
    suppliedParameters2.add(parameterHandle4, PARAMETER4_VALUE.getBytes());
    suppliedParameters2.add(parameterHandle5, PARAMETER5_VALUE.getBytes());
    suppliedParameters2.add(parameterHandle6, PARAMETER6_VALUE.getBytes());

    rtiAmbassadors.get(1).sendInteraction(
      testInteractionClassHandle2, suppliedParameters2, null);

    federateAmbassadors.get(0).checkReceivedInteraction(suppliedParameters);
    federateAmbassadors.get(2).checkReceivedInteraction(suppliedParameters);
  }

  protected static class TestFederateAmbassador
    extends NullFederateAmbassador
  {
    protected RTIambassadorEx rtiAmbassador;

    protected Map<Integer, ObjectInstance> objectInstances =
      new HashMap<Integer, ObjectInstance>();

    protected ReceivedInteraction receivedInteraction;

    public TestFederateAmbassador(RTIambassadorEx rtiAmbassador)
    {
      this.rtiAmbassador = rtiAmbassador;
    }

    public void checkObjectInstanceHandle(int objectInstanceHandle)
      throws Exception
    {
      for (int i = 0;
           i < 5 && !objectInstances.containsKey(objectInstanceHandle); i++)
      {
        rtiAmbassador.tick(0.1, 1.0);
      }
      assert objectInstances.containsKey(objectInstanceHandle);
    }

    public void checkObjectInstanceHandle(int objectInstanceHandle1,
                                          int objectInstanceHandle2)
      throws Exception
    {
      for (int i = 0;
           i < 5 && (!objectInstances.containsKey(objectInstanceHandle1) ||
                     !objectInstances.containsKey(objectInstanceHandle2)); i++)
      {
        rtiAmbassador.tick(0.1, 1.0);
      }
      assert objectInstances.containsKey(objectInstanceHandle1) &&
             objectInstances.containsKey(objectInstanceHandle2);
    }

    public void checkForRemovedlObjectInstanceHandle(int objectInstanceHandle)
      throws Exception
    {
      for (int i = 0;
           i < 5 && !objectInstances.get(objectInstanceHandle).isRemoved();
           i++)
      {
        rtiAmbassador.tick(0.1, 1.0);
      }
      assert objectInstances.get(objectInstanceHandle).isRemoved();
    }

    public void checkReflectedAttributes(int objectInstanceHandle,
                                         SuppliedAttributes suppliedAttributes)
      throws Exception
    {
      for (int i = 0;
           i < 5 && objectInstances.get(
             objectInstanceHandle).getReflectedAttributes() == null;
           i++)
      {
        rtiAmbassador.tick(0.1, 1.0);
      }
      ReflectedAttributes reflectedAttributes =
        objectInstances.get(objectInstanceHandle).getReflectedAttributes();
      assert reflectedAttributes != null;
      assert suppliedAttributes.size() == reflectedAttributes.size();
      for (int i = 0; i < reflectedAttributes.size(); i++)
      {
        for (int j = 0; j < suppliedAttributes.size(); j++)
        {
          if (reflectedAttributes.getAttributeHandle(i) ==
              suppliedAttributes.getHandle(j))
          {
            assert Arrays.equals(reflectedAttributes.getValue(i),
                                 suppliedAttributes.getValue(j));
          }
        }
      }
    }

    public void checkReceivedInteraction(SuppliedParameters suppliedParameters)
      throws Exception
    {
      while (this.receivedInteraction == null)
      {
        rtiAmbassador.tick(0.1, 1.0);
      }
      assert suppliedParameters.size() == receivedInteraction.size();
      for (int i = 0; i < receivedInteraction.size(); i++)
      {
        for (int j = 0; j < suppliedParameters.size(); j++)
        {
          if (receivedInteraction.getParameterHandle(i) ==
              suppliedParameters.getHandle(j))
          {
            assert Arrays.equals(receivedInteraction.getValue(i),
                                 suppliedParameters.getValue(j));
          }
        }
      }
      assert receivedInteraction.equals(this.receivedInteraction);
    }

    public void discoverObjectInstance(int objectInstanceHandle,
                                       int objectClassHandle, String name)
    {
      objectInstances.put(objectInstanceHandle, new ObjectInstance(
        objectInstanceHandle, objectClassHandle, name));
    }

    public void reflectAttributeValues(int objectInstanceHandle,
                                       ReflectedAttributes reflectedAttributes,
                                       byte[] tag)
    {
      objectInstances.get(objectInstanceHandle).setAttributeValues(
        reflectedAttributes);
    }

    public void removeObjectInstance(int objectInstanceHandle, byte[] tag)
    {
      objectInstances.get(objectInstanceHandle).setRemoved(true);
    }

    public void receiveInteraction(int interactionClassHandle,
                                   ReceivedInteraction receivedInteraction,
                                   byte[] tag)
    {
      this.receivedInteraction = receivedInteraction;
    }
  }

  protected static class ObjectInstance
  {
    protected int objectInstanceHandle;
    protected int objectClassHandle;
    protected String name;
    protected ReflectedAttributes reflectedAttributes;
    protected boolean removed;

    public ObjectInstance(int objectInstanceHandle,
                          int objectClassHandle, String name)
    {
      this.objectInstanceHandle = objectInstanceHandle;
      this.objectClassHandle = objectClassHandle;
      this.name = name;
    }

    public int getObjectInstanceHandle()
    {
      return objectInstanceHandle;
    }

    public int getObjectClassHandle()
    {
      return objectClassHandle;
    }

    public String getName()
    {
      return name;
    }

    public ReflectedAttributes getReflectedAttributes()
    {
      return reflectedAttributes;
    }

    public void setAttributeValues(ReflectedAttributes reflectedAttributes)
    {
      this.reflectedAttributes = reflectedAttributes;
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
