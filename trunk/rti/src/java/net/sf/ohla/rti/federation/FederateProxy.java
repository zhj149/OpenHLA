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
import java.io.OutputStream;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.RTIChannelHandler;
import net.sf.ohla.rti.fdd.InteractionClass;
import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.federate.TimeAdvanceType;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleSetFactory;
import net.sf.ohla.rti.i18n.I18nLogger;
import net.sf.ohla.rti.i18n.LogMessages;
import net.sf.ohla.rti.messages.DeleteObjectInstance;
import net.sf.ohla.rti.messages.FederateSaveBegun;
import net.sf.ohla.rti.messages.FederateSaveComplete;
import net.sf.ohla.rti.messages.FederateSaveNotComplete;
import net.sf.ohla.rti.messages.FederateStateFrame;
import net.sf.ohla.rti.messages.PublishInteractionClass;
import net.sf.ohla.rti.messages.PublishObjectClassAttributes;
import net.sf.ohla.rti.messages.QueryInteractionTransportationType;
import net.sf.ohla.rti.messages.ResignedFederationExecution;
import net.sf.ohla.rti.messages.Retract;
import net.sf.ohla.rti.messages.SendInteraction;
import net.sf.ohla.rti.messages.SubscribeInteractionClass;
import net.sf.ohla.rti.messages.SubscribeInteractionClassWithRegions;
import net.sf.ohla.rti.messages.SubscribeObjectClassAttributes;
import net.sf.ohla.rti.messages.SubscribeObjectClassAttributesWithRegions;
import net.sf.ohla.rti.messages.UnpublishInteractionClass;
import net.sf.ohla.rti.messages.UnpublishObjectClass;
import net.sf.ohla.rti.messages.UnpublishObjectClassAttributes;
import net.sf.ohla.rti.messages.UnsubscribeInteractionClass;
import net.sf.ohla.rti.messages.UnsubscribeInteractionClassWithRegions;
import net.sf.ohla.rti.messages.UnsubscribeObjectClassAttributes;
import net.sf.ohla.rti.messages.UnsubscribeObjectClassAttributesWithRegions;
import net.sf.ohla.rti.messages.UpdateAttributeValues;
import net.sf.ohla.rti.messages.callbacks.AnnounceSynchronizationPoint;
import net.sf.ohla.rti.messages.callbacks.DiscoverObjectInstance;
import net.sf.ohla.rti.messages.callbacks.FederationNotRestored;
import net.sf.ohla.rti.messages.callbacks.FederationNotSaved;
import net.sf.ohla.rti.messages.callbacks.FederationRestoreBegun;
import net.sf.ohla.rti.messages.callbacks.FederationRestored;
import net.sf.ohla.rti.messages.callbacks.FederationSaved;
import net.sf.ohla.rti.messages.callbacks.InitiateFederateRestore;
import net.sf.ohla.rti.messages.callbacks.InitiateFederateSave;
import net.sf.ohla.rti.messages.callbacks.ProvideAttributeValueUpdate;
import net.sf.ohla.rti.messages.callbacks.ReceiveInteraction;
import net.sf.ohla.rti.messages.callbacks.ReflectAttributeValues;
import net.sf.ohla.rti.messages.callbacks.RemoveObjectInstance;
import net.sf.ohla.rti.messages.callbacks.ReportInteractionTransportationType;
import net.sf.ohla.rti.messages.callbacks.RequestAttributeOwnershipAssumption;
import net.sf.ohla.rti.messages.callbacks.RequestFederationRestoreFailed;
import net.sf.ohla.rti.messages.callbacks.RequestFederationRestoreSucceeded;
import net.sf.ohla.rti.messages.callbacks.TimeAdvanceGrant;
import net.sf.ohla.rti.messages.callbacks.TimeConstrainedEnabled;
import net.sf.ohla.rti.messages.callbacks.TimeRegulationEnabled;
import net.sf.ohla.rti.proto.FederationExecutionSaveProtos.FederateProxyState;
import net.sf.ohla.rti.proto.OHLAProtos;
import net.sf.ohla.rti.util.AttributeHandles;
import net.sf.ohla.rti.util.InteractionClassHandles;
import net.sf.ohla.rti.util.LogicalTimeIntervals;
import net.sf.ohla.rti.util.LogicalTimes;
import net.sf.ohla.rti.util.ObjectClassHandles;
import net.sf.ohla.rti.util.ObjectInstanceHandles;
import net.sf.ohla.rti.util.Retractable;
import net.sf.ohla.rti.util.RetractableManager;

