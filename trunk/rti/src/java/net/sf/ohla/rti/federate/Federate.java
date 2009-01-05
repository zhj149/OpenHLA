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
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.fdd.FDD;
import net.sf.ohla.rti.hla.rti1516.IEEE1516AttributeHandleFactory;
import net.sf.ohla.rti.hla.rti1516.IEEE1516AttributeHandleSetFactory;
import net.sf.ohla.rti.hla.rti1516.IEEE1516AttributeHandleValueMapFactory;
import net.sf.ohla.rti.hla.rti1516.IEEE1516AttributeSetRegionSetPairListFactory;
import net.sf.ohla.rti.hla.rti1516.IEEE1516DimensionHandleFactory;
import net.sf.ohla.rti.hla.rti1516.IEEE1516DimensionHandleSetFactory;
import net.sf.ohla.rti.hla.rti1516.IEEE1516FederateHandleFactory;
import net.sf.ohla.rti.hla.rti1516.IEEE1516FederateHandleSetFactory;
import net.sf.ohla.rti.hla.rti1516.IEEE1516InteractionClassHandleFactory;
import net.sf.ohla.rti.hla.rti1516.IEEE1516ObjectClassHandleFactory;
import net.sf.ohla.rti.hla.rti1516.IEEE1516ObjectInstanceHandleFactory;
import net.sf.ohla.rti.hla.rti1516.IEEE1516ParameterHandleFactory;
import net.sf.ohla.rti.hla.rti1516.IEEE1516ParameterHandleValueMapFactory;
import net.sf.ohla.rti.hla.rti1516.IEEE1516RegionHandleSetFactory;
import net.sf.ohla.rti.messages.FederateRestoreComplete;
import net.sf.ohla.rti.messages.FederateRestoreNotComplete;
import net.sf.ohla.rti.messages.FederateSaveBegun;
import net.sf.ohla.rti.messages.FederateSaveComplete;
import net.sf.ohla.rti.messages.FederateSaveNotComplete;
import net.sf.ohla.rti.messages.GALTAdvanced;
import net.sf.ohla.rti.messages.JoinFederationExecution;
import net.sf.ohla.rti.messages.JoinFederationExecutionResponse;
import net.sf.ohla.rti.messages.QueryFederationRestoreStatus;
import net.sf.ohla.rti.messages.QueryFederationSaveStatus;
import net.sf.ohla.rti.messages.RegisterFederationSynchronizationPoint;
import net.sf.ohla.rti.messages.RequestAttributeValueUpdate;
import net.sf.ohla.rti.messages.RequestFederationRestore;
import net.sf.ohla.rti.messages.RequestFederationSave;
import net.sf.ohla.rti.messages.Retract;
import net.sf.ohla.rti.messages.SendInteraction;
import net.sf.ohla.rti.messages.SubscribeObjectClassAttributes;
import net.sf.ohla.rti.messages.SynchronizationPointAchieved;
import net.sf.ohla.rti.messages.UnsubscribeObjectClassAttributes;
import net.sf.ohla.rti.messages.callbacks.Callback;
import net.sf.ohla.rti.messages.callbacks.CallbackManager;
import net.sf.ohla.rti.messages.callbacks.ReceiveInteraction;
import net.sf.ohla.rti.messages.callbacks.ReflectAttributeValues;
import net.sf.ohla.rti.messages.callbacks.RemoveObjectInstance;
import net.sf.ohla.rti.messages.callbacks.TimeAdvanceGrant;

import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import hla.rti1516.AsynchronousDeliveryAlreadyDisabled;
import hla.rti1516.AsynchronousDeliveryAlreadyEnabled;
import hla.rti1516.AttributeAcquisitionWasNotCanceled;
import hla.rti1516.AttributeAcquisitionWasNotRequested;
import hla.rti1516.AttributeAlreadyBeingAcquired;
import hla.rti1516.AttributeAlreadyBeingDivested;
import hla.rti1516.AttributeAlreadyOwned;
import hla.rti1516.AttributeDivestitureWasNotRequested;
import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleFactory;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeHandleSetFactory;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.AttributeHandleValueMapFactory;
import hla.rti1516.AttributeNotDefined;
import hla.rti1516.AttributeNotOwned;
import hla.rti1516.AttributeNotPublished;
import hla.rti1516.AttributeNotRecognized;
import hla.rti1516.AttributeNotSubscribed;
import hla.rti1516.AttributeRegionAssociation;
import hla.rti1516.AttributeRelevanceAdvisorySwitchIsOff;
import hla.rti1516.AttributeRelevanceAdvisorySwitchIsOn;
import hla.rti1516.AttributeScopeAdvisorySwitchIsOff;
import hla.rti1516.AttributeScopeAdvisorySwitchIsOn;
import hla.rti1516.AttributeSetRegionSetPairList;
import hla.rti1516.AttributeSetRegionSetPairListFactory;
import hla.rti1516.CouldNotDiscover;
import hla.rti1516.CouldNotInitiateRestore;
import hla.rti1516.DeletePrivilegeNotHeld;
import hla.rti1516.DimensionHandle;
import hla.rti1516.DimensionHandleFactory;
import hla.rti1516.DimensionHandleSet;
import hla.rti1516.DimensionHandleSetFactory;
import hla.rti1516.FederateAlreadyExecutionMember;
import hla.rti1516.FederateAmbassador;
import hla.rti1516.FederateHandle;
import hla.rti1516.FederateHandleFactory;
import hla.rti1516.FederateHandleRestoreStatusPair;
import hla.rti1516.FederateHandleSaveStatusPair;
import hla.rti1516.FederateHandleSet;
import hla.rti1516.FederateHandleSetFactory;
import hla.rti1516.FederateHasNotBegunSave;
import hla.rti1516.FederateInternalError;
import hla.rti1516.FederateOwnsAttributes;
import hla.rti1516.FederateServiceInvocationsAreBeingReportedViaMOM;
import hla.rti1516.FederateUnableToUseTime;
import hla.rti1516.FederationExecutionDoesNotExist;
import hla.rti1516.IllegalName;
import hla.rti1516.InTimeAdvancingState;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.InteractionClassHandleFactory;
import hla.rti1516.InteractionClassNotDefined;
import hla.rti1516.InteractionClassNotPublished;
import hla.rti1516.InteractionClassNotRecognized;
import hla.rti1516.InteractionClassNotSubscribed;
import hla.rti1516.InteractionParameterNotDefined;
import hla.rti1516.InteractionParameterNotRecognized;
import hla.rti1516.InteractionRelevanceAdvisorySwitchIsOff;
import hla.rti1516.InteractionRelevanceAdvisorySwitchIsOn;
import hla.rti1516.InvalidAttributeHandle;
import hla.rti1516.InvalidDimensionHandle;
import hla.rti1516.InvalidFederateHandle;
import hla.rti1516.InvalidInteractionClassHandle;
import hla.rti1516.InvalidLogicalTime;
import hla.rti1516.InvalidLookahead;
import hla.rti1516.InvalidMessageRetractionHandle;
import hla.rti1516.InvalidObjectClassHandle;
import hla.rti1516.InvalidOrderName;
import hla.rti1516.InvalidOrderType;
import hla.rti1516.InvalidParameterHandle;
import hla.rti1516.InvalidRangeBound;
import hla.rti1516.InvalidRegion;
import hla.rti1516.InvalidRegionContext;
import hla.rti1516.InvalidTransportationName;
import hla.rti1516.InvalidTransportationType;
import hla.rti1516.JoinedFederateIsNotInTimeAdvancingState;
import hla.rti1516.LogicalTime;
import hla.rti1516.LogicalTimeAlreadyPassed;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.MessageCanNoLongerBeRetracted;
import hla.rti1516.MessageRetractionHandle;
import hla.rti1516.MessageRetractionReturn;
import hla.rti1516.MobileFederateServices;
import hla.rti1516.NameNotFound;
import hla.rti1516.NoRequestToEnableTimeConstrainedWasPending;
import hla.rti1516.NoRequestToEnableTimeRegulationWasPending;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectClassHandleFactory;
import hla.rti1516.ObjectClassNotDefined;
import hla.rti1516.ObjectClassNotPublished;
import hla.rti1516.ObjectClassNotRecognized;
import hla.rti1516.ObjectClassRelevanceAdvisorySwitchIsOff;
import hla.rti1516.ObjectClassRelevanceAdvisorySwitchIsOn;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ObjectInstanceHandleFactory;
import hla.rti1516.ObjectInstanceNameInUse;
import hla.rti1516.ObjectInstanceNameNotReserved;
import hla.rti1516.ObjectInstanceNotKnown;
import hla.rti1516.OrderType;
import hla.rti1516.OwnershipAcquisitionPending;
import hla.rti1516.ParameterHandle;
import hla.rti1516.ParameterHandleFactory;
import hla.rti1516.ParameterHandleValueMap;
import hla.rti1516.ParameterHandleValueMapFactory;
import hla.rti1516.RTIinternalError;
import hla.rti1516.RangeBounds;
import hla.rti1516.RegionDoesNotContainSpecifiedDimension;
import hla.rti1516.RegionHandle;
import hla.rti1516.RegionHandleSet;
import hla.rti1516.RegionHandleSetFactory;
import hla.rti1516.RegionInUseForUpdateOrSubscription;
import hla.rti1516.RegionNotCreatedByThisFederate;
import hla.rti1516.RequestForTimeConstrainedPending;
import hla.rti1516.RequestForTimeRegulationPending;
import hla.rti1516.ResignAction;
import hla.rti1516.RestoreFailureReason;
import hla.rti1516.RestoreInProgress;
import hla.rti1516.RestoreNotRequested;
import hla.rti1516.RestoreStatus;
import hla.rti1516.SaveFailureReason;
import hla.rti1516.SaveInProgress;
import hla.rti1516.SaveNotInitiated;
import hla.rti1516.SaveStatus;
import hla.rti1516.ServiceGroup;
import hla.rti1516.SpecifiedSaveLabelDoesNotExist;
import hla.rti1516.SynchronizationPointFailureReason;
import hla.rti1516.SynchronizationPointLabelNotAnnounced;
import hla.rti1516.TimeConstrainedAlreadyEnabled;
import hla.rti1516.TimeConstrainedIsNotEnabled;
import hla.rti1516.TimeQueryReturn;
import hla.rti1516.TimeRegulationAlreadyEnabled;
import hla.rti1516.TimeRegulationIsNotEnabled;
import hla.rti1516.TransportationType;
import hla.rti1516.UnableToPerformSave;
import hla.rti1516.UnknownName;
import hla.rti1516.jlc.NullFederateAmbassador;

