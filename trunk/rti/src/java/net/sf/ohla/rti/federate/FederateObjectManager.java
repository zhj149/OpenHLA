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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.SubscriptionManager;
import net.sf.ohla.rti.fdd.InteractionClass;
import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.fdd.TransportationType;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eObjectInstanceHandle;
import net.sf.ohla.rti.messages.DeleteObjectInstance;
import net.sf.ohla.rti.messages.RegisterObjectInstance;
import net.sf.ohla.rti.messages.ReleaseMultipleObjectInstanceName;
import net.sf.ohla.rti.messages.ReleaseObjectInstanceName;
import net.sf.ohla.rti.messages.ReserveMultipleObjectInstanceName;
import net.sf.ohla.rti.messages.ReserveObjectInstanceName;
import net.sf.ohla.rti.messages.SendInteraction;
import net.sf.ohla.rti.messages.SubscribeInteractionClass;
import net.sf.ohla.rti.messages.SubscribeObjectClassAttributes;
import net.sf.ohla.rti.messages.UnpublishObjectClass;
import net.sf.ohla.rti.messages.UnpublishObjectClassAttributes;
import net.sf.ohla.rti.messages.UnsubscribeInteractionClass;
import net.sf.ohla.rti.messages.UnsubscribeObjectClassAttributes;
import net.sf.ohla.rti.messages.callbacks.ReceiveInteraction;
import net.sf.ohla.rti.messages.callbacks.ReflectAttributeValues;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.AttributeRegionAssociation;
import hla.rti1516e.AttributeSetRegionSetPairList;
import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RegionHandleSet;
import hla.rti1516e.ResignAction;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.AttributeAcquisitionWasNotRequested;
import hla.rti1516e.exceptions.AttributeAlreadyBeingAcquired;
import hla.rti1516e.exceptions.AttributeAlreadyBeingChanged;
import hla.rti1516e.exceptions.AttributeAlreadyBeingDivested;
import hla.rti1516e.exceptions.AttributeAlreadyOwned;
import hla.rti1516e.exceptions.AttributeDivestitureWasNotRequested;
import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.AttributeNotOwned;
import hla.rti1516e.exceptions.AttributeNotPublished;
import hla.rti1516e.exceptions.DeletePrivilegeNotHeld;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.FederateOwnsAttributes;
import hla.rti1516e.exceptions.IllegalName;
import hla.rti1516e.exceptions.InteractionClassNotDefined;
import hla.rti1516e.exceptions.InteractionClassNotPublished;
import hla.rti1516e.exceptions.InteractionParameterNotDefined;
import hla.rti1516e.exceptions.InvalidLogicalTime;
import hla.rti1516e.exceptions.InvalidRegion;
import hla.rti1516e.exceptions.InvalidRegionContext;
import hla.rti1516e.exceptions.ObjectClassNotDefined;
import hla.rti1516e.exceptions.ObjectClassNotPublished;
import hla.rti1516e.exceptions.ObjectInstanceNameInUse;
import hla.rti1516e.exceptions.ObjectInstanceNameNotReserved;
import hla.rti1516e.exceptions.ObjectInstanceNotKnown;
import hla.rti1516e.exceptions.OwnershipAcquisitionPending;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RegionNotCreatedByThisFederate;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;

public class FederateObjectManager
{
  private static final Logger log = LoggerFactory.getLogger(FederateObjectManager.class);

  private final Federate federate;

  private final ReadWriteLock publicationLock = new ReentrantReadWriteLock(true);
  private final Map<ObjectClassHandle, AttributeHandleSet> publishedObjectClasses =
    new HashMap<ObjectClassHandle, AttributeHandleSet>();
  private final Set<InteractionClassHandle> publishedInteractionClasses = new HashSet<InteractionClassHandle>();

  private final ReadWriteLock subscriptionLock = new ReentrantReadWriteLock(true);
  private final SubscriptionManager subscriptionManager = new SubscriptionManager();

  private final Lock reservedObjectInstanceNamesLock = new ReentrantLock(true);
  private final Set<String> reservedObjectInstanceNames = new HashSet<String>();
  private final Set<String> objectInstanceNamesBeingReserved = new HashSet<String>();

  private final ReadWriteLock objectsLock = new ReentrantReadWriteLock(true);
  private final Map<ObjectInstanceHandle, FederateObjectInstance> objects = new HashMap<ObjectInstanceHandle, FederateObjectInstance>();
  private final Map<String, FederateObjectInstance> objectsByName = new HashMap<String, FederateObjectInstance>();
  private final Map<ObjectClassHandle, Set<ObjectInstanceHandle>> objectsByObjectClassHandle =
    new HashMap<ObjectClassHandle, Set<ObjectInstanceHandle>>();
  private final Set<ObjectInstanceHandle> removedObjects = new HashSet<ObjectInstanceHandle>();

  private int objectInstanceCount;

  public FederateObjectManager(Federate federate)
  {
    this.federate = federate;
  }

  public void resignFederationExecution(ResignAction resignAction)
    throws OwnershipAcquisitionPending, FederateOwnsAttributes, RTIinternalError
  {
    objectsLock.writeLock().lock();
    try
    {
      for (FederateObjectInstance objectInstance : objects.values())
      {
        switch (resignAction)
        {
          case UNCONDITIONALLY_DIVEST_ATTRIBUTES:
            objectInstance.checkIfOwnershipAcquisitionPending();
            break;
          case DELETE_OBJECTS:
            objectInstance.checkIfFederateOwnsAttributes();
            objectInstance.checkIfOwnershipAcquisitionPending();
            break;
          case CANCEL_PENDING_OWNERSHIP_ACQUISITIONS:
            objectInstance.checkIfFederateOwnsAttributes();
            break;
          case DELETE_OBJECTS_THEN_DIVEST:
            objectInstance.checkIfOwnershipAcquisitionPending();
            break;
          case CANCEL_THEN_DELETE_THEN_DIVEST:
            objectInstance.checkIfOwnershipAcquisitionPending();
            break;
          case NO_ACTION:
            objectInstance.checkIfFederateOwnsAttributes();
            break;
        }
      }
    }
    finally
    {
      objectsLock.writeLock().unlock();
    }
  }

