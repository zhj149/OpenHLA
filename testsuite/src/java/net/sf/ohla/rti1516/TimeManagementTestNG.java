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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hla.rti1516.LogicalTime;
import hla.rti1516.IllegalTimeArithmetic;

public class TimeManagementTestNG
{
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
