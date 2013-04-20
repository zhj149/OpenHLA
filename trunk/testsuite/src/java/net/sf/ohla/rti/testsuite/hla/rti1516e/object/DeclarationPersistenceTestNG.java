package net.sf.ohla.rti.testsuite.hla.rti1516e.object;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.SaveStatus;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.FederateInternalError;

@Test
public class DeclarationPersistenceTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = ObjectPersistenceTestNG.class.getSimpleName();
  private static final String SAVE_NAME = FEDERATION_NAME + UUID.randomUUID();

  private final List<TestFederateAmbassador> federateAmbassadors = new ArrayList<TestFederateAmbassador>(4);
  private final List<FederateHandle> federateHandles = new ArrayList<FederateHandle>(4);

  private ObjectClassHandle testObjectClassHandle;
  private ObjectClassHandle testObjectClassHandle2;

  private InteractionClassHandle testInteractionClassHandle;
  private InteractionClassHandle testInteractionClassHandle2;

  private ParameterHandleValueMap testParameterValues;
  private ParameterHandleValueMap testParameterValues2;

  public DeclarationPersistenceTestNG()
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

    federateHandles.add(rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE_1, FEDERATE_TYPE_1, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(1).joinFederationExecution(
      FEDERATE_TYPE_2, FEDERATE_TYPE_2, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(2).joinFederationExecution(
      FEDERATE_TYPE_3, FEDERATE_TYPE_3, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(3).joinFederationExecution(
      FEDERATE_TYPE_4, FEDERATE_TYPE_4, FEDERATION_NAME));

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

    testInteractionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
    ParameterHandle parameterHandle1 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle, PARAMETER1);
    ParameterHandle parameterHandle2 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle, PARAMETER2);
    ParameterHandle parameterHandle3 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle, PARAMETER3);

    testInteractionClassHandle2 = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION2);
    ParameterHandle parameterHandle4 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle2, PARAMETER4);
    ParameterHandle parameterHandle5 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle2, PARAMETER5);
    ParameterHandle parameterHandle6 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle2, PARAMETER6);

    testParameterValues = rtiAmbassadors.get(0).getParameterHandleValueMapFactory().create(3);
    testParameterValues.put(parameterHandle1, PARAMETER1_VALUE.getBytes());
    testParameterValues.put(parameterHandle2, PARAMETER2_VALUE.getBytes());
    testParameterValues.put(parameterHandle3, PARAMETER3_VALUE.getBytes());

    testParameterValues2 = rtiAmbassadors.get(0).getParameterHandleValueMapFactory().create(6);
    testParameterValues2.put(parameterHandle1, PARAMETER1_VALUE.getBytes());
    testParameterValues2.put(parameterHandle2, PARAMETER2_VALUE.getBytes());
    testParameterValues2.put(parameterHandle3, PARAMETER3_VALUE.getBytes());
    testParameterValues2.put(parameterHandle4, PARAMETER4_VALUE.getBytes());
    testParameterValues2.put(parameterHandle5, PARAMETER5_VALUE.getBytes());
    testParameterValues2.put(parameterHandle6, PARAMETER6_VALUE.getBytes());

    rtiAmbassadors.get(0).publishInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(0).publishInteractionClass(testInteractionClassHandle2);

    rtiAmbassadors.get(1).subscribeInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(2).subscribeInteractionClass(testInteractionClassHandle2);

    rtiAmbassadors.get(0).reserveObjectInstanceName(TEST_OBJECT);
    federateAmbassadors.get(0).checkObjectInstanceNameReserved(TEST_OBJECT);

    rtiAmbassadors.get(0).publishObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(0).publishObjectClassAttributes(testObjectClassHandle2, testObjectAttributeHandles2);

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

    resignFederationExecution(ResignAction.DELETE_OBJECTS);

    destroyFederationExecution(FEDERATION_NAME);

    for (TestFederateAmbassador testFederateAmbassador : federateAmbassadors)
    {
      testFederateAmbassador.reset();
    }

    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);

    federateHandles.add(rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE_1, FEDERATE_TYPE_1, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(1).joinFederationExecution(
      FEDERATE_TYPE_2, FEDERATE_TYPE_2, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(2).joinFederationExecution(
      FEDERATE_TYPE_3, FEDERATE_TYPE_3, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(3).joinFederationExecution(
      FEDERATE_TYPE_4, FEDERATE_TYPE_4, FEDERATION_NAME));

    rtiAmbassadors.get(0).requestFederationRestore(SAVE_NAME);

    federateAmbassadors.get(0).checkRequestFederationRestoreSucceeded(SAVE_NAME);

    federateAmbassadors.get(0).checkInitiateFederateRestore(SAVE_NAME, FEDERATE_TYPE_1, federateHandles.get(0));
    federateAmbassadors.get(1).checkInitiateFederateRestore(SAVE_NAME, FEDERATE_TYPE_2, federateHandles.get(1));
    federateAmbassadors.get(2).checkInitiateFederateRestore(SAVE_NAME, FEDERATE_TYPE_3, federateHandles.get(2));
    federateAmbassadors.get(3).checkInitiateFederateRestore(SAVE_NAME, FEDERATE_TYPE_4, federateHandles.get(3));

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

    destroyFederationExecution(FEDERATION_NAME);

    disconnect();
  }

  @Test
  public void testRegisterObjectInstance()
    throws Exception
  {
    ObjectInstanceHandle objectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);

    String objectInstanceName = rtiAmbassadors.get(0).getObjectInstanceName(objectInstanceHandle);
    assert objectInstanceHandle.equals(rtiAmbassadors.get(0).getObjectInstanceHandle(objectInstanceName));

    federateAmbassadors.get(1).checkObjectInstanceHandle(objectInstanceHandle);
    federateAmbassadors.get(1).checkObjectInstanceName(objectInstanceHandle, objectInstanceName);
    federateAmbassadors.get(1).checkObjectClassHandle(objectInstanceHandle, testObjectClassHandle);
    federateAmbassadors.get(1).checkProducingFederateHandle(objectInstanceHandle, federateHandles.get(0));

    federateAmbassadors.get(2).checkObjectInstanceHandle(objectInstanceHandle);
    federateAmbassadors.get(2).checkObjectInstanceName(objectInstanceHandle, objectInstanceName);
    federateAmbassadors.get(2).checkObjectClassHandle(objectInstanceHandle, testObjectClassHandle);
    federateAmbassadors.get(2).checkProducingFederateHandle(objectInstanceHandle, federateHandles.get(0));

    assert testObjectClassHandle.equals(rtiAmbassadors.get(0).getKnownObjectClassHandle(objectInstanceHandle));
    assert testObjectClassHandle.equals(rtiAmbassadors.get(1).getKnownObjectClassHandle(objectInstanceHandle));
    assert testObjectClassHandle.equals(rtiAmbassadors.get(2).getKnownObjectClassHandle(objectInstanceHandle));

    assert objectInstanceName.equals(rtiAmbassadors.get(1).getObjectInstanceName(objectInstanceHandle));
    assert objectInstanceHandle.equals(rtiAmbassadors.get(1).getObjectInstanceHandle(objectInstanceName));

    assert objectInstanceName.equals(rtiAmbassadors.get(2).getObjectInstanceName(objectInstanceHandle));
    assert objectInstanceHandle.equals(rtiAmbassadors.get(2).getObjectInstanceHandle(objectInstanceName));
  }

  @Test
  public void testRegisterObjectInstanceChild()
    throws Exception
  {
    ObjectInstanceHandle objectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle2);

    String objectInstanceName = rtiAmbassadors.get(0).getObjectInstanceName(objectInstanceHandle);
    assert objectInstanceHandle.equals(rtiAmbassadors.get(0).getObjectInstanceHandle(objectInstanceName));

    federateAmbassadors.get(1).checkObjectInstanceHandle(objectInstanceHandle);
    federateAmbassadors.get(1).checkObjectInstanceName(objectInstanceHandle, objectInstanceName);
    federateAmbassadors.get(1).checkObjectClassHandle(objectInstanceHandle, testObjectClassHandle);
    federateAmbassadors.get(1).checkProducingFederateHandle(objectInstanceHandle, federateHandles.get(0));

    federateAmbassadors.get(2).checkObjectInstanceHandle(objectInstanceHandle);
    federateAmbassadors.get(2).checkObjectInstanceName(objectInstanceHandle, objectInstanceName);
    federateAmbassadors.get(2).checkObjectClassHandle(objectInstanceHandle, testObjectClassHandle2);
    federateAmbassadors.get(2).checkProducingFederateHandle(objectInstanceHandle, federateHandles.get(0));

    assert testObjectClassHandle2.equals(rtiAmbassadors.get(0).getKnownObjectClassHandle(objectInstanceHandle));
    assert testObjectClassHandle.equals(rtiAmbassadors.get(1).getKnownObjectClassHandle(objectInstanceHandle));
    assert testObjectClassHandle2.equals(rtiAmbassadors.get(2).getKnownObjectClassHandle(objectInstanceHandle));

    assert objectInstanceName.equals(rtiAmbassadors.get(1).getObjectInstanceName(objectInstanceHandle));
    assert objectInstanceHandle.equals(rtiAmbassadors.get(1).getObjectInstanceHandle(objectInstanceName));

    assert objectInstanceName.equals(rtiAmbassadors.get(2).getObjectInstanceName(objectInstanceHandle));
    assert objectInstanceHandle.equals(rtiAmbassadors.get(2).getObjectInstanceHandle(objectInstanceName));
  }

  @Test
  public void testSendInteraction()
    throws Exception
  {
    rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle2, testParameterValues2, TAG);

    federateAmbassadors.get(1).checkParameterValues(
      testInteractionClassHandle, testParameterValues, TAG, federateHandles.get(0));
    federateAmbassadors.get(2).checkParameterValues(
      testInteractionClassHandle2, testParameterValues2, TAG, federateHandles.get(0));
  }

  protected void checkFederationSaveStatus(SaveStatus... saveStatii)
    throws Exception
  {
    assert federateHandles.size() == saveStatii.length;

    for (RTIambassador rtiAmbassador : rtiAmbassadors)
    {
      rtiAmbassador.queryFederationSaveStatus();
    }

    Map<FederateHandle, SaveStatus> saveStatusResponse = new HashMap<FederateHandle, SaveStatus>();
    for (ListIterator<FederateHandle> i = federateHandles.listIterator(); i.hasNext();)
    {
      int index = i.nextIndex();
      saveStatusResponse.put(i.next(), saveStatii[index]);
    }

    for (TestFederateAmbassador federateAmbassador : federateAmbassadors)
    {
      federateAmbassador.checkFederationSaveStatus(saveStatusResponse);
    }
  }

  protected static class TestFederateAmbassador
    extends BaseFederateAmbassador
  {
    private final Set<String> successfullyReservedObjectInstanceNames = new HashSet<String>();
    private final Set<String> unsuccessfullyReservedObjectInstanceNames = new HashSet<String>();

    private final Map<ObjectInstanceHandle, TestObjectInstance> objectInstances =
      new HashMap<ObjectInstanceHandle, TestObjectInstance>();

    private InteractionClassHandle interactionClassHandle;
    private ParameterHandleValueMap parameterValues;
    private byte[] tag;
    private SupplementalReceiveInfo receiveInfo;

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

    public void checkObjectInstanceName(ObjectInstanceHandle objectInstanceHandle, String objectInstanceName)
      throws Exception
    {
      assert objectInstances.get(objectInstanceHandle).getObjectInstanceName().equals(objectInstanceName);
    }

    public void checkObjectClassHandle(ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle)
      throws Exception
    {
      assert objectInstances.get(objectInstanceHandle).getObjectClassHandle().equals(objectClassHandle);
    }

    public void checkProducingFederateHandle(ObjectInstanceHandle objectInstanceHandle, FederateHandle federateHandle)
      throws Exception
    {
      assert objectInstances.get(objectInstanceHandle).getProducingFederateHandle().equals(federateHandle);
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

    public void checkParameterValues(
      InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
      FederateHandle federateHandle)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return TestFederateAmbassador.this.interactionClassHandle == null; } });

      assert this.interactionClassHandle != null;
      assert interactionClassHandle.equals(this.interactionClassHandle);
      assert parameterValues.equals(this.parameterValues);
      assert Arrays.equals(tag, this.tag);
      assert !receiveInfo.hasProducingFederate() || federateHandle.equals(receiveInfo.getProducingFederate());
    }

    @Override
    public void reset()
    {
      super.reset();

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
      ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle, String objectInstanceName,
      FederateHandle federateHandle)
      throws FederateInternalError
    {
      objectInstances.put(objectInstanceHandle, new TestObjectInstance(
        objectInstanceHandle, objectClassHandle, objectInstanceName, federateHandle));
    }

    @Override
    public void receiveInteraction(
      InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
      OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, SupplementalReceiveInfo receiveInfo)
      throws FederateInternalError
    {
      this.interactionClassHandle = interactionClassHandle;
      this.parameterValues = parameterValues;
      this.tag = tag;
      this.receiveInfo = receiveInfo;
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
