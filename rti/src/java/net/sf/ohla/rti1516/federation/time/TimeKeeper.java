package net.sf.ohla.rti1516.federation.time;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.ohla.rti1516.messages.callbacks.TimeAdvanceGrant;
import net.sf.ohla.rti1516.messages.callbacks.TimeConstrainedEnabled;
import net.sf.ohla.rti1516.messages.callbacks.TimeRegulationEnabled;
import net.sf.ohla.rti1516.federation.Federate;
import net.sf.ohla.rti1516.federation.FederationExecution;
import net.sf.ohla.rti1516.messages.GALTAdvanced;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hla.rti1516.IllegalTimeArithmetic;
import hla.rti1516.LogicalTime;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.MobileFederateServices;

public class TimeKeeper
{
  private static final Logger log = LoggerFactory.getLogger(TimeKeeper.class);

  protected FederationExecution federationExecution;

  protected MobileFederateServices mobileFederateServices;

  protected LogicalTime galt;

  protected Lock timeLock = new ReentrantLock(true);

  protected Map<Federate, TimeRegulatingFederate> timeRegulatingFederates =
    new HashMap<Federate, TimeRegulatingFederate>();

  protected Map<Federate, TimeConstrainedFederate> timeConstrainedFederates =
    new HashMap<Federate, TimeConstrainedFederate>();

  public TimeKeeper(FederationExecution federationExecution,
                    MobileFederateServices mobileFederateServices)
  {
    this.federationExecution = federationExecution;
    this.mobileFederateServices = mobileFederateServices;

    galt = mobileFederateServices.timeFactory.makeInitial();
  }

  public LogicalTime getGALT()
  {
    return galt;
  }

  public void enableTimeRegulation(Federate federate,
                                   LogicalTimeInterval lookahead)
  {
    timeLock.lock();
    try
    {
      timeRegulatingFederates.put(
        federate, new TimeRegulatingFederate(federate, galt, lookahead));

      federate.getSession().write(new TimeRegulationEnabled(galt));
    }
    catch (IllegalTimeArithmetic ita)
    {
      log.error("unable to request time advance", ita);
    }
    finally
    {
      timeLock.unlock();
    }

    log.debug("time regulation enabled: {}", federate);
  }

  public void disableTimeRegulation(Federate federate)
  {
    timeLock.lock();
    try
    {
      timeRegulatingFederates.remove(federate);
    }
    finally
    {
      timeLock.unlock();
    }

    log.debug("time regulation disabled: {}", federate);
  }

  public void enableTimeConstrained(Federate federate)
  {
    timeLock.lock();
    try
    {
      timeConstrainedFederates.put(
        federate, new TimeConstrainedFederate(federate, galt));

      federate.getSession().write(new TimeConstrainedEnabled(galt));
    }
    finally
    {
      timeLock.unlock();
    }

    log.debug("time constrained enabled: {}", federate);
  }

  public void disableTimeConstrained(Federate federate)
  {
    timeLock.lock();
    try
    {
      timeConstrainedFederates.remove(federate);
    }
    finally
    {
      timeLock.unlock();
    }

    log.debug("time constrained disabled: {}", federate);
  }

  public void timeAdvanceRequest(Federate federate, LogicalTime time)
  {
    log.debug("time advance request: {} to {}", federate, time);

    timeLock.lock();
    try
    {
      assert galt.compareTo(time) >= 0;

      Map<Federate, LogicalTime> advancingFederates =
        new HashMap<Federate, LogicalTime>();

      TimeRegulatingFederate timeRegulatingFederate =
        timeRegulatingFederates.get(federate);
      TimeConstrainedFederate timeConstrainedFederate =
        timeConstrainedFederates.get(federate);

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
              trf.getFederate(), trf.getFederateTime());
          }
        }
      }

      if (galtAdvanced)
      {
        GALTAdvanced message = new GALTAdvanced(galt);

        federationExecution.getFederatesLock().lock();
        try
        {
          for (Federate f : federationExecution.getFederates().values())
          {
            f.galtAdvanced(message);
          }
        }
        finally
        {
          federationExecution.getFederatesLock().unlock();
        }
      }

      if (timeConstrainedFederate != null)
      {
        timeConstrainedFederate.timeAdvanceRequest(time);

        for (TimeConstrainedFederate tcf : timeConstrainedFederates.values())
        {
          if (tcf.galtAdvanced(galt))
          {
            advancingFederates.put(
              tcf.getFederate(), tcf.getFederateTime());
          }
        }
      }
      else if (!advancingFederates.containsKey(federate))
      {
        assert timeRegulatingFederate != null;

        timeRegulatingFederate.timeAdvanceGrant();

        advancingFederates.put(
          timeRegulatingFederate.getFederate(),
          timeRegulatingFederate.getFederateTime());
      }

      log.debug("advancing federates: {}", advancingFederates);

      for (Map.Entry<Federate, LogicalTime> entry :
        advancingFederates.entrySet())
      {
        entry.getKey().getSession().write(
          new TimeAdvanceGrant(entry.getValue()));
      }
    }
    finally
    {
      timeLock.unlock();
    }
  }

  public void timeAdvanceRequestAvailable(Federate federate, LogicalTime time)
  {
    timeAdvanceRequest(federate, min(time, galt));
  }

  protected LogicalTime min(LogicalTime lhs, LogicalTime rhs)
  {
    return lhs.compareTo(rhs) <= 0 ? lhs : rhs;
  }
}
