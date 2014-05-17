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

import java.nio.file.Path;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.fdd.FDD;
import net.sf.ohla.rti.fdd.InteractionClass;
import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleSet;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandleSet;
import net.sf.ohla.rti.i18n.I18nLogger;
import net.sf.ohla.rti.i18n.LogMessages;
import net.sf.ohla.rti.messages.AbortFederationRestore;
import net.sf.ohla.rti.messages.AbortFederationRestoreResponse;
import net.sf.ohla.rti.messages.AbortFederationSave;
import net.sf.ohla.rti.messages.AssociateRegionsForUpdates;
import net.sf.ohla.rti.messages.AssociateRegionsForUpdatesResponse;
import net.sf.ohla.rti.messages.AttributeOwnershipAcquisition;
import net.sf.ohla.rti.messages.AttributeOwnershipAcquisitionIfAvailable;
import net.sf.ohla.rti.messages.AttributeOwnershipDivestitureIfWanted;
import net.sf.ohla.rti.messages.AttributeOwnershipDivestitureIfWantedResponse;
import net.sf.ohla.rti.messages.CancelAttributeOwnershipAcquisition;
import net.sf.ohla.rti.messages.CancelNegotiatedAttributeOwnershipDivestiture;
import net.sf.ohla.rti.messages.CommitRegionModifications;
import net.sf.ohla.rti.messages.ConfirmDivestiture;
import net.sf.ohla.rti.messages.CreateRegion;
import net.sf.ohla.rti.messages.DeleteObjectInstance;
import net.sf.ohla.rti.messages.DeleteRegion;
import net.sf.ohla.rti.messages.DestroyFederationExecution;
import net.sf.ohla.rti.messages.DestroyFederationExecutionResponse;
import net.sf.ohla.rti.messages.DisableTimeConstrained;
import net.sf.ohla.rti.messages.DisableTimeRegulation;
import net.sf.ohla.rti.messages.EnableTimeConstrained;
import net.sf.ohla.rti.messages.EnableTimeRegulation;
import net.sf.ohla.rti.messages.FDDUpdated;
import net.sf.ohla.rti.messages.FederateRestoreComplete;
import net.sf.ohla.rti.messages.FederateRestoreNotComplete;
import net.sf.ohla.rti.messages.FederateSaveBegun;
import net.sf.ohla.rti.messages.FederateSaveComplete;
import net.sf.ohla.rti.messages.FederateSaveNotComplete;
import net.sf.ohla.rti.messages.FederateStateFrame;
import net.sf.ohla.rti.messages.FederationExecutionMessage;
import net.sf.ohla.rti.messages.FlushQueueRequest;
import net.sf.ohla.rti.messages.GetFederateHandle;
import net.sf.ohla.rti.messages.GetFederateHandleResponse;
import net.sf.ohla.rti.messages.GetFederateName;
import net.sf.ohla.rti.messages.GetFederateNameResponse;
import net.sf.ohla.rti.messages.GetUpdateRateValueForAttribute;
import net.sf.ohla.rti.messages.JoinFederationExecution;
import net.sf.ohla.rti.messages.JoinFederationExecutionResponse;
import net.sf.ohla.rti.messages.LocalDeleteObjectInstance;
import net.sf.ohla.rti.messages.ModifyLookahead;
import net.sf.ohla.rti.messages.NegotiatedAttributeOwnershipDivestiture;
import net.sf.ohla.rti.messages.NextMessageRequest;
import net.sf.ohla.rti.messages.NextMessageRequestAvailable;
import net.sf.ohla.rti.messages.PublishInteractionClass;
import net.sf.ohla.rti.messages.PublishObjectClassAttributes;
import net.sf.ohla.rti.messages.QueryAttributeOwnership;
import net.sf.ohla.rti.messages.QueryFederationRestoreStatus;
import net.sf.ohla.rti.messages.QueryFederationSaveStatus;
import net.sf.ohla.rti.messages.QueryGALT;
import net.sf.ohla.rti.messages.QueryInteractionTransportationType;
import net.sf.ohla.rti.messages.QueryLITS;
import net.sf.ohla.rti.messages.RegisterFederationSynchronizationPoint;
import net.sf.ohla.rti.messages.RegisterObjectInstance;
import net.sf.ohla.rti.messages.ReleaseMultipleObjectInstanceName;
import net.sf.ohla.rti.messages.ReleaseObjectInstanceName;
import net.sf.ohla.rti.messages.RequestFederationRestore;
import net.sf.ohla.rti.messages.RequestFederationRestoreResponse;
import net.sf.ohla.rti.messages.RequestFederationSave;
import net.sf.ohla.rti.messages.RequestFederationSaveResponse;
import net.sf.ohla.rti.messages.RequestObjectClassAttributeValueUpdate;
import net.sf.ohla.rti.messages.RequestObjectClassAttributeValueUpdateWithRegions;
import net.sf.ohla.rti.messages.RequestObjectInstanceAttributeValueUpdate;
import net.sf.ohla.rti.messages.ReserveMultipleObjectInstanceName;
import net.sf.ohla.rti.messages.ReserveObjectInstanceName;
import net.sf.ohla.rti.messages.ResignFederationExecution;
import net.sf.ohla.rti.messages.Retract;
import net.sf.ohla.rti.messages.SendInteraction;
import net.sf.ohla.rti.messages.SetAutomaticResignDirective;
import net.sf.ohla.rti.messages.SubscribeInteractionClass;
import net.sf.ohla.rti.messages.SubscribeInteractionClassWithRegions;
import net.sf.ohla.rti.messages.SubscribeObjectClassAttributes;
import net.sf.ohla.rti.messages.SubscribeObjectClassAttributesWithRegions;
import net.sf.ohla.rti.messages.SynchronizationPointAchieved;
import net.sf.ohla.rti.messages.TimeAdvanceRequest;
import net.sf.ohla.rti.messages.TimeAdvanceRequestAvailable;
import net.sf.ohla.rti.messages.UnassociateRegionsForUpdates;
import net.sf.ohla.rti.messages.UnassociateRegionsForUpdatesResponse;
import net.sf.ohla.rti.messages.UnconditionalAttributeOwnershipDivestiture;
import net.sf.ohla.rti.messages.UnpublishInteractionClass;
import net.sf.ohla.rti.messages.UnpublishObjectClass;
import net.sf.ohla.rti.messages.UnpublishObjectClassAttributes;
import net.sf.ohla.rti.messages.UnsubscribeInteractionClass;
import net.sf.ohla.rti.messages.UnsubscribeInteractionClassWithRegions;
import net.sf.ohla.rti.messages.UnsubscribeObjectClassAttributes;
import net.sf.ohla.rti.messages.UnsubscribeObjectClassAttributesWithRegions;
import net.sf.ohla.rti.messages.UpdateAttributeValues;
import net.sf.ohla.rti.messages.callbacks.AnnounceSynchronizationPoint;
import net.sf.ohla.rti.messages.callbacks.AttributeOwnershipAcquisitionNotification;
import net.sf.ohla.rti.messages.callbacks.FederationNotSaved;
import net.sf.ohla.rti.messages.callbacks.FederationRestoreStatusResponse;
import net.sf.ohla.rti.messages.callbacks.FederationSaveStatusResponse;
import net.sf.ohla.rti.messages.callbacks.FederationSynchronized;
import net.sf.ohla.rti.messages.callbacks.ReportInteractionTransportationType;
import net.sf.ohla.rti.messages.callbacks.RequestFederationRestoreSucceeded;
import net.sf.ohla.rti.messages.callbacks.SynchronizationPointRegistrationFailed;
import net.sf.ohla.rti.messages.callbacks.SynchronizationPointRegistrationSucceeded;
import net.sf.ohla.rti.messages.proto.FederateMessageProtos;
import net.sf.ohla.rti.proto.FederationExecutionSaveProtos;
import net.sf.ohla.rti.proto.FederationExecutionSaveProtos.FederationExecutionState.FederationExecutionSynchonizationPointState;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateHandleSaveStatusPair;
import hla.rti1516e.FederateHandleSet;
import hla.rti1516e.FederateRestoreStatus;
import hla.rti1516e.FederationExecutionInformation;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.OrderType;
import hla.rti1516e.RestoreStatus;
import hla.rti1516e.SaveStatus;
import hla.rti1516e.SynchronizationPointFailureReason;
import hla.rti1516e.exceptions.InconsistentFDD;

