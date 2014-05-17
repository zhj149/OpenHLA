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

import java.io.IOException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.util.FederateHandles;
import net.sf.ohla.rti.util.LogicalTimes;
import net.sf.ohla.rti.i18n.I18nLogger;
import net.sf.ohla.rti.i18n.LogMessages;
import net.sf.ohla.rti.messages.EnableTimeRegulation;
import net.sf.ohla.rti.messages.FlushQueueRequest;
import net.sf.ohla.rti.messages.ModifyLookahead;
import net.sf.ohla.rti.messages.NextMessageRequest;
import net.sf.ohla.rti.messages.NextMessageRequestAvailable;
import net.sf.ohla.rti.messages.QueryGALT;
import net.sf.ohla.rti.messages.QueryGALTResponse;
import net.sf.ohla.rti.messages.QueryLITS;
import net.sf.ohla.rti.messages.QueryLITSResponse;
import net.sf.ohla.rti.messages.TimeAdvanceRequest;
import net.sf.ohla.rti.messages.TimeAdvanceRequestAvailable;
import net.sf.ohla.rti.proto.FederationExecutionSaveProtos.FederationExecutionState.FederationExecutionTimeManagerState;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.LogicalTimeInterval;
import hla.rti1516e.exceptions.IllegalTimeArithmetic;
import hla.rti1516e.exceptions.InvalidLogicalTimeInterval;

public class FederationExecutionTimeManager
{
  private final FederationExecution federationExecution;
  private final LogicalTimeFactory logicalTimeFactory;
  private final LogicalTime initialTime;
  private final LogicalTime finalTime;
  private final LogicalTimeInterval epsilon;

  private final ReentrantReadWriteLock timeLock = new ReentrantReadWriteLock(true);

  private final Set<FederateHandle> timeRegulatingFederates = new HashSet<>();
  private final Set<FederateHandle> timeConstrainedFederates = new HashSet<>();

  private final I18nLogger logger;

  /**
   * The federation-wide GALT. Used for all non-regulating federates.
   */
  private LogicalTime galt;

  public FederationExecutionTimeManager(FederationExecution federationExecution, LogicalTimeFactory logicalTimeFactory)
  {
    this.federationExecution = federationExecution;
    this.logicalTimeFactory = logicalTimeFactory;

    initialTime = logicalTimeFactory.makeInitial();
    finalTime = logicalTimeFactory.makeFinal();
    epsilon = logicalTimeFactory.makeEpsilon();

    logger = I18nLogger.getLogger(federationExecution.getMarker(), FederationExecutionTimeManager.class);
  }

  public LogicalTimeFactory getLogicalTimeFactory()
  {
    return logicalTimeFactory;
  }

  public ReentrantReadWriteLock getTimeLock()
  {
    return timeLock;
  }

  public LogicalTime getGALT()
  {
    return galt;
  }

