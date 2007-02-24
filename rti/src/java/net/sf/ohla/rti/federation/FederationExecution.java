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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.fdd.Dimension;
import net.sf.ohla.rti.fdd.FDD;
import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.hla.rti1516.IEEE1516AttributeHandleSet;
import net.sf.ohla.rti.hla.rti1516.IEEE1516FederateHandle;
import net.sf.ohla.rti.hla.rti1516.IEEE1516RegionHandle;
import net.sf.ohla.rti.messages.AttributeOwnershipAcquisition;
import net.sf.ohla.rti.messages.AttributeOwnershipAcquisitionIfAvailable;
import net.sf.ohla.rti.messages.AttributeOwnershipDivestitureIfWanted;
import net.sf.ohla.rti.messages.AttributeOwnershipDivestitureIfWantedResponse;
import net.sf.ohla.rti.messages.CancelAttributeOwnershipAcquisition;
import net.sf.ohla.rti.messages.CancelNegotiatedAttributeOwnershipDivestiture;
import net.sf.ohla.rti.messages.CommitRegionModifications;
import net.sf.ohla.rti.messages.ConfirmDivestiture;
import net.sf.ohla.rti.messages.CreateRegion;
import net.sf.ohla.rti.messages.DefaultResponse;
import net.sf.ohla.rti.messages.DeleteObjectInstance;
import net.sf.ohla.rti.messages.DeleteRegion;
import net.sf.ohla.rti.messages.DisableTimeConstrained;
import net.sf.ohla.rti.messages.DisableTimeRegulation;
import net.sf.ohla.rti.messages.EnableTimeConstrained;
import net.sf.ohla.rti.messages.EnableTimeRegulation;
import net.sf.ohla.rti.messages.FederateRestoreComplete;
import net.sf.ohla.rti.messages.FederateRestoreNotComplete;
import net.sf.ohla.rti.messages.FederateSaveBegun;
import net.sf.ohla.rti.messages.FederateSaveComplete;
import net.sf.ohla.rti.messages.FederateSaveInitiated;
import net.sf.ohla.rti.messages.FederateSaveInitiatedFailed;
import net.sf.ohla.rti.messages.FederateSaveNotComplete;
import net.sf.ohla.rti.messages.GetRangeBounds;
import net.sf.ohla.rti.messages.JoinFederationExecution;
import net.sf.ohla.rti.messages.JoinFederationExecutionResponse;
import net.sf.ohla.rti.messages.ModifyLookahead;
import net.sf.ohla.rti.messages.NegotiatedAttributeOwnershipDivestiture;
import net.sf.ohla.rti.messages.QueryAttributeOwnership;
import net.sf.ohla.rti.messages.QueryFederationRestoreStatus;
import net.sf.ohla.rti.messages.QueryFederationSaveStatus;
import net.sf.ohla.rti.messages.RegisterFederationSynchronizationPoint;
import net.sf.ohla.rti.messages.RegisterObjectInstance;
import net.sf.ohla.rti.messages.RequestAttributeValueUpdate;
import net.sf.ohla.rti.messages.RequestFederationRestore;
import net.sf.ohla.rti.messages.RequestFederationSave;
import net.sf.ohla.rti.messages.RequestResponse;
import net.sf.ohla.rti.messages.ReserveObjectInstanceName;
import net.sf.ohla.rti.messages.ResignFederationExecution;
import net.sf.ohla.rti.messages.Retract;
import net.sf.ohla.rti.messages.SendInteraction;
import net.sf.ohla.rti.messages.SubscribeObjectClassAttributes;
import net.sf.ohla.rti.messages.SynchronizationPointAchieved;
import net.sf.ohla.rti.messages.TimeAdvanceRequest;
import net.sf.ohla.rti.messages.TimeAdvanceRequestAvailable;
import net.sf.ohla.rti.messages.UnconditionalAttributeOwnershipDivestiture;
import net.sf.ohla.rti.messages.UpdateAttributeValues;
import net.sf.ohla.rti.messages.callbacks.AnnounceSynchronizationPoint;
import net.sf.ohla.rti.messages.callbacks.AttributeOwnershipAcquisitionNotification;
import net.sf.ohla.rti.messages.callbacks.DiscoverObjectInstance;
import net.sf.ohla.rti.messages.callbacks.FederationNotSaved;
import net.sf.ohla.rti.messages.callbacks.FederationRestoreStatusResponse;
import net.sf.ohla.rti.messages.callbacks.FederationSaveStatusResponse;
import net.sf.ohla.rti.messages.callbacks.FederationSaved;
import net.sf.ohla.rti.messages.callbacks.FederationSynchronized;
import net.sf.ohla.rti.messages.callbacks.InitiateFederateSave;
import net.sf.ohla.rti.messages.callbacks.ReceiveInteraction;
import net.sf.ohla.rti.messages.callbacks.RemoveObjectInstance;
import net.sf.ohla.rti.messages.callbacks.SynchronizationPointRegistrationFailed;
import net.sf.ohla.rti.messages.callbacks.SynchronizationPointRegistrationSucceeded;

