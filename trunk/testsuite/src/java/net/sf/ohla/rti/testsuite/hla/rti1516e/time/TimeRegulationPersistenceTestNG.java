package net.sf.ohla.rti.testsuite.hla.rti1516e.time;

import java.util.UUID;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.ResignAction;
import hla.rti1516e.exceptions.TimeRegulationAlreadyEnabled;
import hla.rti1516e.time.HLAinteger64TimeFactory;

@Test
public class TimeRegulationPersistenceTestNG
  extends BaseTimeManagementTestNG
{
  private static final String FEDERATION_NAME = TimeRegulationPersistenceTestNG.class.getSimpleName();
  private static final String SAVE_NAME = FEDERATION_NAME + UUID.randomUUID();

  public TimeRegulationPersistenceTestNG()
  {
    super(FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);

    federateAmbassadors.get(0).checkTimeRegulationEnabled(initial);
    rtiAmbassadors.get(0).modifyLookahead(lookahead2);

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

  @Test(expectedExceptions = { TimeRegulationAlreadyEnabled.class })
  public void testEnableTimeRegulationAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);
  }

  @Test
  public void testQueryLookahead()
    throws Exception
  {
    assert lookahead2.equals(rtiAmbassadors.get(0).queryLookahead());
  }

  @Test
  public void testGALTUndefinedWithOneTimeRegulatingFederate()
    throws Exception
  {
    assert !rtiAmbassadors.get(0).queryGALT().timeIsValid;
  }

  @Test
  public void testLITSUndefinedWithOneTimeRegulatingFederate()
    throws Exception
  {
    assert !rtiAmbassadors.get(0).queryLITS().timeIsValid;
  }
}
