package net.sf.ohla.rti1516.federation.time;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.ohla.rti1516.federate.callbacks.TimeAdvanceGrant;
import net.sf.ohla.rti1516.federate.callbacks.TimeConstrainedEnabled;
import net.sf.ohla.rti1516.federate.callbacks.TimeRegulationEnabled;
import net.sf.ohla.rti1516.federation.FederationExecution;
import net.sf.ohla.rti1516.messages.GALTAdvanced;

import org.apache.mina.common.IoSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hla.rti1516.FederateHandle;
import hla.rti1516.LogicalTime;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.MobileFederateServices;
import hla.rti1516.IllegalTimeArithmetic;

public class TimeKeeper
{
  private static final Logger log = LoggerFactory.getLogger(TimeKeeper.class);

  protected FederationExecution federationExecution;

  protected MobileFederateServices mobileFederateServices;

  protected LogicalTime galt;

  protected Lock timeLock = new ReentrantLock(true);

  protected Map<FederateHandle, TimeRegulatingFederate> timeRegulatingFederates =
    new HashMap<FederateHandle, TimeRegulatingFederate>();

  protected Map<FederateHandle, TimeConstrainedFederate> timeConstrainedFederates =
    new HashMap<FederateHandle, TimeConstrainedFederate>();

  public TimeKeeper(FederationExecution federationExecution,
                    MobileFederateServices mobileFederateServices)
  {
    this.federationExecution = federationExecution;
    this.mobileFederateServices = mobileFederateServices;

    galt = mobileFederateServices.timeFactory.makeInitial();
  }

  public void enableTimeRegulation(IoSession session,
                                   FederateHandle federateHandle,
                                   LogicalTimeInterval lookahead)
  {
    timeLock.lock();
    try
    {
      timeRegulatingFederates.put(federateHandle, new TimeRegulatingFederate(
        federateHandle, galt, lookahead));

      session.write(new TimeRegulationEnabled(galt));
    }
    catch (IllegalTimeArithmetic ita)
    {
      log.error("unable to request time advance", ita);
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
      timeConstrainedFederates.put(
        federateHandle, new TimeConstrainedFederate(federateHandle, galt));

      session.write(new TimeConstrainedEnabled(galt));
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
      assert galt.compareTo(time) >= 0;

      Map<FederateHandle, LogicalTime> advancingFederates =
        new HashMap<FederateHandle, LogicalTime>();

      TimeRegulatingFederate timeRegulatingFederate =
        timeRegulatingFederates.get(federateHandle);
      TimeConstrainedFederate timeConstrainedFederate =
        timeConstrainedFederates.get(federateHandle);

      boolean galtAdvanced = false;

      if (timeRegulatingFederate != null)
      {
        try
        {
          timeRegulatingFederate.timeAdvanceRequest(time);
        }
        catch (IllegalTimeArithmetic ita)
        {
          log.error("unable to request time advance", ita);
        }

        LogicalTime newGALT = mobileFederateServices.timeFactory.makeFinal();
        for (TimeRegulatingFederate trf : timeRegulatingFederates.values())
        {
          newGALT = min(newGALT, trf.getLITS());
        }

        galtAdvanced = galt.compareTo(newGALT) < 0;
        if (galtAdvanced)
        {
          galt = newGALT;
        }

        for (TimeRegulatingFederate trf : timeRegulatingFederates.values())
        {
          if (trf.timeAdvanceGrant(galt))
          {
            advancingFederates.put(
              trf.getFederateHandle(), trf.getFederateTime());
          }
        }
      }

      if (galtAdvanced)
      {
        federationExecution.send(new GALTAdvanced(galt));
      }

      if (timeConstrainedFederate != null)
      {
        timeConstrainedFederate.timeAdvanceRequest(time);

        for (TimeConstrainedFederate tcf : timeConstrainedFederates.values())
        {
          if (tcf.galtAdvanced(galt))
          {
            advancingFederates.put(
              tcf.getFederateHandle(), tcf.getFederateTime());
          }
        }
      }
      else if (!advancingFederates.containsKey(federateHandle))
      {
        assert timeRegulatingFederate != null;

        timeRegulatingFederate.timeAdvanceGrant();

        advancingFederates.put(
          timeRegulatingFederate.getFederateHandle(),
          timeRegulatingFederate.getFederateTime());
      }

      log.debug("advancing federates: {}", advancingFederates);

      for (Map.Entry<FederateHandle, LogicalTime> entry :
        advancingFederates.entrySet())
      {
        federationExecution.getFederateSession(entry.getKey()).write(
          new TimeAdvanceGrant(entry.getValue()));
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
    timeAdvanceRequest(federateHandle, min(time, galt));
  }

  protected LogicalTime min(LogicalTime lhs, LogicalTime rhs)
  {
    return lhs.compareTo(rhs) <= 0 ? lhs : rhs;
  }
}
