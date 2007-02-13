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

  protected Set<Federate> timeRegulatingFederates = new HashSet<Federate>();
  protected Set<Federate> timeConstrainedFederates = new HashSet<Federate>();

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

  public void enableTimeRegulation(Federate federate,
                                   LogicalTimeInterval lookahead)
  {
    timeLock.lock();
    try
    {
      timeRegulatingFederates.add(federate);

      federate.enableTimeRegulation(galt, lookahead);
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

  public void disableTimeRegulation(Federate federate)
  {
    timeLock.lock();
    try
    {
      timeRegulatingFederates.remove(federate);

      federate.disableTimeRegulation();
    }
    finally
    {
      timeLock.unlock();
    }
  }

  public void enableTimeConstrained(Federate federate)
  {
    timeLock.lock();
    try
    {
      timeConstrainedFederates.add(federate);

      federate.enableTimeConstrained(galt);
    }
    finally
    {
      timeLock.unlock();
    }
  }

  public void disableTimeConstrained(Federate federate)
  {
    timeLock.lock();
    try
    {
      timeConstrainedFederates.remove(federate);

      federate.disableTimeConstrained();
    }
    finally
    {
      timeLock.unlock();
    }
  }

  public void timeAdvanceRequest(Federate federate, LogicalTime time)
  {
    timeLock.lock();
    try
    {
      assert galt.compareTo(time) >= 0;

      federate.timeAdvanceRequest(time);

      boolean galtAdvanced = false;

      if (timeRegulatingFederates.contains(federate))
      {
        LogicalTime newGALT = mobileFederateServices.timeFactory.makeFinal();
        for (Federate f : timeRegulatingFederates)
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
          for (Federate f : federationExecution.getFederates().values())
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

  public void timeAdvanceRequestAvailable(Federate federate, LogicalTime time)
  {
    timeAdvanceRequest(federate, min(time, galt));
  }

  protected LogicalTime min(LogicalTime lhs, LogicalTime rhs)
  {
    return lhs.compareTo(rhs) <= 0 ? lhs : rhs;
  }
}