import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.DimensionHandle;
import hla.rti1516.FederateHandle;
import hla.rti1516.FederateHandleRestoreStatusPair;
import hla.rti1516.FederateHandleSaveStatusPair;
import hla.rti1516.FederatesCurrentlyJoined;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.OrderType;
import hla.rti1516.RangeBounds;
import hla.rti1516.RegionHandle;
import hla.rti1516.RestoreInProgress;
import hla.rti1516.RestoreStatus;
import hla.rti1516.SaveInProgress;
import hla.rti1516.SaveStatus;
import hla.rti1516.SynchronizationPointFailureReason;

public class FederationExecution
{
  protected final String name;
  protected final FDD fdd;

  protected ReadWriteLock federationExecutionStateLock =
    new ReentrantReadWriteLock(true);
  protected FederationExecutionState federationExecutionState =
    FederationExecutionState.ACTIVE;

  protected FederationExecutionSave federationExecutionSave;
  protected FederationExecutionRestore federationExecutionRestore;

  protected Lock federatesLock = new ReentrantLock(true);
  protected Map<FederateHandle, FederateProxy> federates =
    new HashMap<FederateHandle, FederateProxy>();

  protected Lock synchronizationPointsLock = new ReentrantLock(true);
  protected Map<String, FederationExecutionSynchronizationPoint> synchronizationPoints =
    new HashMap<String, FederationExecutionSynchronizationPoint>();

  protected ReadWriteLock regionsLock = new ReentrantReadWriteLock(true);
  protected Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regions =
    new HashMap<RegionHandle, Map<DimensionHandle, RangeBounds>>();

  protected FederationExecutionObjectManager objectManager =
    new FederationExecutionObjectManager(this);

  protected FederationExecutionTimeManager timeManager;

  protected AtomicInteger objectInstanceCount =
    new AtomicInteger(Integer.MIN_VALUE);
  protected AtomicInteger federateCount = new AtomicInteger(Short.MIN_VALUE);
  protected AtomicInteger regionCount = new AtomicInteger(Short.MIN_VALUE);

  protected final Logger log = LoggerFactory.getLogger(getClass());
  protected final Marker marker;

