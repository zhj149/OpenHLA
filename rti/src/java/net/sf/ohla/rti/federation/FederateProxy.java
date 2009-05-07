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

import net.sf.ohla.rti.federate.TimeAdvanceType;
import net.sf.ohla.rti.messages.FederateRestoreComplete;
import net.sf.ohla.rti.messages.FederateRestoreNotComplete;
import net.sf.ohla.rti.messages.FederateSaveBegun;
import net.sf.ohla.rti.messages.FederateSaveComplete;
import net.sf.ohla.rti.messages.FederateSaveInitiated;
import net.sf.ohla.rti.messages.FederateSaveInitiatedFailed;
import net.sf.ohla.rti.messages.FederateSaveNotComplete;
import net.sf.ohla.rti.messages.GALTAdvanced;
import net.sf.ohla.rti.messages.GALTUndefined;
import net.sf.ohla.rti.messages.RequestAttributeValueUpdate;
import net.sf.ohla.rti.messages.Retract;
import net.sf.ohla.rti.messages.SendInteraction;
import net.sf.ohla.rti.messages.UpdateAttributeValues;
import net.sf.ohla.rti.messages.callbacks.AnnounceSynchronizationPoint;
import net.sf.ohla.rti.messages.callbacks.DiscoverObjectInstance;
import net.sf.ohla.rti.messages.callbacks.FederationNotSaved;
import net.sf.ohla.rti.messages.callbacks.FederationSaved;
import net.sf.ohla.rti.messages.callbacks.InitiateFederateSave;
import net.sf.ohla.rti.messages.callbacks.RemoveObjectInstance;
import net.sf.ohla.rti.messages.callbacks.TimeAdvanceGrant;
import net.sf.ohla.rti.messages.callbacks.TimeConstrainedEnabled;
import net.sf.ohla.rti.messages.callbacks.TimeRegulationEnabled;

import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import hla.rti1516.FederateHandle;
import hla.rti1516.IllegalTimeArithmetic;
import hla.rti1516.LogicalTime;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.ResignAction;
import hla.rti1516.RestoreStatus;
import hla.rti1516.SaveStatus;

public class FederateProxy
{
  private static final String FEDERATE_IO_FILTER = "FederateProxyIoFilter";

  protected final FederateHandle federateHandle;
  protected final String federateName;
  protected final IoSession session;
  protected final FederationExecution federationExecution;

  protected final LogicalTimeInterval zero;
  protected final LogicalTimeInterval epsilon;

  protected SaveStatus saveStatus = SaveStatus.NO_SAVE_IN_PROGRESS;
  protected RestoreStatus restoreStatus = RestoreStatus.NO_RESTORE_IN_PROGRESS;

  protected boolean timeRegulationEnabled;

  protected boolean timeConstrainedEnabled;
  protected boolean timeConstrainedPending;

  protected LogicalTime federateTime;
  protected LogicalTimeInterval lookahead;

  protected LogicalTime advanceRequestTime;
  protected TimeAdvanceType advanceRequestType = TimeAdvanceType.NONE;

  protected LogicalTime galt;
  protected LogicalTime lits;

  /**
   * Least Outgoing Time Stamp.
   */
  protected LogicalTime lots;

  protected final Logger log = LoggerFactory.getLogger(getClass());
  protected final Marker marker;

  public FederateProxy(
    FederateHandle federateHandle, String federateName, IoSession session,
    FederationExecution federationExecution, LogicalTime galt)
  {
    this.federateHandle = federateHandle;
    this.federateName = federateName;
    this.session = session;
    this.federationExecution = federationExecution;
    this.galt = galt;

    zero = federationExecution.getTimeManager().getMobileFederateServices().
      intervalFactory.makeZero();
    epsilon = federationExecution.getTimeManager().getMobileFederateServices().
      intervalFactory.makeEpsilon();

    federateTime = federationExecution.getTimeManager().
      getMobileFederateServices().timeFactory.makeInitial();

    session.getFilterChain().addLast(
      FEDERATE_IO_FILTER, new FederateProxyIoFilter(this, federationExecution));

    marker = MarkerFactory.getMarker(federateName);

    log.debug(marker, "joined: {}", federateName);
  }

