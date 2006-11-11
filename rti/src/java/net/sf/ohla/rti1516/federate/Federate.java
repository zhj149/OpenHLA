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

package net.sf.ohla.rti1516.federate;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

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

import net.sf.ohla.rti1516.OHLAAttributeHandleFactory;
import net.sf.ohla.rti1516.OHLAAttributeHandleSetFactory;
import net.sf.ohla.rti1516.OHLAAttributeHandleValueMapFactory;
import net.sf.ohla.rti1516.OHLAAttributeSetRegionSetPairListFactory;
import net.sf.ohla.rti1516.OHLADimensionHandleFactory;
import net.sf.ohla.rti1516.OHLADimensionHandleSetFactory;
import net.sf.ohla.rti1516.OHLAFederateHandleFactory;
import net.sf.ohla.rti1516.OHLAFederateHandleSetFactory;
import net.sf.ohla.rti1516.OHLAInteractionClassHandleFactory;
import net.sf.ohla.rti1516.OHLAObjectClassHandleFactory;
import net.sf.ohla.rti1516.OHLAObjectInstanceHandleFactory;
import net.sf.ohla.rti1516.OHLAParameterHandleFactory;
import net.sf.ohla.rti1516.OHLAParameterHandleValueMapFactory;
import net.sf.ohla.rti1516.OHLARegionHandleSetFactory;
import net.sf.ohla.rti1516.fdd.FDD;
import net.sf.ohla.rti1516.federate.callbacks.Callback;
import net.sf.ohla.rti1516.federate.callbacks.CallbackManager;
import net.sf.ohla.rti1516.federate.callbacks.DiscoverObjectInstance;
import net.sf.ohla.rti1516.federate.callbacks.InitiateFederateRestore;
import net.sf.ohla.rti1516.federate.callbacks.InitiateFederateSave;
import net.sf.ohla.rti1516.federate.callbacks.ObjectInstanceNameReservationFailed;
import net.sf.ohla.rti1516.federate.callbacks.ObjectInstanceNameReservationSucceeded;
import net.sf.ohla.rti1516.federate.callbacks.ReceiveInteraction;
import net.sf.ohla.rti1516.federate.callbacks.ReflectAttributeValues;
import net.sf.ohla.rti1516.federate.callbacks.SynchronizationPointRegistrationFailed;
import net.sf.ohla.rti1516.federate.callbacks.SynchronizationPointRegistrationSucceeded;
import net.sf.ohla.rti1516.federate.filter.InterestManagementFilter;
import net.sf.ohla.rti1516.federate.objects.ObjectManager;
import net.sf.ohla.rti1516.federate.time.TimeManager;
import net.sf.ohla.rti1516.filter.RequestResponseFilter;
import net.sf.ohla.rti1516.messages.CommitRegionModifications;
import net.sf.ohla.rti1516.messages.CreateRegion;
import net.sf.ohla.rti1516.messages.DefaultResponse;
import net.sf.ohla.rti1516.messages.DeleteRegion;
import net.sf.ohla.rti1516.messages.DisableTimeConstrained;
import net.sf.ohla.rti1516.messages.DisableTimeRegulation;
import net.sf.ohla.rti1516.messages.EnableTimeConstrained;
import net.sf.ohla.rti1516.messages.EnableTimeRegulation;
import net.sf.ohla.rti1516.messages.FederateJoined;
import net.sf.ohla.rti1516.messages.FederateRestoreComplete;
import net.sf.ohla.rti1516.messages.FederateRestoreNotComplete;
import net.sf.ohla.rti1516.messages.FederateSaveBegun;
import net.sf.ohla.rti1516.messages.FederateSaveComplete;
import net.sf.ohla.rti1516.messages.FederateSaveInitiated;
import net.sf.ohla.rti1516.messages.FederateSaveInitiatedFailed;
import net.sf.ohla.rti1516.messages.FederateSaveNotComplete;
import net.sf.ohla.rti1516.messages.JoinFederationExecution;
import net.sf.ohla.rti1516.messages.JoinFederationExecutionResponse;
import net.sf.ohla.rti1516.messages.Message;
import net.sf.ohla.rti1516.messages.ObjectInstanceNameReserved;
import net.sf.ohla.rti1516.messages.QueryFederationRestoreStatus;
import net.sf.ohla.rti1516.messages.QueryFederationSaveStatus;
import net.sf.ohla.rti1516.messages.RegisterFederationSynchronizationPoint;
import net.sf.ohla.rti1516.messages.RequestAttributeValueUpdate;
import net.sf.ohla.rti1516.messages.RequestFederationRestore;
import net.sf.ohla.rti1516.messages.RequestFederationSave;
import net.sf.ohla.rti1516.messages.Retract;
import net.sf.ohla.rti1516.messages.SubscribeObjectClassAttributes;
import net.sf.ohla.rti1516.messages.SynchronizationPointAchieved;
import net.sf.ohla.rti1516.messages.TimeAdvanceRequest;
import net.sf.ohla.rti1516.messages.UnsubscribeObjectClassAttributes;

