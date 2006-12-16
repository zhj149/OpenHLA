package net.sf.ohla.rti1516.federate.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti1516.fdd.InteractionClass;
import net.sf.ohla.rti1516.fdd.ObjectClass;
import net.sf.ohla.rti1516.federate.Federate;
import net.sf.ohla.rti1516.federate.SubscriptionManager;
import net.sf.ohla.rti1516.federate.callbacks.ReceiveInteraction;
import net.sf.ohla.rti1516.federate.callbacks.RemoveObjectInstance;
import net.sf.ohla.rti1516.messages.DefaultResponse;
import net.sf.ohla.rti1516.messages.ObjectInstanceRegistered;
import net.sf.ohla.rti1516.messages.RegisterObjectInstance;
import net.sf.ohla.rti1516.messages.ReserveObjectInstanceName;
import net.sf.ohla.rti1516.messages.ResignFederationExecution;
import net.sf.ohla.rti1516.messages.SubscribeInteractionClass;
import net.sf.ohla.rti1516.messages.SubscribeObjectClassAttributes;
import net.sf.ohla.rti1516.messages.UnconditionalAttributeOwnershipDivestiture;
import net.sf.ohla.rti1516.messages.UnsubscribeInteractionClass;
import net.sf.ohla.rti1516.messages.UnsubscribeObjectClassAttributes;
import net.sf.ohla.rti1516.OHLAAttributeHandleSet;

import org.apache.mina.common.WriteFuture;
import org.apache.mina.common.IoSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hla.rti1516.AttributeAcquisitionWasNotRequested;
import hla.rti1516.AttributeAlreadyBeingAcquired;
import hla.rti1516.AttributeAlreadyBeingDivested;
import hla.rti1516.AttributeAlreadyOwned;
import hla.rti1516.AttributeDivestitureWasNotRequested;
import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.AttributeNotDefined;
import hla.rti1516.AttributeNotOwned;
import hla.rti1516.AttributeNotPublished;
import hla.rti1516.AttributeRegionAssociation;
import hla.rti1516.DeletePrivilegeNotHeld;
import hla.rti1516.FederateOwnsAttributes;
import hla.rti1516.IllegalName;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.InteractionClassNotDefined;
import hla.rti1516.InteractionClassNotPublished;
import hla.rti1516.LogicalTime;
import hla.rti1516.MessageRetractionHandle;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectClassNotDefined;
import hla.rti1516.ObjectClassNotPublished;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ObjectInstanceNameInUse;
import hla.rti1516.ObjectInstanceNameNotReserved;
import hla.rti1516.ObjectInstanceNotKnown;
import hla.rti1516.OrderType;
import hla.rti1516.OwnershipAcquisitionPending;
import hla.rti1516.ParameterHandleValueMap;
import hla.rti1516.RTIinternalError;
import hla.rti1516.RegionHandleSet;
import hla.rti1516.ResignAction;
import hla.rti1516.RestoreInProgress;
import hla.rti1516.SaveInProgress;
import hla.rti1516.TransportationType;
import hla.rti1516.FederateAmbassador;

public class ObjectManager
{
  private static final Logger log =
    LoggerFactory.getLogger(ObjectManager.class);

  protected Federate federate;

  protected ReadWriteLock publicationLock = new ReentrantReadWriteLock(true);
  protected Map<ObjectClassHandle, Set<AttributeHandle>> publishedObjectClasses =
    new HashMap<ObjectClassHandle, Set<AttributeHandle>>();
  protected Set<InteractionClassHandle> publishedInteractionClasses =
    new HashSet<InteractionClassHandle>();

  protected ReadWriteLock subscriptionLock = new ReentrantReadWriteLock(true);
  protected ObjectManagerSubscriptionManager subscriptionManager =
    new ObjectManagerSubscriptionManager();

  protected Lock reservedObjectInstanceNamesLock = new ReentrantLock(true);
  protected Map<String, ObjectInstanceHandle> reservedObjectInstanceNames =
    new HashMap<String, ObjectInstanceHandle>();
  protected Map<ObjectInstanceHandle, String> reservedObjectInstanceNamesByHandle =
    new HashMap<ObjectInstanceHandle, String>();

  protected Lock retiredObjectInstanceNamesLock = new ReentrantLock(true);
  protected Set<String> retiredObjectInstanceNames = new HashSet<String>();

  protected ReadWriteLock objectsLock = new ReentrantReadWriteLock(true);
  protected Map<ObjectInstanceHandle, ObjectInstance> objects =
    new HashMap<ObjectInstanceHandle, ObjectInstance>();
  protected Map<String, ObjectInstance> objectsByName =
    new HashMap<String, ObjectInstance>();
  protected Map<ObjectClassHandle, Set<ObjectInstanceHandle>> objectsByObjectClassHandle =
    new HashMap<ObjectClassHandle, Set<ObjectInstanceHandle>>();
  protected Set<ObjectInstanceHandle> removedObjects =
    new HashSet<ObjectInstanceHandle>();

  protected ConcurrentMap<ObjectInstanceHandle, ObjectClassHandle> reflectedObjectClassHandles =
    new ConcurrentHashMap<ObjectInstanceHandle, ObjectClassHandle>();

  public ObjectManager(Federate federate)
  {
    this.federate = federate;
  }

  public void federateJoined(IoSession session)
  {
    subscriptionLock.readLock().lock();
    try
    {
      subscriptionManager.federateJoined(session);
    }
    finally
    {
      subscriptionLock.readLock().unlock();
    }
  }

