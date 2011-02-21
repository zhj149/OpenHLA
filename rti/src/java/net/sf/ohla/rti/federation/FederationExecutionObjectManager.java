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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.messages.AssociateRegionsForUpdates;
import net.sf.ohla.rti.messages.AssociateRegionsForUpdatesResponse;
import net.sf.ohla.rti.messages.DeleteObjectInstance;
import net.sf.ohla.rti.messages.LocalDeleteObjectInstance;
import net.sf.ohla.rti.messages.RequestObjectClassAttributeValueUpdate;
import net.sf.ohla.rti.messages.RequestObjectClassAttributeValueUpdateWithRegions;
import net.sf.ohla.rti.messages.RequestObjectInstanceAttributeValueUpdate;
import net.sf.ohla.rti.messages.ResignFederationExecution;
import net.sf.ohla.rti.messages.UnassociateRegionsForUpdates;
import net.sf.ohla.rti.messages.UnassociateRegionsForUpdatesResponse;
import net.sf.ohla.rti.messages.UpdateAttributeValues;
import net.sf.ohla.rti.messages.callbacks.MultipleObjectInstanceNameReservationFailed;
import net.sf.ohla.rti.messages.callbacks.MultipleObjectInstanceNameReservationSucceeded;
import net.sf.ohla.rti.messages.callbacks.ObjectInstanceNameReservationFailed;
import net.sf.ohla.rti.messages.callbacks.ObjectInstanceNameReservationSucceeded;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeSetRegionSetPairList;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;

public class FederationExecutionObjectManager
{
  private static final Logger log = LoggerFactory.getLogger(FederationExecutionObjectManager.class);

  private final FederationExecution federationExecution;

  private final Lock objectInstanceNamesLock = new ReentrantLock(true);

  private final Map<String, FederateProxy> reservedObjectInstanceNames = new HashMap<String, FederateProxy>();

  private final ReadWriteLock objectsLock = new ReentrantReadWriteLock(true);
  private final Map<ObjectInstanceHandle, FederationExecutionObjectInstance> objects =
    new HashMap<ObjectInstanceHandle, FederationExecutionObjectInstance>();

  private final Marker marker;

  public FederationExecutionObjectManager(FederationExecution federationExecution)
  {
    this.federationExecution = federationExecution;

    marker = federationExecution.getMarker();
  }

  public FederationExecution getFederationExecution()
  {
    return federationExecution;
  }

  public void resignFederationExecution(
    FederateProxy federateProxy, ResignFederationExecution resignFederationExecution)
  {
    objectsLock.writeLock().lock();
    try
    {
      switch (resignFederationExecution.getResignAction())
      {
        case UNCONDITIONALLY_DIVEST_ATTRIBUTES:
          for (FederationExecutionObjectInstance objectInstance : objects.values())
          {
            objectInstance.unconditionallyDivestAttributes(federateProxy);
          }
          break;
        case DELETE_OBJECTS:
          for (Iterator<FederationExecutionObjectInstance> i = objects.values().iterator(); i.hasNext();)
          {
            FederationExecutionObjectInstance objectInstance = i.next();
            if (objectInstance.isOwner(federateProxy))
            {
              i.remove();

              for (FederateProxy fp : federationExecution.getFederates().values())
              {
                fp.removeObjectInstance(objectInstance.getObjectInstanceHandle(), federateProxy.getFederateHandle());
              }
            }
          }
          break;
        case CANCEL_PENDING_OWNERSHIP_ACQUISITIONS:
          for (FederationExecutionObjectInstance objectInstance : objects.values())
          {
            objectInstance.cancelPendingOwnershipAcquisitions(federateProxy);
          }
          break;
        case DELETE_OBJECTS_THEN_DIVEST:
          for (Iterator<FederationExecutionObjectInstance> i = objects.values().iterator(); i.hasNext();)
          {
            FederationExecutionObjectInstance objectInstance = i.next();
            if (objectInstance.isOwner(federateProxy))
            {
              i.remove();

              for (FederateProxy fp : federationExecution.getFederates().values())
              {
                fp.removeObjectInstance(objectInstance.getObjectInstanceHandle(), federateProxy.getFederateHandle());
              }
            }
            else
            {
              objectInstance.unconditionallyDivestAttributes(federateProxy);
            }
          }
          break;
        case CANCEL_THEN_DELETE_THEN_DIVEST:
          for (Iterator<FederationExecutionObjectInstance> i = objects.values().iterator(); i.hasNext();)
          {
            FederationExecutionObjectInstance objectInstance = i.next();

            objectInstance.cancelPendingOwnershipAcquisitions(federateProxy);

            if (objectInstance.isOwner(federateProxy))
            {
              i.remove();

              for (FederateProxy fp : federationExecution.getFederates().values())
              {
                fp.removeObjectInstance(objectInstance.getObjectInstanceHandle(), federateProxy.getFederateHandle());
              }
            }
            else
            {
              objectInstance.unconditionallyDivestAttributes(federateProxy);
            }
          }
          break;
      }
    }
    finally
    {
      objectsLock.writeLock().unlock();
    }
  }

