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

import net.sf.ohla.rti.messages.DisableTimeConstrained;
import net.sf.ohla.rti.messages.DisableTimeRegulation;
import net.sf.ohla.rti.messages.EnableTimeConstrained;
import net.sf.ohla.rti.messages.EnableTimeRegulation;
import net.sf.ohla.rti.messages.ModifyLookahead;
import net.sf.ohla.rti.messages.TimeAdvanceRequest;
import net.sf.ohla.rti.messages.TimeAdvanceRequestAvailable;
import net.sf.ohla.rti.messages.callbacks.TimeAdvanceGrant;

import org.apache.mina.common.WriteFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hla.rti1516.FederateAmbassador;
import hla.rti1516.IllegalTimeArithmetic;
import hla.rti1516.InTimeAdvancingState;
import hla.rti1516.InvalidLogicalTime;
import hla.rti1516.InvalidLookahead;
import hla.rti1516.LogicalTime;
import hla.rti1516.LogicalTimeAlreadyPassed;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.MobileFederateServices;
import hla.rti1516.RTIinternalError;
import hla.rti1516.RequestForTimeConstrainedPending;
import hla.rti1516.RequestForTimeRegulationPending;
import hla.rti1516.TimeConstrainedAlreadyEnabled;
import hla.rti1516.TimeConstrainedIsNotEnabled;
import hla.rti1516.TimeQueryReturn;
import hla.rti1516.TimeRegulationAlreadyEnabled;
import hla.rti1516.TimeRegulationIsNotEnabled;

public class FederateTimeManager
{
  private static final Logger log = LoggerFactory.getLogger(FederateTimeManager.class);

  protected enum TemporalState
  {
    TIME_ADVANCING, TIME_GRANTED
  }

  protected enum TimeAdvanceType
  {
    NONE, TIME_ADVANCE_REQUEST, TIME_ADVANCE_REQUEST_AVAILABLE,
    NEXT_MESSAGE_REQUEST, NEXT_MESSAGE_REQUEST_AVAILABLE,
    FLUSH_QUEUE_REQUEST
  }

  protected enum TimeRegulatingState
  {
    TIME_REGULATING, BECOMING_TIME_REGULATING, NOT_TIME_REGULATING
  }

  protected enum TimeConstrainedState
  {
    TIME_CONSTRAINED, BECOMING_TIME_CONSTRAINED, NOT_TIME_CONSTRAINED
  }

  protected final Federate federate;
  protected final MobileFederateServices mobileFederateServices;

  protected ReadWriteLock timeLock = new ReentrantReadWriteLock(true);

  protected TemporalState temporalState = TemporalState.TIME_GRANTED;
  protected TimeRegulatingState timeRegulatingState =
    TimeRegulatingState.NOT_TIME_REGULATING;
  protected TimeConstrainedState timeConstrainedState =
    TimeConstrainedState.NOT_TIME_CONSTRAINED;

  protected LogicalTime federateTime;
  protected LogicalTime galt;
  protected LogicalTime lits;

  protected LogicalTimeInterval lookahead;

  protected LogicalTime advanceRequestTime;
  protected TimeAdvanceType advanceRequestTimeType = TimeAdvanceType.NONE;

