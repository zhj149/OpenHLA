/*
 * Copyright (c) 2006, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti1516;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516.FederateInternalError;
import hla.rti1516.InvalidLogicalTime;
import hla.rti1516.InvalidLookahead;
import hla.rti1516.LogicalTime;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.NoRequestToEnableTimeConstrainedWasPending;
import hla.rti1516.NoRequestToEnableTimeRegulationWasPending;
import hla.rti1516.RTIambassador;
import hla.rti1516.RequestForTimeConstrainedPending;
import hla.rti1516.RequestForTimeRegulationPending;
import hla.rti1516.ResignAction;
import hla.rti1516.TimeConstrainedAlreadyEnabled;
import hla.rti1516.TimeConstrainedIsNotEnabled;
import hla.rti1516.TimeRegulationAlreadyEnabled;
import hla.rti1516.TimeRegulationIsNotEnabled;
import hla.rti1516.JoinedFederateIsNotInTimeAdvancingState;
import hla.rti1516.InTimeAdvancingState;
import hla.rti1516.LogicalTimeAlreadyPassed;
import hla.rti1516.jlc.NullFederateAmbassador;

public class TimeManagementTestNG
  extends BaseTestNG
{
  protected List<TestFederateAmbassador> federateAmbassadors =
    new ArrayList<TestFederateAmbassador>(5);

  protected LogicalTimeInterval lookahead1 = new Integer64TimeInterval(1);
  protected LogicalTimeInterval lookahead2 = new Integer64TimeInterval(2);

  protected Integer64Time zero = new Integer64Time(0);
  protected Integer64Time one = new Integer64Time(1);
  protected Integer64Time two = new Integer64Time(2);
  protected Integer64Time three = new Integer64Time(3);
  protected Integer64Time four = new Integer64Time(4);
  protected Integer64Time five = new Integer64Time(5);
  protected Integer64Time ten = new Integer64Time(10);
  protected Integer64Time twenty = new Integer64Time(20);
  protected Integer64Time oneHundred = new Integer64Time(100);

  public TimeManagementTestNG()
  {
    super(5);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);

    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(0)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(1)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(2)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(3)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(4)));

    rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(0),
      mobileFederateServices);
    rtiAmbassadors.get(1).joinFederationExecution(
      FEDERATE_TYPE + "2", FEDERATION_NAME, federateAmbassadors.get(1),
      mobileFederateServices);
    rtiAmbassadors.get(2).joinFederationExecution(
      FEDERATE_TYPE + "3", FEDERATION_NAME, federateAmbassadors.get(2),
      mobileFederateServices);
    rtiAmbassadors.get(3).joinFederationExecution(
      FEDERATE_TYPE + "4", FEDERATION_NAME, federateAmbassadors.get(3),
      mobileFederateServices);
    rtiAmbassadors.get(4).joinFederationExecution(
      FEDERATE_TYPE + "5", FEDERATION_NAME, federateAmbassadors.get(4),
      mobileFederateServices);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(1).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(2).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(3).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(4).resignFederationExecution(ResignAction.NO_ACTION);

    rtiAmbassadors.get(0).destroyFederationExecution(FEDERATION_NAME);
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
    federateAmbassadors.get(0).checkTimeRegulationEnabled();
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

  @Test(dependsOnMethods = {"testModifyLookahead"})
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

  @Test(
    dependsOnMethods = {"testDisableTimeRegulation"},
    expectedExceptions = {InvalidLookahead.class})
  public void testEnableTimeRegulationOfInvalidLookahead()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(null);
  }

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

  @Test
  public void testTimeAdvanceRequestWhileNeitherRegulatingOrConstrained()
    throws Exception
  {
    rtiAmbassadors.get(1).timeAdvanceRequest(ten);
  }

  @Test(dependsOnMethods =
    {"testTimeAdvanceRequestWhileNeitherRegulatingOrConstrained"},
        expectedExceptions = {InTimeAdvancingState.class})
  public void testEnableTimeRegulationWhileInTimeAdvancingState()
    throws Exception
  {
    rtiAmbassadors.get(1).enableTimeRegulation(lookahead1);
  }

  @Test(dependsOnMethods =
    {"testTimeAdvanceRequestWhileNeitherRegulatingOrConstrained"},
        expectedExceptions = {InTimeAdvancingState.class})
  public void testEnableTimeConstrainedWhileInTimeAdvancingState()
    throws Exception
  {
    rtiAmbassadors.get(1).enableTimeConstrained();
  }

  @Test(dependsOnMethods =
    {"testTimeAdvanceRequestWhileNeitherRegulatingOrConstrained"},
        expectedExceptions = {InTimeAdvancingState.class})
  public void testTimeAdvanceRequestWhileInTimeAdvancingState()
    throws Exception
  {
    rtiAmbassadors.get(1).timeAdvanceRequest(oneHundred);
  }

  @Test(dependsOnMethods =
    {"testTimeAdvanceRequestWhileNeitherRegulatingOrConstrained"},
        expectedExceptions = {InTimeAdvancingState.class})
  public void testTimeAdvanceRequestAvailableWhileInTimeAdvancingState()
    throws Exception
  {
    rtiAmbassadors.get(1).timeAdvanceRequestAvailable(oneHundred);
  }

  @Test(dependsOnMethods =
    {"testTimeAdvanceRequestWhileNeitherRegulatingOrConstrained"},
        expectedExceptions = {InTimeAdvancingState.class})
  public void testNextMessageRequestWhileInTimeAdvancingState()
    throws Exception
  {
    rtiAmbassadors.get(1).nextMessageRequest(oneHundred);
  }

  @Test(dependsOnMethods =
    {"testTimeAdvanceRequestWhileNeitherRegulatingOrConstrained"},
        expectedExceptions = {InTimeAdvancingState.class})
  public void testNextMessageRequestAvailableWhileInTimeAdvancingState()
    throws Exception
  {
    rtiAmbassadors.get(1).nextMessageRequestAvailable(oneHundred);
  }

  @Test(dependsOnMethods =
    {"testTimeAdvanceRequestWhileNeitherRegulatingOrConstrained"},
        expectedExceptions = {InTimeAdvancingState.class})
  public void testFlushQueueRequestWhileInTimeAdvancingState()
    throws Exception
  {
    rtiAmbassadors.get(1).flushQueueRequest(oneHundred);
  }

  @Test(dependsOnMethods = {
    "testEnableTimeRegulationWhileInTimeAdvancingState",
    "testEnableTimeConstrainedWhileInTimeAdvancingState",
    "testTimeAdvanceRequestWhileInTimeAdvancingState",
    "testTimeAdvanceRequestAvailableWhileInTimeAdvancingState",
    "testNextMessageRequestWhileInTimeAdvancingState",
    "testNextMessageRequestAvailableWhileInTimeAdvancingState",
    "testFlushQueueRequestWhileInTimeAdvancingState"})
  public void testTimeAdvanceGrantWhileNeitherRegulatingOrConstrained()
    throws Exception
  {
    federateAmbassadors.get(1).checkTimeAdvanceGrant(ten);

    assert ten.equals(rtiAmbassadors.get(1).queryLogicalTime());
  }

  @Test(dependsOnMethods = {
    "testTimeAdvanceGrantWhileNeitherRegulatingOrConstrained"},
        expectedExceptions = {LogicalTimeAlreadyPassed.class})
  public void testTimeAdvanceRequestToLogicalTimeAlreadyPassed()
    throws Exception
  {
    rtiAmbassadors.get(1).timeAdvanceRequest(five);
  }

  @Test(dependsOnMethods = {
    "testTimeAdvanceRequestToLogicalTimeAlreadyPassed"})
  public void testTimeAdvanceRequestToSameTime()
    throws Exception
  {
    rtiAmbassadors.get(1).timeAdvanceRequest(ten);
    federateAmbassadors.get(1).checkTimeAdvanceGrant(ten);

    assert ten.equals(rtiAmbassadors.get(1).queryLogicalTime());
  }

  @Test(dependsOnMethods = {"testTimeAdvanceRequestToSameTime"})
  public void testTimeAdvanceRequestToNextTime()
    throws Exception
  {
    rtiAmbassadors.get(1).timeAdvanceRequest(twenty);
    federateAmbassadors.get(1).checkTimeAdvanceGrant(twenty);

    assert twenty.equals(rtiAmbassadors.get(1).queryLogicalTime());
  }

  @Test(dependsOnMethods = {"testDisableTimeRegulation",
    "testDisableTimeConstrained", "testTimeAdvanceRequestToNextTime"})
  public void testTimeAdvanceRequest()
    throws Exception
  {
    rtiAmbassadors.get(2).enableTimeRegulation(lookahead1);
    federateAmbassadors.get(2).checkTimeRegulationEnabled(zero);

    rtiAmbassadors.get(4).enableTimeRegulation(lookahead1);
    federateAmbassadors.get(4).checkTimeRegulationEnabled(zero);

    rtiAmbassadors.get(3).enableTimeConstrained();
    federateAmbassadors.get(3).checkTimeConstrainedEnabled(zero);

    rtiAmbassadors.get(4).enableTimeConstrained();
    federateAmbassadors.get(4).checkTimeConstrainedEnabled(zero);

    // request advance by regulating-only federate
    //
    rtiAmbassadors.get(2).timeAdvanceRequest(five);

    // should be immediately granted because not constrained
    //
    federateAmbassadors.get(2).checkTimeAdvanceGrant(five);

    // request advance by constrained-only federate
    //
    rtiAmbassadors.get(3).timeAdvanceRequest(three);

    // should NOT be granted because other regulating federate not advanced
    //
    federateAmbassadors.get(3).checkTimeAdvanceGrantNotGranted(zero);

    // request advance by regulating-and-constrained federate
    //
    rtiAmbassadors.get(4).timeAdvanceRequest(two);

    // should be immediately granted because other regulating federate is
    // requesting advance to five
    //
    federateAmbassadors.get(4).checkTimeAdvanceGrant(two);

    // request advance by regulating-and-constrained federate
    //
    rtiAmbassadors.get(4).timeAdvanceRequest(four);

    // should be immediately granted because other regulating federate is
    // requesting advance to five
    //
    federateAmbassadors.get(4).checkTimeAdvanceGrant(four);

    // should be granted because regulating federates are at four
    //
    federateAmbassadors.get(3).checkTimeAdvanceGrant(three);

    // request advance by constrained-only federate
    //
    rtiAmbassadors.get(3).timeAdvanceRequest(four);

    // should be granted because regulating federates are at four
    //
    federateAmbassadors.get(3).checkTimeAdvanceGrant(four);
  }

  protected static class TestFederateAmbassador
    extends NullFederateAmbassador
  {
    protected RTIambassador rtiAmbassador;

    protected LogicalTime timeRegulationEnabledTime;
    protected LogicalTime timeConstrainedEnabledTime;
    protected LogicalTime federateTime;

    public TestFederateAmbassador(RTIambassador rtiAmbassador)
    {
      this.rtiAmbassador = rtiAmbassador;
    }

    public void checkTimeRegulationEnabled()
      throws Exception
    {
      timeRegulationEnabledTime = null;
      for (int i = 0; i < 5 && timeRegulationEnabledTime == null; i++)
      {
        rtiAmbassador.evokeMultipleCallbacks(.1, 1.0);
      }
      assert timeRegulationEnabledTime != null;
    }

    public void checkTimeRegulationEnabled(LogicalTime time)
      throws Exception
    {
      timeRegulationEnabledTime = null;
      for (int i = 0; i < 5 && timeRegulationEnabledTime == null; i++)
      {
        rtiAmbassador.evokeMultipleCallbacks(.1, 1.0);
      }
      assert time.equals(timeRegulationEnabledTime);
    }

    public void checkTimeConstrainedEnabled()
      throws Exception
    {
      timeConstrainedEnabledTime = null;
      for (int i = 0; i < 5 && timeConstrainedEnabledTime == null; i++)
      {
        rtiAmbassador.evokeMultipleCallbacks(.1, 1.0);
      }
      assert timeConstrainedEnabledTime != null;
    }

    public void checkTimeConstrainedEnabled(LogicalTime time)
      throws Exception
    {
      timeConstrainedEnabledTime = null;
      for (int i = 0; i < 5 && timeConstrainedEnabledTime == null; i++)
      {
        rtiAmbassador.evokeMultipleCallbacks(.1, 1.0);
      }
      assert time.equals(timeConstrainedEnabledTime);
    }

    public void checkTimeAdvanceGrant(LogicalTime time)
      throws Exception
    {
      federateTime = null;
      for (int i = 0; i < 5 && federateTime == null; i++)
      {
        rtiAmbassador.evokeMultipleCallbacks(.1, 1.0);
      }
      assert time.equals(federateTime);
    }

    public void checkTimeAdvanceGrantNotGranted(LogicalTime time)
      throws Exception
    {
      federateTime = null;
      for (int i = 0; i < 5; i++)
      {
        rtiAmbassador.evokeMultipleCallbacks(.1, 1.0);
      }
      assert federateTime == null;
    }

    @Override
    public void timeRegulationEnabled(LogicalTime time)
      throws InvalidLogicalTime, NoRequestToEnableTimeRegulationWasPending,
             FederateInternalError
    {
      timeRegulationEnabledTime = time;
    }

    @Override
    public void timeConstrainedEnabled(LogicalTime time)
      throws InvalidLogicalTime, NoRequestToEnableTimeConstrainedWasPending,
             FederateInternalError
    {
      timeConstrainedEnabledTime = time;
    }

    @Override
    public void timeAdvanceGrant(LogicalTime time)
      throws InvalidLogicalTime, JoinedFederateIsNotInTimeAdvancingState,
             FederateInternalError
    {
      federateTime = time;
    }
  }

//  public void test()
//  {
//    TimeClient tc = new TimeClient("A", new Integer64TimeInterval(3000), false);
//    TimeClient tc2 = new TimeClient("B", new Integer64TimeInterval(5000), true);
////    TimeClient tc3 = new TimeClient(35000);
//
//    tc.timeAdvanceGrant(time);
//    tc2.timeAdvanceGrant(time);
//
//    tc.start();
//    tc2.start();
////    tc3.start();
//
//    new Thread()
//    {
//      public void run()
//      {
//        int i = 0;
//        while (true)
//        {
//          log.debug(String.format("[%d] %s", i++, time));
//          try
//          {
//            Thread.sleep(1000);
//          }
//          catch (InterruptedException e)
//          {
//            e.printStackTrace();
//          }
//        }
//      }
//    }.start();
//  }
//
//  protected class TimeClient
//    extends Thread
//  {
//    protected Logger log;
//
//    protected String name;
//    protected Integer64TimeInterval step;
//    protected boolean available;
//
//    protected LogicalTime time;
//    protected LogicalTime timeRequested;
//    protected boolean advanceGranted;
//
//    public TimeClient(String name, Integer64TimeInterval step, boolean available)
//    {
//      log = LoggerFactory.getLogger(name);
//
//      this.name = name;
//      this.step = step;
//      this.available = available;
//
//      clients.add(this);
//    }
//
//    public synchronized void timeAdvanceGrant(LogicalTime time)
//    {
//      log.debug(String.format("advance granted to %s", time));
//      this.time = time;
//      advanceGranted = true;
//      interrupt();
//      notifyAll();
//    }
//
//    public void run()
//    {
//      do
//      {
//        try
//        {
//          timeRequested = time.add(step);
//        }
//        catch (IllegalTimeArithmetic illegalTimeArithmetic)
//        {
//          illegalTimeArithmetic.printStackTrace();
//        }
//        if (available)
//        {
//          timeAdvanceRequestAvailable(timeRequested, this);
//        }
//        else
//        {
//          timeAdvanceRequest(timeRequested, this);
//        }
//
//        long currentTime = System.currentTimeMillis();
//        Integer64TimeInterval distance =
//          (Integer64TimeInterval) time.distance(timeRequested);
//
//        log.debug(String.format("%d %d %d", currentTime, distance.interval, step.interval));
//
//        long waitUntil = currentTime + step.interval;
//        for (long waitTime = waitUntil - System.currentTimeMillis();
//             waitTime > 0; waitTime = waitUntil - System.currentTimeMillis())
//        {
//          synchronized (this)
//          {
//            try
//            {
//              log.debug(String.format("waiting until %d", waitUntil));
//              wait(waitTime);
//            }
//            catch (InterruptedException ie)
//            {
//              log.debug(String.format("interrupted"));
//            }
//          }
//        }
//
//        synchronized (this)
//        {
//          while (!advanceGranted)
//          {
//            try
//            {
//              wait();
//            }
//            catch (InterruptedException ie)
//            {
//            }
//          }
//          advanceGranted = false;
//        }
//      }
//      while (true);
//    }
//  }
}