public class FederationExecution
{
  private final String name;

  private volatile FDD fdd;

  private final ReentrantReadWriteLock federationExecutionStateLock = new ReentrantReadWriteLock(true);
  private FederationExecutionState federationExecutionState = FederationExecutionState.ACTIVE;

  private final Path saveDirectory;

  private FederationExecutionSave federationExecutionSave;
  private FederationExecutionRestore federationExecutionRestore;

  private final Map<FederateHandle, FederateProxy> federates = new HashMap<>();
  private final Map<String, FederateProxy> federatesByName = new HashMap<>();

  private final Map<FederateHandle, ResignedFederate> resignedFederates = new HashMap<>();

  private final Map<String, FederationExecutionSynchronizationPoint> synchronizationPoints = new HashMap<>();

  private final FederationExecutionObjectManager objectManager =
    new FederationExecutionObjectManager(this);

  private final FederationExecutionRegionManager regionManager =
    new FederationExecutionRegionManager(this);

  private final FederationExecutionTimeManager timeManager;

  private final TimeStampOrderedMessageQueue timeStampOrderedMessageQueue = new TimeStampOrderedMessageQueue(this);

  private final Marker marker;
  private final I18nLogger log;

  private int nextFederateHandle;

  public FederationExecution(String name, FDD fdd, LogicalTimeFactory logicalTimeFactory, Path savesDirectory)
  {
    this.name = name;
    this.fdd = fdd;

    saveDirectory = savesDirectory.resolve(name);

    timeManager = new FederationExecutionTimeManager(this, logicalTimeFactory);

    marker = MarkerFactory.getMarker(name);
    log = I18nLogger.getLogger(marker, FederationExecution.class);

    log.debug(LogMessages.FEDERATION_EXECUTION_CREATED);
  }

  public String getName()
  {
    return name;
  }

  public FDD getFDD()
  {
    return fdd;
  }

  public Marker getMarker()
  {
    return marker;
  }

  public Path getSaveDirectory()
  {
    return saveDirectory;
  }

  /**
   * Returns the current {@link FederateProxy} joined to this {@code FederationExecution} with the specified
   * {@link FederateHandle}.
   * <p/><b>WARNING:</b> Use of this method is dangerous and can only be safely accessed when
   * {@link FederationExecution#federationExecutionStateLock} is held.
   *
   * @param federateHandle the
   * @return the current {@link FederateProxy}s joined to this {@code FederationExecution}
   */
  public FederateProxy getFederate(FederateHandle federateHandle)
  {
    FederateProxy federateProxy = federates.get(federateHandle);

    assert federateProxy != null;

    return federateProxy;
  }

  /**
   * Returns the current {@link FederateProxy}s joined to this {@code FederationExecution}.
   * <p/><b>WARNING:</b> Use of this method is dangerous and can only be safely accessed when
   * {@link FederationExecution#federationExecutionStateLock} is held.
   *
   * @return the current {@link FederateProxy}s joined to this {@code FederationExecution}
   */
  public Map<FederateHandle, FederateProxy> getFederates()
  {
    return federates;
  }

  public Map<String, FederateProxy> getFederatesByName()
  {
    return federatesByName;
  }

  public FederationExecutionObjectManager getObjectManager()
  {
    return objectManager;
  }

  public FederationExecutionRegionManager getRegionManager()
  {
    return regionManager;
  }

  public FederationExecutionTimeManager getTimeManager()
  {
    return timeManager;
  }

  public LogicalTimeFactory getLogicalTimeFactory()
  {
    return timeManager.getLogicalTimeFactory();
  }

  public FederationExecutionInformation getFederationExecutionInformation()
  {
    return new FederationExecutionInformation(name, timeManager.getLogicalTimeFactory().getName());
  }

  public DestroyFederationExecutionResponse destroy(DestroyFederationExecution destroyFederationExecution)
  {
    DestroyFederationExecutionResponse response;

    federationExecutionStateLock.writeLock().lock();
    try
    {
      if (federates.isEmpty())
      {
        log.debug(LogMessages.FEDERATION_EXECUTION_DESTROYED);

        response = new DestroyFederationExecutionResponse(destroyFederationExecution.getRequestId());
      }
      else
      {
        Set<String> currentlyJoinedFederates = new HashSet<>();
        for (FederateProxy federate : federates.values())
        {
          currentlyJoinedFederates.add(federate.getFederateName());
        }

        log.debug(LogMessages.DESTROY_FEDERATION_EXECUTION_FAILED_FEDERATES_CURRENTLY_JOINED, currentlyJoinedFederates);

        response = new DestroyFederationExecutionResponse(
          destroyFederationExecution.getRequestId(), currentlyJoinedFederates);
      }
    }
    finally
    {
      federationExecutionStateLock.writeLock().unlock();
    }

    return response;
  }

  public void joinFederationExecution(ChannelHandlerContext context, JoinFederationExecution joinFederationExecution)
  {
    String federateName = joinFederationExecution.getFederateName();
    String federateType = joinFederationExecution.getFederateType();

    federationExecutionStateLock.writeLock().lock();
    try
    {
      if (saveInProgress())
      {
        log.debug(LogMessages.JOIN_FEDERATION_EXECUTION_FAILED_SAVE_IN_PROGRESS, federateName, federateType);

        context.getChannel().write(new JoinFederationExecutionResponse(
          FederateMessageProtos.JoinFederationExecutionResponse.Failure.Cause.SAVE_IN_PROGRESS));
      }
      else if (restoreInProgress())
      {
        log.debug(LogMessages.JOIN_FEDERATION_EXECUTION_FAILED_RESTORE_IN_PROGRESS, federateName, federateType);

        context.getChannel().write(new JoinFederationExecutionResponse(
          FederateMessageProtos.JoinFederationExecutionResponse.Failure.Cause.RESTORE_IN_PROGRESS));
      }
      else if (federateName != null && federatesByName.containsKey(federateName))
      {
        log.debug(LogMessages.JOIN_FEDERATION_EXECUTION_FAILED_RESTORE_IN_PROGRESS, federateName, federateType);

        context.getChannel().write(new JoinFederationExecutionResponse(
          FederateMessageProtos.JoinFederationExecutionResponse.Failure.Cause.FEDERATE_NAME_ALREADY_IN_USE));
      }
      else
      {
        try
        {
          if (joinFederationExecution.getAdditionalFDDs().size() > 0)
          {
            fdd = fdd.merge(joinFederationExecution.getAdditionalFDDs());

            // notify existing federates that the FDD has been updated
            //
            for (FederateProxy federateProxy : federates.values())
            {
              federateProxy.getFederateChannel().write(new FDDUpdated(fdd));
            }
          }

          // get the next federate handle
          //
          IEEE1516eFederateHandle federateHandle = new IEEE1516eFederateHandle(++nextFederateHandle);

          if (federateName == null || federateName.isEmpty())
          {
            federateName = "HLA-Federate-" + federateHandle.toString();
          }

          FederateProxy federateProxy = new FederateProxy(
            this, federateHandle, federateName, federateType, context.getChannel(), timeManager.getGALT());

          context.setAttachment(federateProxy);

          federates.put(federateHandle, federateProxy);
          federatesByName.put(federateName, federateProxy);

          context.getChannel().write(new JoinFederationExecutionResponse(
            federateName, federateHandle, fdd, timeManager.getLogicalTimeFactory().getName()));

          for (FederationExecutionSynchronizationPoint synchronizationPoint : synchronizationPoints.values())
          {
            if (!synchronizationPoint.isExclusive() && synchronizationPoint.getAwaitingSynchronization().size() > 0)
            {
              // add the new Federate to any non-exclusive synchronization points that are not completed

              synchronizationPoint.add(federateHandle);

              federateProxy.announceSynchronizationPoint(
                new AnnounceSynchronizationPoint(synchronizationPoint.getLabel(), synchronizationPoint.getTag()));
            }
          }
        }
        catch (InconsistentFDD iFDD)
        {
          log.warn(LogMessages.JOIN_FEDERATION_EXECUTION_FAILED_INCONSISTENT_FDD, iFDD, federateName);

          // TODO: response needs a message about the failure

          context.getChannel().write(new JoinFederationExecutionResponse(
            FederateMessageProtos.JoinFederationExecutionResponse.Failure.Cause.INCONSISTENT_FDD));
        }
      }
    }
    finally
    {
      federationExecutionStateLock.writeLock().unlock();
    }
  }

