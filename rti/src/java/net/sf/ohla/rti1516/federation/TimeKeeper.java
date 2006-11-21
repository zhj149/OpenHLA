package net.sf.ohla.rti1516.federation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.ohla.rti1516.federate.callbacks.TimeAdvanceGrant;
import net.sf.ohla.rti1516.federate.callbacks.TimeConstrainedEnabled;
import net.sf.ohla.rti1516.federate.callbacks.TimeRegulationEnabled;

import org.apache.mina.common.IoSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hla.rti1516.FederateHandle;
import hla.rti1516.LogicalTime;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.MobileFederateServices;

public class TimeKeeper
{
  private static final Logger log = LoggerFactory.getLogger(TimeKeeper.class);

  protected FederationExecution federationExecution;

  protected MobileFederateServices mobileFederateServices;

  protected LogicalTime time;
  protected LogicalTime minAdvanceRequest;

  protected Lock timeLock = new ReentrantLock(true);

  protected Map<FederateHandle, LogicalTimeInterval> timeRegulatingFederates =
    new HashMap<FederateHandle, LogicalTimeInterval>();
  protected Map<FederateHandle, LogicalTime> timeRegulatingFederateAdvanceRequests =
    new HashMap<FederateHandle, LogicalTime>();

  protected Set<FederateHandle> timeConstrainedFederates =
    new HashSet<FederateHandle>();
  protected Map<FederateHandle, LogicalTime> timeConstrainedFederateAdvanceRequests =
    new HashMap<FederateHandle, LogicalTime>();

  public TimeKeeper(FederationExecution federationExecution,
                    MobileFederateServices mobileFederateServices)
  {
    this.federationExecution = federationExecution;
    this.mobileFederateServices = mobileFederateServices;

    time = mobileFederateServices.timeFactory.makeInitial();
    minAdvanceRequest = mobileFederateServices.timeFactory.makeFinal();
  }

  public void enableTimeRegulation(IoSession session,
                                   FederateHandle federateHandle,
                                   LogicalTimeInterval lookahead)
  {
    timeLock.lock();
    try
    {
      timeRegulatingFederates.put(federateHandle, lookahead);

      session.write(new TimeRegulationEnabled(time));
    }
    finally
    {
      timeLock.unlock();
    }

    log.debug("time regulation enabled: {}", federateHandle);
  }

  public void disableTimeRegulation(FederateHandle federateHandle)
  {
    timeLock.lock();
    try
    {
      timeRegulatingFederates.remove(federateHandle);
      timeRegulatingFederateAdvanceRequests.remove(federateHandle);
    }
    finally
    {
      timeLock.unlock();
    }

    log.debug("time regulation disabled: {}", federateHandle);
  }

  public void enableTimeConstrained(IoSession session,
                                    FederateHandle federateHandle)
  {
    timeLock.lock();
    try
    {
      timeConstrainedFederates.add(federateHandle);

      session.write(new TimeConstrainedEnabled(time));
    }
    finally
    {
      timeLock.unlock();
    }

    log.debug("time constrained enabled: {}", federateHandle);
  }

  public void disableTimeConstrained(FederateHandle federateHandle)
  {
    timeLock.lock();
    try
    {
      timeConstrainedFederates.remove(federateHandle);
      timeConstrainedFederateAdvanceRequests.remove(federateHandle);
    }
    finally
    {
      timeLock.unlock();
    }

    log.debug("time constrained disabled: {}", federateHandle);
  }

  public void timeAdvanceRequest(FederateHandle federateHandle,
                                 LogicalTime time)
  {
    log.debug("time advance request: {} to {}", federateHandle, time);

    timeLock.lock();
    try
    {
      assert this.time.compareTo(time) >= 0;

      if (timeRegulatingFederates.containsKey(federateHandle))
      {
        LogicalTime pendingAdvance =
          timeRegulatingFederateAdvanceRequests.put(federateHandle, time);
        assert pendingAdvance == null;

        minAdvanceRequest = min(minAdvanceRequest, time);
      }

      if (timeConstrainedFederates.contains(federateHandle))
      {
        LogicalTime pendingAdvance =
          timeConstrainedFederateAdvanceRequests.put(federateHandle, time);
        assert pendingAdvance == null;
      }

      if (timeRegulatingFederateAdvanceRequests.size() ==
          timeRegulatingFederates.size())
      {
        this.time = minAdvanceRequest;

        log.debug("advancing time to: {}", this.time);

        minAdvanceRequest = mobileFederateServices.timeFactory.makeFinal();

        Map<FederateHandle, LogicalTime> advancingTimeRegulatingFederates =
          new HashMap<FederateHandle, LogicalTime>();
        for (Map.Entry<FederateHandle, LogicalTime> entry :
          timeRegulatingFederateAdvanceRequests.entrySet())
        {
          if (this.time.equals(entry.getValue()))
          {
            advancingTimeRegulatingFederates.put(entry.getKey(), this.time);
            timeRegulatingFederateAdvanceRequests.remove(entry.getKey());
          }
          else
          {
            // select the next lowest as the next minimum advance request
            //
            minAdvanceRequest = min(minAdvanceRequest, entry.getValue());
          }
        }

        for (Map.Entry<FederateHandle, LogicalTime> entry :
          timeConstrainedFederateAdvanceRequests.entrySet())
        {
          if (this.time.compareTo(entry.getValue()) >= 0)
          {
            advancingTimeRegulatingFederates.put(
              entry.getKey(), entry.getValue());
            timeConstrainedFederateAdvanceRequests.remove(entry.getKey());
          }
        }

        for (Map.Entry<FederateHandle, LogicalTime> entry :
          advancingTimeRegulatingFederates.entrySet())
        {
          federationExecution.getFederateSession(entry.getKey()).write(
            new TimeAdvanceGrant(entry.getValue()));
        }
      }
    }
    finally
    {
      timeLock.unlock();
    }
  }

  public void timeAdvanceRequestAvailable(FederateHandle federateHandle,
                                          LogicalTime time)
  {
    timeAdvanceRequest(federateHandle, min(time, minAdvanceRequest));
  }

  protected LogicalTime min(LogicalTime lhs, LogicalTime rhs)
  {
    return lhs.compareTo(rhs) <= 0 ? lhs : rhs;
  }
}
