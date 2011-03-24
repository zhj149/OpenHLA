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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.RTIChannelUpstreamHandler;
import net.sf.ohla.rti.fdd.InteractionClass;
import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.federate.Federate;
import net.sf.ohla.rti.federate.TimeAdvanceType;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleSetFactory;
import net.sf.ohla.rti.i18n.I18nLogger;
import net.sf.ohla.rti.i18n.LogMessages;
import net.sf.ohla.rti.messages.DeleteObjectInstance;
import net.sf.ohla.rti.messages.FederateRestoreComplete;
import net.sf.ohla.rti.messages.FederateRestoreNotComplete;
import net.sf.ohla.rti.messages.FederateSaveBegun;
import net.sf.ohla.rti.messages.FederateSaveComplete;
import net.sf.ohla.rti.messages.FederateSaveNotComplete;
import net.sf.ohla.rti.messages.GALTAdvanced;
import net.sf.ohla.rti.messages.GALTUndefined;
import net.sf.ohla.rti.messages.MessageDecoder;
import net.sf.ohla.rti.messages.PublishObjectClassAttributes;
import net.sf.ohla.rti.messages.QueryInteractionTransportationType;
import net.sf.ohla.rti.messages.Retract;
import net.sf.ohla.rti.messages.SendInteraction;
import net.sf.ohla.rti.messages.SubscribeInteractionClass;
import net.sf.ohla.rti.messages.SubscribeInteractionClassWithRegions;
import net.sf.ohla.rti.messages.SubscribeObjectClassAttributes;
import net.sf.ohla.rti.messages.SubscribeObjectClassAttributesWithRegions;
import net.sf.ohla.rti.messages.UnsubscribeInteractionClass;
import net.sf.ohla.rti.messages.UnsubscribeInteractionClassWithRegions;
import net.sf.ohla.rti.messages.UnsubscribeObjectClassAttributes;
import net.sf.ohla.rti.messages.UnsubscribeObjectClassAttributesWithRegions;
import net.sf.ohla.rti.messages.UpdateAttributeValues;
import net.sf.ohla.rti.messages.callbacks.AnnounceSynchronizationPoint;
import net.sf.ohla.rti.messages.callbacks.DiscoverObjectInstance;
import net.sf.ohla.rti.messages.callbacks.FederationNotSaved;
import net.sf.ohla.rti.messages.callbacks.FederationSaved;
import net.sf.ohla.rti.messages.callbacks.InitiateFederateSave;
import net.sf.ohla.rti.messages.callbacks.ProvideAttributeValueUpdate;
import net.sf.ohla.rti.messages.callbacks.ReceiveInteraction;
import net.sf.ohla.rti.messages.callbacks.ReflectAttributeValues;
import net.sf.ohla.rti.messages.callbacks.RemoveObjectInstance;
import net.sf.ohla.rti.messages.callbacks.ReportInteractionTransportationType;
import net.sf.ohla.rti.messages.callbacks.RequestAttributeOwnershipAssumption;
import net.sf.ohla.rti.messages.callbacks.TimeAdvanceGrant;
import net.sf.ohla.rti.messages.callbacks.TimeConstrainedEnabled;
import net.sf.ohla.rti.messages.callbacks.TimeRegulationEnabled;

import org.jboss.netty.channel.Channel;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeInterval;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RestoreStatus;
import hla.rti1516e.SaveStatus;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.IllegalTimeArithmetic;
import hla.rti1516e.exceptions.InvalidLogicalTimeInterval;

public class FederateProxy
{
  private final FederationExecution federationExecution;
  private final FederateHandle federateHandle;
  private final String federateName;
  private final String federateType;

  /**
   * The {@code Channel} back to the Federate.
   */
  private final Channel federateChannel;

  private final ReadWriteLock publicationLock = new ReentrantReadWriteLock(true);
  private final Map<ObjectClassHandle, AttributeHandleSet> publishedObjectClasses =
    new HashMap<ObjectClassHandle, AttributeHandleSet>();

  private final ReadWriteLock subscriptionLock = new ReentrantReadWriteLock(true);
  private final FederateProxySubscriptionManager subscriptionManager = new FederateProxySubscriptionManager();

  private final Set<ObjectInstanceHandle> discoveredObjects =
    Collections.synchronizedSet(new HashSet<ObjectInstanceHandle>());

