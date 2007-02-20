package net.sf.ohla.rti1516.federation;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

  protected ReadWriteLock timeLock = new ReentrantReadWriteLock(true);

  protected Set<FederateProxy> timeRegulatingFederates =
    new HashSet<FederateProxy>();
  protected Set<FederateProxy> timeConstrainedFederates =
    new HashSet<FederateProxy>();

  public FederationExecutionTimeManager(
    FederationExecution federationExecution,
    MobileFederateServices mobileFederateServices)
  {
    this.federationExecution = federationExecution;
    this.mobileFederateServices = mobileFederateServices;

    galt = mobileFederateServices.timeFactory.makeInitial();
  }

  public ReadWriteLock getTimeLock()
  {
    return timeLock;
  }

  public LogicalTime getGALT()
  {
    return galt;
  }

  public void enableTimeRegulation(FederateProxy federateProxy,
                                   LogicalTimeInterval lookahead)
  {
    timeLock.writeLock().lock();
    try
    {
      timeRegulatingFederates.add(federateProxy);

      federateProxy.enableTimeRegulation(galt, lookahead);
    }
    catch (IllegalTimeArithmetic ita)
    {
      log.error("unable to enable time regulation", ita);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void disableTimeRegulation(FederateProxy federateProxy)
  {
    timeLock.writeLock().lock();
    try
    {
      timeRegulatingFederates.remove(federateProxy);

      federateProxy.disableTimeRegulation();
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void enableTimeConstrained(FederateProxy federateProxy)
  {
    timeLock.writeLock().lock();
    try
    {
      timeConstrainedFederates.add(federateProxy);

      federateProxy.enableTimeConstrained(galt);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void disableTimeConstrained(FederateProxy federateProxy)
  {
    timeLock.writeLock().lock();
    try
    {
      timeConstrainedFederates.remove(federateProxy);

      federateProxy.disableTimeConstrained();
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void timeAdvanceRequest(FederateProxy federateProxy, LogicalTime time)
  {
    timeAdvanceRequest(federateProxy, time, false);
  }

  public void timeAdvanceRequestAvailable(FederateProxy federateProxy,
                                          LogicalTime time)
  {
    timeAdvanceRequest(federateProxy, time, true);
  }

  protected void timeAdvanceRequest(FederateProxy federateProxy,
                                    LogicalTime time, boolean available)
  {
    timeLock.writeLock().lock();
    try
    {
      if (available)
      {
        time = min(time, galt);
      }
      else
      {
        assert galt.compareTo(time) >= 0;
      }

      federateProxy.timeAdvanceRequest(time);

      boolean galtAdvanced = false;

      if (timeRegulatingFederates.contains(federateProxy))
      {
        LogicalTime newGALT = mobileFederateServices.timeFactory.makeFinal();
        for (FederateProxy f : timeRegulatingFederates)
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
      timeLock.writeLock().unlock();
    }
  }

  protected LogicalTime min(LogicalTime lhs, LogicalTime rhs)
  {
    return lhs.compareTo(rhs) <= 0 ? lhs : rhs;
  }
}