import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketConnector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import static hla.rti1516.OrderType.RECEIVE;
import static hla.rti1516.OrderType.TIMESTAMP;
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
import hla.rti1516.SaveFailureReason;
import hla.rti1516.SaveInProgress;
import hla.rti1516.SaveNotInitiated;
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
  private static final String PEER_FEDERATE_HANDLE = "PeerFederateHandle";

  private static final Logger log =
    LoggerFactory.getLogger(Federate.class);

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

  protected FederateSaveState federateSaveState;
  protected FederateSave federateSave;

  protected FederateRestoreState federateRestoreState;
  protected FederateRestore federateRestore;

  protected Lock synchronizationPointLock = new ReentrantLock(true);
  protected Map<String, FederateSynchronizationPoint> synchronizationPoints =
    new HashMap<String, FederateSynchronizationPoint>();

  protected boolean asynchronousDeliveryEnabled;

  protected ObjectManager objectManager = new ObjectManager(this);
  protected RegionManager regionManager = new RegionManager(this);
  protected MessageRetractionManager messageRetractionManager =
    new MessageRetractionManager(this);

  protected TimeManager timeManager = new TimeManager(this);
  protected CallbackManager callbackManager = new CallbackManager(this);

  protected Lock futureTasksLock = new ReentrantLock(true);
  protected Queue<TimestampedFutureTask> futureTasks =
    new PriorityQueue<TimestampedFutureTask>();

  protected LogicalTime time;
  protected LogicalTimeInterval lookahead;

  protected AttributeHandleFactory attributeHandleFactory =
    new OHLAAttributeHandleFactory();
  protected AttributeHandleSetFactory attributeHandleSetFactory =
    new OHLAAttributeHandleSetFactory();
  protected AttributeHandleValueMapFactory attributeHandleValueMapFactory =
    new OHLAAttributeHandleValueMapFactory();
  protected AttributeSetRegionSetPairListFactory attributeSetRegionSetPairListFactory =
    new OHLAAttributeSetRegionSetPairListFactory();
  protected DimensionHandleFactory dimensionHandleFactory =
    new OHLADimensionHandleFactory();
  protected DimensionHandleSetFactory dimensionHandleSetFactory =
    new OHLADimensionHandleSetFactory();
  protected FederateHandleFactory federateHandleFactory =
    new OHLAFederateHandleFactory();
  protected FederateHandleSetFactory federateHandleSetFactory =
    new OHLAFederateHandleSetFactory();
  protected InteractionClassHandleFactory interactionClassHandleFactory =
    new OHLAInteractionClassHandleFactory();
  protected ObjectClassHandleFactory objectClassHandleFactory =
    new OHLAObjectClassHandleFactory();
  protected ObjectInstanceHandleFactory objectInstanceHandleFactory =
    new OHLAObjectInstanceHandleFactory();
  protected ParameterHandleFactory parameterHandleFactory =
    new OHLAParameterHandleFactory();
  protected ParameterHandleValueMapFactory parameterHandleValueMapFactory =
    new OHLAParameterHandleValueMapFactory();
  protected RegionHandleSetFactory regionHandleSetFactory =
    new OHLARegionHandleSetFactory();

  /**
   * The session with the RTI.
   */
  protected IoSession rtiSession;

  /**
   * Handles communication with other federates (peers).
   */
  protected PeerIoHandler peerIoHandler = new PeerIoHandler();

  protected Lock peersLock = new ReentrantLock(true);

  /**
   * Sessions connecting to other federates (peers).
   */
  protected Map<FederateHandle, IoSession> peerSessions =
    new HashMap<FederateHandle, IoSession>();

  protected SocketAddress peerConnectionInfo;

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

    startPeerAcceptor(federateType);

    JoinFederationExecution joinFederationExecution =
      new JoinFederationExecution(
        federateType, federationExecutionName, mobileFederateServices,
        peerConnectionInfo);
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

        SocketConnector peerConnector = new SocketConnector();

        peerConnector.setHandler(peerIoHandler);

        // TODO: selection of codec factory
        //
        ProtocolCodecFactory codec = new ObjectSerializationCodecFactory();

        // handles messages to/from bytes
        //
        peerConnector.getFilterChain().addLast(
          "ProtocolCodecFilter", new ProtocolCodecFilter(codec));

        peerConnector.getFilterChain().addLast("LoggingFilter", new LoggingFilter());

        // handles request/response pairs
        //
        peerConnector.getFilterChain().addLast(
          "RequestResponseFilter", new RequestResponseFilter());

        // tracks peers interests and transforms messages to meet those
        // requirements
        //
        peerConnector.getFilterChain().addLast(
          "InterestManagementFilter", new InterestManagementFilter(this));

        for (Map.Entry<FederateHandle, SocketAddress> entry : joinFederationExecutionResponse.getPeerConnectionInfo().entrySet())
        {
          log.debug("connecting to peer: {}", entry.getValue());

          // TODO: selection of local address to connect to peer?
          //
          ConnectFuture future = peerConnector.connect(entry.getValue());
          future.join();

          IoSession peerSession = future.getSession();
          peerSession.setAttribute(PEER_FEDERATE_HANDLE, entry.getKey());

          FederateJoined federateJoined = new FederateJoined(federateHandle);

          writeFuture = peerSession.write(federateJoined);

          // TODO: set timeout
          //
          writeFuture.join();

          if (writeFuture.isWritten())
          {
            // TODO: set timeout
            //
            federateJoined.await();

            peersLock.lock();
            try
            {
              peerSessions.put(entry.getKey(), peerSession);
            }
            finally
            {
              peersLock.unlock();
            }
          }
        }

        log.info("joined federation execution; {}", federateHandle);
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

  public ObjectManager getObjectManager()
  {
    return objectManager;
  }

  public RegionManager getRegionManager()
  {
    return regionManager;
  }

  public MessageRetractionManager getMessageRetractionManager()
  {
    return messageRetractionManager;
  }

  public TimeManager getTimeManager()
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

  public boolean isAsynchronousDeliveryEnabled()
  {
    return asynchronousDeliveryEnabled;
  }

  public boolean process(IoSession session, Object message)
  {
    boolean processed = true;
    if (message instanceof Callback)
    {
      if (message instanceof ReflectAttributeValues)
      {
        ReflectAttributeValues reflectAttributeValues =
          (ReflectAttributeValues) message;

        objectManager.objectReflected(
          reflectAttributeValues.getObjectInstanceHandle(),
          reflectAttributeValues.getObjectClassHandle());
      }
      else if (message instanceof DiscoverObjectInstance)
      {
        DiscoverObjectInstance discoverObjectInstance =
          (DiscoverObjectInstance) message;

        String name = objectManager.createObjectInstanceName(
          discoverObjectInstance.getObjectInstanceHandle(),
          discoverObjectInstance.getObjectClassHandle());
        discoverObjectInstance.setName(name);
      }
      else if (message instanceof InitiateFederateSave)
      {
        InitiateFederateSave initiateFederateSave =
          (InitiateFederateSave) message;

        federateStateLock.writeLock().lock();
        try
        {
          if (federateSave == null)
          {
            federateSave = new FederateSave(
              federateHandle, federateType,
              initiateFederateSave.getParticipants());
          }
        }
        finally
        {
          federateStateLock.writeLock().unlock();
        }
      }
      else if (message instanceof InitiateFederateRestore)
      {
      }

      boolean hold = timeManager.isTimeConstrainedAndTimeGranted() &&
                     !isAsynchronousDeliveryEnabled();

      callbackManager.add((Callback) message, hold);
    }
    else if (message instanceof ObjectInstanceNameReserved)
    {
      ObjectInstanceNameReserved objectInstanceNameReserved =
        (ObjectInstanceNameReserved) message;

      objectManager.objectInstanceNameReserved(
        objectInstanceNameReserved.getName(),
        objectInstanceNameReserved.getObjectInstanceHandle());
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

      federateState = null;
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
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      synchronizationPointLock.lock();
      try
      {
        FederateSynchronizationPoint federateSynchronizationPoint =
          synchronizationPoints.get(label);
        if (federateSynchronizationPoint != null)
        {
          callbackManager.add(new SynchronizationPointRegistrationFailed(
            label,
            SynchronizationPointFailureReason.SYNCHRONIZATION_POINT_LABEL_NOT_UNIQUE));
        }
        else
        {
          RegisterFederationSynchronizationPoint
            registerFederationSynchronizationPoint =
            new RegisterFederationSynchronizationPoint(label, tag,
                                                       federateHandles);
          WriteFuture writeFuture =
            rtiSession.write(registerFederationSynchronizationPoint);

          // TODO: set timeout
          //
          writeFuture.join();

          if (!writeFuture.isWritten())
          {
            throw new RTIinternalError("error communicating with RTI");
          }

          // TODO: set timeout
          //
          Object response =
            registerFederationSynchronizationPoint.getResponse();

          if (response == null)
          {
            callbackManager.add(
              new SynchronizationPointRegistrationSucceeded(label));

            // track the synchronization point upon success
            //
            synchronizationPoints.put(label, new FederateSynchronizationPoint(
              label, tag, federateHandles));
          }
          else if (response instanceof SynchronizationPointFailureReason)
          {
            callbackManager.add(
              new SynchronizationPointRegistrationFailed(
                label, (SynchronizationPointFailureReason) response));
          }
          else
          {
            assert false : String.format("unexpected response: %s", response);
          }
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
        synchronizationPointLock.unlock();
      }
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }
  }

  public void synchronizationPointAchieved(String label)
    throws SynchronizationPointLabelNotAnnounced, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
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

          WriteFuture writeFuture =
            rtiSession.write(new SynchronizationPointAchieved(label));

          // TODO: set timeout
          //
          writeFuture.join();

          if (!writeFuture.isWritten())
          {
            throw new RTIinternalError("error communicating with RTI");
          }
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
    federateStateLock.readLock().lock();
    try
    {
      if (federateSaveState != FederateSaveState.INSTRUCTED_TO_SAVE)
      {
        throw new SaveNotInitiated();
      }

      WriteFuture writeFuture = rtiSession.write(new FederateSaveBegun());

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      federateSaveState = FederateSaveState.SAVING;
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

      if (federateSaveState != FederateSaveState.SAVING)
      {
        throw new FederateHasNotBegunSave();
      }

      WriteFuture writeFuture =
        rtiSession.write(new FederateSaveComplete(null));

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      federateSaveState = FederateSaveState.WAITING_FOR_FEDERATION_TO_SAVE;
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

      if (federateSaveState != FederateSaveState.SAVING)
      {
        throw new FederateHasNotBegunSave();
      }

      WriteFuture writeFuture = rtiSession.write(new FederateSaveNotComplete());

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      federateSaveState = FederateSaveState.WAITING_FOR_FEDERATION_TO_SAVE;
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

      WriteFuture writeFuture =
        rtiSession.write(new QueryFederationSaveStatus());

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
      assert response != null :
        String.format("unexpected response: %s", response);
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
    federateStateLock.readLock().lock();
    try
    {
      checkIfSaveInProgress();

      if (federateRestoreState != FederateRestoreState.RESTORING)
      {
        throw new RestoreNotRequested();
      }

      WriteFuture writeFuture = rtiSession.write(new FederateRestoreComplete());

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      federateRestoreState =
        FederateRestoreState.WAITING_FOR_FEDERATION_TO_RESTORE;
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

      if (federateRestoreState != FederateRestoreState.RESTORING)
      {
        throw new RestoreNotRequested();
      }

      WriteFuture writeFuture =
        rtiSession.write(new FederateRestoreNotComplete());

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      federateRestoreState =
        FederateRestoreState.WAITING_FOR_FEDERATION_TO_RESTORE;
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

  public boolean reserveObjectInstanceName(String name)
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

      boolean reserved = objectManager.reserveObjectInstanceName(name);
      if (reserved)
      {
        callbackManager.add(new ObjectInstanceNameReservationSucceeded(name));
      }
      else
      {
        callbackManager.add(new ObjectInstanceNameReservationFailed(name));
      }
      return reserved;
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
        timeManager.checkIfInvalidLogicalTime(updateTime);

        OrderType sentOrderType =
          timeManager.isTimeRegulating() && updateTime != null ?
            TIMESTAMP : RECEIVE;

        if (sentOrderType == TIMESTAMP)
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
        timeManager.checkIfInvalidLogicalTime(sendTime);

        OrderType sentOrderType =
          timeManager.isTimeRegulating() && sendTime != null ?
            TIMESTAMP : RECEIVE;

        if (sentOrderType == TIMESTAMP)
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
        timeManager.checkIfInvalidLogicalTime(deleteTime);

        OrderType sentOrderType =
          timeManager.isTimeRegulating() && deleteTime != null ?
            TIMESTAMP : RECEIVE;

        if (sentOrderType == TIMESTAMP)
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

      sendToPeers(new RequestAttributeValueUpdate(
        objectClassHandle, attributeHandles, tag));
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

      EnableTimeRegulation enableTimeRegulation =
        new EnableTimeRegulation(lookahead);
      WriteFuture writeFuture = rtiSession.write(enableTimeRegulation);

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      // TODO: set timeout
      //
      Object response = enableTimeRegulation.getResponse();
      assert response != null :
        String.format("unexpected response: %s", response);
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

  public void disableTimeRegulation()
    throws TimeRegulationIsNotEnabled, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      timeManager.disableTimeRegulation();

      DisableTimeRegulation disableTimeRegulation = new DisableTimeRegulation();
      WriteFuture writeFuture = rtiSession.write(disableTimeRegulation);

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      // TODO: set timeout
      //
      Object response = disableTimeRegulation.getResponse();
      assert response != null :
        String.format("unexpected response: %s", response);
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

      EnableTimeConstrained enableTimeConstrained = new EnableTimeConstrained();
      WriteFuture writeFuture = rtiSession.write(enableTimeConstrained);

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      // TODO: set timeout
      //
      Object response = enableTimeConstrained.getResponse();
      assert response != null :
        String.format("unexpected response: %s", response);
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

  public void disableTimeConstrained()
    throws TimeConstrainedIsNotEnabled, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      timeManager.disableTimeConstrained();

      DisableTimeConstrained disableTimeConstrained =
        new DisableTimeConstrained();
      WriteFuture writeFuture = rtiSession.write(disableTimeConstrained);

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      // TODO: set timeout
      //
      Object response = disableTimeConstrained.getResponse();
      assert response != null :
        String.format("unexpected response: %s", response);
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

      if (timeManager.isTimeAdvancing())
      {
        // release any callbacks held until we are time advancing
        //
        callbackManager.releaseHeld();
      }

      WriteFuture writeFuture = rtiSession.write(new TimeAdvanceRequest(time));

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

      timeManager.checkIfTimeRegulationIsNotEnabled();

      messageRetractionManager.retract(messageRetractionHandle, time);

      sendToPeers(new Retract(messageRetractionHandle));
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

      CreateRegion createRegion = new CreateRegion(dimensionHandles);
      WriteFuture writeFuture = rtiSession.write(createRegion);

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      // TODO: set timeout
      //
      Object response = createRegion.getResponse();
      assert response instanceof RegionHandle : "unexpected response";

      RegionHandle regionHandle = (RegionHandle) response;

      // TODO: do region stuff

      return regionHandle;
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

  public void commitRegionModifications(RegionHandleSet regionHandles)
    throws InvalidRegion, RegionNotCreatedByThisFederate, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    federateStateLock.readLock().lock();
    try
    {
      checkIfActive();

      sendToPeers(new CommitRegionModifications(
        regionManager.commitRegionModifications(regionHandles)));
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

      sendToPeers(new DeleteRegion(regionHandle));
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

      for (AttributeRegionAssociation attributeRegionAssociation : attributesAndRegions)
      {
        objectManager.associateRegionsForUpdates(
          objectInstanceHandle, attributeRegionAssociation);
        regionManager.associateRegionsForUpdates(
          objectInstanceHandle, attributeRegionAssociation);
      }
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

      for (AttributeRegionAssociation attributeRegionAssociation : attributesAndRegions)
      {
        objectManager.unassociateRegionsForUpdates(
          objectInstanceHandle, attributeRegionAssociation);
        regionManager.unassociateRegionsForUpdates(
          objectInstanceHandle, attributeRegionAssociation);
      }
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

      sendToPeers(new SubscribeObjectClassAttributes(
        objectClassHandle, attributesAndRegions, passive));
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

      sendToPeers(new UnsubscribeObjectClassAttributes(
        objectClassHandle, attributesAndRegions));
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

      sendToPeers(new ReceiveInteraction(
        interactionClassHandle, parameterValues, tag, OrderType.RECEIVE,
        TransportationType.HLA_RELIABLE, regionHandles));
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
          TIMESTAMP : RECEIVE;

      MessageRetractionHandle messageRetractionHandle = null;
      if (sentOrderType == TIMESTAMP)
      {
        messageRetractionHandle = messageRetractionManager.add(sendTime);
      }

      sendToPeers(new ReceiveInteraction(
        interactionClassHandle, parameterValues, tag, sentOrderType,
        TransportationType.HLA_RELIABLE, sendTime, messageRetractionHandle,
        regionHandles));

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

      sendToPeers(new RequestAttributeValueUpdate(
        objectClassHandle, attributesAndRegions, tag));
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

      return null;
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
    }
    finally
    {
      federateStateLock.readLock().unlock();
    }

    regionManager.getRegionThrowIfNull(regionHandle).setRangeBounds(
      dimensionHandle, rangeBounds);
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

  protected void startPeerAcceptor(String federateType)
    throws RTIinternalError
  {
    if (peerConnectionInfo == null)
    {
      String host = System.getProperties().getProperty(
        String.format(OHLA_FEDERATE_HOST_PROPERTY, federateType));
      String port = System.getProperties().getProperty(
        String.format(OHLA_FEDERATE_PORT_PROPERTY, federateType));

      try
      {
        SocketAcceptor peerAcceptor = new SocketAcceptor();
        peerAcceptor.setReuseAddress(true);

        peerAcceptor.setHandler(peerIoHandler);

        // TODO: selection of codec factory
        //
        ProtocolCodecFactory codec = new ObjectSerializationCodecFactory();

        // handles messages to/from bytes
        //
        peerAcceptor.getFilterChain().addLast(
          "ProtocolCodecFilter", new ProtocolCodecFilter(codec));

        peerAcceptor.getFilterChain().addLast("LoggingFilter", new LoggingFilter());

        // handles request/response pairs
        //
        peerAcceptor.getFilterChain().addLast(
          "RequestResponseFilter", new RequestResponseFilter());

        // tracks peers interests and transforms messages to meet those
        // requirements
        //
        peerAcceptor.getFilterChain().addLast(
          "InterestManagementFilter", new InterestManagementFilter(this));

        peerConnectionInfo =
          new InetSocketAddress(
            host == null ? InetAddress.getLocalHost() : InetAddress.getByName(host),
            port == null ? 0 : Integer.parseInt(port));

        log.info("binding to {}", peerConnectionInfo);

        peerAcceptor.setLocalAddress(peerConnectionInfo);

        peerAcceptor.bind();

        peerConnectionInfo = peerAcceptor.getLocalAddress();

        log.info("bound to {}", peerConnectionInfo);
      }
      catch (NumberFormatException nfe)
      {
        throw new RTIinternalError(String.format(
          "invalid port: %s", port), nfe);
      }
      catch (UnknownHostException uhe)
      {
        throw new RTIinternalError(String.format(
          "unknown host: %s", host), uhe);
      }
      catch (IOException ioe)
      {
        throw new RTIinternalError("unable to bind acceptor to: %s", ioe);
      }
    }
  }

  public void sendToPeers(Message message)
  {
    peersLock.lock();
    try
    {
      for (IoSession peerSession : peerSessions.values())
      {
        peerSession.write(message);
      }
    }
    finally
    {
      peersLock.unlock();
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

  public void checkIfSaveInProgress()
    throws SaveInProgress
  {
    if (federateState == FederateState.SAVE_IN_PROGRESS)
    {
      throw new SaveInProgress();
    }
  }

  public void checkIfRestoreInProgress()
    throws RestoreInProgress
  {
    if (federateState == FederateState.RESTORE_IN_PROGRESS)
    {
      throw new RestoreInProgress();
    }
  }

  public void checkIfActive()
    throws SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (federateState != FederateState.ACTIVE)
    {
      checkIfSaveInProgress();
      checkIfRestoreInProgress();

      throw new RTIinternalError("federate not active");
    }
  }

  public Future<Object> schedule(LogicalTime time, Callable<Object> callable)
  {
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
    implements Comparable
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

    public int compareTo(Object rhs)
    {
      return compareTo((TimestampedFutureTask) rhs);
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
      callbackManager.add(callback);

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

  protected class PeerIoHandler
    extends IoHandlerAdapter
  {
    public void sessionClosed(IoSession session)
      throws Exception
    {
      peersLock.lock();
      try
      {
        throw new RuntimeException();
//        log.debug("removing {}", getPeerFederateHandle(session));
//        peerSessions.remove(getPeerFederateHandle(session));
      }
      finally
      {
        peersLock.unlock();
      }
    }

    public void messageReceived(IoSession session, Object message)
      throws Exception
    {
      FederateHandle peerFederateHandle = getPeerFederateHandle(session);
      if (peerFederateHandle == null)
      {
        assert message instanceof FederateJoined :
          String.format("unexpected message: %s", message);

        FederateJoined federateJoined = (FederateJoined) message;

        session.setAttribute(PEER_FEDERATE_HANDLE,
                             federateJoined.getFederateHandle());
        peersLock.lock();
        try
        {
          peerSessions.put(federateJoined.getFederateHandle(), session);
        }
        finally
        {
          peersLock.unlock();
        }

        objectManager.federateJoined(session);

        session.write(new DefaultResponse(federateJoined.getId()));
      }
      else if (message instanceof ReflectAttributeValues)
      {
        ReflectAttributeValues reflectAttributeValues =
          (ReflectAttributeValues) message;

        OrderType receivedOrderType =
          reflectAttributeValues.getSentOrderType() == TIMESTAMP &&
          timeManager.isTimeConstrained() ? TIMESTAMP : RECEIVE;
        reflectAttributeValues.setReceivedOrderType(receivedOrderType);

        if (receivedOrderType == RECEIVE)
        {
          // receive order callbacks need to be held until released if we are
          // constrained and in the time granted state if asynchronous delivery is
          // disabled
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
      else if (message instanceof ReceiveInteraction)
      {
        ReceiveInteraction receiveInteraction = (ReceiveInteraction) message;

        OrderType receivedOrderType =
          receiveInteraction.getSentOrderType() == TIMESTAMP &&
          timeManager.isTimeConstrained() ? TIMESTAMP : RECEIVE;

        receiveInteraction.setReceivedOrderType(receivedOrderType);

        if (receivedOrderType == RECEIVE)
        {
          // receive order callbacks need to be held until released if we are
          // constrained and in the time granted state, if asynchronous delivery is
          // disabled
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
      else if (message instanceof FederateSaveInitiated)
      {
        FederateSaveInitiated federateSaveInitiated =
          (FederateSaveInitiated) message;
        federateStateLock.writeLock().lock();
        try
        {
          if (federateSave == null)
          {
            // handle the case where the message from the federation execution
            // has not arrived yet
            //
            federateSave = new FederateSave(
              federateHandle, federateType,
              federateSaveInitiated.getParticipants());
          }

          federateSave.federateSaveInitiated(peerFederateHandle);
        }
        finally
        {
          federateStateLock.writeLock().unlock();
        }
      }
      else if (message instanceof FederateSaveInitiatedFailed)
      {
        FederateSaveInitiatedFailed federateSaveInitiatedFailed =
          (FederateSaveInitiatedFailed) message;
        federateStateLock.writeLock().lock();
        try
        {
          if (federateSave == null)
          {
            // handle the case where the message from the federation execution
            // has not arrived yet
            //
            federateSave = new FederateSave(
              federateHandle, federateType,
              federateSaveInitiatedFailed.getParticipants());
          }

          federateSave.federateSaveInitiatedFailed(peerFederateHandle);
        }
        finally
        {
          federateStateLock.writeLock().unlock();
        }
      }
      else
      {
        assert false : String.format("unexpected message: %s", message);
      }
    }

    protected FederateHandle getPeerFederateHandle(IoSession session)
    {
      return (FederateHandle) session.getAttribute(PEER_FEDERATE_HANDLE);
    }
  }

  protected class FederateAmbassadorInterceptor
    extends NullFederateAmbassador
  {
    public void synchronizationPointRegistrationSucceeded(String label)
      throws FederateInternalError
    {
      federateAmbassador.synchronizationPointRegistrationSucceeded(label);
    }

    public void synchronizationPointRegistrationFailed(String label,
                                                       SynchronizationPointFailureReason reason)
      throws FederateInternalError
    {
      federateAmbassador.synchronizationPointRegistrationFailed(label, reason);
    }

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

    public void initiateFederateSave(String label)
      throws UnableToPerformSave, FederateInternalError
    {
      federateStateLock.writeLock().lock();
      try
      {
        federateAmbassador.initiateFederateSave(label);

        FederateSaveInitiated federateSaveInitiated =
          new FederateSaveInitiated(federateSave.getParticipants());

        rtiSession.write(federateSaveInitiated);
        sendToPeers(federateSaveInitiated);
      }
      catch (Throwable t)
      {
        FederateSaveInitiatedFailed federateSaveInitiatedFailed =
          new FederateSaveInitiatedFailed(t, federateSave.getParticipants());

        rtiSession.write(federateSaveInitiatedFailed);
        sendToPeers(federateSaveInitiatedFailed);
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

    public void initiateFederateSave(String label, LogicalTime saveTime)
      throws InvalidLogicalTime, UnableToPerformSave, FederateInternalError
    {
      federateStateLock.writeLock().lock();
      try
      {
        federateAmbassador.initiateFederateSave(label, saveTime);

        FederateSaveInitiated federateSaveInitiated =
          new FederateSaveInitiated(federateSave.getParticipants());

        rtiSession.write(federateSaveInitiated);
        sendToPeers(federateSaveInitiated);
      }
      catch (Throwable t)
      {
        FederateSaveInitiatedFailed federateSaveInitiatedFailed =
          new FederateSaveInitiatedFailed(t, federateSave.getParticipants());

        rtiSession.write(federateSaveInitiatedFailed);
        sendToPeers(federateSaveInitiatedFailed);
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
      federateAmbassador.initiateFederateSave(label, time);
    }

    public void federationSaved()
      throws FederateInternalError
    {
      federateAmbassador.federationSaved();
    }

    public void federationNotSaved(SaveFailureReason reason)
      throws FederateInternalError
    {
      federateAmbassador.federationNotSaved(reason);
    }

    public void federationSaveStatusResponse(
      FederateHandleSaveStatusPair[] response)
      throws FederateInternalError
    {
      federateAmbassador.federationSaveStatusResponse(response);
    }

    public void requestFederationRestoreSucceeded(String label)
      throws FederateInternalError
    {
      federateAmbassador.requestFederationRestoreSucceeded(label);
    }

    public void requestFederationRestoreFailed(String label)
      throws FederateInternalError
    {
      federateAmbassador.requestFederationRestoreFailed(label);
    }

    public void federationRestoreBegun()
      throws FederateInternalError
    {
      federateAmbassador.federationRestoreBegun();
    }

    public void initiateFederateRestore(String label,
                                        FederateHandle federateHandle)
      throws SpecifiedSaveLabelDoesNotExist, CouldNotInitiateRestore,
             FederateInternalError
    {
      federateAmbassador.initiateFederateRestore(label, federateHandle);
    }

    public void federationRestored()
      throws FederateInternalError
    {
      federateAmbassador.federationRestored();
    }

    public void federationNotRestored(RestoreFailureReason reason)
      throws FederateInternalError
    {
      federateAmbassador.federationNotRestored(reason);
    }

    public void federationRestoreStatusResponse(
      FederateHandleRestoreStatusPair[] response)
      throws FederateInternalError
    {
      federateAmbassador.federationRestoreStatusResponse(response);
    }

    public void startRegistrationForObjectClass(
      ObjectClassHandle objectClassHandle)
      throws ObjectClassNotPublished, FederateInternalError
    {
      federateAmbassador.startRegistrationForObjectClass(objectClassHandle);
    }

    public void stopRegistrationForObjectClass(
      ObjectClassHandle objectClassHandle)
      throws ObjectClassNotPublished, FederateInternalError
    {
      federateAmbassador.stopRegistrationForObjectClass(objectClassHandle);
    }

    public void turnInteractionsOn(InteractionClassHandle interactionClassHandle)
      throws InteractionClassNotPublished, FederateInternalError
    {
      federateAmbassador.turnInteractionsOn(interactionClassHandle);
    }

    public void turnInteractionsOff(
      InteractionClassHandle interactionClassHandle)
      throws InteractionClassNotPublished, FederateInternalError
    {
      federateAmbassador.turnInteractionsOff(interactionClassHandle);
    }

    public void objectInstanceNameReservationSucceeded(String name)
      throws UnknownName, FederateInternalError
    {
      federateAmbassador.objectInstanceNameReservationSucceeded(name);
    }

    public void objectInstanceNameReservationFailed(String name)
      throws UnknownName, FederateInternalError
    {
      federateAmbassador.objectInstanceNameReservationFailed(name);
    }

    public void discoverObjectInstance(
      ObjectInstanceHandle objectInstanceHandle,
      ObjectClassHandle objectClassHandle, String name)
      throws CouldNotDiscover, ObjectClassNotRecognized, FederateInternalError
    {
      objectManager.discoverObjectInstance(
        objectInstanceHandle, objectClassHandle, name, federateAmbassador);
    }

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

    public void reflectAttributeValues(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleValueMap attributeValues, byte[] tag, OrderType sentOrderType,
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

    public void reflectAttributeValues(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleValueMap attributeValues, byte[] tag, OrderType sentOrderType,
      TransportationType transportationType, LogicalTime updateTime,
      OrderType receivedOrderType,
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

    public void removeObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                                     byte[] tag, OrderType sentOrderType)
      throws ObjectInstanceNotKnown, FederateInternalError
    {
      objectManager.removeObjectInstance(
        objectInstanceHandle, tag, sentOrderType, null, null, null,
        federateAmbassador);
    }

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

    public void attributesInScope(ObjectInstanceHandle objectInstanceHandle,
                                  AttributeHandleSet attributeHandles)
      throws ObjectInstanceNotKnown, AttributeNotRecognized,
             AttributeNotSubscribed, FederateInternalError
    {
      federateAmbassador.attributesInScope(
        objectInstanceHandle, attributeHandles);
    }

    public void attributesOutOfScope(ObjectInstanceHandle objectInstanceHandle,
                                     AttributeHandleSet attributeHandles)
      throws ObjectInstanceNotKnown, AttributeNotRecognized,
             AttributeNotSubscribed, FederateInternalError
    {
      federateAmbassador.attributesOutOfScope(
        objectInstanceHandle, attributeHandles);
    }

    public void provideAttributeValueUpdate(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleSet attributeHandles, byte[] tag)
      throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
             FederateInternalError
    {
      federateAmbassador.provideAttributeValueUpdate(
        objectInstanceHandle, attributeHandles, tag);
    }

    public void turnUpdatesOnForObjectInstance(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleSet attributeHandles)
      throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
             FederateInternalError
    {
      federateAmbassador.turnUpdatesOnForObjectInstance(
        objectInstanceHandle, attributeHandles);
    }

    public void turnUpdatesOffForObjectInstance(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleSet attributeHandles)
      throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
             FederateInternalError
    {
      federateAmbassador.turnUpdatesOffForObjectInstance(
        objectInstanceHandle, attributeHandles);
    }

    public void requestAttributeOwnershipAssumption(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleSet attributeHandles, byte[] tag)
      throws ObjectInstanceNotKnown, AttributeNotRecognized,
             AttributeAlreadyOwned, AttributeNotPublished, FederateInternalError
    {
      federateAmbassador.requestAttributeOwnershipAssumption(
        objectInstanceHandle, attributeHandles, tag);
    }

    public void requestDivestitureConfirmation(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleSet attributeHandles)
      throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
             AttributeDivestitureWasNotRequested, FederateInternalError
    {
      federateAmbassador.requestDivestitureConfirmation(
        objectInstanceHandle, attributeHandles);
    }

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

    public void requestAttributeOwnershipRelease(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleSet attributeHandles, byte[] tag)
      throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
             FederateInternalError
    {
      federateAmbassador.requestAttributeOwnershipRelease(
        objectInstanceHandle, attributeHandles, tag);
    }

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

    public void informAttributeOwnership(
      ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle,
      FederateHandle federateHandle)
      throws ObjectInstanceNotKnown, AttributeNotRecognized, FederateInternalError
    {
      federateAmbassador.informAttributeOwnership(
        objectInstanceHandle, attributeHandle, federateHandle);
    }

    public void attributeIsNotOwned(ObjectInstanceHandle objectInstanceHandle,
                                    AttributeHandle attributeHandle)
      throws ObjectInstanceNotKnown, AttributeNotRecognized, FederateInternalError
    {
      federateAmbassador.attributeIsNotOwned(
        objectInstanceHandle, attributeHandle);
    }

    public void attributeIsOwnedByRTI(ObjectInstanceHandle objectInstanceHandle,
                                      AttributeHandle attributeHandle)
      throws ObjectInstanceNotKnown, AttributeNotRecognized, FederateInternalError
    {
      federateAmbassador.attributeIsOwnedByRTI(
        objectInstanceHandle, attributeHandle);
    }

    public void timeRegulationEnabled(LogicalTime time)
      throws InvalidLogicalTime, NoRequestToEnableTimeRegulationWasPending,
             FederateInternalError
    {
      federateAmbassador.timeRegulationEnabled(time);
    }

    public void timeConstrainedEnabled(LogicalTime time)
      throws InvalidLogicalTime, NoRequestToEnableTimeConstrainedWasPending,
             FederateInternalError
    {
      federateAmbassador.timeConstrainedEnabled(time);
    }

    public void timeAdvanceGrant(LogicalTime time)
      throws InvalidLogicalTime, JoinedFederateIsNotInTimeAdvancingState,
             FederateInternalError
    {
      federateAmbassador.timeAdvanceGrant(time);
    }

    public void requestRetraction(MessageRetractionHandle messageRetractionHandle)
      throws FederateInternalError
    {
      federateAmbassador.requestRetraction(messageRetractionHandle);
    }
  }
}
