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
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.RTIChannelHandler;
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
import net.sf.ohla.rti.messages.MessageDecoder;
import net.sf.ohla.rti.messages.PublishObjectClassAttributes;
import net.sf.ohla.rti.messages.QueryInteractionTransportationType;
import net.sf.ohla.rti.messages.ResignedFederationExecution;
import net.sf.ohla.rti.messages.Retract;
import net.sf.ohla.rti.messages.SendInteraction;
import net.sf.ohla.rti.messages.SubscribeInteractionClass;
import net.sf.ohla.rti.messages.SubscribeInteractionClassWithRegions;
import net.sf.ohla.rti.messages.SubscribeObjectClassAttributes;
import net.sf.ohla.rti.messages.SubscribeObjectClassAttributesWithRegions;
import net.sf.ohla.rti.messages.TimeStampOrderedMessage;
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

  private final Queue<QueuedTimeStampOrderedMessage> queuedTimeStampOrderedMessages =
    new PriorityQueue<QueuedTimeStampOrderedMessage>();

  private final FederateProxyMessageRetractionManager messageRetractionManager =
    new FederateProxyMessageRetractionManager();

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

    ((RTIChannelHandler) federateChannel.getPipeline().get(RTIChannelHandler.NAME)).setFederateProxy(this);
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

  public LogicalTime getLITS()
  {
    QueuedTimeStampOrderedMessage queuedTimeStampOrderedMessage = queuedTimeStampOrderedMessages.peek();
    return queuedTimeStampOrderedMessage == null ? null : queuedTimeStampOrderedMessage.getTime();
  }

  public LogicalTime getLITSOrGALT()
  {
    QueuedTimeStampOrderedMessage queuedTimeStampOrderedMessage = queuedTimeStampOrderedMessages.peek();
    return queuedTimeStampOrderedMessage == null ? galt : queuedTimeStampOrderedMessage.getTime();
  }

  @SuppressWarnings("unchecked")
  public LogicalTime getLITS(LogicalTime potentialGALT)
  {
    LogicalTime lits;

    QueuedTimeStampOrderedMessage queuedTimeStampOrderedMessage = queuedTimeStampOrderedMessages.peek();
    if (queuedTimeStampOrderedMessage == null)
    {
      lits = null;
    }
    else if (advanceRequestType == TimeAdvanceType.NEXT_MESSAGE_REQUEST)
    {
      lits = queuedTimeStampOrderedMessage.getTime().compareTo(potentialGALT) < 0 ?
        null : queuedTimeStampOrderedMessage.getTime();
    }
    else
    {
      assert advanceRequestType == TimeAdvanceType.NEXT_MESSAGE_REQUEST_AVAILABLE;

      lits = queuedTimeStampOrderedMessage.getTime().compareTo(potentialGALT) <= 0 ?
        null : queuedTimeStampOrderedMessage.getTime();
    }

    return lits;
  }

  @SuppressWarnings("unchecked")
  public void adjustNextMessageRequestAdvanceRequestTime(LogicalTime time)
  {
    assert timeRegulationEnabled;

    advanceRequestTime = time;

    LogicalTimeInterval interval;
    if (advanceRequestType == TimeAdvanceType.NEXT_MESSAGE_REQUEST)
    {
      log.debug(LogMessages.ADJUST_NEXT_MESSAGE_REQUEST_ADVANCE_REQUEST_TIME, time);

      interval = lookahead.isZero() ? epsilon : lookahead;
    }
    else
    {
      assert advanceRequestType == TimeAdvanceType.NEXT_MESSAGE_REQUEST_AVAILABLE;

      log.debug(LogMessages.ADJUST_NEXT_MESSAGE_REQUEST_AVAILABLE_ADVANCE_REQUEST_TIME, time);

      interval = lookahead;
    }

    try
    {
      lots = advanceRequestTime.add(interval);

      log.debug(LogMessages.LOTS_UPDATED, lots);
    }
    catch (IllegalTimeArithmetic ita)
    {
      log.error(LogMessages.ILLEGAL_TIME_ARITHMETIC_ADD, ita, advanceRequestTime, interval);
    }
    catch (InvalidLogicalTimeInterval ilti)
    {
      log.error(LogMessages.INVALID_LOGICAL_TIME_INTERVAL, ilti, interval);
    }
  }

  public void resignFederationExecution(ResignAction resignAction)
  {
    ((RTIChannelHandler) federateChannel.getPipeline().get(RTIChannelHandler.NAME)).setFederateProxy(null);

    log.debug(LogMessages.FEDERATE_RESIGNED, resignAction);

    federateChannel.write(new ResignedFederationExecution());
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

    boolean timeStampOrdered =
      updateAttributeValues.getSentOrderType() == OrderType.TIMESTAMP && isTimeConstrainedEnabled();

    subscriptionLock.readLock().lock();
    try
    {
      reflectAttributeValues = subscriptionManager.reflectAttributeValues(
        federateProxy, objectInstance, updateAttributeValues, timeStampOrdered);

      if (reflectAttributeValues != null)
      {
        if (timeStampOrdered && queueTimeStampOrderedMessage(reflectAttributeValues))
        {
          QueuedTimeStampOrderedMessage queuedTimeStampOrderedMessage =
            new QueuedTimeStampOrderedMessage(reflectAttributeValues);
          queuedTimeStampOrderedMessages.offer(queuedTimeStampOrderedMessage);

          messageRetractionManager.add(
            reflectAttributeValues.getMessageRetractionHandle(), reflectAttributeValues.getTime(),
            queuedTimeStampOrderedMessage);
        }
        else
        {
          federateChannel.write(reflectAttributeValues);
        }
      }
    }
    finally
    {
      subscriptionLock.readLock().unlock();
    }
  }

  public void receiveInteraction(
    FederateProxy federateProxy, InteractionClass interactionClass, SendInteraction sendInteraction)
  {
    ReceiveInteraction receiveInteraction;

    boolean timeStampOrdered =
      sendInteraction.getSentOrderType() == OrderType.TIMESTAMP && isTimeConstrainedEnabled();

    subscriptionLock.readLock().lock();
    try
    {
      receiveInteraction = subscriptionManager.receiveInteraction(
        federateProxy, interactionClass, sendInteraction, timeStampOrdered);

      if (receiveInteraction != null)
      {
        if (timeStampOrdered && queueTimeStampOrderedMessage(receiveInteraction))
        {
          QueuedTimeStampOrderedMessage queuedTimeStampOrderedMessage =
            new QueuedTimeStampOrderedMessage(receiveInteraction);
          queuedTimeStampOrderedMessages.offer(queuedTimeStampOrderedMessage);

          messageRetractionManager.add(
            receiveInteraction.getMessageRetractionHandle(), receiveInteraction.getTime(),
            queuedTimeStampOrderedMessage);
        }
        else
        {
          federateChannel.write(receiveInteraction);
        }
      }
    }
    finally
    {
      subscriptionLock.readLock().unlock();
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
      boolean timeStampOrdered =
        deleteObjectInstance.getSentOrderType() == OrderType.TIMESTAMP && isTimeConstrainedEnabled();

      if (timeStampOrdered)
      {
        RemoveObjectInstance removeObjectInstance = new RemoveObjectInstance(
          deleteObjectInstance.getObjectInstanceHandle(), deleteObjectInstance.getTag(), OrderType.TIMESTAMP,
          deleteObjectInstance.getTime(), deleteObjectInstance.getMessageRetractionHandle(), federateHandle);

        if (queueTimeStampOrderedMessage(removeObjectInstance))
        {
          QueuedTimeStampOrderedMessage queuedTimeStampOrderedMessage =
            new QueuedTimeStampOrderedMessage(removeObjectInstance);
          queuedTimeStampOrderedMessages.offer(queuedTimeStampOrderedMessage);

          messageRetractionManager.add(
            removeObjectInstance.getMessageRetractionHandle(), removeObjectInstance.getTime(),
            queuedTimeStampOrderedMessage);
        }
        else
        {
          federateChannel.write(removeObjectInstance);
        }
      }
      else
      {
        federateChannel.write(new RemoveObjectInstance(
          deleteObjectInstance.getObjectInstanceHandle(), deleteObjectInstance.getTag(), OrderType.RECEIVE,
          deleteObjectInstance.getTime(), null, federateHandle));
      }
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
      }
      while ((objectClass = objectClass.getSuperObjectClass()) != null);

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
        unsubscribeObjectClassAttributes.getObjectClassHandle(),
        unsubscribeObjectClassAttributes.getAttributeHandles());
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
        federationExecution.getFDD().getObjectClassSafely(
          subscribeObjectClassAttributesWithRegions.getObjectClassHandle()),
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
    this.lookahead = lookahead;
    this.federateTime = federateTime;

    advanceRequestType = TimeAdvanceType.TIME_ADVANCE_REQUEST_AVAILABLE;

    // set the Least Outgoing Time Stamp
    //
    lots = federateTime.add(lookahead);

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

  public boolean isTimeConstrainedEnabled()
  {
    return timeConstrainedEnabled;
  }

  public boolean isTimeRegulationEnabled()
  {
    return timeRegulationEnabled;
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

      log.debug(LogMessages.LOTS_UPDATED, lots);
    }

    if (!timeConstrainedEnabled || galt == null || advanceRequestTime.compareTo(galt) < 0)
    {
      // immediately grant the request

      releaseTimeStampOrderedMessages(advanceRequestTime);

      timeAdvanceGrant(advanceRequestTime);
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

      log.debug(LogMessages.LOTS_UPDATED, lots);
    }

    if (!timeConstrainedEnabled || galt == null || advanceRequestTime.compareTo(galt) <= 0)
    {
      // immediately grant the request

      releaseTimeStampOrderedMessages(advanceRequestTime);

      timeAdvanceGrant(advanceRequestTime);
    }
  }

  @SuppressWarnings("unchecked")
  public void nextMessageRequest(LogicalTime time)
    throws IllegalTimeArithmetic, InvalidLogicalTimeInterval
  {
    log.debug(LogMessages.NEXT_MESSAGE_REQUEST, time);

    advanceRequestTime = time;
    advanceRequestType = TimeAdvanceType.NEXT_MESSAGE_REQUEST;

    if (timeRegulationEnabled)
    {
      lots = advanceRequestTime.add(lookahead.isZero() ? epsilon : lookahead);

      log.debug(LogMessages.LOTS_UPDATED, lots);

      if (!timeConstrainedEnabled || galt == null)
      {
        // immediately grant the request

        timeAdvanceGrant(advanceRequestTime);
      }
    }
    else if (galt == null)
    {
      // immediately grant the request

      timeAdvanceGrant(advanceRequestTime);
    }
    else if (timeConstrainedEnabled)
    {
      // time constrained only federates need to check LITS themselves

      LogicalTime lits = getLITS();
      if (lits != null && lits.compareTo(galt) < 0)
      {
        // immediately grant the request

        releaseTimeStampOrderedMessages(lits);

        timeAdvanceGrant(lits);
      }
      else if (advanceRequestTime.compareTo(galt) < 0)
      {
        // immediately grant the request

        releaseTimeStampOrderedMessages(advanceRequestTime);

        timeAdvanceGrant(advanceRequestTime);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void nextMessageRequestAvailable(LogicalTime time)
    throws IllegalTimeArithmetic, InvalidLogicalTimeInterval
  {
    log.debug(LogMessages.NEXT_MESSAGE_REQUEST_AVAILABLE, time);

    advanceRequestTime = time;
    advanceRequestType = TimeAdvanceType.NEXT_MESSAGE_REQUEST_AVAILABLE;

    if (timeRegulationEnabled)
    {
      lots = advanceRequestTime.add(lookahead);

      log.debug(LogMessages.LOTS_UPDATED, lots);

      if (!timeConstrainedEnabled || galt == null)
      {
        // immediately grant the request

        timeAdvanceGrant(advanceRequestTime);
      }
    }
    else if (galt == null)
    {
      // immediately grant the request

      timeAdvanceGrant(advanceRequestTime);
    }
    else if (timeConstrainedEnabled)
    {
      // time constrained only federates need to check LITS themselves

      LogicalTime lits = getLITS();
      if (lits != null && lits.compareTo(galt) <= 0)
      {
        // immediately grant the request

        releaseTimeStampOrderedMessages(lits);

        timeAdvanceGrant(lits);
      }
      else if (advanceRequestTime.compareTo(galt) <= 0)
      {
        // immediately grant the request

        releaseTimeStampOrderedMessages(advanceRequestTime);

        timeAdvanceGrant(advanceRequestTime);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void flushQueueRequest(LogicalTime time)
    throws IllegalTimeArithmetic, InvalidLogicalTimeInterval
  {
    log.debug(LogMessages.FLUSH_QUEUE_REQUEST, time);

    advanceRequestType = TimeAdvanceType.FLUSH_QUEUE_REQUEST;

    if (galt != null && galt.compareTo(time) < 0)
    {
      time = galt;
    }

    LogicalTime lits = getLITS();
    if (lits != null && lits.compareTo(time) < 0)
    {
      time = lits;
    }

    if (timeRegulationEnabled)
    {
      lots = time.add(lookahead);

      log.debug(LogMessages.LOTS_UPDATED, lots);
    }

    releaseAllTimeStampOrderedMessages();

    timeAdvanceGrant(time);
  }

  @SuppressWarnings("unchecked")
  public void galtUpdated(LogicalTime galt)
  {
    if (this.galt == null)
    {
      log.debug(LogMessages.GALT_DEFINED, galt);
    }

    log.debug(LogMessages.GALT_UPDATED, this.galt, galt);

    this.galt = galt;

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
            releaseTimeStampOrderedMessages(advanceRequestTime);

            timeAdvanceGrant(advanceRequestTime);
          }
          break;
        }
        case TIME_ADVANCE_REQUEST_AVAILABLE:
        {
          if (advanceRequestTime.compareTo(galt) <= 0)
          {
            releaseTimeStampOrderedMessages(advanceRequestTime);

            timeAdvanceGrant(advanceRequestTime);
          }
          break;
        }
        case NEXT_MESSAGE_REQUEST:
        {
          LogicalTime lits = getLITS();
          if (lits != null && lits.compareTo(galt) < 0)
          {
            releaseTimeStampOrderedMessages(lits);

            timeAdvanceGrant(lits);
          }
          else if (advanceRequestTime.compareTo(galt) < 0)
          {
            releaseTimeStampOrderedMessages(advanceRequestTime);

            timeAdvanceGrant(advanceRequestTime);
          }
          break;
        }
        case NEXT_MESSAGE_REQUEST_AVAILABLE:
        {
          LogicalTime lits = getLITS();
          if (lits != null && lits.compareTo(galt) <= 0)
          {
            releaseTimeStampOrderedMessages(lits);

            timeAdvanceGrant(lits);
          }
          else if (advanceRequestTime.compareTo(galt) <= 0)
          {
            releaseTimeStampOrderedMessages(advanceRequestTime);

            timeAdvanceGrant(advanceRequestTime);
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

    for (QueuedTimeStampOrderedMessage queuedTimeStampOrderedMessage = queuedTimeStampOrderedMessages.poll();
         queuedTimeStampOrderedMessage != null; queuedTimeStampOrderedMessage = queuedTimeStampOrderedMessages.poll())
    {
      queuedTimeStampOrderedMessage.writeReceiveOrder(federateChannel);
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

  @SuppressWarnings("unchecked")
  private boolean queueTimeStampOrderedMessage(TimeStampOrderedMessage timeStampOrderedMessage)
  {
    boolean queue;
    if (advanceRequestTime == null)
    {
      // not time advancing

      queue = true;
    }
    else
    {
      switch (advanceRequestType)
      {
        case NEXT_MESSAGE_REQUEST:
        case NEXT_MESSAGE_REQUEST_AVAILABLE:
          queue = true;
          break;
        case TIME_ADVANCE_REQUEST:
          queue = timeStampOrderedMessage.getTime().compareTo(advanceRequestTime) >= 0;
          break;
        case TIME_ADVANCE_REQUEST_AVAILABLE:
          queue = timeStampOrderedMessage.getTime().compareTo(advanceRequestTime) > 0;
          break;
        default:
          throw new Error();
      }
    }
    return queue;
  }

  @SuppressWarnings("unchecked")
  private void releaseAllTimeStampOrderedMessages()
  {
    for (QueuedTimeStampOrderedMessage queuedTimeStampOrderedMessage : queuedTimeStampOrderedMessages)
    {
      queuedTimeStampOrderedMessage.write(federateChannel);
    }

    queuedTimeStampOrderedMessages.clear();
  }

  @SuppressWarnings("unchecked")
  private void releaseTimeStampOrderedMessages(LogicalTime time)
  {
    for (QueuedTimeStampOrderedMessage queuedTimeStampOrderedMessage = queuedTimeStampOrderedMessages.peek();
         queuedTimeStampOrderedMessage != null && queuedTimeStampOrderedMessage.getTime().compareTo(time) <= 0;
         queuedTimeStampOrderedMessage = queuedTimeStampOrderedMessages.peek())
    {
      queuedTimeStampOrderedMessage.write(federateChannel);

      queuedTimeStampOrderedMessages.poll();
    }
  }

  @SuppressWarnings("unchecked")
  private void timeAdvanceGrant(LogicalTime time)
  {
    log.trace(LogMessages.TIME_ADVANCE_GRANT, federateTime, time);

    federateTime = time;
    advanceRequestTime = null;

    federateChannel.write(new TimeAdvanceGrant(time));
  }
}
