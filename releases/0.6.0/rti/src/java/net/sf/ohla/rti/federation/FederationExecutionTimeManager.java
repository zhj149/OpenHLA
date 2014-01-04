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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandle;
import net.sf.ohla.rti.i18n.I18nLogger;
import net.sf.ohla.rti.i18n.LogMessages;
import net.sf.ohla.rti.messages.QueryGALT;
import net.sf.ohla.rti.messages.QueryGALTResponse;
import net.sf.ohla.rti.messages.QueryLITS;
import net.sf.ohla.rti.messages.QueryLITSResponse;

import hla.rti1516e.FederateHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.LogicalTimeInterval;
import hla.rti1516e.exceptions.CouldNotDecode;
import hla.rti1516e.exceptions.CouldNotEncode;
import hla.rti1516e.exceptions.IllegalTimeArithmetic;
import hla.rti1516e.exceptions.InvalidLogicalTimeInterval;

public class FederationExecutionTimeManager
{
  private final FederationExecution federationExecution;
  private final LogicalTimeFactory logicalTimeFactory;
  private final LogicalTime initialTime;
  private final LogicalTime finalTime;
  private final LogicalTimeInterval epsilon;

  private final ReadWriteLock timeLock = new ReentrantReadWriteLock(true);

  private final Set<FederateHandle> timeRegulatingFederates = new HashSet<FederateHandle>();
  private final Set<FederateHandle> timeConstrainedFederates = new HashSet<FederateHandle>();

  private final I18nLogger log;

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
        FederateProxy lastTimeRegulatingFederate = federationExecution.getFederate(
          timeRegulatingFederates.iterator().next());

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

          galt = leastTimeRegulatingLOTS;

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
      timeConstrainedFederates.remove(federateProxy);

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
      federateProxy.getFederateChannel().write(new QueryGALTResponse(queryGALT.getId(), federateProxy.getGALT()));
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
      federateProxy.getFederateChannel().write(new QueryLITSResponse(queryLITS.getId(), federateProxy.getLITSOrGALT()));
    }
    finally
    {
      timeLock.readLock().unlock();
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

      if (federateProxy.isTimeRegulationEnabled())
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

      if (federateProxy.isTimeRegulationEnabled())
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

      if (federateProxy.isTimeRegulationEnabled())
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

  public void nextMessageRequestAvailable(FederateProxy federateProxy, LogicalTime time)
  {
    timeLock.writeLock().lock();
    try
    {
      federateProxy.nextMessageRequestAvailable(time);

      if (federateProxy.isTimeRegulationEnabled())
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

      if (federateProxy.isTimeRegulationEnabled())
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

  public void saveState(DataOutput out)
    throws IOException, CouldNotEncode
  {
    out.writeInt(timeRegulatingFederates.size());
    for (FederateHandle federateHandle : timeRegulatingFederates)
    {
      ((IEEE1516eFederateHandle) federateHandle).writeTo(out);
    }

    out.writeInt(timeConstrainedFederates.size());
    for (FederateHandle federateHandle : timeConstrainedFederates)
    {
      ((IEEE1516eFederateHandle) federateHandle).writeTo(out);
    }

    if (galt == null)
    {
      // TODO: null should be -1?
      //
      out.writeInt(0);
    }
    else
    {
      byte[] encodedGalt = new byte[galt.encodedLength()];
      out.writeInt(encodedGalt.length);
      galt.encode(encodedGalt, 0);
      out.write(encodedGalt);
    }
  }

  public void restoreState(DataInput in)
    throws IOException, CouldNotDecode
  {
    for (int i = in.readInt(); i > 0; i--)
    {
      timeRegulatingFederates.add(IEEE1516eFederateHandle.decode(in));
    }

    for (int i = in.readInt(); i > 0; i--)
    {
      timeConstrainedFederates.add(IEEE1516eFederateHandle.decode(in));
    }

    int encodedGALTLength = in.readInt();
    if (encodedGALTLength == 0)
    {
      galt = null;
    }
    else
    {
      byte[] encodedGALT = new byte[encodedGALTLength];
      in.readFully(encodedGALT);
      galt = logicalTimeFactory.decodeTime(encodedGALT, 0);
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

      log.debug(LogMessages.POTENTIAL_GALT, oldGALT, galt);

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
                timeRegulatingAndConstrainedFederatesAwaitingNextMessageRequestTimeAdvanceGrant =
                  new HashMap<FederateProxy, LogicalTime>();
              }

              LogicalTime lits = timeRegulatingFederate.getLITS();
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

      log.debug(LogMessages.POTENTIAL_GALT, galt, potentialGALT);

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

      log.debug(LogMessages.GALT_UPDATED, galt, newGALT);

      galt = newGALT;

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
