package net.sf.ohla.rti.testsuite.hla.rti1516e.object;

import java.util.UUID;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.OrderType;

@Test
public class InteractionPersistenceTestNG
  extends BaseInteractionTestNG
{
  private static final String FEDERATION_NAME = InteractionPersistenceTestNG.class.getSimpleName();
  private static final String SAVE_NAME = FEDERATION_NAME + UUID.randomUUID();

  public InteractionPersistenceTestNG()
  {
    super(3, FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).requestFederationSave(SAVE_NAME);

    federateAmbassadors.get(1).checkInitiateFederateSave(SAVE_NAME);
    federateAmbassadors.get(2).checkInitiateFederateSave(SAVE_NAME);

    // the save has started, but the sending federate hasn't acknowledged so he can still send messages, they will be
    // saved along with the federation state and sent upon continuation (or a restore)
    //
    rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle2, testParameterValues2, TAG);

    federateAmbassadors.get(0).checkInitiateFederateSave(SAVE_NAME);

    rtiAmbassadors.get(0).federateSaveBegun();
    rtiAmbassadors.get(1).federateSaveBegun();
    rtiAmbassadors.get(2).federateSaveBegun();

    rtiAmbassadors.get(0).federateSaveComplete();
    rtiAmbassadors.get(1).federateSaveComplete();
    rtiAmbassadors.get(2).federateSaveComplete();

    federateAmbassadors.get(0).checkFederationSaved(SAVE_NAME);
    federateAmbassadors.get(1).checkFederationSaved(SAVE_NAME);
    federateAmbassadors.get(2).checkFederationSaved(SAVE_NAME);
  }

  @Test
  public void testReceiveInteractionAfterSave()
    throws Exception
  {
    federateAmbassadors.get(1).checkParameterValues(
      testInteractionClassHandle, testParameterValues, TAG, OrderType.RECEIVE, reliableTransportationTypeHandle,
      federateHandles.get(0));
    federateAmbassadors.get(2).checkParameterValues(
      testInteractionClassHandle2, testParameterValues2, TAG, OrderType.RECEIVE, reliableTransportationTypeHandle,
      federateHandles.get(0));
  }

  @Test(dependsOnMethods = "testReceiveInteractionAfterSave")
  public void testReceiveInteractionAfterRestore()
    throws Exception
  {
    // resign, destroy, disconnect, reset, connect, create, join, restore

    resignFederationExecution();
    destroyFederationExecution(FEDERATION_NAME);
    disconnect();

    for (InteractionFederateAmbassador federateAmbassador : federateAmbassadors)
    {
      federateAmbassador.reset();
    }

    connect();
    createFederationExecution();
    joinFederationExecution();

    rtiAmbassadors.get(0).requestFederationRestore(SAVE_NAME);

    federateAmbassadors.get(0).checkRequestFederationRestoreSucceeded(SAVE_NAME);

    federateAmbassadors.get(0).checkInitiateFederateRestore(SAVE_NAME, FEDERATE_TYPE_1, federateHandles.get(0));
    federateAmbassadors.get(1).checkInitiateFederateRestore(SAVE_NAME, FEDERATE_TYPE_2, federateHandles.get(1));
    federateAmbassadors.get(2).checkInitiateFederateRestore(SAVE_NAME, FEDERATE_TYPE_3, federateHandles.get(2));

    rtiAmbassadors.get(0).federateRestoreComplete();
    rtiAmbassadors.get(1).federateRestoreComplete();
    rtiAmbassadors.get(2).federateRestoreComplete();

    federateAmbassadors.get(0).checkFederationRestored(SAVE_NAME);
    federateAmbassadors.get(1).checkFederationRestored(SAVE_NAME);
    federateAmbassadors.get(2).checkFederationRestored(SAVE_NAME);

    federateAmbassadors.get(1).checkParameterValues(
      testInteractionClassHandle, testParameterValues, TAG, OrderType.RECEIVE, reliableTransportationTypeHandle,
      federateHandles.get(0));
    federateAmbassadors.get(2).checkParameterValues(
      testInteractionClassHandle2, testParameterValues2, TAG, OrderType.RECEIVE, reliableTransportationTypeHandle,
      federateHandles.get(0));
  }
}
