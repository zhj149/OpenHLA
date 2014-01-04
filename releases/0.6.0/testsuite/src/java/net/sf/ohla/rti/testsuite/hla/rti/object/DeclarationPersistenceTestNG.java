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
import java.util.UUID;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.AttributeHandleSet;
import hla.rti.ReceivedInteraction;
import hla.rti.ResignAction;
import hla.rti.SuppliedParameters;
import hla.rti.jlc.RTIambassadorEx;

@Test
public class DeclarationPersistenceTestNG
  extends BaseTestNG<DeclarationPersistenceTestNG.TestFederateAmbassador>
{
  private static final String FEDERATION_NAME = DeclarationPersistenceTestNG.class.getSimpleName();
  private static final String SAVE_NAME = FEDERATION_NAME + UUID.randomUUID();

  private int testObjectClassHandle;
  private int testObjectClassHandle2;

  private int testInteractionClassHandle;
  private int testInteractionClassHandle2;

  private SuppliedParameters testParameterValues;
  private SuppliedParameters testParameterValues2;

  public DeclarationPersistenceTestNG()
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

    testInteractionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
    int parameterHandle1 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER1, testInteractionClassHandle);
    int parameterHandle2 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER2, testInteractionClassHandle);
    int parameterHandle3 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER3, testInteractionClassHandle);

    testInteractionClassHandle2 = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION2);
    int parameterHandle4 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER4, testInteractionClassHandle2);
    int parameterHandle5 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER5, testInteractionClassHandle2);
    int parameterHandle6 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER6, testInteractionClassHandle2);

    testParameterValues = rtiFactory.createSuppliedParameters();
    testParameterValues.add(parameterHandle1, PARAMETER1_VALUE.getBytes());
    testParameterValues.add(parameterHandle2, PARAMETER2_VALUE.getBytes());
    testParameterValues.add(parameterHandle3, PARAMETER3_VALUE.getBytes());

    testParameterValues2 = rtiFactory.createSuppliedParameters();
    testParameterValues2.add(parameterHandle1, PARAMETER1_VALUE.getBytes());
    testParameterValues2.add(parameterHandle2, PARAMETER2_VALUE.getBytes());
    testParameterValues2.add(parameterHandle3, PARAMETER3_VALUE.getBytes());
    testParameterValues2.add(parameterHandle4, PARAMETER4_VALUE.getBytes());
    testParameterValues2.add(parameterHandle5, PARAMETER5_VALUE.getBytes());
    testParameterValues2.add(parameterHandle6, PARAMETER6_VALUE.getBytes());

    rtiAmbassadors.get(0).publishInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(0).publishInteractionClass(testInteractionClassHandle2);

    rtiAmbassadors.get(1).subscribeInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(2).subscribeInteractionClass(testInteractionClassHandle2);

    rtiAmbassadors.get(0).publishObjectClass(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(0).publishObjectClass(testObjectClassHandle2, testObjectAttributeHandles2);

    rtiAmbassadors.get(1).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(2).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(2).subscribeObjectClassAttributes(testObjectClassHandle2, testObjectAttributeHandles2);

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
  public void testRegisterObjectInstance()
    throws Exception
  {
    int objectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);
    String objectInstanceName = rtiAmbassadors.get(0).getObjectInstanceName(objectInstanceHandle);

    assert testObjectClassHandle == rtiAmbassadors.get(0).getObjectClass(objectInstanceHandle);

    federateAmbassadors.get(2).checkObjectInstanceName(objectInstanceName);
    federateAmbassadors.get(2).checkObjectClassHandle(objectInstanceName, testObjectClassHandle);
  }

  @Test
  public void testRegisterObjectInstanceChild()
    throws Exception
  {
    int objectInstanceHandle0 = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle2);
    String objectInstanceName = rtiAmbassadors.get(0).getObjectInstanceName(objectInstanceHandle0);

    federateAmbassadors.get(1).checkObjectInstanceName(objectInstanceName);
    federateAmbassadors.get(1).checkObjectClassHandle(objectInstanceName, testObjectClassHandle);
    int objectInstanceHandle1 = rtiAmbassadors.get(1).getObjectInstanceHandle(objectInstanceName);

    federateAmbassadors.get(2).checkObjectInstanceName(objectInstanceName);
    federateAmbassadors.get(2).checkObjectClassHandle(objectInstanceName, testObjectClassHandle2);
    int objectInstanceHandle2 = rtiAmbassadors.get(2).getObjectInstanceHandle(objectInstanceName);

    assert testObjectClassHandle2 == rtiAmbassadors.get(0).getObjectClass(objectInstanceHandle0);
    assert testObjectClassHandle == rtiAmbassadors.get(1).getObjectClass(objectInstanceHandle1);
    assert testObjectClassHandle2 == rtiAmbassadors.get(2).getObjectClass(objectInstanceHandle2);
  }

  @Test
  public void testSendInteraction()
    throws Exception
  {
    rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle2, testParameterValues2, TAG);

    federateAmbassadors.get(1).checkParameterValues(testInteractionClassHandle, testParameterValues, TAG);
    federateAmbassadors.get(2).checkParameterValues(testInteractionClassHandle2, testParameterValues2, TAG);
  }

  protected TestFederateAmbassador createFederateAmbassador(RTIambassadorEx rtiAmbassador)
  {
    return new TestFederateAmbassador(rtiAmbassador);
  }

  public static class TestFederateAmbassador
    extends BaseFederateAmbassador
  {
    private final Map<String, TestObjectInstance> objectInstances = new HashMap<String, TestObjectInstance>();

    private Integer interactionClassHandle;
    private ReceivedInteraction receivedInteraction;
    private byte[] tag;

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

    public void checkRemoved(final String objectInstanceName, byte[] tag)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return !objectInstances.get(objectInstanceName).isRemoved(); } });

      TestObjectInstance objectInstance = objectInstances.get(objectInstanceName);
      assert objectInstance.isRemoved();
      assert Arrays.equals(tag, objectInstance.getTag());
    }

    public void checkParameterValues(int interactionClassHandle, SuppliedParameters suppliedParameters, byte[] tag)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return TestFederateAmbassador.this.interactionClassHandle == null; } });

      assert this.interactionClassHandle != null;
      assert interactionClassHandle == this.interactionClassHandle;
      checkReceivedInteraction(receivedInteraction, suppliedParameters);
      assert Arrays.equals(tag, this.tag);
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

    @Override
    public void receiveInteraction(int interactionClassHandle, ReceivedInteraction receivedInteraction, byte[] tag)
    {
      this.interactionClassHandle = interactionClassHandle;
      this.receivedInteraction = receivedInteraction;
      this.tag = tag;
    }

    @Override
    public void removeObjectInstance(int objectInstanceHandle, byte[] tag)
    {
      objectInstances.get(objectInstanceHandle).setRemoved(tag);
    }
  }
}
