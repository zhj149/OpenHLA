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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.fdd.FDD;
import net.sf.ohla.rti.fdd.InteractionClass;
import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleFactory;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleSetFactory;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleValueMapFactory;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeSetRegionSetPairListFactory;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandleFactory;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandleSetFactory;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandleFactory;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandleSetFactory;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eInteractionClassHandleFactory;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eObjectClassHandleFactory;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eObjectInstanceHandleFactory;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eParameterHandleFactory;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eParameterHandleValueMapFactory;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eRegionHandleSetFactory;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eTransportationTypeHandleFactory;
import net.sf.ohla.rti.i18n.ExceptionMessages;
import net.sf.ohla.rti.i18n.I18n;
import net.sf.ohla.rti.i18n.I18nLogger;
import net.sf.ohla.rti.i18n.LogMessages;
import net.sf.ohla.rti.messages.AbortFederationRestore;
import net.sf.ohla.rti.messages.AbortFederationSave;
import net.sf.ohla.rti.messages.FederateRestoreComplete;
import net.sf.ohla.rti.messages.FederateRestoreNotComplete;
import net.sf.ohla.rti.messages.FederateSaveBegun;
import net.sf.ohla.rti.messages.FederateSaveComplete;
import net.sf.ohla.rti.messages.FederateSaveNotComplete;
import net.sf.ohla.rti.messages.GALTAdvanced;
import net.sf.ohla.rti.messages.GALTUndefined;
import net.sf.ohla.rti.messages.GetFederateHandle;
import net.sf.ohla.rti.messages.GetFederateHandleResponse;
import net.sf.ohla.rti.messages.GetFederateName;
import net.sf.ohla.rti.messages.GetFederateNameResponse;
import net.sf.ohla.rti.messages.JoinFederationExecution;
import net.sf.ohla.rti.messages.JoinFederationExecutionResponse;
import net.sf.ohla.rti.messages.MessageDecoder;
import net.sf.ohla.rti.messages.QueryFederationRestoreStatus;
import net.sf.ohla.rti.messages.QueryFederationSaveStatus;
import net.sf.ohla.rti.messages.QueryInteractionTransportationType;
import net.sf.ohla.rti.messages.RegisterFederationSynchronizationPoint;
import net.sf.ohla.rti.messages.RequestFederationSave;
import net.sf.ohla.rti.messages.RequestObjectClassAttributeValueUpdate;
import net.sf.ohla.rti.messages.RequestObjectClassAttributeValueUpdateWithRegions;
import net.sf.ohla.rti.messages.ResignFederationExecution;
import net.sf.ohla.rti.messages.Retract;
import net.sf.ohla.rti.messages.SetAutomaticResignDirective;
import net.sf.ohla.rti.messages.SynchronizationPointAchieved;
import net.sf.ohla.rti.messages.callbacks.ReceiveInteraction;
import net.sf.ohla.rti.messages.callbacks.ReflectAttributeValues;
import net.sf.ohla.rti.messages.callbacks.RemoveObjectInstance;
import net.sf.ohla.rti.messages.callbacks.ReportInteractionTransportationType;
import net.sf.ohla.rti.messages.callbacks.TimeAdvanceGrant;

import org.jboss.netty.channel.Channel;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleFactory;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleSetFactory;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.AttributeHandleValueMapFactory;
import hla.rti1516e.AttributeSetRegionSetPairList;
import hla.rti1516e.AttributeSetRegionSetPairListFactory;
import hla.rti1516e.DimensionHandle;
import hla.rti1516e.DimensionHandleFactory;
import hla.rti1516e.DimensionHandleSet;
import hla.rti1516e.DimensionHandleSetFactory;
import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateHandleFactory;
import hla.rti1516e.FederateHandleSet;
import hla.rti1516e.FederateHandleSetFactory;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.InteractionClassHandleFactory;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.LogicalTimeFactoryFactory;
import hla.rti1516e.LogicalTimeInterval;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.MessageRetractionReturn;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectClassHandleFactory;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.ObjectInstanceHandleFactory;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleFactory;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.ParameterHandleValueMapFactory;
import hla.rti1516e.RangeBounds;
import hla.rti1516e.RegionHandle;
import hla.rti1516e.RegionHandleSet;
import hla.rti1516e.RegionHandleSetFactory;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RestoreStatus;
import hla.rti1516e.SaveFailureReason;
import hla.rti1516e.SaveStatus;
import hla.rti1516e.ServiceGroup;
import hla.rti1516e.TimeQueryReturn;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.TransportationTypeHandleFactory;
import hla.rti1516e.exceptions.AsynchronousDeliveryAlreadyDisabled;
import hla.rti1516e.exceptions.AsynchronousDeliveryAlreadyEnabled;
import hla.rti1516e.exceptions.AttributeAcquisitionWasNotRequested;
import hla.rti1516e.exceptions.AttributeAlreadyBeingAcquired;
import hla.rti1516e.exceptions.AttributeAlreadyBeingChanged;
import hla.rti1516e.exceptions.AttributeAlreadyBeingDivested;
import hla.rti1516e.exceptions.AttributeAlreadyOwned;
import hla.rti1516e.exceptions.AttributeDivestitureWasNotRequested;
import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.AttributeNotOwned;
import hla.rti1516e.exceptions.AttributeNotPublished;
import hla.rti1516e.exceptions.AttributeRelevanceAdvisorySwitchIsOff;
import hla.rti1516e.exceptions.AttributeRelevanceAdvisorySwitchIsOn;
import hla.rti1516e.exceptions.AttributeScopeAdvisorySwitchIsOff;
import hla.rti1516e.exceptions.AttributeScopeAdvisorySwitchIsOn;
import hla.rti1516e.exceptions.CouldNotCreateLogicalTimeFactory;
import hla.rti1516e.exceptions.DeletePrivilegeNotHeld;
import hla.rti1516e.exceptions.FederateAlreadyExecutionMember;
import hla.rti1516e.exceptions.FederateHandleNotKnown;
import hla.rti1516e.exceptions.FederateHasNotBegunSave;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.FederateNameAlreadyInUse;
import hla.rti1516e.exceptions.FederateOwnsAttributes;
import hla.rti1516e.exceptions.FederateServiceInvocationsAreBeingReportedViaMOM;
import hla.rti1516e.exceptions.FederateUnableToUseTime;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.IllegalName;
import hla.rti1516e.exceptions.InTimeAdvancingState;
import hla.rti1516e.exceptions.InconsistentFDD;
import hla.rti1516e.exceptions.InteractionClassNotDefined;
import hla.rti1516e.exceptions.InteractionClassNotPublished;
import hla.rti1516e.exceptions.InteractionParameterNotDefined;
import hla.rti1516e.exceptions.InteractionRelevanceAdvisorySwitchIsOff;
import hla.rti1516e.exceptions.InteractionRelevanceAdvisorySwitchIsOn;
import hla.rti1516e.exceptions.InvalidAttributeHandle;
import hla.rti1516e.exceptions.InvalidDimensionHandle;
import hla.rti1516e.exceptions.InvalidFederateHandle;
import hla.rti1516e.exceptions.InvalidInteractionClassHandle;
import hla.rti1516e.exceptions.InvalidLogicalTime;
import hla.rti1516e.exceptions.InvalidLookahead;
import hla.rti1516e.exceptions.InvalidMessageRetractionHandle;
import hla.rti1516e.exceptions.InvalidObjectClassHandle;
import hla.rti1516e.exceptions.InvalidOrderName;
import hla.rti1516e.exceptions.InvalidOrderType;
import hla.rti1516e.exceptions.InvalidParameterHandle;
import hla.rti1516e.exceptions.InvalidRangeBound;
import hla.rti1516e.exceptions.InvalidRegion;
import hla.rti1516e.exceptions.InvalidRegionContext;
import hla.rti1516e.exceptions.InvalidResignAction;
import hla.rti1516e.exceptions.InvalidTransportationName;
import hla.rti1516e.exceptions.InvalidTransportationType;
import hla.rti1516e.exceptions.InvalidUpdateRateDesignator;
import hla.rti1516e.exceptions.LogicalTimeAlreadyPassed;
import hla.rti1516e.exceptions.MessageCanNoLongerBeRetracted;
import hla.rti1516e.exceptions.NameNotFound;
import hla.rti1516e.exceptions.ObjectClassNotDefined;
import hla.rti1516e.exceptions.ObjectClassNotPublished;
import hla.rti1516e.exceptions.ObjectClassRelevanceAdvisorySwitchIsOff;
import hla.rti1516e.exceptions.ObjectClassRelevanceAdvisorySwitchIsOn;
import hla.rti1516e.exceptions.ObjectInstanceNameInUse;
import hla.rti1516e.exceptions.ObjectInstanceNameNotReserved;
import hla.rti1516e.exceptions.ObjectInstanceNotKnown;
import hla.rti1516e.exceptions.OwnershipAcquisitionPending;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RegionDoesNotContainSpecifiedDimension;
import hla.rti1516e.exceptions.RegionInUseForUpdateOrSubscription;
import hla.rti1516e.exceptions.RegionNotCreatedByThisFederate;
import hla.rti1516e.exceptions.RequestForTimeConstrainedPending;
import hla.rti1516e.exceptions.RequestForTimeRegulationPending;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.RestoreNotInProgress;
import hla.rti1516e.exceptions.RestoreNotRequested;
import hla.rti1516e.exceptions.SaveInProgress;
import hla.rti1516e.exceptions.SaveNotInProgress;
import hla.rti1516e.exceptions.SaveNotInitiated;
import hla.rti1516e.exceptions.SynchronizationPointLabelNotAnnounced;
import hla.rti1516e.exceptions.TimeConstrainedAlreadyEnabled;
import hla.rti1516e.exceptions.TimeConstrainedIsNotEnabled;
import hla.rti1516e.exceptions.TimeRegulationAlreadyEnabled;
import hla.rti1516e.exceptions.TimeRegulationIsNotEnabled;