  public void resignFederationExecution(
    FederateProxy federateProxy, ResignFederationExecution resignFederationExecution)
  {
    federationExecutionStateLock.writeLock().lock();
    try
    {
      if (saveInProgress())
      {
        // TODO: cancel save
      }
      else if (restoreInProgress())
      {
        // TODO: cancel restore
      }

      federates.remove(federateProxy.getFederateHandle());
      federatesByName.remove(federateProxy.getFederateName());

      resignedFederates.put(federateProxy.getFederateHandle(), new ResignedFederate(federateProxy));

      objectManager.resignFederationExecution(federateProxy, resignFederationExecution);

      federateProxy.resignFederationExecution(resignFederationExecution.getResignAction());
    }
    finally
    {
      federationExecutionStateLock.writeLock().unlock();
    }
  }

  public void registerFederationSynchronizationPoint(
    FederateProxy federateProxy, RegisterFederationSynchronizationPoint registerFederationSynchronizationPoint)
  {
    federationExecutionStateLock.writeLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), registerFederationSynchronizationPoint);
      }
      else
      {
        String label = registerFederationSynchronizationPoint.getLabel();

        FederationExecutionSynchronizationPoint federationExecutionSynchronizationPoint =
          synchronizationPoints.get(label);
        if (federationExecutionSynchronizationPoint != null)
        {
          log.debug(LogMessages.REGISTER_FEDERATION_SYNCHRONIZATION_POINT_LABEL_NOT_UNIQUE, label);

          federateProxy.getFederateChannel().write(new SynchronizationPointRegistrationFailed(
            label, SynchronizationPointFailureReason.SYNCHRONIZATION_POINT_LABEL_NOT_UNIQUE));
        }
        else
        {
          FederateHandleSet federateHandles = registerFederationSynchronizationPoint.getFederateHandles();

          boolean exclusive;
          if (federateHandles == null || federateHandles.isEmpty())
          {
            // assign the currently joined federates
            //
            federateHandles = new IEEE1516eFederateHandleSet(federates.keySet());

            exclusive = false;
          }
          else
          {
            exclusive = true;
          }

          // verify all the federates in the set are joined
          //
          if (exclusive && !federates.keySet().containsAll(federateHandles))
          {
            log.debug(LogMessages.REGISTER_FEDERATION_SYNCHRONIZATION_POINT_SYNCHRONIZATION_SET_MEMBER_NOT_JOINED,
                      label, federateHandles, federates.keySet());

            federateProxy.getFederateChannel().write(new SynchronizationPointRegistrationFailed(
              label, SynchronizationPointFailureReason.SYNCHRONIZATION_SET_MEMBER_NOT_JOINED));
          }
          else
          {
            byte[] tag = registerFederationSynchronizationPoint.getTag();

            synchronizationPoints.put(label, new FederationExecutionSynchronizationPoint(
              label, tag, federateHandles, exclusive));

            federateProxy.getFederateChannel().write(new SynchronizationPointRegistrationSucceeded(label));

            AnnounceSynchronizationPoint announceSynchronizationPoint = new AnnounceSynchronizationPoint(label, tag);
            for (FederateHandle federateHandle : federateHandles)
            {
              federates.get(federateHandle).announceSynchronizationPoint(announceSynchronizationPoint);
            }
          }
        }
      }
    }
    finally
    {
      federationExecutionStateLock.writeLock().unlock();
    }
  }

  public void synchronizationPointAchieved(
    FederateProxy federateProxy, SynchronizationPointAchieved synchronizationPointAchieved)
  {
    String label = synchronizationPointAchieved.getLabel();
    boolean success = synchronizationPointAchieved.wasSuccessful();

    federationExecutionStateLock.writeLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), synchronizationPointAchieved);
      }
      else
      {
        FederationExecutionSynchronizationPoint federationExecutionSynchronizationPoint =
          synchronizationPoints.get(label);

        if (federationExecutionSynchronizationPoint.synchronizationPointAchieved(
          federateProxy.getFederateHandle(), success))
        {
          FederationSynchronized federationSynchronized = new FederationSynchronized(
            label, federationExecutionSynchronizationPoint.getFailedToSynchronize());
          for (FederateHandle federateHandle : federationExecutionSynchronizationPoint.getFederateHandles())
          {
            FederateProxy synchronizedFederateProxy = federates.get(federateHandle);
            if (synchronizedFederateProxy == null)
            {
              log.warn(LogMessages.FEDERATE_UNABLE_TO_COMPLETE_SYNCHRONIZATION_FEDERATE_RESIGNED,
                       resignedFederates.get(federateHandle));
            }
            else
            {
              synchronizedFederateProxy.getFederateChannel().write(federationSynchronized);
            }
          }
        }
      }
    }
    finally
    {
      federationExecutionStateLock.writeLock().unlock();
    }
  }

  @SuppressWarnings("unchecked")
  public void requestFederationSave(FederateProxy federateProxy, RequestFederationSave requestFederationSave)
  {
    federationExecutionStateLock.writeLock().lock();
    try
    {
      if (saveInProgress())
      {
        federateProxy.getFederateChannel().write(new RequestFederationSaveResponse(
          requestFederationSave.getRequestId(),
          FederateMessageProtos.RequestFederationSaveResponse.Failure.Cause.SAVE_IN_PROGRESS));
      }
      else if (restoreInProgress())
      {
        federateProxy.getFederateChannel().write(new RequestFederationSaveResponse(
          requestFederationSave.getRequestId(),
          FederateMessageProtos.RequestFederationSaveResponse.Failure.Cause.RESTORE_IN_PROGRESS));
      }
      else if (federationExecutionSave == null)
      {
        createFederationExecutionSave(federateProxy, requestFederationSave);
      }
      else
      {
        assert federationExecutionSave.getSaveTime() != null;
        assert federationExecutionSave.getInstructedToSave().isEmpty();

        // TODO: unschedule

        createFederationExecutionSave(federateProxy, requestFederationSave);
      }
    }
    finally
    {
      federationExecutionStateLock.writeLock().unlock();
    }
  }

  public void federateSaveBegun(FederateProxy federateProxy, FederateSaveBegun federateSaveBegun)
  {
    federationExecutionStateLock.writeLock().lock();
    try
    {
      assert federationExecutionSave != null : federateProxy.getFederateName();

      federationExecutionSave.federateSaveBegun(federateProxy.getFederateHandle());

      federateProxy.federateSaveBegun(federateSaveBegun);
    }
    finally
    {
      federationExecutionStateLock.writeLock().unlock();
    }
  }

  public void federateSaveComplete(FederateProxy federateProxy, FederateSaveComplete federateSaveComplete)
  {
    federationExecutionStateLock.writeLock().lock();
    try
    {
      assert federationExecutionSave != null;

      federateProxy.federateSaveComplete(federateSaveComplete);

      if (federationExecutionSave.federateSaveComplete(federateProxy.getFederateHandle()))
      {
        federationExecutionState = FederationExecutionState.ACTIVE;

        federationExecutionSave.federationSaved(federates);

        federationExecutionSave = null;
      }
    }
    finally
    {
      federationExecutionStateLock.writeLock().unlock();
    }
  }

  public void federateSaveNotComplete(FederateProxy federateProxy, FederateSaveNotComplete federateSaveNotComplete)
  {
    federationExecutionStateLock.writeLock().lock();
    try
    {
      assert federationExecutionSave != null;

      federateProxy.federateSaveNotComplete(federateSaveNotComplete);

      if (federationExecutionSave.federateSaveNotComplete(federateProxy.getFederateHandle()))
      {
        FederationNotSaved federationNotSaved = new FederationNotSaved(federationExecutionSave.getSaveFailureReason());

        for (FederateProxy f : federates.values())
        {
          f.federationNotSaved(federationNotSaved);
        }

        federationExecutionState = FederationExecutionState.ACTIVE;
      }
    }
    finally
    {
      federationExecutionStateLock.writeLock().unlock();
    }
  }

  public void abortFederationSave(FederateProxy federateProxy, AbortFederationSave abortFederationSave)
  {
    federationExecutionStateLock.writeLock().lock();
    try
    {
    }
    finally
    {
      federationExecutionStateLock.writeLock().unlock();
    }
  }

  public void queryFederationSaveStatus(
    FederateProxy federateProxy, QueryFederationSaveStatus queryFederationSaveStatus)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      Map<FederateHandle, SaveStatus> federationSaveStatus = new HashMap<FederateHandle, SaveStatus>();
      for (FederateHandle federateHandle : federates.keySet())
      {
        federationSaveStatus.put(federateHandle, SaveStatus.NO_SAVE_IN_PROGRESS);
      }

      if (federationExecutionSave != null)
      {
        federationExecutionSave.updateFederationSaveStatus(federationSaveStatus);
      }

      FederateHandleSaveStatusPair[] federationSaveStatusPairs =
        new FederateHandleSaveStatusPair[federationSaveStatus.size()];
      int i = 0;
      for (Map.Entry<FederateHandle, SaveStatus> entry : federationSaveStatus.entrySet())
      {
        federationSaveStatusPairs[i++] = new FederateHandleSaveStatusPair(entry.getKey(), entry.getValue());
      }

      federateProxy.getFederateChannel().write(new FederationSaveStatusResponse(federationSaveStatusPairs));
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void requestFederationRestore(FederateProxy federateProxy, RequestFederationRestore requestFederationRestore)
  {
    // use a variable for which lock is held because it may be downgraded to a read lock
    //
    Lock lock = federationExecutionStateLock.writeLock();

    lock.lock();
    try
    {
      if (saveInProgress())
      {
        federateProxy.getFederateChannel().write(new RequestFederationRestoreResponse(
          requestFederationRestore.getRequestId(),
          FederateMessageProtos.RequestFederationRestoreResponse.Failure.Cause.SAVE_IN_PROGRESS));
      }
      else if (restoreInProgress())
      {
        federateProxy.getFederateChannel().write(new RequestFederationRestoreResponse(
          requestFederationRestore.getRequestId(),
          FederateMessageProtos.RequestFederationRestoreResponse.Failure.Cause.RESTORE_IN_PROGRESS));
      }
      else
      {
        try
        {
          federationExecutionRestore = new FederationExecutionRestore(
            this, requestFederationRestore.getLabel(), federateProxy);

          // this is a psuedo restore-in-progress... it will only prevent joins and new requests to save/restore
          //
          federationExecutionState = FederationExecutionState.RESTORE_IN_PROGRESS;

          federateProxy.getFederateChannel().write(
            new RequestFederationRestoreResponse(requestFederationRestore.getRequestId()));

          federateProxy.requestFederationRestoreSucceeded(
            new RequestFederationRestoreSucceeded(requestFederationRestore.getLabel()));

          // downgrade the lock to a read lock so that status requests can be given
          //
          (lock = federationExecutionStateLock.readLock()).lock();
          federationExecutionStateLock.writeLock().unlock();

          federationExecutionRestore.begin();
          federationExecutionRestore.initiate();
        }
        catch (IOException ioe)
        {
          // TODO: write an error message

          ioe.printStackTrace();
        }
      }
    }
    finally
    {
      lock.unlock();
    }
  }

  public void federateRestoreComplete(FederateProxy federateProxy, FederateRestoreComplete federateRestoreComplete)
  {
    federationExecutionStateLock.writeLock().lock();
    try
    {
      if (federationExecutionRestore == null)
      {
        // restore was cancelled or failed
      }
      else if (federationExecutionRestore.federateRestoreComplete(federateProxy.getFederateHandle()))
      {
        federates.clear();
        federatesByName.clear();

        federationExecutionRestore.federationRestored();

        federationExecutionState = FederationExecutionState.ACTIVE;

        federationExecutionRestore = null;
      }
    }
    finally
    {
      federationExecutionStateLock.writeLock().unlock();
    }
  }

  public void federateRestoreNotComplete(
    FederateProxy federateProxy, FederateRestoreNotComplete federateRestoreNotComplete)
  {
    federationExecutionStateLock.writeLock().lock();
    try
    {
      if (federationExecutionRestore == null)
      {
        // restore was cancelled or failed
      }
      else
      {
        federationExecutionRestore.federateRestoreNotComplete(federateProxy.getFederateHandle());

        federationExecutionState = FederationExecutionState.ACTIVE;

        federationExecutionRestore = null;
      }
    }
    finally
    {
      federationExecutionStateLock.writeLock().unlock();
    }
  }

  public void abortFederationRestore(FederateProxy federateProxy, AbortFederationRestore abortFederationRestore)
  {
    federationExecutionStateLock.writeLock().lock();
    try
    {
      if (federationExecutionRestore == null)
      {
        federateProxy.getFederateChannel().write(new AbortFederationRestoreResponse(
          abortFederationRestore.getRequestId(),
          FederateMessageProtos.AbortFederationRestoreResponse.Failure.Cause.RESTORE_NOT_IN_PROGRESS));
      }
      else
      {
        federationExecutionRestore.abort();
      }
    }
    finally
    {
      federationExecutionStateLock.writeLock().unlock();
    }
  }

  public void queryFederationRestoreStatus(
    FederateProxy federateProxy, QueryFederationRestoreStatus queryFederationRestoreStatus)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      FederateRestoreStatus[] federationRestoreStatus;
      if (federationExecutionRestore == null)
      {
        federationRestoreStatus = new FederateRestoreStatus[federates.size()];

        Iterator<FederateHandle> federateHandles = federates.keySet().iterator();
        for (int i = 0; i < federationRestoreStatus.length; i++)
        {
          federationRestoreStatus[i] =
            new FederateRestoreStatus(federateHandles.next(), null, RestoreStatus.NO_RESTORE_IN_PROGRESS);
        }
      }
      else
      {
        federationRestoreStatus = federationExecutionRestore.queryFederationRestoreStatus();
      }

      federateProxy.getFederateChannel().write(new FederationRestoreStatusResponse(federationRestoreStatus));
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void federationRestoreSucceeded()
  {
  }

  public void federationRestoreFailed()
  {
  }

  public void reserveObjectInstanceName(
    FederateProxy federateProxy, ReserveObjectInstanceName reserveObjectInstanceName)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), reserveObjectInstanceName);
      }
      else
      {
        objectManager.reserveObjectInstanceName(federateProxy, reserveObjectInstanceName.getObjectInstanceName());
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void releaseObjectInstanceName(
    FederateProxy federateProxy, ReleaseObjectInstanceName releaseObjectInstanceName)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), releaseObjectInstanceName);
      }
      else
      {
        objectManager.releaseObjectInstanceName(federateProxy, releaseObjectInstanceName.getObjectInstanceName());
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void reserveMultipleObjectInstanceName(
    FederateProxy federateProxy, ReserveMultipleObjectInstanceName reserveMultipleObjectInstanceName)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), reserveMultipleObjectInstanceName);
      }
      else
      {
        objectManager.reserveMultipleObjectInstanceName(
          federateProxy, reserveMultipleObjectInstanceName.getObjectInstanceNames());
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void releaseMultipleObjectInstanceName(
    FederateProxy federateProxy, ReleaseMultipleObjectInstanceName releaseMultipleObjectInstanceName)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), releaseMultipleObjectInstanceName);
      }
      else
      {
        objectManager.releaseMultipleObjectInstanceName(
          federateProxy, releaseMultipleObjectInstanceName.getObjectInstanceNames());
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void registerObjectInstance(FederateProxy federateProxy, RegisterObjectInstance registerObjectInstance)
  {
    federationExecutionStateLock.readLock().lock();
    regionManager.getRegionsLock().readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), registerObjectInstance);
      }
      else
      {
        FederationExecutionObjectInstance objectInstance = objectManager.registerObjectInstance(
          federateProxy, registerObjectInstance.getObjectInstanceHandle(), registerObjectInstance.getObjectClassHandle(),
          registerObjectInstance.getObjectInstanceName(), registerObjectInstance.getPublishedAttributeHandles(),
          registerObjectInstance.getAttributesAndRegions());

        for (FederateProxy f : federates.values())
        {
          if (f != federateProxy)
          {
            f.discoverObjectInstance(objectInstance);
          }
        }
      }
    }
    finally
    {
      regionManager.getRegionsLock().readLock().unlock();
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void updateAttributeValues(FederateProxy producingFederateProxy, UpdateAttributeValues updateAttributeValues)
  {
    federationExecutionStateLock.readLock().lock();
    regionManager.getRegionsLock().readLock().lock();
    if (updateAttributeValues.getSentOrderType() == OrderType.TIMESTAMP)
    {
      // only one timestamp ordered message can be processed at a time
      //
      timeManager.getTimeLock().writeLock().lock();

      // save for future time constrained federates
      //
      timeStampOrderedMessageQueue.add(producingFederateProxy.getFederateHandle(), updateAttributeValues);
    }
    try
    {
      if (producingFederateProxy.isSaving())
      {
        saveMessage(producingFederateProxy.getFederateHandle(), updateAttributeValues);
      }
      else
      {
        objectManager.updateAttributeValues(producingFederateProxy, updateAttributeValues);
      }
    }
    finally
    {
      if (updateAttributeValues.getSentOrderType() == OrderType.TIMESTAMP)
      {
        timeManager.getTimeLock().writeLock().unlock();
      }
      regionManager.getRegionsLock().readLock().unlock();
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public boolean reflectAttributeValues(
    FederateProxy receivingFederateProxy, FederateHandle producingFederateHandle,
    UpdateAttributeValues updateAttributeValues, OrderType orderType)
  {
    assert federationExecutionStateLock.getReadHoldCount() > 0;
    assert regionManager.getRegionsLock().getReadHoldCount() > 0;
    assert timeManager.getTimeLock().isWriteLockedByCurrentThread();

    return objectManager.reflectAttributeValues(
      receivingFederateProxy, producingFederateHandle, updateAttributeValues, orderType);
  }

  public void sendInteraction(FederateProxy producingFederateProxy, SendInteraction sendInteraction)
  {
    federationExecutionStateLock.readLock().lock();
    regionManager.getRegionsLock().readLock().lock();
    if (sendInteraction.getSentOrderType() == OrderType.TIMESTAMP)
    {
      // only one timestamp ordered message can be processed at a time
      //
      timeManager.getTimeLock().writeLock().lock();

      // save for future time constrained federates
      //
      timeStampOrderedMessageQueue.add(producingFederateProxy.getFederateHandle(), sendInteraction);
    }
    try
    {
      if (producingFederateProxy.isSaving())
      {
        saveMessage(producingFederateProxy.getFederateHandle(), sendInteraction);
      }
      else
      {
        for (FederateProxy federateProxy : federates.values())
        {
          if (federateProxy != producingFederateProxy)
          {
            federateProxy.receiveInteraction(producingFederateProxy.getFederateHandle(), sendInteraction);
          }
        }
      }
    }
    finally
    {
      if (sendInteraction.getSentOrderType() == OrderType.TIMESTAMP)
      {
        timeManager.getTimeLock().writeLock().unlock();
      }
      regionManager.getRegionsLock().readLock().unlock();
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public boolean receiveInteraction(
    FederateProxy receivingFederateProxy, FederateHandle producingFederateHandle,
    SendInteraction sendInteraction, OrderType receivedOrderType)
  {
    assert federationExecutionStateLock.getReadHoldCount() > 0;
    assert regionManager.getRegionsLock().getReadHoldCount() > 0;
    assert timeManager.getTimeLock().isWriteLockedByCurrentThread();

    return receivingFederateProxy.receiveInteractionNow(producingFederateHandle, sendInteraction, receivedOrderType);
  }

  public void deleteObjectInstance(FederateProxy producingFederateProxy, DeleteObjectInstance deleteObjectInstance)
  {
    federationExecutionStateLock.readLock().lock();
    if (deleteObjectInstance.getSentOrderType() == OrderType.TIMESTAMP)
    {
      // only one timestamp ordered message can be processed at a time
      //
      timeManager.getTimeLock().writeLock().lock();

      // save for future time constrained federates
      //
      timeStampOrderedMessageQueue.add(producingFederateProxy.getFederateHandle(), deleteObjectInstance);
    }
    try
    {
      if (producingFederateProxy.isSaving())
      {
        saveMessage(producingFederateProxy.getFederateHandle(), deleteObjectInstance);
      }
      else
      {
        objectManager.deleteObjectInstance(producingFederateProxy, deleteObjectInstance);
      }
    }
    finally
    {
      if (deleteObjectInstance.getSentOrderType() == OrderType.TIMESTAMP)
      {
        timeManager.getTimeLock().writeLock().unlock();
      }
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void localDeleteObjectInstance(
    FederateProxy federateProxy, LocalDeleteObjectInstance localDeleteObjectInstance)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), localDeleteObjectInstance);
      }
      else
      {
        objectManager.localDeleteObjectInstance(federateProxy, localDeleteObjectInstance);
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void requestObjectInstanceAttributeValueUpdate(
    FederateProxy federateProxy, RequestObjectInstanceAttributeValueUpdate requestObjectInstanceAttributeValueUpdate)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), requestObjectInstanceAttributeValueUpdate);
      }
      else
      {
        objectManager.requestObjectInstanceAttributeValueUpdate(federateProxy, requestObjectInstanceAttributeValueUpdate);
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void requestObjectClassAttributeValueUpdate(
    FederateProxy federateProxy, RequestObjectClassAttributeValueUpdate requestObjectClassAttributeValueUpdate)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), requestObjectClassAttributeValueUpdate);
      }
      else
      {
        objectManager.requestObjectClassAttributeValueUpdate(federateProxy, requestObjectClassAttributeValueUpdate);
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void requestObjectClassAttributeValueUpdateWithRegions(
    FederateProxy federateProxy,
    RequestObjectClassAttributeValueUpdateWithRegions requestObjectClassAttributeValueUpdateWithRegions)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), requestObjectClassAttributeValueUpdateWithRegions);
      }
      else
      {
        objectManager.requestObjectClassAttributeValueUpdateWithRegions(
          federateProxy, requestObjectClassAttributeValueUpdateWithRegions);
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void retract(FederateProxy retractingFederateProxy, Retract retract)
  {
    federationExecutionStateLock.readLock().lock();
    timeManager.getTimeLock().readLock().lock();
    try
    {
      if (retractingFederateProxy.isSaving())
      {
        saveMessage(retractingFederateProxy.getFederateHandle(), retract);
      }
      else if (timeStampOrderedMessageQueue.retract(retract.getMessageRetractionHandle()))
      {
        objectManager.retract(retractingFederateProxy, retract);

        for (FederateProxy federateProxy : federates.values())
        {
          if (federateProxy != retractingFederateProxy)
          {
            federateProxy.retract(retract);
          }
        }
      }
      else
      {
        // TODO: ignore, log
      }
    }
    finally
    {
      timeManager.getTimeLock().readLock().unlock();
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void publishObjectClassAttributes(
    FederateProxy federateProxy, PublishObjectClassAttributes publishObjectClassAttributes)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), publishObjectClassAttributes);
      }
      else
      {
        ObjectClass objectClass = fdd.getObjectClassSafely(publishObjectClassAttributes.getObjectClassHandle());

        federateProxy.publishObjectClassAttributes(publishObjectClassAttributes);

        objectManager.publishObjectClassAttributes(
          federateProxy, objectClass, publishObjectClassAttributes.getAttributeHandles());
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void unpublishObjectClass(FederateProxy federateProxy, UnpublishObjectClass unpublishObjectClass)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), unpublishObjectClass);
      }
      else
      {
        federateProxy.unpublishObjectClass(unpublishObjectClass);

        objectManager.unpublishObjectClass(federateProxy, unpublishObjectClass.getObjectInstanceHandles());
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void unpublishObjectClassAttributes(
    FederateProxy federateProxy, UnpublishObjectClassAttributes unpublishObjectClassAttributes)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), unpublishObjectClassAttributes);
      }
      else
      {
        federateProxy.unpublishObjectClassAttributes(unpublishObjectClassAttributes);

        objectManager.unpublishObjectClassAttributes(
          federateProxy, unpublishObjectClassAttributes.getAttributeHandles(),
          unpublishObjectClassAttributes.getObjectInstanceHandles());
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void publishInteractionClass(FederateProxy federateProxy, PublishInteractionClass publishInteractionClass)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), publishInteractionClass);
      }
      else
      {
        federateProxy.publishInteractionClass(publishInteractionClass);
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void unpublishInteractionClass(FederateProxy federateProxy,
                                        UnpublishInteractionClass unpublishInteractionClass)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), unpublishInteractionClass);
      }
      else
      {
        federateProxy.unpublishInteractionClass(unpublishInteractionClass);
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void subscribeObjectClassAttributes(
    FederateProxy federateProxy, SubscribeObjectClassAttributes subscribeObjectClassAttributes)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), subscribeObjectClassAttributes);
      }
      else
      {
        ObjectClass objectClass = fdd.getObjectClassSafely(subscribeObjectClassAttributes.getObjectClassHandle());

        federateProxy.subscribeObjectClassAttributes(subscribeObjectClassAttributes);

        objectManager.subscribeObjectClassAttributes(federateProxy, objectClass);
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void unsubscribeObjectClassAttributes(
    FederateProxy federateProxy, UnsubscribeObjectClassAttributes unsubscribeObjectClassAttributes)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), unsubscribeObjectClassAttributes);
      }
      else
      {
        federateProxy.unsubscribeObjectClassAttributes(unsubscribeObjectClassAttributes);
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void subscribeObjectClassAttributesWithRegions(
    FederateProxy federateProxy, SubscribeObjectClassAttributesWithRegions subscribeObjectClassAttributesWithRegions)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), subscribeObjectClassAttributesWithRegions);
      }
      else
      {
        ObjectClass objectClass = fdd.getObjectClassSafely(
          subscribeObjectClassAttributesWithRegions.getObjectClassHandle());

        federateProxy.subscribeObjectClassAttributesWithRegions(subscribeObjectClassAttributesWithRegions);

        objectManager.subscribeObjectClassAttributesWithRegions(
          federateProxy, objectClass, subscribeObjectClassAttributesWithRegions.getAttributesAndRegions());
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void unsubscribeObjectClassAttributesWithRegions(
    FederateProxy federateProxy,
    UnsubscribeObjectClassAttributesWithRegions unsubscribeObjectClassAttributesWithRegions)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), unsubscribeObjectClassAttributesWithRegions);
      }
      else
      {
        federateProxy.unsubscribeObjectClassAttributesWithRegions(unsubscribeObjectClassAttributesWithRegions);
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void subscribeInteractionClass(
    FederateProxy federateProxy, SubscribeInteractionClass subscribeInteractionClass)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), subscribeInteractionClass);
      }
      else
      {
        federateProxy.subscribeInteractionClass(subscribeInteractionClass);
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void unsubscribeInteractionClass(
    FederateProxy federateProxy, UnsubscribeInteractionClass unsubscribeInteractionClass)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), unsubscribeInteractionClass);
      }
      else
      {
        federateProxy.unsubscribeInteractionClass(unsubscribeInteractionClass);
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void subscribeInteractionClassWithRegions(
    FederateProxy federateProxy, SubscribeInteractionClassWithRegions subscribeInteractionClassWithRegions)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), subscribeInteractionClassWithRegions);
      }
      else
      {
        federateProxy.subscribeInteractionClassWithRegions(subscribeInteractionClassWithRegions);
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void unsubscribeInteractionClassWithRegions(
    FederateProxy federateProxy, UnsubscribeInteractionClassWithRegions unsubscribeInteractionClassWithRegions)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), unsubscribeInteractionClassWithRegions);
      }
      else
      {
        federateProxy.unsubscribeInteractionClassWithRegions(unsubscribeInteractionClassWithRegions);
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void unconditionalAttributeOwnershipDivestiture(
    FederateProxy federateProxy, UnconditionalAttributeOwnershipDivestiture unconditionalAttributeOwnershipDivestiture)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), unconditionalAttributeOwnershipDivestiture);
      }
      else
      {
        objectManager.unconditionalAttributeOwnershipDivestiture(
          federateProxy,
          unconditionalAttributeOwnershipDivestiture.getObjectInstanceHandle(),
          unconditionalAttributeOwnershipDivestiture.getAttributeHandles());
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void negotiatedAttributeOwnershipDivestiture(
    FederateProxy federateProxy, NegotiatedAttributeOwnershipDivestiture negotiatedAttributeOwnershipDivestiture)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), negotiatedAttributeOwnershipDivestiture);
      }
      else
      {
        objectManager.negotiatedAttributeOwnershipDivestiture(
          federateProxy,
          negotiatedAttributeOwnershipDivestiture.getObjectInstanceHandle(),
          negotiatedAttributeOwnershipDivestiture.getAttributeHandles(),
          negotiatedAttributeOwnershipDivestiture.getTag());
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void confirmDivestiture(FederateProxy federateProxy, ConfirmDivestiture confirmDivestiture)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), confirmDivestiture);
      }
      else
      {
        objectManager.confirmDivestiture(
          federateProxy, confirmDivestiture.getObjectInstanceHandle(), confirmDivestiture.getAttributeHandles());
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisition(
    FederateProxy federateProxy, AttributeOwnershipAcquisition attributeOwnershipAcquisition)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), attributeOwnershipAcquisition);
      }
      else
      {
        objectManager.attributeOwnershipAcquisition(
          federateProxy, attributeOwnershipAcquisition.getObjectInstanceHandle(),
          attributeOwnershipAcquisition.getAttributeHandles(), attributeOwnershipAcquisition.getTag());
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisitionIfAvailable(
    FederateProxy federateProxy, AttributeOwnershipAcquisitionIfAvailable attributeOwnershipAcquisitionIfAvailable)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), attributeOwnershipAcquisitionIfAvailable);
      }
      else
      {
        objectManager.attributeOwnershipAcquisitionIfAvailable(
          federateProxy, attributeOwnershipAcquisitionIfAvailable.getObjectInstanceHandle(),
          attributeOwnershipAcquisitionIfAvailable.getAttributeHandles());
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void attributeOwnershipDivestitureIfWanted(
    FederateProxy federateProxy, AttributeOwnershipDivestitureIfWanted attributeOwnershipDivestitureIfWanted)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        // this message requires a response
        //
        federateProxy.getFederateChannel().write(new AttributeOwnershipDivestitureIfWantedResponse(
          attributeOwnershipDivestitureIfWanted.getRequestId(), null));
      }
      else
      {
        Map<AttributeHandle, FederationExecutionAttributeInstance.Divestiture> divestitures =
          objectManager.attributeOwnershipDivestitureIfWanted(
            federateProxy, attributeOwnershipDivestitureIfWanted.getObjectInstanceHandle(),
            attributeOwnershipDivestitureIfWanted.getAttributeHandles());

        // notify the divestee what attributes were divested
        //
        federateProxy.getFederateChannel().write(new AttributeOwnershipDivestitureIfWantedResponse(
          attributeOwnershipDivestitureIfWanted.getRequestId(), new IEEE1516eAttributeHandleSet(divestitures.keySet())));

        // divide up the divested attributes by owner
        //
        Map<FederateHandle, AttributeHandleSetTagPair> newOwnerAcquisitions = new HashMap<>();
        for (Map.Entry<AttributeHandle, FederationExecutionAttributeInstance.Divestiture> entry : divestitures.entrySet())
        {
          FederationExecutionAttributeInstance.Divestiture divestiture = entry.getValue();
          AttributeHandleSetTagPair acquiredAttributes = newOwnerAcquisitions.get(divestiture.newOwner);
          if (acquiredAttributes == null)
          {
            acquiredAttributes = new AttributeHandleSetTagPair(divestiture.tag);
            newOwnerAcquisitions.put(divestiture.newOwner, acquiredAttributes);
          }
          acquiredAttributes.attributeHandles.add(entry.getKey());
        }

        // notify the new owners
        //
        for (Map.Entry<FederateHandle, AttributeHandleSetTagPair> entry : newOwnerAcquisitions.entrySet())
        {
          getFederate(entry.getKey()).getFederateChannel().write(new AttributeOwnershipAcquisitionNotification(
            attributeOwnershipDivestitureIfWanted.getObjectInstanceHandle(), entry.getValue().attributeHandles,
            entry.getValue().tag));
        }
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture(
    FederateProxy federateProxy,
    CancelNegotiatedAttributeOwnershipDivestiture cancelNegotiatedAttributeOwnershipDivestiture)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), cancelNegotiatedAttributeOwnershipDivestiture);
      }
      else
      {
        objectManager.cancelNegotiatedAttributeOwnershipDivestiture(
          federateProxy, cancelNegotiatedAttributeOwnershipDivestiture.getObjectInstanceHandle(),
          cancelNegotiatedAttributeOwnershipDivestiture.getAttributeHandles());
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void cancelAttributeOwnershipAcquisition(
    FederateProxy federateProxy, CancelAttributeOwnershipAcquisition cancelAttributeOwnershipAcquisition)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), cancelAttributeOwnershipAcquisition);
      }
      else
      {
        objectManager.cancelAttributeOwnershipAcquisition(
          federateProxy, cancelAttributeOwnershipAcquisition.getObjectInstanceHandle(),
          cancelAttributeOwnershipAcquisition.getAttributeHandles());
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void queryAttributeOwnership(FederateProxy federateProxy, QueryAttributeOwnership queryAttributeOwnership)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), queryAttributeOwnership);
      }
      else
      {
        objectManager.queryAttributeOwnership(
          federateProxy, queryAttributeOwnership.getObjectInstanceHandle(), queryAttributeOwnership.getAttributeHandle());
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void enableTimeRegulation(FederateProxy federateProxy, EnableTimeRegulation enableTimeRegulation)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), enableTimeRegulation);
      }
      else
      {
        timeManager.enableTimeRegulation(federateProxy, enableTimeRegulation);
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void disableTimeRegulation(FederateProxy federateProxy, DisableTimeRegulation disableTimeRegulation)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), disableTimeRegulation);
      }
      else
      {
        timeManager.disableTimeRegulation(federateProxy);
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void enableTimeConstrained(FederateProxy federateProxy, EnableTimeConstrained enableTimeConstrained)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), enableTimeConstrained);
      }
      else
      {
        timeManager.enableTimeConstrained(federateProxy);
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void disableTimeConstrained(FederateProxy federateProxy, DisableTimeConstrained disableTimeConstrained)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), disableTimeConstrained);
      }
      else
      {
        timeManager.disableTimeConstrained(federateProxy);
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void queryGALT(FederateProxy federateProxy, QueryGALT queryGALT)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      timeManager.queryGALT(federateProxy, queryGALT);
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void queryLITS(FederateProxy federateProxy, QueryLITS queryLITS)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      timeManager.queryLITS(federateProxy, queryLITS);
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void modifyLookahead(FederateProxy federateProxy, ModifyLookahead modifyLookahead)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), modifyLookahead);
      }
      else
      {
        timeManager.modifyLookahead(federateProxy, modifyLookahead);
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void timeAdvanceRequest(FederateProxy federateProxy, TimeAdvanceRequest timeAdvanceRequest)
  {
    federationExecutionStateLock.readLock().lock();
    regionManager.getRegionsLock().readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), timeAdvanceRequest);
      }
      else
      {
        timeManager.timeAdvanceRequest(federateProxy, timeAdvanceRequest);
      }
    }
    finally
    {
      regionManager.getRegionsLock().readLock().unlock();
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void timeAdvanceRequestAvailable(
    FederateProxy federateProxy, TimeAdvanceRequestAvailable timeAdvanceRequestAvailable)
  {
    federationExecutionStateLock.readLock().lock();
    regionManager.getRegionsLock().readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), timeAdvanceRequestAvailable);
      }
      else
      {
        timeManager.timeAdvanceRequestAvailable(federateProxy, timeAdvanceRequestAvailable);
      }
    }
    finally
    {
      regionManager.getRegionsLock().readLock().unlock();
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void nextMessageRequest(FederateProxy federateProxy, NextMessageRequest nextMessageRequest)
  {
    federationExecutionStateLock.readLock().lock();
    regionManager.getRegionsLock().readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), nextMessageRequest);
      }
      else
      {
        timeManager.nextMessageRequest(federateProxy, nextMessageRequest);
      }
    }
    finally
    {
      regionManager.getRegionsLock().readLock().unlock();
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void nextMessageRequestAvailable(
    FederateProxy federateProxy, NextMessageRequestAvailable nextMessageRequestAvailable)
  {
    federationExecutionStateLock.readLock().lock();
    regionManager.getRegionsLock().readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), nextMessageRequestAvailable);
      }
      else
      {
        timeManager.nextMessageRequestAvailable(federateProxy, nextMessageRequestAvailable);
      }
    }
    finally
    {
      regionManager.getRegionsLock().readLock().unlock();
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void flushQueueRequest(FederateProxy federateProxy, FlushQueueRequest flushQueueRequest)
  {
    federationExecutionStateLock.readLock().lock();
    regionManager.getRegionsLock().readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), flushQueueRequest);
      }
      else
      {
        timeManager.flushQueueRequest(federateProxy, flushQueueRequest);
      }
    }
    finally
    {
      regionManager.getRegionsLock().readLock().unlock();
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void createRegion(FederateProxy federateProxy, CreateRegion createRegion)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), createRegion);
      }
      else
      {
        regionManager.createRegion(federateProxy, createRegion);
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void commitRegionModifications(
    FederateProxy federateProxy, CommitRegionModifications commitRegionModifications)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), commitRegionModifications);
      }
      else
      {
        regionManager.commitRegionModifications(federateProxy, commitRegionModifications);
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void deleteRegion(FederateProxy federateProxy, DeleteRegion deleteRegion)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), deleteRegion);
      }
      else
      {
        regionManager.deleteRegion(federateProxy, deleteRegion);
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void associateRegionsForUpdates(
    FederateProxy federateProxy, AssociateRegionsForUpdates associateRegionsForUpdates)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        federateProxy.getFederateChannel().write(new AssociateRegionsForUpdatesResponse(
          associateRegionsForUpdates.getRequestId(),
          FederateMessageProtos.AssociateRegionsForUpdatesResponse.Failure.Cause.SAVE_IN_PROGRESS));
      }
      else
      {
        objectManager.associateRegionsForUpdates(federateProxy, associateRegionsForUpdates);
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void unassociateRegionsForUpdates(
    FederateProxy federateProxy, UnassociateRegionsForUpdates unassociateRegionsForUpdates)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        federateProxy.getFederateChannel().write(new UnassociateRegionsForUpdatesResponse(
          unassociateRegionsForUpdates.getRequestId(),
          FederateMessageProtos.UnassociateRegionsForUpdatesResponse.Failure.Cause.SAVE_IN_PROGRESS));
      }
      else
      {
        objectManager.unassociateRegionsForUpdates(federateProxy, unassociateRegionsForUpdates);
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void queryInteractionTransportationType(
    FederateProxy federateProxy, QueryInteractionTransportationType queryInteractionTransportationType)
  {
    FederateHandle federateHandle = queryInteractionTransportationType.getFederateHandle();

    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), queryInteractionTransportationType);
      }
      else
      {
        FederateProxy f = federates.get(federateHandle);
        if (f == null)
        {
          InteractionClassHandle interactionClassHandle = queryInteractionTransportationType.getInteractionClassHandle();

          InteractionClass interactionClass = fdd.getInteractionClasses().get(interactionClassHandle);
          assert interactionClass != null;

          federateProxy.getFederateChannel().write(new ReportInteractionTransportationType(
            interactionClassHandle, federateHandle, interactionClass.getTransportationTypeHandle()));
        }
        else
        {
          federateProxy.queryInteractionTransportationType(federateProxy, queryInteractionTransportationType);
        }
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void getFederateHandle(FederateProxy federateProxy, GetFederateHandle getFederateHandle)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      FederateProxy f = federatesByName.get(getFederateHandle.getFederateName());
      if (f == null)
      {
        federateProxy.getFederateChannel().write(new GetFederateHandleResponse(
          getFederateHandle.getRequestId(),
          FederateMessageProtos.GetFederateHandleResponse.Failure.Cause.NAME_NOT_FOUND));
      }
      else
      {
        federateProxy.getFederateChannel().write(new GetFederateHandleResponse(
          getFederateHandle.getRequestId(), f.getFederateHandle()));
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void getFederateName(FederateProxy federateProxy, GetFederateName getFederateName)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      FederateProxy f = federates.get(getFederateName.getFederateHandle());
      if (f == null)
      {
        federateProxy.getFederateChannel().write(new GetFederateNameResponse(
          getFederateName.getRequestId(),
          FederateMessageProtos.GetFederateNameResponse.Failure.Cause.FEDERATE_HANDLE_NOT_KNOWN));
      }
      else
      {
        federateProxy.getFederateChannel().write(new GetFederateNameResponse(
          getFederateName.getRequestId(), f.getFederateName()));
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void getUpdateRateValueForAttribute(
    FederateProxy federateProxy, GetUpdateRateValueForAttribute getUpdateRateValueForAttribute)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      objectManager.getUpdateRateValueForAttribute(federateProxy, getUpdateRateValueForAttribute);
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void setAutomaticResignDirective(
    FederateProxy federateProxy, SetAutomaticResignDirective setAutomaticResignDirective)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federateProxy.isSaving())
      {
        saveMessage(federateProxy.getFederateHandle(), setAutomaticResignDirective);
      }
      else
      {
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void handleFederateStateFrame(FederateProxy federateProxy, FederateStateFrame federateStateFrame)
  {
    federateProxy.handleFederateStateFrame(federateStateFrame);
  }

  public void galtUpdated(LogicalTime galt)
  {
    timeStampOrderedMessageQueue.expire(galt);

    objectManager.galtUpdated(galt);
  }

  public void galtUndefined()
  {
    timeStampOrderedMessageQueue.clear();

    objectManager.galtUndefined();
  }

  public void saveState(CodedOutputStream out)
    throws IOException
  {
    FederationExecutionSaveProtos.FederationExecutionState.Builder federationExecutionState =
      FederationExecutionSaveProtos.FederationExecutionState.newBuilder();

    federationExecutionState.setNextFederateHandle(nextFederateHandle);
    federationExecutionState.setSynchonizationPointStateCount(synchronizationPoints.size());

    out.writeMessageNoTag(federationExecutionState.build());

    for (FederationExecutionSynchronizationPoint synchronizationPoint : synchronizationPoints.values())
    {
      out.writeMessageNoTag(synchronizationPoint.saveState().build());
    }

    objectManager.saveState(out);
    regionManager.saveState(out);
    timeManager.saveState(out);
    timeStampOrderedMessageQueue.saveState(out);
  }

  public void restoreState(CodedInputStream in)
    throws IOException
  {
    FederationExecutionSaveProtos.FederationExecutionState federationExecutionState =
      in.readMessage(FederationExecutionSaveProtos.FederationExecutionState.PARSER, null);

    nextFederateHandle = federationExecutionState.getNextFederateHandle();

    for (int synchonizationPointStateCount = federationExecutionState.getSynchonizationPointStateCount();
         synchonizationPointStateCount > 0; --synchonizationPointStateCount)
    {
      FederationExecutionSynchonizationPointState synchonizationPointState =
        in.readMessage(FederationExecutionSynchonizationPointState.PARSER, null);

      FederationExecutionSynchronizationPoint synchronizationPoint =
        new FederationExecutionSynchronizationPoint(synchonizationPointState);
      synchronizationPoints.put(synchronizationPoint.getLabel(), synchronizationPoint);
    }

    objectManager.restoreState(in);
    regionManager.restoreState(in);
    timeManager.restoreState(in);
    timeStampOrderedMessageQueue.restoreState(in);
  }

  @SuppressWarnings("unchecked")
  private void createFederationExecutionSave(FederateProxy federateProxy, RequestFederationSave requestFederationSave)
  {
    LogicalTime time = requestFederationSave.getTime(timeManager.getLogicalTimeFactory());
    if (time == null)
    {
      String label = requestFederationSave.getLabel();

      try
      {
        federationExecutionSave = new FederationExecutionSave(this, label);

        // this is a psuedo save-in-progress... it will only prevent joins and new requests to save/restore
        //
        federationExecutionState = FederationExecutionState.SAVE_IN_PROGRESS;

        // tell the federate that the request is going to be honored
        //
        federateProxy.getFederateChannel().write(
          new RequestFederationSaveResponse(requestFederationSave.getRequestId()));

        try
        {
          // notify all federates to initiate save
          //
          for (FederateProxy f : federates.values())
          {
            f.initiateFederateSave(federationExecutionSave);
          }
        }
        catch (IOException ioe)
        {
          log.warn(LogMessages.UNABLE_TO_INITIATE_FEDERATE_SAVE, ioe);

          // TODO: cancel the save
        }
      }
      catch (IOException ioe)
      {
        log.warn(LogMessages.UNABLE_TO_INITIATE_FEDERATION_SAVE, ioe);

        // TODO: possibly send some text detailing the error?

        federateProxy.getFederateChannel().write(new RequestFederationSaveResponse(
          requestFederationSave.getRequestId(),
          FederateMessageProtos.RequestFederationSaveResponse.Failure.Cause.RTI_INTERNAL_ERROR));
      }
    }
    else
    {
      if (federateProxy.isTimeRegulationEnabled())
      {
        if (time.compareTo(federateProxy.getLOTS()) < 0)
        {
          federateProxy.getFederateChannel().write(new RequestFederationSaveResponse(
            requestFederationSave.getRequestId(),
            FederateMessageProtos.RequestFederationSaveResponse.Failure.Cause.LOGICAL_TIME_ALREADY_PASSED));
        }
        else
        {
          scheduleFederationExecutionSave(federateProxy, requestFederationSave, time);
        }
      }
      else if (federateProxy.isGALTDefined())
      {
        if (time.compareTo(federateProxy.getGALT()) <= 0)
        {
          federateProxy.getFederateChannel().write(new RequestFederationSaveResponse(
            requestFederationSave.getRequestId(),
            FederateMessageProtos.RequestFederationSaveResponse.Failure.Cause.LOGICAL_TIME_ALREADY_PASSED));
        }
        else
        {
          scheduleFederationExecutionSave(federateProxy, requestFederationSave, time);
        }
      }
      else
      {
        federateProxy.getFederateChannel().write(new RequestFederationSaveResponse(
          requestFederationSave.getRequestId(),
          FederateMessageProtos.RequestFederationSaveResponse.Failure.Cause.FEDERATE_UNABLE_TO_USE_TIME));
      }
    }
  }

  private void scheduleFederationExecutionSave(
    FederateProxy federateProxy, RequestFederationSave requestFederationSave, LogicalTime time)
  {
    String label = requestFederationSave.getLabel();

    try
    {
      federationExecutionSave = new FederationExecutionSave(this, label, time);

      // tell the federate that the request is going to be honored
      //
      federateProxy.getFederateChannel().write(new RequestFederationSaveResponse(requestFederationSave.getRequestId()));

      // TODO: schedule save
    }
    catch (IOException ioe)
    {
      log.warn(LogMessages.UNABLE_TO_INITIATE_FEDERATION_SAVE, ioe);

      // TODO: possibly send some text detailing the error?

      federateProxy.getFederateChannel().write(new RequestFederationSaveResponse(
        requestFederationSave.getRequestId(),
        FederateMessageProtos.RequestFederationSaveResponse.Failure.Cause.RTI_INTERNAL_ERROR));
    }
  }

  private boolean saveInProgress()
  {
    return federationExecutionState == FederationExecutionState.SAVE_IN_PROGRESS;
  }

  private boolean restoreInProgress()
  {
    return federationExecutionState == FederationExecutionState.RESTORE_IN_PROGRESS;
  }

  private void saveMessage(FederateHandle federateHandle, FederationExecutionMessage message)
  {
    try
    {
      federationExecutionSave.save(federateHandle, message);
    }
    catch (IOException ioe)
    {
      // TODO: unwind the save
    }
  }
}