public class Federate
{
  public static final String OHLA_FEDERATE_HOST_PROPERTY =
    "ohla.federate.%s.host";
  public static final String OHLA_FEDERATE_PORT_PROPERTY =
    "ohla.federate.%s.port";

  protected final String federateType;
  protected final String federationExecutionName;
  protected final FederateAmbassador federateAmbassador;
  protected final MobileFederateServices mobileFederateServices;

  protected final FederateAmbassador federateAmbassadorInterceptor =
    new FederateAmbassadorInterceptor();

  protected FederateHandle federateHandle;

  protected String federationName;
  protected FDD fdd;

  protected FederateState federateState = FederateState.ACTIVE;

  protected ReadWriteLock federateStateLock =
    new ReentrantReadWriteLock(true);

  protected SaveStatus saveStatus;
  protected FederateSave federateSave;

  protected RestoreStatus restoreStatus;
  protected FederateRestore federateRestore;

  protected Lock synchronizationPointLock = new ReentrantLock(true);
  protected Map<String, FederateSynchronizationPoint> synchronizationPoints =
    new HashMap<String, FederateSynchronizationPoint>();

  protected boolean asynchronousDeliveryEnabled;

  protected FederateObjectManager objectManager = new FederateObjectManager(this);
  protected FederateRegionManager regionManager = new FederateRegionManager(this);
  protected FederateMessageRetractionManager messageRetractionManager =
    new FederateMessageRetractionManager(this);

  protected FederateTimeManager timeManager;
  protected CallbackManager callbackManager = new CallbackManager(this);

  protected Lock futureTasksLock = new ReentrantLock(true);
  protected Queue<TimestampedFutureTask> futureTasks =
    new PriorityQueue<TimestampedFutureTask>();

  protected AttributeHandleFactory attributeHandleFactory =
    new IEEE1516AttributeHandleFactory();
  protected AttributeHandleSetFactory attributeHandleSetFactory =
    new IEEE1516AttributeHandleSetFactory();
  protected AttributeHandleValueMapFactory attributeHandleValueMapFactory =
    new IEEE1516AttributeHandleValueMapFactory();
  protected AttributeSetRegionSetPairListFactory attributeSetRegionSetPairListFactory =
    new IEEE1516AttributeSetRegionSetPairListFactory();
  protected DimensionHandleFactory dimensionHandleFactory =
    new IEEE1516DimensionHandleFactory();
  protected DimensionHandleSetFactory dimensionHandleSetFactory =
    new IEEE1516DimensionHandleSetFactory();
  protected FederateHandleFactory federateHandleFactory =
    new IEEE1516FederateHandleFactory();
  protected FederateHandleSetFactory federateHandleSetFactory =
    new IEEE1516FederateHandleSetFactory();
  protected InteractionClassHandleFactory interactionClassHandleFactory =
    new IEEE1516InteractionClassHandleFactory();
  protected ObjectClassHandleFactory objectClassHandleFactory =
    new IEEE1516ObjectClassHandleFactory();
  protected ObjectInstanceHandleFactory objectInstanceHandleFactory =
    new IEEE1516ObjectInstanceHandleFactory();
  protected ParameterHandleFactory parameterHandleFactory =
    new IEEE1516ParameterHandleFactory();
  protected ParameterHandleValueMapFactory parameterHandleValueMapFactory =
    new IEEE1516ParameterHandleValueMapFactory();
  protected RegionHandleSetFactory regionHandleSetFactory =
    new IEEE1516RegionHandleSetFactory();

  /**
   * The session with the RTI.
   */
  protected IoSession rtiSession;

  protected final Logger log = LoggerFactory.getLogger(getClass());
  protected final Marker marker;

  public Federate(String federateType, String federationExecutionName,
                  FederateAmbassador federateAmbassador,
                  MobileFederateServices mobileFederateServices,
                  IoSession rtiSession)
    throws FederationExecutionDoesNotExist, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    this.federateType = federateType;
    this.federationExecutionName = federationExecutionName;
    this.federateAmbassador = federateAmbassador;
    this.mobileFederateServices = mobileFederateServices;
    this.rtiSession = rtiSession;

    marker = MarkerFactory.getMarker(federateType);

    JoinFederationExecution joinFederationExecution =
      new JoinFederationExecution(
        federateType, federationExecutionName, mobileFederateServices);
    WriteFuture writeFuture = rtiSession.write(joinFederationExecution);

    // TODO: set timeout
    //
    writeFuture.join();

    if (!writeFuture.isWritten())
    {
      throw new RTIinternalError("error communicating with RTI");
    }