public class Federate
{
  private final String federateName;
  private final String federateType;
  private final String federationExecutionName;

  private final FederateAmbassador federateAmbassador;

  private final CallbackManager callbackManager;

  /**
   * The channel to the RTI.
   */
  private final Channel rtiChannel;

  private volatile FDD fdd;

  private FederateHandle federateHandle;

  private FederateState federateState = FederateState.ACTIVE;

  private ReadWriteLock federateStateLock = new ReentrantReadWriteLock(true);

  private ResignAction automaticResignDirective;

  private SaveStatus saveStatus;
  private FederateSave federateSave;

  private RestoreStatus restoreStatus;
  private FederateRestore federateRestore;

  private Lock synchronizationPointLock = new ReentrantLock(true);
  private Map<String, FederateSynchronizationPoint> synchronizationPoints =
    new HashMap<String, FederateSynchronizationPoint>();

  private boolean asynchronousDeliveryEnabled;

  private FederateObjectManager objectManager = new FederateObjectManager(this);
  private FederateRegionManager regionManager = new FederateRegionManager(this);
  private FederateMessageRetractionManager messageRetractionManager = new FederateMessageRetractionManager(this);

  private final FederateTimeManager timeManager;

  private Lock futureTasksLock = new ReentrantLock(true);
  private Queue<TimestampedFutureTask> futureTasks = new PriorityQueue<TimestampedFutureTask>();

  private final AttributeHandleFactory attributeHandleFactory =
    IEEE1516eAttributeHandleFactory.INSTANCE;
  private final AttributeHandleSetFactory attributeHandleSetFactory =
    IEEE1516eAttributeHandleSetFactory.INSTANCE;
  private final AttributeHandleValueMapFactory attributeHandleValueMapFactory =
    IEEE1516eAttributeHandleValueMapFactory.INSTANCE;
  private final AttributeSetRegionSetPairListFactory attributeSetRegionSetPairListFactory =
    IEEE1516eAttributeSetRegionSetPairListFactory.INSTANCE;
  private final DimensionHandleFactory dimensionHandleFactory =
    IEEE1516eDimensionHandleFactory.INSTANCE;
  private final DimensionHandleSetFactory dimensionHandleSetFactory =
    IEEE1516eDimensionHandleSetFactory.INSTANCE;
  private final FederateHandleFactory federateHandleFactory =
    IEEE1516eFederateHandleFactory.INSTANCE;
  private final FederateHandleSetFactory federateHandleSetFactory =
    IEEE1516eFederateHandleSetFactory.INSTANCE;
  private final InteractionClassHandleFactory interactionClassHandleFactory =
    IEEE1516eInteractionClassHandleFactory.INSTANCE;
  private final ObjectClassHandleFactory objectClassHandleFactory =
    IEEE1516eObjectClassHandleFactory.INSTANCE;
  private final ObjectInstanceHandleFactory objectInstanceHandleFactory =
    IEEE1516eObjectInstanceHandleFactory.INSTANCE;
  private final ParameterHandleFactory parameterHandleFactory =
    IEEE1516eParameterHandleFactory.INSTANCE;
  private final ParameterHandleValueMapFactory parameterHandleValueMapFactory =
    IEEE1516eParameterHandleValueMapFactory.INSTANCE;
  private final RegionHandleSetFactory regionHandleSetFactory =
    IEEE1516eRegionHandleSetFactory.INSTANCE;
  private final TransportationTypeHandleFactory transportationTypeHandleFactory =
    IEEE1516eTransportationTypeHandleFactory.INSTANCE;

  private final LogicalTimeFactory logicalTimeFactory;

  private final Marker marker;
  private final I18nLogger log;

  private boolean conveyProducingFederate = true;

  public Federate(String federateName, String federateType, String federationExecutionName,
                  List<FDD> additionalFDDs, FederateAmbassador federateAmbassador, CallbackManager callbackManager,
                  Channel rtiChannel)
    throws CouldNotCreateLogicalTimeFactory, FederateNameAlreadyInUse, FederationExecutionDoesNotExist, InconsistentFDD,
           SaveInProgress, RestoreInProgress, FederateAlreadyExecutionMember, RTIinternalError
  {
    this.federateType = federateType;
    this.federationExecutionName = federationExecutionName;
    this.federateAmbassador = federateAmbassador;
    this.callbackManager = callbackManager;
    this.rtiChannel = rtiChannel;

    JoinFederationExecution joinFederationExecution = new JoinFederationExecution(
      federateName, federateType, federationExecutionName, additionalFDDs);

    rtiChannel.write(joinFederationExecution);

    JoinFederationExecutionResponse response = joinFederationExecution.getResponse();
    switch (response.getResponse())
    {
      case FEDERATION_EXECUTION_DOES_NOT_EXIST:
        throw new FederationExecutionDoesNotExist(I18n.getMessage(
          ExceptionMessages.FEDERATION_EXECUTION_DOES_NOT_EXIST, federationExecutionName));
      case SAVE_IN_PROGRESS:
        throw new SaveInProgress(I18n.getMessage(ExceptionMessages.SAVE_IN_PROGRESS, federationExecutionName));
      case RESTORE_IN_PROGRESS:
        throw new RestoreInProgress(I18n.getMessage(ExceptionMessages.RESTORE_IN_PROGRESS, federationExecutionName));
      case INCONSISTENT_FDD:
        // TODO: provide more useful information
        //
        throw new InconsistentFDD("");
      case SUCCESS:
        federateHandle = response.getFederateHandle();

        // don't assign federate name until we have the handle in case the name was null
        //
        this.federateName = federateName == null ? defaultFederateName(federateHandle) : federateName;

        marker = MarkerFactory.getMarker(federationExecutionName + "." + this.federateName);
        log = I18nLogger.getLogger(marker, getClass());

        fdd = response.getFDD();

        logicalTimeFactory = LogicalTimeFactoryFactory.getLogicalTimeFactory(
          response.getLogicalTimeImplementationName());

        if (logicalTimeFactory == null)
        {
          rtiChannel.write(new ResignFederationExecution(ResignAction.NO_ACTION));

          throw new CouldNotCreateLogicalTimeFactory(I18n.getMessage(
            ExceptionMessages.COULD_NOT_CREATE_LOGICAL_TIME_FACTORY, response.getLogicalTimeImplementationName()));
        }
        else
        {
          timeManager = new FederateTimeManager(this, logicalTimeFactory);

          rtiChannel.getPipeline().addBefore(
            CallbackManagerChannelUpstreamHandler.NAME, FederateChannelUpstreamHandler.NAME,
            new FederateChannelUpstreamHandler(this));

          ((MessageDecoder) rtiChannel.getPipeline().get(MessageDecoder.NAME)).setLogicalTimeFactory(
            logicalTimeFactory);
        }
        break;
      default:
        throw new RTIinternalError(I18n.getMessage(ExceptionMessages.UNEXPECTED_EXCEPTION));
    }
  }

  public String getFederateName()
  {
    return federateName;
  }

  public String getFederateType()
  {
    return federateType;
  }

  public String getFederationExecutionName()
  {
    return federationExecutionName;
  }

  public FederateHandle getFederateHandle()
  {
    return federateHandle;
  }

  public Channel getRTIChannel()
  {
    return rtiChannel;
  }

  public FDD getFDD()
  {
    return fdd;
  }

  public FederateObjectManager getObjectManager()
  {
    return objectManager;
  }

  public FederateRegionManager getRegionManager()
  {
    return regionManager;
  }

  public FederateMessageRetractionManager getMessageRetractionManager()
  {
    return messageRetractionManager;
  }

  public FederateTimeManager getTimeManager()
  {
    return timeManager;
  }

  public CallbackManager getCallbackManager()
  {
    return callbackManager;
  }

  public Marker getMarker()
  {
    return marker;
  }

  public boolean isAsynchronousDeliveryEnabled()
  {
    return asynchronousDeliveryEnabled;
  }

  public boolean isConveyProducingFederate()
  {
    return conveyProducingFederate;
  }

  public void fddUpdated(FDD fdd)
  {
    this.fdd = fdd;
  }

  @SuppressWarnings("unchecked")
  public void processFutureTasks(LogicalTime maxFutureTaskTimestamp)
  {
    futureTasksLock.lock();
    try
    {
      log.debug(LogMessages.PROCESSING_FUTURE_TASKS, maxFutureTaskTimestamp);

      for (TimestampedFutureTask timestampedFutureTask = futureTasks.peek();
           timestampedFutureTask != null && timestampedFutureTask.getTime().compareTo(maxFutureTaskTimestamp) <= 0;
           timestampedFutureTask = futureTasks.peek())
      {
        log.debug(LogMessages.PROCESSING_FUTURE_TASK, timestampedFutureTask);

        try
        {
          timestampedFutureTask.run();
        }
        catch (Throwable t)
        {
          log.error(LogMessages.UNABLE_TO_EXECUTE_SCHEDULED_TASK, t, timestampedFutureTask);
        }

        futureTasks.poll();
      }
    }
    finally
    {
      futureTasksLock.unlock();
    }
  }