  public void publishObjectClassAttributes(ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
  {
    publicationLock.writeLock().lock();
    try
    {
      AttributeHandleSet publishedAttributeHandles = publishedObjectClasses.get(objectClassHandle);
      if (publishedAttributeHandles == null)
      {
        publishedObjectClasses.put(objectClassHandle, attributeHandles.clone());
      }
      else
      {
        publishedAttributeHandles.addAll(attributeHandles);
      }
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
      AttributeHandleSet publishedAttributeHandles = publishedObjectClasses.remove(objectClassHandle);
      if (publishedAttributeHandles != null && publishedAttributeHandles.size() > 0)
      {
        objectsLock.writeLock().lock();
        try
        {
          // get all known object instances for the specified object class handle
          //
          Set<ObjectInstanceHandle> objectInstanceHandles = objectsByObjectClassHandle.get(objectClassHandle);
          if (objectInstanceHandles != null && !objectInstanceHandles.isEmpty())
          {
            Collection<FederateObjectInstance> unpublishedObjectInstances = new LinkedList<FederateObjectInstance>();

            // see if any objects are acquiring attributes
            //
            for (ObjectInstanceHandle objectInstanceHandle : objectInstanceHandles)
            {
              FederateObjectInstance objectInstance = objects.get(objectInstanceHandle);
              assert objectInstance != null;

              objectInstance.checkIfOwnershipAcquisitionPending();

              if (objectInstance.ownsAny(publishedAttributeHandles))
              {
                unpublishedObjectInstances.add(objectInstance);
              }
            }

            Set<ObjectInstanceHandle> unpublishedObjectInstanceHandles = new HashSet<ObjectInstanceHandle>();
            for (FederateObjectInstance objectInstance : unpublishedObjectInstances)
            {
              objectInstance.unconditionalAttributeOwnershipDivestitureSafely(publishedAttributeHandles);

              unpublishedObjectInstanceHandles.add(objectInstance.getObjectInstanceHandle());
            }

            federate.getRTIChannel().write(new UnpublishObjectClass(unpublishedObjectInstanceHandles));
          }
        }
        finally
        {
          objectsLock.writeLock().unlock();
        }
      }
    }
    finally
    {
      publicationLock.writeLock().unlock();
    }
  }

  public void unpublishObjectClassAttributes(ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws OwnershipAcquisitionPending, RTIinternalError
  {
    publicationLock.writeLock().lock();
    try
    {
      AttributeHandleSet publishedAttributeHandles = publishedObjectClasses.get(objectClassHandle);
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
            // see if any objects are acquiring any attributes that are being unpublished
            //
            for (ObjectInstanceHandle objectInstanceHandle : objectInstanceHandlesToUnpublishAttributes)
            {
              getObjectInstanceSafely(objectInstanceHandle).checkIfOwnershipAcquisitionPending(attributeHandles);
            }

            // this is done within the safety of the objects write lock
            //
            for (ObjectInstanceHandle objectInstanceHandle : objectInstanceHandlesToUnpublishAttributes)
            {
              getObjectInstanceSafely(objectInstanceHandle).unpublishObjectClassAttributes(attributeHandles, federate);
            }

            federate.getRTIChannel().write(new UnpublishObjectClassAttributes(
              objectInstanceHandlesToUnpublishAttributes, attributeHandles));
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

  public void publishInteractionClass(InteractionClassHandle interactionClassHandle)
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

  public void unpublishInteractionClass(InteractionClassHandle interactionClassHandle)
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
    ObjectClass objectClass, AttributeHandleSet attributeHandles, boolean passive)
    throws RTIinternalError
  {
    subscriptionLock.writeLock().lock();
    try
    {
      subscriptionManager.subscribeObjectClassAttributes(objectClass, attributeHandles, passive);

      federate.getRTIChannel().write(
        new SubscribeObjectClassAttributes(objectClass.getObjectClassHandle(), attributeHandles, passive));
    }
    finally
    {
      subscriptionLock.writeLock().unlock();
    }
  }

  public void unsubscribeObjectClass(ObjectClassHandle objectClassHandle)
    throws RTIinternalError
  {
    subscriptionLock.writeLock().lock();
    try
    {
      subscriptionManager.unsubscribeObjectClass(objectClassHandle);

      federate.getRTIChannel().write(new UnsubscribeObjectClassAttributes(objectClassHandle));
    }
    finally
    {
      subscriptionLock.writeLock().unlock();
    }
  }

  public void unsubscribeObjectClassAttributes(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws RTIinternalError
  {
    subscriptionLock.writeLock().lock();
    try
    {
      subscriptionManager.unsubscribeObjectClassAttributes(objectClassHandle, attributeHandles);

      federate.getRTIChannel().write(new UnsubscribeObjectClassAttributes(objectClassHandle, attributeHandles));
    }
    finally
    {
      subscriptionLock.writeLock().unlock();
    }
  }

  public void subscribeInteractionClass(InteractionClass interactionClass, boolean passive)
    throws RTIinternalError
  {
    subscriptionLock.writeLock().lock();
    try
    {
      subscriptionManager.subscribeInteractionClass(interactionClass, passive);

      federate.getRTIChannel().write(
        new SubscribeInteractionClass(interactionClass.getInteractionClassHandle(), passive));
    }
    finally
    {
      subscriptionLock.writeLock().unlock();
    }
  }

  public void unsubscribeInteractionClass(InteractionClassHandle interactionClassHandle)
    throws RTIinternalError
  {
    subscriptionLock.writeLock().lock();
    try
    {
      subscriptionManager.unsubscribeInteractionClass(interactionClassHandle);

      federate.getRTIChannel().write(new UnsubscribeInteractionClass(interactionClassHandle));
    }
    finally
    {
      subscriptionLock.writeLock().unlock();
    }
  }

  public ObjectClassHandle getObjectClassHandle(ObjectInstanceHandle objectInstanceHandle)
    throws ObjectInstanceNotKnown
  {
    objectsLock.readLock().lock();
    try
    {
      return getObjectInstance(objectInstanceHandle).getObjectClass().getObjectClassHandle();
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
      return getObjectInstance(objectInstanceHandle).getObjectInstanceName();
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void reserveObjectInstanceName(String objectInstanceName)
    throws IllegalName, RTIinternalError
  {
    reservedObjectInstanceNamesLock.lock();
    try
    {
      if (objectInstanceNamesBeingReserved.contains(objectInstanceName))
      {
        throw new IllegalName("object instance name already being reserved: " + objectInstanceName);
      }

      objectInstanceNamesBeingReserved.add(objectInstanceName);
    }
    finally
    {
      reservedObjectInstanceNamesLock.unlock();
    }

    federate.getRTIChannel().write(new ReserveObjectInstanceName(objectInstanceName));
  }

  public void releaseObjectInstanceName(String objectInstanceName)
    throws ObjectInstanceNameNotReserved, RTIinternalError
  {
    reservedObjectInstanceNamesLock.lock();
    try
    {
      if (reservedObjectInstanceNames.remove(objectInstanceName))
      {
        federate.getRTIChannel().write(new ReleaseObjectInstanceName(objectInstanceName));
      }
      else
      {
        throw new ObjectInstanceNameNotReserved(objectInstanceName);
      }
    }
    finally
    {
      reservedObjectInstanceNamesLock.unlock();
    }
  }

  public void reserveMultipleObjectInstanceName(Set<String> objectInstanceNames)
    throws IllegalName, RTIinternalError
  {
    reservedObjectInstanceNamesLock.lock();
    try
    {
      for (String name : objectInstanceNames)
      {
        if (objectInstanceNamesBeingReserved.contains(name))
        {
          throw new IllegalName("object instance name already being reserved: " + name);
        }
      }

      objectInstanceNamesBeingReserved.addAll(objectInstanceNames);
    }
    finally
    {
      reservedObjectInstanceNamesLock.unlock();
    }

    federate.getRTIChannel().write(new ReserveMultipleObjectInstanceName(objectInstanceNames));
  }

  public void releaseMultipleObjectInstanceName(Set<String> objectInstanceNames)
    throws ObjectInstanceNameNotReserved, RTIinternalError
  {
    reservedObjectInstanceNamesLock.lock();
    try
    {
      for (String name : objectInstanceNames)
      {
        if (!reservedObjectInstanceNames.contains(name))
        {
          throw new ObjectInstanceNameNotReserved(name);
        }
      }

      reservedObjectInstanceNames.removeAll(objectInstanceNames);
    }
    finally
    {
      reservedObjectInstanceNamesLock.unlock();
    }

    federate.getRTIChannel().write(new ReleaseMultipleObjectInstanceName(objectInstanceNames));
  }

  public ObjectInstanceHandle registerObjectInstance(ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined, ObjectClassNotPublished, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    ObjectClass objectClass = federate.getFDD().getObjectClass(objectClassHandle);

    publicationLock.readLock().lock();
    try
    {
      AttributeHandleSet publishedAttributeHandles = getPublishedObjectClassAttributes(objectClassHandle);

      objectsLock.writeLock().lock();
      try
      {
        IEEE1516eObjectInstanceHandle objectInstanceHandle =
          new IEEE1516eObjectInstanceHandle(federate.getFederateHandle(), ++objectInstanceCount);

        federate.getRTIChannel().write(new RegisterObjectInstance(
          objectInstanceHandle, objectClassHandle, publishedAttributeHandles));

        FederateObjectInstance objectInstance = new FederateObjectInstance(
          federate, objectInstanceHandle, objectClass, publishedAttributeHandles);

        objects.put(objectInstanceHandle, objectInstance);
        objectsByName.put(objectInstance.getObjectInstanceName(), objectInstance);
        getObjectsByClassHandle(objectClassHandle).add(objectInstanceHandle);

        return objectInstanceHandle;
      }
      finally
      {
        objectsLock.writeLock().unlock();
      }
    }
    finally
    {
      publicationLock.readLock().unlock();
    }
  }

  public ObjectInstanceHandle registerObjectInstance(ObjectClassHandle objectClassHandle, String objectInstanceName)
    throws ObjectClassNotDefined, ObjectClassNotPublished, ObjectInstanceNameNotReserved, ObjectInstanceNameInUse,
           RTIinternalError
  {
    ObjectClass objectClass = federate.getFDD().getObjectClass(objectClassHandle);

    publicationLock.readLock().lock();
    try
    {
      AttributeHandleSet publishedAttributeHandles = getPublishedObjectClassAttributes(objectClassHandle);

      objectsLock.writeLock().lock();
      try
      {
        checkIfObjectInstanceNameNotReserved(objectInstanceName);

        if (objectsByName.containsKey(objectInstanceName))
        {
          throw new ObjectInstanceNameInUse(objectInstanceName);
        }

        IEEE1516eObjectInstanceHandle objectInstanceHandle =
          new IEEE1516eObjectInstanceHandle(federate.getFederateHandle(), ++objectInstanceCount);

        federate.getRTIChannel().write(new RegisterObjectInstance(
          objectInstanceHandle, objectClassHandle, objectInstanceName, publishedAttributeHandles));

        FederateObjectInstance objectInstance = new FederateObjectInstance(
          federate, objectInstanceHandle, objectInstanceName, objectClass, publishedAttributeHandles);

        objects.put(objectInstanceHandle, objectInstance);
        objectsByName.put(objectInstanceName, objectInstance);
        getObjectsByClassHandle(objectClassHandle).add(objectInstanceHandle);

        return objectInstanceHandle;
      }
      finally
      {
        objectsLock.writeLock().unlock();
      }
    }
    finally
    {
      publicationLock.readLock().unlock();
    }
  }

  public ObjectInstanceHandle registerObjectInstanceWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectClassNotDefined, ObjectClassNotPublished, AttributeNotDefined, AttributeNotPublished, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    ObjectClass objectClass = federate.getFDD().getObjectClass(objectClassHandle);

    publicationLock.readLock().lock();
    try
    {
      AttributeHandleSet publishedAttributeHandles = getPublishedObjectClassAttributes(objectClassHandle);

      federate.getRegionManager().getRegionsLock().readLock().lock();
      try
      {
        checkIfAttributeNotDefinedOrAttributeNotPublishedOrRegionNotCreatedByThisFederate(
          objectClass, publishedAttributeHandles, attributesAndRegions);

        objectsLock.writeLock().lock();
        try
        {
          IEEE1516eObjectInstanceHandle objectInstanceHandle =
            new IEEE1516eObjectInstanceHandle(federate.getFederateHandle(), ++objectInstanceCount);

          federate.getRTIChannel().write(new RegisterObjectInstance(
            objectInstanceHandle, objectClassHandle, publishedAttributeHandles, attributesAndRegions));

          FederateObjectInstance objectInstance = new FederateObjectInstance(
            federate, objectInstanceHandle, objectClass, publishedAttributeHandles, attributesAndRegions);

          objects.put(objectInstanceHandle, objectInstance);
          objectsByName.put(objectInstance.getObjectInstanceName(), objectInstance);
          getObjectsByClassHandle(objectClassHandle).add(objectInstanceHandle);

          return objectInstanceHandle;
        }
        finally
        {
          objectsLock.writeLock().unlock();
        }
      }
      finally
      {
        federate.getRegionManager().getRegionsLock().readLock().unlock();
      }
    }
    finally
    {
      publicationLock.readLock().unlock();
    }
  }

  public ObjectInstanceHandle registerObjectInstanceWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions, String objectInstanceName)
    throws ObjectClassNotDefined, ObjectClassNotPublished, AttributeNotDefined, AttributeNotPublished, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext, ObjectInstanceNameNotReserved, ObjectInstanceNameInUse,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    ObjectClass objectClass = federate.getFDD().getObjectClass(objectClassHandle);

    publicationLock.readLock().lock();
    try
    {
      AttributeHandleSet publishedAttributeHandles = getPublishedObjectClassAttributes(objectClassHandle);

      federate.getRegionManager().getRegionsLock().readLock().lock();
      try
      {
        checkIfAttributeNotDefinedOrAttributeNotPublishedOrRegionNotCreatedByThisFederate(
          objectClass, publishedAttributeHandles, attributesAndRegions);

        objectsLock.writeLock().lock();
        try
        {
          checkIfObjectInstanceNameNotReserved(objectInstanceName);

          if (objectsByName.containsKey(objectInstanceName))
          {
            throw new ObjectInstanceNameInUse(objectInstanceName);
          }

          IEEE1516eObjectInstanceHandle objectInstanceHandle =
            new IEEE1516eObjectInstanceHandle(federate.getFederateHandle(), ++objectInstanceCount);

          federate.getRTIChannel().write(new RegisterObjectInstance(
            objectInstanceHandle, objectClassHandle, objectInstanceName, publishedAttributeHandles,
            attributesAndRegions));

          FederateObjectInstance objectInstance = new FederateObjectInstance(
            federate, objectInstanceHandle, objectInstanceName, objectClass, publishedAttributeHandles,
            attributesAndRegions);

          objects.put(objectInstanceHandle, objectInstance);
          objectsByName.put(objectInstanceName, objectInstance);
          getObjectsByClassHandle(objectClassHandle).add(objectInstanceHandle);

          return objectInstanceHandle;
        }
        finally
        {
          objectsLock.writeLock().unlock();
        }
      }
      finally
      {
        federate.getRegionManager().getRegionsLock().readLock().unlock();
      }
    }
    finally
    {
      publicationLock.readLock().unlock();
    }
  }

  public void updateAttributeValues(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned, RTIinternalError
  {
    objectsLock.readLock().lock();
    try
    {
      getObjectInstance(objectInstanceHandle).updateAttributeValues(attributeValues, tag, federate);
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void updateAttributeValues(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues, byte[] tag, LogicalTime time,
    MessageRetractionHandle messageRetractionHandle, OrderType sentOrderType)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned, RTIinternalError
  {
    objectsLock.readLock().lock();
    try
    {
      getObjectInstance(objectInstanceHandle).updateAttributeValues(
        attributeValues, tag, time, messageRetractionHandle, sentOrderType, federate);
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void sendInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag)
    throws InteractionClassNotPublished, RTIinternalError
  {
    publicationLock.readLock().lock();
    try
    {
      checkIfInteractionClassNotPublished(interactionClassHandle);

      federate.getRTIChannel().write(new SendInteraction(
        interactionClassHandle, parameterValues, tag, TransportationType.HLA_RELIABLE.getTransportationTypeHandle()));
    }
    finally
    {
      publicationLock.readLock().unlock();
    }
  }

  public void sendInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
    LogicalTime time, MessageRetractionHandle messageRetractionHandle, OrderType sentOrderType)
    throws InteractionClassNotPublished, RTIinternalError
  {
    publicationLock.readLock().lock();
    try
    {
      checkIfInteractionClassNotPublished(interactionClassHandle);

      federate.getRTIChannel().write(new SendInteraction(
        interactionClassHandle, parameterValues, tag, sentOrderType,
        TransportationType.HLA_RELIABLE.getTransportationTypeHandle(), time, messageRetractionHandle));
    }
    finally
    {
      publicationLock.readLock().unlock();
    }
  }

  public void deleteObjectInstance(ObjectInstanceHandle objectInstanceHandle, byte[] tag)
    throws ObjectInstanceNotKnown, DeletePrivilegeNotHeld, RTIinternalError
  {
    objectsLock.writeLock().lock();
    try
    {
      FederateObjectInstance objectInstance = getObjectInstance(objectInstanceHandle);
      objectInstance.checkIfDeletePrivilegeNotHeld();

      removeObjectInstance(objectInstance);

      federate.getRTIChannel().write(new DeleteObjectInstance(objectInstanceHandle, tag));
    }
    finally
    {
      objectsLock.writeLock().unlock();
    }
  }

  public void deleteObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag, LogicalTime time,
    MessageRetractionHandle messageRetractionHandle, OrderType sentOrderType)
    throws ObjectInstanceNotKnown, DeletePrivilegeNotHeld, RTIinternalError
  {
    objectsLock.writeLock().lock();
    try
    {
      FederateObjectInstance objectInstance = getObjectInstance(objectInstanceHandle);
      objectInstance.checkIfDeletePrivilegeNotHeld();

      if (sentOrderType == OrderType.RECEIVE)
      {
        removeObjectInstance(objectInstance);
      }

      federate.getRTIChannel().write(
        new DeleteObjectInstance(objectInstanceHandle, tag, sentOrderType, time, messageRetractionHandle));
    }
    finally
    {
      objectsLock.writeLock().unlock();
    }
  }

  public void localDeleteObjectInstance(ObjectInstanceHandle objectInstanceHandle)
    throws ObjectInstanceNotKnown, FederateOwnsAttributes
  {
    objectsLock.writeLock().lock();
    try
    {
      FederateObjectInstance objectInstance = getObjectInstance(objectInstanceHandle);
      objectInstance.checkIfFederateOwnsAttributes();

      objects.remove(objectInstanceHandle);
      objectsByName.remove(objectInstance.getObjectInstanceName());
    }
    finally
    {
      objectsLock.writeLock().unlock();
    }
  }

  public void objectInstanceNameReservationSucceeded(String name, FederateAmbassador federateAmbassador)
    throws FederateInternalError
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

  public void multipleObjectInstanceNameReservationSucceeded(Set<String> names, FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    reservedObjectInstanceNamesLock.lock();
    try
    {
      objectInstanceNamesBeingReserved.removeAll(names);
      reservedObjectInstanceNames.addAll(names);

      federateAmbassador.multipleObjectInstanceNameReservationSucceeded(names);
    }
    finally
    {
      reservedObjectInstanceNamesLock.unlock();
    }
  }

  public void objectInstanceNameReservationFailed(String name, FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    reservedObjectInstanceNamesLock.lock();
    try
    {
      objectInstanceNamesBeingReserved.remove(name);

      federateAmbassador.objectInstanceNameReservationFailed(name);
    }
    finally
    {
      reservedObjectInstanceNamesLock.unlock();
    }
  }

  public void multipleObjectInstanceNameReservationFailed(Set<String> names, FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    reservedObjectInstanceNamesLock.lock();
    try
    {
      objectInstanceNamesBeingReserved.removeAll(names);

      federateAmbassador.multipleObjectInstanceNameReservationFailed(names);
    }
    finally
    {
      reservedObjectInstanceNamesLock.unlock();
    }
  }

  public void discoverObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle, String name,
    FederateHandle producingFederateHandle, FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    ObjectClass objectClass = federate.getFDD().getObjectClassSafely(objectClassHandle);

    subscriptionLock.readLock().lock();
    try
    {
      ObjectClass knownObjectClass = subscriptionManager.getSubscribedObjectClass(objectClass);

      if (knownObjectClass == null)
      {
        log.trace("dropping discover object instance, no longer subscribed: {} - {}",
                  objectInstanceHandle, objectClass);
      }
      else
      {
        FederateObjectInstance objectInstance =
          new FederateObjectInstance(producingFederateHandle, objectInstanceHandle, knownObjectClass, name);

        objectsLock.writeLock().lock();
        try
        {
          objects.put(objectInstanceHandle, objectInstance);
          objectsByName.put(name, objectInstance);
          getObjectsByClassHandle(knownObjectClass.getObjectClassHandle()).add(objectInstanceHandle);

          federateAmbassador.discoverObjectInstance(
            objectInstanceHandle, knownObjectClass.getObjectClassHandle(), name);
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

  public void fireReflectAttributeValues(
    ReflectAttributeValues reflectAttributeValues, FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    subscriptionLock.readLock().lock();
    try
    {
      objectsLock.readLock().lock();
      try
      {
        FederateObjectInstance objectInstance = objects.get(reflectAttributeValues.getObjectInstanceHandle());
        if (objectInstance == null)
        {
          log.trace("dropping reflect attribute values, object instance no longer known: {}",
                    reflectAttributeValues.getObjectInstanceHandle());
        }
        else
        {
          ObjectClass objectClass = objectInstance.getObjectClass();
          ObjectClass subscribedObjectClass = subscriptionManager.getSubscribedObjectClass(objectClass);

          if (subscribedObjectClass == null)
          {
            log.trace("dropping reflect attribute values, no longer subscribed: {}", objectInstance);
          }
          else
          {
            if (!subscribedObjectClass.equals(objectClass))
            {
              subscriptionManager.trim(
                reflectAttributeValues.getAttributeValues(), subscribedObjectClass.getObjectClassHandle());
            }

            objectInstance.fireReflectAttributeValues(
              reflectAttributeValues, federateAmbassador, federate.getRegionManager());
          }
        }
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

  public void fireReceiveInteraction(ReceiveInteraction receiveInteraction, FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    InteractionClassHandle interactionClassHandle = receiveInteraction.getInteractionClassHandle();

    InteractionClass interactionClass = federate.getFDD().getInteractionClassSafely(interactionClassHandle);

    subscriptionLock.readLock().lock();
    try
    {
      InteractionClass subscribedInteractionClass =
        subscriptionManager.getSubscribedInteractionClass(interactionClass);

      if (subscribedInteractionClass == null)
      {
        log.trace("dropping receive interaction, no longer subscribed: {}", interactionClass);
      }
      else
      {
        interactionClassHandle = subscribedInteractionClass.getInteractionClassHandle();

        ParameterHandleValueMap parameterValues = receiveInteraction.getParameterValues();
        if (!subscribedInteractionClass.equals(interactionClass))
        {
          // trim the parameter values
          //
          parameterValues.keySet().retainAll(subscribedInteractionClass.getParameters().keySet());
        }

        if (receiveInteraction.hasSentRegions())
        {
          receiveInteraction.setSentRegions(
            federate.getRegionManager().createTemporaryRegions(receiveInteraction.getRegions()));
        }

        byte[] tag = receiveInteraction.getTag();
        OrderType sentOrderType = receiveInteraction.getSentOrderType();
        TransportationTypeHandle transportationTypeHandle = receiveInteraction.getTransportationTypeHandle();
        LogicalTime time = receiveInteraction.getTime();

        try
        {
          if (time == null)
          {
            federateAmbassador.receiveInteraction(
              interactionClassHandle, parameterValues, tag, sentOrderType, transportationTypeHandle,
              receiveInteraction);
          }
          else
          {
            MessageRetractionHandle messageRetractionHandle = receiveInteraction.getMessageRetractionHandle();
            OrderType receivedOrderType = receiveInteraction.getReceivedOrderType();
            if (messageRetractionHandle == null)
            {
              federateAmbassador.receiveInteraction(
                interactionClassHandle, parameterValues, tag, sentOrderType, transportationTypeHandle, time,
                receivedOrderType, receiveInteraction);
            }
            else
            {
              federateAmbassador.receiveInteraction(
                interactionClassHandle, parameterValues, tag, sentOrderType, transportationTypeHandle, time,
                receivedOrderType, messageRetractionHandle, receiveInteraction);
            }
          }
        }
        finally
        {
          if (receiveInteraction.hasSentRegions())
          {
            federate.getRegionManager().deleteTemporaryRegions(receiveInteraction.getSentRegions());
          }
        }
      }
    }
    finally
    {
      subscriptionLock.readLock().unlock();
    }
  }

  public void removeObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag, OrderType sentOrderType, LogicalTime time,
    OrderType receivedOrderType, MessageRetractionHandle messageRetractionHandle,
    FederateAmbassador.SupplementalRemoveInfo supplementalRemoveInfo, FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    objectsLock.writeLock().lock();
    try
    {
      FederateObjectInstance objectInstance = objects.remove(objectInstanceHandle);
      if (objectInstance == null)
      {
        log.trace("dropping remove object instance, object instance no longer known: {}", objectInstanceHandle);
      }
      else
      {
        removeObjectInstance(objectInstance);

        objectInstance.removeObjectInstance(
          objectInstanceHandle, tag, sentOrderType, time, receivedOrderType, messageRetractionHandle,
          supplementalRemoveInfo, federateAmbassador);
      }
    }
    finally
    {
      objectsLock.writeLock().unlock();
    }
  }

  public void attributeOwnershipAcquisitionNotification(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag,
    FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    objectsLock.readLock().lock();
    try
    {
      FederateObjectInstance objectInstance = objects.get(objectInstanceHandle);
      if (objectInstance != null)
      {
        objectInstance.attributeOwnershipAcquisitionNotification(attributeHandles, tag, federateAmbassador);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void changeAttributeOrderType(
    ObjectInstanceHandle objectInstanceHandle, Set<AttributeHandle> attributeHandles, OrderType orderType)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned
  {
    objectsLock.readLock().lock();
    try
    {
      getObjectInstance(objectInstanceHandle).changeAttributeOrderType(attributeHandles, orderType);
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void changeInteractionOrderType(InteractionClassHandle interactionClassHandle, OrderType orderType)
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

  public void requestAttributeTransportationTypeChange(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles,
    TransportationTypeHandle transportationTypeHandle)
    throws AttributeAlreadyBeingChanged, AttributeNotOwned, AttributeNotDefined,
           ObjectInstanceNotKnown, RTIinternalError
  {
    objectsLock.readLock().lock();
    try
    {
      getObjectInstance(objectInstanceHandle).requestAttributeTransportationTypeChange(
        attributeHandles, transportationTypeHandle);
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void queryAttributeTransportationType(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
    throws AttributeNotDefined, ObjectInstanceNotKnown, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    objectsLock.readLock().lock();
    try
    {
      getObjectInstance(objectInstanceHandle).queryAttributeTransportationType(attributeHandle, federate);
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void requestInteractionTransportationTypeChange(
    InteractionClassHandle interactionClassHandle, TransportationTypeHandle transportationType)
    throws InteractionClassNotDefined, InteractionClassNotPublished
  {
    publicationLock.readLock().lock();
    try
    {
      checkIfInteractionClassNotPublished(interactionClassHandle);

      // TODO: need to queue this as a callback

      federate.getFDD().changeInteractionTransportationType(interactionClassHandle, transportationType);
    }
    finally
    {
      publicationLock.readLock().unlock();
    }
  }

  public void requestAttributeValueUpdate(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined
  {
    objectsLock.readLock().lock();
    try
    {
      getObjectInstance(objectInstanceHandle).requestAttributeValueUpdate(attributeHandles, tag, federate);
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public boolean isAttributeOwnedByFederate(ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
    throws ObjectInstanceNotKnown, AttributeNotDefined
  {
    objectsLock.readLock().lock();
    try
    {
      return getObjectInstance(objectInstanceHandle).isAttributeOwnedByFederate(attributeHandle);
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void associateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle, AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectInstanceNotKnown, AttributeNotDefined, RegionNotCreatedByThisFederate, RTIinternalError
  {
    federate.getRegionManager().getRegionsLock().readLock().lock();
    try
    {
      objectsLock.readLock().lock();
      try
      {
        FederateObjectInstance objectInstance = getObjectInstance(objectInstanceHandle);

        checkIfAttributeNotDefinedOrRegionNotCreatedByThisFederate(
          objectInstance.getObjectClass(), attributesAndRegions);

        objectInstance.associateRegionsForUpdates(attributesAndRegions, federate);
      }
      finally
      {
        objectsLock.readLock().unlock();
      }
    }
    finally
    {
      federate.getRegionManager().getRegionsLock().readLock().unlock();
    }
  }

  public void unassociateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle, AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectInstanceNotKnown, AttributeNotDefined, RegionNotCreatedByThisFederate, RTIinternalError
  {
    federate.getRegionManager().getRegionsLock().readLock().lock();
    try
    {
      objectsLock.readLock().lock();
      try
      {
        FederateObjectInstance objectInstance = getObjectInstance(objectInstanceHandle);

        checkIfAttributeNotDefinedOrRegionNotCreatedByThisFederate(
          objectInstance.getObjectClass(), attributesAndRegions);

        objectInstance.unassociateRegionsForUpdates(attributesAndRegions, federate);
      }
      finally
      {
        objectsLock.readLock().unlock();
      }
    }
    finally
    {
      federate.getRegionManager().getRegionsLock().readLock().unlock();
    }
  }

  public void sendInteractionWithRegions(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues,
    RegionHandleSet regionHandles, byte[] tag)
    throws InteractionClassNotDefined, InteractionClassNotPublished, InteractionParameterNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    publicationLock.readLock().lock();
    try
    {
      checkIfInteractionClassNotPublished(interactionClassHandle);

      federate.getRegionManager().getRegionsLock().readLock().lock();
      try
      {
        federate.getRegionManager().checkIfRegionNotCreatedByThisFederate(regionHandles);

        federate.getRTIChannel().write(new SendInteraction(
          interactionClassHandle, parameterValues, tag, TransportationType.HLA_RELIABLE.getTransportationTypeHandle(),
          regionHandles));
      }
      finally
      {
        federate.getRegionManager().getRegionsLock().readLock().unlock();
      }
    }
    finally
    {
      publicationLock.readLock().unlock();
    }
  }

  public void sendInteractionWithRegions(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues,
    RegionHandleSet regionHandles, byte[] tag, LogicalTime time, MessageRetractionHandle messageRetractionHandle,
    OrderType sentOrderType)
    throws InteractionClassNotDefined, InteractionClassNotPublished, InteractionParameterNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext, InvalidLogicalTime, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    publicationLock.readLock().lock();
    try
    {
      checkIfInteractionClassNotPublished(interactionClassHandle);

      federate.getRegionManager().getRegionsLock().readLock().lock();
      try
      {
        federate.getRegionManager().checkIfRegionNotCreatedByThisFederate(regionHandles);

        federate.getRTIChannel().write(new SendInteraction(
          interactionClassHandle, parameterValues, tag, sentOrderType,
          TransportationType.HLA_RELIABLE.getTransportationTypeHandle(), time, messageRetractionHandle, regionHandles));
      }
      finally
      {
        federate.getRegionManager().getRegionsLock().readLock().unlock();
      }
    }
    finally
    {
      publicationLock.readLock().unlock();
    }
  }

  public void unconditionalAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned, RTIinternalError
  {
    objectsLock.readLock().lock();
    try
    {
      getObjectInstance(objectInstanceHandle).unconditionalAttributeOwnershipDivestiture(attributeHandles, federate);
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void negotiatedAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned, AttributeAlreadyBeingDivested,
           RTIinternalError
  {
    objectsLock.readLock().lock();
    try
    {
      getObjectInstance(objectInstanceHandle).negotiatedAttributeOwnershipDivestiture(attributeHandles, tag, federate);
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void confirmDivestiture(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned, AttributeDivestitureWasNotRequested,
           RTIinternalError
  {
    objectsLock.readLock().lock();
    try
    {
      getObjectInstance(objectInstanceHandle).confirmDivestiture(attributeHandles, tag, federate);
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisition(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, ObjectClassNotPublished, AttributeNotDefined, AttributeNotPublished,
           FederateOwnsAttributes, RTIinternalError
  {
    publicationLock.readLock().lock();
    try
    {
      objectsLock.readLock().lock();
      try
      {
        FederateObjectInstance objectInstance = getObjectInstance(objectInstanceHandle);

        checkIfAttributeNotPublished(objectInstance.getObjectClassHandle(), attributeHandles);

        objectInstance.attributeOwnershipAcquisition(attributeHandles, tag, federate);
      }
      finally
      {
        objectsLock.readLock().unlock();
      }
    }
    finally
    {
      publicationLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisitionIfAvailable(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, ObjectClassNotPublished, AttributeNotDefined, AttributeNotPublished,
           FederateOwnsAttributes, AttributeAlreadyBeingAcquired, RTIinternalError
  {
    publicationLock.readLock().lock();
    try
    {
      objectsLock.readLock().lock();
      try
      {
        FederateObjectInstance objectInstance = getObjectInstance(objectInstanceHandle);

        for (AttributeHandle attributeHandle : attributeHandles)
        {
          objectInstance.getObjectClass().checkIfAttributeNotDefined(attributeHandle);
        }
        checkIfAttributeNotPublished(objectInstance.getObjectClassHandle(), attributeHandles);

        objectInstance.attributeOwnershipAcquisitionIfAvailable(attributeHandles, federate);
      }
      finally
      {
        objectsLock.readLock().unlock();
      }
    }
    finally
    {
      publicationLock.readLock().unlock();
    }
  }

  public void attributeOwnershipReleaseDenied(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws AttributeNotOwned, AttributeNotDefined, ObjectInstanceNotKnown, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    objectsLock.readLock().lock();
    try
    {
      getObjectInstance(objectInstanceHandle).attributeOwnershipReleaseDenied(attributeHandles, federate);
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public AttributeHandleSet attributeOwnershipDivestitureIfWanted(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned, RTIinternalError
  {
    objectsLock.readLock().lock();
    try
    {
      return getObjectInstance(objectInstanceHandle).attributeOwnershipDivestitureIfWanted(attributeHandles, federate);
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned, AttributeDivestitureWasNotRequested,
           RTIinternalError
  {
    objectsLock.readLock().lock();
    try
    {
      getObjectInstance(objectInstanceHandle).cancelNegotiatedAttributeOwnershipDivestiture(attributeHandles, federate);
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void cancelAttributeOwnershipAcquisition(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeAlreadyOwned, AttributeAcquisitionWasNotRequested, RTIinternalError
  {
    objectsLock.readLock().lock();
    try
    {
      getObjectInstance(objectInstanceHandle).cancelAttributeOwnershipAcquisition(attributeHandles, federate);
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void queryAttributeOwnership(ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
    throws ObjectInstanceNotKnown, AttributeNotDefined, RTIinternalError
  {
    objectsLock.readLock().lock();
    try
    {
      getObjectInstance(objectInstanceHandle).queryAttributeOwnership(attributeHandle, federate);
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  protected AttributeHandleSet getPublishedObjectClassAttributes(ObjectClassHandle objectClassHandle)
    throws ObjectClassNotPublished
  {
    AttributeHandleSet publishedAttributeHandles = publishedObjectClasses.get(objectClassHandle);
    if (publishedAttributeHandles == null)
    {
      throw new ObjectClassNotPublished(String.format("%s", objectClassHandle));
    }
    return publishedAttributeHandles;
  }

  protected FederateObjectInstance getObjectInstance(ObjectInstanceHandle objectInstanceHandle)
    throws ObjectInstanceNotKnown
  {
    FederateObjectInstance objectInstance = objects.get(objectInstanceHandle);
    if (objectInstance == null)
    {
      throw new ObjectInstanceNotKnown(objectInstanceHandle.toString());
    }
    return objectInstance;
  }

  protected FederateObjectInstance getObjectInstanceSafely(ObjectInstanceHandle objectInstanceHandle)
  {
    FederateObjectInstance objectInstance = objects.get(objectInstanceHandle);

    assert objectInstance != null;

    return objectInstance;
  }

  protected FederateObjectInstance getObjectInstance(String name)
    throws ObjectInstanceNotKnown
  {
    FederateObjectInstance objectInstance = objectsByName.get(name);
    if (objectInstance == null)
    {
      throw new ObjectInstanceNotKnown(name);
    }
    return objectInstance;
  }

  protected Set<ObjectInstanceHandle> getObjectsByClassHandle(
    ObjectClassHandle objectClassHandle)
  {
    Set<ObjectInstanceHandle> objectInstanceHandles = objectsByObjectClassHandle.get(objectClassHandle);
    if (objectInstanceHandles == null)
    {
      objectInstanceHandles = new HashSet<ObjectInstanceHandle>();
      objectsByObjectClassHandle.put(objectClassHandle, objectInstanceHandles);
    }
    return objectInstanceHandles;
  }

  protected void checkIfInteractionClassNotPublished(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotPublished
  {
    if (!publishedInteractionClasses.contains(interactionClassHandle))
    {
      throw new InteractionClassNotPublished(interactionClassHandle.toString());
    }
  }

  protected void checkIfAttributeNotPublished(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws ObjectClassNotPublished, AttributeNotDefined, AttributeNotPublished
  {
    AttributeHandleSet publishedAttributeHandles = publishedObjectClasses.get(objectClassHandle);
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
          ObjectClass objectClass = federate.getFDD().getObjectClasses().get(objectClassHandle);
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

  protected void checkIfAttributeNotDefinedOrRegionNotCreatedByThisFederate(
    ObjectClass objectClass, AttributeSetRegionSetPairList attributesAndRegions)
  throws AttributeNotDefined, RegionNotCreatedByThisFederate
  {
    for (AttributeRegionAssociation attributeRegionAssociation : attributesAndRegions)
    {
      objectClass.checkIfAttributeNotDefined(attributeRegionAssociation.ahset);
      federate.getRegionManager().checkIfRegionNotCreatedByThisFederate(attributeRegionAssociation.rhset);
    }
  }

  protected void checkIfAttributeNotDefinedOrAttributeNotPublishedOrRegionNotCreatedByThisFederate(
    ObjectClass objectClass, AttributeHandleSet publishedAttributeHandles,
    AttributeSetRegionSetPairList attributesAndRegions)
  throws AttributeNotDefined, AttributeNotPublished, RegionNotCreatedByThisFederate
  {
    for (AttributeRegionAssociation attributeRegionAssociation : attributesAndRegions)
    {
      for (AttributeHandle attributeHandle : attributeRegionAssociation.ahset)
      {
        objectClass.checkIfAttributeNotDefined(attributeHandle);

        if (!publishedAttributeHandles.contains(attributeHandle))
        {
          throw new AttributeNotPublished(attributeHandle.toString());
        }
      }

      federate.getRegionManager().checkIfRegionNotCreatedByThisFederate(attributeRegionAssociation.rhset);
    }
  }

  protected void removeObjectInstance(FederateObjectInstance objectInstance)
  {
    objects.remove(objectInstance.getObjectInstanceHandle());
    objectsByName.remove(objectInstance.getObjectInstanceName());
    getObjectsByClassHandle(objectInstance.getObjectClassHandle()).remove(
      objectInstance.getObjectInstanceHandle());
    removedObjects.add(objectInstance.getObjectInstanceHandle());

    reservedObjectInstanceNamesLock.lock();
    try
    {
      reservedObjectInstanceNames.remove(objectInstance.getObjectInstanceName());
    }
    finally
    {
      reservedObjectInstanceNamesLock.unlock();
    }
  }
}
