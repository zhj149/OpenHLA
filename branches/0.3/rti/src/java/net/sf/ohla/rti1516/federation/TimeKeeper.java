package net.sf.ohla.rti1516.federation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.ohla.rti1516.Integer64TimeFactory;
import net.sf.ohla.rti1516.Integer64TimeInterval;

import hla.rti1516.IllegalTimeArithmetic;
import hla.rti1516.LogicalTime;
import hla.rti1516.LogicalTimeFactory;

public class TimeKeeper
{
  protected LogicalTime time;
  protected LogicalTimeFactory logicalTimeFactory;

  protected LogicalTime minAdvanceRequest;

  protected Set<TimeClient> clients = new HashSet<TimeClient>();
  protected Map<TimeClient, LogicalTime> clientAdvanceRequests =
    new HashMap<TimeClient, LogicalTime>();

  public TimeKeeper(LogicalTimeFactory logicalTimeFactory)
  {
    this.logicalTimeFactory = logicalTimeFactory;

    minAdvanceRequest = logicalTimeFactory.makeFinal();
  }

  public synchronized void timeAdvanceRequest(LogicalTime time, TimeClient client)
  {
    System.out.printf("%s requesting advance to %d\n", client, time);

    if (this.time.compareTo(time) > 0)
    {
      throw new RuntimeException(String.format("%s > %s", this.time, time));
    }

    LogicalTime pendingAdvance = clientAdvanceRequests.put(client, time);
    if (pendingAdvance != null)
    {
      clientAdvanceRequests.put(client, pendingAdvance);
      throw new RuntimeException(String.format("advance pending: %s", client));
    }

    minAdvanceRequest = min(minAdvanceRequest, time);

    if (clientAdvanceRequests.size() == clients.size())
    {
      this.time = minAdvanceRequest;
      minAdvanceRequest = logicalTimeFactory.makeFinal();

      Set<TimeClient> advancingClients = new HashSet<TimeClient>();
      for (Map.Entry<TimeClient, LogicalTime> entry : clientAdvanceRequests.entrySet())
      {
        if (this.time.equals(entry.getValue()))
        {
          advancingClients.add(entry.getKey());
        }
        else
        {
          minAdvanceRequest = min(minAdvanceRequest, entry.getValue());
        }
      }
      clientAdvanceRequests.keySet().removeAll(advancingClients);

      for (TimeClient tc : advancingClients)
      {
        tc.timeAdvanceGrant(this.time);
      }
    }
  }

  protected LogicalTime min(LogicalTime lhs, LogicalTime rhs)
  {
    return lhs.compareTo(rhs) <= 0 ? lhs : rhs;
  }

  public synchronized void timeAdvanceRequestAvailable(LogicalTime time,
                                                       TimeClient client)
  {
    timeAdvanceRequest(min(time, minAdvanceRequest), client);
  }

  public void test()
  {
    TimeClient tc = new TimeClient(new Integer64TimeInterval(3000), false);
    TimeClient tc2 = new TimeClient(new Integer64TimeInterval(5000), true);
//    TimeClient tc3 = new TimeClient(35000);
    tc.start();
    tc2.start();
//    tc3.start();
    new Thread()
    {
      public void run()
      {
        int i = 0;
        while (true)
        {
          System.out.printf("[%d] %d\n", i++, time);
          try
          {
            Thread.sleep(1000);
          }
          catch (InterruptedException e)
          {
            e.printStackTrace();
          }
        }
      }
    }.start();
  }

  protected class TimeClient
    extends Thread
  {
    protected Integer64TimeInterval step;
    protected boolean available;

    protected LogicalTime time;
    protected LogicalTime timeRequested;
    protected boolean advanceGranted;

    public TimeClient(Integer64TimeInterval step, boolean available)
    {
      this.step = step;
      this.available = available;

      clients.add(this);
    }

    public synchronized void timeAdvanceGrant(LogicalTime time)
    {
      System.out.printf("%s advance granted to %d\n", this, time);
      this.time = time;
      advanceGranted = true;
      notifyAll();
    }

    public void run()
    {
      do
      {
        try
        {
          timeRequested = time.add(step);
        }
        catch (IllegalTimeArithmetic illegalTimeArithmetic)
        {
          illegalTimeArithmetic.printStackTrace();
        }
        if (available)
        {
          timeAdvanceRequestAvailable(timeRequested, this);
        }
        else
        {
          timeAdvanceRequest(timeRequested, this);
        }

        long waitUntil =
          System.currentTimeMillis() + step.interval + ((Integer64TimeInterval) time.distance(timeRequested)).interval;
        for (long waitTime = waitUntil - System.currentTimeMillis();
             waitTime > 0; waitTime = waitUntil - System.currentTimeMillis())
        {
          synchronized (this)
          {
            try
            {
              wait(waitTime);
            }
            catch (InterruptedException ie)
            {
            }
          }
        }

        synchronized (this)
        {
          while (!advanceGranted)
          {
            try
            {
              wait();
            }
            catch (InterruptedException ie)
            {
            }
          }
          advanceGranted = false;
        }
      }
      while (true);
    }
  }

  public static void main(String... args)
    throws Throwable
  {
    TimeKeeper timeKeeper = new TimeKeeper(new Integer64TimeFactory());
    timeKeeper.test();
  }
}