  public void localDeleteObjectInstance(
    FederateProxy federateProxy, LocalDeleteObjectInstance localDeleteObjectInstance)
  {
    objectsLock.readLock().lock();
    try
    {
      ObjectInstanceHandle objectInstanceHandle = localDeleteObjectInstance.getObjectInstanceHandle();

      FederationExecutionObjectInstance objectInstance = objects.get(objectInstanceHandle);
      if (objectInstance == null)
      {
        log.trace(marker, "dropping local delete object instance, object has been deleted: {}", objectInstanceHandle);
      }
      else
      {
        federateProxy.rediscoverObjectInstance(objectInstance);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void deleteObjectInstance(FederateProxy federateProxy, DeleteObjectInstance deleteObjectInstance)
  {
    objectsLock.writeLock().lock();
    try
    {
      if (deleteObjectInstance.getSentOrderType() == OrderType.TIMESTAMP)
      {
        // TODO: track for future federates?
      }

      objects.remove(deleteObjectInstance.getObjectInstanceHandle());

      for (FederateProxy fp : federationExecution.getFederates().values())
      {
        if (fp != federateProxy)
        {
          fp.removeObjectInstance(deleteObjectInstance, federateProxy.getFederateHandle());
        }
      }
    }
    finally
    {
      objectsLock.writeLock().unlock();
    }
  }

  public void requestObjectInstanceAttributeValueUpdate(
    FederateProxy federateProxy, RequestObjectInstanceAttributeValueUpdate requestObjectInstanceAttributeValueUpdate)
  {
    objectsLock.readLock().lock();
    try
    {
      ObjectInstanceHandle objectInstanceHandle = requestObjectInstanceAttributeValueUpdate.getObjectInstanceHandle();

      FederationExecutionObjectInstance objectInstance = objects.get(objectInstanceHandle);
      if (objectInstance == null)
      {
        log.trace(marker, "dropping request object instance value update, object has been deleted: {}",
                  objectInstanceHandle);
      }
      else
      {
        objectInstance.requestObjectInstanceAttributeValueUpdate(
          federateProxy, requestObjectInstanceAttributeValueUpdate);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void requestObjectClassAttributeValueUpdate(
    FederateProxy federateProxy, RequestObjectClassAttributeValueUpdate requestObjectClassAttributeValueUpdate)
  {
    ObjectClass objectClass = federateProxy.getFederationExecution().getFDD().getObjectClassSafely(
      requestObjectClassAttributeValueUpdate.getObjectClassHandle());

    objectsLock.readLock().lock();
    try
    {
      for (FederationExecutionObjectInstance objectInstance : objects.values())
      {
        if (objectClass.isAssignableFrom(objectInstance.getObjectClass()))
        {
          objectInstance.requestObjectClassAttributeValueUpdate(federateProxy, requestObjectClassAttributeValueUpdate);
        }
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void requestObjectClassAttributeValueUpdateWithRegions(
    FederateProxy federateProxy,
    RequestObjectClassAttributeValueUpdateWithRegions requestObjectClassAttributeValueUpdateWithRegions)
  {
    ObjectClass objectClass = federateProxy.getFederationExecution().getFDD().getObjectClassSafely(
      requestObjectClassAttributeValueUpdateWithRegions.getObjectClassHandle());

    objectsLock.readLock().lock();
    try
    {
      for (FederationExecutionObjectInstance objectInstance : objects.values())
      {
        if (objectClass.isAssignableFrom(objectInstance.getObjectClass()))
        {
          objectInstance.requestObjectClassAttributeValueUpdateWithRegions(
            federateProxy, requestObjectClassAttributeValueUpdateWithRegions);
        }
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void publishObjectClassAttributes(
    FederateProxy federateProxy, ObjectClass objectClass, AttributeHandleSet attributeHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      for (FederationExecutionObjectInstance objectInstance : objects.values())
      {
        if (!federateProxy.getFederateHandle().equals(objectInstance.getProducingFederateHandle()) &&
            objectClass.isAssignableFrom(objectInstance.getObjectClass()))
        {
          objectInstance.publishObjectClassAttributes(federateProxy, attributeHandles);
        }
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void subscribeObjectClassAttributes(FederateProxy federateProxy, ObjectClass objectClass)
  {
    objectsLock.readLock().lock();
    try
    {
      // TODO: consider sending one message instead of many

      for (FederationExecutionObjectInstance objectInstance : objects.values())
      {
        if (!federateProxy.getFederateHandle().equals(objectInstance.getProducingFederateHandle()) &&
            objectClass.isAssignableFrom(objectInstance.getObjectClass()))
        {
          federateProxy.discoverObjectInstance(objectInstance);
        }
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void subscribeObjectClassAttributesWithRegions(
    FederateProxy federateProxy, ObjectClass objectClass, AttributeSetRegionSetPairList attributesAndRegions)
  {
    objectsLock.readLock().lock();
    try
    {
      // TODO: consider sending one message instead of many

      for (FederationExecutionObjectInstance objectInstance : objects.values())
      {
        if (!federateProxy.getFederateHandle().equals(objectInstance.getProducingFederateHandle()) &&
            objectClass.isAssignableFrom(objectInstance.getObjectClass()))
        {
          // TODO: DDM

          federateProxy.discoverObjectInstance(objectInstance);
        }
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void reserveObjectInstanceName(FederateProxy federateProxy, String name)
  {
    objectInstanceNamesLock.lock();
    try
    {
      FederateProxy reservingFederateProxy = reservedObjectInstanceNames.get(name);
      if (reservingFederateProxy == null)
      {
        reservedObjectInstanceNames.put(name, federateProxy);

        federateProxy.getFederateChannel().write(new ObjectInstanceNameReservationSucceeded(name));
      }
      else
      {
        log.debug(marker, "reserve object instance name failed, name already reserved: {} by {}",
                  name, reservingFederateProxy);

        federateProxy.getFederateChannel().write(new ObjectInstanceNameReservationFailed(name));
      }
    }
    finally
    {
      objectInstanceNamesLock.unlock();
    }
  }

  public void releaseObjectInstanceName(FederateProxy federateProxy, String name)
  {
    objectInstanceNamesLock.lock();
    try
    {
      reservedObjectInstanceNames.remove(name);
    }
    finally
    {
      objectInstanceNamesLock.unlock();
    }
  }

  public void reserveMultipleObjectInstanceName(FederateProxy federateProxy, Set<String> objectInstanceNames)
  {
    objectInstanceNamesLock.lock();
    try
    {
      Map<String, FederateProxy> alreadyReservedObjectInstanceNames = new HashMap<String, FederateProxy>();
      Map<String, FederateProxy> reservedObjectInstanceNames = new HashMap<String, FederateProxy>();
      for (String objectInstanceName : objectInstanceNames)
      {
        FederateProxy reservingFederateProxy = this.reservedObjectInstanceNames.get(objectInstanceName);
        if (reservingFederateProxy == null)
        {
          reservedObjectInstanceNames.put(objectInstanceName, federateProxy);
        }
        else
        {
          alreadyReservedObjectInstanceNames.put(objectInstanceName, reservingFederateProxy);
        }
      }

      if (reservedObjectInstanceNames.size() == objectInstanceNames.size())
      {
        this.reservedObjectInstanceNames.putAll(reservedObjectInstanceNames);

        federateProxy.getFederateChannel().write(
          new MultipleObjectInstanceNameReservationSucceeded(objectInstanceNames));
      }
      else
      {
        log.debug(marker, "reserve multiple object instance name failed, names already reserved: {}",
                  alreadyReservedObjectInstanceNames);

        federateProxy.getFederateChannel().write(new MultipleObjectInstanceNameReservationFailed(objectInstanceNames));
      }
    }
    finally
    {
      objectInstanceNamesLock.unlock();
    }
  }

  public void releaseMultipleObjectInstanceName(FederateProxy federateProxy, Set<String> names)
  {
    objectInstanceNamesLock.lock();
    try
    {
      reservedObjectInstanceNames.keySet().removeAll(names);
    }
    finally
    {
      objectInstanceNamesLock.unlock();
    }
  }

  public FederationExecutionObjectInstance registerObjectInstance(
    FederateProxy federateProxy, ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle,
    String objectInstanceName, AttributeHandleSet publishedAttributeHandles)
  {
    ObjectClass objectClass = federationExecution.getFDD().getObjectClassSafely(objectClassHandle);

    assert objectInstanceName.startsWith("HLA") ||
           federateProxy.equals(reservedObjectInstanceNames.get(objectInstanceName));

    FederationExecutionObjectInstance objectInstance = new FederationExecutionObjectInstance(
      objectInstanceHandle, objectClass, objectInstanceName, publishedAttributeHandles, federateProxy);

    objectsLock.writeLock().lock();
    try
    {
      objects.put(objectInstanceHandle, objectInstance);
    }
    finally
    {
      objectsLock.writeLock().unlock();
    }

    return objectInstance;
  }

  public void updateAttributeValues(FederateProxy federateProxy, UpdateAttributeValues updateAttributeValues)
  {
    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance = objects.get(updateAttributeValues.getObjectInstanceHandle());
      if (objectInstance != null)
      {
        if (updateAttributeValues.getSentOrderType() == OrderType.TIMESTAMP)
        {
          // TODO: track for future federates?
        }

        objectInstance.updateAttributeValues(federateProxy, updateAttributeValues);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void unconditionalAttributeOwnershipDivestiture(
    FederateProxy owner, ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance = objects.get(objectInstanceHandle);
      if (objectInstance != null)
      {
        objectInstance.unconditionalAttributeOwnershipDivestiture(owner, attributeHandles);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void negotiatedAttributeOwnershipDivestiture(
    FederateProxy owner, ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
  {
    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance = objects.get(objectInstanceHandle);
      if (objectInstance == null)
      {
        log.trace(marker, "dropping negotiated attribute ownership divestiture, object has been deleted: {}",
                  objectInstanceHandle);
      }
      else
      {
        objectInstance.negotiatedAttributeOwnershipDivestiture(owner, attributeHandles, tag);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void confirmDivestiture(
    FederateProxy owner, ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance = objects.get(objectInstanceHandle);
      if (objectInstance == null)
      {
        log.trace(marker, "dropping confirm divestiture, object has been deleted: {}", objectInstanceHandle);
      }
      else
      {
        objectInstance.confirmDivestiture(owner, attributeHandles);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisition(
    FederateProxy acquiree, ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
  {
    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance = objects.get(objectInstanceHandle);
      if (objectInstance == null)
      {
        log.trace(marker, "dropping attribute ownership acquisition, object has been deleted: {}",
                  objectInstanceHandle);
      }
      else
      {
        objectInstance.attributeOwnershipAcquisition(acquiree, attributeHandles, tag);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisitionIfAvailable(
    FederateProxy acquiree, ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance = objects.get(objectInstanceHandle);
      if (objectInstance == null)
      {
        log.trace(marker, "dropping attribute ownership acquisition, object has been deleted: {}",
                  objectInstanceHandle);
      }
      else
      {
        objectInstance.attributeOwnershipAcquisitionIfAvailable(acquiree, attributeHandles);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public Map<AttributeHandle, FederateProxy> attributeOwnershipDivestitureIfWanted(
    FederateProxy owner, ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance = objects.get(objectInstanceHandle);
      return objectInstance != null ?
        objectInstance.attributeOwnershipDivestitureIfWanted(owner, attributeHandles) :
        (Map<AttributeHandle, FederateProxy>) Collections.EMPTY_MAP;
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture(
    FederateProxy owner, ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance = objects.get(objectInstanceHandle);
      if (objectInstance != null)
      {
        objectInstance.cancelNegotiatedAttributeOwnershipDivestiture(owner, attributeHandles);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void cancelAttributeOwnershipAcquisition(
    FederateProxy acquiree, ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance = objects.get(objectInstanceHandle);
      if (objectInstance != null)
      {
        objectInstance.cancelAttributeOwnershipAcquisition(acquiree, attributeHandles);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void queryAttributeOwnership(
    FederateProxy federateProxy, ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
  {
    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance = objects.get(objectInstanceHandle);
      if (objectInstance == null)
      {
        log.trace(marker, "dropping query attribute ownership, object has been deleted: {}", objectInstanceHandle);
      }
      else
      {
        objectInstance.queryAttributeOwnership(federateProxy, attributeHandle);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void associateRegionsForUpdates(
    FederateProxy federateProxy, AssociateRegionsForUpdates associateRegionsForUpdates)
  {
    ObjectInstanceHandle objectInstanceHandle = associateRegionsForUpdates.getObjectInstanceHandle();

    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance = objects.get(objectInstanceHandle);
      if (objectInstance == null)
      {
        log.trace(marker, "dropping associate regions for updates, object has been deleted: {}", objectInstanceHandle);

        federateProxy.getFederateChannel().write(new AssociateRegionsForUpdatesResponse(
          associateRegionsForUpdates.getId(), AssociateRegionsForUpdatesResponse.Response.OBJECT_INSTANCE_NOT_KNOWN));
      }
      else
      {
        objectInstance.associateRegionsForUpdates(federateProxy, associateRegionsForUpdates);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void unassociateRegionsForUpdates(
    FederateProxy federateProxy, UnassociateRegionsForUpdates unassociateRegionsForUpdates)
  {
    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance =
        objects.get(unassociateRegionsForUpdates.getObjectInstanceHandle());
      if (objectInstance == null)
      {
        // the object was deleted after an associate was issued...

        federateProxy.getFederateChannel().write(new UnassociateRegionsForUpdatesResponse(
          unassociateRegionsForUpdates.getId(),
          UnassociateRegionsForUpdatesResponse.Response.OBJECT_INSTANCE_NOT_KNOWN));
      }
      else
      {
        objectInstance.unassociateRegionsForUpdates(federateProxy, unassociateRegionsForUpdates);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }
}