import org.jboss.netty.channel.Channel;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
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
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.IllegalTimeArithmetic;
import hla.rti1516e.exceptions.InvalidLogicalTimeInterval;

public class FederateProxy
{
  private final FederationExecution federationExecution;

  private FederateHandle federateHandle;
  private String federateName;
  private String federateType;

  /**
   * The {@code Channel} back to the Federate.
   */
  private final Channel federateChannel;

  private final FederateProxyTimeStampOrderedMessageQueue federateProxyTimeStampOrderedMessageQueue;

  private FederateProxySave federateProxySave;

  private final ReadWriteLock publicationLock = new ReentrantReadWriteLock(true);
  private final Map<ObjectClassHandle, AttributeHandleSet> publishedObjectClasses = new HashMap<>();
  private final Set<InteractionClassHandle> publishedInteractionClasses = new HashSet<>();

  private final ReadWriteLock subscriptionLock = new ReentrantReadWriteLock(true);
  private final FederateProxySubscriptionManager subscriptionManager = new FederateProxySubscriptionManager(this);

  private final Set<ObjectInstanceHandle> discoveredObjects =
    Collections.synchronizedSet(new HashSet<ObjectInstanceHandle>());

  private final Map<InteractionClassHandle, TransportationTypeHandle> interactionClassTransportationTypeHandles =
    new HashMap<>();

  private final RetractableManager<Retractable> retractableManager = new RetractableManager<>();

  private final LogicalTimeInterval zero;
  private final LogicalTimeInterval epsilon;

  private boolean timeRegulationEnabled;
  private LogicalTimeInterval lookahead;

  private boolean timeConstrainedEnabled;
  private boolean timeConstrainedPending;

  private LogicalTime advanceRequestTime;
  private TimeAdvanceType advanceRequestType = TimeAdvanceType.NONE;

  private LogicalTime federateTime;

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
    this.federateName = federateName;
    this.federateType = federateType;
    this.federateChannel = federateChannel;
    this.galt = galt;

    federateProxyTimeStampOrderedMessageQueue =
      new FederateProxyTimeStampOrderedMessageQueue(federationExecution, this);

    zero = federationExecution.getTimeManager().getLogicalTimeFactory().makeZero();
    epsilon = federationExecution.getTimeManager().getLogicalTimeFactory().makeEpsilon();

    federateTime = federationExecution.getTimeManager().getLogicalTimeFactory().makeInitial();

    federateChannel.getPipeline().addBefore(
      RTIChannelHandler.NAME, FederateProxyChannelHandler.NAME, new FederateProxyChannelHandler(this));

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

  public Map<ObjectClassHandle, AttributeHandleSet> getPublishedObjectClasses()
  {
    return publishedObjectClasses;
  }

  public Set<InteractionClassHandle> getPublishedInteractionClasses()
  {
    return publishedInteractionClasses;
  }

  public FederateProxySubscriptionManager getSubscriptionManager()
  {
    return subscriptionManager;
  }

  public Set<ObjectInstanceHandle> getDiscoveredObjects()
  {
    return discoveredObjects;
  }

