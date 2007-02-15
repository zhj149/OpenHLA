package net.sf.ohla.rti1516.federation;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hla.rti1516.IllegalTimeArithmetic;
import hla.rti1516.LogicalTime;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.MobileFederateServices;

public class FederationExecutionTimeManager
{
  private static final Logger log = LoggerFactory.getLogger(
    FederationExecutionTimeManager.class);

  protected FederationExecution federationExecution;

  protected MobileFederateServices mobileFederateServices;

  protected LogicalTime galt;

  protected Lock timeLock = new ReentrantLock(true);

  protected Set<FederateProxy> timeRegulatingFederateProxies = new HashSet<FederateProxy>();
  protected Set<FederateProxy> timeConstrainedFederateProxies = new HashSet<FederateProxy>();

  public FederationExecutionTimeManager(FederationExecution federationExecution,
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

  public void enableTimeRegulation(FederateProxy federateProxy,
                                   LogicalTimeInterval lookahead)
  {
    timeLock.lock();
    try
    {
      timeRegulatingFederateProxies.add(federateProxy);

      federateProxy.enableTimeRegulation(galt, lookahead);
    }
    catch (IllegalTimeArithmetic ita)
    {
      log.error("unable to enable time regulation", ita);
    }
    finally
    {
      timeLock.unlock();
    }
  }

  public void disableTimeRegulation(FederateProxy federateProxy)
  {
    timeLock.lock();
    try
    {
      timeRegulatingFederateProxies.remove(federateProxy);

      federateProxy.disableTimeRegulation();
    }
    finally
    {
      timeLock.unlock();
    }
  }

  public void enableTimeConstrained(FederateProxy federateProxy)
  {
    timeLock.lock();
    try
    {
      timeConstrainedFederateProxies.add(federateProxy);

      federateProxy.enableTimeConstrained(galt);
    }
    finally
    {
      timeLock.unlock();
    }
  }

  public void disableTimeConstrained(FederateProxy federateProxy)
  {
    timeLock.lock();
    try
    {
      timeConstrainedFederateProxies.remove(federateProxy);

      federateProxy.disableTimeConstrained();
    }
    finally
    {
      timeLock.unlock();
    }
  }

  public void timeAdvanceRequest(FederateProxy federateProxy, LogicalTime time)
  {
    timeLock.lock();
    try
    {
      assert galt.compareTo(time) >= 0;

      federateProxy.timeAdvanceRequest(time);

      boolean galtAdvanced = false;

      if (timeRegulatingFederateProxies.contains(federateProxy))
      {
        LogicalTime newGALT = mobileFederateServices.timeFactory.makeFinal();
        for (FederateProxy f : timeRegulatingFederateProxies)
        {
          newGALT = min(newGALT, f.getLITS());
        }

        galtAdvanced = galt.compareTo(newGALT) < 0;
        if (galtAdvanced)
        {
          galt = newGALT;
        }
      }

      if (galtAdvanced)
      {
        federationExecution.getFederatesLock().lock();
        try
        {
          for (FederateProxy f : federationExecution.getFederates().values())
          {
            f.galtAdvanced(galt);
          }
        }
        finally
        {
          federationExecution.getFederatesLock().unlock();
        }
      }
    }
    catch (IllegalTimeArithmetic ita)
    {
      log.error("unable to request time advance", ita);
    }
    finally
    {
      timeLock.unlock();
    }
  }

  public void timeAdvanceRequestAvailable(FederateProxy federateProxy, LogicalTime time)
  {
    timeAdvanceRequest(federateProxy, min(time, galt));
  }

  protected LogicalTime min(LogicalTime lhs, LogicalTime rhs)
  {
    return lhs.compareTo(rhs) <= 0 ? lhs : rhs;
  }
}