  public FederationExecution(String name, FDD fdd)
  {
    this.name = name;
    this.fdd = fdd;

    marker = MarkerFactory.getMarker(name);
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

  public Lock getFederatesLock()
  {
    return federatesLock;
  }

  public Map<FederateHandle, FederateProxy> getFederates()
  {
    return federates;
  }

  public void destroy()
    throws FederatesCurrentlyJoined
  {
  }

  public FederateProxy getFederate(FederateHandle federateHandle)
  {
    federatesLock.lock();
    try
    {
      return federates.get(federateHandle);
    }
    finally
    {
      federatesLock.unlock();
    }
  }

  public void joinFederationExecution(
    IoSession session, JoinFederationExecution joinFederationExecution)
  {
    log.debug(marker, "client joining: {}", session.getRemoteAddress());

    federationExecutionStateLock.readLock().lock();
    try
    {
      checkIfSaveInProgress();
      checkIfRestoreInProgress();

      // get the next federate handle
      //
      FederateHandle federateHandle = nextFederateHandle();

      federatesLock.lock();
      try
      {
        if (timeManager != null)
        {
          // TODO: ensure each federate has the same mobile federate services
        }
        else
        {
          // use the first federate's mobile services
          //
          timeManager = new FederationExecutionTimeManager(
            this, joinFederationExecution.getMobileFederateServices());
        }

        FederateProxy federateProxy = new FederateProxy(
          federateHandle, joinFederationExecution.getFederateType(),
          session, this, timeManager.getGALT());

        federates.put(federateHandle, federateProxy);

        WriteFuture writeFuture = session.write(new DefaultResponse(
          joinFederationExecution.getId(),
          new JoinFederationExecutionResponse(
            federateHandle, fdd, timeManager.getGALT())));

        // TODO: set timeout
        //
        writeFuture.join();
      }
      finally
      {
        federatesLock.unlock();
      }
    }
    catch (SaveInProgress sip)
    {
      session.write(new DefaultResponse(joinFederationExecution.getId(), sip));
    }
    catch (RestoreInProgress rip)
    {
      session.write(new DefaultResponse(joinFederationExecution.getId(), rip));
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void resignFederationExecution(
    FederateProxy federateProxy, ResignFederationExecution resignFederationExecution)
  {
    log.debug(marker, "federate resigning: {} - {}", federateProxy,
              resignFederationExecution.getResignAction());

    federationExecutionStateLock.readLock().lock();
    try
    {
      federatesLock.lock();
      try
      {
        federateProxy.resignFederationExecution(
          resignFederationExecution.getResignAction());

        federates.remove(federateProxy.getFederateHandle());
      }
      finally
      {
        federatesLock.unlock();
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void registerFederationSynchronizationPoint(
    FederateProxy federateProxy,
    RegisterFederationSynchronizationPoint registerFederationSynchronizationPoint)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      synchronizationPointsLock.lock();
      try
      {
        FederationExecutionSynchronizationPoint federationExecutionSynchronizationPoint =
          synchronizationPoints.get(
            registerFederationSynchronizationPoint.getLabel());
        if (federationExecutionSynchronizationPoint != null)
        {
          federateProxy.getSession().write(
            new SynchronizationPointRegistrationFailed(
              registerFederationSynchronizationPoint.getLabel(),
              SynchronizationPointFailureReason.SYNCHRONIZATION_POINT_LABEL_NOT_UNIQUE));
        }
        else
        {
          Set<FederateHandle> federateHandles =
            registerFederationSynchronizationPoint.getFederateHandles();

          federatesLock.lock();
          try
          {
            if (federateHandles == null || federateHandles.isEmpty())
            {
              // assign the currently joined federates
              //
              federateHandles = new HashSet<FederateHandle>(federates.keySet());
            }

            // verify all the federates in the set are joined
            //
            if (!federates.keySet().containsAll(federateHandles))
            {
              federateProxy.getSession().write(
                new SynchronizationPointRegistrationFailed(
                  registerFederationSynchronizationPoint.getLabel(),
                  SynchronizationPointFailureReason.SYNCHRONIZATION_SET_MEMBER_NOT_JOINED));
            }
            else
            {
              synchronizationPoints.put(
                registerFederationSynchronizationPoint.getLabel(),
                new FederationExecutionSynchronizationPoint(
                  registerFederationSynchronizationPoint.getLabel(),
                  registerFederationSynchronizationPoint.getTag(),
                  federateHandles));

              federateProxy.getSession().write(
                new SynchronizationPointRegistrationSucceeded(
                  registerFederationSynchronizationPoint.getLabel()));

              AnnounceSynchronizationPoint announceSynchronizationPoint =
                new AnnounceSynchronizationPoint(
                  registerFederationSynchronizationPoint.getLabel(),
                  registerFederationSynchronizationPoint.getTag());
              for (FederateProxy f : federates.values())
              {
                f.announceSynchronizationPoint(announceSynchronizationPoint);
              }
            }
          }
          finally
          {
            federatesLock.unlock();
          }
        }
      }
      finally
      {
        synchronizationPointsLock.unlock();
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void synchronizationPointAchieved(
    FederateProxy federateProxy,
    SynchronizationPointAchieved synchronizationPointAchieved)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      synchronizationPointsLock.lock();
      try
      {
        FederationExecutionSynchronizationPoint federationExecutionSynchronizationPoint =
          synchronizationPoints.get(synchronizationPointAchieved.getLabel());
        assert federationExecutionSynchronizationPoint != null;

        if (federationExecutionSynchronizationPoint.synchronizationPointAchieved(
          federateProxy.getFederateHandle()))
        {
          FederationSynchronized federationSynchronized =
            new FederationSynchronized(
              federationExecutionSynchronizationPoint.getLabel());
          federatesLock.lock();
          try
          {
            for (FederateProxy f : federates.values())
            {
              f.getSession().write(federationSynchronized);
            }
          }
          finally
          {
            federatesLock.unlock();
          }
        }
      }
      finally
      {
        synchronizationPointsLock.unlock();
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void requestFederationSave(FederateProxy federateProxy,
                                    RequestFederationSave requestFederationSave)
  {
    federationExecutionStateLock.writeLock().lock();
    try
    {
      checkIfSaveInProgress();
      checkIfRestoreInProgress();

      if (federationExecutionSave != null)
      {
        log.info("replacing federation execution save: {}",
                 federationExecutionSave.getLabel());
      }

      federationExecutionSave = new FederationExecutionSave(
        requestFederationSave.getLabel(), requestFederationSave.getTime());

      if (requestFederationSave.getTime() != null)
      {
        // TODO: check time

        // tell the federate that the request is going to be honored
        //
        federateProxy.getSession().write(null);

        // TODO: schedule save
      }
      else
      {
        // tell the federate that the request is going to be honored
        //
        federateProxy.getSession().write(null);

        // this is a psuedo save-in-progress... it will only prevent joins
        // and new requests to save/restore
        //
        federationExecutionState = FederationExecutionState.SAVE_IN_PROGRESS;

        InitiateFederateSave initiateFederateSave =
          new InitiateFederateSave(requestFederationSave.getLabel());

        federatesLock.lock();
        try
        {
          // track who was instructed to save
          //
          federationExecutionSave.instructedToSave(federates.keySet());

          // notify all federates to initiate save
          //
          for (FederateProxy f : federates.values())
          {
            f.initiateFederateSave(initiateFederateSave);
          }
        }
        finally
        {
          federatesLock.unlock();
        }
      }
    }
    catch (SaveInProgress sip)
    {
      federateProxy.getSession().write(
        new DefaultResponse(requestFederationSave.getId(), sip));
    }
    catch (RestoreInProgress rip)
    {
      federateProxy.getSession().write(
        new DefaultResponse(requestFederationSave.getId(), rip));
    }
    finally
    {
      federationExecutionStateLock.writeLock().unlock();
    }
  }

  public void federateSaveInitiated(
    FederateProxy federateProxy, FederateSaveInitiated federateSaveInitiated)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      assert federationExecutionSave != null;

      federationExecutionSave.federateSaveInitiated(
        federateProxy.getFederateHandle());

      federateProxy.federateSaveInitiated(federateSaveInitiated);
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void federateSaveInitiatedFailed(
    FederateProxy federateProxy, FederateSaveInitiatedFailed federateSaveInitiatedFailed)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      assert federationExecutionSave != null;

      federationExecutionSave.federateSaveInitiatedFailed(
        federateProxy.getFederateHandle(), federateSaveInitiatedFailed.getCause());

      federateProxy.federateSaveInitiatedFailed(federateSaveInitiatedFailed);
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void federateSaveBegun(
    FederateProxy federateProxy, FederateSaveBegun federateSaveBegun)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      assert federationExecutionSave != null;

      federationExecutionSave.federateSaveBegun(federateProxy.getFederateHandle());

      federateProxy.federateSaveBegun(federateSaveBegun);
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void federateSaveComplete(
    FederateProxy federateProxy, FederateSaveComplete federateSaveComplete)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      assert federationExecutionSave != null;

      federateProxy.federateSaveComplete(federateSaveComplete);

      if (federationExecutionSave.federateSaveComplete(
        federateProxy.getFederateHandle(), federateSaveComplete.getFederateSave()))
      {
        // upgrade to write lock
        //
        federationExecutionStateLock.readLock().unlock();
        federationExecutionStateLock.writeLock().lock();
        try
        {
          FederationSaved federationSaved = new FederationSaved();

          federatesLock.lock();
          try
          {
            for (FederateProxy f : federates.values())
            {
              f.federationSaved(federationSaved);
            }

            federationExecutionState = FederationExecutionState.ACTIVE;
          }
          finally
          {
            federatesLock.unlock();
          }
        }
        finally
        {
          // downgrade to read lock
          //
          federationExecutionStateLock.readLock().lock();
          federationExecutionStateLock.writeLock().unlock();
        }
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void federateSaveNotComplete(
    FederateProxy federateProxy, FederateSaveNotComplete federateSaveNotComplete)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      assert federationExecutionSave != null;

      federateProxy.federateSaveNotComplete(federateSaveNotComplete);

      if (federationExecutionSave.federateSaveNotComplete(
        federateProxy.getFederateHandle()))
      {
        // upgrade to write lock
        //
        federationExecutionStateLock.readLock().unlock();
        federationExecutionStateLock.writeLock().lock();
        try
        {
          FederationNotSaved federationNotSaved = new FederationNotSaved(
            federationExecutionSave.getSaveFailureReason());

          federatesLock.lock();
          try
          {
            for (FederateProxy f : federates.values())
            {
              f.federationNotSaved(federationNotSaved);
            }

            federationExecutionState = FederationExecutionState.ACTIVE;
          }
          finally
          {
            federatesLock.unlock();
          }
        }
        finally
        {
          // downgrade to read lock
          //
          federationExecutionStateLock.readLock().lock();
          federationExecutionStateLock.writeLock().unlock();
        }
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void queryFederationSaveStatus(
    FederateProxy federateProxy, QueryFederationSaveStatus queryFederationSaveStatus)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federationExecutionSave != null)
      {
        federateProxy.getSession().write(new FederationSaveStatusResponse(
          federationExecutionSave.getFederationSaveStatus()));
      }
      else
      {
        federatesLock.lock();
        try
        {
          FederateHandleSaveStatusPair[] federationSaveStatus =
            new FederateHandleSaveStatusPair[federates.size()];
          int i = 0;
          for (FederateHandle federateHandle : federates.keySet())
          {
            federationSaveStatus[i++] = new FederateHandleSaveStatusPair(
              federateHandle, SaveStatus.NO_SAVE_IN_PROGRESS);
          }

          federateProxy.getSession().write(
            new FederationSaveStatusResponse(federationSaveStatus));
        }
        finally
        {
          federatesLock.unlock();
        }
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void requestFederationRestore(
    FederateProxy federateProxy, RequestFederationRestore requestFederationRestore)
  {
    federationExecutionStateLock.writeLock().lock();
    try
    {
      checkIfSaveInProgress();
      checkIfRestoreInProgress();

      // TODO: locate the saved information

      federatesLock.lock();
      try
      {
        Set<FederateHandle> federateHandles =
          new HashSet<FederateHandle>(federates.keySet());

        // TODO: determine if the same number of federate types are joined

        federationExecutionRestore = new FederationExecutionRestore(
          requestFederationRestore.getLabel(), federateHandles);

        // this is a psuedo restore-in-progress... it will only prevent joins
        // and new requests to save/restore
        //
        federationExecutionState = FederationExecutionState.RESTORE_IN_PROGRESS;

        // notify all federates to initiate restore
        //
        for (FederateProxy f : federates.values())
        {
//          f.initiateFederateRestore(initiateFederateSave);
        }
      }
      finally
      {
        federatesLock.unlock();
      }

      federateProxy.getSession().write(
        new DefaultResponse(requestFederationRestore.getId()));
    }
    catch (SaveInProgress sip)
    {
      federateProxy.getSession().write(
        new DefaultResponse(requestFederationRestore.getId(), sip));
    }
    catch (RestoreInProgress rip)
    {
      federateProxy.getSession().write(
        new DefaultResponse(requestFederationRestore.getId(), rip));
    }
    finally
    {
      federationExecutionStateLock.writeLock().unlock();
    }
  }

  public void federateRestoreComplete(
    FederateProxy federateProxy, FederateRestoreComplete federateRestoreComplete)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      federateProxy.federateRestoreComplete(federateRestoreComplete);

      if (federationExecutionRestore.federateRestoreComplete(
        federateProxy.getFederateHandle()))
      {
        federationExecutionState = FederationExecutionState.ACTIVE;

        // TODO: federation restored
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void federateRestoreNotComplete(
    FederateProxy federateProxy, FederateRestoreNotComplete federateRestoreNotComplete)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      federateProxy.federateRestoreNotComplete(federateRestoreNotComplete);

      if (federationExecutionRestore.federateRestoreNotComplete(
        federateProxy.getFederateHandle()))
      {
        federationExecutionState = FederationExecutionState.ACTIVE;

        // TODO: federation not restored
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void queryFederationRestoreStatus(
    FederateProxy federateProxy,
    QueryFederationRestoreStatus queryFederationRestoreStatus)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federationExecutionRestore != null)
      {
        federateProxy.getSession().write(new FederationRestoreStatusResponse(
          federationExecutionRestore.getFederationRestoreStatus()));
      }
      else
      {
        federatesLock.lock();
        try
        {
          FederateHandleRestoreStatusPair[] federationRestoreStatus =
            new FederateHandleRestoreStatusPair[federates.size()];
          int i = 0;
          for (FederateHandle federateHandle : federates.keySet())
          {
            federationRestoreStatus[i++] = new FederateHandleRestoreStatusPair(
              federateHandle, RestoreStatus.NO_RESTORE_IN_PROGRESS);
          }
          federateProxy.getSession().write(new FederationRestoreStatusResponse(
            federationRestoreStatus));
        }
        finally
        {
          federatesLock.unlock();
        }
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void reserveObjectInstanceName(
    FederateProxy federateProxy, ReserveObjectInstanceName reserveObjectInstanceName)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      objectManager.reserveObjectInstanceName(
        federateProxy, reserveObjectInstanceName.getName());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void registerObjectInstance(
    FederateProxy federateProxy, RegisterObjectInstance registerObjectInstance)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      ObjectInstanceHandle objectInstanceHandle =
        objectManager.registerObjectInstance(
          federateProxy, registerObjectInstance.getObjectClassHandle(),
          registerObjectInstance.getPublishedAttributeHandles(),
          registerObjectInstance.getName());

      // notify the registering federate of the new object instance handle
      //
      federateProxy.getSession().write(new DefaultResponse(
        registerObjectInstance.getId(), objectInstanceHandle));

      DiscoverObjectInstance discoverObjectInstance =
        new DiscoverObjectInstance(
          objectInstanceHandle, registerObjectInstance.getObjectClassHandle(),
          registerObjectInstance.getName());

      federatesLock.lock();
      try
      {
        for (FederateProxy f : federates.values())
        {
          if (f != federateProxy)
          {
            f.discoverObjectInstance(discoverObjectInstance);
          }
        }
      }
      finally
      {
        federatesLock.unlock();
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void updateAttributeValues(
    FederateProxy federateProxy, UpdateAttributeValues updateAttributeValues)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      objectManager.updateAttributeValues(
        federateProxy, updateAttributeValues.getObjectInstanceHandle(),
          updateAttributeValues.getAttributeValues(),
          updateAttributeValues.getTag(),
          updateAttributeValues.getSentRegionHandles(),
          updateAttributeValues.getSentOrderType(),
          updateAttributeValues.getTransportationType(),
          updateAttributeValues.getUpdateTime(),
          updateAttributeValues.getMessageRetractionHandle());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void sendInteraction(
    FederateProxy federateProxy, SendInteraction sendInteraction)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (sendInteraction.getSentOrderType() == OrderType.TIMESTAMP)
      {
        // TODO: track for future federates
      }

      ReceiveInteraction receiveInteraction =
        new ReceiveInteraction(
          sendInteraction.getInteractionClassHandle(),
          sendInteraction.getParameterValues(), sendInteraction.getTag(),
          sendInteraction.getSentOrderType(),
          sendInteraction.getTransportationType(),
          sendInteraction.getSendTime(),
          sendInteraction.getMessageRetractionHandle(),
          sendInteraction.getSentRegionHandles());

      federatesLock.lock();
      try
      {
        for (FederateProxy f : federates.values())
        {
          if (f != federateProxy)
          {
            f.receiveInteraction(receiveInteraction);
          }
        }
      }
      finally
      {
        federatesLock.unlock();
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void deleteObjectInstance(
    FederateProxy federateProxy, DeleteObjectInstance deleteObjectInstance)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (deleteObjectInstance.getSentOrderType() == OrderType.TIMESTAMP)
      {
        // TODO: track for future federates
      }

      RemoveObjectInstance removeObjectInstance =
        new RemoveObjectInstance(
          deleteObjectInstance.getObjectInstanceHandle(),
          deleteObjectInstance.getTag(),
          deleteObjectInstance.getSentOrderType(),
          deleteObjectInstance.getDeleteTime(),
          deleteObjectInstance.getMessageRetractionHandle());

      federatesLock.lock();
      try
      {
        for (FederateProxy f : federates.values())
        {
          if (f != federateProxy)
          {
            f.removeObjectInstance(removeObjectInstance);
          }
        }
      }
      finally
      {
        federatesLock.unlock();
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void requestAttributeValueUpdate(
    FederateProxy federateProxy, RequestAttributeValueUpdate requestAttributeValueUpdate)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      federatesLock.lock();
      try
      {
        for (FederateProxy f : federates.values())
        {
          if (f != federateProxy)
          {
            f.requestAttributeValueUpdate(requestAttributeValueUpdate);
          }
        }
      }
      finally
      {
        federatesLock.unlock();
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void retract(FederateProxy federateProxy, Retract retract)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      federatesLock.lock();
      try
      {
        for (FederateProxy f : federates.values())
        {
          if (f != federateProxy)
          {
            f.retract(retract);
          }
        }
      }
      finally
      {
        federatesLock.unlock();
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void subscribeObjectClassAttributes(
    FederateProxy federateProxy,
    SubscribeObjectClassAttributes subscribeObjectClassAttributes)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      ObjectClass objectClass =
        fdd.getObjectClasses().get(
          subscribeObjectClassAttributes.getObjectClassHandle());
      assert objectClass != null;

      objectManager.subscribeObjectClassAttributes(
        federateProxy, objectClass,
        subscribeObjectClassAttributes.getAttributeHandles(),
        subscribeObjectClassAttributes.getAttributesAndRegions());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void unconditionalAttributeOwnershipDivestiture(
    FederateProxy federateProxy,
    UnconditionalAttributeOwnershipDivestiture unconditionalAttributeOwnershipDivestiture)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      objectManager.unconditionalAttributeOwnershipDivestiture(
        federateProxy,
        unconditionalAttributeOwnershipDivestiture.getObjectInstanceHandle(),
        unconditionalAttributeOwnershipDivestiture.getAttributeHandles());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void negotiatedAttributeOwnershipDivestiture(
    FederateProxy federateProxy,
    NegotiatedAttributeOwnershipDivestiture negotiatedAttributeOwnershipDivestiture)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      objectManager.negotiatedAttributeOwnershipDivestiture(
        federateProxy,
        negotiatedAttributeOwnershipDivestiture.getObjectInstanceHandle(),
        negotiatedAttributeOwnershipDivestiture.getAttributeHandles(),
        negotiatedAttributeOwnershipDivestiture.getTag());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void confirmDivestiture(
    FederateProxy federateProxy, ConfirmDivestiture confirmDivestiture)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      objectManager.confirmDivestiture(
        federateProxy, confirmDivestiture.getObjectInstanceHandle(),
        confirmDivestiture.getAttributeHandles());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisition(
    FederateProxy federateProxy,
    AttributeOwnershipAcquisition attributeOwnershipAcquisition)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      objectManager.attributeOwnershipAcquisition(
        federateProxy, attributeOwnershipAcquisition.getObjectInstanceHandle(),
        attributeOwnershipAcquisition.getAttributeHandles(),
        attributeOwnershipAcquisition.getTag());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisitionIfAvailable(
    FederateProxy federateProxy,
    AttributeOwnershipAcquisitionIfAvailable attributeOwnershipAcquisitionIfAvailable)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      objectManager.attributeOwnershipAcquisitionIfAvailable(
        federateProxy,
        attributeOwnershipAcquisitionIfAvailable.getObjectInstanceHandle(),
        attributeOwnershipAcquisitionIfAvailable.getAttributeHandles());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void attributeOwnershipDivestitureIfWanted(
    FederateProxy federateProxy,
    AttributeOwnershipDivestitureIfWanted attributeOwnershipDivestitureIfWanted)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      Map<AttributeHandle, FederateProxy> newOwners =
        objectManager.attributeOwnershipDivestitureIfWanted(
          federateProxy,
          attributeOwnershipDivestitureIfWanted.getObjectInstanceHandle(),
          attributeOwnershipDivestitureIfWanted.getAttributeHandles());

      // notify the divestee what attributes were divested
      //
      AttributeOwnershipDivestitureIfWantedResponse
        attributeOwnershipDivestitureIfWantedResponse =
        new AttributeOwnershipDivestitureIfWantedResponse(
          new IEEE1516AttributeHandleSet(newOwners.keySet()));

      RequestResponse requestResponse = new RequestResponse(
        attributeOwnershipDivestitureIfWanted.getId(),
        attributeOwnershipDivestitureIfWantedResponse);
      WriteFuture writeFuture = federateProxy.getSession().write(requestResponse);

      // TODO: set timeout
      //
      writeFuture.join();

      if (writeFuture.isWritten())
      {
        try
        {
          // TODO: set timeout
          //
          attributeOwnershipDivestitureIfWantedResponse.awaitUninterruptibly();
        }
        catch (ExecutionException ee)
        {
          log.warn("did not receive reply", ee);
        }
      }

      // divide up the divested attributes by owner
      //
      Map<FederateProxy, AttributeHandleSet> newOwnerAcquisitions =
        new HashMap<FederateProxy, AttributeHandleSet>();
      for (Map.Entry<AttributeHandle, FederateProxy> entry : newOwners.entrySet())
      {
        AttributeHandleSet acquiredAttributeHandles =
          newOwnerAcquisitions.get(entry.getValue());
        if (acquiredAttributeHandles == null)
        {
          acquiredAttributeHandles = new IEEE1516AttributeHandleSet();
          newOwnerAcquisitions.put(entry.getValue(), acquiredAttributeHandles);
        }
        acquiredAttributeHandles.add(entry.getKey());
      }

      // notify the new owners
      //
      for (Map.Entry<FederateProxy, AttributeHandleSet> entry :
        newOwnerAcquisitions.entrySet())
      {
        entry.getKey().getSession().write(
          new AttributeOwnershipAcquisitionNotification(
            attributeOwnershipDivestitureIfWanted.getObjectInstanceHandle(),
            entry.getValue()));
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
      objectManager.cancelNegotiatedAttributeOwnershipDivestiture(
        federateProxy,
        cancelNegotiatedAttributeOwnershipDivestiture.getObjectInstanceHandle(),
        cancelNegotiatedAttributeOwnershipDivestiture.getAttributeHandles());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void cancelAttributeOwnershipAcquisition(
    FederateProxy federateProxy,
    CancelAttributeOwnershipAcquisition cancelAttributeOwnershipAcquisition)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      objectManager.cancelAttributeOwnershipAcquisition(
        federateProxy, cancelAttributeOwnershipAcquisition.getObjectInstanceHandle(),
        cancelAttributeOwnershipAcquisition.getAttributeHandles());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void queryAttributeOwnership(
    FederateProxy federateProxy, QueryAttributeOwnership queryAttributeOwnership)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      objectManager.queryAttributeOwnership(
        federateProxy, queryAttributeOwnership.getObjectInstanceHandle(),
        queryAttributeOwnership.getAttributeHandle());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void enableTimeRegulation(
    FederateProxy federateProxy, EnableTimeRegulation enableTimeRegulation)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      timeManager.enableTimeRegulation(
        federateProxy, enableTimeRegulation.getLookahead());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void disableTimeRegulation(
    FederateProxy federateProxy, DisableTimeRegulation disableTimeRegulation)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      timeManager.disableTimeRegulation(federateProxy);
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void enableTimeConstrained(
    FederateProxy federateProxy, EnableTimeConstrained enableTimeConstrained)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      timeManager.enableTimeConstrained(federateProxy);
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void disableTimeConstrained(
    FederateProxy federateProxy, DisableTimeConstrained disableTimeConstrained)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      timeManager.disableTimeConstrained(federateProxy);
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void modifyLookahead(
    FederateProxy federateProxy, ModifyLookahead modifyLookahead)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      timeManager.modifyLookahead(
        federateProxy, modifyLookahead.getLookahead());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void timeAdvanceRequest(
    FederateProxy federateProxy, TimeAdvanceRequest timeAdvanceRequest)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      timeManager.timeAdvanceRequest(federateProxy, timeAdvanceRequest.getTime());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void timeAdvanceRequestAvailable(
    FederateProxy federateProxy, TimeAdvanceRequestAvailable timeAdvanceRequestAvailable)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      timeManager.timeAdvanceRequestAvailable(
        federateProxy, timeAdvanceRequestAvailable.getTime());
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
      regionsLock.readLock().lock();
      try
      {
        for (Map.Entry<RegionHandle, Map<DimensionHandle, RangeBounds>> entry :
          commitRegionModifications.getRegionModifications().entrySet())
        {
          regions.get(entry.getKey()).putAll(entry.getValue());
        }
      }
      finally
      {
        regionsLock.readLock().unlock();
      }

      federateProxy.getSession().write(new DefaultResponse(commitRegionModifications.getId()));
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void getRangeBounds(FederateProxy federateProxy, GetRangeBounds getRangeBounds)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      RangeBounds rangeBounds;

      regionsLock.readLock().lock();
      try
      {
        rangeBounds = regions.get(getRangeBounds.getRegionHandle()).get(
          getRangeBounds.getDimensionHandle());
      }
      finally
      {
        regionsLock.readLock().unlock();
      }

      federateProxy.getSession().write(new DefaultResponse(getRangeBounds.getId(), rangeBounds));
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void createRegion(FederateProxy federateProxy, CreateRegion createRegion)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      RegionHandle regionHandle = nextRegionHandle();

      regionsLock.writeLock().lock();
      try
      {
        Map<DimensionHandle, RangeBounds> rangeBounds =
          new ConcurrentHashMap<DimensionHandle, RangeBounds>();
        for (DimensionHandle dimensionHandle : createRegion.getDimensionHandles())
        {
          Dimension dimension = fdd.getDimensions().get(dimensionHandle);
          assert dimension != null;

          rangeBounds.put(dimensionHandle, dimension.getRangeBounds());
        }
        regions.put(regionHandle, rangeBounds);
      }
      finally
      {
        regionsLock.writeLock().unlock();
      }

      federateProxy.getSession().write(new DefaultResponse(createRegion.getId(), regionHandle));
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
      regionsLock.writeLock().lock();
      try
      {
        regions.remove(deleteRegion.getRegionHandle());
      }
      finally
      {
        regionsLock.writeLock().unlock();
      }

      federateProxy.getSession().write(new DefaultResponse(deleteRegion.getId()));
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void checkIfSaveInProgress()
    throws SaveInProgress
  {
    if (federationExecutionState == FederationExecutionState.SAVE_IN_PROGRESS)
    {
      throw new SaveInProgress();
    }
  }

  protected void checkIfRestoreInProgress()
    throws RestoreInProgress
  {
    if (federationExecutionState == FederationExecutionState
      .RESTORE_IN_PROGRESS)
    {
      throw new RestoreInProgress();
    }
  }

  protected FederateHandle nextFederateHandle()
  {
    return new IEEE1516FederateHandle(federateCount.incrementAndGet());
  }

  protected RegionHandle nextRegionHandle()
  {
    return new IEEE1516RegionHandle(regionCount.incrementAndGet());
  }
}
