package net.sf.ohla.rti.testsuite.hla.rti1516.time;

import org.testng.annotations.Test;

import hla.rti1516.TimeConstrainedAlreadyEnabled;
import hla.rti1516.TimeConstrainedIsNotEnabled;

@Test
public class TimeConstrainedTestNG
  extends BaseTimeManagementTestNG
{
  private static final String FEDERATION_NAME = "OHLA IEEE 1516 Time Constrained Test Federation";

  public TimeConstrainedTestNG()
  {
    super(FEDERATION_NAME);
  }

  @Test
  public void testEnableTimeConstrained()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeConstrained();

    federateAmbassadors.get(0).checkTimeConstrainedEnabled(initial);
  }

  @Test(dependsOnMethods = {"testEnableTimeConstrained"}, expectedExceptions = {TimeConstrainedAlreadyEnabled.class})
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

  @Test(dependsOnMethods = {"testDisableTimeConstrained"}, expectedExceptions = {TimeConstrainedIsNotEnabled.class})
  public void testDisableTimeConstrainedAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).disableTimeConstrained();
  }
}
