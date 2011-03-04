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

package net.sf.ohla.rti.federate;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.i18n.ExceptionMessages;
import net.sf.ohla.rti.i18n.I18n;
import net.sf.ohla.rti.i18n.I18nLogger;
import net.sf.ohla.rti.i18n.LogMessages;
import net.sf.ohla.rti.messages.DisableTimeConstrained;
import net.sf.ohla.rti.messages.DisableTimeRegulation;
import net.sf.ohla.rti.messages.EnableTimeConstrained;
import net.sf.ohla.rti.messages.EnableTimeRegulation;
import net.sf.ohla.rti.messages.FlushQueueRequest;
import net.sf.ohla.rti.messages.ModifyLookahead;
import net.sf.ohla.rti.messages.NextMessageRequest;
import net.sf.ohla.rti.messages.NextMessageRequestAvailable;
import net.sf.ohla.rti.messages.NextMessageRequestAvailableTimeAdvanceGrant;
import net.sf.ohla.rti.messages.NextMessageRequestTimeAdvanceGrant;
import net.sf.ohla.rti.messages.TimeAdvanceRequest;
import net.sf.ohla.rti.messages.TimeAdvanceRequestAvailable;
import net.sf.ohla.rti.messages.UpdateLITS;

import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.LogicalTimeInterval;
import hla.rti1516e.TimeQueryReturn;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.IllegalTimeArithmetic;
import hla.rti1516e.exceptions.InTimeAdvancingState;
import hla.rti1516e.exceptions.InvalidLogicalTime;
import hla.rti1516e.exceptions.InvalidLogicalTimeInterval;
import hla.rti1516e.exceptions.InvalidLookahead;
import hla.rti1516e.exceptions.LogicalTimeAlreadyPassed;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RequestForTimeConstrainedPending;
import hla.rti1516e.exceptions.RequestForTimeRegulationPending;
import hla.rti1516e.exceptions.TimeConstrainedAlreadyEnabled;
import hla.rti1516e.exceptions.TimeConstrainedIsNotEnabled;
import hla.rti1516e.exceptions.TimeRegulationAlreadyEnabled;
import hla.rti1516e.exceptions.TimeRegulationIsNotEnabled;

public class FederateTimeManager
{
  private enum TemporalState
  {
    TIME_ADVANCING, TIME_GRANTED
  }

  private enum TimeRegulatingState
  {
    TIME_REGULATING, BECOMING_TIME_REGULATING, NOT_TIME_REGULATING
  }

  private enum TimeConstrainedState
  {
    TIME_CONSTRAINED, BECOMING_TIME_CONSTRAINED, NOT_TIME_CONSTRAINED
  }

  private final Federate federate;

  private final I18nLogger log;

  private final LogicalTimeInterval epsilon;

  private final ReadWriteLock timeLock = new ReentrantReadWriteLock(true);

  private TemporalState temporalState = TemporalState.TIME_GRANTED;
  private TimeRegulatingState timeRegulatingState = TimeRegulatingState.NOT_TIME_REGULATING;
  private TimeConstrainedState timeConstrainedState = TimeConstrainedState.NOT_TIME_CONSTRAINED;

  private LogicalTime federateTime;
  private LogicalTime galt;

  private LogicalTimeInterval lookahead;

  /**
   * Least Outgoing Time Stamp.
   */
  private LogicalTime lots;

  private LogicalTime advanceRequestTime;
  private TimeAdvanceType advanceRequestType = TimeAdvanceType.NONE;

  public FederateTimeManager(Federate federate, LogicalTimeFactory logicalTimeFactory)
  {
    this.federate = federate;

    log = I18nLogger.getLogger(federate.getMarker(), getClass());

    epsilon = logicalTimeFactory.makeEpsilon();

    federateTime = logicalTimeFactory.makeInitial();
    galt = logicalTimeFactory.makeInitial();
  }

  public ReadWriteLock getTimeLock()
  {
    return timeLock;
  }

  public boolean isTimeRegulating()
  {
    return timeRegulatingState == TimeRegulatingState.TIME_REGULATING;
  }

  public boolean isTimeConstrained()
  {
    return timeConstrainedState == TimeConstrainedState.TIME_CONSTRAINED;
  }

  public boolean isTimeAdvancing()
  {
    return temporalState == TemporalState.TIME_ADVANCING;
  }

