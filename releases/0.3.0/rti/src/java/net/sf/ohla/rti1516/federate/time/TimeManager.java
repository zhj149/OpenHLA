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
  protected TimeRegulatingState timeRegulatingState = TimeRegulatingState.NOT_TIME_REGULATING;
  protected TimeConstrainedState timeConstrainedState = NOT_TIME_CONSTRAINED;

  protected LogicalTime federateTime;
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

  public synchronized boolean isTimeRegulating()
  {
    return timeRegulatingState.equals(TimeRegulatingState.TIME_REGULATING);
  }

  public synchronized boolean isTimeConstrained()
  {
    return timeConstrainedState.equals(TIME_CONSTRAINED);
  }

  public synchronized boolean isTimeAdvancing()
  {
    return temporalState.equals(TIME_ADVANCING);
  }

  public synchronized boolean isTimeGranted()
  {
    return temporalState.equals(TIME_GRANTED);
  }

  public synchronized boolean isTimeConstrainedAndTimeGranted()
  {
    return isTimeConstrained() && isTimeGranted();
  }

  public synchronized void enableTimeRegulation(LogicalTimeInterval lookahead)
    throws TimeRegulationAlreadyEnabled, InvalidLookahead, InTimeAdvancingState,
           RequestForTimeRegulationPending
  {
    checkIfInTimeAdvancingState();

    if (timeRegulatingState.equals(TimeRegulatingState.TIME_REGULATING))
    {
      throw new TimeRegulationAlreadyEnabled();
    }

    // TODO: validate lookahead

    checkIfRequestForTimeRegulationPending();

    timeRegulatingState = TimeRegulatingState.BECOMING_TIME_REGULATING;
  }

  public synchronized void disableTimeRegulation()
    throws TimeRegulationIsNotEnabled
  {
    checkIfTimeRegulationIsNotEnabled();

    timeRegulatingState = TimeRegulatingState.NOT_TIME_REGULATING;
  }

  public synchronized void enableTimeConstrained()
    throws TimeConstrainedAlreadyEnabled, InTimeAdvancingState,
           RequestForTimeConstrainedPending, RTIinternalError
  {
    checkIfInTimeAdvancingState();

    if (timeConstrainedState.equals(TIME_CONSTRAINED))
    {
      throw new TimeConstrainedAlreadyEnabled();
    }

    checkIfRequestForTimeConstrainedPending();

    timeConstrainedState = BECOMING_TIME_CONSTRAINED;
  }

  public synchronized void disableTimeConstrained()
    throws TimeConstrainedIsNotEnabled, RTIinternalError
  {
    checkIfTimeConstrainedIsNotEnabled();

    timeConstrainedState = NOT_TIME_CONSTRAINED;
  }

  public synchronized void timeAdvanceRequest(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           RTIinternalError
  {
    checkIfLogicalTimeAlreadyPassed(time);
    checkIfInTimeAdvancingState();
    checkIfRequestForTimeRegulationPending();
    checkIfRequestForTimeConstrainedPending();

    temporalState = TIME_ADVANCING;
  }

  public synchronized void timeAdvanceRequestAvailable(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           RTIinternalError
  {
    checkIfLogicalTimeAlreadyPassed(time);
    checkIfInTimeAdvancingState();
    checkIfRequestForTimeRegulationPending();
    checkIfRequestForTimeConstrainedPending();

    temporalState = TIME_ADVANCING;
  }

  public synchronized void nextMessageRequest(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           RTIinternalError
  {
    checkIfLogicalTimeAlreadyPassed(time);
    checkIfInTimeAdvancingState();
    checkIfRequestForTimeRegulationPending();
    checkIfRequestForTimeConstrainedPending();

    temporalState = TIME_ADVANCING;
  }

  public synchronized void nextMessageRequestAvailable(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           RTIinternalError
  {
    checkIfLogicalTimeAlreadyPassed(time);
    checkIfInTimeAdvancingState();
    checkIfRequestForTimeConstrainedPending();
    checkIfRequestForTimeRegulationPending();

    temporalState = TIME_ADVANCING;
  }

  public synchronized void flushQueueRequest(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           RTIinternalError
  {
    checkIfLogicalTimeAlreadyPassed(time);
    checkIfInTimeAdvancingState();
    checkIfRequestForTimeRegulationPending();
    checkIfRequestForTimeConstrainedPending();

    temporalState = TIME_ADVANCING;
  }

  public synchronized TimeQueryReturn queryGALT()
    throws RTIinternalError
  {
    TimeQueryReturn timeQueryReturn = new TimeQueryReturn();
    timeQueryReturn.timeIsValid = true;
    timeQueryReturn.time = galt;

    return timeQueryReturn;
  }

  public synchronized LogicalTime queryLogicalTime()
    throws RTIinternalError
  {
    return federateTime;
  }

  public synchronized TimeQueryReturn queryLITS()
    throws RTIinternalError
  {
    TimeQueryReturn timeQueryReturn = new TimeQueryReturn();
    timeQueryReturn.timeIsValid = true;
    timeQueryReturn.time = lits;

    return timeQueryReturn;
  }

  public synchronized void modifyLookahead(LogicalTimeInterval lookahead)
    throws TimeRegulationIsNotEnabled, InvalidLookahead, InTimeAdvancingState,
           RTIinternalError
  {
    checkIfTimeRegulationIsNotEnabled();
    checkIfInTimeAdvancingState();
  }

  public synchronized LogicalTimeInterval queryLookahead()
    throws TimeRegulationIsNotEnabled, RTIinternalError
  {
    checkIfTimeRegulationIsNotEnabled();

    return lookahead;
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
    if (!timeRegulatingState.equals(TimeRegulatingState.BECOMING_TIME_REGULATING))
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
    if (timeConstrainedState.equals(BECOMING_TIME_CONSTRAINED))
    {
      throw new RequestForTimeConstrainedPending();
    }
  }

  public void checkIfLogicalTimeAlreadyPassed(LogicalTime time)
    throws LogicalTimeAlreadyPassed
  {
    if (time.compareTo(federateTime) <= 0)
    {
      throw new LogicalTimeAlreadyPassed(
        String.format("%s <= %s", time, federateTime));
    }
  }

  public void checkIfInvalidLogicalTime(LogicalTime time)
  {
  }
}