  public FederateHandle getFederateHandle()
  {
    return federateHandle;
  }

  public String getFederateName()
  {
    return federateName;
  }

  public IoSession getSession()
  {
    return session;
  }

  public SaveStatus getSaveStatus()
  {
    return saveStatus;
  }

  public RestoreStatus getRestoreStatus()
  {
    return restoreStatus;
  }

  public LogicalTime getFederateTime()
  {
    return federateTime;
  }

  public LogicalTimeInterval getLookahead()
  {
    return lookahead;
  }

  public void modifyLookahead(LogicalTimeInterval lookahead)
  {
    this.lookahead = lookahead;
  }

  public LogicalTime getAdvanceRequestTime()
  {
    return advanceRequestTime;
  }

  public TimeAdvanceType getAdvanceRequestType()
  {
    return advanceRequestType;
  }

  public LogicalTime getGALT()
  {
    return galt;
  }

  public LogicalTime getLOTS()
  {
    return lots;
  }

  public void resignFederationExecution(ResignAction resignAction)
  {
    session.getFilterChain().remove(FEDERATE_IO_FILTER);

    log.debug(marker, "resigned: {}", resignAction);
  }

  public WriteFuture announceSynchronizationPoint(
    AnnounceSynchronizationPoint announceSynchronizationPoint)
  {
    return session.write(announceSynchronizationPoint);
  }

  public WriteFuture initiateFederateSave(
    InitiateFederateSave initiateFederateSave)
  {
    saveStatus = SaveStatus.FEDERATE_INSTRUCTED_TO_SAVE;

    return session.write(initiateFederateSave);
  }

  public void federateSaveInitiated(FederateSaveInitiated federateSaveInitiated)
  {
  }

  public void federateSaveInitiatedFailed(
    FederateSaveInitiatedFailed federateSaveInitiatedFailed)
  {
    saveStatus = SaveStatus.NO_SAVE_IN_PROGRESS;
  }

  public void federateSaveBegun(FederateSaveBegun federateSaveBegun)
  {
    saveStatus = SaveStatus.FEDERATE_SAVING;
  }

  public void federateSaveComplete(FederateSaveComplete federateSaveComplete)
  {
    saveStatus = SaveStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_SAVE;
  }

  public void federateSaveNotComplete(
    FederateSaveNotComplete federateSaveNotComplete)
  {
    saveStatus = SaveStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_SAVE;
  }

  public WriteFuture federationSaved(FederationSaved federationSaved)
  {
    saveStatus = SaveStatus.NO_SAVE_IN_PROGRESS;

    return session.write(federationSaved);
  }

  public WriteFuture federationNotSaved(FederationNotSaved federationNotSaved)
  {
    saveStatus = SaveStatus.NO_SAVE_IN_PROGRESS;

    return session.write(federationNotSaved);
  }

  public void federateRestoreComplete(
    FederateRestoreComplete federateRestoreComplete)
  {
    restoreStatus = RestoreStatus.NO_RESTORE_IN_PROGRESS;
  }

  public void federateRestoreNotComplete(
    FederateRestoreNotComplete federateRestoreNotComplete)
  {
    restoreStatus = RestoreStatus.NO_RESTORE_IN_PROGRESS;
  }

  public WriteFuture discoverObjectInstance(
    DiscoverObjectInstance discoverObjectInstance)
  {
    return session.write(discoverObjectInstance);
  }

  public WriteFuture reflectAttributeValues(
    UpdateAttributeValues updateAttributeValues)
  {
    return session.write(updateAttributeValues);
  }

