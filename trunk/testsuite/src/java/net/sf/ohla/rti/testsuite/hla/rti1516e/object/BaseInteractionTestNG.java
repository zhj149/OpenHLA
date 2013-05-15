package net.sf.ohla.rti.testsuite.hla.rti1516e.object;

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;

public abstract class BaseInteractionTestNG
  extends BaseTestNG<InteractionFederateAmbassador>
{
  protected InteractionClassHandle testInteractionClassHandle;
  protected InteractionClassHandle testInteractionClassHandle2;

  protected ParameterHandleValueMap testParameterValues;
  protected ParameterHandleValueMap testParameterValues2;

  protected BaseInteractionTestNG(String federationName)
  {
    super(2, federationName);
  }

  protected BaseInteractionTestNG(int rtiAmbassadorCount, String federationName)
  {
    super(rtiAmbassadorCount, federationName);
  }

  @BeforeClass
  public void baseInteractionSetup()
    throws Exception
  {
    connect();
    createFederationExecution();
    joinFederationExecution();

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

    synchronize(SYNCHRONIZATION_POINT_SETUP_COMPLETE, federateAmbassadors);
  }

  @AfterClass
  public void baseInteractionTeardown()
    throws Exception
  {
    resignFederationExecution();
    destroyFederationExecution();
    disconnect();
  }

  protected InteractionFederateAmbassador createFederateAmbassador(RTIambassador rtiAmbassador)
  {
    return new InteractionFederateAmbassador(rtiAmbassador);
  }
}