  public boolean isTimeGranted()
  {
    return temporalState == TemporalState.TIME_GRANTED;
  }

  public boolean isTimeConstrainedAndTimeGranted()
  {
    return isTimeConstrained() && isTimeGranted();
  }

  public boolean galtDefined()
  {
    return galt != null;
  }

  public void enableTimeRegulation(LogicalTimeInterval lookahead)
    throws TimeRegulationAlreadyEnabled, InvalidLookahead, InTimeAdvancingState, RequestForTimeRegulationPending,
           RTIinternalError
  {
    timeLock.writeLock().lock();
    try
    {
      if (timeRegulatingState == TimeRegulatingState.TIME_REGULATING)
      {
        throw new TimeRegulationAlreadyEnabled(I18n.getMessage(ExceptionMessages.TIME_REGULATION_ALREADY_ENABLED));
      }

      checkIfInvalidLookahead(lookahead);
      checkIfInTimeAdvancingState();
      checkIfRequestForTimeRegulationPending();

      federate.getRTIChannel().write(new EnableTimeRegulation(lookahead));

      this.lookahead = lookahead;

      timeRegulatingState = TimeRegulatingState.BECOMING_TIME_REGULATING;
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void disableTimeRegulation()
    throws TimeRegulationIsNotEnabled, RTIinternalError
  {
    timeLock.writeLock().lock();
    try
    {
      checkIfTimeRegulationIsNotEnabled();

      federate.getRTIChannel().write(new DisableTimeRegulation());

      timeRegulatingState = TimeRegulatingState.NOT_TIME_REGULATING;

      lookahead = null;
      lots = null;
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void enableTimeConstrained()
    throws TimeConstrainedAlreadyEnabled, InTimeAdvancingState, RequestForTimeConstrainedPending, RTIinternalError
  {
    timeLock.writeLock().lock();
    try
    {
      if (timeConstrainedState == TimeConstrainedState.TIME_CONSTRAINED)
      {
        throw new TimeConstrainedAlreadyEnabled(I18n.getMessage(ExceptionMessages.TIME_CONSTRAINED_ALREADY_ENABLED));
      }

      checkIfInTimeAdvancingState();
      checkIfRequestForTimeConstrainedPending();

      federate.getRTIChannel().write(new EnableTimeConstrained());

      timeConstrainedState = TimeConstrainedState.BECOMING_TIME_CONSTRAINED;
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void disableTimeConstrained()
    throws TimeConstrainedIsNotEnabled, RTIinternalError
  {
    timeLock.writeLock().lock();
    try
    {
      checkIfTimeConstrainedIsNotEnabled();

      federate.getRTIChannel().write(new DisableTimeConstrained());

      timeConstrainedState = TimeConstrainedState.NOT_TIME_CONSTRAINED;

      // TODO: release all TSO messages
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  @SuppressWarnings("unchecked")
  public void timeAdvanceRequest(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState, RequestForTimeRegulationPending,
           RequestForTimeConstrainedPending, RTIinternalError
  {
    timeLock.writeLock().lock();
    try
    {
      checkIfInvalidLogicalTime(time);
      checkIfLogicalTimeAlreadyPassed(time);
      checkIfInTimeAdvancingState();
      checkIfRequestForTimeRegulationPending();
      checkIfRequestForTimeConstrainedPending();

      federate.getRTIChannel().write(new TimeAdvanceRequest(time));

      if (isTimeRegulating())
      {
        try
        {
          lots = time.add(lookahead.isZero() ? epsilon : lookahead);
        }
        catch (IllegalTimeArithmetic ita)
        {
          throw new InvalidLogicalTime(ita.getMessage(), ita);
        }
        catch (InvalidLogicalTimeInterval ilti)
        {
          throw new InvalidLogicalTime(ilti.getMessage(), ilti);
        }
      }

      advanceRequestTime = time;
      advanceRequestType = TimeAdvanceType.TIME_ADVANCE_REQUEST;

      temporalState = TemporalState.TIME_ADVANCING;

      // release any callbacks held until we are time advancing
      //
      federate.getCallbackManager().releaseHeld();
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  @SuppressWarnings("unchecked")
  public void timeAdvanceRequestAvailable(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState, RequestForTimeRegulationPending,
           RequestForTimeConstrainedPending, RTIinternalError
  {
    timeLock.writeLock().lock();
    try
    {
      checkIfInvalidLogicalTime(time);
      checkIfLogicalTimeAlreadyPassed(time);
      checkIfInTimeAdvancingState();
      checkIfRequestForTimeRegulationPending();
      checkIfRequestForTimeConstrainedPending();

      federate.getRTIChannel().write(new TimeAdvanceRequestAvailable(time));

      if (isTimeRegulating())
      {
        try
        {
          lots = time.add(lookahead);
        }
        catch (IllegalTimeArithmetic ita)
        {
          throw new InvalidLogicalTime(ita.getMessage(), ita);
        }
        catch (InvalidLogicalTimeInterval ilti)
        {
          throw new InvalidLogicalTime(ilti.getMessage(), ilti);
        }
      }

      advanceRequestTime = time;
      advanceRequestType = TimeAdvanceType.TIME_ADVANCE_REQUEST_AVAILABLE;

      temporalState = TemporalState.TIME_ADVANCING;

      // release any callbacks held until we are time advancing
      //
      federate.getCallbackManager().releaseHeld();
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  @SuppressWarnings("unchecked")
  public void nextMessageRequest(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState, RequestForTimeRegulationPending,
           RequestForTimeConstrainedPending, RTIinternalError
  {
    timeLock.writeLock().lock();
    try
    {
      checkIfInvalidLogicalTime(time);
      checkIfLogicalTimeAlreadyPassed(time);
      checkIfInTimeAdvancingState();
      checkIfRequestForTimeRegulationPending();
      checkIfRequestForTimeConstrainedPending();

      federate.getRTIChannel().write(new NextMessageRequest(time));

      if (isTimeRegulating())
      {
        try
        {
          lots = time.add(lookahead.isZero() ? epsilon : lookahead);
        }
        catch (IllegalTimeArithmetic ita)
        {
          throw new InvalidLogicalTime(ita.getMessage(), ita);
        }
        catch (InvalidLogicalTimeInterval ilti)
        {
          throw new InvalidLogicalTime(ilti.getMessage(), ilti);
        }
      }

      advanceRequestTime = time;
      advanceRequestType = TimeAdvanceType.NEXT_MESSAGE_REQUEST;

      temporalState = TemporalState.TIME_ADVANCING;

      // release any callbacks held until we are time advancing
      //
      federate.getCallbackManager().releaseHeld();
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  @SuppressWarnings("unchecked")
  public void nextMessageRequestAvailable(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState, RequestForTimeRegulationPending,
           RequestForTimeConstrainedPending, RTIinternalError
  {
    timeLock.writeLock().lock();
    try
    {
      checkIfInvalidLogicalTime(time);
      checkIfLogicalTimeAlreadyPassed(time);
      checkIfInTimeAdvancingState();
      checkIfRequestForTimeConstrainedPending();
      checkIfRequestForTimeRegulationPending();

      federate.getRTIChannel().write(new NextMessageRequestAvailable(time));

      if (isTimeRegulating())
      {
        try
        {
          lots = time.add(lookahead);
        }
        catch (IllegalTimeArithmetic ita)
        {
          throw new InvalidLogicalTime(ita.getMessage(), ita);
        }
        catch (InvalidLogicalTimeInterval ilti)
        {
          throw new InvalidLogicalTime(ilti.getMessage(), ilti);
        }
      }

      advanceRequestTime = time;
      advanceRequestType = TimeAdvanceType.NEXT_MESSAGE_REQUEST_AVAILABLE;

      temporalState = TemporalState.TIME_ADVANCING;

      // release any callbacks held until we are time advancing
      //
      federate.getCallbackManager().releaseHeld();
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  @SuppressWarnings("unchecked")
  public void flushQueueRequest(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState, RequestForTimeRegulationPending,
           RequestForTimeConstrainedPending, RTIinternalError
  {
    timeLock.writeLock().lock();
    try
    {
      checkIfInvalidLogicalTime(time);
      checkIfLogicalTimeAlreadyPassed(time);
      checkIfInTimeAdvancingState();
      checkIfRequestForTimeRegulationPending();
      checkIfRequestForTimeConstrainedPending();

      federate.getRTIChannel().write(new FlushQueueRequest(time));

      if (isTimeRegulating())
      {
        try
        {
          lots = time.add(lookahead);
        }
        catch (IllegalTimeArithmetic ita)
        {
          throw new InvalidLogicalTime(ita.getMessage(), ita);
        }
        catch (InvalidLogicalTimeInterval ilti)
        {
          throw new InvalidLogicalTime(ilti.getMessage(), ilti);
        }
      }

      advanceRequestTime = time;
      advanceRequestType = TimeAdvanceType.FLUSH_QUEUE_REQUEST;

      temporalState = TemporalState.TIME_ADVANCING;

      // release any callbacks held until we are time advancing
      //
      federate.getCallbackManager().releaseHeld();
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public TimeQueryReturn queryGALT()
    throws RTIinternalError
  {
    timeLock.readLock().lock();
    try
    {
      return new TimeQueryReturn(galt != null, galt);
    }
    finally
    {
      timeLock.readLock().unlock();
    }
  }

  public LogicalTime queryLogicalTime()
    throws RTIinternalError
  {
    timeLock.readLock().lock();
    try
    {
      return federateTime;
    }
    finally
    {
      timeLock.readLock().unlock();
    }
  }

  @SuppressWarnings("unchecked")
  public TimeQueryReturn queryLITS()
    throws RTIinternalError
  {
    TimeQueryReturn timeQueryReturn;

    timeLock.readLock().lock();
    try
    {
      LogicalTime nextMessageTime = federate.getNextMessageTime();
      if (nextMessageTime == null)
      {
        // no TSO messages in the queue, LITS is GALT (if GALT is defined)

        timeQueryReturn = new TimeQueryReturn(galt != null, galt);
      }
      else
      {
        if (galt == null || nextMessageTime.compareTo(galt) < 0)
        {
          // GALT is undefined (no more time regulating federates) but there is
          // still a TSO message in the queue or the next TSO message is < GALT

          timeQueryReturn = new TimeQueryReturn(true, nextMessageTime);
        }
        else
        {
          timeQueryReturn = new TimeQueryReturn(true, galt);
        }
      }
    }
    finally
    {
      timeLock.readLock().unlock();
    }

    return timeQueryReturn;
  }

  public void modifyLookahead(LogicalTimeInterval lookahead)
    throws TimeRegulationIsNotEnabled, InvalidLookahead, InTimeAdvancingState, RTIinternalError
  {
    timeLock.writeLock().lock();
    try
    {
      checkIfTimeRegulationIsNotEnabled();
      checkIfInvalidLookahead(lookahead);
      checkIfInTimeAdvancingState();

      federate.getRTIChannel().write(new ModifyLookahead(lookahead));

      this.lookahead = lookahead;
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public LogicalTimeInterval queryLookahead()
    throws TimeRegulationIsNotEnabled, RTIinternalError
  {
    timeLock.readLock().lock();
    try
    {
      checkIfTimeRegulationIsNotEnabled();

      return lookahead;
    }
    finally
    {
      timeLock.readLock().unlock();
    }
  }

  @SuppressWarnings("unchecked")
  public void timeRegulationEnabled(LogicalTime time, FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    timeLock.writeLock().lock();
    try
    {
      federateTime = time;
      lots = time.add(lookahead);

      timeRegulatingState = TimeRegulatingState.TIME_REGULATING;

      federateAmbassador.timeRegulationEnabled(time);
    }
    catch (IllegalTimeArithmetic ilta)
    {
      throw new RuntimeException(ilta);
    }
    catch (InvalidLogicalTimeInterval ilti)
    {
      throw new RuntimeException(ilti);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void timeConstrainedEnabled(LogicalTime time, FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    timeLock.writeLock().lock();
    try
    {
      federateTime = time;

      timeConstrainedState = TimeConstrainedState.TIME_CONSTRAINED;

      federateAmbassador.timeConstrainedEnabled(time);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void timeAdvanceGrant(LogicalTime time, FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    timeLock.writeLock().lock();
    try
    {
      federateTime = time;

      temporalState = TemporalState.TIME_GRANTED;
      advanceRequestType = TimeAdvanceType.NONE;

      if (isTimeConstrained())
      {
        LogicalTime lits = federate.getNextMessageTime();
        if (lits != null)
        {
          // notify the RTI of the new LITS

          federate.getRTIChannel().write(new UpdateLITS(lits));
        }
      }

      federateAmbassador.timeAdvanceGrant(time);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  @SuppressWarnings("unchecked")
  public void galtAdvanced(LogicalTime galt)
  {
    timeLock.writeLock().lock();
    try
    {
      log.trace(LogMessages.GALT_ADVANCED, this.galt, galt);

      this.galt = galt;

      LogicalTime maxFutureTaskTimestamp;

      if (isTimeAdvancing())
      {
        switch (advanceRequestType)
        {
          case NEXT_MESSAGE_REQUEST:
          {
            maxFutureTaskTimestamp = handleNextMessageRequestGALTAdvanced(galt);
            break;
          }
          case NEXT_MESSAGE_REQUEST_AVAILABLE:
          {
            maxFutureTaskTimestamp = handleNextMessageRequestAvailableGALTAdvanced(galt);
            break;
          }
          default:
            maxFutureTaskTimestamp = galt.compareTo(advanceRequestTime) > 0 ? advanceRequestTime : galt;
        }
      }
      else
      {
        maxFutureTaskTimestamp = galt.compareTo(federateTime) > 0 ? federateTime : galt;
      }

      federate.processFutureTasks(maxFutureTaskTimestamp);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void galtUndefined()
  {
    timeLock.writeLock().lock();
    try
    {
      log.debug(LogMessages.GALT_UNDEFINED);

      galt = null;

      federate.clearFutureTasks();
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void checkIfInTimeAdvancingState()
    throws InTimeAdvancingState
  {
    if (isTimeAdvancing())
    {
      throw new InTimeAdvancingState(I18n.getMessage(ExceptionMessages.IN_TIME_ADVANCING_STATE));
    }
  }

  public void checkIfTimeRegulationIsNotEnabled()
    throws TimeRegulationIsNotEnabled
  {
    if (!isTimeRegulating())
    {
      throw new TimeRegulationIsNotEnabled(I18n.getMessage(ExceptionMessages.TIME_REGULATION_IS_NOT_ENABLED));
    }
  }

  public void checkIfRequestForTimeRegulationPending()
    throws RequestForTimeRegulationPending
  {
    if (timeRegulatingState == TimeRegulatingState.BECOMING_TIME_REGULATING)
    {
      throw new RequestForTimeRegulationPending(I18n.getMessage(ExceptionMessages.REQUEST_FOR_TIME_REGULATION_PENDING));
    }
  }

  public void checkIfTimeConstrainedIsNotEnabled()
    throws TimeConstrainedIsNotEnabled
  {
    if (!isTimeConstrained())
    {
      throw new TimeConstrainedIsNotEnabled(I18n.getMessage(ExceptionMessages.TIME_CONSTRAINED_IS_NOT_ENABLED));
    }
  }

  public void checkIfRequestForTimeConstrainedPending()
    throws RequestForTimeConstrainedPending
  {
    if (timeConstrainedState == TimeConstrainedState.BECOMING_TIME_CONSTRAINED)
    {
      throw new RequestForTimeConstrainedPending(I18n.getMessage(
        ExceptionMessages.REQUEST_FOR_TIME_CONSTRAINED_PENDING));
    }
  }

  @SuppressWarnings("unchecked")
  public void checkIfLogicalTimeAlreadyPassed(LogicalTime time)
    throws LogicalTimeAlreadyPassed
  {
    if (time.compareTo(federateTime) < 0)
    {
      throw new LogicalTimeAlreadyPassed(I18n.getMessage(
        ExceptionMessages.LOGICAL_TIME_ALREADY_PASSED, time, federateTime));
    }
  }

  public void checkIfInvalidLogicalTime(LogicalTime time)
    throws InvalidLogicalTime
  {
    if (time == null)
    {
      throw new InvalidLogicalTime(I18n.getMessage(ExceptionMessages.LOGICAL_TIME_IS_NULL));
    }

    // TODO: check against factory type?
  }

  public void checkIfInvalidLookahead(LogicalTimeInterval lookahead)
    throws InvalidLookahead
  {
    if (lookahead == null)
    {
      throw new InvalidLookahead(I18n.getMessage(ExceptionMessages.LOGICAL_TIME_IS_NULL));
    }

    // TODO: check against factory type?
  }

  public void updateAttributeValues(LogicalTime updateTime)
    throws InvalidLogicalTime
  {
    checkIfInvalidTimestamp(updateTime);
  }

  public void sendInteraction(LogicalTime sendTime)
    throws InvalidLogicalTime
  {
    checkIfInvalidTimestamp(sendTime);
  }

  public void deleteObjectInstance(LogicalTime deleteTime)
    throws InvalidLogicalTime
  {
    checkIfInvalidTimestamp(deleteTime);
  }

  @SuppressWarnings("unchecked")
  private LogicalTime handleNextMessageRequestGALTAdvanced(LogicalTime galt)
  {
    LogicalTime maxFutureTaskTimestamp;

    LogicalTime nextMessageTime = federate.getNextMessageTime();
    if (nextMessageTime == null || nextMessageTime.compareTo(galt) >= 0)
    {
      // there are no pending TSO messages or they are >= GALT

      if (advanceRequestTime.compareTo(galt) < 0)
      {
        // GALT has advanced past the requested time

        maxFutureTaskTimestamp = advanceRequestTime;

        federate.getRTIChannel().write(new NextMessageRequestTimeAdvanceGrant(advanceRequestTime));
      }
      else
      {
        maxFutureTaskTimestamp = galt;
      }
    }
    else if (advanceRequestTime.compareTo(nextMessageTime) < 0)
    {
      // the requested time is < the pending TSO message which is < GALT 

      maxFutureTaskTimestamp = advanceRequestTime;

      federate.getRTIChannel().write(new NextMessageRequestTimeAdvanceGrant(advanceRequestTime));
    }
    else
    {
      // the pending TSO message is < GALT which is <= the requested time

      advanceRequestTime = nextMessageTime;
      maxFutureTaskTimestamp = nextMessageTime;

      federate.getRTIChannel().write(new NextMessageRequestTimeAdvanceGrant(nextMessageTime));
    }

    return maxFutureTaskTimestamp;
  }

  @SuppressWarnings("unchecked")
  private LogicalTime handleNextMessageRequestAvailableGALTAdvanced(LogicalTime galt)
  {
    LogicalTime maxFutureTaskTimestamp;

    LogicalTime nextMessageTime = federate.getNextMessageTime();
    if (nextMessageTime == null || nextMessageTime.compareTo(galt) > 0)
    {
      // there are no pending TSO messages or they are > GALT

      if (advanceRequestTime.compareTo(galt) <= 0)
      {
        // GALT has advanced past the requested time

        maxFutureTaskTimestamp = advanceRequestTime;

        federate.getRTIChannel().write(new NextMessageRequestAvailableTimeAdvanceGrant(advanceRequestTime));
      }
      else
      {
        maxFutureTaskTimestamp = galt;
      }
    }
    else if (advanceRequestTime.compareTo(nextMessageTime) < 0)
    {
      // the requested time is < the pending TSO message which is <= GALT

      maxFutureTaskTimestamp = advanceRequestTime;

      federate.getRTIChannel().write(new NextMessageRequestTimeAdvanceGrant(advanceRequestTime));
    }
    else
    {
      // the pending TSO message is <= GALT which is <= the requested time

      advanceRequestTime = nextMessageTime;
      maxFutureTaskTimestamp = nextMessageTime;

      federate.getRTIChannel().write(new NextMessageRequestTimeAdvanceGrant(nextMessageTime));
    }

    return maxFutureTaskTimestamp;
  }

  @SuppressWarnings("unchecked")
  private void checkIfInvalidTimestamp(LogicalTime time)
    throws InvalidLogicalTime
  {
    checkIfInvalidLogicalTime(time);

    if (isTimeRegulating())
    {
      if (time.compareTo(lots) < 0)
      {
        throw new InvalidLogicalTime(I18n.getMessage(ExceptionMessages.LOGICAL_TIME_IS_LESS_THAN_LOTS, time, lots));
      }
    }
    else
    {
      // TODO: is validating the timestamp required for non time regulating?

      LogicalTime minimumTime = isTimeAdvancing() ? advanceRequestTime : federateTime;
      if (time.compareTo(minimumTime) <= 0)
      {
        throw new InvalidLogicalTime(I18n.getMessage(
          ExceptionMessages.LOGICAL_TIME_IS_LESS_THAN_OR_EQUAL_TO_MINIMUM_TIME, time, minimumTime));
      }
    }
  }
}