    try
    {
      // TODO: set timeout
      //
      Object response = joinFederationExecution.getResponse();
      if (response instanceof JoinFederationExecutionResponse)
      {
        JoinFederationExecutionResponse joinFederationExecutionResponse =
          (JoinFederationExecutionResponse) response;

        federateHandle = joinFederationExecutionResponse.getFederateHandle();
        fdd = joinFederationExecutionResponse.getFdd();

        LogicalTime galt = joinFederationExecutionResponse.getGALT();
        timeManager = new FederateTimeManager(this, mobileFederateServices, galt);

        log.info(marker, "joined federation execution: {}", federateHandle);
      }
      else if (response instanceof FederationExecutionDoesNotExist)
      {
        throw new FederationExecutionDoesNotExist(
          (FederationExecutionDoesNotExist) response);
      }
      else if (response instanceof SaveInProgress)
      {
        throw new SaveInProgress((SaveInProgress) response);
      }
      else if (response instanceof RestoreInProgress)
      {
        throw new RestoreInProgress((RestoreInProgress) response);
      }
      else
      {
        assert false : String.format("unexpected response: %s", response);
      }
    }
    catch (InterruptedException ie)
    {
      throw new RTIinternalError("interrupted awaiting timeout", ie);
    }
    catch (ExecutionException ee)
    {
      throw new RTIinternalError("unable to get response", ee);
    }
  }

  public FederateHandle getFederateHandle()
  {
    return federateHandle;
  }

  public FederateAmbassador getFederateAmbassador()
  {
    return federateAmbassadorInterceptor;
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

  public IoSession getRTISession()
  {
    return rtiSession;
  }

  public Marker getMarker()
  {
    return marker;
  }

  public boolean isAsynchronousDeliveryEnabled()
  {
    return asynchronousDeliveryEnabled;
  }

  public void processFutureTasks(LogicalTime maxFutureTaskTimestamp)
  {
    futureTasksLock.lock();
    try
    {
      log.debug(marker, "processing future tasks: {}", maxFutureTaskTimestamp);

      for (TimestampedFutureTask timestampedFutureTask = futureTasks.peek();
           timestampedFutureTask != null &&
           timestampedFutureTask.getTime().compareTo(
             maxFutureTaskTimestamp) <= 0;
           timestampedFutureTask = futureTasks.peek())
      {
        log.debug(marker, "processing future task: {}", timestampedFutureTask);

        try
        {
          timestampedFutureTask.run();
        }
        catch (Throwable t)
        {
          log.error(marker, String.format("unable to execute scheduled task: %s",
                                          timestampedFutureTask), t);
        }

        futureTasks.poll();
      }
    }
    finally
    {
      futureTasksLock.unlock();
    }
  }

  public boolean messageReceived(IoSession session, Object message)
  {
    log.debug(marker, "processing: {}", message);

    boolean processed = true;
    if (message instanceof Callback)
    {
      if (message instanceof ReflectAttributeValues)
      {
        ReflectAttributeValues reflectAttributeValues =
          (ReflectAttributeValues) message;

        timeManager.getTimeLock().readLock().lock();
        try
        {
          OrderType receivedOrderType =
            reflectAttributeValues.getSentOrderType() == OrderType.TIMESTAMP &&
            timeManager.isTimeConstrained() ? OrderType.TIMESTAMP :
              OrderType.RECEIVE;
          reflectAttributeValues.setReceivedOrderType(receivedOrderType);

          if (receivedOrderType == OrderType.RECEIVE)
          {
            // receive order callbacks need to be held until released if we
            // are constrained and in the time granted state if asynchronous
            // delivery is disabled
            //
            boolean hold = timeManager.isTimeConstrainedAndTimeGranted() &&
                           !isAsynchronousDeliveryEnabled();

            callbackManager.add(reflectAttributeValues, hold);
          }
          else
          {
            // schedule the callback for the appropriate time
            //
            Future future = schedule(
              reflectAttributeValues.getUpdateTime(),
              new AddCallback(reflectAttributeValues));

            // register the message retraction handle
            //
            messageRetractionManager.add(
              reflectAttributeValues.getUpdateTime(), future,
              reflectAttributeValues.getMessageRetractionHandle());
          }
        }
        finally
        {
          timeManager.getTimeLock().readLock().unlock();
        }
      }
      else if (message instanceof ReceiveInteraction)
      {
        ReceiveInteraction receiveInteraction = (ReceiveInteraction) message;

        timeManager.getTimeLock().readLock().lock();
        try
        {
          OrderType receivedOrderType =
            receiveInteraction.getSentOrderType() == OrderType.TIMESTAMP &&
            timeManager.isTimeConstrained() ? OrderType.TIMESTAMP :
              OrderType.RECEIVE;

          receiveInteraction.setReceivedOrderType(receivedOrderType);

          if (receivedOrderType == OrderType.RECEIVE)
          {
            // receive order callbacks need to be held until released if we
            // are constrained and in the time granted state, if asynchronous
            // delivery is disabled
            //
            boolean hold = timeManager.isTimeConstrainedAndTimeGranted() &&
                           !isAsynchronousDeliveryEnabled();

            callbackManager.add(receiveInteraction, hold);
          }
          else
          {
            // schedule the callback for the appropriate time
            //
            Future future = schedule(receiveInteraction.getSendTime(),
                                     new AddCallback(receiveInteraction));

            // register the message retraction handle
            //
            messageRetractionManager.add(
              receiveInteraction.getSendTime(), future,
              receiveInteraction.getMessageRetractionHandle());
          }
        }
        finally
        {
          timeManager.getTimeLock().readLock().unlock();
        }
      }
      else if (message instanceof RemoveObjectInstance)
      {
        RemoveObjectInstance removeObjectInstance =
          (RemoveObjectInstance) message;

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
            Future future = schedule(removeObjectInstance.getDeleteTime(),
                                     new AddCallback(removeObjectInstance));

            // register the message retraction handle
            //
            messageRetractionManager.add(
              removeObjectInstance.getDeleteTime(), future,
              removeObjectInstance.getMessageRetractionHandle());
          }
        }
        finally
        {
          timeManager.getTimeLock().readLock().unlock();
        }
      }
      else
      {
        if (message instanceof TimeAdvanceGrant)
        {
          processFutureTasks(((TimeAdvanceGrant) message).getTime());
        }

        callbackManager.add((Callback) message);
      }
    }
    else if (message instanceof GALTAdvanced)
    {
      GALTAdvanced galtAdvanced = (GALTAdvanced) message;

      LogicalTime galt = galtAdvanced.getGALT();

      timeManager.galtAdvanced(galt);
    }
    else
    {
      processed = false;
    }
    return processed;
  }

  public void resignFederationExecution(ResignAction resignAction)
    throws OwnershipAcquisitionPending, FederateOwnsAttributes,
           RTIinternalError
  {
    federateStateLock.writeLock().lock();
    try
    {
      objectManager.resignFederationExecution(resignAction);

      federateState = FederateState.RESIGNED;
    }
    finally
    {
      federateStateLock.writeLock().unlock();
    }
  }

  public void registerFederationSynchronizationPoint(String label, byte[] tag)
    throws SaveInProgress, RestoreInProgress, RTIinternalError
  {
    registerFederationSynchronizationPoint(label, tag, null);
  }

  public void registerFederationSynchronizationPoint(
    String label, byte[] tag, FederateHandleSet federateHandles)
    throws SaveInProgress, RestoreInProgress, RTIinternalError
  {
    WriteFuture writeFuture;

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      writeFuture = rtiSession.write(
        new RegisterFederationSynchronizationPoint(
          label, tag, federateHandles));
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }

    // TODO: set timeout
    //
    writeFuture.join();

    if (!writeFuture.isWritten())
    {
      throw new RTIinternalError("error communicating with RTI");
    }
  }

  public void synchronizationPointAchieved(String label)
    throws SynchronizationPointLabelNotAnnounced, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    WriteFuture writeFuture = null;

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      synchronizationPointLock.lock();
      try
      {
        FederateSynchronizationPoint federateSynchronizationPoint =
          synchronizationPoints.get(label);
        if (federateSynchronizationPoint == null)
        {
          throw new SynchronizationPointLabelNotAnnounced(label);
        }
        else
        {
          federateSynchronizationPoint.synchronizationPointAchieved();

          writeFuture =
            rtiSession.write(new SynchronizationPointAchieved(label));
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

    if (writeFuture != null)
    {
      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }
    }
  }

  public void requestFederationSave(String label)
    throws SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.writeLock().lock();
    try
    {
      checkIfActive();

      RequestFederationSave requestFederationSave =
        new RequestFederationSave(label);
      WriteFuture writeFuture = rtiSession.write(requestFederationSave);

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      // TODO: set timeout
      //
      Object response = requestFederationSave.getResponse();
      if (response instanceof SaveInProgress)
      {
        throw new SaveInProgress((SaveInProgress) response);
      }
      else if (response instanceof RestoreInProgress)
      {
        throw new RestoreInProgress((RestoreInProgress) response);
      }
      else
      {
        assert response == null :
          String.format("unexpected response: %s", response);
      }
    }
    catch (InterruptedException ie)
    {
      throw new RTIinternalError("interrupted awaiting timeout", ie);
    }
    catch (ExecutionException ee)
    {
      throw new RTIinternalError("unable to get response", ee);
    }
    finally
    {
      federateStateLock.writeLock().unlock();
    }
  }

  public void requestFederationSave(String label, LogicalTime saveTime)
    throws LogicalTimeAlreadyPassed, InvalidLogicalTime,
           FederateUnableToUseTime, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    federateStateLock.writeLock().lock();
    try
    {
      checkIfActive();

      // no need to lock time manager because we have a write lock on the
      // federate state
      //
      timeManager.checkIfLogicalTimeAlreadyPassed(saveTime);

      RequestFederationSave requestFederationSave =
        new RequestFederationSave(label, saveTime);
      WriteFuture writeFuture = rtiSession.write(requestFederationSave);

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      // TODO: set timeout
      //
      Object response = requestFederationSave.getResponse();
      if (response instanceof LogicalTimeAlreadyPassed)
      {
        throw new LogicalTimeAlreadyPassed((LogicalTimeAlreadyPassed) response);
      }
      else if (response instanceof SaveInProgress)
      {
        throw new SaveInProgress((SaveInProgress) response);
      }
      else if (response instanceof RestoreInProgress)
      {
        throw new RestoreInProgress((RestoreInProgress) response);
      }
      else
      {
        assert false : String.format("unexpected response: %s", response);
      }
    }
    catch (InterruptedException ie)
    {
      throw new RTIinternalError("interrupted awaiting timeout", ie);
    }
    catch (ExecutionException ee)
    {
      throw new RTIinternalError("unable to get response", ee);
    }
    finally
    {
      federateStateLock.writeLock().unlock();
    }
  }

  public void federateSaveBegun()
    throws SaveNotInitiated, RestoreInProgress, RTIinternalError
  {
    WriteFuture writeFuture;

    federateStateLock.readLock().lock();
    try
    {
      if (saveStatus != SaveStatus.FEDERATE_INSTRUCTED_TO_SAVE)
      {
        throw new SaveNotInitiated();
      }

      writeFuture = rtiSession.write(new FederateSaveBegun());

      saveStatus = SaveStatus.FEDERATE_SAVING;
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }

    // TODO: set timeout
    //
    writeFuture.join();

    if (!writeFuture.isWritten())
    {
      throw new RTIinternalError("error communicating with RTI");
    }
  }

  public void federateSaveComplete()
    throws FederateHasNotBegunSave, RestoreInProgress, RTIinternalError
  {
    WriteFuture writeFuture;

    federateStateLock.readLock().lock();
    try
    {
      checkIfRestoreInProgress();

      if (saveStatus != SaveStatus.FEDERATE_SAVING)
      {
        throw new FederateHasNotBegunSave();
      }

      writeFuture = rtiSession.write(new FederateSaveComplete(null));

      saveStatus = SaveStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_SAVE;
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }

    // TODO: set timeout
    //
    writeFuture.join();

    if (!writeFuture.isWritten())
    {
      throw new RTIinternalError("error communicating with RTI");
    }
  }

  public void federateSaveNotComplete()
    throws FederateHasNotBegunSave, RestoreInProgress, RTIinternalError
  {
    WriteFuture writeFuture;

    federateStateLock.readLock().lock();
    try
    {
      checkIfRestoreInProgress();

      if (saveStatus != SaveStatus.FEDERATE_SAVING)
      {
        throw new FederateHasNotBegunSave();
      }

      writeFuture = rtiSession.write(new FederateSaveNotComplete());

      saveStatus = SaveStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_SAVE;
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }

    // TODO: set timeout
    //
    writeFuture.join();

    if (!writeFuture.isWritten())
    {
      throw new RTIinternalError("error communicating with RTI");
    }
  }

  public void queryFederationSaveStatus()
    throws RestoreInProgress, RTIinternalError
  {
    WriteFuture writeFuture;

    federateStateLock.readLock().lock();
    try
    {
      checkIfRestoreInProgress();

      writeFuture = rtiSession.write(new QueryFederationSaveStatus());
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }

    // TODO: set timeout
    //
    writeFuture.join();

    if (!writeFuture.isWritten())
    {
      throw new RTIinternalError("error communicating with RTI");
    }
  }

  public void requestFederationRestore(String label)
    throws SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      RequestFederationRestore requestFederationRestore =
        new RequestFederationRestore(label);
      WriteFuture writeFuture = rtiSession.write(requestFederationRestore);

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      // TODO: set timeout
      //
      Object response = requestFederationRestore.getResponse();
      if (response instanceof SaveInProgress)
      {
        throw new SaveInProgress((SaveInProgress) response);
      }
      else if (response instanceof RestoreInProgress)
      {
        throw new RestoreInProgress((RestoreInProgress) response);
      }
      else
      {
        assert false : String.format("unexpected response: %s", response);
      }
    }
    catch (InterruptedException ie)
    {
      throw new RTIinternalError("interrupted awaiting timeout", ie);
    }
    catch (ExecutionException ee)
    {
      throw new RTIinternalError("unable to get response", ee);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void federateRestoreComplete()
    throws RestoreNotRequested, SaveInProgress, RTIinternalError
  {
    WriteFuture writeFuture;

    federateStateLock.readLock().lock();
    try
    {
      checkIfSaveInProgress();

      if (restoreStatus != RestoreStatus.FEDERATE_RESTORING)
      {
        throw new RestoreNotRequested();
      }

      writeFuture = rtiSession.write(new FederateRestoreComplete());

      restoreStatus = RestoreStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_RESTORE;
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }

    // TODO: set timeout
    //
    writeFuture.join();

    if (!writeFuture.isWritten())
    {
      throw new RTIinternalError("error communicating with RTI");
    }
  }

  public void federateRestoreNotComplete()
    throws RestoreNotRequested, SaveInProgress, RTIinternalError
  {
    WriteFuture writeFuture;

    federateStateLock.readLock().lock();
    try
    {
      checkIfSaveInProgress();

      if (restoreStatus != RestoreStatus.FEDERATE_RESTORING)
      {
        throw new RestoreNotRequested();
      }

      writeFuture = rtiSession.write(new FederateRestoreNotComplete());

      restoreStatus = RestoreStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_RESTORE;
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }

    // TODO: set timeout
    //
    writeFuture.join();

    if (!writeFuture.isWritten())
    {
      throw new RTIinternalError("error communicating with RTI");
    }
  }

  public void queryFederationRestoreStatus()
    throws SaveInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfSaveInProgress();

      WriteFuture writeFuture =
        rtiSession.write(new QueryFederationRestoreStatus());

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void publishObjectClassAttributes(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    // ensure we have a valid object class and attribute handles
    //
    fdd.checkIfAttributeNotDefined(objectClassHandle, attributeHandles);

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.publishObjectClassAttributes(
        objectClassHandle, attributeHandles);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void unpublishObjectClass(ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined, OwnershipAcquisitionPending,
           SaveInProgress, RestoreInProgress, RTIinternalError
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
    throws ObjectClassNotDefined, AttributeNotDefined,
           OwnershipAcquisitionPending, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    // ensure we have a valid object class handle
    //
    fdd.checkIfObjectClassNotDefined(objectClassHandle);

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.unpublishObjectClassAttributes(
        objectClassHandle, attributeHandles);

      // TODO: give up ownership of the specified attributes
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void publishInteractionClass(
    InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, SaveInProgress, RestoreInProgress,
           RTIinternalError
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

  public void unpublishInteractionClass(
    InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, SaveInProgress, RestoreInProgress,
           RTIinternalError
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

  public void subscribeObjectClassAttributes(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    subscribeObjectClassAttributes(objectClassHandle, attributeHandles, false);
  }

  public void subscribeObjectClassAttributesPassively(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    subscribeObjectClassAttributes(objectClassHandle, attributeHandles, true);
  }

  protected void subscribeObjectClassAttributes(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles,
    boolean passive)
    throws ObjectClassNotDefined, AttributeNotDefined, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    // ensure we have a valid object class and attribute handles
    //
    fdd.checkIfAttributeNotDefined(objectClassHandle, attributeHandles);

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.subscribeObjectClassAttributes(
        objectClassHandle, attributeHandles, passive);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void unsubscribeObjectClass(ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined, SaveInProgress, RestoreInProgress,
           RTIinternalError
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
    throws ObjectClassNotDefined, AttributeNotDefined, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    // ensure we have a valid object class handle
    //
    fdd.checkIfObjectClassNotDefined(objectClassHandle);

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.unsubscribeObjectClassAttributes(
        objectClassHandle, attributeHandles);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void subscribeInteractionClass(
    InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined,
           FederateServiceInvocationsAreBeingReportedViaMOM, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    subscribeInteractionClass(interactionClassHandle, false);
  }

  public void subscribeInteractionClassPassively(
    InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined,
           FederateServiceInvocationsAreBeingReportedViaMOM, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    subscribeInteractionClass(interactionClassHandle, true);
  }

  protected void subscribeInteractionClass(
    InteractionClassHandle interactionClassHandle, boolean passive)
    throws InteractionClassNotDefined,
           FederateServiceInvocationsAreBeingReportedViaMOM, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    // ensure we have a valid interaction class handle
    //
    fdd.checkIfInteractionClassNotDefined(interactionClassHandle);

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.subscribeInteractionClass(interactionClassHandle, passive);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void unsubscribeInteractionClass(
    InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, SaveInProgress, RestoreInProgress,
           RTIinternalError
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

  public void reserveObjectInstanceName(String name)
    throws IllegalName, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (name.startsWith("HLA"))
    {
      throw new IllegalName(String.format("starts with 'HLA': %s", name));
    }

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.reserveObjectInstanceName(name);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public ObjectInstanceHandle registerObjectInstance(
    ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined, ObjectClassNotPublished, SaveInProgress,
           RestoreInProgress, RTIinternalError
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

  public ObjectInstanceHandle registerObjectInstance(
    ObjectClassHandle objectClassHandle, String name)
    throws ObjectClassNotDefined, ObjectClassNotPublished,
           ObjectInstanceNameNotReserved, ObjectInstanceNameInUse,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      return objectManager.registerObjectInstance(objectClassHandle, name);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void updateAttributeValues(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleValueMap attributeValues, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.updateAttributeValues(
        objectInstanceHandle, attributeValues, tag);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public MessageRetractionReturn updateAttributeValues(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleValueMap attributeValues, byte[] tag, LogicalTime updateTime)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           InvalidLogicalTime, SaveInProgress, RestoreInProgress,
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
        timeManager.updateAttributeValues(updateTime);

        OrderType sentOrderType =
          timeManager.isTimeRegulating() && updateTime != null ?
            OrderType.TIMESTAMP : OrderType.RECEIVE;

        if (sentOrderType == OrderType.TIMESTAMP)
        {
          messageRetractionHandle = messageRetractionManager.add(updateTime);
        }

        objectManager.updateAttributeValues(
          objectInstanceHandle, attributeValues, tag, updateTime,
          messageRetractionHandle, sentOrderType);
      }
      finally
      {
        timeManager.getTimeLock().readLock().unlock();
      }

      return new MessageRetractionReturn(messageRetractionHandle != null,
                                         messageRetractionHandle);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void sendInteraction(
    InteractionClassHandle interactionClassHandle,
    ParameterHandleValueMap parameterValues, byte[] tag)
    throws InteractionClassNotPublished, InteractionClassNotDefined,
           InteractionParameterNotDefined, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    // ensure we have a valid interaction class and parameter handles
    //
    fdd.checkIfInteractionParameterNotDefined(
      interactionClassHandle, parameterValues.keySet());

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.sendInteraction(
        interactionClassHandle, parameterValues, tag);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public MessageRetractionReturn sendInteraction(
    InteractionClassHandle interactionClassHandle,
    ParameterHandleValueMap parameterValues, byte[] tag, LogicalTime sendTime)
    throws InteractionClassNotPublished, InteractionClassNotDefined,
           InteractionParameterNotDefined, InvalidLogicalTime, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    // ensure we have a valid interaction class and parameter handles
    //
    fdd.checkIfInteractionParameterNotDefined(
      interactionClassHandle, parameterValues.keySet());

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
          timeManager.isTimeRegulating() && sendTime != null ?
            OrderType.TIMESTAMP : OrderType.RECEIVE;

        if (sentOrderType == OrderType.TIMESTAMP)
        {
          messageRetractionHandle = messageRetractionManager.add(sendTime);
        }

        objectManager.sendInteraction(
          interactionClassHandle, parameterValues, tag, sendTime,
          messageRetractionHandle, sentOrderType);
      }
      finally
      {
        timeManager.getTimeLock().readLock().unlock();
      }

      return new MessageRetractionReturn(messageRetractionHandle != null,
                                         messageRetractionHandle);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void deleteObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag)
    throws ObjectInstanceNotKnown, DeletePrivilegeNotHeld, SaveInProgress,
           RestoreInProgress, RTIinternalError
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
    ObjectInstanceHandle objectInstanceHandle, byte[] tag,
    LogicalTime deleteTime)
    throws ObjectInstanceNotKnown, DeletePrivilegeNotHeld, InvalidLogicalTime,
           SaveInProgress, RestoreInProgress, RTIinternalError
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

      return new MessageRetractionReturn(messageRetractionHandle != null,
                                         messageRetractionHandle);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void localDeleteObjectInstance(
    ObjectInstanceHandle objectInstanceHandle)
    throws ObjectInstanceNotKnown, FederateOwnsAttributes,
           OwnershipAcquisitionPending, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    objectManager.localDeleteObjectInstance(objectInstanceHandle);
  }

  public void changeAttributeTransportationType(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, TransportationType transportationType)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.changeAttributeTransportationType(
        objectInstanceHandle, attributeHandles, transportationType);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void changeInteractionTransportationType(
    InteractionClassHandle interactionClassHandle,
    TransportationType transportationType)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.changeInteractionTransportationType(
        interactionClassHandle, transportationType);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void requestAttributeValueUpdate(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.requestAttributeValueUpdate(
        objectInstanceHandle, attributeHandles, tag);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void requestAttributeValueUpdate(
    ObjectClassHandle objectClassHandle,
    AttributeHandleSet attributeHandles,
    byte[] tag)
    throws ObjectClassNotDefined, AttributeNotDefined, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    // check to see if the handles are valid
    //
    fdd.checkIfAttributeNotDefined(objectClassHandle, attributeHandles);

    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      WriteFuture writeFuture = rtiSession.write(
        new RequestAttributeValueUpdate(
          objectClassHandle, attributeHandles, tag));

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void unconditionalAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.unconditionalAttributeOwnershipDivestiture(
        objectInstanceHandle, attributeHandles);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void negotiatedAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           AttributeAlreadyBeingDivested, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.negotiatedAttributeOwnershipDivestiture(
        objectInstanceHandle, attributeHandles, tag);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void confirmDivestiture(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           AttributeDivestitureWasNotRequested, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.confirmDivestiture(
        objectInstanceHandle, attributeHandles, tag);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisition(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, ObjectClassNotPublished, AttributeNotDefined,
           AttributeNotPublished, FederateOwnsAttributes, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.attributeOwnershipAcquisition(
        objectInstanceHandle, attributeHandles, tag);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisitionIfAvailable(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, ObjectClassNotPublished, AttributeNotDefined,
           AttributeNotPublished, FederateOwnsAttributes,
           AttributeAlreadyBeingAcquired, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.attributeOwnershipAcquisitionIfAvailable(
        objectInstanceHandle, attributeHandles);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public AttributeHandleSet attributeOwnershipDivestitureIfWanted(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      return objectManager.attributeOwnershipDivestitureIfWanted(
        objectInstanceHandle, attributeHandles);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           AttributeDivestitureWasNotRequested, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.cancelNegotiatedAttributeOwnershipDivestiture(
        objectInstanceHandle, attributeHandles);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void cancelAttributeOwnershipAcquisition(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeAlreadyOwned,
           AttributeAcquisitionWasNotRequested, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.cancelAttributeOwnershipAcquisition(
        objectInstanceHandle, attributeHandles);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void queryAttributeOwnership(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
    throws ObjectInstanceNotKnown, AttributeNotDefined, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.queryAttributeOwnership(
        objectInstanceHandle, attributeHandle);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public boolean isAttributeOwnedByFederate(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
    throws ObjectInstanceNotKnown, AttributeNotDefined, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      return objectManager.isAttributeOwnedByFederate(
        objectInstanceHandle, attributeHandle);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void enableTimeRegulation(LogicalTimeInterval lookahead)
    throws TimeRegulationAlreadyEnabled, InvalidLookahead, InTimeAdvancingState,
           RequestForTimeRegulationPending, SaveInProgress, RestoreInProgress,
           RTIinternalError
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
    throws TimeRegulationIsNotEnabled, SaveInProgress, RestoreInProgress,
           RTIinternalError
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
    throws TimeConstrainedAlreadyEnabled, InTimeAdvancingState,
           RequestForTimeConstrainedPending, SaveInProgress, RestoreInProgress,
           RTIinternalError
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
    throws TimeConstrainedIsNotEnabled, SaveInProgress, RestoreInProgress,
           RTIinternalError
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
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           SaveInProgress, RestoreInProgress, RTIinternalError
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
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           SaveInProgress, RestoreInProgress, RTIinternalError
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
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           SaveInProgress, RestoreInProgress, RTIinternalError
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
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           SaveInProgress, RestoreInProgress, RTIinternalError
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
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           SaveInProgress, RestoreInProgress, RTIinternalError
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
    throws AsynchronousDeliveryAlreadyEnabled, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      if (isAsynchronousDeliveryEnabled())
      {
        throw new AsynchronousDeliveryAlreadyEnabled();
      }

      asynchronousDeliveryEnabled = true;
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void disableAsynchronousDelivery()
    throws AsynchronousDeliveryAlreadyDisabled, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      if (!isAsynchronousDeliveryEnabled())
      {
        throw new AsynchronousDeliveryAlreadyDisabled();
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
    throws TimeRegulationIsNotEnabled, InvalidLookahead, InTimeAdvancingState,
           SaveInProgress, RestoreInProgress, RTIinternalError
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
    throws TimeRegulationIsNotEnabled, SaveInProgress, RestoreInProgress,
           RTIinternalError
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
    throws InvalidMessageRetractionHandle, TimeRegulationIsNotEnabled,
           MessageCanNoLongerBeRetracted, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      timeManager.getTimeLock().readLock().lock();
      try
      {
        timeManager.checkIfTimeRegulationIsNotEnabled();

        messageRetractionManager.retract(messageRetractionHandle,
                                         timeManager.queryLogicalTime());

        WriteFuture writeFuture = rtiSession.write(
          new Retract(messageRetractionHandle));

        // TODO: set timeout
        //
        writeFuture.join();

        if (!writeFuture.isWritten())
        {
          throw new RTIinternalError("error communicating with RTI");
        }
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
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, OrderType orderType)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.changeAttributeOrderType(
        objectInstanceHandle, attributeHandles, orderType);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void changeInteractionOrderType(
    InteractionClassHandle interactionClassHandle, OrderType orderType)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.changeInteractionOrderType(
        interactionClassHandle, orderType);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public RegionHandle createRegion(DimensionHandleSet dimensionHandles)
    throws InvalidDimensionHandle, SaveInProgress, RestoreInProgress,
           RTIinternalError
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
    throws InvalidRegion, RegionNotCreatedByThisFederate, SaveInProgress,
           RestoreInProgress, RTIinternalError
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
    throws InvalidRegion, RegionNotCreatedByThisFederate,
           RegionInUseForUpdateOrSubscription, SaveInProgress,
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

  public ObjectInstanceHandle registerObjectInstanceWithRegions(
    ObjectClassHandle objectClassHandle,
    AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectClassNotDefined, ObjectClassNotPublished, AttributeNotDefined,
           AttributeNotPublished, InvalidRegion, RegionNotCreatedByThisFederate,
           InvalidRegionContext, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      ObjectInstanceHandle objectInstanceHandle =
        registerObjectInstance(objectClassHandle);

      try
      {
        associateRegionsForUpdates(objectInstanceHandle, attributesAndRegions);
      }
      catch (ObjectInstanceNotKnown oink)
      {
        // should never occur
        //
        throw new RTIinternalError("unexpected exception", oink);
      }

      return objectInstanceHandle;
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public ObjectInstanceHandle registerObjectInstanceWithRegions(
    ObjectClassHandle objectClassHandle,
    AttributeSetRegionSetPairList attributesAndRegions, String name)
    throws ObjectClassNotDefined, ObjectClassNotPublished, AttributeNotDefined,
           AttributeNotPublished, InvalidRegion, RegionNotCreatedByThisFederate,
           InvalidRegionContext, ObjectInstanceNameNotReserved,
           ObjectInstanceNameInUse, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      ObjectInstanceHandle objectInstanceHandle =
        registerObjectInstance(objectClassHandle, name);

      try
      {
        associateRegionsForUpdates(objectInstanceHandle, attributesAndRegions);
      }
      catch (ObjectInstanceNotKnown oink)
      {
        // should never occur
        //
        throw new RTIinternalError("unexpected exception", oink);
      }

      return objectInstanceHandle;
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void associateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectInstanceNotKnown, AttributeNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.associateRegionsForUpdates(
        objectInstanceHandle, attributesAndRegions);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void unassociateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectInstanceNotKnown, AttributeNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      objectManager.unassociateRegionsForUpdates(
        objectInstanceHandle, attributesAndRegions);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void subscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle,
    AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    subscribeObjectClassAttributesWithRegions(
      objectClassHandle, attributesAndRegions, false);
  }

  public void subscribeObjectClassAttributesPassivelyWithRegions(
    ObjectClassHandle objectClassHandle,
    AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    subscribeObjectClassAttributesWithRegions(
      objectClassHandle, attributesAndRegions, true);
  }

  protected void subscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle,
    AttributeSetRegionSetPairList attributesAndRegions, boolean passive)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      for (AttributeRegionAssociation attributeRegionAssociation : attributesAndRegions)
      {
        fdd.checkIfAttributeNotDefined(objectClassHandle,
                                       attributeRegionAssociation.attributes);

        regionManager.subscribeObjectClassAttributesWithRegions(
          objectClassHandle, attributeRegionAssociation, passive);
      }

      WriteFuture writeFuture = rtiSession.write(
        new SubscribeObjectClassAttributes(
          objectClassHandle, attributesAndRegions, passive));

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void unsubscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle,
    AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      for (AttributeRegionAssociation attributeRegionAssociation : attributesAndRegions)
      {
        fdd.checkIfAttributeNotDefined(objectClassHandle,
                                       attributeRegionAssociation.attributes);

        regionManager.unsubscribeObjectClassAttributesWithRegions(
          objectClassHandle, attributeRegionAssociation);
      }

      WriteFuture writeFuture = rtiSession.write(
        new UnsubscribeObjectClassAttributes(
          objectClassHandle, attributesAndRegions));

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void subscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle,
    RegionHandleSet regionHandles)
    throws InteractionClassNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext,
           FederateServiceInvocationsAreBeingReportedViaMOM, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    subscribeInteractionClassWithRegions(
      interactionClassHandle, regionHandles, false);
  }

  public void subscribeInteractionClassPassivelyWithRegions(
    InteractionClassHandle interactionClassHandle,
    RegionHandleSet regionHandles)
    throws InteractionClassNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext,
           FederateServiceInvocationsAreBeingReportedViaMOM, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    subscribeInteractionClassWithRegions(
      interactionClassHandle, regionHandles, true);
  }

  protected void subscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle,
    RegionHandleSet regionHandles, boolean passive)
    throws InteractionClassNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext,
           FederateServiceInvocationsAreBeingReportedViaMOM, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    fdd.checkIfInteractionClassNotDefined(interactionClassHandle);
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      regionManager.subscribeInteractionClassWithRegions(
        interactionClassHandle, regionHandles, passive);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void unsubscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle,
    RegionHandleSet regionHandles)
    throws InteractionClassNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      regionManager.unsubscribeInteractionClassWithRegions(
        interactionClassHandle, regionHandles);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void sendInteractionWithRegions(
    InteractionClassHandle interactionClassHandle,
    ParameterHandleValueMap parameterValues,
    RegionHandleSet regionHandles, byte[] tag)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           InteractionParameterNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    // ensure we have a valid interaction class and parameter handles
    //
    fdd.checkIfInteractionParameterNotDefined(interactionClassHandle,
                                              parameterValues.keySet());
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

//      objectManager.checkIfInteractionClassPublished(interactionClassHandle);

      WriteFuture writeFuture = rtiSession.write(new SendInteraction(
        interactionClassHandle, parameterValues, tag, OrderType.RECEIVE,
        TransportationType.HLA_RELIABLE, regionHandles));

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public MessageRetractionReturn sendInteractionWithRegions(
    InteractionClassHandle interactionClassHandle,
    ParameterHandleValueMap parameterValues,
    RegionHandleSet regionHandles, byte[] tag, LogicalTime sendTime)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           InteractionParameterNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext,
           InvalidLogicalTime, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    // ensure we have a valid interaction class and parameter handles
    //
    fdd.checkIfInteractionParameterNotDefined(interactionClassHandle,
                                              parameterValues.keySet());
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

//      objectManager.checkIfInteractionClassPublished(interactionClassHandle);

      OrderType sentOrderType =
        timeManager.isTimeRegulating() && sendTime != null ?
          OrderType.TIMESTAMP : OrderType.RECEIVE;

      MessageRetractionHandle messageRetractionHandle = null;
      if (sentOrderType == OrderType.TIMESTAMP)
      {
        messageRetractionHandle = messageRetractionManager.add(sendTime);
      }

      WriteFuture writeFuture = rtiSession.write(new SendInteraction(
        interactionClassHandle, parameterValues, tag, OrderType.RECEIVE,
        TransportationType.HLA_RELIABLE, sendTime, messageRetractionHandle,
        regionHandles));

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      return new MessageRetractionReturn(messageRetractionHandle != null,
                                         messageRetractionHandle);
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void requestAttributeValueUpdateWithRegions(
    ObjectClassHandle objectClassHandle,
    AttributeSetRegionSetPairList attributesAndRegions, byte[] tag)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      WriteFuture writeFuture = rtiSession.write(
        new RequestAttributeValueUpdate(
          objectClassHandle, attributesAndRegions, tag));

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }
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

  public AttributeHandle getAttributeHandle(
    ObjectClassHandle objectClassHandle, String name)
    throws InvalidObjectClassHandle, NameNotFound, RTIinternalError
  {
    return fdd.getAttributeHandle(objectClassHandle, name);
  }

  public String getAttributeName(
    ObjectClassHandle objectClassHandle, AttributeHandle attributeHandle)
    throws InvalidObjectClassHandle, InvalidAttributeHandle,
           AttributeNotDefined, RTIinternalError
  {
    return fdd.getAttributeName(objectClassHandle, attributeHandle);
  }

  public InteractionClassHandle getInteractionClassHandle(String name)
    throws NameNotFound, RTIinternalError
  {
    return fdd.getInteractionClassHandle(name);
  }

  public String getInteractionClassName(
    InteractionClassHandle interactionClassHandle)
    throws InvalidInteractionClassHandle, RTIinternalError
  {
    return fdd.getInteractionClassName(interactionClassHandle);
  }

  public ParameterHandle getParameterHandle(
    InteractionClassHandle interactionClassHandle, String name)
    throws InvalidInteractionClassHandle, NameNotFound, RTIinternalError
  {
    return fdd.getParameterHandle(interactionClassHandle, name);
  }

  public String getParameterName(
    InteractionClassHandle interactionClassHandle,
    ParameterHandle parameterHandle)
    throws InvalidInteractionClassHandle, InvalidParameterHandle,
           InteractionParameterNotDefined, RTIinternalError
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
    throws InvalidObjectClassHandle, InvalidAttributeHandle,
           AttributeNotDefined, RTIinternalError
  {
    return fdd.getAvailableDimensionsForClassAttribute(
      objectClassHandle, attributeHandle,
      getDimensionHandleSetFactory().create());
  }

  public ObjectClassHandle getKnownObjectClassHandle(
    ObjectInstanceHandle objectInstanceHandle)
    throws ObjectInstanceNotKnown, RTIinternalError
  {
    return objectManager.getObjectClassHandle(objectInstanceHandle);
  }

  public DimensionHandleSet getAvailableDimensionsForInteractionClass(
    InteractionClassHandle interactionClassHandle)
    throws InvalidInteractionClassHandle, RTIinternalError
  {
    return fdd.getAvailableDimensionsForInteractionClass(
      interactionClassHandle, getDimensionHandleSetFactory().create());
  }

  public TransportationType getTransportationType(String name)
    throws InvalidTransportationName, RTIinternalError
  {
    return fdd.getTransportationType(name);
  }

  public String getTransportationName(TransportationType transportationType)
    throws InvalidTransportationType, RTIinternalError
  {
    return fdd.getTransportationName(transportationType);
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
    throws ObjectClassRelevanceAdvisorySwitchIsOn, SaveInProgress,
           RestoreInProgress, RTIinternalError
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
    throws ObjectClassRelevanceAdvisorySwitchIsOff, SaveInProgress,
           RestoreInProgress, RTIinternalError
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
    throws AttributeRelevanceAdvisorySwitchIsOn, SaveInProgress,
           RestoreInProgress, RTIinternalError
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
    throws AttributeRelevanceAdvisorySwitchIsOff, SaveInProgress,
           RestoreInProgress, RTIinternalError
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
    throws AttributeScopeAdvisorySwitchIsOn, SaveInProgress, RestoreInProgress,
           RTIinternalError
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
    throws AttributeScopeAdvisorySwitchIsOff, SaveInProgress, RestoreInProgress,
           RTIinternalError
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
    throws InteractionRelevanceAdvisorySwitchIsOn, SaveInProgress,
           RestoreInProgress, RTIinternalError
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
    throws InteractionRelevanceAdvisorySwitchIsOff, SaveInProgress,
           RestoreInProgress, RTIinternalError
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

  public RangeBounds getRangeBounds(RegionHandle regionHandle,
                                    DimensionHandle dimensionHandle)
    throws InvalidRegion, RegionDoesNotContainSpecifiedDimension,
           SaveInProgress, RestoreInProgress, RTIinternalError
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

  public void setRangeBounds(RegionHandle regionHandle,
                             DimensionHandle dimensionHandle,
                             RangeBounds rangeBounds)
    throws InvalidRegion, RegionNotCreatedByThisFederate,
           RegionDoesNotContainSpecifiedDimension, InvalidRangeBound,
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

  public boolean evokeCallback(double seconds)
    throws RTIinternalError
  {
    return callbackManager.evokeCallback(seconds);
  }

  public boolean evokeMultipleCallbacks(double minimumTime,
                                        double maximumTime)
    throws RTIinternalError
  {
    return callbackManager.evokeMultipleCallbacks(minimumTime, maximumTime);
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

  public String getHLAversion()
  {
    return "1516.1.5";
  }

  public LogicalTime getNextMessageTime()
  {
    futureTasksLock.lock();
    try
    {
      TimestampedFutureTask timestampedFutureTask = futureTasks.peek();
      return timestampedFutureTask == null ?
        null : timestampedFutureTask.getTime();
    }
    finally
    {
      futureTasksLock.unlock();
    }
  }

  protected void checkIfAlreadyExecutionMember()
    throws FederateAlreadyExecutionMember
  {
    if (federateHandle != null)
    {
      throw new FederateAlreadyExecutionMember(federateHandle.toString());
    }
  }

  protected void checkIfSaveInProgress()
    throws SaveInProgress
  {
    if (federateState == FederateState.SAVE_IN_PROGRESS)
    {
      throw new SaveInProgress();
    }
  }

  protected void checkIfRestoreInProgress()
    throws RestoreInProgress
  {
    if (federateState == FederateState.RESTORE_IN_PROGRESS)
    {
      throw new RestoreInProgress();
    }
  }

  protected void checkIfActive()
    throws SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (federateState != FederateState.ACTIVE)
    {
      checkIfSaveInProgress();
      checkIfRestoreInProgress();

      throw new RTIinternalError("federate not active");
    }
  }

  protected Future<Object> schedule(LogicalTime time, Callable<Object> callable)
  {
    log.debug(marker, "scheduling task: {} at {}", callable, time);

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

  protected class TimestampedFutureTask
    extends FutureTask<Object>
    implements Comparable<TimestampedFutureTask>
  {
    protected LogicalTime time;

    public TimestampedFutureTask(LogicalTime time, Callable<Object> callable)
    {
      super(callable);

      this.time = time;
    }

    public LogicalTime getTime()
    {
      return time;
    }

    public int compareTo(TimestampedFutureTask rhs)
    {
      return time.compareTo(rhs.time);
    }
  }

  protected class AddCallback
    implements Callable<Object>
  {
    protected Callback callback;

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

  protected class ScheduledDeleteObjectInstance
    implements Callable<Object>
  {
    protected final ObjectInstanceHandle objectInstanceHandle;
    protected final byte[] tag;

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

  protected class FederateAmbassadorInterceptor
    extends NullFederateAmbassador
  {
    @Override
    public void synchronizationPointRegistrationSucceeded(String label)
      throws FederateInternalError
    {
      federateAmbassador.synchronizationPointRegistrationSucceeded(label);
    }

    @Override
    public void synchronizationPointRegistrationFailed(
      String label, SynchronizationPointFailureReason reason)
      throws FederateInternalError
    {
      federateAmbassador.synchronizationPointRegistrationFailed(label, reason);
    }

    @Override
    public void announceSynchronizationPoint(String label, byte[] tag)
      throws FederateInternalError
    {
      FederateSynchronizationPoint federateSynchronizationPoint;

      synchronizationPointLock.lock();
      try
      {
        federateSynchronizationPoint = synchronizationPoints.get(label);
        if (federateSynchronizationPoint == null)
        {
          federateSynchronizationPoint =
            new FederateSynchronizationPoint(label, tag);
          synchronizationPoints.put(label, federateSynchronizationPoint);
        }
      }
      finally
      {
        synchronizationPointLock.unlock();
      }

      federateSynchronizationPoint.announceSynchronizationPoint();

      federateAmbassador.announceSynchronizationPoint(label, tag);
    }

    @Override
    public void federationSynchronized(String label)
      throws FederateInternalError
    {
      FederateSynchronizationPoint federateSynchronizationPoint;

      synchronizationPointLock.lock();
      try
      {
        // remove the synchronization point
        //
        federateSynchronizationPoint = synchronizationPoints.remove(label);
      }
      finally
      {
        synchronizationPointLock.unlock();
      }

      assert federateSynchronizationPoint != null;

      federateSynchronizationPoint.federationSynchronized();

      federateAmbassador.federationSynchronized(label);
    }

    @Override
    public void initiateFederateSave(String label)
      throws UnableToPerformSave, FederateInternalError
    {
      federateStateLock.writeLock().lock();
      try
      {
        federateAmbassador.initiateFederateSave(label);
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

    @Override
    public void initiateFederateSave(String label, LogicalTime saveTime)
      throws InvalidLogicalTime, UnableToPerformSave, FederateInternalError
    {
      federateStateLock.writeLock().lock();
      try
      {
        federateAmbassador.initiateFederateSave(label, saveTime);
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

    @Override
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

    @Override
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

    @Override
    public void federationSaveStatusResponse(
      FederateHandleSaveStatusPair[] response)
      throws FederateInternalError
    {
      federateAmbassador.federationSaveStatusResponse(response);
    }

    @Override
    public void requestFederationRestoreSucceeded(String label)
      throws FederateInternalError
    {
      federateAmbassador.requestFederationRestoreSucceeded(label);
    }

    @Override
    public void requestFederationRestoreFailed(String label)
      throws FederateInternalError
    {
      federateAmbassador.requestFederationRestoreFailed(label);
    }

    @Override
    public void federationRestoreBegun()
      throws FederateInternalError
    {
      federateAmbassador.federationRestoreBegun();
    }

    @Override
    public void initiateFederateRestore(String label,
                                        FederateHandle federateHandle)
      throws SpecifiedSaveLabelDoesNotExist, CouldNotInitiateRestore,
             FederateInternalError
    {
      federateAmbassador.initiateFederateRestore(label, federateHandle);
    }

    @Override
    public void federationRestored()
      throws FederateInternalError
    {
      federateAmbassador.federationRestored();
    }

    @Override
    public void federationNotRestored(RestoreFailureReason reason)
      throws FederateInternalError
    {
      federateAmbassador.federationNotRestored(reason);
    }

    @Override
    public void federationRestoreStatusResponse(
      FederateHandleRestoreStatusPair[] response)
      throws FederateInternalError
    {
      federateAmbassador.federationRestoreStatusResponse(response);
    }

    @Override
    public void startRegistrationForObjectClass(
      ObjectClassHandle objectClassHandle)
      throws ObjectClassNotPublished, FederateInternalError
    {
      federateAmbassador.startRegistrationForObjectClass(objectClassHandle);
    }

    @Override
    public void stopRegistrationForObjectClass(
      ObjectClassHandle objectClassHandle)
      throws ObjectClassNotPublished, FederateInternalError
    {
      federateAmbassador.stopRegistrationForObjectClass(objectClassHandle);
    }

    @Override
    public void turnInteractionsOn(
      InteractionClassHandle interactionClassHandle)
      throws InteractionClassNotPublished, FederateInternalError
    {
      federateAmbassador.turnInteractionsOn(interactionClassHandle);
    }

    @Override
    public void turnInteractionsOff(
      InteractionClassHandle interactionClassHandle)
      throws InteractionClassNotPublished, FederateInternalError
    {
      federateAmbassador.turnInteractionsOff(interactionClassHandle);
    }

    @Override
    public void objectInstanceNameReservationSucceeded(String name)
      throws UnknownName, FederateInternalError
    {
      objectManager.objectInstanceNameReservationSucceeded(
        name, federateAmbassador);
    }

    @Override
    public void objectInstanceNameReservationFailed(String name)
      throws UnknownName, FederateInternalError
    {
      federateAmbassador.objectInstanceNameReservationFailed(name);
    }

    @Override
    public void discoverObjectInstance(
      ObjectInstanceHandle objectInstanceHandle,
      ObjectClassHandle objectClassHandle, String name)
      throws CouldNotDiscover, ObjectClassNotRecognized, FederateInternalError
    {
      objectManager.discoverObjectInstance(
        objectInstanceHandle, objectClassHandle, name, federateAmbassador);
    }

    @Override
    public void reflectAttributeValues(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleValueMap attributeValues, byte[] tag,
      OrderType sentOrderType, TransportationType transportationType)
      throws ObjectInstanceNotKnown, AttributeNotRecognized,
             AttributeNotSubscribed, FederateInternalError
    {
      objectManager.reflectAttributeValues(
        objectInstanceHandle, attributeValues, tag, sentOrderType,
        transportationType, null, null, null, null, federateAmbassador);
    }

    @Override
    public void reflectAttributeValues(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleValueMap attributeValues, byte[] tag,
      OrderType sentOrderType, TransportationType transportationType,
      RegionHandleSet regionHandles)
      throws ObjectInstanceNotKnown, AttributeNotRecognized,
             AttributeNotSubscribed, FederateInternalError
    {
      objectManager.reflectAttributeValues(
        objectInstanceHandle, attributeValues, tag, sentOrderType,
        transportationType, null, null, null, regionHandles,
        federateAmbassador);
    }

    @Override
    public void reflectAttributeValues(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleValueMap attributeValues, byte[] tag,
      OrderType sentOrderType, TransportationType transportationType,
      LogicalTime updateTime, OrderType receivedOrderType)
      throws ObjectInstanceNotKnown, AttributeNotRecognized,
             AttributeNotSubscribed, FederateInternalError
    {
      objectManager.reflectAttributeValues(
        objectInstanceHandle, attributeValues, tag, sentOrderType,
        transportationType, updateTime, receivedOrderType, null, null,
        federateAmbassador);
    }

    @Override
    public void reflectAttributeValues(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleValueMap attributeValues, byte[] tag,
      OrderType sentOrderType, TransportationType transportationType,
      LogicalTime updateTime, OrderType receivedOrderType,
      RegionHandleSet regionHandles)
      throws ObjectInstanceNotKnown, AttributeNotRecognized,
             AttributeNotSubscribed, FederateInternalError
    {
      objectManager.reflectAttributeValues(
        objectInstanceHandle, attributeValues, tag, sentOrderType,
        transportationType, updateTime, receivedOrderType, null, regionHandles,
        federateAmbassador);
    }

    @Override
    public void reflectAttributeValues(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleValueMap attributeValues, byte[] tag,
      OrderType sentOrderType,
      TransportationType transportationType, LogicalTime updateTime,
      OrderType receivedOrderType,
      MessageRetractionHandle messageRetractionHandle)
      throws ObjectInstanceNotKnown, AttributeNotRecognized,
             AttributeNotSubscribed, InvalidLogicalTime, FederateInternalError
    {
      objectManager.reflectAttributeValues(
        objectInstanceHandle, attributeValues, tag, sentOrderType,
        transportationType, updateTime, receivedOrderType,
        messageRetractionHandle, null, federateAmbassador);
    }

    @Override
    public void reflectAttributeValues(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleValueMap attributeValues, byte[] tag,
      OrderType sentOrderType, TransportationType transportationType,
      LogicalTime updateTime, OrderType receivedOrderType,
      MessageRetractionHandle messageRetractionHandle,
      RegionHandleSet regionHandles)
      throws ObjectInstanceNotKnown, AttributeNotRecognized,
             AttributeNotSubscribed, InvalidLogicalTime, FederateInternalError
    {
      objectManager.reflectAttributeValues(
        objectInstanceHandle, attributeValues, tag, sentOrderType,
        transportationType, updateTime, receivedOrderType,
        messageRetractionHandle, regionHandles, federateAmbassador);
    }

    @Override
    public void receiveInteraction(
      InteractionClassHandle interactionClassHandle,
      ParameterHandleValueMap parameterValues, byte[] tag,
      OrderType sentOrderType, TransportationType transportationType)
      throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
             InteractionClassNotSubscribed, FederateInternalError
    {
      objectManager.receiveInteraction(
        interactionClassHandle, parameterValues, tag, sentOrderType,
        transportationType, null, null, null, null, federateAmbassador);
    }

    @Override
    public void receiveInteraction(
      InteractionClassHandle interactionClassHandle,
      ParameterHandleValueMap parameterValues, byte[] tag,
      OrderType sentOrderType, TransportationType transportationType,
      RegionHandleSet regionHandles)
      throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
             InteractionClassNotSubscribed, FederateInternalError
    {
      objectManager.receiveInteraction(
        interactionClassHandle, parameterValues, tag, sentOrderType,
        transportationType, null, null, null, regionHandles,
        federateAmbassador);
    }

    @Override
    public void receiveInteraction(
      InteractionClassHandle interactionClassHandle,
      ParameterHandleValueMap parameterValues, byte[] tag,
      OrderType sentOrderType, TransportationType transportationType,
      LogicalTime sentTime, OrderType receivedOrderType)
      throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
             InteractionClassNotSubscribed, FederateInternalError
    {
      objectManager.receiveInteraction(
        interactionClassHandle, parameterValues, tag, sentOrderType,
        transportationType, sentTime, receivedOrderType, null, null,
        federateAmbassador);
    }

    @Override
    public void receiveInteraction(
      InteractionClassHandle interactionClassHandle,
      ParameterHandleValueMap parameterValues, byte[] tag,
      OrderType sentOrderType, TransportationType transportationType,
      LogicalTime sentTime, OrderType receivedOrderType,
      RegionHandleSet regionHandles)
      throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
             InteractionClassNotSubscribed, FederateInternalError
    {
      objectManager.receiveInteraction(
        interactionClassHandle, parameterValues, tag, sentOrderType,
        transportationType, sentTime, receivedOrderType, null, regionHandles,
        federateAmbassador);
    }

    @Override
    public void receiveInteraction(
      InteractionClassHandle interactionClassHandle,
      ParameterHandleValueMap parameterValues, byte[] tag,
      OrderType sentOrderType, TransportationType transportationType,
      LogicalTime sentTime, OrderType receivedOrderType,
      MessageRetractionHandle messageRetractionHandle)
      throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
             InteractionClassNotSubscribed, InvalidLogicalTime,
             FederateInternalError
    {
      objectManager.receiveInteraction(
        interactionClassHandle, parameterValues, tag, sentOrderType,
        transportationType, sentTime, receivedOrderType,
        messageRetractionHandle, null, federateAmbassador);
    }

    @Override
    public void receiveInteraction(
      InteractionClassHandle interactionClassHandle,
      ParameterHandleValueMap parameterValues, byte[] tag,
      OrderType sentOrderType, TransportationType transportationType,
      LogicalTime sentTime, OrderType receivedOrderType,
      MessageRetractionHandle messageRetractionHandle,
      RegionHandleSet regionHandles)
      throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
             InteractionClassNotSubscribed, InvalidLogicalTime,
             FederateInternalError
    {
      objectManager.receiveInteraction(
        interactionClassHandle, parameterValues, tag, sentOrderType,
        transportationType, sentTime, receivedOrderType,
        messageRetractionHandle, regionHandles, federateAmbassador);
    }

    @Override
    public void removeObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                                     byte[] tag, OrderType sentOrderType)
      throws ObjectInstanceNotKnown, FederateInternalError
    {
      objectManager.removeObjectInstance(
        objectInstanceHandle, tag, sentOrderType, null, null, null,
        federateAmbassador);
    }

    @Override
    public void removeObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                                     byte[] tag, OrderType sentOrderType,
                                     LogicalTime deleteTime,
                                     OrderType receivedOrderType)
      throws ObjectInstanceNotKnown, FederateInternalError
    {
      objectManager.removeObjectInstance(
        objectInstanceHandle, tag, sentOrderType, deleteTime, receivedOrderType,
        null, federateAmbassador);
    }

    @Override
    public void removeObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                                     byte[] tag, OrderType sentOrderType,
                                     LogicalTime deleteTime,
                                     OrderType receivedOrderType,
                                     MessageRetractionHandle messageRetractionHandle)
      throws ObjectInstanceNotKnown, InvalidLogicalTime, FederateInternalError
    {
      objectManager.removeObjectInstance(
        objectInstanceHandle, tag, sentOrderType, deleteTime, receivedOrderType,
        messageRetractionHandle, federateAmbassador);
    }

    @Override
    public void attributesInScope(ObjectInstanceHandle objectInstanceHandle,
                                  AttributeHandleSet attributeHandles)
      throws ObjectInstanceNotKnown, AttributeNotRecognized,
             AttributeNotSubscribed, FederateInternalError
    {
      federateAmbassador.attributesInScope(
        objectInstanceHandle, attributeHandles);
    }

    @Override
    public void attributesOutOfScope(ObjectInstanceHandle objectInstanceHandle,
                                     AttributeHandleSet attributeHandles)
      throws ObjectInstanceNotKnown, AttributeNotRecognized,
             AttributeNotSubscribed, FederateInternalError
    {
      federateAmbassador.attributesOutOfScope(
        objectInstanceHandle, attributeHandles);
    }

    @Override
    public void provideAttributeValueUpdate(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleSet attributeHandles, byte[] tag)
      throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
             FederateInternalError
    {
      federateAmbassador.provideAttributeValueUpdate(
        objectInstanceHandle, attributeHandles, tag);
    }

    @Override
    public void turnUpdatesOnForObjectInstance(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleSet attributeHandles)
      throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
             FederateInternalError
    {
      federateAmbassador.turnUpdatesOnForObjectInstance(
        objectInstanceHandle, attributeHandles);
    }

    @Override
    public void turnUpdatesOffForObjectInstance(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleSet attributeHandles)
      throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
             FederateInternalError
    {
      federateAmbassador.turnUpdatesOffForObjectInstance(
        objectInstanceHandle, attributeHandles);
    }

    @Override
    public void requestAttributeOwnershipAssumption(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleSet attributeHandles, byte[] tag)
      throws ObjectInstanceNotKnown, AttributeNotRecognized,
             AttributeAlreadyOwned, AttributeNotPublished, FederateInternalError
    {
      federateAmbassador.requestAttributeOwnershipAssumption(
        objectInstanceHandle, attributeHandles, tag);
    }

    @Override
    public void requestDivestitureConfirmation(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleSet attributeHandles)
      throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
             AttributeDivestitureWasNotRequested, FederateInternalError
    {
      federateAmbassador.requestDivestitureConfirmation(
        objectInstanceHandle, attributeHandles);
    }

    @Override
    public void attributeOwnershipAcquisitionNotification(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleSet attributeHandles, byte[] tag)
      throws ObjectInstanceNotKnown, AttributeNotRecognized,
             AttributeAcquisitionWasNotRequested, AttributeAlreadyOwned,
             AttributeNotPublished, FederateInternalError
    {
      objectManager.attributeOwnershipAcquisitionNotification(
        objectInstanceHandle, attributeHandles, tag, federateAmbassador);
    }

    @Override
    public void attributeOwnershipUnavailable(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleSet attributeHandles)
      throws ObjectInstanceNotKnown, AttributeNotRecognized,
             AttributeAlreadyOwned, AttributeAcquisitionWasNotRequested,
             FederateInternalError
    {
      federateAmbassador.attributeOwnershipUnavailable(
        objectInstanceHandle, attributeHandles);
    }

    @Override
    public void requestAttributeOwnershipRelease(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleSet attributeHandles, byte[] tag)
      throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
             FederateInternalError
    {
      federateAmbassador.requestAttributeOwnershipRelease(
        objectInstanceHandle, attributeHandles, tag);
    }

    @Override
    public void confirmAttributeOwnershipAcquisitionCancellation(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleSet attributeHandles)
      throws ObjectInstanceNotKnown, AttributeNotRecognized,
             AttributeAlreadyOwned, AttributeAcquisitionWasNotCanceled,
             FederateInternalError
    {
      federateAmbassador.confirmAttributeOwnershipAcquisitionCancellation(
        objectInstanceHandle, attributeHandles);
    }

    @Override
    public void informAttributeOwnership(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandle attributeHandle, FederateHandle federateHandle)
      throws ObjectInstanceNotKnown, AttributeNotRecognized,
             FederateInternalError
    {
      federateAmbassador.informAttributeOwnership(
        objectInstanceHandle, attributeHandle, federateHandle);
    }

    @Override
    public void attributeIsNotOwned(ObjectInstanceHandle objectInstanceHandle,
                                    AttributeHandle attributeHandle)
      throws ObjectInstanceNotKnown, AttributeNotRecognized,
             FederateInternalError
    {
      federateAmbassador.attributeIsNotOwned(
        objectInstanceHandle, attributeHandle);
    }

    @Override
    public void attributeIsOwnedByRTI(ObjectInstanceHandle objectInstanceHandle,
                                      AttributeHandle attributeHandle)
      throws ObjectInstanceNotKnown, AttributeNotRecognized,
             FederateInternalError
    {
      federateAmbassador.attributeIsOwnedByRTI(
        objectInstanceHandle, attributeHandle);
    }

    @Override
    public void timeRegulationEnabled(LogicalTime time)
      throws InvalidLogicalTime, NoRequestToEnableTimeRegulationWasPending,
             FederateInternalError
    {
      timeManager.timeRegulationEnabled(time, federateAmbassador);
    }

    @Override
    public void timeConstrainedEnabled(LogicalTime time)
      throws InvalidLogicalTime, NoRequestToEnableTimeConstrainedWasPending,
             FederateInternalError
    {
      timeManager.timeConstrainedEnabled(time, federateAmbassador);
    }

    @Override
    public void timeAdvanceGrant(LogicalTime time)
      throws InvalidLogicalTime, JoinedFederateIsNotInTimeAdvancingState,
             FederateInternalError
    {
      timeManager.timeAdvanceGrant(time, federateAmbassador);
    }

    @Override
    public void requestRetraction(
      MessageRetractionHandle messageRetractionHandle)
      throws FederateInternalError
    {
      federateAmbassador.requestRetraction(messageRetractionHandle);
    }
  }
}
