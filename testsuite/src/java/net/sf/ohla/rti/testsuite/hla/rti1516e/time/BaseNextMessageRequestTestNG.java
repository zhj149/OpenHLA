package net.sf.ohla.rti.testsuite.hla.rti1516e.time;

import org.testng.annotations.BeforeClass;

import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.TransportationTypeHandle;

public abstract class BaseNextMessageRequestTestNG
  extends BaseTimeManagementTestNG
{
  protected TransportationTypeHandle reliableTransportationTypeHandle;

  protected InteractionClassHandle testInteractionClassHandle2;
  protected InteractionClassHandle testInteractionClassHandle3;

  protected ParameterHandleValueMap testParameterValues;

  protected MessageRetractionHandle testInteractionMessageRetractionHandle2;
  protected MessageRetractionHandle testInteractionMessageRetractionHandle3;

  protected BaseNextMessageRequestTestNG(String federationName)
    throws Exception
  {
    super(5, federationName);
  }

  @BeforeClass
  public void baseNextMessageRequestSetup()
    throws Exception
  {
    reliableTransportationTypeHandle = rtiAmbassadors.get(0).getTransportationTypeHandle(HLA_RELIABLE);

    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);
    rtiAmbassadors.get(1).enableTimeRegulation(lookahead2);
    rtiAmbassadors.get(2).enableTimeRegulation(lookahead2);
    rtiAmbassadors.get(3).enableTimeRegulation(lookahead1);
    rtiAmbassadors.get(4).enableTimeRegulation(lookahead1);

    federateAmbassadors.get(0).checkTimeRegulationEnabled(initial);
    federateAmbassadors.get(1).checkTimeRegulationEnabled(initial);
    federateAmbassadors.get(2).checkTimeRegulationEnabled(initial);
    federateAmbassadors.get(3).checkTimeRegulationEnabled(initial);
    federateAmbassadors.get(4).checkTimeRegulationEnabled(initial);

    rtiAmbassadors.get(0).enableTimeConstrained();
    rtiAmbassadors.get(1).enableTimeConstrained();
    rtiAmbassadors.get(2).enableTimeConstrained();
    rtiAmbassadors.get(3).enableTimeConstrained();
    rtiAmbassadors.get(4).enableTimeConstrained();

    federateAmbassadors.get(0).checkTimeConstrainedEnabled(initial);
    federateAmbassadors.get(1).checkTimeConstrainedEnabled(initial);
    federateAmbassadors.get(2).checkTimeConstrainedEnabled(initial);
    federateAmbassadors.get(3).checkTimeConstrainedEnabled(initial);
    federateAmbassadors.get(4).checkTimeConstrainedEnabled(initial);

    InteractionClassHandle testInteractionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
    testInteractionClassHandle2 = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION2);
    testInteractionClassHandle3 = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION3);

    ParameterHandle parameterHandle1 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle, PARAMETER1);
    ParameterHandle parameterHandle2 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle, PARAMETER2);
    ParameterHandle parameterHandle3 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle, PARAMETER3);

    testParameterValues = rtiAmbassadors.get(0).getParameterHandleValueMapFactory().create(3);
    testParameterValues.put(parameterHandle1, PARAMETER1_VALUE.getBytes());
    testParameterValues.put(parameterHandle2, PARAMETER2_VALUE.getBytes());
    testParameterValues.put(parameterHandle3, PARAMETER3_VALUE.getBytes());

    rtiAmbassadors.get(0).publishInteractionClass(testInteractionClassHandle2);
    rtiAmbassadors.get(0).publishInteractionClass(testInteractionClassHandle3);

    rtiAmbassadors.get(1).subscribeInteractionClass(testInteractionClassHandle2);
    rtiAmbassadors.get(2).subscribeInteractionClass(testInteractionClassHandle2);

    rtiAmbassadors.get(3).subscribeInteractionClass(testInteractionClassHandle3);
    rtiAmbassadors.get(4).subscribeInteractionClass(testInteractionClassHandle3);

    synchronize(SYNCHRONIZATION_POINT_SETUP_COMPLETE, federateAmbassadors);

    testInteractionMessageRetractionHandle2 =
      rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle2, testParameterValues, TAG, three).handle;
    testInteractionMessageRetractionHandle3 =
      rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle3, testParameterValues, TAG, four).handle;
  }
}
