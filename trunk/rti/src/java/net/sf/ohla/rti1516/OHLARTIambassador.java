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

package net.sf.ohla.rti1516;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.net.UnknownHostException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti1516.fdd.FDD;
import net.sf.ohla.rti1516.federate.LocalFederate;
import net.sf.ohla.rti1516.filter.RequestResponseFilter;
import net.sf.ohla.rti1516.messages.CreateFederationExecution;
import net.sf.ohla.rti1516.messages.DestroyFederationExecution;

import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.RuntimeIOException;
import org.apache.mina.common.WriteFuture;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.nio.SocketConnector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hla.rti1516.AsynchronousDeliveryAlreadyDisabled;
import hla.rti1516.AsynchronousDeliveryAlreadyEnabled;
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
import hla.rti1516.AttributeRelevanceAdvisorySwitchIsOff;
import hla.rti1516.AttributeRelevanceAdvisorySwitchIsOn;
import hla.rti1516.AttributeScopeAdvisorySwitchIsOff;
import hla.rti1516.AttributeScopeAdvisorySwitchIsOn;
import hla.rti1516.AttributeSetRegionSetPairList;
import hla.rti1516.AttributeSetRegionSetPairListFactory;
import hla.rti1516.CouldNotOpenFDD;
import hla.rti1516.DeletePrivilegeNotHeld;
import hla.rti1516.DimensionHandle;
import hla.rti1516.DimensionHandleFactory;
import hla.rti1516.DimensionHandleSet;
import hla.rti1516.DimensionHandleSetFactory;
import hla.rti1516.ErrorReadingFDD;
import hla.rti1516.FederateAlreadyExecutionMember;
import hla.rti1516.FederateAmbassador;
import hla.rti1516.FederateHandle;
import hla.rti1516.FederateHandleFactory;
import hla.rti1516.FederateHandleSet;
import hla.rti1516.FederateHandleSetFactory;
import hla.rti1516.FederateHasNotBegunSave;
import hla.rti1516.FederateNotExecutionMember;
import hla.rti1516.FederateOwnsAttributes;
import hla.rti1516.FederateServiceInvocationsAreBeingReportedViaMOM;
import hla.rti1516.FederateUnableToUseTime;
import hla.rti1516.FederatesCurrentlyJoined;
import hla.rti1516.FederationExecutionAlreadyExists;
import hla.rti1516.FederationExecutionDoesNotExist;
import hla.rti1516.IllegalName;
import hla.rti1516.InTimeAdvancingState;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.InteractionClassHandleFactory;
import hla.rti1516.InteractionClassNotDefined;
import hla.rti1516.InteractionClassNotPublished;
import hla.rti1516.InteractionParameterNotDefined;
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
import hla.rti1516.LogicalTime;
import hla.rti1516.LogicalTimeAlreadyPassed;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.MessageCanNoLongerBeRetracted;
import hla.rti1516.MessageRetractionHandle;
import hla.rti1516.MessageRetractionReturn;
import hla.rti1516.MobileFederateServices;
import hla.rti1516.NameNotFound;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectClassHandleFactory;
import hla.rti1516.ObjectClassNotDefined;
import hla.rti1516.ObjectClassNotPublished;
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
import hla.rti1516.RTIambassador;
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
import hla.rti1516.RestoreInProgress;
import hla.rti1516.RestoreNotRequested;
import hla.rti1516.SaveInProgress;
import hla.rti1516.SaveNotInitiated;
import hla.rti1516.ServiceGroup;
import hla.rti1516.SynchronizationPointLabelNotAnnounced;
import hla.rti1516.TimeConstrainedAlreadyEnabled;
import hla.rti1516.TimeConstrainedIsNotEnabled;
import hla.rti1516.TimeQueryReturn;
import hla.rti1516.TimeRegulationAlreadyEnabled;
import hla.rti1516.TimeRegulationIsNotEnabled;
import hla.rti1516.TransportationType;