  public void clearFutureTasks()
  {
    futureTasksLock.lock();
    try
    {
      log.debug(LogMessages.CLEARING_FUTURE_TASKS, futureTasks.size());

      for (TimestampedFutureTask timestampedFutureTask = futureTasks.poll();
           timestampedFutureTask != null; timestampedFutureTask = futureTasks.poll())
      {
        log.debug(LogMessages.PROCESSING_FUTURE_TASK, timestampedFutureTask);

        try
        {
          timestampedFutureTask.run();
        }
        catch (Throwable t)
        {
          log.error(LogMessages.UNABLE_TO_EXECUTE_SCHEDULED_TASK, t, timestampedFutureTask);
        }
      }
    }
    finally
    {
      futureTasksLock.unlock();
    }
  }

  public void reflectAttributeValues(ReflectAttributeValues reflectAttributeValues)
  {
    timeManager.getTimeLock().readLock().lock();
    try
    {
      OrderType receivedOrderType =
        reflectAttributeValues.getSentOrderType() == OrderType.TIMESTAMP &&
        timeManager.isTimeConstrained() && timeManager.galtDefined() ? OrderType.TIMESTAMP : OrderType.RECEIVE;
      reflectAttributeValues.setReceivedOrderType(receivedOrderType);

      if (receivedOrderType == OrderType.RECEIVE)
      {
        // receive order callbacks need to be held until released if we are constrained and in the time granted state
        // if asynchronous delivery is disabled
        //
        boolean hold = timeManager.isTimeConstrainedAndTimeGranted() && !isAsynchronousDeliveryEnabled();

        callbackManager.add(reflectAttributeValues, hold);
      }
      else
      {
        // schedule the callback for the appropriate time
        //
        Future future = schedule(reflectAttributeValues.getTime(), new AddCallback(reflectAttributeValues));

        // register the message retraction handle
        //
        messageRetractionManager.add(
          reflectAttributeValues.getTime(), future, reflectAttributeValues.getMessageRetractionHandle());
      }
    }
    finally
    {
      timeManager.getTimeLock().readLock().unlock();
    }
  }

  public void receiveInteraction(ReceiveInteraction receiveInteraction)
  {
    timeManager.getTimeLock().readLock().lock();
    try
    {
      OrderType receivedOrderType =
        receiveInteraction.getSentOrderType() == OrderType.TIMESTAMP && timeManager.isTimeConstrained() ?
          OrderType.TIMESTAMP : OrderType.RECEIVE;

      receiveInteraction.setReceivedOrderType(receivedOrderType);

      if (receivedOrderType == OrderType.RECEIVE)
      {
        // receive order callbacks need to be held until released if we are constrained and in the time granted state,
        // if asynchronous delivery is disabled
        //
        boolean hold = timeManager.isTimeConstrainedAndTimeGranted() && !isAsynchronousDeliveryEnabled();

        callbackManager.add(receiveInteraction, hold);
      }
      else
      {
        // schedule the callback for the appropriate time
        //
        Future future = schedule(receiveInteraction.getTime(), new AddCallback(receiveInteraction));

        // register the message retraction handle
        //
        messageRetractionManager.add(
          receiveInteraction.getTime(), future, receiveInteraction.getMessageRetractionHandle());
      }
    }
    finally
    {
      timeManager.getTimeLock().readLock().unlock();
    }
  }

  public void removeObjectInstance(RemoveObjectInstance removeObjectInstance)
  {
    timeManager.getTimeLock().readLock().lock();
    try
    {
      OrderType receivedOrderType =
        removeObjectInstance.getSentOrderType() == OrderType.TIMESTAMP &&
        timeManager.isTimeConstrained() ? OrderType.TIMESTAMP :
          OrderType.RECEIVE;

      removeObjectInstance.setReceivedOrderType(receivedOrderType);

      if (receivedOrderType == OrderType.RECEIVE)
      {
        // receive order callbacks need to be held until released if we
        // are constrained and in the time granted state, if asynchronous
        // delivery is disabled
        //
        boolean hold = timeManager.isTimeConstrainedAndTimeGranted() &&
                       !isAsynchronousDeliveryEnabled();

        callbackManager.add(removeObjectInstance, hold);
      }
      else
      {
        // schedule the callback for the appropriate time
        //
        Future future = schedule(removeObjectInstance.getTime(), new AddCallback(removeObjectInstance));

        // register the message retraction handle
        //
        messageRetractionManager.add(
          removeObjectInstance.getTime(), future, removeObjectInstance.getMessageRetractionHandle());
      }
    }
    finally
    {
      timeManager.getTimeLock().readLock().unlock();
    }
  }

  public void timeAdvanceGrant(TimeAdvanceGrant timeAdvanceGrant)
  {
    processFutureTasks(timeAdvanceGrant.getTime());

    callbackManager.add(timeAdvanceGrant);
  }

  public void callbackReceived(Callback callback)
  {
    callbackManager.add(callback);
  }

  public void galtAdvanced(GALTAdvanced galtAdvanced)
  {
    timeManager.galtAdvanced(galtAdvanced.getTime());
  }

  public void galtUndefined(GALTUndefined galtUndefined)
  {
    timeManager.galtUndefined();
  }

  public void resignFederationExecution(ResignAction resignAction)
    throws OwnershipAcquisitionPending, FederateOwnsAttributes, RTIinternalError
  {
    federateStateLock.writeLock().lock();
    try
    {
      objectManager.resignFederationExecution(resignAction);

      federateState = FederateState.RESIGNED;

      rtiChannel.getPipeline().remove(FederateChannelUpstreamHandler.NAME);
    }
    finally
    {
      federateStateLock.writeLock().unlock();
    }
  }