  public boolean isSaving()
  {
    return federateProxySave != null;
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

  public boolean isGALTDefined()
  {
    return galt != null;
  }

  public LogicalTime getLOTS()
  {
    return lots;
  }

  @SuppressWarnings("unchecked")
  public LogicalTime getLITS()
  {
    return federateProxyTimeStampOrderedMessageQueue.lits();
  }

  public LogicalTime getLITSOrGALT()
  {
    LogicalTime lits = federateProxyTimeStampOrderedMessageQueue.lits();
    return lits == null ? galt : lits;
  }

  @SuppressWarnings("unchecked")
  public LogicalTime getLITS(LogicalTime potentialGALT)
  {
    LogicalTime lits;

    LogicalTime possibleLITS = federateProxyTimeStampOrderedMessageQueue.lits();
    if (possibleLITS == null)
    {
      lits = null;
    }
    else if (advanceRequestType == TimeAdvanceType.NEXT_MESSAGE_REQUEST)
    {
      lits = possibleLITS.compareTo(potentialGALT) < 0 ? null : possibleLITS;
    }
    else
    {
      assert advanceRequestType == TimeAdvanceType.NEXT_MESSAGE_REQUEST_AVAILABLE;

      lits = possibleLITS.compareTo(potentialGALT) <= 0 ? null : possibleLITS;
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
    federateChannel.getPipeline().remove(FederateProxyChannelHandler.NAME);

    log.debug(LogMessages.FEDERATE_RESIGNED, resignAction);

    federateChannel.write(new ResignedFederationExecution());
  }

  public void announceSynchronizationPoint(AnnounceSynchronizationPoint announceSynchronizationPoint)
  {
    federateChannel.write(announceSynchronizationPoint);
  }

  public void initiateFederateSave(FederationExecutionSave federationExecutionSave)
    throws IOException
  {
    federateProxySave = federationExecutionSave.instructedToSave(this);

    federateChannel.write(new InitiateFederateSave(federationExecutionSave.getLabel()));
  }

  public void federateSaveInitiatedFailed()
  {
  }

  public void federateSaveBegun(FederateSaveBegun federateSaveBegun)
  {
    try (OutputStream out = federateProxySave.getFederateProxyStateOutputStream())
    {
      CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(out);
      saveState(codedOutputStream);
      codedOutputStream.flush();
    }
    catch (IOException ioe)
    {
      log.error(LogMessages.UNEXPECTED_EXCEPTION, ioe, ioe);

      // TODO: close connection
    }
  }

  public void federateSaveComplete(FederateSaveComplete federateSaveComplete)
  {
  }

  public void federateSaveNotComplete(FederateSaveNotComplete federateSaveNotComplete)
  {
  }

  public void federationSaved(FederationSaved federationSaved)
  {
    federateProxySave = null;

    federateChannel.write(federationSaved);
  }

  public void federationNotSaved(FederationNotSaved federationNotSaved)
  {
    federateProxySave = null;

    federateChannel.write(federationNotSaved);
  }

  public void requestFederationRestoreSucceeded(RequestFederationRestoreSucceeded requestFederationRestoreSucceeded)
  {
    federateChannel.write(requestFederationRestoreSucceeded);
  }

  public void requestFederationRestoreFailed(RequestFederationRestoreFailed requestFederationRestoreFailed)
  {
    federateChannel.write(requestFederationRestoreFailed);
  }

  public void federationRestoreBegun(FederationRestoreBegun federationRestoreBegun)
  {
    federateChannel.write(federationRestoreBegun);
  }

  public void initiateFederateRestore(InitiateFederateRestore initiateFederateRestore)
  {
    federateChannel.write(initiateFederateRestore);
  }

  public void federationRestored(FederationRestored federationRestored)
  {
    federateChannel.write(federationRestored);
  }

  public void federationNotRestored(FederationNotRestored federationNotRestored)
  {
    federateChannel.write(federationNotRestored);
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
    FederateHandle producingFederateHandle, UpdateAttributeValues updateAttributeValues,
    FederationExecutionObjectInstance objectInstance)
  {
    if (updateAttributeValues.getSentOrderType() == OrderType.TIMESTAMP && isTimeConstrainedEnabled() &&
        queueTimeStampOrderedMessage(updateAttributeValues.getTime(federationExecution.getTimeManager().getLogicalTimeFactory())))
    {
      // might not be subscribed until later, must save it
      //
      federateProxyTimeStampOrderedMessageQueue.add(producingFederateHandle, updateAttributeValues);
    }
    else
    {
      subscriptionLock.readLock().lock();
      try
      {
        reflectAttributeValuesNow(producingFederateHandle, updateAttributeValues, OrderType.RECEIVE, objectInstance);
      }
      finally
      {
        subscriptionLock.readLock().unlock();
      }
    }
  }

  public boolean reflectAttributeValuesNow(
    FederateHandle producingFederateHandle, UpdateAttributeValues updateAttributeValues,
    OrderType orderType, FederationExecutionObjectInstance objectInstance)
  {
    boolean delivered;

    // trim the update based upon our subscriptions
    //
    ReflectAttributeValues reflectAttributeValues = subscriptionManager.reflectAttributeValues(
      producingFederateHandle, objectInstance, updateAttributeValues, orderType);

    if (reflectAttributeValues != null &&
        (updateAttributeValues.getSentOrderType() == OrderType.RECEIVE ||
         retractableManager.add(new Retractable(
           updateAttributeValues.getMessageRetractionHandle(),
           updateAttributeValues.getTime(federationExecution.getLogicalTimeFactory())))))
    {
      // only send if:
      // - the update is still subscribed to
      // - was sent receive order or was not already sent

      federateChannel.write(reflectAttributeValues);

      delivered = true;
    }
    else
    {
      delivered = false;
    }
    return delivered;
  }

  public boolean wouldReflectAttributeValues(UpdateAttributeValues updateAttributeValues)
  {
    FederationExecutionObjectInstance federationExecutionObjectInstance =
      federationExecution.getObjectManager().getObjects().get(updateAttributeValues.getObjectInstanceHandle());
    assert federationExecutionObjectInstance != null;

    return subscriptionManager.wouldReflectAttributeValues(federationExecutionObjectInstance, updateAttributeValues) &&
           (updateAttributeValues.getSentOrderType() == OrderType.RECEIVE ||
            !retractableManager.contains(updateAttributeValues.getMessageRetractionHandle()));
  }

  public void receiveInteraction(FederateHandle producingFederateHandle, SendInteraction sendInteraction)
  {
    if (sendInteraction.getSentOrderType() == OrderType.TIMESTAMP && isTimeConstrainedEnabled() &&
        queueTimeStampOrderedMessage(sendInteraction.getTime(federationExecution.getTimeManager().getLogicalTimeFactory())))
    {
      // might not be subscribed until later, must save it
      //
      federateProxyTimeStampOrderedMessageQueue.add(producingFederateHandle, sendInteraction);
    }
    else
    {
      subscriptionLock.readLock().lock();
      try
      {
        receiveInteractionNow(producingFederateHandle, sendInteraction, OrderType.RECEIVE);
      }
      finally
      {
        subscriptionLock.readLock().unlock();
      }
    }
  }

  public boolean receiveInteractionNow(
    FederateHandle producingFederateHandle, SendInteraction sendInteraction, OrderType receivedOrderType)
  {
    boolean delivered;

    // trim the update based upon our subscriptions
    //
    ReceiveInteraction receiveInteraction = subscriptionManager.receiveInteraction(
      producingFederateHandle, sendInteraction, receivedOrderType);

    if (receiveInteraction != null &&
        (sendInteraction.getSentOrderType() == OrderType.RECEIVE ||
         retractableManager.add(new Retractable(
           sendInteraction.getMessageRetractionHandle(),
           sendInteraction.getTime(federationExecution.getLogicalTimeFactory())))))
    {
      // only send if:
      // - the interaction is still subscribed to
      // - was sent receive order or was not already sent

      federateChannel.write(receiveInteraction);

      delivered = true;
    }
    else
    {
      delivered = false;
    }
    return delivered;
  }

  public boolean wouldReceiveInteraction(SendInteraction sendInteraction)
  {
    return subscriptionManager.wouldReceiveInteraction(sendInteraction) &&
           (sendInteraction.getSentOrderType() == OrderType.RECEIVE ||
            !retractableManager.contains(sendInteraction.getMessageRetractionHandle()));
  }

  public void removeObjectInstance(ObjectInstanceHandle objectInstanceHandle, FederateHandle producingFederateHandle)
  {
    if (discoveredObjects.contains(objectInstanceHandle))
    {
      federateChannel.write(new RemoveObjectInstance(objectInstanceHandle, producingFederateHandle));
    }
  }

  public void removeObjectInstance(FederateHandle producingFederateHandle, DeleteObjectInstance deleteObjectInstance)
  {
    if (deleteObjectInstance.getSentOrderType() == OrderType.TIMESTAMP && isTimeConstrainedEnabled() &&
        queueTimeStampOrderedMessage(deleteObjectInstance.getTime(federationExecution.getTimeManager().getLogicalTimeFactory())))
    {
      federateProxyTimeStampOrderedMessageQueue.add(producingFederateHandle, deleteObjectInstance);
    }
    else if (discoveredObjects.contains(deleteObjectInstance.getObjectInstanceHandle()))
    {
      removeObjectInstanceNow(producingFederateHandle, deleteObjectInstance, OrderType.RECEIVE);
    }
  }

  public boolean wouldRemoveObjectInstance(DeleteObjectInstance deleteObjectInstance)
  {
    return discoveredObjects.contains(deleteObjectInstance.getObjectInstanceHandle()) &&
           (deleteObjectInstance.getSentOrderType() == OrderType.RECEIVE ||
            !retractableManager.contains(deleteObjectInstance.getMessageRetractionHandle()));
  }

  public boolean removeObjectInstanceNow(
    FederateHandle producingFederateHandle, DeleteObjectInstance deleteObjectInstance, OrderType receivedOrderType)
  {
    boolean delivered;

    if (discoveredObjects.remove(deleteObjectInstance.getObjectInstanceHandle()) &&
        (deleteObjectInstance.getSentOrderType() == OrderType.RECEIVE ||
         retractableManager.add(new Retractable(
           deleteObjectInstance.getMessageRetractionHandle(),
           deleteObjectInstance.getTime(federationExecution.getLogicalTimeFactory())))))
    {
      // only send if:
      // - the object is still discovered
      // - was sent receive order or was not already sent

      federateChannel.write(new RemoveObjectInstance(
        deleteObjectInstance.getBuilder(), receivedOrderType, producingFederateHandle));

      delivered = true;
    }
    else
    {
      delivered = false;
    }
    return delivered;
  }

  public void provideAttributeValueUpdate(ProvideAttributeValueUpdate provideAttributeValueUpdate)
  {
    federateChannel.write(provideAttributeValueUpdate);
  }

  public void retract(Retract retract)
  {
    if (!federateProxyTimeStampOrderedMessageQueue.retract(retract.getMessageRetractionHandle()))
    {
      // forward the retract because the message could have already been delivered
      //
      federateChannel.write(retract);
    }
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

  public void unpublishObjectClass(UnpublishObjectClass unpublishObjectClass)
  {
    publicationLock.writeLock().lock();
    try
    {
      publishedObjectClasses.remove(unpublishObjectClass.getObjectClassHandle());
    }
    finally
    {
      publicationLock.writeLock().unlock();
    }
  }

  public void unpublishObjectClassAttributes(UnpublishObjectClassAttributes unpublishObjectClassAttributes)
  {
    ObjectClassHandle objectClassHandle = unpublishObjectClassAttributes.getObjectClassHandle();

    publicationLock.writeLock().lock();
    try
    {
      AttributeHandleSet publishedAttributeHandles = publishedObjectClasses.get(objectClassHandle);
      if (publishedAttributeHandles != null)
      {
        publishedAttributeHandles.removeAll(unpublishObjectClassAttributes.getAttributeHandles());

        if (publishedAttributeHandles.isEmpty())
        {
          publishedObjectClasses.remove(objectClassHandle);
        }
      }
    }
    finally
    {
      publicationLock.writeLock().unlock();
    }
  }

  public void publishInteractionClass(PublishInteractionClass publishInteractionClass)
  {
    publicationLock.writeLock().lock();
    try
    {
      publishedInteractionClasses.add(publishInteractionClass.getInteractionClassHandle());
    }
    finally
    {
      publicationLock.writeLock().unlock();
    }
  }

  public void unpublishInteractionClass(UnpublishInteractionClass unpublishInteractionClass)
  {
    publicationLock.writeLock().lock();
    try
    {
      publishedInteractionClasses.remove(unpublishInteractionClass.getInteractionClassHandle());
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

  public boolean isTimeRegulationEnabled()
  {
    return timeRegulationEnabled;
  }

  public boolean isTimeConstrainedEnabled()
  {
    return timeConstrainedEnabled;
  }

  public boolean isTimeConstrainedPending()
  {
    return timeConstrainedPending;
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

      federateProxyTimeStampOrderedMessageQueue.deliverTo(advanceRequestTime);

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

      federateProxyTimeStampOrderedMessageQueue.deliverTo(advanceRequestTime);

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

        federateProxyTimeStampOrderedMessageQueue.deliverTo(lits);

        timeAdvanceGrant(lits);
      }
      else if (advanceRequestTime.compareTo(galt) < 0)
      {
        // immediately grant the request

        federateProxyTimeStampOrderedMessageQueue.deliverTo(advanceRequestTime);

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

        federateProxyTimeStampOrderedMessageQueue.deliverTo(lits);

        timeAdvanceGrant(lits);
      }
      else if (advanceRequestTime.compareTo(galt) <= 0)
      {
        // immediately grant the request

        federateProxyTimeStampOrderedMessageQueue.deliverTo(advanceRequestTime);

        timeAdvanceGrant(advanceRequestTime);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void flushQueueRequest(LogicalTime time)
    throws IllegalTimeArithmetic, InvalidLogicalTimeInterval
  {
    log.debug(LogMessages.FLUSH_QUEUE_REQUEST, time);

    subscriptionLock.readLock().lock();
    try
    {
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

      federateProxyTimeStampOrderedMessageQueue.flush();

      // expire
      federateProxyTimeStampOrderedMessageQueue.expire(time);

      timeAdvanceGrant(time);
    }
    finally
    {
      subscriptionLock.readLock().unlock();
    }
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

    // expire retractable messages since they can no longer be retracted
    //
    retractableManager.expire(galt);

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
            federateProxyTimeStampOrderedMessageQueue.deliverTo(advanceRequestTime);

            timeAdvanceGrant(advanceRequestTime);
          }
          break;
        }
        case TIME_ADVANCE_REQUEST_AVAILABLE:
        {
          if (advanceRequestTime.compareTo(galt) <= 0)
          {
            federateProxyTimeStampOrderedMessageQueue.deliverTo(advanceRequestTime);

            timeAdvanceGrant(advanceRequestTime);
          }
          break;
        }
        case NEXT_MESSAGE_REQUEST:
        {
          LogicalTime lits = getLITS();
          if (lits != null && lits.compareTo(galt) < 0)
          {
            federateProxyTimeStampOrderedMessageQueue.deliverTo(lits);

            timeAdvanceGrant(lits);
          }
          else if (advanceRequestTime.compareTo(galt) < 0)
          {
            federateProxyTimeStampOrderedMessageQueue.deliverTo(advanceRequestTime);

            timeAdvanceGrant(advanceRequestTime);
          }
          break;
        }
        case NEXT_MESSAGE_REQUEST_AVAILABLE:
        {
          LogicalTime lits = getLITS();
          if (lits != null && lits.compareTo(galt) <= 0)
          {
            federateProxyTimeStampOrderedMessageQueue.deliverTo(lits);

            timeAdvanceGrant(lits);
          }
          else if (advanceRequestTime.compareTo(galt) <= 0)
          {
            federateProxyTimeStampOrderedMessageQueue.deliverTo(advanceRequestTime);

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

    federateProxyTimeStampOrderedMessageQueue.deliverAll(OrderType.RECEIVE);
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

  public void handleFederateStateFrame(FederateStateFrame federateStateFrame)
  {
    assert federateProxySave != null;

    try
    {
      federateProxySave.handleFederateStateFrame(federateStateFrame);
    }
    catch (IOException ioe)
    {
      log.error(LogMessages.UNEXPECTED_EXCEPTION, ioe, ioe);

      // TODO: close connection
    }
  }

  public void restoreState(FederateHandle federateHandle, String federateName, String federateType, CodedInputStream in)
    throws IOException
  {
    this.federateHandle = federateHandle;
    this.federateName = federateName;
    this.federateType = federateType;

    FederateProxyState federateProxyState = in.readMessage(FederateProxyState.PARSER, null);

    publishedObjectClasses.clear();
    for (OHLAProtos.PublishedObjectClass publishedObjectClass : federateProxyState.getPublishedObjectClassesList())
    {
      publishedObjectClasses.put(
        ObjectClassHandles.convert(publishedObjectClass.getObjectClassHandle()),
        AttributeHandles.convertAttributeHandles(publishedObjectClass.getAttributeHandlesList()));
    }

    publishedInteractionClasses.clear();
    publishedInteractionClasses.addAll(
      InteractionClassHandles.convertFromProto(federateProxyState.getPublishedInteractionClassesList()));

    if (timeRegulationEnabled = federateProxyState.getTimeRegulationEnabled())
    {
      // TODO: throw exception?
      assert federateProxyState.hasLookahead();
      lookahead = LogicalTimeIntervals.convert(
        federationExecution.getTimeManager().getLogicalTimeFactory(), federateProxyState.getLookahead());

      // TODO: throw exception?
      assert federateProxyState.hasLots();
      lots = LogicalTimes.convert(
        federationExecution.getTimeManager().getLogicalTimeFactory(), federateProxyState.getLots());
    }
    else
    {
      lookahead = null;
      lots = null;
    }

    advanceRequestType = TimeAdvanceType.values()[federateProxyState.getAdvanceRequestType().ordinal()];

    if (timeConstrainedEnabled = federateProxyState.getTimeConstrainedEnabled())
    {
      timeConstrainedPending = false;

      advanceRequestType = TimeAdvanceType.values()[federateProxyState.getAdvanceRequestType().ordinal()];

      if (federateProxyState.hasAdvanceRequestTime())
      {
        advanceRequestTime = LogicalTimes.convert(
          federationExecution.getTimeManager().getLogicalTimeFactory(), federateProxyState.getAdvanceRequestTime());
      }
      else
      {
        advanceRequestTime = null;
      }
    }
    else
    {
      advanceRequestType = TimeAdvanceType.NONE;
      advanceRequestTime = null;

      timeConstrainedPending = federateProxyState.getTimeConstrainedPending();
    }

    if (federateProxyState.hasGalt())
    {
      galt = LogicalTimes.convert(
        federationExecution.getTimeManager().getLogicalTimeFactory(), federateProxyState.getGalt());
    }
    else
    {
      galt = null;
    }

    federateTime = LogicalTimes.convert(
      federationExecution.getTimeManager().getLogicalTimeFactory(), federateProxyState.getFederateTime());

    discoveredObjects.clear();
    for (int discoveredObjectInstanceHandleCount = federateProxyState.getDiscoveredObjectInstanceHandleCount();
         discoveredObjectInstanceHandleCount > 0; --discoveredObjectInstanceHandleCount)
    {
      discoveredObjects.add(
        ObjectInstanceHandles.convert(in.readMessage(OHLAProtos.ObjectInstanceHandle.PARSER, null)));
    }

    subscriptionManager.restoreState(federationExecution.getFDD(), in);
    federateProxyTimeStampOrderedMessageQueue.restoreState(in);
  }

  @Override
  public int hashCode()
  {
    return federateHandle.hashCode();
  }

  @Override
  public String toString()
  {
    return new StringBuilder().append(federateHandle).append("-").append(federateName).toString();
  }

  private void saveState(CodedOutputStream out)
    throws IOException
  {
    FederateProxyState.Builder federateProxyState = FederateProxyState.newBuilder();

    for (Map.Entry<ObjectClassHandle, AttributeHandleSet> entry : publishedObjectClasses.entrySet())
    {
      federateProxyState.addPublishedObjectClasses(
        OHLAProtos.PublishedObjectClass.newBuilder().setObjectClassHandle(
          ObjectClassHandles.convert(entry.getKey())).addAllAttributeHandles(
          AttributeHandles.convert(entry.getValue())));
    }

    federateProxyState.addAllPublishedInteractionClasses(
      InteractionClassHandles.convertToProto(publishedInteractionClasses));

    federateProxyState.setTimeRegulationEnabled(timeRegulationEnabled);
    if (timeRegulationEnabled)
    {
      federateProxyState.setLookahead(LogicalTimeIntervals.convert(lookahead));

      assert lots != null;
      federateProxyState.setLots(LogicalTimes.convert(lots));
    }

    federateProxyState.setTimeConstrainedEnabled(timeConstrainedEnabled);
    federateProxyState.setTimeConstrainedPending(timeConstrainedPending);
    federateProxyState.setAdvanceRequestType(OHLAProtos.AdvanceRequestType.values()[advanceRequestType.ordinal()]);
    if (timeConstrainedEnabled)
    {
      if (advanceRequestTime != null)
      {
        federateProxyState.setAdvanceRequestTime(LogicalTimes.convert(advanceRequestTime));
      }
    }

    if (galt != null)
    {
      federateProxyState.setGalt(LogicalTimes.convert(galt));
    }

    federateProxyState.setFederateTime(LogicalTimes.convert(federateTime));

    federateProxyState.setDiscoveredObjectInstanceHandleCount(discoveredObjects.size());

    out.writeMessageNoTag(federateProxyState.build());

    for (ObjectInstanceHandle objectInstanceHandle : discoveredObjects)
    {
      out.writeMessageNoTag(ObjectInstanceHandles.convert(objectInstanceHandle).build());
    }

    subscriptionManager.saveState(out);
    federateProxyTimeStampOrderedMessageQueue.saveState(out);
  }

  @SuppressWarnings("unchecked")
  private boolean queueTimeStampOrderedMessage(LogicalTime time)
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
          queue = time.compareTo(advanceRequestTime) >= 0;
          break;
        case TIME_ADVANCE_REQUEST_AVAILABLE:
          queue = time.compareTo(advanceRequestTime) > 0;
          break;
        default:
          throw new Error();
      }
    }
    return queue;
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