  @SuppressWarnings("unchecked")
  public void enableTimeRegulation(FederateProxy federateProxy, EnableTimeRegulation enableTimeRegulation)
  {
    timeLock.writeLock().lock();
    try
    {
      LogicalTimeInterval lookahead = enableTimeRegulation.getLookahead();

      LogicalTime federateTime;

      if (timeConstrainedFederates.isEmpty())
      {
        federateTime = federateProxy.getFederateTime();
      }
      else
      {
        // go through all the time constrained federates to find the maximum constrained LITS

        LogicalTime maxLITS = initialTime;
        for (FederateHandle timeConstrainedFederateHandle : timeConstrainedFederates)
        {
          FederateProxy timeConstrainedFederate = federationExecution.getFederate(timeConstrainedFederateHandle);

          // since there are no time regulating federates, no federate should be in a time advancing state because any
          // requests will be granted immediately
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
          // the new time regulating federate's time is >= all the time constrained federates LITS
          //
          federateTime = federateProxy.getFederateTime();
        }
        else
        {
          LogicalTime lots = federateProxy.getFederateTime().add(lookahead);
          if (lots.compareTo(maxLITS) >= 0)
          {
            // the new time regulating federate's time + lookahead is >= all the time constrained federates LITS
            //
            federateTime = federateProxy.getFederateTime();
          }
          else
          {
            // subtract the lookahead from the max time constrained LITS to determine the time regulating
            // federate's time
            //
            federateTime = maxLITS.subtract(lookahead);
          }
        }
      }

      timeRegulatingFederates.add(federateProxy.getFederateHandle());

      federateProxy.enableTimeRegulation(lookahead, federateTime);

      recalculateGALT(federateProxy);
    }
    catch (IllegalTimeArithmetic | InvalidLogicalTimeInterval e)
    {
      logger.error(LogMessages.UNABLE_TO_ENABLE_TIME_REGULATION, e);
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
      timeRegulatingFederates.remove(federateProxy.getFederateHandle());

      if (timeRegulatingFederates.isEmpty())
      {
        // no more time regulating federates

        galt = null;

        logger.debug(LogMessages.GALT_UNDEFINED);

        federationExecution.galtUndefined();

        for (FederateProxy f : federationExecution.getFederates().values())
        {
          f.galtUndefined();
        }
      }
      else if (timeRegulatingFederates.size() == 1)
      {
        FederateProxy lastTimeRegulatingFederate = federationExecution.getFederate(
          timeRegulatingFederates.iterator().next());

        if (lastTimeRegulatingFederate.getLOTS().compareTo(galt) > 0)
        {
          LogicalTime oldGALT = galt;
          galt = lastTimeRegulatingFederate.getLOTS();

          logger.debug(LogMessages.GALT_UPDATED, oldGALT, galt);

          for (FederateProxy f : federationExecution.getFederates().values())
          {
            if (f != lastTimeRegulatingFederate)
            {
              // GALT advances for everyone else
              //
              f.galtUpdated(galt);
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
        for (FederateHandle timeRegulatingFederateHandle : timeRegulatingFederates)
        {
          leastTimeRegulatingLOTS = min(
            leastTimeRegulatingLOTS, federationExecution.getFederate(timeRegulatingFederateHandle).getLOTS());
        }

        if (leastTimeRegulatingLOTS.equals(galt))
        {
          // federation-wide GALT did not change 

          // recalculate the GALT for each time regulating federate
          //
          for (FederateHandle timeRegulatingFederateHandle : timeRegulatingFederates)
          {
            FederateProxy timeRegulatingFederate = federationExecution.getFederate(timeRegulatingFederateHandle);

            LogicalTime newGALT = finalTime;
            for (FederateHandle timeRegulatingFederateHandle2 : timeRegulatingFederates)
            {
              FederateProxy timeRegulatingFederate2 = federationExecution.getFederate(timeRegulatingFederateHandle2);

              if (timeRegulatingFederate != timeRegulatingFederate2)
              {
                newGALT = min(newGALT, timeRegulatingFederate.getLOTS());
              }
            }

            timeRegulatingFederate.galtUpdated(newGALT);
          }
        }
        else
        {
          // federation-wide GALT advanced

          LogicalTime oldGALT = galt;
          galt = leastTimeRegulatingLOTS;

          logger.debug(LogMessages.GALT_UPDATED, oldGALT, galt);

          // recalculate the GALT for each time regulating federate
          //
          for (FederateHandle timeRegulatingFederateHandle : timeRegulatingFederates)
          {
            FederateProxy timeRegulatingFederate = federationExecution.getFederate(timeRegulatingFederateHandle);

            LogicalTime newGALT = finalTime;
            for (FederateHandle timeRegulatingFederateHandle2 : timeRegulatingFederates)
            {
              FederateProxy timeRegulatingFederate2 = federationExecution.getFederate(timeRegulatingFederateHandle2);

              if (timeRegulatingFederate != timeRegulatingFederate2)
              {
                newGALT = min(newGALT, timeRegulatingFederate.getLOTS());
              }
            }

            timeRegulatingFederate.galtUpdated(newGALT);
          }

          for (FederateProxy f : federationExecution.getFederates().values())
          {
            if (!f.isTimeRegulationEnabled())
            {
              // GALT advances for everyone else
              //
              f.galtUpdated(galt);
            }
          }
        }
      }

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
      federateProxy.enableTimeConstrained();

      timeConstrainedFederates.add(federateProxy.getFederateHandle());
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
      timeConstrainedFederates.remove(federateProxy.getFederateHandle());

      federateProxy.disableTimeConstrained();
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void queryGALT(FederateProxy federateProxy, QueryGALT queryGALT)
  {
    timeLock.readLock().lock();
    try
    {
      federateProxy.getFederateChannel().write(
        new QueryGALTResponse(queryGALT.getRequestId(), federateProxy.getGALT()));
    }
    finally
    {
      timeLock.readLock().unlock();
    }
  }

  public void queryLITS(FederateProxy federateProxy, QueryLITS queryLITS)
  {
    timeLock.readLock().lock();
    try
    {
      federateProxy.getFederateChannel().write(
        new QueryLITSResponse(queryLITS.getRequestId(), federateProxy.getLITSOrGALT()));
    }
    finally
    {
      timeLock.readLock().unlock();
    }
  }

  public void modifyLookahead(FederateProxy federateProxy, ModifyLookahead modifyLookahead)
  {
    timeLock.writeLock().lock();
    try
    {
      LogicalTimeInterval lookahead = modifyLookahead.getLookahead();

      federateProxy.modifyLookahead(lookahead);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void timeAdvanceRequest(FederateProxy federateProxy, TimeAdvanceRequest timeAdvanceRequest)
  {
    timeLock.writeLock().lock();
    try
    {
      LogicalTime time = timeAdvanceRequest.getTime();

      federateProxy.timeAdvanceRequest(time);

      if (federateProxy.isTimeRegulationEnabled())
      {
        recalculateGALT(federateProxy);
      }
    }
    catch (IllegalTimeArithmetic | InvalidLogicalTimeInterval e)
    {
      logger.error(LogMessages.UNABLE_TO_REQUEST_TIME_ADVANCE, e);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void timeAdvanceRequestAvailable(
    FederateProxy federateProxy, TimeAdvanceRequestAvailable timeAdvanceRequestAvailable)
  {
    timeLock.writeLock().lock();
    try
    {
      LogicalTime time = timeAdvanceRequestAvailable.getTime();

      federateProxy.timeAdvanceRequestAvailable(time);

      if (federateProxy.isTimeRegulationEnabled())
      {
        recalculateGALT(federateProxy);
      }
    }
    catch (IllegalTimeArithmetic | InvalidLogicalTimeInterval e)
    {
      logger.error(LogMessages.UNABLE_TO_REQUEST_TIME_ADVANCE, e);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void nextMessageRequest(FederateProxy federateProxy, NextMessageRequest nextMessageRequest)
  {
    timeLock.writeLock().lock();
    try
    {
      LogicalTime time = nextMessageRequest.getTime();

      federateProxy.nextMessageRequest(time);

      if (federateProxy.isTimeRegulationEnabled())
      {
        recalculateGALT(federateProxy);
      }
    }
    catch (IllegalTimeArithmetic | InvalidLogicalTimeInterval e)
    {
      logger.error(LogMessages.UNABLE_TO_REQUEST_TIME_ADVANCE, e);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void nextMessageRequestAvailable(
    FederateProxy federateProxy, NextMessageRequestAvailable nextMessageRequestAvailable)
  {
    timeLock.writeLock().lock();
    try
    {
      LogicalTime time = nextMessageRequestAvailable.getTime();

      federateProxy.nextMessageRequestAvailable(time);

      if (federateProxy.isTimeRegulationEnabled())
      {
        recalculateGALT(federateProxy);
      }
    }
    catch (IllegalTimeArithmetic | InvalidLogicalTimeInterval e)
    {
      logger.error(LogMessages.UNABLE_TO_REQUEST_TIME_ADVANCE, e);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void flushQueueRequest(FederateProxy federateProxy, FlushQueueRequest flushQueueRequest)
  {
    timeLock.writeLock().lock();
    try
    {
      LogicalTime time = flushQueueRequest.getTime();

      federateProxy.flushQueueRequest(time);

      if (federateProxy.isTimeRegulationEnabled())
      {
        recalculateGALT(federateProxy);
      }
    }
    catch (IllegalTimeArithmetic | InvalidLogicalTimeInterval e)
    {
      logger.error(LogMessages.UNABLE_TO_REQUEST_TIME_ADVANCE, e);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void saveState(CodedOutputStream out)
    throws IOException
  {
    FederationExecutionTimeManagerState.Builder timeManagerState = FederationExecutionTimeManagerState.newBuilder();

    timeManagerState.addAllTimeRegulatingFederateHandles(FederateHandles.convertToProto(timeRegulatingFederates));
    timeManagerState.addAllTimeConstrainedFederateHandles(FederateHandles.convertToProto(timeConstrainedFederates));

    if (galt != null)
    {
      timeManagerState.setGalt(LogicalTimes.convert(galt));
    }

    out.writeMessageNoTag(timeManagerState.build());
  }

  public void restoreState(CodedInputStream in)
    throws IOException
  {
    FederationExecutionTimeManagerState timeManagerState =
      in.readMessage(FederationExecutionTimeManagerState.PARSER, null);

    timeRegulatingFederates.clear();
    timeConstrainedFederates.clear();

    timeRegulatingFederates.addAll(
      FederateHandles.convertFromProto(timeManagerState.getTimeRegulatingFederateHandlesList()));
    timeConstrainedFederates.addAll(
      FederateHandles.convertFromProto(timeManagerState.getTimeConstrainedFederateHandlesList()));

    if (timeManagerState.hasGalt())
    {
      galt = LogicalTimes.convert(logicalTimeFactory, timeManagerState.getGalt());
    }
  }

  @SuppressWarnings("unchecked")
  private void recalculateGALT(FederateProxy federateProxy)
    throws IllegalTimeArithmetic
  {
    assert timeRegulatingFederates.size() > 0;

    if (timeRegulatingFederates.size() == 1)
    {
      LogicalTime oldGALT = galt;
      galt = federateProxy.getLOTS();

      logger.debug(LogMessages.GALT_UPDATED, oldGALT, galt);

      federationExecution.galtUpdated(galt);

      for (FederateProxy f : federationExecution.getFederates().values())
      {
        if (f != federateProxy)
        {
          f.galtUpdated(galt);
        }
      }
    }
    else
    {
      LogicalTime potentialGALT = finalTime;

      // track any time regulating-and-constrained federates that are awaiting advances from a next message request
      //
      Map<FederateProxy, LogicalTime> timeRegulatingAndConstrainedFederatesAwaitingNextMessageRequestTimeAdvanceGrant =
        null;

      // track the smallest LITS of any time regulating-and-constrained federates that are awaiting advances from a
      // next message request
      //
      LogicalTime smallestLITS = null;

      // calculate the potential federation-wide GALT
      //
      for (FederateHandle timeRegulatingFederateHandle : timeRegulatingFederates)
      {
        FederateProxy timeRegulatingFederate = federationExecution.getFederate(timeRegulatingFederateHandle);

        potentialGALT = min(potentialGALT, timeRegulatingFederate.getLOTS());

        if (timeRegulatingFederate.isTimeConstrainedEnabled() && timeRegulatingFederate.getAdvanceRequestTime() != null)
        {
          // this federate is constrained and awaiting a time advance grant

          switch (timeRegulatingFederate.getAdvanceRequestType())
          {
            case NEXT_MESSAGE_REQUEST:
            case NEXT_MESSAGE_REQUEST_AVAILABLE:
              if (timeRegulatingAndConstrainedFederatesAwaitingNextMessageRequestTimeAdvanceGrant == null)
              {
                timeRegulatingAndConstrainedFederatesAwaitingNextMessageRequestTimeAdvanceGrant = new HashMap<>();
              }

              LogicalTime lits = timeRegulatingFederate.getLITS(potentialGALT);
              timeRegulatingAndConstrainedFederatesAwaitingNextMessageRequestTimeAdvanceGrant.put(
                timeRegulatingFederate, lits);

              if (smallestLITS == null)
              {
                smallestLITS = lits;
              }
              else if (lits != null)
              {
                smallestLITS = min(smallestLITS, lits);
              }
              break;
          }
        }
      }

      logger.debug(LogMessages.POTENTIAL_GALT, galt, potentialGALT);

      LogicalTime newGALT;
      if (timeRegulatingAndConstrainedFederatesAwaitingNextMessageRequestTimeAdvanceGrant == null ||
          smallestLITS == null || smallestLITS.compareTo(potentialGALT) > 0)
      {
        newGALT = potentialGALT;
      }
      else
      {
        assert timeRegulatingAndConstrainedFederatesAwaitingNextMessageRequestTimeAdvanceGrant.size() > 0;

        newGALT = null;

        // use the federates with the smallest LITS to determine the new GALT (if < potential GALT)

        for (Map.Entry<FederateProxy, LogicalTime> entry :
          timeRegulatingAndConstrainedFederatesAwaitingNextMessageRequestTimeAdvanceGrant.entrySet())
        {
          if (smallestLITS.equals(entry.getValue()))
          {
            newGALT = min(potentialGALT, federateProxy.getLOTS());
            entry.getKey().adjustNextMessageRequestAdvanceRequestTime(smallestLITS);
          }
        }

        assert newGALT != null;

        for (Map.Entry<FederateProxy, LogicalTime> entry :
          timeRegulatingAndConstrainedFederatesAwaitingNextMessageRequestTimeAdvanceGrant.entrySet())
        {
          // adjust the advance request time of any federate with a LITS <= the new GALT

          if (entry.getValue() != null && entry.getValue().compareTo(smallestLITS) > 0 &&
              entry.getValue().compareTo(newGALT) <= 0)
          {
            entry.getKey().adjustNextMessageRequestAdvanceRequestTime(entry.getValue());
          }
        }
      }

      logger.debug(LogMessages.GALT_UPDATED, galt, newGALT);

      galt = newGALT;

      federationExecution.galtUpdated(galt);

      for (FederateHandle timeRegulatingFederateHandle : timeRegulatingFederates)
      {
        FederateProxy timeRegulatingFederate = federationExecution.getFederate(timeRegulatingFederateHandle);

        // recalculate the GALT for each time regulating federate
        //
        LogicalTime newLocalGALT = finalTime;
        for (FederateHandle timeRegulatingFederateHandle2 : timeRegulatingFederates)
        {
          FederateProxy timeRegulatingFederate2 = federationExecution.getFederate(timeRegulatingFederateHandle2);

          if (timeRegulatingFederate != timeRegulatingFederate2)
          {
            newLocalGALT = min(newLocalGALT, timeRegulatingFederate2.getLOTS());
          }
        }

        timeRegulatingFederate.galtUpdated(newLocalGALT);
      }

      // notify the non time regulating federates that GALT has advanced
      //
      for (FederateProxy f : federationExecution.getFederates().values())
      {
        if (!f.isTimeRegulationEnabled())
        {
          f.galtUpdated(galt);
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
