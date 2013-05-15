package net.sf.ohla.rti.testsuite.hla.rti1516e.time;

import org.testng.annotations.BeforeClass;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;

public abstract class BaseTimeAdvanceRequestTestNG
  extends BaseTimeManagementTestNG
{
  protected ParameterHandleValueMap testParameterValues;

  protected ObjectInstanceHandle testObjectInstanceHandle;
  protected AttributeHandleValueMap testAttributeValues;

  protected BaseTimeAdvanceRequestTestNG(String federationName)
  {
    super(2, federationName);
  }

  @BeforeClass
  public void baseTimeAdvanceRequestSetup()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);
    rtiAmbassadors.get(1).enableTimeRegulation(lookahead1);

    federateAmbassadors.get(0).checkTimeRegulationEnabled(initial);
    federateAmbassadors.get(1).checkTimeRegulationEnabled(initial);

    rtiAmbassadors.get(0).enableTimeConstrained();
    rtiAmbassadors.get(1).enableTimeConstrained();

    federateAmbassadors.get(0).checkTimeConstrainedEnabled(initial);
    federateAmbassadors.get(1).checkTimeConstrainedEnabled(initial);

    InteractionClassHandle testInteractionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);

    ParameterHandle parameterHandle1 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle, PARAMETER1);
    ParameterHandle parameterHandle2 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle, PARAMETER2);
    ParameterHandle parameterHandle3 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle, PARAMETER3);

    testParameterValues = rtiAmbassadors.get(0).getParameterHandleValueMapFactory().create(3);
    testParameterValues.put(parameterHandle1, PARAMETER1_VALUE.getBytes());
    testParameterValues.put(parameterHandle2, PARAMETER2_VALUE.getBytes());
    testParameterValues.put(parameterHandle3, PARAMETER3_VALUE.getBytes());

    rtiAmbassadors.get(0).publishInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(1).subscribeInteractionClass(testInteractionClassHandle);

    ObjectClassHandle testObjectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    AttributeHandle attributeHandle1 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE1);
    AttributeHandle attributeHandle2 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE2);
    AttributeHandle attributeHandle3 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE3);

    AttributeHandleSet testObjectAttributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    testObjectAttributeHandles.add(attributeHandle1);
    testObjectAttributeHandles.add(attributeHandle2);
    testObjectAttributeHandles.add(attributeHandle3);

    rtiAmbassadors.get(0).publishObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(1).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);

    testObjectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);

    federateAmbassadors.get(1).checkObjectInstanceHandle(testObjectInstanceHandle);

    testAttributeValues = rtiAmbassadors.get(0).getAttributeHandleValueMapFactory().create(3);
    testAttributeValues.put(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());
    testAttributeValues.put(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
    testAttributeValues.put(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());

    synchronize(SYNCHRONIZATION_POINT_SETUP_COMPLETE, federateAmbassadors);

    rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle, testParameterValues, TAG, five);
    rtiAmbassadors.get(0).updateAttributeValues(testObjectInstanceHandle, testAttributeValues, TAG, ten);
  }
}
