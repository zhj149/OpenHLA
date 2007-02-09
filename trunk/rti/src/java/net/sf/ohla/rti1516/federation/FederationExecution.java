package net.sf.ohla.rti1516.federation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti1516.OHLAAttributeHandleSet;
import net.sf.ohla.rti1516.OHLAFederateHandle;
import net.sf.ohla.rti1516.OHLARegionHandle;
import net.sf.ohla.rti1516.fdd.Dimension;
import net.sf.ohla.rti1516.fdd.FDD;
import net.sf.ohla.rti1516.fdd.ObjectClass;
import net.sf.ohla.rti1516.federation.objects.ObjectManager;
import net.sf.ohla.rti1516.federation.time.TimeKeeper;
import net.sf.ohla.rti1516.messages.AttributeOwnershipAcquisition;
import net.sf.ohla.rti1516.messages.AttributeOwnershipAcquisitionIfAvailable;
import net.sf.ohla.rti1516.messages.AttributeOwnershipDivestitureIfWanted;
import net.sf.ohla.rti1516.messages.AttributeOwnershipDivestitureIfWantedResponse;
import net.sf.ohla.rti1516.messages.CancelAttributeOwnershipAcquisition;
import net.sf.ohla.rti1516.messages.CancelNegotiatedAttributeOwnershipDivestiture;
import net.sf.ohla.rti1516.messages.CommitRegionModifications;
import net.sf.ohla.rti1516.messages.ConfirmDivestiture;
import net.sf.ohla.rti1516.messages.CreateRegion;
import net.sf.ohla.rti1516.messages.DefaultResponse;
import net.sf.ohla.rti1516.messages.DeleteObjectInstance;
import net.sf.ohla.rti1516.messages.DeleteRegion;
import net.sf.ohla.rti1516.messages.DisableTimeConstrained;
import net.sf.ohla.rti1516.messages.DisableTimeRegulation;
import net.sf.ohla.rti1516.messages.EnableTimeConstrained;
import net.sf.ohla.rti1516.messages.EnableTimeRegulation;
import net.sf.ohla.rti1516.messages.FederateRestoreComplete;
import net.sf.ohla.rti1516.messages.FederateRestoreNotComplete;
import net.sf.ohla.rti1516.messages.FederateSaveBegun;
import net.sf.ohla.rti1516.messages.FederateSaveComplete;
import net.sf.ohla.rti1516.messages.FederateSaveInitiated;
import net.sf.ohla.rti1516.messages.FederateSaveInitiatedFailed;
import net.sf.ohla.rti1516.messages.FederateSaveNotComplete;
import net.sf.ohla.rti1516.messages.GetRangeBounds;
import net.sf.ohla.rti1516.messages.JoinFederationExecution;
import net.sf.ohla.rti1516.messages.JoinFederationExecutionResponse;
import net.sf.ohla.rti1516.messages.NegotiatedAttributeOwnershipDivestiture;
import net.sf.ohla.rti1516.messages.QueryAttributeOwnership;
import net.sf.ohla.rti1516.messages.QueryFederationRestoreStatus;
import net.sf.ohla.rti1516.messages.QueryFederationSaveStatus;
import net.sf.ohla.rti1516.messages.RegisterFederationSynchronizationPoint;
import net.sf.ohla.rti1516.messages.RegisterObjectInstance;
import net.sf.ohla.rti1516.messages.RequestAttributeValueUpdate;
import net.sf.ohla.rti1516.messages.RequestFederationRestore;
import net.sf.ohla.rti1516.messages.RequestFederationSave;
import net.sf.ohla.rti1516.messages.RequestResponse;
import net.sf.ohla.rti1516.messages.ReserveObjectInstanceName;
import net.sf.ohla.rti1516.messages.ResignFederationExecution;
import net.sf.ohla.rti1516.messages.Retract;
import net.sf.ohla.rti1516.messages.SendInteraction;
import net.sf.ohla.rti1516.messages.SubscribeObjectClassAttributes;
import net.sf.ohla.rti1516.messages.SynchronizationPointAchieved;
import net.sf.ohla.rti1516.messages.TimeAdvanceRequest;
import net.sf.ohla.rti1516.messages.TimeAdvanceRequestAvailable;
import net.sf.ohla.rti1516.messages.UnconditionalAttributeOwnershipDivestiture;
import net.sf.ohla.rti1516.messages.UpdateAttributeValues;
import net.sf.ohla.rti1516.messages.callbacks.AnnounceSynchronizationPoint;
import net.sf.ohla.rti1516.messages.callbacks.AttributeOwnershipAcquisitionNotification;
import net.sf.ohla.rti1516.messages.callbacks.DiscoverObjectInstance;
import net.sf.ohla.rti1516.messages.callbacks.FederationNotSaved;
import net.sf.ohla.rti1516.messages.callbacks.FederationRestoreStatusResponse;
import net.sf.ohla.rti1516.messages.callbacks.FederationSaveStatusResponse;
import net.sf.ohla.rti1516.messages.callbacks.FederationSaved;
import net.sf.ohla.rti1516.messages.callbacks.FederationSynchronized;
import net.sf.ohla.rti1516.messages.callbacks.InitiateFederateSave;
import net.sf.ohla.rti1516.messages.callbacks.ReceiveInteraction;
import net.sf.ohla.rti1516.messages.callbacks.RemoveObjectInstance;
import net.sf.ohla.rti1516.messages.callbacks.SynchronizationPointRegistrationFailed;
import net.sf.ohla.rti1516.messages.callbacks.SynchronizationPointRegistrationSucceeded;

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
  protected Map<FederateHandle, Federate> federates =
    new HashMap<FederateHandle, Federate>();

  protected Lock synchronizationPointsLock = new ReentrantLock(true);
  protected Map<String, FederationExecutionSynchronizationPoint> synchronizationPoints =
    new HashMap<String, FederationExecutionSynchronizationPoint>();

  protected ReadWriteLock regionsLock = new ReentrantReadWriteLock(true);
  protected Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regions =
    new HashMap<RegionHandle, Map<DimensionHandle, RangeBounds>>();

  protected ObjectManager objectManager = new ObjectManager(this);

  protected TimeKeeper timeKeeper;

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

  public Lock getFederatesLock()
  {
    return federatesLock;
  }

  public Map<FederateHandle, Federate> getFederates()
  {
    return federates;
  }

  public void destroy()
    throws FederatesCurrentlyJoined
  {
  }

  public Federate getFederate(FederateHandle federateHandle)
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
        if (timeKeeper != null)
        {
          // TODO: ensure each federate has the same mobile federate services
        }
        else
        {
          // use the first federate's mobile services
          //
          timeKeeper = new TimeKeeper(
            this, joinFederationExecution.getMobileFederateServices());
        }

        Federate federate = new Federate(
          federateHandle, joinFederationExecution.getFederateType(),
          session, this);

        federates.put(federateHandle, federate);

        WriteFuture writeFuture = session.write(new DefaultResponse(
          joinFederationExecution.getId(),
          new JoinFederationExecutionResponse(
            federateHandle, fdd, timeKeeper.getGALT())));

        log.debug(marker, "federate joined: {}", federate);

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
    Federate federate, ResignFederationExecution resignFederationExecution)
  {
    log.debug(marker, "federate resigning: {} - {}", federate,
              resignFederationExecution.getResignAction());

    federationExecutionStateLock.readLock().lock();
    try
    {
      federatesLock.lock();
      try
      {
        federate.resignFederationExecution();

        federates.remove(federate.getFederateHandle());

        log.debug(marker, "federate resigned: {}", federate);
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
    Federate federate,
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
          federate.getSession().write(
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
              federate.getSession().write(
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

              federate.getSession().write(
                new SynchronizationPointRegistrationSucceeded(
                  registerFederationSynchronizationPoint.getLabel()));

              AnnounceSynchronizationPoint announceSynchronizationPoint =
                new AnnounceSynchronizationPoint(
                  registerFederationSynchronizationPoint.getLabel(),
                  registerFederationSynchronizationPoint.getTag());
              for (Federate f : federates.values())
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
    Federate federate,
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
          federate.getFederateHandle()))
        {
          FederationSynchronized federationSynchronized =
            new FederationSynchronized(
              federationExecutionSynchronizationPoint.getLabel());
          federatesLock.lock();
          try
          {
            for (Federate f : federates.values())
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

  public void requestFederationSave(Federate federate,
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
        federate.getSession().write(null);

        // TODO: schedule save
      }
      else
      {
        // tell the federate that the request is going to be honored
        //
        federate.getSession().write(null);

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
          for (Federate f : federates.values())
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
      federate.getSession().write(
        new DefaultResponse(requestFederationSave.getId(), sip));
    }
    catch (RestoreInProgress rip)
    {
      federate.getSession().write(
        new DefaultResponse(requestFederationSave.getId(), rip));
    }
    finally
    {
      federationExecutionStateLock.writeLock().unlock();
    }
  }

  public void federateSaveInitiated(
    Federate federate, FederateSaveInitiated federateSaveInitiated)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      assert federationExecutionSave != null;

      federationExecutionSave.federateSaveInitiated(
        federate.getFederateHandle());

      federate.federateSaveInitiated(federateSaveInitiated);
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void federateSaveInitiatedFailed(
    Federate federate, FederateSaveInitiatedFailed federateSaveInitiatedFailed)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      assert federationExecutionSave != null;

      federationExecutionSave.federateSaveInitiatedFailed(
        federate.getFederateHandle(), federateSaveInitiatedFailed.getCause());

      federate.federateSaveInitiatedFailed(federateSaveInitiatedFailed);
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void federateSaveBegun(
    Federate federate, FederateSaveBegun federateSaveBegun)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      assert federationExecutionSave != null;

      federationExecutionSave.federateSaveBegun(federate.getFederateHandle());

      federate.federateSaveBegun(federateSaveBegun);
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void federateSaveComplete(
    Federate federate, FederateSaveComplete federateSaveComplete)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      assert federationExecutionSave != null;

      federate.federateSaveComplete(federateSaveComplete);

      if (federationExecutionSave.federateSaveComplete(
        federate.getFederateHandle(), federateSaveComplete.getFederateSave()))
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
            for (Federate f : federates.values())
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
    Federate federate, FederateSaveNotComplete federateSaveNotComplete)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      assert federationExecutionSave != null;

      federate.federateSaveNotComplete(federateSaveNotComplete);

      if (federationExecutionSave.federateSaveNotComplete(
        federate.getFederateHandle()))
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
            for (Federate f : federates.values())
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
    Federate federate, QueryFederationSaveStatus queryFederationSaveStatus)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federationExecutionSave != null)
      {
        federate.getSession().write(new FederationSaveStatusResponse(
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

          federate.getSession().write(
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
    Federate federate, RequestFederationRestore requestFederationRestore)
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
        for (Federate f : federates.values())
        {
//          f.initiateFederateRestore(initiateFederateSave);
        }
      }
      finally
      {
        federatesLock.unlock();
      }

      federate.getSession().write(
        new DefaultResponse(requestFederationRestore.getId()));
    }
    catch (SaveInProgress sip)
    {
      federate.getSession().write(
        new DefaultResponse(requestFederationRestore.getId(), sip));
    }
    catch (RestoreInProgress rip)
    {
      federate.getSession().write(
        new DefaultResponse(requestFederationRestore.getId(), rip));
    }
    finally
    {
      federationExecutionStateLock.writeLock().unlock();
    }
  }

  public void federateRestoreComplete(
    Federate federate, FederateRestoreComplete federateRestoreComplete)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      federate.federateRestoreComplete(federateRestoreComplete);

      if (federationExecutionRestore.federateRestoreComplete(
        federate.getFederateHandle()))
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
    Federate federate, FederateRestoreNotComplete federateRestoreNotComplete)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      federate.federateRestoreNotComplete(federateRestoreNotComplete);

      if (federationExecutionRestore.federateRestoreNotComplete(
        federate.getFederateHandle()))
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
    Federate federate,
    QueryFederationRestoreStatus queryFederationRestoreStatus)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federationExecutionRestore != null)
      {
        federate.getSession().write(new FederationRestoreStatusResponse(
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
          federate.getSession().write(new FederationRestoreStatusResponse(
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
    Federate federate, ReserveObjectInstanceName reserveObjectInstanceName)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      objectManager.reserveObjectInstanceName(
        federate, reserveObjectInstanceName.getName());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void registerObjectInstance(
    Federate federate, RegisterObjectInstance registerObjectInstance)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      ObjectInstanceHandle objectInstanceHandle =
        objectManager.registerObjectInstance(
          federate, registerObjectInstance.getObjectClassHandle(),
          registerObjectInstance.getPublishedAttributeHandles(),
          registerObjectInstance.getName());

      // notify the registering federate of the new object instance handle
      //
      federate.getSession().write(new DefaultResponse(
        registerObjectInstance.getId(), objectInstanceHandle));

      DiscoverObjectInstance discoverObjectInstance =
        new DiscoverObjectInstance(
          objectInstanceHandle, registerObjectInstance.getObjectClassHandle(),
          registerObjectInstance.getName());

      federatesLock.lock();
      try
      {
        for (Federate f : federates.values())
        {
          if (f != federate)
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
    Federate federate, UpdateAttributeValues updateAttributeValues)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      objectManager.updateAttributeValues(
          federate, updateAttributeValues.getObjectInstanceHandle(),
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
    Federate federate, SendInteraction sendInteraction)
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
        for (Federate f : federates.values())
        {
          if (f != federate)
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
    Federate federate, DeleteObjectInstance deleteObjectInstance)
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
        for (Federate f : federates.values())
        {
          if (f != federate)
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
    Federate federate, RequestAttributeValueUpdate requestAttributeValueUpdate)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      federatesLock.lock();
      try
      {
        for (Federate f : federates.values())
        {
          if (f != federate)
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

  public void retract(Federate federate, Retract retract)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      federatesLock.lock();
      try
      {
        for (Federate f : federates.values())
        {
          if (f != federate)
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
    Federate federate,
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
        federate, objectClass,
        subscribeObjectClassAttributes.getAttributeHandles(),
        subscribeObjectClassAttributes.getAttributesAndRegions());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void unconditionalAttributeOwnershipDivestiture(
    Federate federate,
    UnconditionalAttributeOwnershipDivestiture unconditionalAttributeOwnershipDivestiture)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      objectManager.unconditionalAttributeOwnershipDivestiture(
        federate,
        unconditionalAttributeOwnershipDivestiture.getObjectInstanceHandle(),
        unconditionalAttributeOwnershipDivestiture.getAttributeHandles());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void negotiatedAttributeOwnershipDivestiture(
    Federate federate,
    NegotiatedAttributeOwnershipDivestiture negotiatedAttributeOwnershipDivestiture)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      objectManager.negotiatedAttributeOwnershipDivestiture(
        federate,
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
    Federate federate, ConfirmDivestiture confirmDivestiture)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      objectManager.confirmDivestiture(
        federate, confirmDivestiture.getObjectInstanceHandle(),
        confirmDivestiture.getAttributeHandles());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisition(
    Federate federate,
    AttributeOwnershipAcquisition attributeOwnershipAcquisition)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      objectManager.attributeOwnershipAcquisition(
        federate, attributeOwnershipAcquisition.getObjectInstanceHandle(),
        attributeOwnershipAcquisition.getAttributeHandles(),
        attributeOwnershipAcquisition.getTag());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisitionIfAvailable(
    Federate federate,
    AttributeOwnershipAcquisitionIfAvailable attributeOwnershipAcquisitionIfAvailable)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      objectManager.attributeOwnershipAcquisitionIfAvailable(
        federate,
        attributeOwnershipAcquisitionIfAvailable.getObjectInstanceHandle(),
        attributeOwnershipAcquisitionIfAvailable.getAttributeHandles());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void attributeOwnershipDivestitureIfWanted(
    Federate federate,
    AttributeOwnershipDivestitureIfWanted attributeOwnershipDivestitureIfWanted)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      Map<AttributeHandle, Federate> newOwners =
        objectManager.attributeOwnershipDivestitureIfWanted(
          federate,
          attributeOwnershipDivestitureIfWanted.getObjectInstanceHandle(),
          attributeOwnershipDivestitureIfWanted.getAttributeHandles());

      // notify the divestee what attributes were divested
      //
      AttributeOwnershipDivestitureIfWantedResponse
        attributeOwnershipDivestitureIfWantedResponse =
        new AttributeOwnershipDivestitureIfWantedResponse(
          new OHLAAttributeHandleSet(newOwners.keySet()));

      RequestResponse requestResponse = new RequestResponse(
        attributeOwnershipDivestitureIfWanted.getId(),
        attributeOwnershipDivestitureIfWantedResponse);
      WriteFuture writeFuture = federate.getSession().write(requestResponse);

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
      Map<Federate, AttributeHandleSet> newOwnerAcquisitions =
        new HashMap<Federate, AttributeHandleSet>();
      for (Map.Entry<AttributeHandle, Federate> entry : newOwners.entrySet())
      {
        AttributeHandleSet acquiredAttributeHandles =
          newOwnerAcquisitions.get(entry.getValue());
        if (acquiredAttributeHandles == null)
        {
          acquiredAttributeHandles = new OHLAAttributeHandleSet();
          newOwnerAcquisitions.put(entry.getValue(), acquiredAttributeHandles);
        }
        acquiredAttributeHandles.add(entry.getKey());
      }

      // notify the new owners
      //
      for (Map.Entry<Federate, AttributeHandleSet> entry :
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
    Federate federate,
    CancelNegotiatedAttributeOwnershipDivestiture cancelNegotiatedAttributeOwnershipDivestiture)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      objectManager.cancelNegotiatedAttributeOwnershipDivestiture(
        federate,
        cancelNegotiatedAttributeOwnershipDivestiture.getObjectInstanceHandle(),
        cancelNegotiatedAttributeOwnershipDivestiture.getAttributeHandles());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void cancelAttributeOwnershipAcquisition(
    Federate federate,
    CancelAttributeOwnershipAcquisition cancelAttributeOwnershipAcquisition)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      objectManager.cancelAttributeOwnershipAcquisition(
        federate, cancelAttributeOwnershipAcquisition.getObjectInstanceHandle(),
        cancelAttributeOwnershipAcquisition.getAttributeHandles());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void queryAttributeOwnership(
    Federate federate, QueryAttributeOwnership queryAttributeOwnership)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      objectManager.queryAttributeOwnership(
        federate, queryAttributeOwnership.getObjectInstanceHandle(),
        queryAttributeOwnership.getAttributeHandle());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void enableTimeRegulation(
    Federate federate, EnableTimeRegulation enableTimeRegulation)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      timeKeeper.enableTimeRegulation(
        federate, enableTimeRegulation.getLookahead());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void disableTimeRegulation(
    Federate federate, DisableTimeRegulation disableTimeRegulation)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      timeKeeper.disableTimeRegulation(federate);
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void enableTimeConstrained(
    Federate federate, EnableTimeConstrained enableTimeConstrained)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      timeKeeper.enableTimeConstrained(federate);
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void disableTimeConstrained(
    Federate federate, DisableTimeConstrained disableTimeConstrained)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      timeKeeper.disableTimeConstrained(federate);
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void timeAdvanceRequest(
    Federate federate, TimeAdvanceRequest timeAdvanceRequest)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      timeKeeper.timeAdvanceRequest(federate, timeAdvanceRequest.getTime());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void timeAdvanceRequestAvailable(
    Federate federate, TimeAdvanceRequestAvailable timeAdvanceRequestAvailable)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      timeKeeper.timeAdvanceRequestAvailable(
        federate, timeAdvanceRequestAvailable.getTime());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void commitRegionModifications(
    Federate federate, CommitRegionModifications commitRegionModifications)
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

      federate.getSession().write(new DefaultResponse(commitRegionModifications.getId()));
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void getRangeBounds(Federate federate, GetRangeBounds getRangeBounds)
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

      federate.getSession().write(new DefaultResponse(getRangeBounds.getId(), rangeBounds));
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void createRegion(Federate federate, CreateRegion createRegion)
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

      federate.getSession().write(new DefaultResponse(createRegion.getId(), regionHandle));
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  public void deleteRegion(Federate federate, DeleteRegion deleteRegion)
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

      federate.getSession().write(new DefaultResponse(deleteRegion.getId()));
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
    return new OHLAFederateHandle(federateCount.incrementAndGet());
  }

  protected RegionHandle nextRegionHandle()
  {
    return new OHLARegionHandle(regionCount.incrementAndGet());
  }
}
