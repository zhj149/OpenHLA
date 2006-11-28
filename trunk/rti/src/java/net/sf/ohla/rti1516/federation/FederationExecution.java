package net.sf.ohla.rti1516.federation;

import java.net.SocketAddress;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti1516.OHLAAttributeHandleSet;
import net.sf.ohla.rti1516.OHLAFederateHandle;
import net.sf.ohla.rti1516.OHLAObjectInstanceHandle;
import net.sf.ohla.rti1516.OHLARegionHandle;
import net.sf.ohla.rti1516.RTI;
import net.sf.ohla.rti1516.fdd.Dimension;
import net.sf.ohla.rti1516.fdd.FDD;
import net.sf.ohla.rti1516.fdd.ObjectClass;
import net.sf.ohla.rti1516.federate.callbacks.AnnounceSynchronizationPoint;
import net.sf.ohla.rti1516.federate.callbacks.AttributeOwnershipAcquisitionNotification;
import net.sf.ohla.rti1516.federate.callbacks.DiscoverObjectInstance;
import net.sf.ohla.rti1516.federate.callbacks.FederationNotSaved;
import net.sf.ohla.rti1516.federate.callbacks.FederationRestoreStatusResponse;
import net.sf.ohla.rti1516.federate.callbacks.FederationSaveStatusResponse;
import net.sf.ohla.rti1516.federate.callbacks.FederationSaved;
import net.sf.ohla.rti1516.federate.callbacks.FederationSynchronized;
import net.sf.ohla.rti1516.federate.callbacks.InitiateFederateSave;
import net.sf.ohla.rti1516.federate.callbacks.RemoveObjectInstance;
import net.sf.ohla.rti1516.federation.ownership.OwnershipManager;
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
import net.sf.ohla.rti1516.messages.JoinFederationExecution;
import net.sf.ohla.rti1516.messages.JoinFederationExecutionResponse;
import net.sf.ohla.rti1516.messages.Message;
import net.sf.ohla.rti1516.messages.NegotiatedAttributeOwnershipDivestiture;
import net.sf.ohla.rti1516.messages.ObjectInstanceNameReserved;
import net.sf.ohla.rti1516.messages.ObjectInstanceRegistered;
import net.sf.ohla.rti1516.messages.QueryAttributeOwnership;
import net.sf.ohla.rti1516.messages.QueryFederationRestoreStatus;
import net.sf.ohla.rti1516.messages.QueryFederationSaveStatus;
import net.sf.ohla.rti1516.messages.RegisterFederationSynchronizationPoint;
import net.sf.ohla.rti1516.messages.RegisterObjectInstance;
import net.sf.ohla.rti1516.messages.RequestFederationRestore;
import net.sf.ohla.rti1516.messages.RequestFederationSave;
import net.sf.ohla.rti1516.messages.RequestResponse;
import net.sf.ohla.rti1516.messages.ReserveObjectInstanceName;
import net.sf.ohla.rti1516.messages.ResignFederationExecution;
import net.sf.ohla.rti1516.messages.SubscribeObjectClassAttributes;
import net.sf.ohla.rti1516.messages.SynchronizationPointAchieved;
import net.sf.ohla.rti1516.messages.TimeAdvanceRequest;
import net.sf.ohla.rti1516.messages.UnconditionalAttributeOwnershipDivestiture;
import net.sf.ohla.rti1516.messages.GetRangeBounds;
import net.sf.ohla.rti1516.messages.TimeAdvanceRequestAvailable;

