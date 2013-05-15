package net.sf.ohla.rti.testsuite.hla.rti1516e.time;

import java.net.URL;

import java.util.UUID;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.ResignAction;
import hla.rti1516e.exceptions.TimeConstrainedAlreadyEnabled;
import hla.rti1516e.time.HLAinteger64TimeFactory;

@Test
public class TimeConstrainedPersistenceTestNG
  extends BaseTimeManagementTestNG
{
  private static final String FEDERATION_NAME = TimeConstrainedPersistenceTestNG.class.getSimpleName();
  private static final String SAVE_NAME = FEDERATION_NAME + UUID.randomUUID();

  public TimeConstrainedPersistenceTestNG()
  {
    super(FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeConstrained();

    federateAmbassadors.get(0).checkTimeConstrainedEnabled(initial);

    rtiAmbassadors.get(0).requestFederationSave(SAVE_NAME);

    federateAmbassadors.get(0).checkInitiateFederateSave(SAVE_NAME);

    rtiAmbassadors.get(0).federateSaveBegun();

    rtiAmbassadors.get(0).federateSaveComplete();

    federateAmbassadors.get(0).checkFederationSaved(SAVE_NAME);

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

    rtiAmbassadors.get(0).federateRestoreComplete();

    federateAmbassadors.get(0).checkFederationRestored(SAVE_NAME);
  }

  @Test(expectedExceptions = { TimeConstrainedAlreadyEnabled.class })
  public void testEnableTimeConstrainedAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeConstrained();
  }
}