  private final Map<InteractionClassHandle, TransportationTypeHandle> interactionClassTransportationTypeHandles =
    new HashMap<InteractionClassHandle, TransportationTypeHandle>();

  private final LogicalTimeInterval zero;
  private final LogicalTimeInterval epsilon;

  private SaveStatus saveStatus = SaveStatus.NO_SAVE_IN_PROGRESS;
  private RestoreStatus restoreStatus = RestoreStatus.NO_RESTORE_IN_PROGRESS;

  private boolean timeRegulationEnabled;

  private boolean timeConstrainedEnabled;
  private boolean timeConstrainedPending;

  private LogicalTime federateTime;
  private LogicalTimeInterval lookahead;

  private LogicalTime advanceRequestTime;
  private TimeAdvanceType advanceRequestType = TimeAdvanceType.NONE;

  private LogicalTime galt;
  private LogicalTime lits;

  /**
   * Least Outgoing Time Stamp.
   */
  private LogicalTime lots;

  private boolean conveyRegionDesignatorSets = true;

  private final I18nLogger log;

  public FederateProxy(
    FederationExecution federationExecution, FederateHandle federateHandle, String federateName, String federateType,
    Channel federateChannel, LogicalTime galt)
  {
    this.federationExecution = federationExecution;
    this.federateHandle = federateHandle;
    this.federateName = federateName == null ? Federate.defaultFederateName(federateHandle) : federateName;
    this.federateType = federateType;
    this.federateChannel = federateChannel;
    this.galt = galt;

    zero = federationExecution.getTimeManager().getLogicalTimeFactory().makeZero();
    epsilon = federationExecution.getTimeManager().getLogicalTimeFactory().makeEpsilon();

    federateTime = federationExecution.getTimeManager().getLogicalTimeFactory().makeInitial();

    federateChannel.getPipeline().addBefore(
      RTIChannelUpstreamHandler.NAME, FederateProxyChannelHandler.NAME, new FederateProxyChannelHandler(this));

    ((MessageDecoder) federateChannel.getPipeline().get(MessageDecoder.NAME)).setLogicalTimeFactory(
      federationExecution.getTimeManager().getLogicalTimeFactory());

    Marker marker = MarkerFactory.getMarker(federationExecution.getName() + "." + this.federateName);
    log = I18nLogger.getLogger(marker, FederateProxy.class);

    log.debug(LogMessages.FEDERATE_JOINED);
  }

  public FederationExecution getFederationExecution()
  {
    return federationExecution;
  }

  public FederateHandle getFederateHandle()
  {
    return federateHandle;
  }

  public String getFederateName()
  {
    return federateName;
  }

  public String getFederateType()
  {
    return federateType;
  }