  public FederateTimeManager(Federate federate,
                             MobileFederateServices mobileFederateServices,
                             LogicalTime galt)
  {
    this.federate = federate;
    this.mobileFederateServices = mobileFederateServices;
    this.galt = galt;

    federateTime = mobileFederateServices.timeFactory.makeInitial();
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

  public void enableTimeRegulation(LogicalTimeInterval lookahead)
    throws TimeRegulationAlreadyEnabled, InvalidLookahead, InTimeAdvancingState,
           RequestForTimeRegulationPending, RTIinternalError
  {
    timeLock.writeLock().lock();
    try
    {
      if (timeRegulatingState == TimeRegulatingState.TIME_REGULATING)
      {
        throw new TimeRegulationAlreadyEnabled();
      }

      checkIfInvalidLookahead(lookahead);
      checkIfInTimeAdvancingState();
      checkIfRequestForTimeRegulationPending();

      WriteFuture writeFuture =
        federate.getRTISession().write(new EnableTimeRegulation(lookahead));

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

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

      WriteFuture writeFuture =
        federate.getRTISession().write(new DisableTimeRegulation());

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      timeRegulatingState = TimeRegulatingState.NOT_TIME_REGULATING;
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void enableTimeConstrained()
    throws TimeConstrainedAlreadyEnabled, InTimeAdvancingState,
           RequestForTimeConstrainedPending, RTIinternalError
  {
    timeLock.writeLock().lock();
    try
    {
      if (timeConstrainedState == TimeConstrainedState.TIME_CONSTRAINED)
      {
        throw new TimeConstrainedAlreadyEnabled();
      }

      checkIfInTimeAdvancingState();

      checkIfRequestForTimeConstrainedPending();

      WriteFuture writeFuture =
        federate.getRTISession().write(new EnableTimeConstrained());

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

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

      WriteFuture writeFuture =
        federate.getRTISession().write(new DisableTimeConstrained());

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      timeConstrainedState = TimeConstrainedState.NOT_TIME_CONSTRAINED;

      // TODO: release all TSO messages
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void timeAdvanceRequest(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           RTIinternalError
  {
    timeLock.writeLock().lock();
    try
    {
      checkIfInvalidLogicalTime(time);
      checkIfLogicalTimeAlreadyPassed(time);
      checkIfInTimeAdvancingState();
      checkIfRequestForTimeRegulationPending();
      checkIfRequestForTimeConstrainedPending();

      advanceRequestTime = time;
      advanceRequestTimeType = TimeAdvanceType.TIME_ADVANCE_REQUEST;

      if (timeRegulatingState == TimeRegulatingState.NOT_TIME_REGULATING &&
          timeConstrainedState == TimeConstrainedState.NOT_TIME_CONSTRAINED)
      {
        // immediately grant the request
        //
        federate.getCallbackManager().add(new TimeAdvanceGrant(time));
      }
      else
      {
        WriteFuture writeFuture =
          federate.getRTISession().write(new TimeAdvanceRequest(time));

        // TODO: set timeout
        //
        writeFuture.join();

        if (!writeFuture.isWritten())
        {
          throw new RTIinternalError("error communicating with RTI");
        }

        // TODO: will need to send to peers as well
      }

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

  public void timeAdvanceRequestAvailable(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           RTIinternalError
  {
    timeLock.writeLock().lock();
    try
    {
      checkIfInvalidLogicalTime(time);
      checkIfLogicalTimeAlreadyPassed(time);
      checkIfInTimeAdvancingState();
      checkIfRequestForTimeRegulationPending();
      checkIfRequestForTimeConstrainedPending();

      advanceRequestTime = time;
      advanceRequestTimeType = TimeAdvanceType.TIME_ADVANCE_REQUEST_AVAILABLE;

      if (timeRegulatingState == TimeRegulatingState.NOT_TIME_REGULATING &&
          timeConstrainedState == TimeConstrainedState.NOT_TIME_CONSTRAINED)
      {
        // immediately grant the request
        //
        federate.getCallbackManager().add(new TimeAdvanceGrant(time));
      }
      else
      {
        WriteFuture writeFuture =
          federate.getRTISession().write(new TimeAdvanceRequestAvailable(time));

        // TODO: set timeout
        //
        writeFuture.join();

        if (!writeFuture.isWritten())
        {
          throw new RTIinternalError("error communicating with RTI");
        }
      }

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

  public void nextMessageRequest(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           RTIinternalError
  {
    timeLock.writeLock().lock();
    try
    {
      checkIfInvalidLogicalTime(time);
      checkIfLogicalTimeAlreadyPassed(time);
      checkIfInTimeAdvancingState();
      checkIfRequestForTimeRegulationPending();
      checkIfRequestForTimeConstrainedPending();

      LogicalTime nextMessageTime = federate.getNextMessageTime();
      time = nextMessageTime == null ? time :
        nextMessageTime.compareTo(time) > 1 ? time : nextMessageTime;

      advanceRequestTime = time;
      advanceRequestTimeType = TimeAdvanceType.NEXT_MESSAGE_REQUEST;

      if (timeRegulatingState == TimeRegulatingState.NOT_TIME_REGULATING &&
          timeConstrainedState == TimeConstrainedState.NOT_TIME_CONSTRAINED)
      {
        // immediately grant the request
        //
        federate.getCallbackManager().add(new TimeAdvanceGrant(time));
      }
      else
      {
        WriteFuture writeFuture =
          federate.getRTISession().write(new TimeAdvanceRequest(time));

        // TODO: set timeout
        //
        writeFuture.join();

        if (!writeFuture.isWritten())
        {
          throw new RTIinternalError("error communicating with RTI");
        }
      }

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

  public void nextMessageRequestAvailable(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           RTIinternalError
  {
    timeLock.writeLock().lock();
    try
    {
      checkIfInvalidLogicalTime(time);
      checkIfLogicalTimeAlreadyPassed(time);
      checkIfInTimeAdvancingState();
      checkIfRequestForTimeConstrainedPending();
      checkIfRequestForTimeRegulationPending();

      advanceRequestTime = time;
      advanceRequestTimeType = TimeAdvanceType.NEXT_MESSAGE_REQUEST_AVAILABLE;

      if (timeRegulatingState == TimeRegulatingState.NOT_TIME_REGULATING &&
          timeConstrainedState == TimeConstrainedState.NOT_TIME_CONSTRAINED)
      {
        // immediately grant the request
        //
        federate.getCallbackManager().add(new TimeAdvanceGrant(time));
      }
      else
      {
        WriteFuture writeFuture =
          federate.getRTISession().write(new TimeAdvanceRequest(time));

        // TODO: set timeout
        //
        writeFuture.join();

        if (!writeFuture.isWritten())
        {
          throw new RTIinternalError("error communicating with RTI");
        }
      }

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

  public void flushQueueRequest(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           RTIinternalError
  {
    timeLock.writeLock().lock();
    try
    {
      checkIfInvalidLogicalTime(time);
      checkIfLogicalTimeAlreadyPassed(time);
      checkIfInTimeAdvancingState();
      checkIfRequestForTimeRegulationPending();
      checkIfRequestForTimeConstrainedPending();

      advanceRequestTime = time;
      advanceRequestTimeType = TimeAdvanceType.FLUSH_QUEUE_REQUEST;

      if (timeRegulatingState == TimeRegulatingState.NOT_TIME_REGULATING &&
          timeConstrainedState == TimeConstrainedState.NOT_TIME_CONSTRAINED)
      {
        // immediately grant the request
        //
        federate.getCallbackManager().add(new TimeAdvanceGrant(time));
      }
      else
      {
        WriteFuture writeFuture =
          federate.getRTISession().write(new TimeAdvanceRequest(time));

        // TODO: set timeout
        //
        writeFuture.join();

        if (!writeFuture.isWritten())
        {
          throw new RTIinternalError("error communicating with RTI");
        }
      }

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
    TimeQueryReturn timeQueryReturn = new TimeQueryReturn();

    timeLock.readLock().lock();
    try
    {
      timeQueryReturn.timeIsValid = galt != null;
      timeQueryReturn.time = galt;
    }
    finally
    {
      timeLock.readLock().unlock();
    }

    return timeQueryReturn;
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

  public TimeQueryReturn queryLITS()
    throws RTIinternalError
  {
    TimeQueryReturn timeQueryReturn = new TimeQueryReturn();

    timeLock.readLock().lock();
    try
    {
      timeQueryReturn.timeIsValid = lits != null;
      timeQueryReturn.time = lits;
    }
    finally
    {
      timeLock.readLock().unlock();
    }

    return timeQueryReturn;
  }

  public void modifyLookahead(LogicalTimeInterval lookahead)
    throws TimeRegulationIsNotEnabled, InvalidLookahead, InTimeAdvancingState,
           RTIinternalError
  {
    timeLock.writeLock().lock();
    try
    {
      checkIfTimeRegulationIsNotEnabled();
      checkIfInvalidLookahead(lookahead);
      checkIfInTimeAdvancingState();

      WriteFuture writeFuture =
        federate.getRTISession().write(new ModifyLookahead(lookahead));

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

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

  public void timeRegulationEnabled(LogicalTime time,
                                    FederateAmbassador federateAmbassador)
  {
    timeLock.writeLock().lock();
    try
    {
      federateTime = time;

      timeRegulatingState = TimeRegulatingState.TIME_REGULATING;

      federateAmbassador.timeRegulationEnabled(time);
    }
    catch (Throwable t)
    {
      log.warn(String.format(
        "federate unable to enable time regulation to: %s", time), t);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void timeConstrainedEnabled(LogicalTime time,
                                     FederateAmbassador federateAmbassador)
  {
    timeLock.writeLock().lock();
    try
    {
      federateTime = time;

      timeConstrainedState = TimeConstrainedState.TIME_CONSTRAINED;

      federateAmbassador.timeConstrainedEnabled(time);
    }
    catch (Throwable t)
    {
      log.warn(String.format(
        "federate unable to enable time constrained to: %s", time), t);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void timeAdvanceGrant(LogicalTime time,
                               FederateAmbassador federateAmbassador)
  {
    timeLock.writeLock().lock();
    try
    {
      federateTime = time;

      temporalState = TemporalState.TIME_GRANTED;
      advanceRequestTimeType = TimeAdvanceType.NONE;

      federateAmbassador.timeAdvanceGrant(time);
    }
    catch (Throwable t)
    {
      log.warn(String.format("federate unable to advance to: %s", time), t);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void galtAdvanced(LogicalTime galt)
  {
    timeLock.writeLock().lock();
    try
    {
      this.galt = galt;

      log.debug("GALT advanced: {}", galt);

      LogicalTime maxFutureTaskTimestamp = isTimeAdvancing() ?
        (galt.compareTo(advanceRequestTime) <= 0 ? galt : advanceRequestTime) :
        (galt.compareTo(federateTime) <= 0 ? galt : federateTime);
      federate.processFutureTasks(maxFutureTaskTimestamp);
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
      throw new InTimeAdvancingState();
    }
  }

  public void checkIfTimeRegulationIsNotEnabled()
    throws TimeRegulationIsNotEnabled
  {
    if (!isTimeRegulating())
    {
      throw new TimeRegulationIsNotEnabled();
    }
  }

  public void checkIfRequestForTimeRegulationPending()
    throws RequestForTimeRegulationPending
  {
    if (timeRegulatingState == TimeRegulatingState.BECOMING_TIME_REGULATING)
    {
      throw new RequestForTimeRegulationPending();
    }
  }

  public void checkIfTimeConstrainedIsNotEnabled()
    throws TimeConstrainedIsNotEnabled
  {
    if (!isTimeConstrained())
    {
      throw new TimeConstrainedIsNotEnabled();
    }
  }

  public void checkIfRequestForTimeConstrainedPending()
    throws RequestForTimeConstrainedPending
  {
    if (timeConstrainedState == TimeConstrainedState.BECOMING_TIME_CONSTRAINED)
    {
      throw new RequestForTimeConstrainedPending();
    }
  }

  public void checkIfLogicalTimeAlreadyPassed(LogicalTime time)
    throws LogicalTimeAlreadyPassed
  {
    if (time.compareTo(federateTime) < 0)
    {
      throw new LogicalTimeAlreadyPassed(
        String.format("%s < %s", time, federateTime));
    }
  }

  public void checkIfInvalidLogicalTime(LogicalTime time)
    throws InvalidLogicalTime
  {
    if (time == null)
    {
      throw new InvalidLogicalTime("null");
    }

    // TODO: check against factory type?
  }

  public void checkIfInvalidLookahead(LogicalTimeInterval lookahead)
    throws InvalidLookahead
  {
    if (lookahead == null)
    {
      throw new InvalidLookahead("null");
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

  public void checkIfInvalidTimestamp(LogicalTime time)
    throws InvalidLogicalTime
  {
    checkIfInvalidLogicalTime(time);

    LogicalTime minimumTime =
      isTimeAdvancing() ? advanceRequestTime : federateTime;

    if (!isTimeRegulating() && time.compareTo(minimumTime) <= 0)
    {
      throw new InvalidLogicalTime(
        String.format("%s <= %s", time, minimumTime));
    }
    else
    {
      switch (advanceRequestTimeType)
      {
        case NONE:
        case TIME_ADVANCE_REQUEST:
        case NEXT_MESSAGE_REQUEST:
        {
          if (lookahead.isZero())
          {
            // handle special case when lookahead is 0

            if (time.compareTo(minimumTime) <= 0)
            {
              throw new InvalidLogicalTime(
                String.format("%s <= %s", time, minimumTime));
            }

            break;
          }
        }
        case TIME_ADVANCE_REQUEST_AVAILABLE:
        case NEXT_MESSAGE_REQUEST_AVAILABLE:
        case FLUSH_QUEUE_REQUEST:
        {
          try
          {
            LogicalTime minimumTimePlusLookahead = minimumTime.add(lookahead);

            if (time.compareTo(minimumTimePlusLookahead) < 0)
            {
              throw new InvalidLogicalTime(String.format(
                "%s < %s (%s + %s)", time, minimumTimePlusLookahead,
                minimumTime, lookahead));
            }
          }
          catch (IllegalTimeArithmetic ita)
          {
            throw new InvalidLogicalTime(ita);
          }

          break;
        }
        default:
        {
          assert false :
            String.format("unknown TimeAdvanceType: %s",
                          advanceRequestTimeType);
        }
      }
    }
  }
}