public class OHLARTIambassador
  implements RTIambassador
{
  private static final Logger log =
    LoggerFactory.getLogger(OHLARTIambassador.class);

  /**
   * The session with the RTI.
   */
  protected IoSession rtiSession;

  /**
   * Handles communication with the RTI.
   */
  protected RTIIoHandler rtiIoHandler = new RTIIoHandler();

  /**
   * Allows concurrent access to all methods, but ensures that join/resignFederationExecution
   * are exclusive to all others.
   */
  protected ReadWriteLock joinResignLock = new ReentrantReadWriteLock(true);

  /**
   * Ensures only one callback is in progress at a time.
   */
  protected Semaphore callbackSemaphore = new Semaphore(1, true);

  protected LocalFederate federate;

  public LocalFederate getJoinedFederate()
  {
    return federate;
  }

  public void createFederationExecution(String name, FDD fdd)
    throws FederationExecutionAlreadyExists, RTIinternalError
  {
    connectToRTI();

    CreateFederationExecution createFederationExecution =
      new CreateFederationExecution(name, fdd);
    WriteFuture writeFuture = rtiSession.write(createFederationExecution);

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
      Object response = createFederationExecution.getResponse();
      if (response instanceof FederationExecutionAlreadyExists)
      {
        throw new FederationExecutionAlreadyExists(
          (FederationExecutionAlreadyExists) response);
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
  }

  public void createFederationExecution(String name, URL fdd)
    throws FederationExecutionAlreadyExists, CouldNotOpenFDD, ErrorReadingFDD,
           RTIinternalError
  {
    createFederationExecution(name, new FDD(fdd));
  }

  public void destroyFederationExecution(String name)
    throws FederatesCurrentlyJoined, FederationExecutionDoesNotExist,
           RTIinternalError
  {
    connectToRTI();

    DestroyFederationExecution destroyFederationExecution =
      new DestroyFederationExecution(name);
    WriteFuture writeFuture = rtiSession.write(destroyFederationExecution);

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
      Object response = destroyFederationExecution.getResponse();
      if (response instanceof FederatesCurrentlyJoined)
      {
        throw new FederatesCurrentlyJoined(
          (FederatesCurrentlyJoined) response);
      }
      else if (response instanceof FederationExecutionDoesNotExist)
      {
        throw new FederationExecutionDoesNotExist(
          (FederationExecutionDoesNotExist) response);
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
  }

  public FederateHandle joinFederationExecution(
    String federateType, String federationName,
    FederateAmbassador federateAmbassador,
    MobileFederateServices mobileFederateServices)
    throws FederateAlreadyExecutionMember, FederationExecutionDoesNotExist,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (mobileFederateServices == null)
    {
      throw new RTIinternalError(
        "MobileFederateServices must be supplied");
    }
    else if (mobileFederateServices.timeFactory == null)
    {
      throw new RTIinternalError(
        "MobileFederateServices.timeFactory must be supplied");
    }
    else if (mobileFederateServices.intervalFactory == null)
    {
      throw new RTIinternalError(
        "MobileFederateServices.intervalFactory must be supplied");
    }

    connectToRTI();

    joinResignLock.writeLock().lock();
    try
    {
      checkIfFederateAlreadyExecutionMember();

      federate =
        new LocalFederate(federateType, federationName, federateAmbassador,
                     mobileFederateServices, rtiSession);

      return federate.getFederateHandle();
    }
    finally
    {
      joinResignLock.writeLock().unlock();
    }
  }

  public void resignFederationExecution(ResignAction resignAction)
    throws OwnershipAcquisitionPending, FederateOwnsAttributes,
           FederateNotExecutionMember, RTIinternalError
  {
    joinResignLock.writeLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.resignFederationExecution(resignAction);

      federate = null;
    }
    finally
    {
      joinResignLock.writeLock().unlock();
    }
  }

  public void registerFederationSynchronizationPoint(String label, byte[] tag)
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.registerFederationSynchronizationPoint(label, tag);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void registerFederationSynchronizationPoint(
    String label, byte[] tag, FederateHandleSet federateHandles)
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.registerFederationSynchronizationPoint(
        label, tag, federateHandles);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void synchronizationPointAchieved(String label)
    throws SynchronizationPointLabelNotAnnounced, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.synchronizationPointAchieved(label);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void requestFederationSave(String label)
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.requestFederationSave(label);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void requestFederationSave(String label, LogicalTime time)
    throws LogicalTimeAlreadyPassed, InvalidLogicalTime,
           FederateUnableToUseTime, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.requestFederationSave(label, time);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void federateSaveBegun()
    throws SaveNotInitiated, FederateNotExecutionMember, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.federateSaveBegun();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void federateSaveComplete()
    throws FederateHasNotBegunSave, FederateNotExecutionMember,
           RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.federateSaveComplete();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void federateSaveNotComplete()
    throws FederateHasNotBegunSave, FederateNotExecutionMember,
           RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.federateSaveNotComplete();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void queryFederationSaveStatus()
    throws FederateNotExecutionMember, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.queryFederationSaveStatus();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void requestFederationRestore(String label)
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.requestFederationRestore(label);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void federateRestoreComplete()
    throws RestoreNotRequested, FederateNotExecutionMember, SaveInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.federateRestoreComplete();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void federateRestoreNotComplete()
    throws RestoreNotRequested, FederateNotExecutionMember, SaveInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.federateRestoreNotComplete();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void queryFederationRestoreStatus()
    throws FederateNotExecutionMember, SaveInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.queryFederationRestoreStatus();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void publishObjectClassAttributes(ObjectClassHandle objectClassHandle,
                                           AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.publishObjectClassAttributes(
        objectClassHandle, attributeHandles);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void unpublishObjectClass(ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined, OwnershipAcquisitionPending,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.unpublishObjectClass(objectClassHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void unpublishObjectClassAttributes(
    ObjectClassHandle objectClassHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined,
           OwnershipAcquisitionPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.unpublishObjectClassAttributes(
        objectClassHandle, attributeHandles);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void publishInteractionClass(
    InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.publishInteractionClass(interactionClassHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void unpublishInteractionClass(
    InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.unpublishInteractionClass(interactionClassHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void subscribeObjectClassAttributes(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.subscribeObjectClassAttributes(
        objectClassHandle, attributeHandles);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void subscribeObjectClassAttributesPassively(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.subscribeObjectClassAttributesPassively(
        objectClassHandle, attributeHandles);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void unsubscribeObjectClass(ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.unsubscribeObjectClass(objectClassHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void unsubscribeObjectClassAttributes(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.unsubscribeObjectClassAttributes(
        objectClassHandle, attributeHandles);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void subscribeInteractionClass(
    InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined,
           FederateServiceInvocationsAreBeingReportedViaMOM,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.subscribeInteractionClass(interactionClassHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void subscribeInteractionClassPassively(
    InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined,
           FederateServiceInvocationsAreBeingReportedViaMOM,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.subscribeInteractionClassPassively(interactionClassHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void unsubscribeInteractionClass(
    InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.unsubscribeInteractionClass(interactionClassHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void reserveObjectInstanceName(String name)
    throws IllegalName, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.reserveObjectInstanceName(name);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public ObjectInstanceHandle registerObjectInstance(
    ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined, ObjectClassNotPublished,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.registerObjectInstance(objectClassHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public ObjectInstanceHandle registerObjectInstance(
    ObjectClassHandle objectClassHandle, String name)
    throws ObjectClassNotDefined, ObjectClassNotPublished,
           ObjectInstanceNameNotReserved, ObjectInstanceNameInUse,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.registerObjectInstance(objectClassHandle, name);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void updateAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                                    AttributeHandleValueMap attributeValues,
                                    byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.updateAttributeValues(
        objectInstanceHandle, attributeValues, tag);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public MessageRetractionReturn updateAttributeValues(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleValueMap attributeValues, byte[] tag,
    LogicalTime updateTime)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           InvalidLogicalTime, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.updateAttributeValues(
        objectInstanceHandle, attributeValues, tag, updateTime);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void sendInteraction(InteractionClassHandle interactionClassHandle,
                              ParameterHandleValueMap parameterValues,
                              byte[] tag)
    throws InteractionClassNotPublished, InteractionClassNotDefined,
           InteractionParameterNotDefined, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.sendInteraction(
        interactionClassHandle, parameterValues, tag);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public MessageRetractionReturn sendInteraction(
    InteractionClassHandle interactionClassHandle,
    ParameterHandleValueMap parameterValues, byte[] tag, LogicalTime sendTime)
    throws InteractionClassNotPublished, InteractionClassNotDefined,
           InteractionParameterNotDefined, InvalidLogicalTime,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.sendInteraction(
        interactionClassHandle, parameterValues, tag, sendTime);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void deleteObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                                   byte[] tag)
    throws ObjectInstanceNotKnown, DeletePrivilegeNotHeld,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.deleteObjectInstance(objectInstanceHandle, tag);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public MessageRetractionReturn deleteObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag,
    LogicalTime deleteTime)
    throws ObjectInstanceNotKnown, DeletePrivilegeNotHeld, InvalidLogicalTime,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.deleteObjectInstance(
        objectInstanceHandle, tag, deleteTime);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void localDeleteObjectInstance(
    ObjectInstanceHandle objectInstanceHandle)
    throws ObjectInstanceNotKnown, FederateOwnsAttributes,
           OwnershipAcquisitionPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.localDeleteObjectInstance(objectInstanceHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void changeAttributeTransportationType(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, TransportationType transportationType)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.changeAttributeTransportationType(
        objectInstanceHandle, attributeHandles, transportationType);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void changeInteractionTransportationType(
    InteractionClassHandle interactionClassHandle,
    TransportationType transportationType)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.changeInteractionTransportationType(
        interactionClassHandle, transportationType);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void requestAttributeValueUpdate(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.requestAttributeValueUpdate(
        objectInstanceHandle, attributeHandles, tag);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void requestAttributeValueUpdate(ObjectClassHandle objectClassHandle,
                                          AttributeHandleSet attributeHandles,
                                          byte[] tag)
    throws ObjectClassNotDefined, AttributeNotDefined,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.requestAttributeValueUpdate(
        objectClassHandle, attributeHandles, tag);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void unconditionalAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.unconditionalAttributeOwnershipDivestiture(
        objectInstanceHandle, attributeHandles);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void negotiatedAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           AttributeAlreadyBeingDivested, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.negotiatedAttributeOwnershipDivestiture(
        objectInstanceHandle, attributeHandles, tag);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void confirmDivestiture(ObjectInstanceHandle objectInstanceHandle,
                                 AttributeHandleSet attributeHandles,
                                 byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           AttributeDivestitureWasNotRequested, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.confirmDivestiture(
        objectInstanceHandle, attributeHandles, tag);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisition(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, ObjectClassNotPublished, AttributeNotDefined,
           AttributeNotPublished, FederateOwnsAttributes,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.attributeOwnershipAcquisition(
        objectInstanceHandle, attributeHandles, tag);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisitionIfAvailable(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, ObjectClassNotPublished, AttributeNotDefined,
           AttributeNotPublished, FederateOwnsAttributes,
           AttributeAlreadyBeingAcquired, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.attributeOwnershipAcquisitionIfAvailable(
        objectInstanceHandle, attributeHandles);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public AttributeHandleSet attributeOwnershipDivestitureIfWanted(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.attributeOwnershipDivestitureIfWanted(
        objectInstanceHandle, attributeHandles);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           AttributeDivestitureWasNotRequested, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.cancelNegotiatedAttributeOwnershipDivestiture(
        objectInstanceHandle, attributeHandles);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void cancelAttributeOwnershipAcquisition(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeAlreadyOwned,
           AttributeAcquisitionWasNotRequested, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.cancelAttributeOwnershipAcquisition(
        objectInstanceHandle, attributeHandles);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void queryAttributeOwnership(ObjectInstanceHandle objectInstanceHandle,
                                      AttributeHandle attributeHandle)
    throws ObjectInstanceNotKnown, AttributeNotDefined,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.queryAttributeOwnership(
        objectInstanceHandle, attributeHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public boolean isAttributeOwnedByFederate(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
    throws ObjectInstanceNotKnown, AttributeNotDefined,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.isAttributeOwnedByFederate(
        objectInstanceHandle, attributeHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void enableTimeRegulation(LogicalTimeInterval lookahead)
    throws TimeRegulationAlreadyEnabled, InvalidLookahead, InTimeAdvancingState,
           RequestForTimeRegulationPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.enableTimeRegulation(lookahead);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void disableTimeRegulation()
    throws TimeRegulationIsNotEnabled, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.disableTimeRegulation();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void enableTimeConstrained()
    throws TimeConstrainedAlreadyEnabled, InTimeAdvancingState,
           RequestForTimeConstrainedPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.enableTimeConstrained();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void disableTimeConstrained()
    throws TimeConstrainedIsNotEnabled, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.disableTimeConstrained();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void timeAdvanceRequest(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.timeAdvanceRequest(time);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void timeAdvanceRequestAvailable(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.timeAdvanceRequestAvailable(time);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void nextMessageRequest(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.nextMessageRequest(time);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void nextMessageRequestAvailable(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.nextMessageRequestAvailable(time);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void flushQueueRequest(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.flushQueueRequest(time);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void enableAsynchronousDelivery()
    throws AsynchronousDeliveryAlreadyEnabled, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.enableAsynchronousDelivery();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void disableAsynchronousDelivery()
    throws AsynchronousDeliveryAlreadyDisabled, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.disableAsynchronousDelivery();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public TimeQueryReturn queryGALT()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.queryGALT();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public LogicalTime queryLogicalTime()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.queryLogicalTime();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public TimeQueryReturn queryLITS()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.queryLITS();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void modifyLookahead(LogicalTimeInterval lookahead)
    throws TimeRegulationIsNotEnabled, InvalidLookahead, InTimeAdvancingState,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.modifyLookahead(lookahead);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public LogicalTimeInterval queryLookahead()
    throws TimeRegulationIsNotEnabled, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.queryLookahead();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void retract(MessageRetractionHandle messageRetractionHandle)
    throws InvalidMessageRetractionHandle, TimeRegulationIsNotEnabled,
           MessageCanNoLongerBeRetracted, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.retract(messageRetractionHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void changeAttributeOrderType(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, OrderType orderType)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.changeAttributeOrderType(
        objectInstanceHandle, attributeHandles, orderType);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void changeInteractionOrderType(
    InteractionClassHandle interactionClassHandle, OrderType orderType)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.changeInteractionOrderType(
        interactionClassHandle, orderType);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public RegionHandle createRegion(DimensionHandleSet dimensionHandles)
    throws InvalidDimensionHandle, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.createRegion(dimensionHandles);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void commitRegionModifications(RegionHandleSet regionHandles)
    throws InvalidRegion, RegionNotCreatedByThisFederate,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.commitRegionModifications(regionHandles);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void deleteRegion(RegionHandle regionHandle)
    throws InvalidRegion, RegionNotCreatedByThisFederate,
           RegionInUseForUpdateOrSubscription, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.deleteRegion(regionHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public ObjectInstanceHandle registerObjectInstanceWithRegions(
    ObjectClassHandle objectClassHandle,
    AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectClassNotDefined, ObjectClassNotPublished, AttributeNotDefined,
           AttributeNotPublished, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.registerObjectInstanceWithRegions(
        objectClassHandle, attributesAndRegions);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public ObjectInstanceHandle registerObjectInstanceWithRegions(
    ObjectClassHandle objectClassHandle,
    AttributeSetRegionSetPairList attributesAndRegions, String name)
    throws ObjectClassNotDefined, ObjectClassNotPublished, AttributeNotDefined,
           AttributeNotPublished, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext,
           ObjectInstanceNameNotReserved, ObjectInstanceNameInUse,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.registerObjectInstanceWithRegions(
        objectClassHandle, attributesAndRegions, name);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void associateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectInstanceNotKnown, AttributeNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.associateRegionsForUpdates(
        objectInstanceHandle, attributesAndRegions);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void unassociateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectInstanceNotKnown, AttributeNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.unassociateRegionsForUpdates(
        objectInstanceHandle, attributesAndRegions);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void subscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle,
    AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.subscribeObjectClassAttributesWithRegions(
        objectClassHandle, attributesAndRegions);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void subscribeObjectClassAttributesPassivelyWithRegions(
    ObjectClassHandle objectClassHandle,
    AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.subscribeObjectClassAttributesPassivelyWithRegions(
        objectClassHandle, attributesAndRegions);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void unsubscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle,
    AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.unsubscribeObjectClassAttributesWithRegions(
        objectClassHandle, attributesAndRegions);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void subscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle,
    RegionHandleSet regionHandles)
    throws InteractionClassNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext,
           FederateServiceInvocationsAreBeingReportedViaMOM,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.subscribeInteractionClassWithRegions(
        interactionClassHandle, regionHandles);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void subscribeInteractionClassPassivelyWithRegions(
    InteractionClassHandle interactionClassHandle,
    RegionHandleSet regionHandles)
    throws InteractionClassNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext,
           FederateServiceInvocationsAreBeingReportedViaMOM,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.subscribeInteractionClassPassivelyWithRegions(
        interactionClassHandle, regionHandles);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void unsubscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle,
    RegionHandleSet regionHandles)
    throws InteractionClassNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.unsubscribeInteractionClassWithRegions(
        interactionClassHandle, regionHandles);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void sendInteractionWithRegions(
    InteractionClassHandle interactionClassHandle,
    ParameterHandleValueMap parameterValues,
    RegionHandleSet regionHandles, byte[] tag)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           InteractionParameterNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.sendInteractionWithRegions(
        interactionClassHandle, parameterValues, regionHandles, tag);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public MessageRetractionReturn sendInteractionWithRegions(
    InteractionClassHandle interactionClassHandle,
    ParameterHandleValueMap parameterValues,
    RegionHandleSet regionHandles, byte[] tag, LogicalTime sendTime)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           InteractionParameterNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext,
           InvalidLogicalTime, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.sendInteractionWithRegions(
        interactionClassHandle, parameterValues, regionHandles, tag, sendTime);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void requestAttributeValueUpdateWithRegions(
    ObjectClassHandle objectClassHandle,
    AttributeSetRegionSetPairList attributesAndRegions, byte[] tag)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.requestAttributeValueUpdateWithRegions(
        objectClassHandle, attributesAndRegions, tag);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public ObjectClassHandle getObjectClassHandle(String name)
    throws NameNotFound, FederateNotExecutionMember, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getObjectClassHandle(name);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public String getObjectClassName(ObjectClassHandle objectClassHandle)
    throws InvalidObjectClassHandle, FederateNotExecutionMember,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getObjectClassName(objectClassHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public AttributeHandle getAttributeHandle(ObjectClassHandle objectClassHandle,
                                            String name)
    throws InvalidObjectClassHandle, NameNotFound, FederateNotExecutionMember,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getAttributeHandle(objectClassHandle, name);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public String getAttributeName(ObjectClassHandle objectClassHandle,
                                 AttributeHandle attributeHandle)
    throws InvalidObjectClassHandle, InvalidAttributeHandle,
           AttributeNotDefined, FederateNotExecutionMember, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getAttributeName(objectClassHandle, attributeHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public InteractionClassHandle getInteractionClassHandle(String name)
    throws NameNotFound, FederateNotExecutionMember, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getInteractionClassHandle(name);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public String getInteractionClassName(
    InteractionClassHandle interactionClassHandle)
    throws InvalidInteractionClassHandle, FederateNotExecutionMember,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getInteractionClassName(interactionClassHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public ParameterHandle getParameterHandle(
    InteractionClassHandle interactionClassHandle, String name)
    throws InvalidInteractionClassHandle, NameNotFound,
           FederateNotExecutionMember, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getParameterHandle(interactionClassHandle, name);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public String getParameterName(InteractionClassHandle interactionClassHandle,
                                 ParameterHandle parameterHandle)
    throws InvalidInteractionClassHandle, InvalidParameterHandle,
           InteractionParameterNotDefined, FederateNotExecutionMember,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getParameterName(interactionClassHandle, parameterHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public ObjectInstanceHandle getObjectInstanceHandle(String name)
    throws ObjectInstanceNotKnown, FederateNotExecutionMember, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getObjectInstanceHandle(name);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public String getObjectInstanceName(ObjectInstanceHandle objectInstanceHandle)
    throws ObjectInstanceNotKnown, FederateNotExecutionMember, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getObjectInstanceName(objectInstanceHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public DimensionHandle getDimensionHandle(String name)
    throws NameNotFound, FederateNotExecutionMember, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getDimensionHandle(name);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public String getDimensionName(DimensionHandle dimensionHandle)
    throws InvalidDimensionHandle, FederateNotExecutionMember, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getDimensionName(dimensionHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public long getDimensionUpperBound(DimensionHandle dimensionHandle)
    throws InvalidDimensionHandle, FederateNotExecutionMember, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getDimensionUpperBound(dimensionHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public DimensionHandleSet getAvailableDimensionsForClassAttribute(
    ObjectClassHandle objectClassHandle, AttributeHandle attributeHandle)
    throws InvalidObjectClassHandle, InvalidAttributeHandle,
           AttributeNotDefined, FederateNotExecutionMember, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getAvailableDimensionsForClassAttribute(
        objectClassHandle, attributeHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public ObjectClassHandle getKnownObjectClassHandle(
    ObjectInstanceHandle objectInstanceHandle)
    throws ObjectInstanceNotKnown, FederateNotExecutionMember, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getKnownObjectClassHandle(objectInstanceHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public DimensionHandleSet getAvailableDimensionsForInteractionClass(
    InteractionClassHandle interactionClassHandle)
    throws InvalidInteractionClassHandle, FederateNotExecutionMember,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getAvailableDimensionsForInteractionClass(
        interactionClassHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public TransportationType getTransportationType(String name)
    throws InvalidTransportationName, FederateNotExecutionMember,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getTransportationType(name);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public String getTransportationName(TransportationType transportationType)
    throws InvalidTransportationType, FederateNotExecutionMember,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getTransportationName(transportationType);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public OrderType getOrderType(String name)
    throws InvalidOrderName, FederateNotExecutionMember, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getOrderType(name);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public String getOrderName(OrderType orderType)
    throws InvalidOrderType, FederateNotExecutionMember, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getOrderName(orderType);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void enableObjectClassRelevanceAdvisorySwitch()
    throws FederateNotExecutionMember, ObjectClassRelevanceAdvisorySwitchIsOn,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.enableObjectClassRelevanceAdvisorySwitch();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void disableObjectClassRelevanceAdvisorySwitch()
    throws ObjectClassRelevanceAdvisorySwitchIsOff, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.disableObjectClassRelevanceAdvisorySwitch();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void enableAttributeRelevanceAdvisorySwitch()
    throws AttributeRelevanceAdvisorySwitchIsOn, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.enableAttributeRelevanceAdvisorySwitch();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void disableAttributeRelevanceAdvisorySwitch()
    throws AttributeRelevanceAdvisorySwitchIsOff, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.disableAttributeRelevanceAdvisorySwitch();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void enableAttributeScopeAdvisorySwitch()
    throws AttributeScopeAdvisorySwitchIsOn, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.enableAttributeScopeAdvisorySwitch();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void disableAttributeScopeAdvisorySwitch()
    throws AttributeScopeAdvisorySwitchIsOff, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.disableAttributeScopeAdvisorySwitch();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void enableInteractionRelevanceAdvisorySwitch()
    throws InteractionRelevanceAdvisorySwitchIsOn, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.enableInteractionRelevanceAdvisorySwitch();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void disableInteractionRelevanceAdvisorySwitch()
    throws InteractionRelevanceAdvisorySwitchIsOff, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.disableInteractionRelevanceAdvisorySwitch();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public DimensionHandleSet getDimensionHandleSet(RegionHandle regionHandle)
    throws InvalidRegion, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getDimensionHandleSet(regionHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public RangeBounds getRangeBounds(RegionHandle regionHandle,
                                    DimensionHandle dimensionHandle)
    throws InvalidRegion, RegionDoesNotContainSpecifiedDimension,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getRangeBounds(regionHandle, dimensionHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void setRangeBounds(RegionHandle regionHandle,
                             DimensionHandle dimensionHandle,
                             RangeBounds rangeBounds)
    throws InvalidRegion, RegionNotCreatedByThisFederate,
           RegionDoesNotContainSpecifiedDimension, InvalidRangeBound,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.setRangeBounds(regionHandle, dimensionHandle, rangeBounds);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public long normalizeFederateHandle(FederateHandle federateHandle)
    throws InvalidFederateHandle, FederateNotExecutionMember, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.normalizeFederateHandle(federateHandle);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public long normalizeServiceGroup(ServiceGroup serviceGroup)
    throws FederateNotExecutionMember, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.normalizeServiceGroup(serviceGroup);
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public boolean evokeCallback(double seconds)
    throws FederateNotExecutionMember, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      // ensure only one callback is in progress
      //
      if (!callbackSemaphore.tryAcquire())
      {
        throw new RTIinternalError("concurrent access attempted");
      }

      try
      {
        checkIfFederateNotExecutionMember();

        return federate.evokeCallback(seconds);
      }
      finally
      {
        callbackSemaphore.release();
      }
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public boolean evokeMultipleCallbacks(double minimumTime, double maximumTime)
    throws FederateNotExecutionMember, RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      // ensure only one callback is in progress
      //
      if (!callbackSemaphore.tryAcquire())
      {
        throw new RTIinternalError("concurrent access attempted");
      }

      try
      {
        checkIfFederateNotExecutionMember();

        return federate.evokeMultipleCallbacks(minimumTime, maximumTime);
      }
      finally
      {
        callbackSemaphore.release();
      }
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void enableCallbacks()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.enableCallbacks();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public void disableCallbacks()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      federate.disableCallbacks();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public AttributeHandleFactory getAttributeHandleFactory()
    throws FederateNotExecutionMember
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getAttributeHandleFactory();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public AttributeHandleSetFactory getAttributeHandleSetFactory()
    throws FederateNotExecutionMember
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getAttributeHandleSetFactory();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public AttributeHandleValueMapFactory getAttributeHandleValueMapFactory()
    throws FederateNotExecutionMember
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getAttributeHandleValueMapFactory();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public AttributeSetRegionSetPairListFactory
    getAttributeSetRegionSetPairListFactory()
    throws FederateNotExecutionMember
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getAttributeSetRegionSetPairListFactory();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public DimensionHandleFactory getDimensionHandleFactory()
    throws FederateNotExecutionMember
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getDimensionHandleFactory();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public DimensionHandleSetFactory getDimensionHandleSetFactory()
    throws FederateNotExecutionMember
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getDimensionHandleSetFactory();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public FederateHandleFactory getFederateHandleFactory()
    throws FederateNotExecutionMember
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getFederateHandleFactory();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public FederateHandleSetFactory getFederateHandleSetFactory()
    throws FederateNotExecutionMember
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getFederateHandleSetFactory();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public InteractionClassHandleFactory getInteractionClassHandleFactory()
    throws FederateNotExecutionMember
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getInteractionClassHandleFactory();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public ObjectClassHandleFactory getObjectClassHandleFactory()
    throws FederateNotExecutionMember
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getObjectClassHandleFactory();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public ObjectInstanceHandleFactory getObjectInstanceHandleFactory()
    throws FederateNotExecutionMember
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getObjectInstanceHandleFactory();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public ParameterHandleFactory getParameterHandleFactory()
    throws FederateNotExecutionMember
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getParameterHandleFactory();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public ParameterHandleValueMapFactory getParameterHandleValueMapFactory()
    throws FederateNotExecutionMember
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getParameterHandleValueMapFactory();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public RegionHandleSetFactory getRegionHandleSetFactory()
    throws FederateNotExecutionMember
  {
    joinResignLock.readLock().lock();
    try
    {
      checkIfFederateNotExecutionMember();

      return federate.getRegionHandleSetFactory();
    }
    finally
    {
      joinResignLock.readLock().unlock();
    }
  }

  public String getHLAversion()
  {
    return "1516.1.5";
  }

  protected void checkIfFederateAlreadyExecutionMember()
    throws FederateAlreadyExecutionMember
  {
    if (federate != null)
    {
      throw new FederateAlreadyExecutionMember(
        federate.getFederateHandle().toString());
    }
  }

  protected void checkIfFederateNotExecutionMember()
    throws FederateNotExecutionMember
  {
    if (federate == null)
    {
      throw new FederateNotExecutionMember();
    }
  }

  protected synchronized void connectToRTI()
    throws RTIinternalError
  {
    if (rtiSession == null || !rtiSession.isConnected())
    {
      String host =
        System.getProperties().getProperty("ohla.rti.host");
      String port =
        System.getProperties().getProperty("ohla.rti.port");

      try
      {
        SocketConnector connector = new SocketConnector();

        // set socket options
        //
        connector.setConnectTimeout(30);

        connector.setHandler(rtiIoHandler);

        // TODO: selection of codec factory
        //
        ProtocolCodecFactory codec = new ObjectSerializationCodecFactory();

        // handles messages to/from bytes
        //
        connector.getFilterChain().addLast(
          "ProtocolCodecFilter", new ProtocolCodecFilter(codec));

        connector.getFilterChain().addLast("LoggingFilter", new LoggingFilter());

        // handles request/response pairs
        //
        connector.getFilterChain().addLast(
          "RequestResponseFilter", new RequestResponseFilter());

        SocketAddress rtiConnectionInfo =
          new InetSocketAddress(
            host == null ? null : InetAddress.getByName(host),
            port == null ? 0 : Integer.parseInt(port));

        log.debug("connecting to rti: {}", rtiConnectionInfo);

        // TODO: selection of local address to connect to rti?
        //
        ConnectFuture future = connector.connect(rtiConnectionInfo);
        future.join();

        try
        {
          rtiSession = future.getSession();
        }
        catch (RuntimeIOException rioe)
        {
          throw new RTIinternalError(
            String.format("unable to connect to RTI: %s", rtiConnectionInfo),
            rioe);
        }
      }
      catch (NumberFormatException nfe)
      {
        throw new RTIinternalError(String.format("invalid port: %s", port),
                                   nfe);
      }
      catch (UnknownHostException uhe)
      {
        throw new RTIinternalError(String.format("unknown host: %s", host),
                                   uhe);
      }
      catch (IOException ioe)
      {
        throw new RTIinternalError("unable to bind acceptor to: %s", ioe);
      }
    }
  }

  protected class RTIIoHandler
    extends IoHandlerAdapter
  {
    public void messageReceived(IoSession session, Object message)
      throws Exception
    {
      joinResignLock.readLock().lock();
      try
      {
        if (federate == null)
        {
          log.debug("discarding message (no longer joined): {}", message);
        }
        else if (!federate.process(session, message))
        {
          assert false : String.format("unexpected message: %s", message);
        }
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
  }
}
