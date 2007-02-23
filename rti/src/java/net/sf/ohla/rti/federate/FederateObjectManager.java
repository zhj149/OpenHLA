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

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.SubscriptionManager;
import net.sf.ohla.rti.fdd.Attribute;
import net.sf.ohla.rti.fdd.InteractionClass;
import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.hla.rti1516.IEEE1516AttributeHandleSet;
import net.sf.ohla.rti.hla.rti1516.IEEE1516RegionHandleSet;
import net.sf.ohla.rti.messages.AttributeOwnershipAcquisition;
import net.sf.ohla.rti.messages.AttributeOwnershipAcquisitionIfAvailable;
import net.sf.ohla.rti.messages.AttributeOwnershipDivestitureIfWanted;
import net.sf.ohla.rti.messages.AttributeOwnershipDivestitureIfWantedResponse;
import net.sf.ohla.rti.messages.CancelAttributeOwnershipAcquisition;
import net.sf.ohla.rti.messages.CancelNegotiatedAttributeOwnershipDivestiture;
import net.sf.ohla.rti.messages.ConfirmDivestiture;
import net.sf.ohla.rti.messages.DefaultResponse;
import net.sf.ohla.rti.messages.DeleteObjectInstance;
import net.sf.ohla.rti.messages.NegotiatedAttributeOwnershipDivestiture;
import net.sf.ohla.rti.messages.QueryAttributeOwnership;
import net.sf.ohla.rti.messages.RegisterObjectInstance;
import net.sf.ohla.rti.messages.RequestAttributeValueUpdate;
import net.sf.ohla.rti.messages.ReserveObjectInstanceName;
import net.sf.ohla.rti.messages.ResignFederationExecution;
import net.sf.ohla.rti.messages.SendInteraction;
import net.sf.ohla.rti.messages.SubscribeInteractionClass;
import net.sf.ohla.rti.messages.SubscribeObjectClassAttributes;
import net.sf.ohla.rti.messages.UnconditionalAttributeOwnershipDivestiture;
import net.sf.ohla.rti.messages.UnsubscribeInteractionClass;
import net.sf.ohla.rti.messages.UnsubscribeObjectClassAttributes;
import net.sf.ohla.rti.messages.UpdateAttributeValues;
import net.sf.ohla.rti.messages.callbacks.RemoveObjectInstance;

import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

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
import hla.rti1516.AttributeNotRecognized;
import hla.rti1516.AttributeNotSubscribed;
import hla.rti1516.AttributeRegionAssociation;
import hla.rti1516.CouldNotDiscover;
import hla.rti1516.DeletePrivilegeNotHeld;
import hla.rti1516.FederateAmbassador;
import hla.rti1516.FederateInternalError;
import hla.rti1516.FederateOwnsAttributes;
import hla.rti1516.IllegalName;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.InteractionClassNotDefined;
import hla.rti1516.InteractionClassNotPublished;
import hla.rti1516.InvalidLogicalTime;
import hla.rti1516.LogicalTime;
import hla.rti1516.MessageRetractionHandle;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectClassNotDefined;
import hla.rti1516.ObjectClassNotPublished;
import hla.rti1516.ObjectClassNotRecognized;
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

public class FederateObjectManager
{
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
  protected Set<String> reservedObjectInstanceNames = new HashSet<String>();
  protected Set<String> objectInstanceNamesBeingReserved =
    new HashSet<String>();

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

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final Marker marker;