  public void resignFederationExecution(ResignAction resignAction)
    throws OwnershipAcquisitionPending, FederateOwnsAttributes,
           RTIinternalError
  {
    WriteFuture writeFuture;

    objectsLock.readLock().lock();
    try
    {
      for (ObjectInstance objectInstance : objects.values())
      {
        objectInstance.checkIfOwnershipAcquisitionPending();
        objectInstance.checkIfFederateOwnsAttributes();
      }

      writeFuture = federate.getRTISession().write(
        new ResignFederationExecution(resignAction));
    }
    finally
    {
      objectsLock.readLock().unlock();
    }

    // TODO: set timeout
    //
    writeFuture.join();
    if (!writeFuture.isWritten())
    {
      throw new RTIinternalError("error communicating with RTI");
    }
  }

  public void publishObjectClassAttributes(ObjectClassHandle objectClassHandle,
                                           AttributeHandleSet attributeHandles)
  {
    publicationLock.writeLock().lock();
    try
    {
      Set<AttributeHandle> publishedAttributeHandles =
        publishedObjectClasses.get(objectClassHandle);
      if (publishedAttributeHandles == null)
      {
        publishedAttributeHandles = new HashSet<AttributeHandle>();
        publishedObjectClasses.put(objectClassHandle,
                                   publishedAttributeHandles);
      }

      publishedAttributeHandles.addAll(attributeHandles);
    }
    finally
    {
      publicationLock.writeLock().unlock();
    }
  }