import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static final String FEDERATE_HANDLE = "FederateHandle";
  private static final String FEDERATE_TYPE = "FederateType";
  private static final String CONNECTION_INFO = "ConnectionInfo";
  private static final String SAVE_STATUS = "SaveStatus";
  private static final String RESTORE_STATUS = "RestoreStatus";

  protected final String name;
  protected final FDD fdd;

  protected ReadWriteLock federationExecutionStateLock =
    new ReentrantReadWriteLock(true);
  protected FederationExecutionState federationExecutionState =
    FederationExecutionState.ACTIVE;

  protected FederationExecutionSave federationExecutionSave;
  protected FederationExecutionRestore federationExecutionRestore;

  protected Lock reservedObjectNamesLock = new ReentrantLock(true);
  protected Map<String, ObjectInstanceHandle> reservedObjectNames =
    new HashMap<String, ObjectInstanceHandle>();

  protected Lock federatesLock = new ReentrantLock(true);
  protected Map<FederateHandle, IoSession> federateSessions =
    new HashMap<FederateHandle, IoSession>();

  protected Lock synchronizationPointsLock = new ReentrantLock(true);
  protected Map<String, FederationExecutionSynchronizationPoint> synchronizationPoints =
    new HashMap<String, FederationExecutionSynchronizationPoint>();

  protected ReadWriteLock regionsLock = new ReentrantReadWriteLock(true);
  protected Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regions =
    new HashMap<RegionHandle, Map<DimensionHandle, RangeBounds>>();

  protected OwnershipManager ownershipManager = new OwnershipManager(this);

  protected TimeKeeper timeKeeper;

  protected AtomicInteger objectInstanceCount =
    new AtomicInteger(Integer.MIN_VALUE);
  protected AtomicInteger federateCount = new AtomicInteger(Short.MIN_VALUE);
  protected AtomicInteger regionCount = new AtomicInteger(Short.MIN_VALUE);

  protected Logger log;

  protected ExecutorService synchronousWaiter =
    Executors.newSingleThreadExecutor();

  public FederationExecution(String name, FDD fdd)
  {
    this.name = name;
    this.fdd = fdd;

    log = LoggerFactory.getLogger(
      String.format("%s.%s", FederationExecution.class, name));
  }

  public void destroy()
    throws FederatesCurrentlyJoined
  {
  }

  public IoSession getFederateSession(FederateHandle federateHandle)
  {
    federatesLock.lock();
    try
    {
      return federateSessions.get(federateHandle);
    }
    finally
    {
      federatesLock.unlock();
    }
  }

  public void send(Message message)
  {
    send(message, null);
  }

  public void send(Message message, IoSession sender)
  {
    for (IoSession federateSession : federateSessions.values())
    {
      if (federateSession != sender)
      {
        federateSession.write(message);
      }
    }
  }

  public boolean process(IoSession session, Object message)
  {
    boolean processed = true;

    if (message instanceof RegisterObjectInstance)
    {
      process(session, (RegisterObjectInstance) message);
    }
    else if (message instanceof ReserveObjectInstanceName)
    {
      process(session, (ReserveObjectInstanceName) message);
    }
    else if (message instanceof RemoveObjectInstance)
    {
      process(session, (RemoveObjectInstance) message);
    }
    else if (message instanceof SubscribeObjectClassAttributes)
    {
      process(session, (SubscribeObjectClassAttributes) message);
    }
    else if (message instanceof RegisterFederationSynchronizationPoint)
    {
      process(session, (RegisterFederationSynchronizationPoint) message);
    }
    else if (message instanceof SynchronizationPointAchieved)
    {
      process(session, (SynchronizationPointAchieved) message);
    }
    else if (message instanceof RequestFederationSave)
    {
      process(session, (RequestFederationSave) message);
    }
    else if (message instanceof FederateSaveInitiated)
    {
      process(session, (FederateSaveInitiated) message);
    }
    else if (message instanceof FederateSaveInitiatedFailed)
    {
      process(session, (FederateSaveInitiatedFailed) message);
    }
    else if (message instanceof FederateSaveBegun)
    {
      process(session, (FederateSaveBegun) message);
    }
    else if (message instanceof FederateSaveComplete)
    {
      process(session, (FederateSaveComplete) message);
    }
    else if (message instanceof FederateSaveNotComplete)
    {
      process(session, (FederateSaveNotComplete) message);
    }
    else if (message instanceof QueryFederationSaveStatus)
    {
      process(session, (QueryFederationSaveStatus) message);
    }
    else if (message instanceof RequestFederationRestore)
    {
      process(session, (RequestFederationRestore) message);
    }
    else if (message instanceof FederateRestoreComplete)
    {
      process(session, (FederateRestoreComplete) message);
    }
    else if (message instanceof FederateRestoreNotComplete)
    {
      process(session, (FederateRestoreNotComplete) message);
    }
    else if (message instanceof QueryFederationRestoreStatus)
    {
      process(session, (QueryFederationRestoreStatus) message);
    }
    else if (message instanceof UnconditionalAttributeOwnershipDivestiture)
    {
      process(session, (UnconditionalAttributeOwnershipDivestiture) message);
    }
    else if (message instanceof NegotiatedAttributeOwnershipDivestiture)
    {
      process(session, (NegotiatedAttributeOwnershipDivestiture) message);
    }
    else if (message instanceof ConfirmDivestiture)
    {
      process(session, (ConfirmDivestiture) message);
    }
    else if (message instanceof AttributeOwnershipAcquisition)
    {
      process(session, (AttributeOwnershipAcquisition) message);
    }
    else if (message instanceof AttributeOwnershipAcquisitionIfAvailable)
    {
      process(session, (AttributeOwnershipAcquisitionIfAvailable) message);
    }
    else if (message instanceof AttributeOwnershipDivestitureIfWanted)
    {
      process(session, (AttributeOwnershipDivestitureIfWanted) message);
    }
    else if (message instanceof CancelNegotiatedAttributeOwnershipDivestiture)
    {
      process(session, (CancelNegotiatedAttributeOwnershipDivestiture) message);
    }
    else if (message instanceof CancelAttributeOwnershipAcquisition)
    {
      process(session, (CancelAttributeOwnershipAcquisition) message);
    }
    else if (message instanceof QueryAttributeOwnership)
    {
      process(session, (QueryAttributeOwnership) message);
    }
    else if (message instanceof EnableTimeRegulation)
    {
      process(session, (EnableTimeRegulation) message);
    }
    else if (message instanceof DisableTimeRegulation)
    {
      process(session, (DisableTimeRegulation) message);
    }
    else if (message instanceof EnableTimeConstrained)
    {
      process(session, (EnableTimeConstrained) message);
    }
    else if (message instanceof DisableTimeConstrained)
    {
      process(session, (DisableTimeConstrained) message);
    }
    else if (message instanceof TimeAdvanceRequest)
    {
      process(session, (TimeAdvanceRequest) message);
    }
    else if (message instanceof TimeAdvanceRequestAvailable)
    {
      process(session, (TimeAdvanceRequestAvailable) message);
    }
    else if (message instanceof CommitRegionModifications)
    {
      process(session, (CommitRegionModifications) message);
    }
    else if (message instanceof GetRangeBounds)
    {
      process(session, (GetRangeBounds) message);
    }
    else if (message instanceof CreateRegion)
    {
      process(session, (CreateRegion) message);
    }
    else if (message instanceof DeleteRegion)
    {
      process(session, (DeleteRegion) message);
    }
    else if (message instanceof JoinFederationExecution)
    {
      process(session, (JoinFederationExecution) message);
    }
    else if (message instanceof ResignFederationExecution)
    {
      process(session, (ResignFederationExecution) message);
    }
    else
    {
      processed = false;
    }

    return processed;
  }

  protected void process(IoSession session,
                         RegisterObjectInstance registerObjectInstance)
  {
    boolean unlock = true;

    federationExecutionStateLock.readLock().lock();
    try
    {
      ObjectClass objectClass =
        fdd.getObjectClasses().get(
          registerObjectInstance.getObjectClassHandle());
      assert objectClass != null;

      String name = registerObjectInstance.getName();

      ObjectInstanceHandle objectInstanceHandle;
      if (name == null)
      {
        objectInstanceHandle = nextObjectInstanceHandle();
      }
      else
      {
        reservedObjectNamesLock.lock();
        try
        {
          objectInstanceHandle = reservedObjectNames.get(name);
          assert objectInstanceHandle != null;
        }
        finally
        {
          reservedObjectNamesLock.unlock();
        }
      }

      ObjectInstanceRegistered objectInstanceRegistered =
        new ObjectInstanceRegistered(objectInstanceHandle, name);

      session.write(new RequestResponse(
        registerObjectInstance.getId(), objectInstanceRegistered));

      ownershipManager.registerObjectInstance(
        objectInstanceHandle, objectClass,
        registerObjectInstance.getPublishedAttributeHandles(),
        getFederateHandle(session));

      unlock = false;
      synchronousWaiter.submit(new WaitForObjectInstanceRegisteredConfirmation(
        objectInstanceRegistered,
        new DiscoverObjectInstance(objectInstanceHandle,
                                   objectClass.getObjectClassHandle()),
        session));
    }
    finally
    {
      if (unlock)
      {
        federationExecutionStateLock.readLock().unlock();
      }
    }
  }

  protected void process(IoSession session,
                         ReserveObjectInstanceName reserveObjectInstanceName)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      ObjectInstanceHandle objectInstanceHandle = null;

      reservedObjectNamesLock.lock();
      try
      {
        if (!reservedObjectNames.containsKey(
          reserveObjectInstanceName.getName()))
        {
          objectInstanceHandle = nextObjectInstanceHandle();
          reservedObjectNames.put(reserveObjectInstanceName.getName(),
                                  objectInstanceHandle);
        }
      }
      finally
      {
        reservedObjectNamesLock.unlock();
      }

      if (objectInstanceHandle != null)
      {
        federatesLock.lock();
        try
        {
          for (IoSession federateSession : federateSessions.values())
          {
            if (federateSession != session)
            {
              ObjectInstanceNameReserved objectInstanceNameReserved =
                new ObjectInstanceNameReserved(
                  reserveObjectInstanceName.getName(), objectInstanceHandle);

              WriteFuture writeFuture =
                federateSession.write(objectInstanceNameReserved);

              // TODO: set timeout
              //
              writeFuture.join();

              if (writeFuture.isWritten())
              {
                try
                {
                  // TODO: set timeout
                  //
                  objectInstanceNameReserved.awaitUninterruptibly();
                }
                catch (ExecutionException ee)
                {
                  log.warn("did not receive reply", ee);
                }
              }
            }
          }
        }
        finally
        {
          federatesLock.unlock();
        }
      }

      session.write(new DefaultResponse(
        reserveObjectInstanceName.getId(), objectInstanceHandle));
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(IoSession session,
                         RemoveObjectInstance removeObjectInstance)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (removeObjectInstance.getSentOrderType() == OrderType.RECEIVE)
      {
        send(removeObjectInstance);
      }
      else
      {
        // TODO: schedule delete
      }
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(
    IoSession session,
    SubscribeObjectClassAttributes subscribeObjectClassAttributes)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      ObjectClass objectClass =
        fdd.getObjectClasses().get(
          subscribeObjectClassAttributes.getObjectClassHandle());
      assert objectClass != null;

      ownershipManager.subscribeObjectClassAttributes(
        objectClass, subscribeObjectClassAttributes.getAttributeHandles(),
        subscribeObjectClassAttributes.getAttributesAndRegions(), session);
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(
    IoSession session,
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
          session.write(new DefaultResponse(
            registerFederationSynchronizationPoint.getId(),
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
              federateHandles = new HashSet<FederateHandle>(federateSessions.keySet());
            }

            // verify all the federates in the set are joined
            //
            if (!federateSessions.keySet().containsAll(federateHandles))
            {
              session.write(new DefaultResponse(
                registerFederationSynchronizationPoint.getId(),
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

              session.write(new DefaultResponse(
                registerFederationSynchronizationPoint.getId()));

              send(new AnnounceSynchronizationPoint(
                registerFederationSynchronizationPoint.getLabel(),
                registerFederationSynchronizationPoint.getTag()));
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

  protected void process(
    IoSession session,
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
          getFederateHandle(session)))
        {
          federatesLock.lock();
          try
          {
            send(new FederationSynchronized(
              federationExecutionSynchronizationPoint.getLabel()));
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

  protected void process(IoSession session,
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
        session.write(null);

        // TODO: schedule save
      }
      else
      {
        // tell the federate that the request is going to be honored
        //
        session.write(null);

        // this is a psuedo save-in-progress... it will only prevent joins
        // and new requests to save/restore
        //
        federationExecutionState = FederationExecutionState.SAVE_IN_PROGRESS;

        federatesLock.lock();
        try
        {
          // track who was instructed to save
          //
          federationExecutionSave.instructedToSave(federateSessions.keySet());

          for (IoSession federateSession : federateSessions.values())
          {
            // update the federate save status
            //
            federateSession.setAttribute(
              SAVE_STATUS, SaveStatus.FEDERATE_INSTRUCTED_TO_SAVE);
          }

          // notify all federates to initiate save
          //
          send(new InitiateFederateSave(requestFederationSave.getLabel(),
                                        federateSessions.keySet()));
        }
        finally
        {
          federatesLock.unlock();
        }
      }
    }
    catch (SaveInProgress sip)
    {
      session.write(new DefaultResponse(requestFederationSave.getId(), sip));
    }
    catch (RestoreInProgress rip)
    {
      session.write(new DefaultResponse(requestFederationSave.getId(), rip));
    }
    finally
    {
      federationExecutionStateLock.writeLock().unlock();
    }
  }

  protected void process(IoSession session,
                         FederateSaveInitiated federateSaveInitiated)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      assert federationExecutionSave != null;

      federationExecutionSave.federateSaveInitiated(getFederateHandle(session));
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(
    IoSession session, FederateSaveInitiatedFailed federateSaveInitiatedFailed)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      assert federationExecutionSave != null;

      federationExecutionSave.federateSaveInitiatedFailed(
        getFederateHandle(session), federateSaveInitiatedFailed.getCause());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(IoSession session, FederateSaveBegun federateSaveBegun)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      assert federationExecutionSave != null;

      // update the federate save status
      //
      session.setAttribute(SAVE_STATUS, SaveStatus.FEDERATE_SAVING);

      federationExecutionSave.federateSaveBegun(getFederateHandle(session));
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(IoSession session,
                         FederateSaveComplete federateSaveComplete)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      assert federationExecutionSave != null;

      // update the federate save status
      //
      session.setAttribute(
        SAVE_STATUS, SaveStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_SAVE);

      if (federationExecutionSave.federateSaveComplete(
        getFederateHandle(session), federateSaveComplete.getFederateSave()))
      {
        // upgrade to write lock
        //
        federationExecutionStateLock.readLock().unlock();
        federationExecutionStateLock.writeLock().lock();
        try
        {
          federatesLock.lock();
          try
          {
            for (IoSession federateSession : federateSessions.values())
            {
              // update the federate save status
              //
              federateSession.setAttribute(SAVE_STATUS, SaveStatus.NO_SAVE_IN_PROGRESS);
            }

            send(new FederationSaved());

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

  protected void process(IoSession session,
                         FederateSaveNotComplete federateSaveNotComplete)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      assert federationExecutionSave != null;

      // update the federate save status
      //
      session.setAttribute(
        SAVE_STATUS, SaveStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_SAVE);

      if (federationExecutionSave.federateSaveNotComplete(
        getFederateHandle(session)))
      {
        // upgrade to write lock
        //
        federationExecutionStateLock.readLock().unlock();
        federationExecutionStateLock.writeLock().lock();
        try
        {
          federatesLock.lock();
          try
          {
            for (IoSession federateSession : federateSessions.values())
            {
              // update the federate save status
              //
              federateSession.setAttribute(SAVE_STATUS, SaveStatus.NO_SAVE_IN_PROGRESS);
            }

            send(new FederationNotSaved(
              federationExecutionSave.getSaveFailureReason()));

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

  protected void process(IoSession session,
                         QueryFederationSaveStatus queryFederationSaveStatus)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federationExecutionSave != null)
      {
        session.write(new FederationSaveStatusResponse(
          federationExecutionSave.getFederationSaveStatus()));
      }
      else
      {
        federatesLock.lock();
        try
        {
          FederateHandleSaveStatusPair[] federationSaveStatus =
            new FederateHandleSaveStatusPair[federateSessions.size()];
          int i = 0;
          for (FederateHandle federateHandle : federateSessions.keySet())
          {
            federationSaveStatus[i] = new FederateHandleSaveStatusPair(
              federateHandle, getSaveStatus(session));
          }
          session.write(new FederationSaveStatusResponse(federationSaveStatus));
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

  protected void process(IoSession session,
                         RequestFederationRestore requestFederationRestore)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      checkIfSaveInProgress();
      checkIfRestoreInProgress();

      // TODO: locate the saved information

      Set<FederateHandle> federateHandles =
        new HashSet<FederateHandle>(federateSessions.keySet());

      // TODO: determine if the same number of federate types are joined

      federationExecutionRestore = new FederationExecutionRestore(
        requestFederationRestore.getLabel(), federateHandles);

      session.write(new DefaultResponse(requestFederationRestore.getId()));
    }
    catch (SaveInProgress sip)
    {
      session.write(new DefaultResponse(requestFederationRestore.getId(), sip));
    }
    catch (RestoreInProgress rip)
    {
      session.write(new DefaultResponse(requestFederationRestore.getId(), rip));
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(IoSession session,
                         FederateRestoreComplete federateRestoreComplete)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federationExecutionRestore.federateRestoreComplete(
        getFederateHandle(session)))
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

  protected void process(IoSession session,
                         FederateRestoreNotComplete federateRestoreNotComplete)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federationExecutionRestore.federateRestoreNotComplete(
        getFederateHandle(session)))
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

  protected void process(
    IoSession session,
    QueryFederationRestoreStatus queryFederationRestoreStatus)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      if (federationExecutionRestore != null)
      {
        session.write(new FederationRestoreStatusResponse(
          federationExecutionRestore.getFederationRestoreStatus()));
      }
      else
      {
        federatesLock.lock();
        try
        {
          FederateHandleRestoreStatusPair[] federationRestoreStatus =
            new FederateHandleRestoreStatusPair[federateSessions.size()];
          int i = 0;
          for (FederateHandle federateHandle : federateSessions.keySet())
          {
            federationRestoreStatus[i] = new FederateHandleRestoreStatusPair(
              federateHandle, getRestoreStatus(session));
          }
          session.write(new FederationRestoreStatusResponse(
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

  protected void process(
    IoSession session,
    UnconditionalAttributeOwnershipDivestiture unconditionalAttributeOwnershipDivestiture)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      ownershipManager.unconditionalAttributeOwnershipDivestiture(
        unconditionalAttributeOwnershipDivestiture.getObjectInstanceHandle(),
        unconditionalAttributeOwnershipDivestiture.getAttributeHandles());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(
    IoSession session,
    NegotiatedAttributeOwnershipDivestiture negotiatedAttributeOwnershipDivestiture)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      ownershipManager.negotiatedAttributeOwnershipDivestiture(
        negotiatedAttributeOwnershipDivestiture.getObjectInstanceHandle(),
        negotiatedAttributeOwnershipDivestiture.getAttributeHandles(),
        negotiatedAttributeOwnershipDivestiture.getTag(), session);
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(IoSession session,
                         ConfirmDivestiture confirmDivestiture)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      ownershipManager.confirmDivestiture(
        confirmDivestiture.getObjectInstanceHandle(),
        confirmDivestiture.getAttributeHandles());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(
    IoSession session,
    AttributeOwnershipAcquisition attributeOwnershipAcquisition)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      ownershipManager.attributeOwnershipAcquisition(
        attributeOwnershipAcquisition.getObjectInstanceHandle(),
        attributeOwnershipAcquisition.getAttributeHandles(),
        attributeOwnershipAcquisition.getTag(), getFederateHandle(session),
        session);
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(
    IoSession session,
    AttributeOwnershipAcquisitionIfAvailable attributeOwnershipAcquisitionIfAvailable)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      ownershipManager.attributeOwnershipAcquisitionIfAvailable(
        attributeOwnershipAcquisitionIfAvailable.getObjectInstanceHandle(),
        attributeOwnershipAcquisitionIfAvailable.getAttributeHandles(),
        getFederateHandle(session), session);
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(
    IoSession session,
    AttributeOwnershipDivestitureIfWanted attributeOwnershipDivestitureIfWanted)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      Map<AttributeHandle, FederateHandle> newOwners =
        ownershipManager.attributeOwnershipDivestitureIfWanted(
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
      WriteFuture writeFuture = session.write(requestResponse);

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
      Map<FederateHandle, AttributeHandleSet> newOwnerAcquisitions =
        new HashMap<FederateHandle, AttributeHandleSet>();
      for (Map.Entry<AttributeHandle, FederateHandle> entry :
        newOwners.entrySet())
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
      for (Map.Entry<FederateHandle, AttributeHandleSet> entry :
        newOwnerAcquisitions.entrySet())
      {
        federatesLock.lock();
        try
        {
          IoSession federateSession = federateSessions.get(entry.getKey());
          if (federateSession != null)
          {
            federateSession.write(new AttributeOwnershipAcquisitionNotification(
              attributeOwnershipDivestitureIfWanted.getObjectInstanceHandle(),
              entry.getValue()));
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
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(
    IoSession session,
    CancelNegotiatedAttributeOwnershipDivestiture cancelNegotiatedAttributeOwnershipDivestiture)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      ownershipManager.cancelNegotiatedAttributeOwnershipDivestiture(
        cancelNegotiatedAttributeOwnershipDivestiture.getObjectInstanceHandle(),
        cancelNegotiatedAttributeOwnershipDivestiture.getAttributeHandles(),
        getFederateHandle(session));
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(
    IoSession session,
    CancelAttributeOwnershipAcquisition cancelAttributeOwnershipAcquisition)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      ownershipManager.cancelAttributeOwnershipAcquisition(
        cancelAttributeOwnershipAcquisition.getObjectInstanceHandle(),
        cancelAttributeOwnershipAcquisition.getAttributeHandles(),
        getFederateHandle(session), session);
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(IoSession session,
                         QueryAttributeOwnership queryAttributeOwnership)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      ownershipManager.queryAttributeOwnership(
        queryAttributeOwnership.getObjectInstanceHandle(),
        queryAttributeOwnership.getAttributeHandle(), session);
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(IoSession session,
                         EnableTimeRegulation enableTimeRegulation)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      timeKeeper.enableTimeRegulation(session, getFederateHandle(session),
                                      enableTimeRegulation.getLookahead());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(IoSession session,
                         DisableTimeRegulation disableTimeRegulation)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      timeKeeper.disableTimeRegulation(getFederateHandle(session));
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(IoSession session,
                         EnableTimeConstrained enableTimeConstrained)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      timeKeeper.enableTimeConstrained(session, getFederateHandle(session));
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(IoSession session,
                         DisableTimeConstrained disableTimeConstrained)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      timeKeeper.disableTimeConstrained(getFederateHandle(session));
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(IoSession session,
                         TimeAdvanceRequest timeAdvanceRequest)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      timeKeeper.timeAdvanceRequest(
        getFederateHandle(session), timeAdvanceRequest.getTime());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(
    IoSession session, TimeAdvanceRequestAvailable timeAdvanceRequestAvailable)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      timeKeeper.timeAdvanceRequestAvailable(
        getFederateHandle(session), timeAdvanceRequestAvailable.getTime());
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(IoSession session,
                         CommitRegionModifications commitRegionModifications)
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

      session.write(new DefaultResponse(commitRegionModifications.getId()));
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(IoSession session, GetRangeBounds getRangeBounds)
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

      session.write(new DefaultResponse(getRangeBounds.getId(), rangeBounds));
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(IoSession session, CreateRegion createRegion)
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

      session.write(new DefaultResponse(createRegion.getId(), regionHandle));
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(IoSession session, DeleteRegion deleteRegion)
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

      session.write(new DefaultResponse(deleteRegion.getId()));
    }
    finally
    {
      federationExecutionStateLock.readLock().unlock();
    }
  }

  protected void process(IoSession session,
                         JoinFederationExecution joinFederationExecution)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      checkIfSaveInProgress();
      checkIfRestoreInProgress();

      // get the next federate handle
      //
      FederateHandle federateHandle = nextFederateHandle();

      log.debug("new federate: {}", federateHandle);

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

        // get all the current federate's connection info
        //
        Map<FederateHandle, SocketAddress> peerSocketAddresses =
          new HashMap<FederateHandle, SocketAddress>(federateSessions.size());
        for (Map.Entry<FederateHandle, IoSession> entry : federateSessions.entrySet())
        {
          peerSocketAddresses.put(
            entry.getKey(), getConnectionInfo(entry.getValue()));
        }

        // track the session
        //
        federateSessions.put(federateHandle, session);

        // let the session track it's FederateHandle and ConnectionInfo
        //
        session.setAttribute(RTI.FEDERATION_EXECUTION, this);
        session.setAttribute(FEDERATE_HANDLE, federateHandle);
        session.setAttribute(FEDERATE_TYPE,
                             joinFederationExecution.getFederateType());
        session.setAttribute(SAVE_STATUS, SaveStatus.NO_SAVE_IN_PROGRESS);
        session.setAttribute(RESTORE_STATUS,
                             RestoreStatus.NO_RESTORE_IN_PROGRESS);
        session.setAttribute(CONNECTION_INFO,
                             joinFederationExecution.getConnectionInfo());

        WriteFuture writeFuture = session.write(new DefaultResponse(
          joinFederationExecution.getId(),
          new JoinFederationExecutionResponse(
            federateHandle, fdd, peerSocketAddresses)));

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

  protected void process(IoSession session,
                         ResignFederationExecution resignFederationExecution)
  {
    federationExecutionStateLock.readLock().lock();
    try
    {
      session.removeAttribute(RTI.FEDERATION_EXECUTION);

      federatesLock.lock();
      try
      {
        federateSessions.remove(getFederateHandle(session));
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

  protected FederateHandle getFederateHandle(IoSession session)
  {
    return (FederateHandle) session.getAttribute(FEDERATE_HANDLE);
  }

  protected String getFederateType(IoSession session)
  {
    return (String) session.getAttribute(FEDERATE_TYPE);
  }

  protected SaveStatus getSaveStatus(IoSession session)
  {
    return (SaveStatus) session.getAttribute(SAVE_STATUS);
  }

  protected RestoreStatus getRestoreStatus(IoSession session)
  {
    return (RestoreStatus) session.getAttribute(RESTORE_STATUS);
  }

  protected SocketAddress getConnectionInfo(IoSession session)
  {
    return (SocketAddress) session.getAttribute(CONNECTION_INFO);
  }

  protected FederateHandle nextFederateHandle()
  {
    return new OHLAFederateHandle(federateCount.incrementAndGet());
  }

  protected ObjectInstanceHandle nextObjectInstanceHandle()
  {
    return new OHLAObjectInstanceHandle(objectInstanceCount.incrementAndGet());
  }

  protected RegionHandle nextRegionHandle()
  {
    return new OHLARegionHandle(regionCount.incrementAndGet());
  }

  protected class WaitForObjectInstanceRegisteredConfirmation
    implements Callable<Object>
  {
    protected ObjectInstanceRegistered objectInstanceRegistered;
    protected DiscoverObjectInstance discoverObjectInstance;
    protected IoSession session;

    public WaitForObjectInstanceRegisteredConfirmation(
      ObjectInstanceRegistered objectInstanceRegistered,
      DiscoverObjectInstance discoverObjectInstance, IoSession session)
    {
      this.objectInstanceRegistered = objectInstanceRegistered;
      this.discoverObjectInstance = discoverObjectInstance;
      this.session = session;
    }

    public Object call()
    {
      try
      {
        try
        {
          // TODO: set timeout
          //
          objectInstanceRegistered.awaitUninterruptibly();
        }
        catch (ExecutionException ee)
        {
          log.warn("did not receive reply", ee);
        }

        send(discoverObjectInstance, session);
      }
      finally
      {
        federationExecutionStateLock.readLock().unlock();
      }

      return null;
    }
  }
}