  public WriteFuture receiveInteraction(SendInteraction sendInteraction)
  {
    return session.write(sendInteraction);
  }

  public WriteFuture removeObjectInstance(RemoveObjectInstance removeObjectInstance)
  {
    return session.write(removeObjectInstance);
  }

  public WriteFuture requestAttributeValueUpdate(
    RequestAttributeValueUpdate requestAttributeValueUpdate)
  {
    return session.write(requestAttributeValueUpdate);
  }

  public WriteFuture retract(Retract retract)
  {
    return session.write(retract);
  }

  public void enableTimeRegulation(
    LogicalTimeInterval lookahead, LogicalTime federateTime)
    throws IllegalTimeArithmetic
  {
    assert !timeConstrainedEnabled || galt == null ||
           federateTime.compareTo(galt) <= 0;

    this.lookahead = lookahead;
    this.federateTime = federateTime;

    advanceRequestType = TimeAdvanceType.TIME_ADVANCE_REQUEST_AVAILABLE;

    // set the Least Outgoing Time Stamp
    //
    lots = federateTime.add(lookahead);
  }

  public void timeRegulationEnabled()
  {
    timeRegulationEnabled = true;

    log.debug(marker, "time regulation enabled: {}", this);

    session.write(new TimeRegulationEnabled(federateTime));
  }

  public void disableTimeRegulation()
  {
    timeRegulationEnabled = false;

    lookahead = null;
    lots = null;

    log.debug(marker, "time regulation disabled: {}");
  }

  public void enableTimeConstrained()
  {
    if (galt == null || federateTime.compareTo(galt) <= 0)
    {
      // GALT is undefined or federate time <= GALT

      timeConstrainedEnabled = true;

      log.debug(marker, "time constrained enabled: {}", this);

      session.write(new TimeConstrainedEnabled(federateTime));
    }
    else
    {
      timeConstrainedPending = true;

      log.debug(marker, "enable time constrained pending: {}", this);
    }
  }

  public void disableTimeConstrained()
  {
    timeConstrainedEnabled = false;
    lits = null;

    log.debug(marker, "time constrained disabled: {}", this);
  }

  public void timeAdvanceRequest(LogicalTime time)
    throws IllegalTimeArithmetic
  {
    log.debug(marker, "time advance request: {}", time);

    advanceRequestTime = time;
    advanceRequestType = TimeAdvanceType.TIME_ADVANCE_REQUEST;

    if (timeRegulationEnabled)
    {
      lots = advanceRequestTime.add(lookahead.isZero() ? epsilon : lookahead);

      log.debug(marker, "LOTS: {}", lots);
    }

    if (!timeConstrainedEnabled || galt == null ||
        advanceRequestTime.compareTo(galt) < 0)
    {
      // immediately grant the request

      federateTime = advanceRequestTime;
      advanceRequestTime = null;

      session.write(new TimeAdvanceGrant(federateTime));
    }
  }

  public void timeAdvanceRequestAvailable(LogicalTime time)
    throws IllegalTimeArithmetic
  {
    log.debug(marker, "time advance request available: {}", time);

    advanceRequestTime = time;
    advanceRequestType = TimeAdvanceType.TIME_ADVANCE_REQUEST_AVAILABLE;

    if (timeRegulationEnabled)
    {
      lots = advanceRequestTime.add(lookahead);

      log.debug(marker, "LOTS: {}", lots);
    }

    if (!timeConstrainedEnabled || galt == null ||
        advanceRequestTime.compareTo(galt) <= 0)
    {
      // immediately grant the request

      federateTime = advanceRequestTime;
      advanceRequestTime = null;

      session.write(new TimeAdvanceGrant(federateTime));
    }
  }

