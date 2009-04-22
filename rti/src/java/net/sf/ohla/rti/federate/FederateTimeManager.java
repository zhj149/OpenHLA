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
import net.sf.ohla.rti.messages.FlushQueueRequest;
import net.sf.ohla.rti.messages.ModifyLookahead;
import net.sf.ohla.rti.messages.NextMessageRequest;
import net.sf.ohla.rti.messages.NextMessageRequestAvailable;
import net.sf.ohla.rti.messages.NextMessageRequestAvailableTimeAdvanceGrant;
import net.sf.ohla.rti.messages.NextMessageRequestTimeAdvanceGrant;
import net.sf.ohla.rti.messages.TimeAdvanceRequest;
import net.sf.ohla.rti.messages.TimeAdvanceRequestAvailable;
import net.sf.ohla.rti.messages.UpdateLITS;

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
  private static final Logger log =
    LoggerFactory.getLogger(FederateTimeManager.class);

  protected enum TemporalState
  {
    TIME_ADVANCING, TIME_GRANTED
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

  protected final LogicalTimeInterval epsilon;

  protected final ReadWriteLock timeLock = new ReentrantReadWriteLock(true);

  protected TemporalState temporalState = TemporalState.TIME_GRANTED;
  protected TimeRegulatingState timeRegulatingState =
    TimeRegulatingState.NOT_TIME_REGULATING;
  protected TimeConstrainedState timeConstrainedState =
    TimeConstrainedState.NOT_TIME_CONSTRAINED;

  protected LogicalTime federateTime;
  protected LogicalTime galt;

  protected LogicalTimeInterval lookahead;

  /**
   * Least Outgoing Time Stamp.
   */
  protected LogicalTime lots;

  protected LogicalTime advanceRequestTime;
  protected TimeAdvanceType advanceRequestType = TimeAdvanceType.NONE;

  public FederateTimeManager(Federate federate,
                             MobileFederateServices mobileFederateServices,
                             LogicalTime galt)
  {
    this.federate = federate;
    this.mobileFederateServices = mobileFederateServices;
    this.galt = galt;

    epsilon = mobileFederateServices.intervalFactory.makeEpsilon();

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

  public boolean galtDefined()
  {
    return galt != null;
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

      lookahead = null;
      lots = null;
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

      WriteFuture writeFuture =
        federate.getRTISession().write(new TimeAdvanceRequest(time));

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      if (isTimeRegulating())
      {
        try
        {
          lots = time.add(lookahead.isZero() ? epsilon : lookahead);
        }
        catch (IllegalTimeArithmetic ita)
        {
          throw new InvalidLogicalTime(ita);
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

      WriteFuture writeFuture =
        federate.getRTISession().write(new TimeAdvanceRequestAvailable(time));

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      if (isTimeRegulating())
      {
        try
        {
          lots = time.add(lookahead);
        }
        catch (IllegalTimeArithmetic ita)
        {
          throw new InvalidLogicalTime(ita);
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

      WriteFuture writeFuture =
        federate.getRTISession().write(new NextMessageRequest(time));

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      if (isTimeRegulating())
      {
        try
        {
          lots = time.add(lookahead.isZero() ? epsilon : lookahead);
        }
        catch (IllegalTimeArithmetic ita)
        {
          throw new InvalidLogicalTime(ita);
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

      WriteFuture writeFuture =
        federate.getRTISession().write(new NextMessageRequestAvailable(time));

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      if (isTimeRegulating())
      {
        try
        {
          lots = time.add(lookahead);
        }
        catch (IllegalTimeArithmetic ita)
        {
          throw new InvalidLogicalTime(ita);
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

      WriteFuture writeFuture =
        federate.getRTISession().write(new FlushQueueRequest(time));

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      if (isTimeRegulating())
      {
        try
        {
          lots = time.add(lookahead);
        }
        catch (IllegalTimeArithmetic ita)
        {
          throw new InvalidLogicalTime(ita);
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
      LogicalTime nextMessageTime = federate.getNextMessageTime();
      if (nextMessageTime == null)
      {
        // no TSO messages in the queue, LITS is GALT (if GALT is defined)

        timeQueryReturn.timeIsValid = galt != null;
        timeQueryReturn.time = galt;
      }
      else
      {
        timeQueryReturn.timeIsValid = true;

        if (galt == null || nextMessageTime.compareTo(galt) < 0)
        {
          // GALT is undefined (no more time regulating federates) but there is
          // still a TSO message in the queue or the next TSO message is < GALT

          timeQueryReturn.time = nextMessageTime;
        }
        else
        {
          timeQueryReturn.time = galt;
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
      log.warn(federate.getMarker(), String.format(
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
      log.warn(federate.getMarker(),
               "federate unable to enable time constrained to: {}", time);
      log.warn(federate.getMarker(), "", t);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void timeAdvanceGrant(
    LogicalTime time, FederateAmbassador federateAmbassador)
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

          federate.getRTISession().write(new UpdateLITS(lits));
        }
      }

      federateAmbassador.timeAdvanceGrant(time);
    }
    catch (Throwable t)
    {
      log.warn(federate.getMarker(), "federate unable to advance to: {}", time);
      log.warn(federate.getMarker(), "", t);
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

      log.debug(federate.getMarker(), "GALT advanced: {}", galt);

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
            maxFutureTaskTimestamp =
              handleNextMessageRequestAvailableGALTAdvanced(galt);
            break;
          }
          default:
            maxFutureTaskTimestamp = galt.compareTo(advanceRequestTime) > 0 ?
              advanceRequestTime : galt;
        }
      }
      else
      {
        maxFutureTaskTimestamp =
          galt.compareTo(federateTime) > 0 ? federateTime : galt;
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
      log.debug(federate.getMarker(), "GALT undefined");

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

  protected LogicalTime handleNextMessageRequestGALTAdvanced(LogicalTime galt)
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

        WriteFuture writeFuture = federate.getRTISession().write(
          new NextMessageRequestTimeAdvanceGrant(advanceRequestTime));

        // TODO: set timeout
        //
        writeFuture.join();

        if (!writeFuture.isWritten())
        {
          // TODO: what to do if an error occurs
        }
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

      WriteFuture writeFuture = federate.getRTISession().write(
        new NextMessageRequestTimeAdvanceGrant(advanceRequestTime));

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        // TODO: what to do if an error occurs
      }
    }
    else
    {
      // the pending TSO message is < GALT which is <= the requested time

      advanceRequestTime = nextMessageTime;
      maxFutureTaskTimestamp = nextMessageTime;

      WriteFuture writeFuture = federate.getRTISession().write(
        new NextMessageRequestTimeAdvanceGrant(nextMessageTime));

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        // TODO: what to do if an error occurs
      }
    }

    return maxFutureTaskTimestamp;
  }

  protected LogicalTime handleNextMessageRequestAvailableGALTAdvanced(
    LogicalTime galt)
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

        WriteFuture writeFuture = federate.getRTISession().write(
          new NextMessageRequestAvailableTimeAdvanceGrant(advanceRequestTime));

        // TODO: set timeout
        //
        writeFuture.join();

        if (!writeFuture.isWritten())
        {
          // TODO: what to do if an error occurs
        }
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

      WriteFuture writeFuture = federate.getRTISession().write(
        new NextMessageRequestTimeAdvanceGrant(advanceRequestTime));

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        // TODO: what to do if an error occurs
      }
    }
    else
    {
      // the pending TSO message is <= GALT which is <= the requested time

      advanceRequestTime = nextMessageTime;
      maxFutureTaskTimestamp = nextMessageTime;

      WriteFuture writeFuture = federate.getRTISession().write(
        new NextMessageRequestTimeAdvanceGrant(nextMessageTime));

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        // TODO: what to do if an error occurs
      }
    }

    return maxFutureTaskTimestamp;
  }

  protected void checkIfInvalidTimestamp(LogicalTime time)
    throws InvalidLogicalTime
  {
    checkIfInvalidLogicalTime(time);

    if (isTimeRegulating())
    {
      if (time.compareTo(lots) < 0)
      {
        throw new InvalidLogicalTime(String.format("%s <= %s", time, lots));
      }
    }
    else
    {
      // TODO: is validating the timestamp required for non time regulating

      LogicalTime minimumTime =
        isTimeAdvancing() ? advanceRequestTime : federateTime;
      if (time.compareTo(minimumTime) <= 0)
      {
        throw new InvalidLogicalTime(
          String.format("%s <= %s", time, minimumTime));
      }
    }
  }
}
