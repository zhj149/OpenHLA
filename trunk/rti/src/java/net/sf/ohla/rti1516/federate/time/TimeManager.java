/*
 * Copyright (c) 2006, Michael Newcomb
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

package net.sf.ohla.rti1516.federate.time;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti1516.federate.Federate;
import static net.sf.ohla.rti1516.federate.time.TemporalState.TIME_ADVANCING;
import static net.sf.ohla.rti1516.federate.time.TemporalState.TIME_GRANTED;
import static net.sf.ohla.rti1516.federate.time.TimeConstrainedState.BECOMING_TIME_CONSTRAINED;
import static net.sf.ohla.rti1516.federate.time.TimeConstrainedState.NOT_TIME_CONSTRAINED;
import static net.sf.ohla.rti1516.federate.time.TimeConstrainedState.TIME_CONSTRAINED;
import static net.sf.ohla.rti1516.federate.time.TimeRegulatingState.BECOMING_TIME_REGULATING;
import static net.sf.ohla.rti1516.federate.time.TimeRegulatingState.NOT_TIME_REGULATING;
import static net.sf.ohla.rti1516.federate.time.TimeRegulatingState.TIME_REGULATING;
import net.sf.ohla.rti1516.messages.DisableTimeConstrained;
import net.sf.ohla.rti1516.messages.DisableTimeRegulation;
import net.sf.ohla.rti1516.messages.EnableTimeConstrained;
import net.sf.ohla.rti1516.messages.EnableTimeRegulation;
import net.sf.ohla.rti1516.messages.ModifyLookahead;
import net.sf.ohla.rti1516.messages.TimeAdvanceRequest;
import net.sf.ohla.rti1516.messages.TimeAdvanceRequestAvailable;

import org.apache.mina.common.WriteFuture;

import hla.rti1516.InTimeAdvancingState;
import hla.rti1516.InvalidLogicalTime;
import hla.rti1516.InvalidLookahead;
import hla.rti1516.LogicalTime;
import hla.rti1516.LogicalTimeAlreadyPassed;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.RTIinternalError;
import hla.rti1516.RequestForTimeConstrainedPending;
import hla.rti1516.RequestForTimeRegulationPending;
import hla.rti1516.TimeConstrainedAlreadyEnabled;
import hla.rti1516.TimeConstrainedIsNotEnabled;
import hla.rti1516.TimeQueryReturn;
import hla.rti1516.TimeRegulationAlreadyEnabled;
import hla.rti1516.TimeRegulationIsNotEnabled;

public class TimeManager
{
  protected Federate federate;

  protected ReadWriteLock timeLock = new ReentrantReadWriteLock(true);

  protected TemporalState temporalState = TIME_GRANTED;
  protected TimeRegulatingState timeRegulatingState = NOT_TIME_REGULATING;
  protected TimeConstrainedState timeConstrainedState = NOT_TIME_CONSTRAINED;

  protected LogicalTime time;
  protected LogicalTime galt;
  protected LogicalTime lits;

  protected LogicalTimeInterval lookahead;

  public TimeManager(Federate federate)
  {
    this.federate = federate;
  }

  public ReadWriteLock getTimeLock()
  {
    return timeLock;
  }

  public boolean isTimeRegulating()
  {
    return timeRegulatingState == TIME_REGULATING;
  }

  public boolean isTimeConstrained()
  {
    return timeConstrainedState == TIME_CONSTRAINED;
  }

  public boolean isTimeAdvancing()
  {
    return temporalState == TIME_ADVANCING;
  }

  public boolean isTimeGranted()
  {
    return temporalState == TIME_GRANTED;
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
      if (timeRegulatingState == TIME_REGULATING)
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

      timeRegulatingState = BECOMING_TIME_REGULATING;
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

      timeRegulatingState = NOT_TIME_REGULATING;
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
      if (timeConstrainedState == TIME_CONSTRAINED)
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

      timeConstrainedState = BECOMING_TIME_CONSTRAINED;
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

      timeConstrainedState = NOT_TIME_CONSTRAINED;

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

      // TODO: will need to send to peers as well

      temporalState = TIME_ADVANCING;

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

      temporalState = TIME_ADVANCING;

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
        federate.getRTISession().write(new TimeAdvanceRequest(time));

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      temporalState = TIME_ADVANCING;

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
        federate.getRTISession().write(new TimeAdvanceRequest(time));

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      temporalState = TIME_ADVANCING;

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
        federate.getRTISession().write(new TimeAdvanceRequest(time));

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      temporalState = TIME_ADVANCING;

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
      TimeQueryReturn timeQueryReturn = new TimeQueryReturn();
      timeQueryReturn.timeIsValid = true;
      timeQueryReturn.time = galt;

      return timeQueryReturn;
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
      return time;
    }
    finally
    {
      timeLock.readLock().unlock();
    }
  }

  public TimeQueryReturn queryLITS()
    throws RTIinternalError
  {
    timeLock.readLock().lock();
    try
    {
      TimeQueryReturn timeQueryReturn = new TimeQueryReturn();
      timeQueryReturn.timeIsValid = true;
      timeQueryReturn.time = lits;

      return timeQueryReturn;
    }
    finally
    {
      timeLock.readLock().unlock();
    }
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
    if (timeRegulatingState == BECOMING_TIME_REGULATING)
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
    if (timeConstrainedState == BECOMING_TIME_CONSTRAINED)
    {
      throw new RequestForTimeConstrainedPending();
    }
  }

  public void checkIfLogicalTimeAlreadyPassed(LogicalTime time)
    throws LogicalTimeAlreadyPassed
  {
    if (time.compareTo(this.time) <= 0)
    {
      throw new LogicalTimeAlreadyPassed(
        String.format("%s <= %s", time, this.time));
    }
  }

  public void checkIfInvalidLogicalTime(LogicalTime time)
    throws InvalidLogicalTime
  {
  }

  public void checkIfInvalidLookahead(LogicalTimeInterval lookahead)
    throws InvalidLookahead
  {
  }
}