  public void nextMessageRequest(LogicalTime time)
    throws IllegalTimeArithmetic
  {
    log.debug(marker, "next message request: {}", time);

    advanceRequestType = TimeAdvanceType.NEXT_MESSAGE_REQUEST;

    if (!timeConstrainedEnabled || galt == null)
    {
      assert lits == null;

      if (timeRegulationEnabled)
      {
        lots = time.add(lookahead.isZero() ? epsilon : lookahead);

        log.debug(marker, "LOTS: {}", lots);
      }

      // immediately grant the request

      federateTime = time;

      session.write(new TimeAdvanceGrant(federateTime));
    }
    else if (time.compareTo(galt) < 0)
    {
      // the specified time is < GALT

      // use LITS if it is defined and < the specified time
      //
      time = lits == null || time.compareTo(lits) < 0 ? time : lits;

      if (timeRegulationEnabled)
      {
        lots = time.add(lookahead.isZero() ? epsilon : lookahead);

        log.debug(marker, "LOTS: {}", lots);
      }

      // immediately grant the request

      federateTime = time;

      session.write(new TimeAdvanceGrant(federateTime));
    }
    else if (lits != null && lits.compareTo(galt) < 0)
    {
      // LITS < GALT

      if (timeRegulationEnabled)
      {
        lots = lits.add(lookahead.isZero() ? epsilon : lookahead);

        log.debug(marker, "LOTS: {}", lots);
      }

      // immediately grant the request

      federateTime = lits;

      session.write(new TimeAdvanceGrant(federateTime));
    }
    else
    {
      advanceRequestTime = time;

      if (timeRegulationEnabled)
      {
        // LOTS can only advance as GALT advances

        lots = galt.add(lookahead.isZero() ? epsilon : lookahead);

        log.debug(marker, "LOTS: {}", lots);
      }
    }
  }

  public void nextMessageRequestAvailable(LogicalTime time)
    throws IllegalTimeArithmetic
  {
    log.debug(marker, "next message request available: {}", time);

    advanceRequestType = TimeAdvanceType.NEXT_MESSAGE_REQUEST_AVAILABLE;

    if (!timeConstrainedEnabled || galt == null)
    {
      assert lits == null;

      if (timeRegulationEnabled)
      {
        lots = time.add(lookahead);

        log.debug(marker, "LOTS: {}", lots);
      }

      // immediately grant the request

      federateTime = time;

      session.write(new TimeAdvanceGrant(federateTime));
    }
    else if (time.compareTo(galt) <= 0)
    {
      // the specified time is < GALT

      // use LITS if it is defined and < the specified time
      //
      time = lits == null || time.compareTo(lits) < 0 ? time : lits;

      if (timeRegulationEnabled)
      {
        lots = time.add(lookahead);

        log.debug(marker, "LOTS: {}", lots);
      }

      // immediately grant the request

      federateTime = time;

      session.write(new TimeAdvanceGrant(federateTime));
    }
    else if (lits != null && lits.compareTo(galt) <= 0)
    {
      // LITS < GALT

      if (timeRegulationEnabled)
      {
        lots = lits.add(lookahead);

        log.debug(marker, "LOTS: {}", lots);
      }

      // immediately grant the request

      federateTime = lits;

      session.write(new TimeAdvanceGrant(federateTime));
    }
    else
    {
      advanceRequestTime = time;

      if (timeRegulationEnabled)
      {
        // LOTS can only advance as GALT advances

        lots = galt.add(lookahead);

        log.debug(marker, "LOTS: {}", lots);
      }
    }
  }

  public void flushQueueRequest(LogicalTime time)
    throws IllegalTimeArithmetic
  {
    log.debug(marker, "flush queue request: {}", time);

    advanceRequestType = TimeAdvanceType.FLUSH_QUEUE_REQUEST;

    federateTime = galt == null || time.compareTo(galt) < 0 ? time : galt;

    if (timeRegulationEnabled)
    {
      lots = federateTime.add(lookahead);

      log.debug(marker, "LOTS: {}", lots);
    }

    session.write(new TimeAdvanceGrant(federateTime));
  }