  public void registerFederationSynchronizationPoint(String label, byte[] tag, FederateHandleSet federateHandles)
    throws SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      rtiChannel.write(new RegisterFederationSynchronizationPoint(label, tag, federateHandles));
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void synchronizationPointAchieved(String label, boolean success)
    throws SynchronizationPointLabelNotAnnounced, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      synchronizationPointLock.lock();
      try
      {
        FederateSynchronizationPoint federateSynchronizationPoint = synchronizationPoints.get(label);
        if (federateSynchronizationPoint == null)
        {
          throw new SynchronizationPointLabelNotAnnounced(I18n.getMessage(
            ExceptionMessages.SYNCHRONIZATION_POINT_LABEL_NOT_ANNOUNCED, label));
        }
        else if (federateSynchronizationPoint.synchronizationPointAchieved())
        {
          rtiChannel.write(new SynchronizationPointAchieved(label, success));
        }
        else
        {
          log.debug(LogMessages.SYNCHRONIZATION_POINT_ALREADY_ACHIEVED, label);
        }
      }
      finally
      {
        synchronizationPointLock.unlock();
      }
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void requestFederationSave(String label)
    throws SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.writeLock().lock();
    try
    {
      checkIfActive();

      RequestFederationSave requestFederationSave = new RequestFederationSave(label);

      rtiChannel.write(requestFederationSave);

      switch (requestFederationSave.getResponse().getResponse())
      {
        case SAVE_IN_PROGRESS:
          throw new SaveInProgress(I18n.getMessage(ExceptionMessages.SAVE_IN_PROGRESS, federationExecutionName));
        case RESTORE_IN_PROGRESS:
          throw new RestoreInProgress(I18n.getMessage(ExceptionMessages.RESTORE_IN_PROGRESS, federationExecutionName));
        case SUCCESS:
          break;
      }
    }
    finally
    {
      federateStateLock.writeLock().unlock();
    }
  }

  public void requestFederationSave(String label, LogicalTime time)
    throws LogicalTimeAlreadyPassed, InvalidLogicalTime, FederateUnableToUseTime, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    federateStateLock.writeLock().lock();
    try
    {
      checkIfActive();

      // no need to lock time manager because we have a write lock on the
      // federate state
      //
      timeManager.checkIfLogicalTimeAlreadyPassed(time);

      RequestFederationSave requestFederationSave = new RequestFederationSave(label, time);
      rtiChannel.write(requestFederationSave);

      switch (requestFederationSave.getResponse().getResponse())
      {
        case LOGICAL_TIME_ALREADY_PASSED:
          throw new LogicalTimeAlreadyPassed(time.toString());
        case SAVE_IN_PROGRESS:
          throw new SaveInProgress(I18n.getMessage(ExceptionMessages.SAVE_IN_PROGRESS, federationExecutionName));
        case RESTORE_IN_PROGRESS:
          throw new RestoreInProgress(I18n.getMessage(ExceptionMessages.RESTORE_IN_PROGRESS, federationExecutionName));
        case SUCCESS:
          break;
      }
    }
    finally
    {
      federateStateLock.writeLock().unlock();
    }
  }

  public void federateSaveBegun()
    throws SaveNotInitiated, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      if (saveStatus != SaveStatus.FEDERATE_INSTRUCTED_TO_SAVE)
      {
        throw new SaveNotInitiated(I18n.getMessage(ExceptionMessages.SAVE_NOT_INITIATED, this));
      }

      saveStatus = SaveStatus.FEDERATE_SAVING;

      rtiChannel.write(new FederateSaveBegun());
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void federateSaveComplete()
    throws FederateHasNotBegunSave, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfRestoreInProgress();

      if (saveStatus != SaveStatus.FEDERATE_SAVING)
      {
        throw new FederateHasNotBegunSave(I18n.getMessage(ExceptionMessages.FEDERATE_HAS_NOT_BEGUN_SAVE, this));
      }

      saveStatus = SaveStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_SAVE;

      FederateSave federateSave = new FederateSave();

      // TODO: collect all items to save

      rtiChannel.write(new FederateSaveComplete(federateSave));
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void federateSaveNotComplete()
    throws FederateHasNotBegunSave, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfRestoreInProgress();

      if (saveStatus != SaveStatus.FEDERATE_SAVING)
      {
        throw new FederateHasNotBegunSave(I18n.getMessage(ExceptionMessages.FEDERATE_HAS_NOT_BEGUN_SAVE, this));
      }

      saveStatus = SaveStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_SAVE;

      rtiChannel.write(new FederateSaveNotComplete());
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void abortFederationSave()
    throws SaveNotInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      AbortFederationSave abortFederationSave = new AbortFederationSave();

      rtiChannel.write(abortFederationSave);

      switch (abortFederationSave.getResponse().getResponse())
      {
        case SAVE_NOT_IN_PROGRESS:
          throw new SaveNotInProgress(I18n.getMessage(ExceptionMessages.SAVE_NOT_IN_PROGRESS, this));
        case SUCCESS:
          break;
      }
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void queryFederationSaveStatus()
    throws RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfRestoreInProgress();

      rtiChannel.write(new QueryFederationSaveStatus());
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void federateRestoreComplete()
    throws RestoreNotRequested, SaveInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfSaveInProgress();

      if (restoreStatus != RestoreStatus.FEDERATE_RESTORING)
      {
        throw new RestoreNotRequested(I18n.getMessage(
          ExceptionMessages.RESTORE_NOT_IN_PROGRESS, federationExecutionName));
      }

      restoreStatus = RestoreStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_RESTORE;

      rtiChannel.write(new FederateRestoreComplete());
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void federateRestoreNotComplete()
    throws RestoreNotRequested, SaveInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfSaveInProgress();

      if (restoreStatus != RestoreStatus.FEDERATE_RESTORING)
      {
        throw new RestoreNotRequested(I18n.getMessage(
          ExceptionMessages.RESTORE_NOT_IN_PROGRESS, federationExecutionName));
      }

      restoreStatus = RestoreStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_RESTORE;

      rtiChannel.write(new FederateRestoreNotComplete());
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void abortFederationRestore()
    throws RestoreNotInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      AbortFederationRestore abortFederationRestore = new AbortFederationRestore();

      rtiChannel.write(abortFederationRestore);

      switch (abortFederationRestore.getResponse().getResponse())
      {
        case RESTORE_NOT_IN_PROGRESS:
          throw new RestoreNotInProgress(I18n.getMessage(
            ExceptionMessages.RESTORE_NOT_IN_PROGRESS, federationExecutionName));
        case SUCCESS:
          break;
      }
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void queryFederationRestoreStatus()
    throws SaveInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfSaveInProgress();

      rtiChannel.write(new QueryFederationRestoreStatus());
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void publishObjectClassAttributes(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    // ensure we have a valid object class and attribute handles
    //
    fdd.checkIfAttributeNotDefined(objectClassHandle, attributeHandles);

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.publishObjectClassAttributes(objectClassHandle, attributeHandles);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void unpublishObjectClass(ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined, OwnershipAcquisitionPending, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    // ensure we have a valid object class handle
    //
    fdd.checkIfObjectClassNotDefined(objectClassHandle);

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.unpublishObjectClass(objectClassHandle);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void unpublishObjectClassAttributes(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined, OwnershipAcquisitionPending, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    // ensure we have a valid object class handle
    //
    fdd.checkIfObjectClassNotDefined(objectClassHandle);

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.unpublishObjectClassAttributes(objectClassHandle, attributeHandles);

      // TODO: give up ownership of the specified attributes
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void publishInteractionClass(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    // ensure we have a valid interaction class handle
    //
    fdd.checkIfInteractionClassNotDefined(interactionClassHandle);

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.publishInteractionClass(interactionClassHandle);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void unpublishInteractionClass(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    // ensure we have a valid interaction class handle
    //
    fdd.checkIfInteractionClassNotDefined(interactionClassHandle);

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.unpublishInteractionClass(interactionClassHandle);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void subscribeObjectClassAttributes(ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    subscribeObjectClassAttributes(objectClassHandle, attributeHandles, false);
  }

  public void subscribeObjectClassAttributesPassively(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    subscribeObjectClassAttributes(objectClassHandle, attributeHandles, true);
  }

  private void subscribeObjectClassAttributes(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles, boolean passive)
    throws ObjectClassNotDefined, AttributeNotDefined, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    ObjectClass objectClass = fdd.getObjectClass(objectClassHandle);
    objectClass.checkIfAttributeNotDefined(attributeHandles);

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.subscribeObjectClassAttributes(objectClass, attributeHandles, passive);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void unsubscribeObjectClass(ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    // ensure we have a valid object class handle
    //
    fdd.checkIfObjectClassNotDefined(objectClassHandle);

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.unsubscribeObjectClass(objectClassHandle);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void unsubscribeObjectClassAttributes(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    // ensure we have a valid object class handle
    //
    fdd.checkIfObjectClassNotDefined(objectClassHandle);

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.unsubscribeObjectClassAttributes(objectClassHandle, attributeHandles);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void subscribeInteractionClass(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, FederateServiceInvocationsAreBeingReportedViaMOM, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    subscribeInteractionClass(interactionClassHandle, false);
  }

  public void subscribeInteractionClassPassively(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, FederateServiceInvocationsAreBeingReportedViaMOM, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    subscribeInteractionClass(interactionClassHandle, true);
  }

  private void subscribeInteractionClass(InteractionClassHandle interactionClassHandle, boolean passive)
    throws InteractionClassNotDefined, FederateServiceInvocationsAreBeingReportedViaMOM, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    InteractionClass interactionClass = fdd.getInteractionClass(interactionClassHandle);

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.subscribeInteractionClass(interactionClass, passive);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void unsubscribeInteractionClass(
    InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    // ensure we have a valid interaction class handle
    //
    fdd.checkIfInteractionClassNotDefined(interactionClassHandle);

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.unsubscribeInteractionClass(interactionClassHandle);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void reserveObjectInstanceName(String objectInstanceName)
    throws IllegalName, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.reserveObjectInstanceName(objectInstanceName);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void releaseObjectInstanceName(String name)
    throws ObjectInstanceNameNotReserved, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.releaseObjectInstanceName(name);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void reserveMultipleObjectInstanceName(Set<String> names)
    throws IllegalName, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.reserveMultipleObjectInstanceName(names);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void releaseMultipleObjectInstanceName(Set<String> names)
    throws ObjectInstanceNameNotReserved, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.releaseMultipleObjectInstanceName(names);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public ObjectInstanceHandle registerObjectInstance(ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined, ObjectClassNotPublished, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      return objectManager.registerObjectInstance(objectClassHandle);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public ObjectInstanceHandle registerObjectInstance(ObjectClassHandle objectClassHandle, String objectInstanceName)
    throws ObjectClassNotDefined, ObjectClassNotPublished, ObjectInstanceNameNotReserved, ObjectInstanceNameInUse,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      return objectManager.registerObjectInstance(objectClassHandle, objectInstanceName);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void updateAttributeValues(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.updateAttributeValues(objectInstanceHandle, attributeValues, tag);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public MessageRetractionReturn updateAttributeValues(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues,
    byte[] tag, LogicalTime updateTime)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned, InvalidLogicalTime, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      MessageRetractionHandle messageRetractionHandle = null;

      timeManager.getTimeLock().readLock().lock();
      try
      {
        timeManager.updateAttributeValues(updateTime);

        OrderType sentOrderType =
          timeManager.isTimeRegulating() && updateTime != null ? OrderType.TIMESTAMP : OrderType.RECEIVE;

        if (sentOrderType == OrderType.TIMESTAMP)
        {
          messageRetractionHandle = messageRetractionManager.add(updateTime);
        }

        objectManager.updateAttributeValues(
          objectInstanceHandle, attributeValues, tag, updateTime, messageRetractionHandle, sentOrderType);
      }
      finally
      {
        timeManager.getTimeLock().readLock().unlock();
      }

      return new MessageRetractionReturn(messageRetractionHandle != null, messageRetractionHandle);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void sendInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag)
    throws InteractionClassNotPublished, InteractionClassNotDefined, InteractionParameterNotDefined, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    // ensure we have a valid interaction class and parameter handles
    //
    fdd.checkIfInteractionParameterNotDefined(interactionClassHandle, parameterValues.keySet());

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.sendInteraction(interactionClassHandle, parameterValues, tag);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public MessageRetractionReturn sendInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues,
    byte[] tag, LogicalTime sendTime)
    throws InteractionClassNotPublished, InteractionClassNotDefined, InteractionParameterNotDefined,
           InvalidLogicalTime, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    // ensure we have a valid interaction class and parameter handles
    //
    fdd.checkIfInteractionParameterNotDefined(interactionClassHandle, parameterValues.keySet());

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      MessageRetractionHandle messageRetractionHandle = null;

      timeManager.getTimeLock().readLock().lock();
      try
      {
        timeManager.sendInteraction(sendTime);

        OrderType sentOrderType =
          timeManager.isTimeRegulating() && sendTime != null ? OrderType.TIMESTAMP : OrderType.RECEIVE;

        if (sentOrderType == OrderType.TIMESTAMP)
        {
          messageRetractionHandle = messageRetractionManager.add(sendTime);
        }

        objectManager.sendInteraction(
          interactionClassHandle, parameterValues, tag, sendTime, messageRetractionHandle, sentOrderType);
      }
      finally
      {
        timeManager.getTimeLock().readLock().unlock();
      }

      return new MessageRetractionReturn(messageRetractionHandle != null, messageRetractionHandle);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void deleteObjectInstance(ObjectInstanceHandle objectInstanceHandle, byte[] tag)
    throws ObjectInstanceNotKnown, DeletePrivilegeNotHeld, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.deleteObjectInstance(objectInstanceHandle, tag);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public MessageRetractionReturn deleteObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag, LogicalTime deleteTime)
    throws ObjectInstanceNotKnown, DeletePrivilegeNotHeld, InvalidLogicalTime, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      MessageRetractionHandle messageRetractionHandle = null;

      timeManager.getTimeLock().readLock().lock();
      try
      {
        timeManager.deleteObjectInstance(deleteTime);

        OrderType sentOrderType =
          timeManager.isTimeRegulating() && deleteTime != null ?
            OrderType.TIMESTAMP : OrderType.RECEIVE;

        if (sentOrderType == OrderType.TIMESTAMP)
        {
          messageRetractionHandle = messageRetractionManager.add(deleteTime);
        }

        objectManager.deleteObjectInstance(
          objectInstanceHandle, tag, deleteTime, messageRetractionHandle,
          sentOrderType);
      }
      finally
      {
        timeManager.getTimeLock().readLock().unlock();
      }

      return new MessageRetractionReturn(messageRetractionHandle != null, messageRetractionHandle);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void localDeleteObjectInstance(ObjectInstanceHandle objectInstanceHandle)
    throws ObjectInstanceNotKnown, FederateOwnsAttributes, OwnershipAcquisitionPending, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    objectManager.localDeleteObjectInstance(objectInstanceHandle);
  }

  public void requestAttributeTransportationTypeChange(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles,
    TransportationTypeHandle transportationTypeHandle)
    throws AttributeAlreadyBeingChanged, AttributeNotOwned, AttributeNotDefined, ObjectInstanceNotKnown,
           InvalidTransportationType, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      fdd.checkIfInvalidTransportationType(transportationTypeHandle);

      objectManager.requestAttributeTransportationTypeChange(
        objectInstanceHandle, attributeHandles, transportationTypeHandle);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void queryAttributeTransportationType(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
    throws AttributeNotDefined, ObjectInstanceNotKnown, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.queryAttributeTransportationType(objectInstanceHandle, attributeHandle);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void requestInteractionTransportationTypeChange(
    InteractionClassHandle interactionClassHandle, TransportationTypeHandle transportationTypeHandle)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.requestInteractionTransportationTypeChange(interactionClassHandle, transportationTypeHandle);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void queryInteractionTransportationType(
    FederateHandle federateHandle, InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      if (federateHandle.equals(this.federateHandle))
      {
        callbackManager.add(new ReportInteractionTransportationType(
          interactionClassHandle, federateHandle,
          fdd.getInteractionClass(interactionClassHandle).getTransportationTypeHandle()));
      }
      else
      {
        rtiChannel.write(new QueryInteractionTransportationType(interactionClassHandle, federateHandle));
      }
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void requestAttributeValueUpdate(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.requestAttributeValueUpdate(objectInstanceHandle, attributeHandles, tag);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void requestAttributeValueUpdate(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectClassNotDefined, AttributeNotDefined, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    // check to see if the handles are valid
    //
    fdd.checkIfAttributeNotDefined(objectClassHandle, attributeHandles);

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      rtiChannel.write(new RequestObjectClassAttributeValueUpdate(objectClassHandle, attributeHandles, tag));
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void unconditionalAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.unconditionalAttributeOwnershipDivestiture(objectInstanceHandle, attributeHandles);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void negotiatedAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws AttributeAlreadyBeingDivested, AttributeNotOwned, AttributeNotDefined, ObjectInstanceNotKnown,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.negotiatedAttributeOwnershipDivestiture(objectInstanceHandle, attributeHandles, tag);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void confirmDivestiture(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned, AttributeDivestitureWasNotRequested,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.confirmDivestiture(objectInstanceHandle, attributeHandles, tag);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisition(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, ObjectClassNotPublished, AttributeNotDefined, AttributeNotPublished,
           FederateOwnsAttributes, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.attributeOwnershipAcquisition(objectInstanceHandle, attributeHandles, tag);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisitionIfAvailable(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, ObjectClassNotPublished, AttributeNotDefined, AttributeNotPublished,
           FederateOwnsAttributes, AttributeAlreadyBeingAcquired, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.attributeOwnershipAcquisitionIfAvailable(objectInstanceHandle, attributeHandles);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void attributeOwnershipReleaseDenied(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws AttributeNotOwned, AttributeNotDefined, ObjectInstanceNotKnown, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.attributeOwnershipReleaseDenied(objectInstanceHandle, attributeHandles);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public AttributeHandleSet attributeOwnershipDivestitureIfWanted(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      return objectManager.attributeOwnershipDivestitureIfWanted(objectInstanceHandle, attributeHandles);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned, AttributeDivestitureWasNotRequested,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.cancelNegotiatedAttributeOwnershipDivestiture(objectInstanceHandle, attributeHandles);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void cancelAttributeOwnershipAcquisition(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeAlreadyOwned, AttributeAcquisitionWasNotRequested,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.cancelAttributeOwnershipAcquisition(objectInstanceHandle, attributeHandles);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void queryAttributeOwnership(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
    throws ObjectInstanceNotKnown, AttributeNotDefined, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.queryAttributeOwnership(objectInstanceHandle, attributeHandle);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public boolean isAttributeOwnedByFederate(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
    throws ObjectInstanceNotKnown, AttributeNotDefined, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      return objectManager.isAttributeOwnedByFederate(objectInstanceHandle, attributeHandle);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void enableTimeRegulation(LogicalTimeInterval lookahead)
    throws TimeRegulationAlreadyEnabled, InvalidLookahead, InTimeAdvancingState, RequestForTimeRegulationPending,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      timeManager.enableTimeRegulation(lookahead);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void disableTimeRegulation()
    throws TimeRegulationIsNotEnabled, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      timeManager.disableTimeRegulation();
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void enableTimeConstrained()
    throws TimeConstrainedAlreadyEnabled, InTimeAdvancingState, RequestForTimeConstrainedPending, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      timeManager.enableTimeConstrained();
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void disableTimeConstrained()
    throws TimeConstrainedIsNotEnabled, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      timeManager.disableTimeConstrained();
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void timeAdvanceRequest(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState, RequestForTimeRegulationPending,
           RequestForTimeConstrainedPending, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      timeManager.timeAdvanceRequest(time);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void timeAdvanceRequestAvailable(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState, RequestForTimeRegulationPending,
           RequestForTimeConstrainedPending, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      timeManager.timeAdvanceRequestAvailable(time);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void nextMessageRequest(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState, RequestForTimeRegulationPending,
           RequestForTimeConstrainedPending, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      timeManager.nextMessageRequest(time);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void nextMessageRequestAvailable(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState, RequestForTimeRegulationPending,
           RequestForTimeConstrainedPending, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      timeManager.nextMessageRequestAvailable(time);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void flushQueueRequest(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState, RequestForTimeRegulationPending,
           RequestForTimeConstrainedPending, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      timeManager.flushQueueRequest(time);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void enableAsynchronousDelivery()
    throws AsynchronousDeliveryAlreadyEnabled, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      if (isAsynchronousDeliveryEnabled())
      {
        throw new AsynchronousDeliveryAlreadyEnabled(I18n.getMessage(
          ExceptionMessages.ASYNCHRONOUS_DELIVERY_ALREADY_ENABLED, this));
      }

      asynchronousDeliveryEnabled = true;
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void disableAsynchronousDelivery()
    throws AsynchronousDeliveryAlreadyDisabled, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      if (!isAsynchronousDeliveryEnabled())
      {
        throw new AsynchronousDeliveryAlreadyDisabled(I18n.getMessage(
          ExceptionMessages.ASYNCHRONOUS_DELIVERY_ALREADY_DISABLED, this));
      }

      asynchronousDeliveryEnabled = false;
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public TimeQueryReturn queryGALT()
    throws SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      return timeManager.queryGALT();
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public LogicalTime queryLogicalTime()
    throws SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      return timeManager.queryLogicalTime();
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public TimeQueryReturn queryLITS()
    throws SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      return timeManager.queryLITS();
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void modifyLookahead(LogicalTimeInterval lookahead)
    throws TimeRegulationIsNotEnabled, InvalidLookahead, InTimeAdvancingState, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      timeManager.modifyLookahead(lookahead);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public LogicalTimeInterval queryLookahead()
    throws TimeRegulationIsNotEnabled, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      return timeManager.queryLookahead();
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void retract(MessageRetractionHandle messageRetractionHandle)
    throws InvalidMessageRetractionHandle, TimeRegulationIsNotEnabled, MessageCanNoLongerBeRetracted,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      timeManager.getTimeLock().readLock().lock();
      try
      {
        timeManager.checkIfTimeRegulationIsNotEnabled();

        messageRetractionManager.retract(messageRetractionHandle, timeManager.queryLogicalTime());

        rtiChannel.write(new Retract(messageRetractionHandle));
      }
      finally
      {
        timeManager.getTimeLock().readLock().unlock();
      }
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void changeAttributeOrderType(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, OrderType orderType)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.changeAttributeOrderType(objectInstanceHandle, attributeHandles, orderType);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void changeInteractionOrderType(InteractionClassHandle interactionClassHandle, OrderType orderType)
    throws InteractionClassNotDefined, InteractionClassNotPublished, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.changeInteractionOrderType(interactionClassHandle, orderType);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public RegionHandle createRegion(DimensionHandleSet dimensionHandles)
    throws InvalidDimensionHandle, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    // check to see if the handles are valid
    //
    fdd.checkIfInvalidDimensionHandle(dimensionHandles);

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      return regionManager.createRegion(dimensionHandles);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void commitRegionModifications(RegionHandleSet regionHandles)
    throws InvalidRegion, RegionNotCreatedByThisFederate, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      regionManager.commitRegionModifications(regionHandles);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void deleteRegion(RegionHandle regionHandle)
    throws InvalidRegion, RegionNotCreatedByThisFederate, RegionInUseForUpdateOrSubscription, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      regionManager.deleteRegion(regionHandle);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void deleteRegions(RegionHandleSet regionHandles)
    throws InvalidRegion, RegionNotCreatedByThisFederate, RegionInUseForUpdateOrSubscription, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      regionManager.deleteRegions(regionHandles);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public ObjectInstanceHandle registerObjectInstanceWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectClassNotDefined, ObjectClassNotPublished, AttributeNotDefined, AttributeNotPublished, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      return objectManager.registerObjectInstanceWithRegions(objectClassHandle, attributesAndRegions);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public ObjectInstanceHandle registerObjectInstanceWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions, String objectInstanceName)
    throws ObjectClassNotDefined, ObjectClassNotPublished, AttributeNotDefined, AttributeNotPublished, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext, ObjectInstanceNameNotReserved, ObjectInstanceNameInUse,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      return objectManager.registerObjectInstanceWithRegions(
        objectClassHandle, attributesAndRegions, objectInstanceName);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void associateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle, AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectInstanceNotKnown, AttributeNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
           InvalidRegionContext, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.associateRegionsForUpdates(objectInstanceHandle, attributesAndRegions);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void unassociateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle, AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectInstanceNotKnown, AttributeNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.unassociateRegionsForUpdates(objectInstanceHandle, attributesAndRegions);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void subscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
           InvalidRegionContext, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    subscribeObjectClassAttributesWithRegions(objectClassHandle, attributesAndRegions, null, false);
  }

  public void subscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions,
    String updateRateDesignator)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
           InvalidRegionContext, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    subscribeObjectClassAttributesWithRegions(objectClassHandle, attributesAndRegions, updateRateDesignator, false);
  }

  public void subscribeObjectClassAttributesPassivelyWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
           InvalidRegionContext, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    subscribeObjectClassAttributesWithRegions(objectClassHandle, attributesAndRegions, null, true);
  }

  public void subscribeObjectClassAttributesPassivelyWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions,
    String updateRateDesignator)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
           InvalidRegionContext, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    subscribeObjectClassAttributesWithRegions(objectClassHandle, attributesAndRegions, updateRateDesignator, true);
  }

  public void subscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions,
    String updateRateDesignator, boolean passive)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
           InvalidRegionContext, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.subscribeObjectClassAttributesWithRegions(objectClassHandle, attributesAndRegions, passive);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void unsubscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.unsubscribeObjectClassAttributesWithRegions(objectClassHandle, attributesAndRegions);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void subscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle, RegionHandleSet regionHandles)
    throws InteractionClassNotDefined, InvalidRegion, RegionNotCreatedByThisFederate, InvalidRegionContext,
           FederateServiceInvocationsAreBeingReportedViaMOM, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    subscribeInteractionClassWithRegions(interactionClassHandle, regionHandles, false);
  }

  public void subscribeInteractionClassPassivelyWithRegions(
    InteractionClassHandle interactionClassHandle, RegionHandleSet regionHandles)
    throws InteractionClassNotDefined, InvalidRegion, RegionNotCreatedByThisFederate, InvalidRegionContext,
           FederateServiceInvocationsAreBeingReportedViaMOM, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    subscribeInteractionClassWithRegions(interactionClassHandle, regionHandles, true);
  }

  private void subscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle, RegionHandleSet regionHandles, boolean passive)
    throws InteractionClassNotDefined, InvalidRegion, RegionNotCreatedByThisFederate, InvalidRegionContext,
           FederateServiceInvocationsAreBeingReportedViaMOM, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    InteractionClass interactionClass = fdd.getInteractionClass(interactionClassHandle);

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.subscribeInteractionClassWithRegions(interactionClass, regionHandles, passive);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void unsubscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle, RegionHandleSet regionHandles)
    throws InteractionClassNotDefined, InvalidRegion, RegionNotCreatedByThisFederate, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    fdd.checkIfInteractionClassNotDefined(interactionClassHandle);

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.unsubscribeInteractionClassWithRegions(interactionClassHandle, regionHandles);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void sendInteractionWithRegions(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues,
    RegionHandleSet regionHandles, byte[] tag)
    throws InteractionClassNotDefined, InteractionClassNotPublished, InteractionParameterNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    // ensure we have a valid interaction class and parameter handles
    //
    InteractionClass interactionClass = fdd.getInteractionClass(interactionClassHandle);
    interactionClass.checkIfInteractionParameterNotDefined(parameterValues.keySet());

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.sendInteractionWithRegions(interactionClass, parameterValues, regionHandles, tag);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public MessageRetractionReturn sendInteractionWithRegions(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues,
    RegionHandleSet regionHandles, byte[] tag, LogicalTime time)
    throws InteractionClassNotDefined, InteractionClassNotPublished, InteractionParameterNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext, InvalidLogicalTime, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    // ensure we have a valid interaction class and parameter handles
    //
    InteractionClass interactionClass = fdd.getInteractionClass(interactionClassHandle);
    interactionClass.checkIfInteractionParameterNotDefined(parameterValues.keySet());

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      MessageRetractionHandle messageRetractionHandle;

      timeManager.getTimeLock().readLock().lock();
      try
      {
        timeManager.sendInteraction(time);

        OrderType sentOrderType =
          timeManager.isTimeRegulating() && time != null ? OrderType.TIMESTAMP : OrderType.RECEIVE;

        if (sentOrderType == OrderType.TIMESTAMP)
        {
          messageRetractionHandle = messageRetractionManager.add(time);
        }
        else
        {
          messageRetractionHandle = null;
        }

        objectManager.sendInteractionWithRegions(
          interactionClass, parameterValues, regionHandles, tag, time, messageRetractionHandle, sentOrderType);
      }
      finally
      {
        timeManager.getTimeLock().readLock().unlock();
      }

      return new MessageRetractionReturn(messageRetractionHandle != null, messageRetractionHandle);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void requestAttributeValueUpdateWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions, byte[] tag)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
           InvalidRegionContext, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      rtiChannel.write(new RequestObjectClassAttributeValueUpdateWithRegions(
        objectClassHandle, attributesAndRegions, tag));
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public ResignAction getAutomaticResignDirective()
  {
    federateStateLock.readLock().lock();
    try
    {
      return automaticResignDirective;
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void setAutomaticResignDirective(ResignAction resignAction)
    throws InvalidResignAction
  {
    federateStateLock.writeLock().lock();
    try
    {
      automaticResignDirective = resignAction;

      rtiChannel.write(new SetAutomaticResignDirective(resignAction));
    }
    finally
    {
      federateStateLock.writeLock().unlock();
    }
  }

  public FederateHandle getFederateHandle(String federateName)
    throws NameNotFound, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      FederateHandle federateHandle;
      if (this.federateName.equals(federateName))
      {
        federateHandle = this.federateHandle;
      }
      else
      {
        GetFederateHandle getFederateHandle = new GetFederateHandle(federateName);
        rtiChannel.write(getFederateHandle);

        GetFederateHandleResponse response = getFederateHandle.getResponse();
        switch (response.getResponse())
        {
          case NAME_NOT_FOUND:
            throw new NameNotFound(I18n.getMessage(ExceptionMessages.FEDERATE_NAME_NOT_FOUND, federateName));
          case SUCCESS:
            federateHandle = response.getFederateHandle();
            break;
          default:
            throw new RTIinternalError(I18n.getMessage(ExceptionMessages.UNEXPECTED_EXCEPTION));
        }
      }
      return federateHandle;
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public String getFederateName(FederateHandle federateHandle)
    throws InvalidFederateHandle, FederateHandleNotKnown, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      String federateName;
      if (this.federateHandle.equals(federateHandle))
      {
        federateName = this.federateName;
      }
      else
      {
        GetFederateName getFederateName = new GetFederateName(federateHandle);
        rtiChannel.write(getFederateName);

        GetFederateNameResponse response = getFederateName.getResponse();
        switch (response.getResponse())
        {
          case FEDERATE_HANDLE_NOT_KNOWN:
            throw new FederateHandleNotKnown(I18n.getMessage(
              ExceptionMessages.FEDERATE_HANDLE_NOT_KNOWN, federateHandle));
          case SUCCESS:
            federateName = response.getFederateName();
            break;
          default:
            throw new RTIinternalError(I18n.getMessage(ExceptionMessages.UNEXPECTED_EXCEPTION));
        }
      }
      return federateName;
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public ObjectClassHandle getObjectClassHandle(String name)
    throws NameNotFound, RTIinternalError
  {
    return fdd.getObjectClassHandle(name);
  }

  public String getObjectClassName(ObjectClassHandle objectClassHandle)
    throws InvalidObjectClassHandle, RTIinternalError
  {
    return fdd.getObjectClassName(objectClassHandle);
  }

  public AttributeHandle getAttributeHandle(ObjectClassHandle objectClassHandle, String name)
    throws InvalidObjectClassHandle, NameNotFound, RTIinternalError
  {
    return fdd.getAttributeHandle(objectClassHandle, name);
  }

  public String getAttributeName(ObjectClassHandle objectClassHandle, AttributeHandle attributeHandle)
    throws InvalidObjectClassHandle, InvalidAttributeHandle, AttributeNotDefined, RTIinternalError
  {
    return fdd.getAttributeName(objectClassHandle, attributeHandle);
  }

  public double getUpdateRateValue(String updateRateDesignator)
    throws InvalidUpdateRateDesignator, RTIinternalError
  {
    return 0.0;
  }

  public double getUpdateRateValueForAttribute(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
    throws ObjectInstanceNotKnown, AttributeNotDefined, RTIinternalError
  {
    return objectManager.getUpdateRateValueForAttribute(objectInstanceHandle, attributeHandle);
  }

  public InteractionClassHandle getInteractionClassHandle(String interactionClassName)
    throws NameNotFound, RTIinternalError
  {
    return fdd.getInteractionClassHandle(interactionClassName);
  }

  public String getInteractionClassName(InteractionClassHandle interactionClassHandle)
    throws InvalidInteractionClassHandle, RTIinternalError
  {
    return fdd.getInteractionClassName(interactionClassHandle);
  }

  public ParameterHandle getParameterHandle(InteractionClassHandle interactionClassHandle, String name)
    throws InvalidInteractionClassHandle, NameNotFound, RTIinternalError
  {
    return fdd.getParameterHandle(interactionClassHandle, name);
  }

  public String getParameterName(InteractionClassHandle interactionClassHandle, ParameterHandle parameterHandle)
    throws InvalidInteractionClassHandle, InvalidParameterHandle, InteractionParameterNotDefined, RTIinternalError
  {
    return fdd.getParameterName(interactionClassHandle, parameterHandle);
  }

  public ObjectInstanceHandle getObjectInstanceHandle(String name)
    throws ObjectInstanceNotKnown, RTIinternalError
  {
    return objectManager.getObjectInstanceHandle(name);
  }

  public String getObjectInstanceName(ObjectInstanceHandle objectInstanceHandle)
    throws ObjectInstanceNotKnown, RTIinternalError
  {
    return objectManager.getObjectInstanceName(objectInstanceHandle);
  }

  public DimensionHandle getDimensionHandle(String name)
    throws NameNotFound, RTIinternalError
  {
    return fdd.getDimensionHandle(name);
  }

  public String getDimensionName(DimensionHandle dimensionHandle)
    throws InvalidDimensionHandle, RTIinternalError
  {
    return fdd.getDimensionName(dimensionHandle);
  }

  public long getDimensionUpperBound(DimensionHandle dimensionHandle)
    throws InvalidDimensionHandle, RTIinternalError
  {
    return fdd.getDimensionUpperBound(dimensionHandle);
  }

  public DimensionHandleSet getAvailableDimensionsForClassAttribute(
    ObjectClassHandle objectClassHandle, AttributeHandle attributeHandle)
    throws InvalidObjectClassHandle, InvalidAttributeHandle, AttributeNotDefined, RTIinternalError
  {
    return fdd.getAvailableDimensionsForClassAttribute(objectClassHandle, attributeHandle);
  }

  public ObjectClassHandle getKnownObjectClassHandle(ObjectInstanceHandle objectInstanceHandle)
    throws ObjectInstanceNotKnown, RTIinternalError
  {
    return objectManager.getObjectClassHandle(objectInstanceHandle);
  }

  public DimensionHandleSet getAvailableDimensionsForInteractionClass(InteractionClassHandle interactionClassHandle)
    throws InvalidInteractionClassHandle, RTIinternalError
  {
    return fdd.getAvailableDimensionsForInteractionClass(
      interactionClassHandle, getDimensionHandleSetFactory().create());
  }

  public TransportationTypeHandle getTransportationTypeHandle(String name)
    throws InvalidTransportationName, RTIinternalError
  {
    return fdd.getTransportationTypeHandle(name);
  }

  public String getTransportationTypeName(TransportationTypeHandle transportationType)
    throws InvalidTransportationType, RTIinternalError
  {
    return fdd.getTransportationTypeName(transportationType);
  }

  public OrderType getOrderType(String name)
    throws InvalidOrderName, RTIinternalError
  {
    return fdd.getOrderType(name);
  }

  public String getOrderName(OrderType orderType)
    throws InvalidOrderType, RTIinternalError
  {
    return fdd.getOrderName(orderType);
  }

  public void enableObjectClassRelevanceAdvisorySwitch()
    throws ObjectClassRelevanceAdvisorySwitchIsOn, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void disableObjectClassRelevanceAdvisorySwitch()
    throws ObjectClassRelevanceAdvisorySwitchIsOff, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void enableAttributeRelevanceAdvisorySwitch()
    throws AttributeRelevanceAdvisorySwitchIsOn, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void disableAttributeRelevanceAdvisorySwitch()
    throws AttributeRelevanceAdvisorySwitchIsOff, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void enableAttributeScopeAdvisorySwitch()
    throws AttributeScopeAdvisorySwitchIsOn, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void disableAttributeScopeAdvisorySwitch()
    throws AttributeScopeAdvisorySwitchIsOff, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void enableInteractionRelevanceAdvisorySwitch()
    throws InteractionRelevanceAdvisorySwitchIsOn, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void disableInteractionRelevanceAdvisorySwitch()
    throws InteractionRelevanceAdvisorySwitchIsOff, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public DimensionHandleSet getDimensionHandleSet(RegionHandle regionHandle)
    throws InvalidRegion, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      return null;
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public RangeBounds getRangeBounds(RegionHandle regionHandle, DimensionHandle dimensionHandle)
    throws InvalidRegion, RegionDoesNotContainSpecifiedDimension, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      return regionManager.getRangeBounds(regionHandle, dimensionHandle);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void setRangeBounds(RegionHandle regionHandle, DimensionHandle dimensionHandle, RangeBounds rangeBounds)
    throws InvalidRegion, RegionNotCreatedByThisFederate, RegionDoesNotContainSpecifiedDimension, InvalidRangeBound,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    // ensure we have a valid dimension handle and range
    //
    fdd.checkIfInvalidRangeBound(dimensionHandle, rangeBounds);

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      regionManager.setRangeBounds(regionHandle, dimensionHandle, rangeBounds);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public long normalizeFederateHandle(FederateHandle federateHandle)
    throws InvalidFederateHandle, RTIinternalError
  {
    return federateHandle.hashCode();
  }

  public long normalizeServiceGroup(ServiceGroup serviceGroup)
    throws RTIinternalError
  {
    return serviceGroup.hashCode();
  }

  public void enableCallbacks()
    throws SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      callbackManager.enableCallbacks();
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void disableCallbacks()
    throws SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      callbackManager.disableCallbacks();
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public AttributeHandleFactory getAttributeHandleFactory()
  {
    return attributeHandleFactory;
  }

  public AttributeHandleSetFactory getAttributeHandleSetFactory()
  {
    return attributeHandleSetFactory;
  }

  public AttributeHandleValueMapFactory getAttributeHandleValueMapFactory()
  {
    return attributeHandleValueMapFactory;
  }

  public AttributeSetRegionSetPairListFactory getAttributeSetRegionSetPairListFactory()
  {
    return attributeSetRegionSetPairListFactory;
  }

  public DimensionHandleFactory getDimensionHandleFactory()
  {
    return dimensionHandleFactory;
  }

  public DimensionHandleSetFactory getDimensionHandleSetFactory()
  {
    return dimensionHandleSetFactory;
  }

  public FederateHandleFactory getFederateHandleFactory()
  {
    return federateHandleFactory;
  }

  public FederateHandleSetFactory getFederateHandleSetFactory()
  {
    return federateHandleSetFactory;
  }

  public InteractionClassHandleFactory getInteractionClassHandleFactory()
  {
    return interactionClassHandleFactory;
  }

  public ObjectClassHandleFactory getObjectClassHandleFactory()
  {
    return objectClassHandleFactory;
  }

  public ObjectInstanceHandleFactory getObjectInstanceHandleFactory()
  {
    return objectInstanceHandleFactory;
  }

  public ParameterHandleFactory getParameterHandleFactory()
  {
    return parameterHandleFactory;
  }

  public ParameterHandleValueMapFactory getParameterHandleValueMapFactory()
  {
    return parameterHandleValueMapFactory;
  }

  public RegionHandleSetFactory getRegionHandleSetFactory()
  {
    return regionHandleSetFactory;
  }

  public TransportationTypeHandleFactory getTransportationTypeHandleFactory()
  {
    return transportationTypeHandleFactory;
  }

  public LogicalTimeFactory getLogicalTimeFactory()
  {
    return logicalTimeFactory;
  }

  public void announceSynchronizationPoint(String label, byte[] tag)
    throws FederateInternalError
  {
    FederateSynchronizationPoint federateSynchronizationPoint;

    synchronizationPointLock.lock();
    try
    {
      synchronizationPoints.put(label, new FederateSynchronizationPoint(label, tag));
    }
    finally
    {
      synchronizationPointLock.unlock();
    }

    federateAmbassador.announceSynchronizationPoint(label, tag);
  }

  public void federationSynchronized(String label, FederateHandleSet failedToSynchronize)
    throws FederateInternalError
  {
    FederateSynchronizationPoint federateSynchronizationPoint;

    synchronizationPointLock.lock();
    try
    {
      federateSynchronizationPoint = synchronizationPoints.remove(label);

      assert federateSynchronizationPoint != null;
    }
    finally
    {
      synchronizationPointLock.unlock();
    }

    federateAmbassador.federationSynchronized(label, failedToSynchronize);
  }

  public void initiateFederateSave(String label, LogicalTime time)
    throws FederateInternalError
  {
    federateStateLock.writeLock().lock();
    try
    {
      if (time == null)
      {
        federateAmbassador.initiateFederateSave(label);
      }
      else
      {
        federateAmbassador.initiateFederateSave(label, time);
      }
    }
    finally
    {
      federateState = FederateState.SAVE_IN_PROGRESS;

      // hold any pending callbacks so only callbacks that can occur during
      // a save will get through
      //
      callbackManager.holdCallbacks();

      federateStateLock.writeLock().unlock();
    }
  }

  public void federationSaved()
    throws FederateInternalError
  {
    federateStateLock.writeLock().lock();
    try
    {
      federateAmbassador.federationSaved();
    }
    finally
    {
      federateState = FederateState.ACTIVE;

      // TODO: unhold callbacks?

      federateStateLock.writeLock().unlock();
    }
  }

  public void federationNotSaved(SaveFailureReason reason)
    throws FederateInternalError
  {
    federateStateLock.writeLock().lock();
    try
    {
      federateAmbassador.federationNotSaved(reason);
    }
    finally
    {
      federateState = FederateState.ACTIVE;

      // TODO: unhold callbacks?

      federateStateLock.writeLock().unlock();
    }
  }

  public void objectInstanceNameReservationSucceeded(String objectInstanceName)
    throws FederateInternalError
  {
    objectManager.objectInstanceNameReservationSucceeded(objectInstanceName, federateAmbassador);
  }

  public void multipleObjectInstanceNameReservationSucceeded(Set<String> objectInstanceNames)
    throws FederateInternalError
  {
    objectManager.multipleObjectInstanceNameReservationSucceeded(objectInstanceNames, federateAmbassador);
  }

  public void objectInstanceNameReservationFailed(String objectInstanceName)
    throws FederateInternalError
  {
    objectManager.objectInstanceNameReservationFailed(objectInstanceName, federateAmbassador);
  }

  public void multipleObjectInstanceNameReservationFailed(Set<String> objectInstanceNames)
    throws FederateInternalError
  {
    objectManager.multipleObjectInstanceNameReservationFailed(objectInstanceNames, federateAmbassador);
  }

  public void discoverObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle, String objectInstanceName,
    FederateHandle producingFederateHandle)
    throws FederateInternalError
  {
    objectManager.discoverObjectInstance(
      objectInstanceHandle, objectClassHandle, objectInstanceName, producingFederateHandle, federateAmbassador);
  }

  public void fireReflectAttributeValues(ReflectAttributeValues reflectAttributeValues)
    throws FederateInternalError
  {
    objectManager.fireReflectAttributeValues(reflectAttributeValues, federateAmbassador);
  }

  public void fireReceiveInteraction(ReceiveInteraction receiveInteraction)
    throws FederateInternalError
  {
    objectManager.fireReceiveInteraction(receiveInteraction, federateAmbassador);
  }

  public void removeObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag, OrderType sentOrderType, LogicalTime deleteTime,
    OrderType receivedOrderType, MessageRetractionHandle messageRetractionHandle,
    FederateAmbassador.SupplementalRemoveInfo supplementalRemoveInfo)
    throws FederateInternalError
  {
    objectManager.removeObjectInstance(
      objectInstanceHandle, tag, sentOrderType, deleteTime, receivedOrderType,
      messageRetractionHandle, supplementalRemoveInfo, federateAmbassador);
  }

  public void attributeOwnershipAcquisitionNotification(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws FederateInternalError
  {
    objectManager.attributeOwnershipAcquisitionNotification(
      objectInstanceHandle, attributeHandles, tag, federateAmbassador);
  }

  public void timeRegulationEnabled(LogicalTime time)
    throws FederateInternalError
  {
    timeManager.timeRegulationEnabled(time, federateAmbassador);
  }

  public void timeConstrainedEnabled(LogicalTime time)
    throws FederateInternalError
  {
    timeManager.timeConstrainedEnabled(time, federateAmbassador);
  }

  public void timeAdvanceGrant(LogicalTime time)
    throws FederateInternalError
  {
    timeManager.timeAdvanceGrant(time, federateAmbassador);
  }

  public LogicalTime getNextMessageTime()
  {
    futureTasksLock.lock();
    try
    {
      TimestampedFutureTask timestampedFutureTask = futureTasks.peek();
      return timestampedFutureTask == null ? null : timestampedFutureTask.getTime();
    }
    finally
    {
      futureTasksLock.unlock();
    }
  }

  @Override
  public String toString()
  {
    return new StringBuilder(federateName).append(":").append(federateHandle).toString();
  }

  private void checkIfSaveInProgress()
    throws SaveInProgress
  {
    if (federateState == FederateState.SAVE_IN_PROGRESS)
    {
      throw new SaveInProgress(I18n.getMessage(ExceptionMessages.SAVE_IN_PROGRESS, federationExecutionName));
    }
  }

  private void checkIfRestoreInProgress()
    throws RestoreInProgress
  {
    if (federateState == FederateState.RESTORE_IN_PROGRESS)
    {
      throw new RestoreInProgress(I18n.getMessage(ExceptionMessages.RESTORE_IN_PROGRESS, federationExecutionName));
    }
  }

  private void checkIfActive()
    throws SaveInProgress, RestoreInProgress
  {
    switch (federateState)
    {
      case SAVE_IN_PROGRESS:
        throw new SaveInProgress(I18n.getMessage(ExceptionMessages.SAVE_IN_PROGRESS, federationExecutionName));
      case RESTORE_IN_PROGRESS:
        throw new RestoreInProgress(I18n.getMessage(ExceptionMessages.RESTORE_IN_PROGRESS, federationExecutionName));
      default:
        assert federateState == FederateState.ACTIVE;
        break;
    }
  }

  private Future<Object> schedule(LogicalTime time, Callable<Object> callable)
  {
    log.debug(LogMessages.SCHEDULING_TASK, callable, time);

    TimestampedFutureTask future = new TimestampedFutureTask(time, callable);

    futureTasksLock.lock();
    try
    {
      futureTasks.offer(future);
    }
    finally
    {
      futureTasksLock.unlock();
    }

    return future;
  }

  private class TimestampedFutureTask
    extends FutureTask<Object>
    implements Comparable<TimestampedFutureTask>
  {
    private final LogicalTime time;

    public TimestampedFutureTask(LogicalTime time, Callable<Object> callable)
    {
      super(callable);

      this.time = time;
    }

    public LogicalTime getTime()
    {
      return time;
    }

    @SuppressWarnings("unchecked")
    public int compareTo(TimestampedFutureTask rhs)
    {
      return time.compareTo(rhs.time);
    }
  }

  private class AddCallback
    implements Callable<Object>
  {
    private final Callback callback;

    public AddCallback(Callback callback)
    {
      this.callback = callback;
    }

    public Object call()
    {
      timeManager.getTimeLock().readLock().lock();
      try
      {
        boolean hold = !isAsynchronousDeliveryEnabled() &&
                       timeManager.isTimeConstrainedAndTimeGranted();

        callbackManager.add(callback, hold);
      }
      finally
      {
        timeManager.getTimeLock().readLock().unlock();
      }

      return null;
    }
  }

  private class ScheduledDeleteObjectInstance
    implements Callable<Object>
  {
    private final ObjectInstanceHandle objectInstanceHandle;
    private final byte[] tag;

    public ScheduledDeleteObjectInstance(
      ObjectInstanceHandle objectInstanceHandle, byte[] tag)
    {
      this.objectInstanceHandle = objectInstanceHandle;
      this.tag = tag;
    }

    public Object call()
      throws Exception
    {
      objectManager.deleteObjectInstance(objectInstanceHandle, tag);

      return null;
    }
  }

  public static String defaultFederateName(FederateHandle federateHandle)
  {
    return "HLA-Federate-" + federateHandle.toString();
  }
}
