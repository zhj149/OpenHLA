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
import hla.rti1516.jlc.NullFederateAmbassador;

public class TimeManagementTestNG
  extends BaseTestNG
{
  protected List<TestFederateAmbassador> federateAmbassadors =
    new ArrayList<TestFederateAmbassador>(3);

  protected LogicalTimeInterval lookahead1 = new Integer64TimeInterval(1);
  protected LogicalTimeInterval lookahead2 = new Integer64TimeInterval(2);

  public TimeManagementTestNG()
  {
    super(3);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);

    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(0)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(1)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(2)));

    rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(0),
      mobileFederateServices);
    rtiAmbassadors.get(1).joinFederationExecution(
      FEDERATE_TYPE + "2", FEDERATION_NAME, federateAmbassadors.get(1),
      mobileFederateServices);
    rtiAmbassadors.get(2).joinFederationExecution(
      FEDERATE_TYPE + "3", FEDERATION_NAME, federateAmbassadors.get(2),
      mobileFederateServices);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(1).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(2).resignFederationExecution(ResignAction.NO_ACTION);

    rtiAmbassadors.get(0).destroyFederationExecution(FEDERATION_NAME);
  }

  @Test
  public void testEnableTimeRegulation()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);
    federateAmbassadors.get(0).checkTimeRegulationEnabled();

    rtiAmbassadors.get(2).enableTimeRegulation(lookahead2);
    federateAmbassadors.get(2).checkTimeRegulationEnabled();
  }

  @Test(dependsOnMethods = {"testEnableTimeRegulation"},
        expectedExceptions = {TimeRegulationAlreadyEnabled.class})
  public void testEnableTimeRegulationAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);
  }

  @Test(dependsOnMethods = {"testEnableTimeRegulationAgain"})
  public void testQueryLookahead()
    throws Exception
  {
    assert lookahead1.equals(rtiAmbassadors.get(0).queryLookahead());
    assert lookahead2.equals(rtiAmbassadors.get(2).queryLookahead());
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

  @Test(dependsOnMethods = {"testDisableTimeRegulationAgain"},
        expectedExceptions = {TimeRegulationIsNotEnabled.class})
  public void testQueryLookaheadWhenTimeRegulationDisabled()
    throws Exception
  {
    rtiAmbassadors.get(0).queryLookahead();
  }

  @Test(dependsOnMethods = {"testQueryLookaheadWhenTimeRegulationDisabled"},
        expectedExceptions = {TimeRegulationIsNotEnabled.class})
  public void testModifyLookaheadWhenTimeRegulationDisabled()
    throws Exception
  {
    rtiAmbassadors.get(0).modifyLookahead(lookahead1);
  }

  @Test(dependsOnMethods = {"testModifyLookaheadWhenTimeRegulationDisabled"},
        expectedExceptions = {RequestForTimeRegulationPending.class})
  public void testEnableTimeRegulationWhileEnableTimeRegulationPending()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);

    try
    {
      rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);
    }
    finally
    {
      federateAmbassadors.get(0).checkTimeRegulationEnabled();
      rtiAmbassadors.get(0).disableTimeRegulation();
    }
  }

  @Test(
    dependsOnMethods = {"testEnableTimeRegulationWhileEnableTimeRegulationPending"},
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
    rtiAmbassadors.get(1).enableTimeConstrained();
    federateAmbassadors.get(1).checkTimeConstrainedEnabled();

    rtiAmbassadors.get(2).enableTimeConstrained();
    federateAmbassadors.get(2).checkTimeConstrainedEnabled();
  }

  @Test(dependsOnMethods = {"testEnableTimeConstrained"},
        expectedExceptions = {TimeConstrainedAlreadyEnabled.class})
  public void testEnableTimeConstrainedAgain()
    throws Exception
  {
    rtiAmbassadors.get(1).enableTimeConstrained();
  }

  @Test(dependsOnMethods = {"testEnableTimeConstrainedAgain"})
  public void testDisableTimeConstrained()
    throws Exception
  {
    rtiAmbassadors.get(1).disableTimeConstrained();
  }

  @Test(dependsOnMethods = {"testDisableTimeConstrained"},
        expectedExceptions = {TimeConstrainedIsNotEnabled.class})
  public void testDisableTimeConstrainedAgain()
    throws Exception
  {
    rtiAmbassadors.get(1).disableTimeConstrained();
  }

  @Test(dependsOnMethods = {"testDisableTimeConstrainedAgain"},
        expectedExceptions = {RequestForTimeConstrainedPending.class})
  public void testEnableTimeConstrainedWhileEnableTimeConstrainedPending()
    throws Exception
  {
    rtiAmbassadors.get(1).enableTimeConstrained();

    try
    {
      rtiAmbassadors.get(1).enableTimeConstrained();
    }
    finally
    {
      federateAmbassadors.get(1).checkTimeConstrainedEnabled();
      rtiAmbassadors.get(1).disableTimeConstrained();
    }
  }

  protected static class TestFederateAmbassador
    extends NullFederateAmbassador
  {
    protected RTIambassador rtiAmbassador;

    protected LogicalTime timeRegulationEnabledTime;
    protected LogicalTime timeConstrainedEnabledTime;

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