  public void galtAdvanced(LogicalTime galt)
    throws IllegalTimeArithmetic
  {
    if (this.galt == null)
    {
      log.debug(marker, "GALT defined: {}", galt);
    }
    else
    {
      assert this.galt.compareTo(galt) < 0;

      log.debug(marker, "GALT advanced: {} to {}", this.galt, galt);
    }

    this.galt = galt;

    session.write(new GALTAdvanced(galt));

    if (timeConstrainedPending && federateTime.compareTo(galt) < 0)
    {
      timeConstrainedPending = false;
      timeConstrainedEnabled = true;

      log.debug(marker, "time constrained enabled: {}", federateTime);

      session.write(new TimeConstrainedEnabled(federateTime));
    }
    else if (advanceRequestTime != null)
    {
      switch (advanceRequestType)
      {
        case TIME_ADVANCE_REQUEST:
        {
          if (advanceRequestTime.compareTo(galt) < 0)
          {
            federateTime = advanceRequestTime;

            advanceRequestTime = null;
            lits = null;

            session.write(new TimeAdvanceGrant(federateTime));
          }
          break;
        }
        case TIME_ADVANCE_REQUEST_AVAILABLE:
        {
          if (advanceRequestTime.compareTo(galt) <= 0)
          {
            federateTime = advanceRequestTime;

            advanceRequestTime = null;
            lits = null;

            session.write(new TimeAdvanceGrant(federateTime));
          }
          break;
        }
        case NEXT_MESSAGE_REQUEST:
        {
          if (advanceRequestTime.compareTo(galt) < 0)
          {
            if (lits == null || advanceRequestTime.compareTo(lits) < 0)
            {
              federateTime = advanceRequestTime;
            }
            else
            {
              federateTime = lits;
            }

            advanceRequestTime = null;
            lits = null;

            session.write(new TimeAdvanceGrant(federateTime));
          }
          else if (lits.compareTo(galt) < 0)
          {
            federateTime = lits;
            lits = null;

            advanceRequestTime = null;
            lits = null;

            session.write(new TimeAdvanceGrant(federateTime));
          }
          break;
        }
        case NEXT_MESSAGE_REQUEST_AVAILABLE:
        {
          if (advanceRequestTime.compareTo(galt) <= 0)
          {
            if (lits == null || advanceRequestTime.compareTo(lits) < 0)
            {
              federateTime = advanceRequestTime;
            }
            else
            {
              federateTime = lits;
            }

            advanceRequestTime = null;
            lits = null;

            session.write(new TimeAdvanceGrant(federateTime));
          }
          else if (lits.compareTo(galt) <= 0)
          {
            federateTime = lits;

            advanceRequestTime = null;
            lits = null;

            session.write(new TimeAdvanceGrant(federateTime));
          }
          break;
        }
        default:
          assert false;
      }
    }
  }

  public void galtUndefined()
  {
    galt = null;
    lits = null;

    session.write(new GALTUndefined());
  }

  public void updateLITS(LogicalTime time)
  {
    federationExecution.getTimeManager().getTimeLock().writeLock().lock();
    try
    {
      if (timeConstrainedEnabled)
      {
        if (lits == null || time.compareTo(lits) < 0)
        {
          lits = time;

          log.trace(marker, "LITS: {}", lits);
        }
      }
    }
    finally
    {
      federationExecution.getTimeManager().getTimeLock().writeLock().unlock();
    }
  }

  @Override
  public int hashCode()
  {
    return federateHandle.hashCode();
  }

  @Override
  public boolean equals(Object rhs)
  {
    return this == rhs ||
           (rhs instanceof FederateProxy &&
            federateHandle.equals(((FederateProxy) rhs).federateHandle));
  }

  @Override
  public String toString()
  {
    return String.format("%s,%s,%s", federateHandle,
                         session.getLocalAddress(), federateName);
  }
}