  public void unpublishObjectClass(ObjectClassHandle objectClassHandle)
    throws OwnershipAcquisitionPending, RTIinternalError
  {
    publicationLock.writeLock().lock();
    try
    {
      objectsLock.writeLock().lock();
      try
      {
        // optimistically remove the objects to unpublish
        //
        Set<ObjectInstanceHandle> objectInstanceHandlesToUnpublish =
          objectsByObjectClassHandle.remove(objectClassHandle);
        if (objectInstanceHandlesToUnpublish != null &&
            !objectInstanceHandlesToUnpublish.isEmpty())
        {
          Collection<String> unpublishedObjectInstanceNames =
            new ArrayList<String>(objectInstanceHandlesToUnpublish.size());

          try
          {
            // see if any objects are acquiring attributes
            //
            for (ObjectInstanceHandle objectInstanceHandle : objectInstanceHandlesToUnpublish)
            {
              ObjectInstance objectInstance = objects.get(objectInstanceHandle);
              assert objectInstance != null;

              objectInstance.checkIfOwnershipAcquisitionPending();

              unpublishedObjectInstanceNames.add(objectInstance.getName());
            }
          }
          catch (OwnershipAcquisitionPending oap)
          {
            // put it back if the operation failed
            //
            objectsByObjectClassHandle.put(
              objectClassHandle, objectInstanceHandlesToUnpublish);

            throw oap;
          }

          objects.keySet().removeAll(objectInstanceHandlesToUnpublish);
          objectsByName.keySet().removeAll(unpublishedObjectInstanceNames);

          WriteFuture writeFuture = federate.getRTISession().write(
            new UnconditionalAttributeOwnershipDivestiture(
              objectInstanceHandlesToUnpublish));

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
        objectsLock.writeLock().unlock();
      }

      // unpublish the object class
      //
      publishedObjectClasses.remove(objectClassHandle);
    }
    finally
    {
      publicationLock.writeLock().unlock();
    }
  }

  public void unpublishObjectClassAttributes(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws OwnershipAcquisitionPending, RTIinternalError
  {
    publicationLock.writeLock().lock();
    try
    {
      Set<AttributeHandle> publishedAttributeHandles =
        publishedObjectClasses.get(objectClassHandle);
      if (publishedAttributeHandles != null)
      {
        objectsLock.writeLock().lock();
        try
        {
          Set<ObjectInstanceHandle> objectInstanceHandlesToUnpublishAttributes =
            objectsByObjectClassHandle.get(objectClassHandle);
          if (objectInstanceHandlesToUnpublishAttributes != null &&
              !objectInstanceHandlesToUnpublishAttributes.isEmpty())
          {
            // see if any objects are acquiring any attributes that are being
            // unpublished
            //
            for (ObjectInstanceHandle objectInstanceHandle : objectInstanceHandlesToUnpublishAttributes)
            {
              ObjectInstance objectInstance = objects.get(objectInstanceHandle);
              assert objectInstance != null;

              objectInstance.checkIfOwnershipAcquisitionPending(
                attributeHandles);
            }

            for (ObjectInstanceHandle objectInstanceHandle : objectInstanceHandlesToUnpublishAttributes)
            {
              ObjectInstance objectInstance = objects.get(objectInstanceHandle);
              assert objectInstance != null;

              objectInstance.unpublishObjectClassAttributes(
                attributeHandles, federate.getRTISession());
            }
          }
        }
        finally
        {
          objectsLock.writeLock().unlock();
        }
        // remove all the attribute handles that aren't published anymore
        //
        publishedAttributeHandles.removeAll(attributeHandles);
      }
    }
    finally
    {
      publicationLock.writeLock().unlock();
    }
  }

  public void publishInteractionClass(
    InteractionClassHandle interactionClassHandle)
  {
    publicationLock.writeLock().lock();
    try
    {
      publishedInteractionClasses.add(interactionClassHandle);
    }
    finally
    {
      publicationLock.writeLock().unlock();
    }
  }

  public void unpublishInteractionClass(
    InteractionClassHandle interactionClassHandle)
  {
    publicationLock.writeLock().lock();
    try
    {
      publishedInteractionClasses.remove(interactionClassHandle);
    }
    finally
    {
      publicationLock.writeLock().unlock();
    }
  }

  public void subscribeObjectClassAttributes(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles,
    boolean passive)
    throws RTIinternalError
  {
    subscriptionLock.writeLock().lock();
    try
    {
      subscriptionManager.subscribeObjectClassAttributes(
        objectClassHandle, attributeHandles, passive);

      SubscribeObjectClassAttributes subscribeObjectClassAttributes =
        new SubscribeObjectClassAttributes(
          objectClassHandle, attributeHandles, passive);

      WriteFuture writeFuture =
        federate.getRTISession().write(subscribeObjectClassAttributes);

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      federate.sendToPeers(subscribeObjectClassAttributes);
    }
    finally
    {
      subscriptionLock.writeLock().unlock();
    }
  }

  public void unsubscribeObjectClass(ObjectClassHandle objectClassHandle)
  {
    subscriptionLock.writeLock().lock();
    try
    {
      subscriptionManager.unsubscribeObjectClass(objectClassHandle);

      federate.sendToPeers(new UnsubscribeObjectClassAttributes(
        objectClassHandle));
    }
    finally
    {
      subscriptionLock.writeLock().unlock();
    }
  }

  public void unsubscribeObjectClassAttributes(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
  {
    subscriptionLock.writeLock().lock();
    try
    {
      subscriptionManager.unsubscribeObjectClassAttributes(
        objectClassHandle, attributeHandles);

      federate.sendToPeers(new UnsubscribeObjectClassAttributes(
        objectClassHandle, attributeHandles));
    }
    finally
    {
      subscriptionLock.writeLock().unlock();
    }
  }

  public void subscribeInteractionClass(
    InteractionClassHandle interactionClassHandle, boolean passive)
  {
    subscriptionLock.writeLock().lock();
    try
    {
      subscriptionManager.subscribeInteractionClass(
        interactionClassHandle, passive);

      federate.sendToPeers(
        new SubscribeInteractionClass(interactionClassHandle, passive));
    }
    finally
    {
      subscriptionLock.writeLock().unlock();
    }
  }

  public void unsubscribeInteractionClass(
    InteractionClassHandle interactionClassHandle)
  {
    subscriptionLock.writeLock().lock();
    try
    {
      subscriptionManager.unsubscribeInteractionClass(interactionClassHandle);

      federate.sendToPeers(new UnsubscribeInteractionClass(
        interactionClassHandle));
    }
    finally
    {
      subscriptionLock.writeLock().unlock();
    }
  }

  public ObjectClassHandle getObjectClassHandle(
    ObjectInstanceHandle objectInstanceHandle)
    throws ObjectInstanceNotKnown
  {
    objectsLock.readLock().lock();
    try
    {
      return getObjectInstance(
        objectInstanceHandle).getObjectClass().getObjectClassHandle();
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public ObjectInstanceHandle getObjectInstanceHandle(String name)
    throws ObjectInstanceNotKnown
  {
    objectsLock.readLock().lock();
    try
    {
      return getObjectInstance(name).getObjectInstanceHandle();
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public String getObjectInstanceName(ObjectInstanceHandle objectInstanceHandle)
    throws ObjectInstanceNotKnown
  {
    objectsLock.readLock().lock();
    try
    {
      return getObjectInstance(objectInstanceHandle).getName();
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public boolean reserveObjectInstanceName(String name)
    throws IllegalName, RTIinternalError
  {
    reservedObjectInstanceNamesLock.lock();
    try
    {
      if (reservedObjectInstanceNames.containsKey(name))
      {
        throw new IllegalName(
          String.format("object instance name already reserved: %s", name));
      }
    }
    finally
    {
      reservedObjectInstanceNamesLock.unlock();
    }

    retiredObjectInstanceNamesLock.lock();
    try
    {
      if (retiredObjectInstanceNames.contains(name))
      {
        throw new IllegalName(
          String.format("object instance name retired: %s", name));
      }
    }
    finally
    {
      retiredObjectInstanceNamesLock.unlock();
    }

    ReserveObjectInstanceName reserveObjectInstanceName =
      new ReserveObjectInstanceName(name);
    WriteFuture writeFuture =
      federate.getRTISession().write(reserveObjectInstanceName);

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
      Object response = reserveObjectInstanceName.getResponse();
      boolean reserved = response instanceof ObjectInstanceHandle;
      if (reserved)
      {
        reservedObjectInstanceNamesLock.lock();
        try
        {
          reservedObjectInstanceNames.put(
            name, (ObjectInstanceHandle) response);
        }
        finally
        {
          reservedObjectInstanceNamesLock.unlock();
        }
      }
      return reserved;
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

  public ObjectInstanceHandle registerObjectInstance(
    ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined, ObjectClassNotPublished, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    ObjectClass objectClass =
      federate.getFDD().getObjectClass(objectClassHandle);
    assert objectClass != null;

    publicationLock.readLock().lock();
    try
    {
      Set<AttributeHandle> publishedAttributeHandles =
        getPublishedObjectClassAttributes(objectClassHandle);

      RegisterObjectInstance registerObjectInstance =
        new RegisterObjectInstance(objectClassHandle,
                                   publishedAttributeHandles);
      WriteFuture writeFuture =
        federate.getRTISession().write(registerObjectInstance);

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      // TODO: set timeout
      //
      Object response = registerObjectInstance.getResponse();

      assert response instanceof ObjectInstanceRegistered :
        String.format("unexpected response: %s");

      ObjectInstanceRegistered objectInstanceRegistered =
        (ObjectInstanceRegistered) response;

      ObjectInstanceHandle objectInstanceHandle =
        objectInstanceRegistered.getObjectInstanceHandle();
      String name = String.format("HLA-%s", objectInstanceHandle);

      ObjectInstance objectInstance =
        new ObjectInstance(objectInstanceHandle, objectClass, name,
                           publishedAttributeHandles);

      objectsLock.writeLock().lock();
      try
      {
        objects.put(objectInstanceHandle, objectInstance);
        objectsByName.put(name, objectInstance);
        getObjectsByClassHandle(objectClassHandle).add(objectInstanceHandle);
      }
      finally
      {
        objectsLock.writeLock().unlock();
      }

      federate.getRTISession().write(new DefaultResponse(
        objectInstanceRegistered.getId()));

      return objectInstanceHandle;
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
      publicationLock.readLock().unlock();
    }
  }

  public ObjectInstanceHandle registerObjectInstance(
    ObjectClassHandle objectClassHandle, String name)
    throws ObjectClassNotDefined, ObjectClassNotPublished,
           ObjectInstanceNameNotReserved, ObjectInstanceNameInUse,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    ObjectClass objectClass =
      federate.getFDD().getObjectClass(objectClassHandle);

    publicationLock.readLock().lock();
    try
    {
      Set<AttributeHandle> publishedAttributeHandles =
        getPublishedObjectClassAttributes(objectClassHandle);

      objectsLock.writeLock().lock();
      try
      {
        checkIfObjectInstanceNameNotReserved(name);
        checkIfObjectInstanceNameInUse(name);

        if (objectsByName.containsKey(name))
        {
          throw new ObjectInstanceNameInUse(name);
        }

        // reserve this name
        //
        objectsByName.put(name, null);
      }
      finally
      {
        objectsLock.writeLock().unlock();
      }

      RegisterObjectInstance registerObjectInstance =
        new RegisterObjectInstance(objectClassHandle,
                                   publishedAttributeHandles);
      WriteFuture writeFuture =
        federate.getRTISession().write(registerObjectInstance);

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      // TODO: set timeout
      //
      Object response = registerObjectInstance.getResponse();

      assert response instanceof ObjectInstanceRegistered :
        String.format("unexpected response: %s");

      ObjectInstanceRegistered objectInstanceRegistered =
        (ObjectInstanceRegistered) response;

      ObjectInstanceHandle objectInstanceHandle =
        objectInstanceRegistered.getObjectInstanceHandle();

      ObjectInstance objectInstance =
        new ObjectInstance(objectInstanceHandle, objectClass, name,
                           publishedAttributeHandles);

      objectsLock.writeLock().lock();
      try
      {
        objects.put(objectInstanceHandle, objectInstance);
        objectsByName.put(name, objectInstance);
        getObjectsByClassHandle(objectClassHandle).add(objectInstanceHandle);
      }
      finally
      {
        objectsLock.writeLock().unlock();
      }

      writeFuture = federate.getRTISession().write(new DefaultResponse(
        objectInstanceRegistered.getId()));

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      return objectInstanceHandle;
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
      publicationLock.readLock().unlock();
    }
  }

  public void updateAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                                    AttributeHandleValueMap attributeValues,
                                    byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           RTIinternalError
  {
    objectsLock.readLock().lock();
    try
    {
      getObjectInstance(objectInstanceHandle).updateAttributeValues(
        attributeValues, tag, federate);
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void updateAttributeValues(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleValueMap attributeValues, byte[] tag, LogicalTime updateTime,
    MessageRetractionHandle messageRetractionHandle, OrderType sentOrderType)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           RTIinternalError
  {
    objectsLock.readLock().lock();
    try
    {
      getObjectInstance(objectInstanceHandle).updateAttributeValues(
        attributeValues, tag, updateTime, messageRetractionHandle,
        sentOrderType, federate);
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void sendInteraction(InteractionClassHandle interactionClassHandle,
                              ParameterHandleValueMap parameterValues,
                              byte[] tag)
    throws InteractionClassNotPublished
  {
    publicationLock.readLock().lock();
    try
    {
      checkIfInteractionClassNotPublished(interactionClassHandle);

      federate.sendToPeers(new ReceiveInteraction(
        interactionClassHandle, parameterValues, tag, OrderType.RECEIVE,
        TransportationType.HLA_RELIABLE));
    }
    finally
    {
      publicationLock.readLock().unlock();
    }
  }

  public void sendInteraction(
    InteractionClassHandle interactionClassHandle,
    ParameterHandleValueMap parameterValues, byte[] tag, LogicalTime sendTime,
    MessageRetractionHandle messageRetractionHandle, OrderType sentOrderType)
    throws InteractionClassNotPublished
  {
    publicationLock.readLock().lock();
    try
    {
      checkIfInteractionClassNotPublished(interactionClassHandle);

      federate.sendToPeers(new ReceiveInteraction(
        interactionClassHandle, parameterValues, tag, sentOrderType,
        TransportationType.HLA_RELIABLE, sendTime, messageRetractionHandle));
    }
    finally
    {
      publicationLock.readLock().unlock();
    }
  }

  public void deleteObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag)
    throws ObjectInstanceNotKnown, DeletePrivilegeNotHeld, RTIinternalError
  {
    WriteFuture writeFuture;

    objectsLock.writeLock().lock();
    try
    {
      ObjectInstance objectInstance = getObjectInstance(objectInstanceHandle);
      objectInstance.deleteObjectInstance(tag);

      removeObjectInstance(objectInstance);

      writeFuture = federate.getRTISession().write(new RemoveObjectInstance(
        objectInstanceHandle, tag, OrderType.RECEIVE));
    }
    finally
    {
      objectsLock.writeLock().unlock();
    }

    // TODO: set timeout
    //
    writeFuture.join();
    if (!writeFuture.isWritten())
    {
      throw new RTIinternalError("error communicating with RTI");
    }
  }

  public void deleteObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag,
    LogicalTime deleteTime, MessageRetractionHandle messageRetractionHandle,
    OrderType sentOrderType)
    throws ObjectInstanceNotKnown, DeletePrivilegeNotHeld, RTIinternalError
  {
    WriteFuture writeFuture;

    objectsLock.writeLock().lock();
    try
    {
      ObjectInstance objectInstance = getObjectInstance(objectInstanceHandle);
      objectInstance.deleteObjectInstance(tag);

      if (sentOrderType == OrderType.RECEIVE)
      {
        removeObjectInstance(objectInstance);
      }

      writeFuture = federate.getRTISession().write(
        new RemoveObjectInstance(
          objectInstanceHandle, tag, sentOrderType, deleteTime,
          messageRetractionHandle));
    }
    finally
    {
      objectsLock.writeLock().unlock();
    }

    // TODO: set timeout
    //
    writeFuture.join();
    if (!writeFuture.isWritten())
    {
      throw new RTIinternalError("error communicating with RTI");
    }
  }

  public void localDeleteObjectInstance(
    ObjectInstanceHandle objectInstanceHandle)
    throws ObjectInstanceNotKnown, FederateOwnsAttributes
  {
    objectsLock.writeLock().lock();
    try
    {
      ObjectInstance objectInstance = getObjectInstance(objectInstanceHandle);
      objectInstance.localDeleteObjectInstance();

      objects.remove(objectInstanceHandle);
      objectsByName.remove(objectInstance.getName());
    }
    finally
    {
      objectsLock.writeLock().unlock();
    }

    // TODO: send local delete to peers
  }

  public void discoverObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                                     ObjectClassHandle objectClassHandle,
                                     String name,
                                     FederateAmbassador federateAmbassador)
  {
    ObjectClass objectClass =
      federate.getFDD().getObjectClasses().get(objectClassHandle);
    assert objectClass != null;

    subscriptionLock.readLock().lock();
    try
    {
      objectClass = subscriptionManager.getSubscribedObjectClass(objectClass);

      if (objectClass != null)
      {
        objectsLock.writeLock().lock();
        try
        {
          ObjectInstance objectInstance =
            new ObjectInstance(objectInstanceHandle, objectClass, name);
          ObjectInstance oldObjectInstance =
            objects.put(objectInstanceHandle, objectInstance);
          if (oldObjectInstance != null)
          {
            // the object has already been discovered, put it back and don't
            // complete the callback
            //
            objects.put(objectInstanceHandle, oldObjectInstance);
          }
          else
          {
            objectsByName.put(name, objectInstance);
            getObjectsByClassHandle(objectClassHandle).add(
              objectInstanceHandle);

            objectInstance.discoverObjectInstance(
              objectInstanceHandle, objectClassHandle, name,
              federateAmbassador);
          }
        }
        catch (Throwable t)
        {
          log.warn(String.format(
            "federate could not discover object instance: %s",
            objectInstanceHandle), t);
        }
        finally
        {
          objectsLock.writeLock().unlock();
        }
      }
    }
    finally
    {
      subscriptionLock.readLock().unlock();
    }
  }

  public void reflectAttributeValues(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleValueMap attributeValues, byte[] tag,
    OrderType sentOrderType, TransportationType transportationType,
    LogicalTime updateTime, OrderType receivedOrderType,
    MessageRetractionHandle messageRetractionHandle,
    RegionHandleSet sentRegionHandles, FederateAmbassador federateAmbassador)
  {
    subscriptionLock.readLock().lock();
    try
    {
      ObjectClass objectClass = null;
      ObjectClass subscribedObjectClass = null;

      objectsLock.readLock().lock();
      try
      {
        ObjectInstance objectInstance = objects.get(objectInstanceHandle);
        if (objectInstance != null)
        {
          objectClass = objectInstance.getObjectClass();
          subscribedObjectClass =
            subscriptionManager.getSubscribedObjectClass(objectClass);
        }
        else if (!removedObjects.contains(objectInstanceHandle))
        {
          // object has not been discovered yet

          ObjectClassHandle objectClassHandle =
            reflectedObjectClassHandles.get(objectInstanceHandle);

          objectClass =
            federate.getFDD().getObjectClasses().get(objectClassHandle);
          assert objectClass != null;

          subscribedObjectClass =
            subscriptionManager.getSubscribedObjectClass(objectClass);

          // upgrade to a write lock
          //
          objectsLock.readLock().unlock();
          objectsLock.writeLock().lock();
          try
          {
            // objects only get removed/added during callbacks and only one
            // callback can occur at a time (this method is called in one)
            //
            assert !objects.containsKey(objectInstanceHandle);
            assert !removedObjects.contains(objectInstanceHandle);

            String name =
              reservedObjectInstanceNamesByHandle.get(
                objectInstanceHandle);
            name = name != null ?
              name : String.format("HLA-%s", objectInstanceHandle);

            objectInstance = new ObjectInstance(
              objectInstanceHandle, subscribedObjectClass, name);

            objects.put(objectInstanceHandle, objectInstance);
            objectsByName.put(name, objectInstance);
            getObjectsByClassHandle(objectClassHandle).add(
              objectInstanceHandle);

            // TODO: this means 2 callbacks are happening
            //
            objectInstance.discoverObjectInstance(
              objectInstanceHandle, objectClassHandle, name,
              federateAmbassador);
          }
          finally
          {
            // downgrade to read lock
            //
            objectsLock.readLock().lock();
            objectsLock.writeLock().unlock();
          }
        }

        if (objectInstance != null && subscribedObjectClass != null)
        {
          if (!subscribedObjectClass.equals(objectClass))
          {
            subscriptionManager.trim(
              attributeValues, subscribedObjectClass.getObjectClassHandle());
          }

          objectInstance.reflectAttributeValues(
            objectInstanceHandle, attributeValues, tag, sentOrderType,
            transportationType, updateTime, receivedOrderType,
            messageRetractionHandle, sentRegionHandles, federateAmbassador);
        }
      }
      catch (Throwable t)
      {
        log.warn(String.format(
          "federate could not reflect attributes: %s", objectInstanceHandle),
                 t);
      }
      finally
      {
        objectsLock.readLock().unlock();
      }
    }
    finally
    {
      subscriptionLock.readLock().unlock();
    }
  }

  public void receiveInteraction(
    InteractionClassHandle interactionClassHandle,
    ParameterHandleValueMap parameterValues, byte[] tag,
    OrderType sentOrderType, TransportationType transportationType,
    LogicalTime sendTime, OrderType receivedOrderType,
    MessageRetractionHandle messageRetractionHandle,
    RegionHandleSet sentRegionHandles, FederateAmbassador federateAmbassador)
  {
    InteractionClass interactionClass =
      federate.getFDD().getInteractionClasses().get(interactionClassHandle);
    assert interactionClass != null;

    subscriptionLock.readLock().lock();
    try
    {
      InteractionClass subscribedInteractionClass =
        subscriptionManager.getSubscribedInteractionClass(interactionClass);

      if (subscribedInteractionClass != null)
      {
        interactionClassHandle =
          subscribedInteractionClass.getInteractionClassHandle();

        if (!subscribedInteractionClass.equals(interactionClass))
        {
          // trim the parameter values
          //
          parameterValues.keySet().retainAll(
            subscribedInteractionClass.getParameters().keySet());
        }

        if (sendTime == null)
        {
          if (sentRegionHandles == null)
          {
            federateAmbassador.receiveInteraction(
              interactionClassHandle, parameterValues, tag, sentOrderType,
              transportationType);
          }
          else
          {
            federateAmbassador.receiveInteraction(
              interactionClassHandle, parameterValues, tag, sentOrderType,
              transportationType, sentRegionHandles);
          }
        }
        else if (messageRetractionHandle == null)
        {
          if (sentRegionHandles == null)
          {
            federateAmbassador.receiveInteraction(
              interactionClassHandle, parameterValues, tag, sentOrderType,
              transportationType, sendTime, receivedOrderType);
          }
          else
          {
            federateAmbassador.receiveInteraction(
              interactionClassHandle, parameterValues, tag, sentOrderType,
              transportationType, sendTime, receivedOrderType,
              sentRegionHandles);
          }
        }
        else if (sentRegionHandles == null)
        {
          federateAmbassador.receiveInteraction(
            interactionClassHandle, parameterValues, tag, sentOrderType,
            transportationType, sendTime, receivedOrderType,
            messageRetractionHandle);
        }
        else
        {
          federateAmbassador.receiveInteraction(
            interactionClassHandle, parameterValues, tag, sentOrderType,
            transportationType, sendTime, receivedOrderType,
            messageRetractionHandle, sentRegionHandles);
        }
      }
    }
    catch (Throwable t)
    {
      log.warn(String.format(
        "federate could not receive interaction: %s", interactionClassHandle),
               t);
    }
    finally
    {
      subscriptionLock.readLock().unlock();
    }
  }

  public void removeObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag,
    OrderType sentOrderType, LogicalTime deleteTime,
    OrderType receivedOrderType,
    MessageRetractionHandle messageRetractionHandle,
    FederateAmbassador federateAmbassador)
  {
    objectsLock.writeLock().lock();
    try
    {
      ObjectInstance objectInstance = objects.remove(objectInstanceHandle);
      if (objectInstance != null)
      {
        removeObjectInstance(objectInstance);

        objectInstance.removeObjectInstance(
          objectInstanceHandle, tag, sentOrderType, deleteTime,
          receivedOrderType, messageRetractionHandle, federateAmbassador);
      }
    }
    finally
    {
      objectsLock.writeLock().unlock();
    }
  }

  public void attributeOwnershipAcquisitionNotification(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag,
    FederateAmbassador federateAmbassador)
  {
    objectsLock.readLock().lock();
    try
    {
      ObjectInstance objectInstance = objects.get(objectInstanceHandle);
      if (objectInstance != null)
      {
        objectInstance.attributeOwnershipAcquisitionNotification(
          attributeHandles, tag, federateAmbassador);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void changeAttributeOrderType(
    ObjectInstanceHandle objectInstanceHandle,
    Set<AttributeHandle> attributeHandles, OrderType orderType)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned
  {
    objectsLock.readLock().lock();
    try
    {
      getObjectInstance(objectInstanceHandle).changeAttributeOrderType(
        attributeHandles, orderType);
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void changeInteractionOrderType(
    InteractionClassHandle interactionClassHandle, OrderType orderType)
    throws InteractionClassNotDefined, InteractionClassNotPublished
  {
    publicationLock.readLock().lock();
    try
    {
      checkIfInteractionClassNotPublished(interactionClassHandle);

      federate.getFDD().changeInteractionOrderType(
        interactionClassHandle, orderType);
    }
    finally
    {
      publicationLock.readLock().unlock();
    }
  }

  public void changeAttributeTransportationType(
    ObjectInstanceHandle objectInstanceHandle,
    Set<AttributeHandle> attributeHandles,
    TransportationType transportationType)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned
  {
    objectsLock.readLock().lock();
    try
    {
      getObjectInstance(objectInstanceHandle).changeAttributeTransportationType(
        attributeHandles, transportationType);
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void changeInteractionTransportationType(
    InteractionClassHandle interactionClassHandle,
    TransportationType transportationType)
    throws InteractionClassNotDefined, InteractionClassNotPublished
  {
    publicationLock.readLock().lock();
    try
    {
      checkIfInteractionClassNotPublished(interactionClassHandle);

      federate.getFDD().changeInteractionTransportationType(
        interactionClassHandle, transportationType);
    }
    finally
    {
      publicationLock.readLock().unlock();
    }
  }

  public void requestAttributeValueUpdate(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined
  {
    objectsLock.readLock().lock();
    try
    {
      getObjectInstance(objectInstanceHandle).requestAttributeValueUpdate(
        attributeHandles, tag, federate);
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public boolean isAttributeOwnedByFederate(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
    throws ObjectInstanceNotKnown, AttributeNotDefined
  {
    objectsLock.readLock().lock();
    try
    {
      return getObjectInstance(
        objectInstanceHandle).isAttributeOwnedByFederate(attributeHandle);
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void associateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeRegionAssociation attributeRegionAssociation)
    throws ObjectInstanceNotKnown, AttributeNotDefined
  {
    objectsLock.readLock().lock();
    try
    {
      getObjectInstance(objectInstanceHandle).associateRegionsForUpdates(
        attributeRegionAssociation);
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void unassociateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeRegionAssociation attributeRegionAssociation)
    throws ObjectInstanceNotKnown, AttributeNotDefined
  {
    objectsLock.readLock().lock();
    try
    {
      getObjectInstance(objectInstanceHandle).unassociateRegionsForUpdates(
        attributeRegionAssociation);
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void unconditionalAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           RTIinternalError
  {
    objectsLock.readLock().lock();
    try
    {
      getObjectInstance(
        objectInstanceHandle).unconditionalAttributeOwnershipDivestiture(
        attributeHandles, federate.getRTISession());
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void negotiatedAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           AttributeAlreadyBeingDivested, RTIinternalError
  {
    objectsLock.readLock().lock();
    try
    {
      getObjectInstance(
        objectInstanceHandle).negotiatedAttributeOwnershipDivestiture(
        attributeHandles, tag, federate.getRTISession());
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void confirmDivestiture(ObjectInstanceHandle objectInstanceHandle,
                                 AttributeHandleSet attributeHandles,
                                 byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           AttributeDivestitureWasNotRequested, RTIinternalError
  {
    objectsLock.readLock().lock();
    try
    {
      getObjectInstance(objectInstanceHandle).confirmDivestiture(
        attributeHandles, tag, federate.getRTISession());
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisition(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, ObjectClassNotPublished, AttributeNotDefined,
           AttributeNotPublished, FederateOwnsAttributes, RTIinternalError
  {
    objectsLock.readLock().lock();
    try
    {
      publicationLock.readLock().lock();
      try
      {
        ObjectInstance objectInstance = getObjectInstance(objectInstanceHandle);

        checkIfAttributeNotPublished(
          objectInstance.getObjectClassHandle(), attributeHandles);

        objectInstance.attributeOwnershipAcquisition(
          attributeHandles, tag, federate.getRTISession());
      }
      finally
      {
        publicationLock.readLock().unlock();
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisitionIfAvailable(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, ObjectClassNotPublished, AttributeNotDefined,
           AttributeNotPublished, FederateOwnsAttributes,
           AttributeAlreadyBeingAcquired, RTIinternalError
  {
    objectsLock.readLock().lock();
    try
    {
      ObjectInstance objectInstance = getObjectInstance(objectInstanceHandle);

      publicationLock.readLock().lock();
      try
      {
        for (AttributeHandle attributeHandle : attributeHandles)
        {
          objectInstance.getObjectClass().checkIfAttributeNotDefined(
            attributeHandle);
        }
        checkIfAttributeNotPublished(
          objectInstance.getObjectClassHandle(), attributeHandles);

        objectInstance.attributeOwnershipAcquisitionIfAvailable(
          attributeHandles, federate.getRTISession());
      }
      finally
      {
        publicationLock.readLock().unlock();
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public AttributeHandleSet attributeOwnershipDivestitureIfWanted(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           RTIinternalError
  {
    objectsLock.readLock().lock();
    try
    {
      return getObjectInstance(
        objectInstanceHandle).attributeOwnershipDivestitureIfWanted(
        attributeHandles, federate.getRTISession());
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           AttributeDivestitureWasNotRequested, RTIinternalError
  {
    objectsLock.readLock().lock();
    try
    {
      getObjectInstance(
        objectInstanceHandle).cancelNegotiatedAttributeOwnershipDivestiture(
        attributeHandles, federate.getRTISession());
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void cancelAttributeOwnershipAcquisition(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeAlreadyOwned,
           AttributeAcquisitionWasNotRequested, RTIinternalError
  {
    objectsLock.readLock().lock();
    try
    {
      getObjectInstance(
        objectInstanceHandle).cancelAttributeOwnershipAcquisition(
        attributeHandles, federate.getRTISession());
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void queryAttributeOwnership(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
    throws ObjectInstanceNotKnown, AttributeNotDefined, RTIinternalError
  {
    getObjectInstance(objectInstanceHandle).queryAttributeOwnership(
      attributeHandle, federate.getRTISession());
  }

  public void objectInstanceNameReserved(
    String name, ObjectInstanceHandle objectInstanceHandle)
  {
    reservedObjectInstanceNamesLock.lock();
    try
    {
      ObjectInstanceHandle oldObjectInstanceHandle =
        reservedObjectInstanceNames.put(name, objectInstanceHandle);
      assert oldObjectInstanceHandle == null;
    }
    finally
    {
      reservedObjectInstanceNamesLock.unlock();
    }
  }

  protected Set<AttributeHandle> getPublishedObjectClassAttributes(
    ObjectClassHandle objectClassHandle)
    throws ObjectClassNotPublished
  {
    Set<AttributeHandle> publishedAttributeHandles =
      publishedObjectClasses.get(objectClassHandle);
    if (publishedAttributeHandles == null)
    {
      throw new ObjectClassNotPublished(String.format("%s", objectClassHandle));
    }
    return publishedAttributeHandles;
  }

  protected ObjectInstance getObjectInstance(
    ObjectInstanceHandle objectInstanceHandle)
    throws ObjectInstanceNotKnown
  {
    ObjectInstance objectInstance = objects.get(objectInstanceHandle);
    if (objectInstance == null)
    {
      throw new ObjectInstanceNotKnown(
        String.format("%s", objectInstanceHandle));
    }
    return objectInstance;
  }

  protected ObjectInstance getObjectInstance(String name)
    throws ObjectInstanceNotKnown
  {
    ObjectInstance objectInstance = objectsByName.get(name);
    if (objectInstance == null)
    {
      throw new ObjectInstanceNotKnown(name);
    }
    return objectInstance;
  }

  protected Set<ObjectInstanceHandle> getObjectsByClassHandle(
    ObjectClassHandle objectClassHandle)
  {
    Set<ObjectInstanceHandle> objectInstanceHandles =
      objectsByObjectClassHandle.get(objectClassHandle);
    if (objectInstanceHandles == null)
    {
      objectInstanceHandles = new HashSet<ObjectInstanceHandle>();
      objectsByObjectClassHandle.put(objectClassHandle, objectInstanceHandles);
    }
    return objectInstanceHandles;
  }

  protected void checkIfInteractionClassNotPublished(
    InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotPublished
  {
    if (!publishedInteractionClasses.contains(interactionClassHandle))
    {
      throw new InteractionClassNotPublished(interactionClassHandle);
    }
  }

  protected void checkIfAttributeNotPublished(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws ObjectClassNotPublished, AttributeNotDefined,
           AttributeNotPublished
  {
    Set<AttributeHandle> publishedAttributeHandles =
      publishedObjectClasses.get(objectClassHandle);
    if (publishedAttributeHandles == null)
    {
      throw new ObjectClassNotPublished(String.format("%s", objectClassHandle));
    }
    else
    {
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        if (!publishedAttributeHandles.contains(attributeHandle))
        {
          ObjectClass objectClass =
            federate.getFDD().getObjectClasses().get(objectClassHandle);
          assert objectClass != null;

          objectClass.checkIfAttributeNotDefined(attributeHandle);
        }
      }
    }
  }

  protected void checkIfObjectInstanceNameNotReserved(String name)
    throws ObjectInstanceNameNotReserved
  {
    reservedObjectInstanceNamesLock.lock();
    try
    {
      if (!reservedObjectInstanceNames.containsKey(name))
      {
        throw new ObjectInstanceNameNotReserved(name);
      }
    }
    finally
    {
      reservedObjectInstanceNamesLock.unlock();
    }
  }

  protected void checkIfObjectInstanceNameInUse(String name)
    throws ObjectInstanceNameInUse
  {
    retiredObjectInstanceNamesLock.lock();
    try
    {
      if (retiredObjectInstanceNames.contains(name))
      {
        throw new ObjectInstanceNameInUse(name);
      }
    }
    finally
    {
      retiredObjectInstanceNamesLock.unlock();
    }
  }

  protected void removeObjectInstance(ObjectInstance objectInstance)
  {
    objects.remove(objectInstance.getObjectInstanceHandle());
    objectsByName.remove(objectInstance.getName());
    getObjectsByClassHandle(objectInstance.getObjectClassHandle()).remove(
      objectInstance.getObjectInstanceHandle());
    removedObjects.add(objectInstance.getObjectInstanceHandle());

    reservedObjectInstanceNamesLock.lock();
    try
    {
      if (reservedObjectInstanceNames.containsKey(objectInstance.getName()))
      {
        retiredObjectInstanceNamesLock.lock();
        try
        {
          retiredObjectInstanceNames.add(objectInstance.getName());
        }
        finally
        {
          retiredObjectInstanceNamesLock.unlock();
        }
      }
    }
    finally
    {
      reservedObjectInstanceNamesLock.unlock();
    }
  }

  public String createObjectInstanceName(
    ObjectInstanceHandle objectInstanceHandle,
    ObjectClassHandle objectClassHandle)
  {
    String objectInstanceName;

    objectsLock.readLock().lock();
    try
    {
      objectInstanceName =
        reservedObjectInstanceNamesByHandle.get(objectInstanceHandle);
    }
    finally
    {
      objectsLock.readLock().unlock();
    }

    if (objectInstanceName == null)
    {
      objectInstanceName = String.format("HLA-%s", objectInstanceHandle);
    }

    return objectInstanceName;
  }

  public void objectReflected(ObjectInstanceHandle objectInstanceHandle,
                              ObjectClassHandle objectClassHandle)
  {
    reflectedObjectClassHandles.putIfAbsent(
      objectInstanceHandle, objectClassHandle);
  }

  protected class ObjectManagerSubscriptionManager
    extends SubscriptionManager
  {
    public void federateJoined(IoSession session)
    {
      for (Map.Entry<ObjectClassHandle, Map<AttributeHandle, AttributeSubscription>> entry :
        subscribedObjectClasses.entrySet())
      {
        AttributeHandleSet passiveDefaultRegionAttributeHandles =
          new OHLAAttributeHandleSet();
        AttributeHandleSet defaultRegionAttributeHandles =
          new OHLAAttributeHandleSet();

        for (Map.Entry<AttributeHandle, AttributeSubscription> entry2 :
          entry.getValue().entrySet())
        {
          if (entry2.getValue().isDefaultRegionSubscribed())
          {
            if (entry2.getValue().isDefaultRegionPassive())
            {
              passiveDefaultRegionAttributeHandles.add(entry2.getKey());
            }
            else
            {
              defaultRegionAttributeHandles.add(entry2.getKey());
            }
          }

          if (!passiveDefaultRegionAttributeHandles.isEmpty())
          {
            session.write(new SubscribeObjectClassAttributes(
              entry.getKey(), passiveDefaultRegionAttributeHandles, true));
          }

          if (!defaultRegionAttributeHandles.isEmpty())
          {
            session.write(new SubscribeObjectClassAttributes(
              entry.getKey(), defaultRegionAttributeHandles, false));
          }
        }

        // TODO: DDM
      }

      for (Map.Entry<InteractionClassHandle, InteractionClassSubscription> entry :
        subscribedInteractionClasses.entrySet())
      {
        if (entry.getValue().isDefaultRegionSubscribed())
        {
          session.write(new SubscribeInteractionClass(
            entry.getKey(), entry.getValue().isDefaultRegionPassive()));
        }

        // TODO: DDM
      }
    }
  }
}
