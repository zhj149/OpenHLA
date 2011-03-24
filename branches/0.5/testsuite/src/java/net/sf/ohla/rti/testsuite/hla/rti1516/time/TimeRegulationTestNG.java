package net.sf.ohla.rti.testsuite.hla.rti1516.time;

import net.sf.ohla.rti.hla.rti1516.Integer64Time;
import net.sf.ohla.rti.hla.rti1516.Integer64TimeInterval;

import org.testng.annotations.Test;

import hla.rti1516.InvalidLookahead;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.RequestForTimeRegulationPending;
import hla.rti1516.TimeRegulationAlreadyEnabled;
import hla.rti1516.TimeRegulationIsNotEnabled;

public class TimeRegulationTestNG
  extends BaseTimeManagementTestNG
{
  protected final LogicalTimeInterval lookahead1 = new Integer64TimeInterval(1);
  protected final LogicalTimeInterval lookahead2 = new Integer64TimeInterval(2);

  protected final Integer64Time zero = new Integer64Time(0);
  protected final Integer64Time ninetyNine = new Integer64Time(99);
  protected final Integer64Time oneHundred = new Integer64Time(100);
  protected final Integer64Time oneHundredOne = new Integer64Time(101);

  public TimeRegulationTestNG()
  {
    super(2);
  }

  @Test
  public void testEnableTimeRegulation()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);
  }

  @Test(dependsOnMethods = {"testEnableTimeRegulation"},
        expectedExceptions = {RequestForTimeRegulationPending.class})
  public void testEnableTimeRegulationWhileEnableTimeRegulationPending()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);
  }

  @Test(dependsOnMethods = {"testEnableTimeRegulation"},
        expectedExceptions = {RequestForTimeRegulationPending.class})
  public void testTimeAdvanceRequestWhileEnableTimeRegulationPending()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequest(oneHundred);
  }

  @Test(dependsOnMethods = {"testEnableTimeRegulation"},
        expectedExceptions = {RequestForTimeRegulationPending.class})
  public void testTimeAdvanceRequestAvailableWhileEnableTimeRegulationPending()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequestAvailable(oneHundred);
  }

  @Test(dependsOnMethods = {"testEnableTimeRegulation"},
        expectedExceptions = {RequestForTimeRegulationPending.class})
  public void testNextMessageRequestWhileEnableTimeRegulationPending()
    throws Exception
  {
    rtiAmbassadors.get(0).nextMessageRequest(oneHundred);
  }

  @Test(dependsOnMethods = {"testEnableTimeRegulation"},
        expectedExceptions = {RequestForTimeRegulationPending.class})
  public void testNextMessageRequestAvailableWhileEnableTimeRegulationPending()
    throws Exception
  {
    rtiAmbassadors.get(0).nextMessageRequestAvailable(oneHundred);
  }

  @Test(dependsOnMethods = {"testEnableTimeRegulation"},
        expectedExceptions = {RequestForTimeRegulationPending.class})
  public void testFlushQueueRequestWhileEnableTimeRegulationPending()
    throws Exception
  {
    rtiAmbassadors.get(0).flushQueueRequest(oneHundred);
  }

  @Test(dependsOnMethods = {
    "testEnableTimeRegulationWhileEnableTimeRegulationPending",
    "testTimeAdvanceRequestWhileEnableTimeRegulationPending",
    "testTimeAdvanceRequestAvailableWhileEnableTimeRegulationPending",
    "testNextMessageRequestWhileEnableTimeRegulationPending",
    "testNextMessageRequestAvailableWhileEnableTimeRegulationPending",
    "testFlushQueueRequestWhileEnableTimeRegulationPending"})
  public void testTimeRegulationEnabled()
    throws Exception
  {
    federateAmbassadors.get(0).checkTimeRegulationEnabled(zero);
  }

  @Test(dependsOnMethods = {"testTimeRegulationEnabled"})
  public void testGALTUndefinedWithOneTimeRegulatingFederate()
    throws Exception
  {
    assert !rtiAmbassadors.get(0).queryGALT().timeIsValid;
  }

  @Test(dependsOnMethods = {"testTimeRegulationEnabled"})
  public void testLITSUndefinedWithOneTimeRegulatingFederate()
    throws Exception
  {
    assert !rtiAmbassadors.get(0).queryLITS().timeIsValid;
  }

  @Test(dependsOnMethods = {"testTimeRegulationEnabled"},
        expectedExceptions = {TimeRegulationAlreadyEnabled.class})
  public void testEnableTimeRegulationAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);
  }

  @Test(dependsOnMethods = {"testTimeRegulationEnabled"})
  public void testQueryLookahead()
    throws Exception
  {
    assert lookahead1.equals(rtiAmbassadors.get(0).queryLookahead());
  }

  @Test(dependsOnMethods = {"testQueryLookahead"})
  public void testModifyLookahead()
    throws Exception
  {
    rtiAmbassadors.get(0).modifyLookahead(lookahead2);
    assert lookahead2.equals(rtiAmbassadors.get(0).queryLookahead());

    rtiAmbassadors.get(0).modifyLookahead(lookahead1);
    assert lookahead1.equals(rtiAmbassadors.get(0).queryLookahead());
  }

  @Test(dependsOnMethods = {
    "testModifyLookahead",
    "testGALTUndefinedWithOneTimeRegulatingFederate"})
  public void testDisableTimeRegulation()
    throws Exception
  {
    rtiAmbassadors.get(0).disableTimeRegulation();
  }

  @Test(dependsOnMethods = {"testDisableTimeRegulation"},
        expectedExceptions = {TimeRegulationIsNotEnabled.class})
  public void testDisableTimeRegulationAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).disableTimeRegulation();
  }

  @Test(dependsOnMethods = {"testDisableTimeRegulation"},
        expectedExceptions = {TimeRegulationIsNotEnabled.class})
  public void testQueryLookaheadWhenTimeRegulationDisabled()
    throws Exception
  {
    rtiAmbassadors.get(0).queryLookahead();
  }

  @Test(dependsOnMethods = {"testDisableTimeRegulation"},
        expectedExceptions = {TimeRegulationIsNotEnabled.class})
  public void testModifyLookaheadWhenTimeRegulationDisabled()
    throws Exception
  {
    rtiAmbassadors.get(0).modifyLookahead(lookahead1);
  }

  @Test(dependsOnMethods = {"testDisableTimeRegulation"},
        expectedExceptions = {InvalidLookahead.class})
  public void testEnableTimeRegulationOfInvalidLookahead()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(null);
  }

  @Test(dependsOnMethods = {
    "testDisableTimeRegulationAgain",
    "testQueryLookaheadWhenTimeRegulationDisabled",
    "testModifyLookaheadWhenTimeRegulationDisabled",
    "testEnableTimeRegulationOfInvalidLookahead"})
  public void testEnableTimeRegulationAfterDisabled()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);
    federateAmbassadors.get(0).checkTimeRegulationEnabled(zero);

    rtiAmbassadors.get(0).timeAdvanceRequest(oneHundred);
    federateAmbassadors.get(0).checkTimeAdvanceGrant(oneHundred);
  }

  @Test(dependsOnMethods = {"testEnableTimeRegulationAfterDisabled"})
  public void testEnableTimeRegulationOf2ndFederate()
    throws Exception
  {
    rtiAmbassadors.get(1).enableTimeRegulation(lookahead2);
    federateAmbassadors.get(1).checkTimeRegulationEnabled(ninetyNine);
  }

  @Test(dependsOnMethods = {"testEnableTimeRegulationOf2ndFederate"})
  public void testGALTDefinedForBothFederates()
    throws Exception
  {
    federateAmbassadors.get(0).checkGALT(oneHundredOne);
    federateAmbassadors.get(1).checkGALT(oneHundredOne);
  }

  @Test(dependsOnMethods = {"testEnableTimeRegulationOf2ndFederate"})
  public void testLITSDefinedForBothFederates()
    throws Exception
  {
    federateAmbassadors.get(0).checkLITS(oneHundredOne);
    federateAmbassadors.get(1).checkLITS(oneHundredOne);
  }
}
