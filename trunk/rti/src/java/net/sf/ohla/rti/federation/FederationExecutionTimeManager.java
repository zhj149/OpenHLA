/*
 * Copyright (c) 2006-2007, Michael Newcomb
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

package net.sf.ohla.rti.federation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.i18n.I18nLogger;
import net.sf.ohla.rti.i18n.LogMessages;
import net.sf.ohla.rti.messages.GetLITS;

import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.LogicalTimeInterval;
import hla.rti1516e.exceptions.IllegalTimeArithmetic;
import hla.rti1516e.exceptions.InvalidLogicalTimeInterval;
import hla.rti1516e.exceptions.RTIinternalError;

public class FederationExecutionTimeManager
{
  private final FederationExecution federationExecution;
  private final LogicalTimeFactory logicalTimeFactory;
  private final LogicalTime initialTime;
  private final LogicalTime finalTime;
  private final LogicalTimeInterval epsilon;

  /**
   * The federation-wide GALT. Used for all non-regulating federates.
   */
  private LogicalTime galt;

  private final ReadWriteLock timeLock = new ReentrantReadWriteLock(true);

  private final Set<FederateProxy> timeRegulatingFederates = new HashSet<FederateProxy>();
  private final Set<FederateProxy> timeConstrainedFederates = new HashSet<FederateProxy>();

  private final I18nLogger log;

  public FederationExecutionTimeManager(FederationExecution federationExecution, LogicalTimeFactory logicalTimeFactory)
  {
    this.federationExecution = federationExecution;
    this.logicalTimeFactory = logicalTimeFactory;

    initialTime = logicalTimeFactory.makeInitial();
    finalTime = logicalTimeFactory.makeFinal();
    epsilon = logicalTimeFactory.makeEpsilon();

    log = I18nLogger.getLogger(federationExecution.getMarker(), FederationExecutionTimeManager.class);
  }

  public LogicalTimeFactory getLogicalTimeFactory()
  {
    return logicalTimeFactory;
  }

  public ReadWriteLock getTimeLock()
  {
    return timeLock;
  }

  public LogicalTime getGALT()
  {
    return galt;
  }

  @SuppressWarnings("unchecked")
  public void enableTimeRegulation(FederateProxy federateProxy, LogicalTimeInterval lookahead)
  {
    timeLock.writeLock().lock();
    try
    {
      LogicalTime federateTime;

      if (timeRegulatingFederates.isEmpty())
      {
        // no time regulating federates to consider

        if (timeConstrainedFederates.isEmpty())
        {
          federateTime = federateProxy.getFederateTime();
        }
        else
        {
          // go through all the time constrained federates to find the maximum
          // constrained LITS

          LogicalTime maxLITS = initialTime;
          for (FederateProxy timeConstrainedFederate : timeConstrainedFederates)
          {
            // since there are no time regulating federates, no federate should
            // be in a time advancing state because any requests will be granted
            // immediately
            //
            assert timeConstrainedFederate.getAdvanceRequestTime() == null;

            LogicalTime lits = timeConstrainedFederate.getFederateTime();
            switch (timeConstrainedFederate.getAdvanceRequestType())
            {
              case NONE: // TODO: NONE might not belong here
              case TIME_ADVANCE_REQUEST:
              case NEXT_MESSAGE_REQUEST:
                lits = lits.add(epsilon);
                break;
            }
            maxLITS = max(maxLITS, lits);
          }

          if (federateProxy.getFederateTime().compareTo(maxLITS) >= 0)
          {
            // the new time regulating federate's time is >= all the time
            // constrained federates LITS
            //
            federateTime = federateProxy.getFederateTime();
          }
          else
          {
            LogicalTime lots = federateProxy.getFederateTime().add(lookahead);
            if (lots.compareTo(maxLITS) >= 0)
            {
              // the new time regulating federate's time + lookahead is >= all
              // the time constrained federates LITS
              //
              federateTime = federateProxy.getFederateTime();
            }
            else
            {
              // subtract the lookahead from the max time constrained LITS to
              // determine the time regulating federate's time
              //
              federateTime = maxLITS.subtract(lookahead);
            }
          }
        }

        timeRegulatingFederates.add(federateProxy);

        federateProxy.enableTimeRegulation(lookahead, federateTime);

        // determine the federation-wide GALT
        //
        galt = federateTime.add(lookahead);

        for (FederateProxy f : federationExecution.getFederates().values())
        {
          // since there were no time regulating federates, GALT should be
          // undefined for all federates
          //
          assert f.getGALT() == null;

          // GALT will remain undefined for this newly time regulating federate
          // but will be defined for all others
          //
          if (f != federateProxy)
          {
            f.galtAdvanced(galt);
          }
        }
      }
      else
      {
        if (federateProxy.getFederateTime().compareTo(galt) > 0)
        {
          // the new time regulating federate's time is > GALT

          assert !timeConstrainedFederates.contains(federateProxy);

          federateTime = federateProxy.getFederateTime();
        }
        else if (federateProxy.getFederateTime().equals(galt))
        {
          // the new time regulating federate's time is = GALT

          federateTime = federateProxy.getFederateTime();
        }
        else
        {
          LogicalTime lots = federateProxy.getFederateTime().add(lookahead);
          if (lots.compareTo(galt) >= 0)
          {
            // the new time regulating federate's time + lookahead is >= GALT

            federateTime = federateProxy.getFederateTime();
          }
          else
          {
            // subtract the lookahead from the GALT to determine the time
            // regulating federate's time
            //
            federateTime = galt.subtract(lookahead);
          }
        }

        timeRegulatingFederates.add(federateProxy);

        federateProxy.enableTimeRegulation(lookahead, federateTime);

        recalculateGALT(federateProxy);
      }

      federateProxy.timeRegulationEnabled();
    }
    catch (IllegalTimeArithmetic ita)
    {
      log.error(LogMessages.UNABLE_TO_ENABLE_TIME_REGULATION, ita);
    }
    catch (InvalidLogicalTimeInterval ilti)
    {
      log.error(LogMessages.UNABLE_TO_ENABLE_TIME_REGULATION, ilti);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  @SuppressWarnings("unchecked")
  public void disableTimeRegulation(FederateProxy federateProxy)
  {
    timeLock.writeLock().lock();
    try
    {
      timeRegulatingFederates.remove(federateProxy);

      if (timeRegulatingFederates.isEmpty())
      {
        // no more time regulating federates

        galt = null;

        for (FederateProxy f : federationExecution.getFederates().values())
        {
          f.galtUndefined();
        }
      }
      else if (timeRegulatingFederates.size() == 1)
      {
        FederateProxy lastTimeRegulatingFederate = timeRegulatingFederates.iterator().next();

        lastTimeRegulatingFederate.galtUndefined();

        if (lastTimeRegulatingFederate.getLOTS().compareTo(galt) > 0)
        {
          galt = lastTimeRegulatingFederate.getLOTS();

          for (FederateProxy f : federationExecution.getFederates().values())
          {
            if (f != lastTimeRegulatingFederate)
            {
              // GALT advances for everyone else
              //
              f.galtAdvanced(galt);
            }
          }
        }
      }
      else
      {
        // still time regulating federates

        // find the least LOTS of the time regulating federates
        //
        LogicalTime leastTimeRegulatingLOTS = finalTime;
        for (FederateProxy timeRegulatingFederate : timeRegulatingFederates)
        {
          leastTimeRegulatingLOTS = min(leastTimeRegulatingLOTS, timeRegulatingFederate.getLOTS());
        }

        if (leastTimeRegulatingLOTS.equals(galt))
        {
          // federation-wide GALT did not change 

          // recalculate the GALT for each time regulating federate
          //
          for (FederateProxy timeRegulatingFederate : timeRegulatingFederates)
          {
            LogicalTime newGALT = finalTime;
            for (FederateProxy timeRegulatingFederate2 : timeRegulatingFederates)
            {
              if (timeRegulatingFederate != timeRegulatingFederate2)
              {
                newGALT = min(newGALT, timeRegulatingFederate.getLOTS());
              }
            }
            if (newGALT.compareTo(timeRegulatingFederate.getGALT()) > 0)
            {
              timeRegulatingFederate.galtAdvanced(newGALT);
            }
          }
        }
        else
        {
          // federation-wide GALT advanced

          galt = leastTimeRegulatingLOTS;

          // recalculate the GALT for each time regulating federate
          //
          for (FederateProxy timeRegulatingFederate : timeRegulatingFederates)
          {
            LogicalTime newGALT = finalTime;
            for (FederateProxy timeRegulatingFederate2 : timeRegulatingFederates)
            {
              if (timeRegulatingFederate != timeRegulatingFederate2)
              {
                newGALT = min(newGALT, timeRegulatingFederate.getLOTS());
              }
            }
            if (newGALT.compareTo(timeRegulatingFederate.getGALT()) > 0)
            {
              timeRegulatingFederate.galtAdvanced(newGALT);
            }
          }

          for (FederateProxy f : federationExecution.getFederates().values())
          {
            if (!timeRegulatingFederates.contains(f))
            {
              // GALT advances for everyone else
              //
              f.galtAdvanced(galt);
            }
          }
        }
      }

      federateProxy.disableTimeRegulation();
    }
    catch (IllegalTimeArithmetic ita)
    {
      log.error(LogMessages.UNABLE_TO_DISABLE_TIME_REGULATION, ita);
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
      federateProxy.enableTimeConstrained();

      timeConstrainedFederates.add(federateProxy);
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

  public void modifyLookahead(FederateProxy federateProxy, LogicalTimeInterval lookahead)
  {
    timeLock.writeLock().lock();
    try
    {
      federateProxy.modifyLookahead(lookahead);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void timeAdvanceRequest(FederateProxy federateProxy, LogicalTime time)
  {
    timeLock.writeLock().lock();
    try
    {
      federateProxy.timeAdvanceRequest(time);

      if (timeRegulatingFederates.contains(federateProxy))
      {
        recalculateGALT(federateProxy);
      }
    }
    catch (IllegalTimeArithmetic ita)
    {
      log.error(LogMessages.UNABLE_TO_REQUEST_TIME_ADVANCE, ita);
    }
    catch (InvalidLogicalTimeInterval ilti)
    {
      log.error(LogMessages.UNABLE_TO_REQUEST_TIME_ADVANCE, ilti);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void timeAdvanceRequestAvailable(FederateProxy federateProxy, LogicalTime time)
  {
    timeLock.writeLock().lock();
    try
    {
      federateProxy.timeAdvanceRequestAvailable(time);

      if (timeRegulatingFederates.contains(federateProxy))
      {
        recalculateGALT(federateProxy);
      }
    }
    catch (IllegalTimeArithmetic ita)
    {
      log.error(LogMessages.UNABLE_TO_REQUEST_TIME_ADVANCE, ita);
    }
    catch (InvalidLogicalTimeInterval ilti)
    {
      log.error(LogMessages.UNABLE_TO_REQUEST_TIME_ADVANCE, ilti);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void nextMessageRequest(FederateProxy federateProxy, LogicalTime time)
  {
    timeLock.writeLock().lock();
    try
    {
      federateProxy.nextMessageRequest(time);

      if (timeRegulatingFederates.contains(federateProxy))
      {
        recalculateGALT(federateProxy);
      }
    }
    catch (IllegalTimeArithmetic ita)
    {
      log.error(LogMessages.UNABLE_TO_REQUEST_TIME_ADVANCE, ita);
    }
    catch (InvalidLogicalTimeInterval ilti)
    {
      log.error(LogMessages.UNABLE_TO_REQUEST_TIME_ADVANCE, ilti);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void nextMessageRequestAvailable(
    FederateProxy federateProxy, LogicalTime time)
  {
    timeLock.writeLock().lock();
    try
    {
      federateProxy.nextMessageRequestAvailable(time);

      if (timeRegulatingFederates.contains(federateProxy))
      {
        recalculateGALT(federateProxy);
      }
    }
    catch (IllegalTimeArithmetic ita)
    {
      log.error(LogMessages.UNABLE_TO_REQUEST_TIME_ADVANCE, ita);
    }
    catch (InvalidLogicalTimeInterval ilti)
    {
      log.error(LogMessages.UNABLE_TO_REQUEST_TIME_ADVANCE, ilti);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void flushQueueRequest(FederateProxy federateProxy, LogicalTime time)
  {
    timeLock.writeLock().lock();
    try
    {
      federateProxy.flushQueueRequest(time);

      if (timeRegulatingFederates.contains(federateProxy))
      {
        recalculateGALT(federateProxy);
      }
    }
    catch (IllegalTimeArithmetic ita)
    {
      log.error(LogMessages.UNABLE_TO_REQUEST_TIME_ADVANCE, ita);
    }
    catch (InvalidLogicalTimeInterval ilti)
    {
      log.error(LogMessages.UNABLE_TO_REQUEST_TIME_ADVANCE, ilti);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  @SuppressWarnings("unchecked")
  private void recalculateGALT(FederateProxy federateProxy)
    throws IllegalTimeArithmetic
  {
    if (timeRegulatingFederates.size() == 1)
    {
      galt = federateProxy.getLOTS();

      for (FederateProxy f : federationExecution.getFederates().values())
      {
        if (f != federateProxy)
        {
          f.galtAdvanced(galt);
        }
      }
    }
    else
    {
      LogicalTime potentialGALT = finalTime;

      // track any time regulating-and-constrained federates that are awaiting advances from a next message request
      //
      Map<FederateProxy, GetLITS> federateProxiesAwaitingNextMessageRequestTimeAdvanceGrants = null;

      // calculate the potential federation-wide GALT
      //
      for (FederateProxy timeRegulatingFederate : timeRegulatingFederates)
      {
        potentialGALT = min(potentialGALT, timeRegulatingFederate.getLOTS());

        if (timeRegulatingFederate.isTimeConstrainedEnabled() && timeRegulatingFederate.getAdvanceRequestTime() != null)
        {
          // this federate is constrained and awaiting a time advance grant

          switch (timeRegulatingFederate.getAdvanceRequestType())
          {
            case NEXT_MESSAGE_REQUEST:
            case NEXT_MESSAGE_REQUEST_AVAILABLE:
              if (federateProxiesAwaitingNextMessageRequestTimeAdvanceGrants == null)
              {
                federateProxiesAwaitingNextMessageRequestTimeAdvanceGrants = new HashMap<FederateProxy, GetLITS>();
              }
              federateProxiesAwaitingNextMessageRequestTimeAdvanceGrants.put(timeRegulatingFederate, null);
              break;
          }
        }
      }

      LogicalTime newGALT;
      if (federateProxiesAwaitingNextMessageRequestTimeAdvanceGrants == null)
      {
        newGALT = potentialGALT;
      }
      else if (federateProxiesAwaitingNextMessageRequestTimeAdvanceGrants.size() == 1)
      {
        FederateProxy federateProxyAwaitingNextMessageRequestTimeAdvanceGrant =
          federateProxiesAwaitingNextMessageRequestTimeAdvanceGrants.keySet().iterator().next();

        GetLITS getLITS = new GetLITS(potentialGALT);
        federateProxyAwaitingNextMessageRequestTimeAdvanceGrant.getFederateChannel().write(getLITS);

        try
        {
          LogicalTime lits = getLITS.getResponse().getLITS();
          if (lits == null)
          {
            newGALT = potentialGALT;
          }
          else
          {
            federateProxyAwaitingNextMessageRequestTimeAdvanceGrant.adjustNextMessageRequestAdvanceRequestTime(lits);

            newGALT = federateProxyAwaitingNextMessageRequestTimeAdvanceGrant.getLOTS();
          }
        }
        catch (RTIinternalError rtiie)
        {
          // TODO: handle appropriately
          //
          log.error(null, rtiie);

          newGALT = potentialGALT;
        }
      }
      else
      {
        assert federateProxiesAwaitingNextMessageRequestTimeAdvanceGrants.size() > 1;

        // send all the requests
        //
        for (Map.Entry<FederateProxy, GetLITS> entry :
          federateProxiesAwaitingNextMessageRequestTimeAdvanceGrants.entrySet())
        {
          GetLITS getLITS = new GetLITS(potentialGALT);
          entry.setValue(getLITS);

          entry.getKey().getFederateChannel().write(getLITS);
        }

        Map<FederateProxy, LogicalTime> federateProxyLITS = null;

        // find the smallest LITS
        //
        LogicalTime smallestLITS = finalTime;
        for (Map.Entry<FederateProxy, GetLITS> entry :
          federateProxiesAwaitingNextMessageRequestTimeAdvanceGrants.entrySet())
        {
          LogicalTime lits;
          try
          {
            lits = entry.getValue().getResponse().getLITS();
          }
          catch (RTIinternalError rtiie)
          {
            // TODO: handle appropriately
            //
            log.error(null, rtiie);

            lits = null;
          }

          if (lits != null)
          {
            smallestLITS = min(smallestLITS, lits);

            if (federateProxyLITS == null)
            {
              federateProxyLITS = new HashMap<FederateProxy, LogicalTime>();
            }
            federateProxyLITS.put(federateProxy, lits);
          }
        }

        if (smallestLITS.compareTo(potentialGALT) < 0)
        {
          assert federateProxyLITS != null;

          // at least one of the federates had a LITS < the potential GALT

          newGALT = finalTime;
          for (Map.Entry<FederateProxy, LogicalTime> entry : federateProxyLITS.entrySet())
          {
            if (smallestLITS.equals(entry.getValue()))
            {
              entry.getKey().adjustNextMessageRequestAdvanceRequestTime(entry.getValue());

              newGALT = min(newGALT, entry.getKey().getLOTS());
            }
          }
        }
        else
        {
          newGALT = potentialGALT;
        }
      }

      for (FederateProxy timeRegulatingFederate : timeRegulatingFederates)
      {
        // recalculate the GALT for each time regulating federate
        //
        LogicalTime newLocalGALT = finalTime;
        for (FederateProxy timeRegulatingFederate2 : timeRegulatingFederates)
        {
          if (timeRegulatingFederate != timeRegulatingFederate2)
          {
            newLocalGALT = min(newLocalGALT, timeRegulatingFederate2.getLOTS());
          }
        }

        if (timeRegulatingFederate.getGALT() == null || newLocalGALT.compareTo(timeRegulatingFederate.getGALT()) > 0)
        {
          timeRegulatingFederate.galtAdvanced(newLocalGALT);
        }
      }

      if (newGALT.compareTo(galt) > 0)
      {
        galt = newGALT;

        // notify the non time regulating federates that GALT has advanced
        //
        for (FederateProxy f : federationExecution.getFederates().values())
        {
          if (!timeRegulatingFederates.contains(f))
          {
            f.galtAdvanced(galt);
          }
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private LogicalTime min(LogicalTime lhs, LogicalTime rhs)
  {
    return lhs.compareTo(rhs) <= 0 ? lhs : rhs;
  }

  @SuppressWarnings("unchecked")
  private LogicalTime max(LogicalTime lhs, LogicalTime rhs)
  {
    return lhs.compareTo(rhs) >= 0 ? lhs : rhs;
  }
}