  public Channel getFederateChannel()
  {
    return federateChannel;
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

  public boolean isConveyRegionDesignatorSets()
  {
    return conveyRegionDesignatorSets;
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
    federateChannel.getPipeline().remove(FederateProxyChannelHandler.NAME);

    log.debug(LogMessages.FEDERATE_RESIGNED, resignAction);
  }

  public void announceSynchronizationPoint(AnnounceSynchronizationPoint announceSynchronizationPoint)
  {
    federateChannel.write(announceSynchronizationPoint);
  }

  public void initiateFederateSave(InitiateFederateSave initiateFederateSave)
  {
    saveStatus = SaveStatus.FEDERATE_INSTRUCTED_TO_SAVE;

    federateChannel.write(initiateFederateSave);
  }

  public void federateSaveInitiatedFailed()
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

  public void federateSaveNotComplete(FederateSaveNotComplete federateSaveNotComplete)
  {
    saveStatus = SaveStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_SAVE;
  }

  public void federationSaved(FederationSaved federationSaved)
  {
    saveStatus = SaveStatus.NO_SAVE_IN_PROGRESS;

    federateChannel.write(federationSaved);
  }

  public void federationNotSaved(FederationNotSaved federationNotSaved)
  {
    saveStatus = SaveStatus.NO_SAVE_IN_PROGRESS;

    federateChannel.write(federationNotSaved);
  }

  public void federateRestoreComplete(FederateRestoreComplete federateRestoreComplete)
  {
    restoreStatus = RestoreStatus.NO_RESTORE_IN_PROGRESS;
  }

  public void federateRestoreNotComplete(FederateRestoreNotComplete federateRestoreNotComplete)
  {
    restoreStatus = RestoreStatus.NO_RESTORE_IN_PROGRESS;
  }

  public void rediscoverObjectInstance(FederationExecutionObjectInstance objectInstance)
  {
    // this is triggered by a local delete object instance, of which only one thread can change at a time

    DiscoverObjectInstance discoverObjectInstance = subscriptionManager.discoverObjectInstance(objectInstance);
    if (discoverObjectInstance != null)
    {
      federateChannel.write(discoverObjectInstance);
    }
  }

  public void discoverObjectInstance(FederationExecutionObjectInstance objectInstance)
  {
    // this is triggered by a subscription change, of which only one thread can change at a time

    if (discoveredObjects.add(objectInstance.getObjectInstanceHandle()))
    {
      federateChannel.write(new DiscoverObjectInstance(
        objectInstance.getObjectInstanceHandle(), objectInstance.getObjectClass().getObjectClassHandle(),
        objectInstance.getObjectInstanceName(), objectInstance.getProducingFederateHandle()));
    }
  }

  public void discoverObjectInstance(FederateProxy federateProxy, FederationExecutionObjectInstance objectInstance)
  {
    subscriptionLock.readLock().lock();
    try
    {
      DiscoverObjectInstance discoverObjectInstance = subscriptionManager.discoverObjectInstance(objectInstance);
      if (discoverObjectInstance != null)
      {
        discoveredObjects.add(discoverObjectInstance.getObjectInstanceHandle());

        federateChannel.write(discoverObjectInstance);
      }
    }
    finally
    {
      subscriptionLock.readLock().unlock();
    }
  }

  public void reflectAttributeValues(
    FederateProxy federateProxy, FederationExecutionObjectInstance objectInstance,
    UpdateAttributeValues updateAttributeValues)
  {
    ReflectAttributeValues reflectAttributeValues;

    subscriptionLock.readLock().lock();
    try
    {
      reflectAttributeValues = subscriptionManager.reflectAttributeValues(
        federateProxy, objectInstance, updateAttributeValues);
    }
    finally
    {
      subscriptionLock.readLock().unlock();
    }

    if (reflectAttributeValues != null)
    {
      if (reflectAttributeValues.getSentOrderType() == OrderType.TIMESTAMP)
      {
        updateLITS(reflectAttributeValues.getTime());
      }

      federateChannel.write(reflectAttributeValues);
    }
  }

  public void receiveInteraction(
    FederateProxy federateProxy, InteractionClass interactionClass, SendInteraction sendInteraction)
  {
    ReceiveInteraction receiveInteraction;

    subscriptionLock.readLock().lock();
    try
    {
      receiveInteraction = subscriptionManager.receiveInteraction(federateProxy, interactionClass, sendInteraction);
    }
    finally
    {
      subscriptionLock.readLock().unlock();
    }

    if (receiveInteraction != null)
    {
      if (receiveInteraction.getSentOrderType() == OrderType.TIMESTAMP)
      {
        updateLITS(receiveInteraction.getTime());
      }

      federateChannel.write(receiveInteraction);
    }
  }

  public void removeObjectInstance(ObjectInstanceHandle objectInstanceHandle, FederateHandle federateHandle)
  {
    if (discoveredObjects.contains(objectInstanceHandle))
    {
      federateChannel.write(new RemoveObjectInstance(objectInstanceHandle, federateHandle));
    }
  }

  public void removeObjectInstance(DeleteObjectInstance deleteObjectInstance, FederateHandle federateHandle)
  {
    if (discoveredObjects.contains(deleteObjectInstance.getObjectInstanceHandle()))
    {
      federateChannel.write(new RemoveObjectInstance(
        deleteObjectInstance.getObjectInstanceHandle(), deleteObjectInstance.getTag(),
        deleteObjectInstance.getSentOrderType(), deleteObjectInstance.getTime(),
        deleteObjectInstance.getMessageRetractionHandle(), federateHandle));
    }
  }

  public void provideAttributeValueUpdate(ProvideAttributeValueUpdate provideAttributeValueUpdate)
  {
    federateChannel.write(provideAttributeValueUpdate);
  }

  public void retract(Retract retract)
  {
    federateChannel.write(retract);
  }

  public void negotiatedAttributeOwnershipDivestiture(
    FederationExecutionObjectInstance objectInstance, AttributeHandleSet attributeHandles, byte[] tag)
  {
    publicationLock.readLock().lock();
    try
    {
      AttributeHandleSet candidateAttributeHandles = null;

      // search the whole object class hierarchy for any published attributes
      //
      ObjectClass objectClass = objectInstance.getObjectClass();
      do
      {
        AttributeHandleSet publishedAttributeHandles = publishedObjectClasses.get(objectClass.getObjectClassHandle());
        if (publishedAttributeHandles != null)
        {
          for (AttributeHandle attributeHandle : attributeHandles)
          {
            if (publishedAttributeHandles.contains(attributeHandle))
            {
              if (candidateAttributeHandles == null)
              {
                candidateAttributeHandles = IEEE1516eAttributeHandleSetFactory.INSTANCE.create();
              }
              candidateAttributeHandles.add(attributeHandle);
            }
          }
        }
      } while ((objectClass = objectClass.getSuperObjectClass()) != null);

      if (candidateAttributeHandles != null)
      {
        federateChannel.write(new RequestAttributeOwnershipAssumption(
          objectInstance.getObjectInstanceHandle(), candidateAttributeHandles, tag));
      }
    }
    finally
    {
      publicationLock.readLock().unlock();
    }
  }

  public void requestAttributeOwnershipAssumption(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
  {
    // this should only be called when a federate updates its publishing object classes and there is an object with an
    // attribute that is willing to be divested

    federateChannel.write(new RequestAttributeOwnershipAssumption(objectInstanceHandle, attributeHandles, tag));
  }

  public void publishObjectClassAttributes(PublishObjectClassAttributes publishObjectClassAttributes)
  {
    ObjectClassHandle objectClassHandle = publishObjectClassAttributes.getObjectClassHandle();

    publicationLock.writeLock().lock();
    try
    {
      AttributeHandleSet publishedAttributeHandles = publishedObjectClasses.get(objectClassHandle);
      if (publishedAttributeHandles == null)
      {
        publishedObjectClasses.put(objectClassHandle, publishObjectClassAttributes.getAttributeHandles());
      }
      else
      {
        publishedAttributeHandles.addAll(publishObjectClassAttributes.getAttributeHandles());
      }
    }
    finally
    {
      publicationLock.writeLock().unlock();
    }
  }

  public void subscribeObjectClassAttributes(SubscribeObjectClassAttributes subscribeObjectClassAttributes)
  {
    subscriptionLock.writeLock().lock();
    try
    {
      subscriptionManager.subscribeObjectClassAttributes(
        federationExecution.getFDD().getObjectClassSafely(subscribeObjectClassAttributes.getObjectClassHandle()),
        subscribeObjectClassAttributes.getAttributeHandles(), subscribeObjectClassAttributes.isPassive());
    }
    finally
    {
      subscriptionLock.writeLock().unlock();
    }
  }

  public void unsubscribeObjectClassAttributes(UnsubscribeObjectClassAttributes unsubscribeObjectClassAttributes)
  {
    subscriptionLock.writeLock().lock();
    try
    {
      subscriptionManager.unsubscribeObjectClassAttributes(
        unsubscribeObjectClassAttributes.getObjectClassHandle(), unsubscribeObjectClassAttributes.getAttributeHandles());
    }
    finally
    {
      subscriptionLock.writeLock().unlock();
    }
  }

  public void subscribeObjectClassAttributesWithRegions(
    SubscribeObjectClassAttributesWithRegions subscribeObjectClassAttributesWithRegions)
  {
    subscriptionLock.writeLock().lock();
    try
    {
      subscriptionManager.subscribeObjectClassAttributes(
        federationExecution.getFDD().getObjectClassSafely(subscribeObjectClassAttributesWithRegions.getObjectClassHandle()),
        subscribeObjectClassAttributesWithRegions.getAttributesAndRegions(),
        subscribeObjectClassAttributesWithRegions.isPassive());
    }
    finally
    {
      subscriptionLock.writeLock().unlock();
    }
  }

  public void unsubscribeObjectClassAttributesWithRegions(
    UnsubscribeObjectClassAttributesWithRegions unsubscribeObjectClassAttributesWithRegions)
  {
    subscriptionLock.writeLock().lock();
    try
    {
      subscriptionManager.unsubscribeObjectClassAttributes(
        unsubscribeObjectClassAttributesWithRegions.getObjectClassHandle(),
        unsubscribeObjectClassAttributesWithRegions.getAttributesAndRegions());
    }
    finally
    {
      subscriptionLock.writeLock().unlock();
    }
  }

  public void subscribeInteractionClass(SubscribeInteractionClass subscribeInteractionClass)
  {
    subscriptionLock.writeLock().lock();
    try
    {
      subscriptionManager.subscribeInteractionClass(
        federationExecution.getFDD().getInteractionClassSafely(subscribeInteractionClass.getInteractionClassHandle()),
        subscribeInteractionClass.isPassive());
    }
    finally
    {
      subscriptionLock.writeLock().unlock();
    }
  }

  public void unsubscribeInteractionClass(UnsubscribeInteractionClass unsubscribeInteractionClass)
  {
    subscriptionLock.writeLock().lock();
    try
    {
      subscriptionManager.unsubscribeInteractionClass(unsubscribeInteractionClass.getInteractionClassHandle());
    }
    finally
    {
      subscriptionLock.writeLock().unlock();
    }
  }

  public void subscribeInteractionClassWithRegions(
    SubscribeInteractionClassWithRegions subscribeInteractionClassWithRegions)
  {
    subscriptionLock.writeLock().lock();
    try
    {
      subscriptionManager.subscribeInteractionClass(
        federationExecution.getFDD().getInteractionClassSafely(
          subscribeInteractionClassWithRegions.getInteractionClassHandle()),
        subscribeInteractionClassWithRegions.getRegionHandles(), subscribeInteractionClassWithRegions.isPassive());
    }
    finally
    {
      subscriptionLock.writeLock().unlock();
    }
  }

  public void unsubscribeInteractionClassWithRegions(
    UnsubscribeInteractionClassWithRegions unsubscribeInteractionClassWithRegions)
  {
    subscriptionLock.writeLock().lock();
    try
    {
      subscriptionManager.unsubscribeInteractionClass(
        unsubscribeInteractionClassWithRegions.getInteractionClassHandle(),
        unsubscribeInteractionClassWithRegions.getRegionHandles());
    }
    finally
    {
      subscriptionLock.writeLock().unlock();
    }
  }

  @SuppressWarnings("unchecked")
  public void enableTimeRegulation(LogicalTimeInterval lookahead, LogicalTime federateTime)
    throws IllegalTimeArithmetic, InvalidLogicalTimeInterval
  {
    assert !timeConstrainedEnabled || galt == null || federateTime.compareTo(galt) <= 0;

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

    log.debug(LogMessages.TIME_REGULATION_ENABLED, federateTime);

    federateChannel.write(new TimeRegulationEnabled(federateTime));
  }

  public void disableTimeRegulation()
  {
    timeRegulationEnabled = false;

    lookahead = null;
    lots = null;

    log.debug(LogMessages.TIME_REGULATION_DISABLED);
  }

  @SuppressWarnings("unchecked")
  public void enableTimeConstrained()
  {
    if (galt == null || federateTime.compareTo(galt) <= 0)
    {
      // GALT is undefined or federate time <= GALT

      timeConstrainedEnabled = true;

      log.debug(LogMessages.TIME_CONSTRAINED_ENABLED, federateTime);

      federateChannel.write(new TimeConstrainedEnabled(federateTime));
    }
    else
    {
      timeConstrainedPending = true;

      log.debug(LogMessages.ENABLE_TIME_CONSTRAINED_PENDING);
    }
  }

  public void disableTimeConstrained()
  {
    timeConstrainedEnabled = false;
    lits = null;

    log.debug(LogMessages.TIME_CONSTRAINED_DISABLED);
  }

  @SuppressWarnings("unchecked")
  public void timeAdvanceRequest(LogicalTime time)
    throws IllegalTimeArithmetic, InvalidLogicalTimeInterval
  {
    log.debug(LogMessages.TIME_ADVANCE_REQUEST, time);

    advanceRequestTime = time;
    advanceRequestType = TimeAdvanceType.TIME_ADVANCE_REQUEST;

    if (timeRegulationEnabled)
    {
      lots = advanceRequestTime.add(lookahead.isZero() ? epsilon : lookahead);

      log.debug(LogMessages.LOTS_ADVANCED, lots);
    }

    if (!timeConstrainedEnabled || galt == null || advanceRequestTime.compareTo(galt) < 0)
    {
      // immediately grant the request

      federateTime = advanceRequestTime;
      advanceRequestTime = null;

      federateChannel.write(new TimeAdvanceGrant(federateTime));
    }
  }

  @SuppressWarnings("unchecked")
  public void timeAdvanceRequestAvailable(LogicalTime time)
    throws IllegalTimeArithmetic, InvalidLogicalTimeInterval
  {
    log.debug(LogMessages.TIME_ADVANCE_REQUEST_AVAILABLE, time);

    advanceRequestTime = time;
    advanceRequestType = TimeAdvanceType.TIME_ADVANCE_REQUEST_AVAILABLE;

    if (timeRegulationEnabled)
    {
      lots = advanceRequestTime.add(lookahead);

      log.debug(LogMessages.LOTS_ADVANCED, lots);
    }

    if (!timeConstrainedEnabled || galt == null || advanceRequestTime.compareTo(galt) <= 0)
    {
      // immediately grant the request

      federateTime = advanceRequestTime;
      advanceRequestTime = null;

      federateChannel.write(new TimeAdvanceGrant(federateTime));
    }
  }

  @SuppressWarnings("unchecked")
  public void nextMessageRequest(LogicalTime time)
    throws IllegalTimeArithmetic, InvalidLogicalTimeInterval
  {
    log.debug(LogMessages.NEXT_MESSAGE_REQUEST, time);

    advanceRequestType = TimeAdvanceType.NEXT_MESSAGE_REQUEST;

    if (!timeConstrainedEnabled || galt == null)
    {
      assert lits == null;

      if (timeRegulationEnabled)
      {
        lots = time.add(lookahead.isZero() ? epsilon : lookahead);

        log.debug(LogMessages.LOTS_ADVANCED, lots);
      }

      // immediately grant the request

      federateTime = time;

      federateChannel.write(new TimeAdvanceGrant(federateTime));
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

        log.debug(LogMessages.LOTS_ADVANCED, lots);
      }

      // immediately grant the request

      federateTime = time;

      federateChannel.write(new TimeAdvanceGrant(federateTime));
    }
    else if (lits != null && lits.compareTo(galt) < 0)
    {
      // LITS < GALT

      if (timeRegulationEnabled)
      {
        lots = lits.add(lookahead.isZero() ? epsilon : lookahead);

        log.debug(LogMessages.LOTS_ADVANCED, lots);
      }

      // immediately grant the request

      federateTime = lits;

      federateChannel.write(new TimeAdvanceGrant(federateTime));
    }
    else
    {
      advanceRequestTime = time;

      if (timeRegulationEnabled)
      {
        // LOTS can only advance as GALT advances

        lots = galt.add(lookahead.isZero() ? epsilon : lookahead);

        log.debug(LogMessages.LOTS_ADVANCED, lots);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void nextMessageRequestAvailable(LogicalTime time)
    throws IllegalTimeArithmetic, InvalidLogicalTimeInterval
  {
    log.debug(LogMessages.NEXT_MESSAGE_REQUEST_AVAILABLE, time);

    advanceRequestType = TimeAdvanceType.NEXT_MESSAGE_REQUEST_AVAILABLE;

    if (!timeConstrainedEnabled || galt == null)
    {
      assert lits == null;

      if (timeRegulationEnabled)
      {
        lots = time.add(lookahead);

        log.debug(LogMessages.LOTS_ADVANCED, lots);
      }

      // immediately grant the request

      federateTime = time;

      federateChannel.write(new TimeAdvanceGrant(federateTime));
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

        log.debug(LogMessages.LOTS_ADVANCED, lots);
      }

      // immediately grant the request

      federateTime = time;

      federateChannel.write(new TimeAdvanceGrant(federateTime));
    }
    else if (lits != null && lits.compareTo(galt) <= 0)
    {
      // LITS < GALT

      if (timeRegulationEnabled)
      {
        lots = lits.add(lookahead);

        log.debug(LogMessages.LOTS_ADVANCED, lots);
      }

      // immediately grant the request

      federateTime = lits;

      federateChannel.write(new TimeAdvanceGrant(federateTime));
    }
    else
    {
      advanceRequestTime = time;

      if (timeRegulationEnabled)
      {
        // LOTS can only advance as GALT advances

        lots = galt.add(lookahead);

        log.debug(LogMessages.LOTS_ADVANCED, lots);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void flushQueueRequest(LogicalTime time)
    throws IllegalTimeArithmetic, InvalidLogicalTimeInterval
  {
    log.debug(LogMessages.FLUSH_QUEUE_REQUEST, time);

    advanceRequestType = TimeAdvanceType.FLUSH_QUEUE_REQUEST;

    federateTime = galt == null || time.compareTo(galt) < 0 ? time : galt;

    if (timeRegulationEnabled)
    {
      lots = federateTime.add(lookahead);

      log.debug(LogMessages.LOTS_ADVANCED, lots);
    }

    federateChannel.write(new TimeAdvanceGrant(federateTime));
  }

  @SuppressWarnings("unchecked")
  public void galtAdvanced(LogicalTime galt)
    throws IllegalTimeArithmetic
  {
    if (this.galt == null)
    {
      log.debug(LogMessages.GALT_DEFINED, galt);
    }
    else
    {
      assert this.galt.compareTo(galt) < 0;

      log.debug(LogMessages.GALT_ADVANCED, this.galt, galt);
    }

    this.galt = galt;

    federateChannel.write(new GALTAdvanced(galt));

    if (timeConstrainedPending && federateTime.compareTo(galt) < 0)
    {
      timeConstrainedPending = false;
      timeConstrainedEnabled = true;

      log.debug(LogMessages.TIME_CONSTRAINED_ENABLED, federateTime);

      federateChannel.write(new TimeConstrainedEnabled(federateTime));
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

            federateChannel.write(new TimeAdvanceGrant(federateTime));
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

            federateChannel.write(new TimeAdvanceGrant(federateTime));
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

            federateChannel.write(new TimeAdvanceGrant(federateTime));
          }
          else if (lits != null && lits.compareTo(galt) < 0)
          {
            federateTime = lits;
            lits = null;

            advanceRequestTime = null;
            lits = null;

            federateChannel.write(new TimeAdvanceGrant(federateTime));
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

            federateChannel.write(new TimeAdvanceGrant(federateTime));
          }
          else if (lits != null && lits.compareTo(galt) <= 0)
          {
            federateTime = lits;

            advanceRequestTime = null;
            lits = null;

            federateChannel.write(new TimeAdvanceGrant(federateTime));
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

    federateChannel.write(new GALTUndefined());
  }

  @SuppressWarnings("unchecked")
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

          log.debug(LogMessages.LITS_ADVANCED, lots);
        }
      }
    }
    finally
    {
      federationExecution.getTimeManager().getTimeLock().writeLock().unlock();
    }
  }

  public void queryInteractionTransportationType(
    FederateProxy federateProxy, QueryInteractionTransportationType queryInteractionTransportationType)
  {
    InteractionClassHandle interactionClassHandle = queryInteractionTransportationType.getInteractionClassHandle();

    TransportationTypeHandle transportationTypeHandle =
      interactionClassTransportationTypeHandles.get(interactionClassHandle);

    if (transportationTypeHandle == null)
    {
      InteractionClass interactionClass =
        federationExecution.getFDD().getInteractionClasses().get(interactionClassHandle);
      assert interactionClass != null;

      federateProxy.getFederateChannel().write(new ReportInteractionTransportationType(
        interactionClassHandle, federateHandle, interactionClass.getTransportationTypeHandle()));
    }
    else
    {
      federateProxy.getFederateChannel().write(new ReportInteractionTransportationType(
        interactionClassHandle, federateHandle, transportationTypeHandle));
    }
  }

  @Override
  public int hashCode()
  {
    return federateHandle.hashCode();
  }

  @Override
  public String toString()
  {
    return String.format("%s,%s,%s", federateHandle, federateChannel.getLocalAddress(), federateName);
  }
}
