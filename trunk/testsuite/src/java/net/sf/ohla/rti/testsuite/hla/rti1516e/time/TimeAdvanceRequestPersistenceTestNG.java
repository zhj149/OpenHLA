package net.sf.ohla.rti.testsuite.hla.rti1516e.time;

import java.util.UUID;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.ResignAction;
import hla.rti1516e.exceptions.LogicalTimeAlreadyPassed;
import hla.rti1516e.time.HLAinteger64TimeFactory;

@Test
public class TimeAdvanceRequestPersistenceTestNG
  extends BaseTimeAdvanceRequestTestNG
{
  private static final String FEDERATION_NAME = TimeAdvanceRequestPersistenceTestNG.class.getSimpleName();
  private static final String SAVE_NAME = FEDERATION_NAME + UUID.randomUUID();

  public TimeAdvanceRequestPersistenceTestNG()
  {
    super(FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    // advance both federates 4
    //
    rtiAmbassadors.get(0).timeAdvanceRequest(three);
    rtiAmbassadors.get(1).timeAdvanceRequest(three);

    federateAmbassadors.get(0).checkTimeAdvanceGrant(three);
    federateAmbassadors.get(1).checkTimeAdvanceGrant(three);

    rtiAmbassadors.get(0).requestFederationSave(SAVE_NAME);

    federateAmbassadors.get(1).checkInitiateFederateSave(SAVE_NAME);

    // the save has started, but the sending federate hasn't acknowledged so he can still send messages, they will be
    // saved along with the federation state and sent upon continuation (or a restore)
    //

    federateAmbassadors.get(0).checkInitiateFederateSave(SAVE_NAME);

    rtiAmbassadors.get(0).federateSaveBegun();
    rtiAmbassadors.get(1).federateSaveBegun();

    rtiAmbassadors.get(0).federateSaveComplete();
    rtiAmbassadors.get(1).federateSaveComplete();

    federateAmbassadors.get(0).checkFederationSaved(SAVE_NAME);
    federateAmbassadors.get(1).checkFederationSaved(SAVE_NAME);
  }

  @Test(expectedExceptions = { LogicalTimeAlreadyPassed.class })
  public void testTimeAdvanceRequestToLogicalTimeAlreadyPassed()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequest(two);
  }

  @Test(dependsOnMethods = { "testTimeAdvanceRequestToLogicalTimeAlreadyPassed" })
  public void testTimeAdvanceRequestAfterSave()
    throws Exception
  {
    // advance federate 0 to 10, this will make both messages available to federate 1
    //
    rtiAmbassadors.get(0).timeAdvanceRequest(ten);

    // advance federate 1 to 4
    //
    rtiAmbassadors.get(1).timeAdvanceRequest(four);
    federateAmbassadors.get(1).checkTimeAdvanceGrant(four);

    // nothing should be received yet
    //
    federateAmbassadors.get(1).checkParameterValuesNotReceived();

    // advance federate 1 to 5
    //
    rtiAmbassadors.get(1).timeAdvanceRequest(five);

    // the first message should be received
    //
    federateAmbassadors.get(1).checkParameterValues(testParameterValues, five);

    federateAmbassadors.get(1).checkTimeAdvanceGrant(five);

    // advance federate 1 to 4
    //
    rtiAmbassadors.get(1).timeAdvanceRequest(nine);
    federateAmbassadors.get(1).checkTimeAdvanceGrant(nine);

    // nothing should be received yet
    //
    federateAmbassadors.get(1).checkAttributeValuesNotReceived(testObjectInstanceHandle);

    // advance federate 1 to 10
    //
    rtiAmbassadors.get(1).timeAdvanceRequest(ten);

    // the second message should be received
    //
    federateAmbassadors.get(1).checkAttributeValues(testObjectInstanceHandle, testAttributeValues, ten);

    federateAmbassadors.get(0).checkTimeAdvanceGrant(ten);
    federateAmbassadors.get(1).checkTimeAdvanceGrant(ten);
  }

  @Test(dependsOnMethods = "testTimeAdvanceRequestAfterSave")
  public void testReceiveInteractionAfterRestore()
    throws Exception
  {
    // resign, destroy, disconnect, reset, connect, create, join, restore

    resignFederationExecution(ResignAction.UNCONDITIONALLY_DIVEST_ATTRIBUTES);
    destroyFederationExecution();
    disconnect();

    for (TimeManagementFederateAmbassador federateAmbassador : federateAmbassadors)
    {
      federateAmbassador.reset();
    }

    connect();
    createFederationExecution(HLAinteger64TimeFactory.NAME);
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
  }
}
