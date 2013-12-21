package net.sf.ohla.rti.testsuite.hla.rti1516.time;

import org.testng.annotations.Test;

import hla.rti1516.InvalidLookahead;
import hla.rti1516.TimeRegulationAlreadyEnabled;
import hla.rti1516.TimeRegulationIsNotEnabled;

@Test
public class TimeRegulationTestNG
  extends BaseTimeManagementTestNG
{
  private static final String FEDERATION_NAME = TimeRegulationTestNG.class.getSimpleName();

  public TimeRegulationTestNG()
  {
    super(FEDERATION_NAME);
  }

  @Test
  public void testEnableTimeRegulation()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);

    federateAmbassadors.get(0).checkTimeRegulationEnabled(initial);

    assert lookahead1.equals(rtiAmbassadors.get(0).queryLookahead());
  }

  @Test(dependsOnMethods = "testEnableTimeRegulation", expectedExceptions = TimeRegulationAlreadyEnabled.class)
  public void testEnableTimeRegulationAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);
  }

  @Test(dependsOnMethods = "testEnableTimeRegulation")
  public void testModifyLookahead()
    throws Exception
  {
    rtiAmbassadors.get(0).modifyLookahead(lookahead2);
    assert lookahead2.equals(rtiAmbassadors.get(0).queryLookahead());

    rtiAmbassadors.get(0).modifyLookahead(lookahead1);
    assert lookahead1.equals(rtiAmbassadors.get(0).queryLookahead());
  }

  @Test(dependsOnMethods = "testModifyLookahead")
  public void testDisableTimeRegulation()
    throws Exception
  {
    rtiAmbassadors.get(0).disableTimeRegulation();
  }

  @Test(dependsOnMethods = "testDisableTimeRegulation", expectedExceptions = TimeRegulationIsNotEnabled.class)
  public void testDisableTimeRegulationAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).disableTimeRegulation();
  }

  @Test(dependsOnMethods = "testDisableTimeRegulation", expectedExceptions = TimeRegulationIsNotEnabled.class)
  public void testQueryLookaheadWhenTimeRegulationDisabled()
    throws Exception
  {
    rtiAmbassadors.get(0).queryLookahead();
  }

  @Test(dependsOnMethods = "testDisableTimeRegulation", expectedExceptions = TimeRegulationIsNotEnabled.class)
  public void testModifyLookaheadWhenTimeRegulationDisabled()
    throws Exception
  {
    rtiAmbassadors.get(0).modifyLookahead(lookahead1);
  }

  @Test(dependsOnMethods = "testDisableTimeRegulation", expectedExceptions = InvalidLookahead.class)
  public void testEnableTimeRegulationWithNullLookahead()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(null);
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
