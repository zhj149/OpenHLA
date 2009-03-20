package net.sf.ohla.rti1516.time;

import net.sf.ohla.rti.hla.rti1516.Integer64Time;

import org.testng.annotations.Test;

import hla.rti1516.RequestForTimeConstrainedPending;
import hla.rti1516.TimeConstrainedAlreadyEnabled;
import hla.rti1516.TimeConstrainedIsNotEnabled;

public class TimeConstrainedTestNG
  extends BaseTimeManagementTestNG
{
  protected final Integer64Time oneHundred = new Integer64Time(100);

  @Test
  public void testEnableTimeConstrained()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeConstrained();
  }

  @Test(dependsOnMethods = {"testEnableTimeConstrained"},
        expectedExceptions = {RequestForTimeConstrainedPending.class})
  public void testEnableTimeConstrainedWhileEnableTimeConstrainedPending()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeConstrained();
  }

  @Test(dependsOnMethods = {"testEnableTimeConstrained"},
        expectedExceptions = {RequestForTimeConstrainedPending.class})
  public void testTimeAdvanceRequestWhileEnableTimeConstrainedPending()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequest(oneHundred);
  }

  @Test(dependsOnMethods = {"testEnableTimeConstrained"},
        expectedExceptions = {RequestForTimeConstrainedPending.class})
  public void testTimeAdvanceRequestAvailableWhileEnableTimeConstrainedPending()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequestAvailable(oneHundred);
  }

  @Test(dependsOnMethods = {"testEnableTimeConstrained"},
        expectedExceptions = {RequestForTimeConstrainedPending.class})
  public void testNextMessageRequestWhileEnableTimeConstrainedPending()
    throws Exception
  {
    rtiAmbassadors.get(0).nextMessageRequest(oneHundred);
  }

  @Test(dependsOnMethods = {"testEnableTimeConstrained"},
        expectedExceptions = {RequestForTimeConstrainedPending.class})
  public void testNextMessageRequestAvailableWhileEnableTimeConstrainedPending()
    throws Exception
  {
    rtiAmbassadors.get(0).nextMessageRequestAvailable(oneHundred);
  }

  @Test(dependsOnMethods = {"testEnableTimeConstrained"},
        expectedExceptions = {RequestForTimeConstrainedPending.class})
  public void testFlushQueueRequestWhileEnableTimeConstrainedPending()
    throws Exception
  {
    rtiAmbassadors.get(0).flushQueueRequest(oneHundred);
  }

  @Test(dependsOnMethods = {
    "testEnableTimeConstrainedWhileEnableTimeConstrainedPending",
    "testTimeAdvanceRequestWhileEnableTimeConstrainedPending",
    "testTimeAdvanceRequestAvailableWhileEnableTimeConstrainedPending",
    "testNextMessageRequestWhileEnableTimeConstrainedPending",
    "testNextMessageRequestAvailableWhileEnableTimeConstrainedPending",
    "testFlushQueueRequestWhileEnableTimeConstrainedPending"})
  public void testTimeConstrainedEnabled()
    throws Exception
  {
    federateAmbassadors.get(0).checkTimeConstrainedEnabled();
  }

  @Test(dependsOnMethods = {"testTimeConstrainedEnabled"},
        expectedExceptions = {TimeConstrainedAlreadyEnabled.class})
  public void testEnableTimeConstrainedAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeConstrained();
  }

  @Test(dependsOnMethods = {"testEnableTimeConstrainedAgain"})
  public void testDisableTimeConstrained()
    throws Exception
  {
    rtiAmbassadors.get(0).disableTimeConstrained();
  }

  @Test(dependsOnMethods = {"testDisableTimeConstrained"},
        expectedExceptions = {TimeConstrainedIsNotEnabled.class})
  public void testDisableTimeConstrainedAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).disableTimeConstrained();
  }
}