  public FederateObjectManager(Federate federate)
  {
    this.federate = federate;

    marker = federate.getMarker();
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

  public WriteFuture resignFederationExecution(ResignAction resignAction)
    throws OwnershipAcquisitionPending, FederateOwnsAttributes,
           RTIinternalError
  {
    objectsLock.readLock().lock();
    try
    {
      for (ObjectInstance objectInstance : objects.values())
      {
        objectInstance.checkIfOwnershipAcquisitionPending();
        objectInstance.checkIfFederateOwnsAttributes();
      }

      return federate.getRTISession().write(
        new ResignFederationExecution(resignAction));
    }
    finally
    {
      objectsLock.readLock().unlock();
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

  public WriteFuture subscribeObjectClassAttributes(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles,
    boolean passive)
    throws RTIinternalError
  {
    subscriptionLock.writeLock().lock();
    try
    {
      subscriptionManager.subscribeObjectClassAttributes(
        objectClassHandle, attributeHandles, passive);

      return federate.getRTISession().write(
        new SubscribeObjectClassAttributes(
          objectClassHandle, attributeHandles, passive));
    }
    finally
    {
      subscriptionLock.writeLock().unlock();
    }
  }

  public WriteFuture unsubscribeObjectClass(ObjectClassHandle objectClassHandle)
    throws RTIinternalError
  {
    subscriptionLock.writeLock().lock();
    try
    {
      subscriptionManager.unsubscribeObjectClass(objectClassHandle);

      return federate.getRTISession().write(
        new UnsubscribeObjectClassAttributes(objectClassHandle));
    }
    finally
    {
      subscriptionLock.writeLock().unlock();
    }
  }

  public WriteFuture unsubscribeObjectClassAttributes(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws RTIinternalError
  {
    subscriptionLock.writeLock().lock();
    try
    {
      subscriptionManager.unsubscribeObjectClassAttributes(
        objectClassHandle, attributeHandles);

      return federate.getRTISession().write(
        new UnsubscribeObjectClassAttributes(
          objectClassHandle, attributeHandles));
    }
    finally
    {
      subscriptionLock.writeLock().unlock();
    }
  }

  public WriteFuture subscribeInteractionClass(
    InteractionClassHandle interactionClassHandle, boolean passive)
    throws RTIinternalError
  {
    subscriptionLock.writeLock().lock();
    try
    {
      subscriptionManager.subscribeInteractionClass(
        interactionClassHandle, passive);

      return federate.getRTISession().write(
        new SubscribeInteractionClass(interactionClassHandle, passive));
    }
    finally
    {
      subscriptionLock.writeLock().unlock();
    }
  }

  public WriteFuture unsubscribeInteractionClass(
    InteractionClassHandle interactionClassHandle)
    throws RTIinternalError
  {
    subscriptionLock.writeLock().lock();
    try
    {
      subscriptionManager.unsubscribeInteractionClass(interactionClassHandle);

      return federate.getRTISession().write(
        new UnsubscribeInteractionClass(interactionClassHandle));
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

  public WriteFuture reserveObjectInstanceName(String name)
    throws IllegalName, RTIinternalError
  {
    reservedObjectInstanceNamesLock.lock();
    try
    {
      if (reservedObjectInstanceNames.contains(name))
      {
        throw new IllegalName(String.format(
          "object instance name already reserved: %s", name));
      }
      else if (objectInstanceNamesBeingReserved.contains(name))
      {
        throw new IllegalName(String.format(
          "object instance name already being reserved: %s", name));
      }

      objectInstanceNamesBeingReserved.add(name);
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

    return federate.getRTISession().write(new ReserveObjectInstanceName(name));
  }

  public ObjectInstanceHandle registerObjectInstance(
    ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined, ObjectClassNotPublished, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    ObjectClass objectClass =
      federate.getFDD().getObjectClass(objectClassHandle);
    assert objectClass != null;

    Set<AttributeHandle> publishedAttributeHandles;
    RegisterObjectInstance registerObjectInstance;
    WriteFuture writeFuture;

    publicationLock.readLock().lock();
    try
    {
      publishedAttributeHandles =
        getPublishedObjectClassAttributes(objectClassHandle);

      registerObjectInstance = new RegisterObjectInstance(
        objectClassHandle, publishedAttributeHandles);
      writeFuture = federate.getRTISession().write(registerObjectInstance);
    }
    finally
    {
      publicationLock.readLock().unlock();
    }

    // TODO: set timeout
    //
    writeFuture.join();

    if (!writeFuture.isWritten())
    {
      throw new RTIinternalError("error communicating with RTI");
    }

    Object response;
    try
    {
      // TODO: set timeout
      //
      response = registerObjectInstance.getResponse();
    }
    catch (InterruptedException ie)
    {
      throw new RTIinternalError("interrupted awaiting timeout", ie);
    }
    catch (ExecutionException ee)
    {
      throw new RTIinternalError("unable to get response", ee);
    }

    assert response instanceof ObjectInstanceHandle :
      String.format("unexpected response: %s", response);

    ObjectInstanceHandle objectInstanceHandle = (ObjectInstanceHandle) response;

    // TODO: get this from response
    //
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

    return objectInstanceHandle;
  }

  public ObjectInstanceHandle registerObjectInstance(
    ObjectClassHandle objectClassHandle, String name)
    throws ObjectClassNotDefined, ObjectClassNotPublished,
           ObjectInstanceNameNotReserved, ObjectInstanceNameInUse,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    ObjectClass objectClass =
      federate.getFDD().getObjectClass(objectClassHandle);

    Set<AttributeHandle> publishedAttributeHandles;
    RegisterObjectInstance registerObjectInstance;
    WriteFuture writeFuture;

    publicationLock.readLock().lock();
    try
    {
      publishedAttributeHandles =
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

      registerObjectInstance = new RegisterObjectInstance(
        objectClassHandle, publishedAttributeHandles);
      writeFuture = federate.getRTISession().write(registerObjectInstance);
    }
    finally
    {
      publicationLock.readLock().unlock();
    }

    // TODO: set timeout
    //
    writeFuture.join();

    if (!writeFuture.isWritten())
    {
      throw new RTIinternalError("error communicating with RTI");
    }

    Object response;
    try
    {
      // TODO: set timeout
      //
      response = registerObjectInstance.getResponse();
    }
    catch (InterruptedException ie)
    {
      throw new RTIinternalError("interrupted awaiting timeout", ie);
    }
    catch (ExecutionException ee)
    {
      throw new RTIinternalError("unable to get response", ee);
    }

    assert response instanceof ObjectInstanceHandle :
      String.format("unexpected response: %s", response);

    ObjectInstanceHandle objectInstanceHandle = (ObjectInstanceHandle) response;

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

    return objectInstanceHandle;
  }

  public WriteFuture updateAttributeValues(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleValueMap attributeValues, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           RTIinternalError
  {
    objectsLock.readLock().lock();
    try
    {
      return getObjectInstance(objectInstanceHandle).updateAttributeValues(
        attributeValues, tag, federate.getRTISession());
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public WriteFuture updateAttributeValues(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleValueMap attributeValues, byte[] tag, LogicalTime updateTime,
    MessageRetractionHandle messageRetractionHandle, OrderType sentOrderType)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           RTIinternalError
  {
    objectsLock.readLock().lock();
    try
    {
      return getObjectInstance(objectInstanceHandle).updateAttributeValues(
        attributeValues, tag, updateTime, messageRetractionHandle,
        sentOrderType, federate.getRTISession());
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public WriteFuture sendInteraction(
    InteractionClassHandle interactionClassHandle,
    ParameterHandleValueMap parameterValues, byte[] tag)
    throws InteractionClassNotPublished, RTIinternalError
  {
    publicationLock.readLock().lock();
    try
    {
      checkIfInteractionClassNotPublished(interactionClassHandle);

      return federate.getRTISession().write(
        new SendInteraction(
          interactionClassHandle, parameterValues, tag, OrderType.RECEIVE,
          TransportationType.HLA_RELIABLE));
    }
    finally
    {
      publicationLock.readLock().unlock();
    }
  }

  public WriteFuture sendInteraction(
    InteractionClassHandle interactionClassHandle,
    ParameterHandleValueMap parameterValues, byte[] tag, LogicalTime sendTime,
    MessageRetractionHandle messageRetractionHandle, OrderType sentOrderType)
    throws InteractionClassNotPublished, RTIinternalError
  {
    publicationLock.readLock().lock();
    try
    {
      checkIfInteractionClassNotPublished(interactionClassHandle);

      return federate.getRTISession().write(
        new SendInteraction(
          interactionClassHandle, parameterValues, tag, sentOrderType,
          TransportationType.HLA_RELIABLE, sendTime, messageRetractionHandle));
    }
    finally
    {
      publicationLock.readLock().unlock();
    }
  }

  public WriteFuture deleteObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag)
    throws ObjectInstanceNotKnown, DeletePrivilegeNotHeld, RTIinternalError
  {
    objectsLock.writeLock().lock();
    try
    {
      ObjectInstance objectInstance = getObjectInstance(objectInstanceHandle);
      objectInstance.deleteObjectInstance(tag);

      removeObjectInstance(objectInstance);

      return federate.getRTISession().write(
        new DeleteObjectInstance(objectInstanceHandle, tag, OrderType.RECEIVE));
    }
    finally
    {
      objectsLock.writeLock().unlock();
    }
  }

  public WriteFuture deleteObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag,
    LogicalTime deleteTime, MessageRetractionHandle messageRetractionHandle,
    OrderType sentOrderType)
    throws ObjectInstanceNotKnown, DeletePrivilegeNotHeld, RTIinternalError
  {
    objectsLock.writeLock().lock();
    try
    {
      ObjectInstance objectInstance = getObjectInstance(objectInstanceHandle);
      objectInstance.deleteObjectInstance(tag);

      if (sentOrderType == OrderType.RECEIVE)
      {
        removeObjectInstance(objectInstance);
      }

      return federate.getRTISession().write(
        new RemoveObjectInstance(
          objectInstanceHandle, tag, sentOrderType, deleteTime,
          messageRetractionHandle));
    }
    finally
    {
      objectsLock.writeLock().unlock();
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
  }

  public void objectInstanceNameReservationSucceeded(
    String name, FederateAmbassador federateAmbassador)
  {
    try
    {
      reservedObjectInstanceNamesLock.lock();
      try
      {
        objectInstanceNamesBeingReserved.remove(name);
        reservedObjectInstanceNames.add(name);

        federateAmbassador.objectInstanceNameReservationSucceeded(name);
      }
      finally
      {
        reservedObjectInstanceNamesLock.unlock();
      }
    }
    catch (Throwable t)
    {
      log.warn(marker, String.format(
        "federate could not process name reservation success: %s", name), t);
    }
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
        ObjectInstance objectInstance =
          new ObjectInstance(objectInstanceHandle, objectClass, name);

        objectsLock.writeLock().lock();
        try
        {
          objects.put(objectInstanceHandle, objectInstance);
          objectsByName.put(name, objectInstance);
          getObjectsByClassHandle(objectClassHandle).add(objectInstanceHandle);

          objectInstance.discoverObjectInstance(
            objectInstanceHandle, objectClassHandle, name, federateAmbassador);
        }
        catch (Throwable t)
        {
          log.warn(marker, String.format(
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
      objectsLock.readLock().lock();
      try
      {
        ObjectInstance objectInstance = objects.get(objectInstanceHandle);
        if (objectInstance != null)
        {
          ObjectClass objectClass = objectInstance.getObjectClass();
          ObjectClass subscribedObjectClass =
            subscriptionManager.getSubscribedObjectClass(objectClass);

          if (subscribedObjectClass != null)
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
      }
      catch (Throwable t)
      {
        log.warn(marker, String.format(
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
      log.warn(marker, String.format(
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
    catch (Throwable t)
    {
      log.warn(marker, String.format(
        "federate unable to remove object instance: %s",
        objectInstanceHandle), t);
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
    catch (Throwable t)
    {
      log.warn(marker, String.format(
        "federate unable to acquire object instance attributes: %s - %s",
        objectInstanceHandle, attributeHandles), t);
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
        attributeHandles, tag, federate.getRTISession());
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
      if (!reservedObjectInstanceNames.contains(name))
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
      if (reservedObjectInstanceNames.contains(objectInstance.getName()))
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

  protected class ObjectManagerSubscriptionManager
    extends SubscriptionManager
  {
    public void federateJoined(IoSession session)
    {
      for (Map.Entry<ObjectClassHandle, Map<AttributeHandle, AttributeSubscription>> entry :
        subscribedObjectClasses.entrySet())
      {
        AttributeHandleSet passiveDefaultRegionAttributeHandles =
          new IEEE1516AttributeHandleSet();
        AttributeHandleSet defaultRegionAttributeHandles =
          new IEEE1516AttributeHandleSet();

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

  public static class ObjectInstance
    implements Serializable
  {
    protected final ObjectInstanceHandle objectInstanceHandle;
    protected final ObjectClass objectClass;
    protected final String name;

    protected Map<AttributeHandle, AttributeInstance> attributes =
      new HashMap<AttributeHandle, AttributeInstance>();

    protected Set<AttributeHandle> attributeHandlesBeingAcquired =
      new HashSet<AttributeHandle>();
    protected Set<AttributeHandle> attributeHandlesBeingAcquiredIfAvailable =
      new HashSet<AttributeHandle>();

    protected ReadWriteLock objectLock = new ReentrantReadWriteLock(true);

    public ObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                          ObjectClass objectClass, String name)
    {
      this.objectInstanceHandle = objectInstanceHandle;
      this.objectClass = objectClass;
      this.name = name;
    }

    public ObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                          ObjectClass objectClass, String name,
                          Set<AttributeHandle> publishedAttributeHandles)
    {
      this(objectInstanceHandle, objectClass, name);

      for (AttributeHandle attributeHandle : publishedAttributeHandles)
      {
        Attribute attribute = objectClass.getAttributes().get(attributeHandle);
        assert attribute != null;

        // create an attribute instance for each of the published attributes
        //
        attributes.put(attributeHandle, new AttributeInstance(attribute));
      }

      Attribute attribute = objectClass.getAttributesByName().get(
        Attribute.HLA_PRIVILEGE_TO_DELETE_OBJECT);
      assert attribute != null;
      if (!attributes.containsKey(attribute.getAttributeHandle()))
      {
        // create an attribute instance for the HLA privilege to delete
        // object attribute
        //
        attributes.put(attribute.getAttributeHandle(),
                       new AttributeInstance(attribute));
      }
    }

    public ObjectInstanceHandle getObjectInstanceHandle()
    {
      return objectInstanceHandle;
    }

    public ObjectClassHandle getObjectClassHandle()
    {
      return objectClass.getObjectClassHandle();
    }

    public ObjectClass getObjectClass()
    {
      return objectClass;
    }

    public String getName()
    {
      return name;
    }

    public WriteFuture updateAttributeValues(
      AttributeHandleValueMap attributeValues, byte[] tag, IoSession rtiSession)
      throws AttributeNotDefined, AttributeNotOwned
    {
      objectLock.readLock().lock();
      try
      {
        checkIfAttributeNotOwned(attributeValues.keySet());

        RegionHandleSet sentRegionHandles = new IEEE1516RegionHandleSet();
        for (AttributeHandle attributeHandle : attributeValues.keySet())
        {
          sentRegionHandles.addAll(
            getAttributeInstance(attributeHandle).getAssociatedRegions());
        }

        return rtiSession.write(new UpdateAttributeValues(
          objectInstanceHandle, attributeValues, tag, sentRegionHandles,
          OrderType.RECEIVE, TransportationType.HLA_RELIABLE));
      }
      finally
      {
        objectLock.readLock().unlock();
      }
    }

    public WriteFuture updateAttributeValues(
      AttributeHandleValueMap attributeValues, byte[] tag,
      LogicalTime updateTime, MessageRetractionHandle messageRetractionHandle,
      OrderType sentOrderType, IoSession rtiSession)
      throws AttributeNotDefined, AttributeNotOwned
    {
      objectLock.readLock().lock();
      try
      {
        if (sentOrderType == OrderType.TIMESTAMP)
        {
          // TODO: divide attributes by order type
        }

        RegionHandleSet sentRegionHandles = new IEEE1516RegionHandleSet();
        for (AttributeHandle attributeHandle : attributeValues.keySet())
        {
          sentRegionHandles.addAll(
            getAttributeInstance(attributeHandle).getAssociatedRegions());
        }

        return rtiSession.write(new UpdateAttributeValues(
          objectInstanceHandle, attributeValues, tag, sentRegionHandles,
          sentOrderType, TransportationType.HLA_RELIABLE, updateTime,
          messageRetractionHandle));
      }
      finally
      {
        objectLock.readLock().unlock();
      }
    }

    public void deleteObjectInstance(byte[] tag)
      throws DeletePrivilegeNotHeld
    {
      objectLock.readLock().lock();
      try
      {
        Attribute attribute = objectClass.getAttributesByName().get(
          Attribute.HLA_PRIVILEGE_TO_DELETE_OBJECT);
        assert attribute != null;
        if (!attributes.containsKey(attribute.getAttributeHandle()))
        {
          throw new DeletePrivilegeNotHeld(objectInstanceHandle.toString());
        }
      }
      finally
      {
        objectLock.readLock().unlock();
      }
    }

    public void localDeleteObjectInstance()
      throws FederateOwnsAttributes
    {
      objectLock.readLock().lock();
      try
      {
        if (!attributes.isEmpty())
        {
          throw new FederateOwnsAttributes(String.format(
            "%s - %s", objectInstanceHandle, attributes.keySet()));
        }
      }
      finally
      {
        objectLock.readLock().unlock();
      }
    }

    public void unconditionalAttributeOwnershipDivestiture(
      AttributeHandleSet attributeHandles, IoSession rtiSession)
      throws AttributeNotDefined, AttributeNotOwned, RTIinternalError
    {
      WriteFuture writeFuture;

      objectLock.writeLock().lock();
      try
      {
        checkIfAttributeNotOwned(attributeHandles);

        attributes.keySet().removeAll(attributeHandles);

        writeFuture = rtiSession.write(
          new UnconditionalAttributeOwnershipDivestiture(
            objectInstanceHandle, attributeHandles));
      }
      finally
      {
        objectLock.writeLock().unlock();
      }

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }
    }

    public void negotiatedAttributeOwnershipDivestiture(
      AttributeHandleSet attributeHandles, byte[] tag, IoSession rtiSession)
      throws AttributeNotDefined, AttributeNotOwned,
             AttributeAlreadyBeingDivested, RTIinternalError
    {
      WriteFuture writeFuture;

      objectLock.writeLock().lock();
      try
      {
        checkIfAttributeAlreadyBeingDivested(attributeHandles);

        for (AttributeHandle attributeHandle : attributeHandles)
        {
          getAttributeInstance(
            attributeHandle).negotiatedAttributeOwnershipDivestiture();
        }

        writeFuture = rtiSession.write(
          new NegotiatedAttributeOwnershipDivestiture(
            objectInstanceHandle, attributeHandles, tag));
      }
      finally
      {
        objectLock.writeLock().unlock();
      }

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }
    }

    public void confirmDivestiture(AttributeHandleSet attributeHandles,
                                   byte[] tag, IoSession rtiSession)
      throws AttributeNotDefined, AttributeNotOwned,
             AttributeDivestitureWasNotRequested, RTIinternalError
    {
      WriteFuture writeFuture;

      objectLock.writeLock().lock();
      try
      {
        checkIfAttributeDivestitureWasNotRequested(attributeHandles);

        for (AttributeHandle attributeHandle : attributeHandles)
        {
          getAttributeInstance(attributeHandle).confirmDivestiture();
          attributes.remove(attributeHandle);
        }

        writeFuture = rtiSession.write(
          new ConfirmDivestiture(objectInstanceHandle, attributeHandles, tag));
      }
      finally
      {
        objectLock.writeLock().unlock();
      }

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }
    }

    public void attributeOwnershipAcquisition(
      AttributeHandleSet attributeHandles, byte[] tag, IoSession rtiSession)
      throws FederateOwnsAttributes, RTIinternalError
    {
      WriteFuture writeFuture;

      objectLock.writeLock().lock();
      try
      {
        checkIfFederateOwnsAttributes(attributeHandles);

        attributeHandlesBeingAcquired.addAll(attributeHandles);

        writeFuture =
          rtiSession.write(new AttributeOwnershipAcquisition(
            objectInstanceHandle, attributeHandles, tag));
      }
      finally
      {
        objectLock.writeLock().unlock();
      }

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }
    }

    public void attributeOwnershipAcquisitionIfAvailable(
      AttributeHandleSet attributeHandles, IoSession rtiSession)
      throws AttributeNotDefined, FederateOwnsAttributes,
             AttributeAlreadyBeingAcquired, RTIinternalError
    {
      WriteFuture writeFuture;

      objectLock.writeLock().lock();
      try
      {
        Set<AttributeHandle> ownedAttributeHandles =
          new HashSet<AttributeHandle>();
        Set<AttributeHandle> attributeHandlesAlreadyBeingAcquired =
          new HashSet<AttributeHandle>();

        for (AttributeHandle attributeHandle : attributeHandles)
        {
          objectClass.checkIfAttributeNotDefined(attributeHandle);

          if (attributes.containsKey(attributeHandle))
          {
            ownedAttributeHandles.add(attributeHandle);
          }
          else if (attributeHandlesBeingAcquiredIfAvailable.contains(
            attributeHandle))
          {
            attributeHandlesAlreadyBeingAcquired.add(attributeHandle);
          }
        }

        if (!ownedAttributeHandles.isEmpty())
        {
          throw new FederateOwnsAttributes(String.format(
            "%s - %s", objectInstanceHandle, ownedAttributeHandles));
        }
        else if (!attributeHandlesAlreadyBeingAcquired.isEmpty())
        {
          throw new AttributeAlreadyBeingAcquired(
            attributeHandlesAlreadyBeingAcquired.toString());
        }

        attributeHandlesBeingAcquiredIfAvailable.addAll(attributeHandles);

        writeFuture =
          rtiSession.write(new AttributeOwnershipAcquisitionIfAvailable(
            objectInstanceHandle, attributeHandles));
      }
      finally
      {
        objectLock.writeLock().unlock();
      }

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }
    }

    public AttributeHandleSet attributeOwnershipDivestitureIfWanted(
      AttributeHandleSet attributeHandles, IoSession rtiSession)
      throws AttributeNotDefined, AttributeNotOwned, RTIinternalError
    {
      objectLock.writeLock().lock();
      try
      {
        checkIfAttributeNotOwned(attributeHandles);

        AttributeOwnershipDivestitureIfWanted
          attributeOwnershipDivestitureIfWanted =
          new AttributeOwnershipDivestitureIfWanted(
            objectInstanceHandle, attributeHandles);
        WriteFuture writeFuture =
          rtiSession.write(attributeOwnershipDivestitureIfWanted);

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
          Object response = attributeOwnershipDivestitureIfWanted.getResponse();

          assert response instanceof AttributeOwnershipDivestitureIfWantedResponse :
            String.format("unexpected response: %s", response);

          AttributeOwnershipDivestitureIfWantedResponse
            attributeOwnershipDivestitureIfWantedResponse =
            (AttributeOwnershipDivestitureIfWantedResponse) response;

          AttributeHandleSet divestedAttributeHandles =
            attributeOwnershipDivestitureIfWantedResponse.getAttributeHandles();

          attributes.keySet().removeAll(divestedAttributeHandles);

          // confirm that the attributes have been divested
          //
          writeFuture = rtiSession.write(new DefaultResponse(
            attributeOwnershipDivestitureIfWantedResponse.getId()));

          if (!writeFuture.isWritten())
          {
            throw new RTIinternalError("error communicating with RTI");
          }

          return divestedAttributeHandles;
        }
        catch (InterruptedException ie)
        {
          throw new RTIinternalError("interrupted awaitng response", ie);
        }
        catch (ExecutionException ee)
        {
          throw new RTIinternalError("unable to get response", ee);
        }
      }
      finally
      {
        objectLock.writeLock().unlock();
      }
    }

    public void cancelNegotiatedAttributeOwnershipDivestiture(
      AttributeHandleSet attributeHandles, IoSession rtiSession)
      throws AttributeNotDefined, AttributeNotOwned,
             AttributeDivestitureWasNotRequested, RTIinternalError
    {
      WriteFuture writeFuture;

      objectLock.readLock().lock();
      try
      {
        for (AttributeHandle attributeHandle : attributeHandles)
        {
          getAttributeInstance(
            attributeHandle).checkIfAttributeDivestitureWasNotRequested();
        }

        for (AttributeHandle attributeHandle : attributeHandles)
        {
          getAttributeInstance(
            attributeHandle).cancelNegotiatedAttributeOwnershipDivestiture();
        }

        writeFuture = rtiSession.write(
          new CancelNegotiatedAttributeOwnershipDivestiture(
            objectInstanceHandle, attributeHandles));
      }
      finally
      {
        objectLock.readLock().unlock();
      }

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }
    }

    public void cancelAttributeOwnershipAcquisition(
      AttributeHandleSet attributeHandles, IoSession rtiSession)
      throws AttributeNotDefined, AttributeAlreadyOwned,
             AttributeAcquisitionWasNotRequested, RTIinternalError
    {
      WriteFuture writeFuture;

      objectLock.readLock().lock();
      try
      {
        for (AttributeHandle attributeHandle : attributeHandles)
        {
          AttributeInstance attributeInstance = attributes.get(attributeHandle);
          if (attributeInstance != null)
          {
            throw new AttributeAlreadyOwned(String.format("%s", attributeHandle));
          }
          else if (!attributeHandlesBeingAcquired.contains(attributeHandle))
          {
            throw new AttributeAcquisitionWasNotRequested(
              String.format("%s", attributeHandle));
          }
          else
          {
            objectClass.checkIfAttributeNotDefined(attributeHandle);
          }
        }

        for (AttributeHandle attributeHandle : attributeHandles)
        {
          attributes.get(attributeHandle).cancelAttributeOwnershipAcquisition();
        }

        writeFuture = rtiSession.write(
          new CancelAttributeOwnershipAcquisition(
            objectInstanceHandle, attributeHandles));
      }
      finally
      {
        objectLock.readLock().unlock();
      }

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }
    }

    public void queryAttributeOwnership(AttributeHandle attributeHandle,
                                        IoSession rtiSession)
      throws AttributeNotDefined, RTIinternalError
    {
      objectClass.checkIfAttributeNotDefined(attributeHandle);

      WriteFuture writeFuture;

      objectLock.readLock().lock();
      try
      {
        writeFuture = rtiSession.write(
          new QueryAttributeOwnership(objectInstanceHandle, attributeHandle));
      }
      finally
      {
        objectLock.readLock().unlock();
      }

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }
    }

    public boolean isAttributeOwnedByFederate(AttributeHandle attributeHandle)
      throws AttributeNotDefined
    {
      boolean owned;

      objectLock.readLock().lock();
      try
      {
        owned = attributes.get(attributeHandle) != null;
      }
      finally
      {
        objectLock.readLock().unlock();
      }

      if (!owned)
      {
        objectClass.checkIfAttributeNotDefined(attributeHandle);
      }

      return owned;
    }

    public void changeAttributeOrderType(Set<AttributeHandle> attributeHandles,
                                         OrderType orderType)
      throws AttributeNotDefined, AttributeNotOwned
    {
      objectLock.readLock().lock();
      try
      {
        for (AttributeHandle attributeHandle : attributeHandles)
        {
          getAttributeInstance(attributeHandle).setOrderType(orderType);
        }
      }
      finally
      {
        objectLock.readLock().unlock();
      }
    }

    public void changeAttributeTransportationType(
      Set<AttributeHandle> attributeHandles,
      TransportationType transportationType)
      throws AttributeNotDefined, AttributeNotOwned
    {
      objectLock.readLock().lock();
      try
      {
        for (AttributeHandle attributeHandle : attributeHandles)
        {
          getAttributeInstance(attributeHandle).setTransportationType(
            transportationType);
        }
      }
      finally
      {
        objectLock.readLock().unlock();
      }
    }

    public WriteFuture requestAttributeValueUpdate(
      AttributeHandleSet attributeHandles, byte[] tag, IoSession rtiSession)
      throws AttributeNotDefined
    {
      objectClass.checkIfAttributeNotDefined(attributeHandles);

      attributeHandles = new IEEE1516AttributeHandleSet(attributeHandles);

      objectLock.readLock().lock();
      try
      {
        // only request updates for un-owned attributes
        //
        attributeHandles.removeAll(attributes.keySet());

        return rtiSession.write(new RequestAttributeValueUpdate(
          objectInstanceHandle, attributeHandles, tag));
      }
      finally
      {
        objectLock.readLock().unlock();
      }
    }

    public void associateRegionsForUpdates(
      AttributeRegionAssociation attributeRegionAssociation)
    {
      objectLock.readLock().lock();
      try
      {
        for (AttributeHandle attributeHandle : attributeRegionAssociation.attributes)
        {
          AttributeInstance attribute = attributes.get(attributeHandle);
          if (attribute != null)
          {
            attribute.associateRegionsForUpdates(
              attributeRegionAssociation.regions);
          }
        }
      }
      finally
      {
        objectLock.readLock().unlock();
      }
    }

    public void unassociateRegionsForUpdates(
      AttributeRegionAssociation attributeRegionAssociation)
    {
      objectLock.readLock().lock();
      try
      {
        for (AttributeHandle attributeHandle : attributeRegionAssociation.attributes)
        {
          AttributeInstance attribute = attributes.get(attributeHandle);
          if (attribute != null)
          {
            attribute.unassociateRegionsForUpdates(
              attributeRegionAssociation.regions);
          }
        }
      }
      finally
      {
        objectLock.readLock().unlock();
      }
    }

    public void unpublishObjectClassAttributes(
      AttributeHandleSet attributeHandles, IoSession rtiSession)
      throws RTIinternalError
    {
      WriteFuture writeFuture;

      objectLock.writeLock().lock();
      try
      {
        attributes.keySet().removeAll(attributeHandles);

        writeFuture = rtiSession.write(
          new UnconditionalAttributeOwnershipDivestiture(
            objectInstanceHandle, attributeHandles));
      }
      finally
      {
        objectLock.writeLock().unlock();
      }

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }
    }

    public void checkIfOwnershipAcquisitionPending()
      throws OwnershipAcquisitionPending
    {
      objectLock.writeLock().lock();
      try
      {
        if (!attributeHandlesBeingAcquired.isEmpty() ||
            !attributeHandlesBeingAcquiredIfAvailable.isEmpty())
        {
          AttributeHandleSet allAttributeHandlesBeingAcquired =
            new IEEE1516AttributeHandleSet(attributeHandlesBeingAcquired);
          allAttributeHandlesBeingAcquired.addAll(
            attributeHandlesBeingAcquiredIfAvailable);
          throw new OwnershipAcquisitionPending(
            allAttributeHandlesBeingAcquired.toString());
        }
      }
      finally
      {
        objectLock.writeLock().unlock();
      }
    }

    public void checkIfOwnershipAcquisitionPending(
      Set<AttributeHandle> attributeHandles)
      throws OwnershipAcquisitionPending
    {
      objectLock.writeLock().lock();
      try
      {
        AttributeHandleSet allAttributeHandlesBeingAcquired =
          new IEEE1516AttributeHandleSet();
        for (AttributeHandle attributeHandle : attributeHandles)
        {
          if (attributeHandlesBeingAcquired.contains(attributeHandle) ||
              attributeHandlesBeingAcquiredIfAvailable.contains(attributeHandle))
          {
            allAttributeHandlesBeingAcquired.add(attributeHandle);
          }
        }

        if (!allAttributeHandlesBeingAcquired.isEmpty())
        {
          throw new OwnershipAcquisitionPending(
            allAttributeHandlesBeingAcquired.toString());
        }
      }
      finally
      {
        objectLock.writeLock().unlock();
      }
    }

    public void checkIfFederateOwnsAttributes()
      throws FederateOwnsAttributes
    {
      objectLock.readLock().lock();
      try
      {
        if (!attributes.isEmpty())
        {
          throw new FederateOwnsAttributes(String.format(
            "%s - %s", objectInstanceHandle, attributes.keySet()));
        }
      }
      finally
      {
        objectLock.readLock().unlock();
      }
    }

    public int hashCode()
    {
      return objectInstanceHandle.hashCode();
    }

    public boolean equals(Object rhs)
    {
      return rhs instanceof ObjectInstance && equals((ObjectInstance) rhs);
    }

    public boolean equals(ObjectInstance rhs)
    {
      return rhs != null && objectInstanceHandle.equals(rhs.objectInstanceHandle);
    }

    public String toString()
    {
      return String.format("%s (%s, %s)", objectInstanceHandle, name,
                           objectClass);
    }

    protected AttributeInstance getAttributeInstance(
      AttributeHandle attributeHandle)
      throws AttributeNotDefined, AttributeNotOwned
    {
      AttributeInstance attributeInstance = attributes.get(attributeHandle);
      if (attributeInstance == null)
      {
        // it might not be defined
        //
        objectClass.checkIfAttributeNotDefined(attributeHandle);

        throw new AttributeNotOwned(String.format("%s", attributeHandle));
      }
      return attributeInstance;
    }

    protected void checkIfAttributeNotOwned(Set<AttributeHandle> attributeHandles)
      throws AttributeNotDefined, AttributeNotOwned
    {
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        if (!attributes.containsKey(attributeHandle))
        {
          // it might not be defined
          //
          objectClass.checkIfAttributeNotDefined(attributeHandle);

          throw new AttributeNotOwned(String.format("%s", attributeHandle));
        }
      }
    }

    protected void checkIfAttributeAlreadyBeingDivested(
      Set<AttributeHandle> attributeHandles)
      throws AttributeNotDefined, AttributeNotOwned, AttributeAlreadyBeingDivested
    {
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        getAttributeInstance(
          attributeHandle).checkIfAttributeAlreadyBeingDivested();
      }
    }

    protected void checkIfAttributeDivestitureWasNotRequested(
      Set<AttributeHandle> attributeHandles)
      throws AttributeNotDefined, AttributeNotOwned,
             AttributeDivestitureWasNotRequested
    {
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        getAttributeInstance(
          attributeHandle).checkIfAttributeDivestitureWasNotRequested();
      }
    }

    protected void checkIfFederateOwnsAttributes(
      Set<AttributeHandle> attributeHandles)
      throws FederateOwnsAttributes
    {
      Set<AttributeHandle> ownedAttributeHandles = null;
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        if (attributes.containsKey(attributeHandle))
        {
          if (ownedAttributeHandles == null)
          {
            ownedAttributeHandles = new HashSet<AttributeHandle>();
          }
          ownedAttributeHandles.add(attributeHandle);
        }
      }
      if (ownedAttributeHandles != null)
      {
        throw new FederateOwnsAttributes(String.format(
          "%s - %s", objectInstanceHandle, ownedAttributeHandles));
      }
    }

    public void discoverObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                                       ObjectClassHandle objectClassHandle,
                                       String name,
                                       FederateAmbassador federateAmbassador)
      throws ObjectClassNotRecognized, CouldNotDiscover, FederateInternalError
    {
      objectLock.readLock().lock();
      try
      {
        federateAmbassador.discoverObjectInstance(
          objectInstanceHandle, objectClassHandle, name);
      }
      finally
      {
        objectLock.readLock().unlock();
      }
    }

    public void reflectAttributeValues(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleValueMap attributeValues, byte[] tag,
      OrderType sentOrderType, TransportationType transportationType,
      LogicalTime updateTime, OrderType receivedOrderType,
      MessageRetractionHandle messageRetractionHandle,
      RegionHandleSet sentRegionHandles, FederateAmbassador federateAmbassador)
      throws AttributeNotSubscribed, InvalidLogicalTime, FederateInternalError,
             AttributeNotRecognized, ObjectInstanceNotKnown
    {
      objectLock.readLock().lock();
      try
      {
        if (updateTime == null)
        {
          if (sentRegionHandles == null)
          {
            federateAmbassador.reflectAttributeValues(
              objectInstanceHandle, attributeValues, tag, sentOrderType,
              transportationType);
          }
          else
          {
            federateAmbassador.reflectAttributeValues(
              objectInstanceHandle, attributeValues, tag, sentOrderType,
              transportationType, sentRegionHandles);
          }
        }
        else if (messageRetractionHandle == null)
        {
          if (sentRegionHandles == null)
          {
            federateAmbassador.reflectAttributeValues(
              objectInstanceHandle, attributeValues, tag, sentOrderType,
              transportationType, updateTime, receivedOrderType);
          }
          else
          {
            federateAmbassador.reflectAttributeValues(
              objectInstanceHandle, attributeValues, tag, sentOrderType,
              transportationType, updateTime, receivedOrderType, sentRegionHandles);
          }
        }
        else if (sentRegionHandles == null)
        {
          federateAmbassador.reflectAttributeValues(
            objectInstanceHandle, attributeValues, tag, sentOrderType,
            transportationType, updateTime, receivedOrderType,
            messageRetractionHandle);
        }
        else
        {
          federateAmbassador.reflectAttributeValues(
            objectInstanceHandle, attributeValues, tag, sentOrderType,
            transportationType, updateTime, receivedOrderType,
            messageRetractionHandle, sentRegionHandles);
        }
      }
      finally
      {
        objectLock.readLock().unlock();
      }
    }

    public void removeObjectInstance(
      ObjectInstanceHandle objectInstanceHandle, byte[] tag,
      OrderType sentOrderType, LogicalTime deleteTime,
      OrderType receivedOrderType,
      MessageRetractionHandle messageRetractionHandle,
      FederateAmbassador federateAmbassador)
      throws InvalidLogicalTime, FederateInternalError, ObjectInstanceNotKnown
    {
      objectLock.readLock().lock();
      try
      {
        Attribute attribute = objectClass.getAttributesByName().get(
          Attribute.HLA_PRIVILEGE_TO_DELETE_OBJECT);
        assert attribute != null;
        if (!attributes.containsKey(attribute.getAttributeHandle()))
        {
          if (deleteTime == null)
          {
            federateAmbassador.removeObjectInstance(
              objectInstanceHandle, tag, sentOrderType);
          }
          else if (messageRetractionHandle == null)
          {
            federateAmbassador.removeObjectInstance(
              objectInstanceHandle, tag, sentOrderType, deleteTime,
              receivedOrderType);
          }
          else
          {
            federateAmbassador.removeObjectInstance(
              objectInstanceHandle, tag, sentOrderType, deleteTime,
              receivedOrderType, messageRetractionHandle);
          }
        }
      }
      finally
      {
        objectLock.readLock().unlock();
      }
    }

    public void attributeOwnershipAcquisitionNotification(
      AttributeHandleSet attributeHandles, byte[] tag,
      FederateAmbassador federateAmbassador)
      throws AttributeAlreadyOwned, FederateInternalError,
             AttributeNotRecognized, ObjectInstanceNotKnown,
             AttributeNotPublished, AttributeAcquisitionWasNotRequested
    {
      objectLock.writeLock().lock();
      try
      {
        attributeHandlesBeingAcquired.removeAll(attributeHandles);

        for (AttributeHandle attributeHandle : attributeHandles)
        {
          Attribute attribute = objectClass.getAttributes().get(attributeHandle);
          assert attribute != null;

          // create an attribute instance for each of the published attributes
          //
          attributes.put(attributeHandle, new AttributeInstance(attribute));
        }

        federateAmbassador.attributeOwnershipAcquisitionNotification(
          objectInstanceHandle, attributeHandles, tag);
      }
      finally
      {
        objectLock.writeLock().unlock();
      }
    }

    public static class AttributeInstance
      implements Serializable
    {
      protected final Attribute attribute;

      protected TransportationType transportationType;
      protected OrderType orderType;

      protected RegionHandleSet associatedRegions = new IEEE1516RegionHandleSet();

      public AttributeInstance(Attribute attribute)
      {
        this.attribute = attribute;

        transportationType = attribute.getTransportationType();
        orderType = attribute.getOrderType();
      }

      public Attribute getAttribute()
      {
        return attribute;
      }

      public AttributeHandle getAttributeHandle()
      {
        return attribute.getAttributeHandle();
      }

      public TransportationType getTransportationType()
      {
        return transportationType;
      }

      public void setTransportationType(TransportationType transportationType)
      {
        this.transportationType = transportationType;
      }

      public OrderType getOrderType()
      {
        return orderType;
      }

      public void setOrderType(OrderType orderType)
      {
        this.orderType = orderType;
      }

      public RegionHandleSet getAssociatedRegions()
      {
        return associatedRegions;
      }

      public void associateRegionsForUpdates(RegionHandleSet regionHandles)
      {
        associatedRegions.addAll(regionHandles);
      }

      public void unassociateRegionsForUpdates(RegionHandleSet regionHandles)
      {
        associatedRegions.removeAll(regionHandles);
      }

      public void negotiatedAttributeOwnershipDivestiture()
      {
      }

      public void confirmDivestiture()
      {
      }

      public void cancelNegotiatedAttributeOwnershipDivestiture()
      {
      }

      public void cancelAttributeOwnershipAcquisition()
      {
      }

      public void checkIfAttributeDivestitureWasNotRequested()
        throws AttributeDivestitureWasNotRequested
      {
        // TODO: check status
      }

      public void checkIfAttributeAlreadyBeingDivested()
        throws AttributeAlreadyBeingDivested
      {
        // TODO: check status
      }
    }
  }
}
