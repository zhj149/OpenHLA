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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.i18n.I18nLogger;
import net.sf.ohla.rti.i18n.LogMessages;
import net.sf.ohla.rti.messages.AssociateRegionsForUpdates;
import net.sf.ohla.rti.messages.AssociateRegionsForUpdatesResponse;
import net.sf.ohla.rti.messages.DeleteObjectInstance;
import net.sf.ohla.rti.messages.GetUpdateRateValueForAttribute;
import net.sf.ohla.rti.messages.GetUpdateRateValueForAttributeResponse;
import net.sf.ohla.rti.messages.LocalDeleteObjectInstance;
import net.sf.ohla.rti.messages.RequestObjectClassAttributeValueUpdate;
import net.sf.ohla.rti.messages.RequestObjectClassAttributeValueUpdateWithRegions;
import net.sf.ohla.rti.messages.RequestObjectInstanceAttributeValueUpdate;
import net.sf.ohla.rti.messages.ResignFederationExecution;
import net.sf.ohla.rti.messages.Retract;
import net.sf.ohla.rti.messages.UnassociateRegionsForUpdates;
import net.sf.ohla.rti.messages.UnassociateRegionsForUpdatesResponse;
import net.sf.ohla.rti.messages.UpdateAttributeValues;
import net.sf.ohla.rti.messages.callbacks.MultipleObjectInstanceNameReservationFailed;
import net.sf.ohla.rti.messages.callbacks.MultipleObjectInstanceNameReservationSucceeded;
import net.sf.ohla.rti.messages.callbacks.ObjectInstanceNameReservationFailed;
import net.sf.ohla.rti.messages.callbacks.ObjectInstanceNameReservationSucceeded;
import net.sf.ohla.rti.messages.proto.FederateMessageProtos;
import net.sf.ohla.rti.proto.FederationExecutionSaveProtos.FederationExecutionState.FederationExecutionObjectManagerState;
import net.sf.ohla.rti.util.FederateHandles;
import net.sf.ohla.rti.util.Retractable;
import net.sf.ohla.rti.util.RetractableManager;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeSetRegionSetPairList;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;

public class FederationExecutionObjectManager
{
  private final FederationExecution federationExecution;

  private final Lock objectInstanceNamesLock = new ReentrantLock(true);
  private final Map<String, FederateHandle> reservedObjectInstanceNames = new HashMap<>();

  private final ReentrantReadWriteLock objectsLock = new ReentrantReadWriteLock(true);
  private final Map<ObjectInstanceHandle, FederationExecutionObjectInstance> objects = new HashMap<>();

  private final RetractableManager<ScheduledDelete> scheduledDeletes = new RetractableManager<>();

  private final I18nLogger log;

  public FederationExecutionObjectManager(FederationExecution federationExecution)
  {
    this.federationExecution = federationExecution;

    log = I18nLogger.getLogger(federationExecution.getMarker(), FederationExecutionObjectManager.class);
  }

  public FederationExecution getFederationExecution()
  {
    return federationExecution;
  }

  /**
   * Returns the current {@link FederationExecutionObjectInstance}s in this {@code FederationExecution}.<br/><br/>
   * <b>WARNING:</b> Use of this method is dangerous and can only be safely accessed when
   * {@link FederationExecutionObjectManager#objectsLock} is held.
   *
   * @return the current {@link FederationExecutionObjectInstance}s in this {@code FederationExecution}
   */
  public Map<ObjectInstanceHandle, FederationExecutionObjectInstance> getObjects()
  {
    return objects;
  }

  public void resignFederationExecution(
    FederateProxy resigningFederateProxy, ResignFederationExecution resignFederationExecution)
  {
    objectsLock.writeLock().lock();
    try
    {
      switch (resignFederationExecution.getResignAction())
      {
        case UNCONDITIONALLY_DIVEST_ATTRIBUTES:
          for (FederationExecutionObjectInstance objectInstance : objects.values())
          {
            objectInstance.unconditionallyDivestAttributes(resigningFederateProxy);
          }
          break;
        case DELETE_OBJECTS:
          for (Iterator<FederationExecutionObjectInstance> i = objects.values().iterator(); i.hasNext();)
          {
            FederationExecutionObjectInstance objectInstance = i.next();
            if (objectInstance.isOwner(resigningFederateProxy))
            {
              i.remove();

              for (FederateProxy federateProxy : federationExecution.getFederates().values())
              {
                federateProxy.removeObjectInstance(
                  objectInstance.getObjectInstanceHandle(), resigningFederateProxy.getFederateHandle());
              }
            }
          }
          break;
        case CANCEL_PENDING_OWNERSHIP_ACQUISITIONS:
          for (FederationExecutionObjectInstance objectInstance : objects.values())
          {
            objectInstance.cancelPendingOwnershipAcquisitions(resigningFederateProxy);
          }
          break;
        case DELETE_OBJECTS_THEN_DIVEST:
          for (Iterator<FederationExecutionObjectInstance> i = objects.values().iterator(); i.hasNext();)
          {
            FederationExecutionObjectInstance objectInstance = i.next();
            if (objectInstance.isOwner(resigningFederateProxy))
            {
              i.remove();

              for (FederateProxy federateProxy : federationExecution.getFederates().values())
              {
                federateProxy.removeObjectInstance(
                  objectInstance.getObjectInstanceHandle(), resigningFederateProxy.getFederateHandle());
              }
            }
            else
            {
              objectInstance.unconditionallyDivestAttributes(resigningFederateProxy);
            }
          }
          break;
        case CANCEL_THEN_DELETE_THEN_DIVEST:
          for (Iterator<FederationExecutionObjectInstance> i = objects.values().iterator(); i.hasNext();)
          {
            FederationExecutionObjectInstance objectInstance = i.next();

            objectInstance.cancelPendingOwnershipAcquisitions(resigningFederateProxy);

            if (objectInstance.isOwner(resigningFederateProxy))
            {
              i.remove();

              for (FederateProxy federateProxy : federationExecution.getFederates().values())
              {
                federateProxy.removeObjectInstance(
                  objectInstance.getObjectInstanceHandle(), resigningFederateProxy.getFederateHandle());
              }
            }
            else
            {
              objectInstance.unconditionallyDivestAttributes(resigningFederateProxy);
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
        log.trace(LogMessages.DROPPING_LOCAL_DELETE_OBJECT_INSTANCE_OBJECT_DELETED, objectInstanceHandle);
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

  public void deleteObjectInstance(FederateProxy deletingFederateProxy, DeleteObjectInstance deleteObjectInstance)
  {
    objectsLock.writeLock().lock();
    try
    {
      if (deleteObjectInstance.getSentOrderType() == OrderType.RECEIVE)
      {
        // remove immediately if sent receive order
        //
        objects.remove(deleteObjectInstance.getObjectInstanceHandle());
      }
      else
      {
        FederationExecutionObjectInstance federationExecutionObjectInstance =
          objects.get(deleteObjectInstance.getObjectInstanceHandle());
        if (federationExecutionObjectInstance == null)
        {
          // TODO: ignore, log
        }
        else if (federationExecutionObjectInstance.isScheduledForDeletion())
        {
          // TODO: ignore, log
        }
        else
        {
          LogicalTime deleteTime = deleteObjectInstance.getTime(federationExecution.getLogicalTimeFactory());

          federationExecutionObjectInstance.setDeleteObjectInstance(deleteObjectInstance);

          // schedule the delete
          //
          scheduledDeletes.add(new ScheduledDelete(
            deleteObjectInstance.getMessageRetractionHandle(), deleteTime, federationExecutionObjectInstance));
        }
      }

      for (FederateProxy federateProxy : federationExecution.getFederates().values())
      {
        if (federateProxy != deletingFederateProxy)
        {
          federateProxy.removeObjectInstance(deletingFederateProxy.getFederateHandle(), deleteObjectInstance);
        }
      }
    }
    finally
    {
      objectsLock.writeLock().unlock();
    }
  }

  public void removeObjectInstance(
    FederateHandle producingFederateHandle, DeleteObjectInstance deleteObjectInstance, OrderType receivedOrderType)
  {
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
        log.trace(LogMessages.DROPPING_REQUEST_OBJECT_INSTANCE_ATTRIBUTE_VALUE_UPDATE_OBJECT_INSTANCE_DELETED,
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

  public void retract(FederateProxy retractingFederateProxy, Retract retract)
  {
    objectsLock.readLock().lock();
    try
    {
      scheduledDeletes.retract(retract.getMessageRetractionHandle());
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

  public void unpublishObjectClass(FederateProxy federateProxy, Collection<ObjectInstanceHandle> objectInstanceHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      for (ObjectInstanceHandle objectInstanceHandle : objectInstanceHandles)
      {
        FederationExecutionObjectInstance objectInstance = objects.get(objectInstanceHandle);

        if (objectInstance != null)
        {
          objectInstance.unpublishObjectClass(federationExecution, federateProxy);
        }
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void unpublishObjectClassAttributes(
    FederateProxy federateProxy, AttributeHandleSet attributeHandles,
    Collection<ObjectInstanceHandle> objectInstanceHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      for (ObjectInstanceHandle objectInstanceHandle : objectInstanceHandles)
      {
        FederationExecutionObjectInstance objectInstance = objects.get(objectInstanceHandle);

        if (objectInstance != null)
        {
          objectInstance.unpublishObjectClassAttributes(federationExecution, federateProxy, attributeHandles);
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

  public void reserveObjectInstanceName(FederateProxy federateProxy, String objectInstanceName)
  {
    objectInstanceNamesLock.lock();
    try
    {
      FederateHandle reservingFederateHandle = reservedObjectInstanceNames.get(objectInstanceName);
      if (reservingFederateHandle == null)
      {
        reservedObjectInstanceNames.put(objectInstanceName, federateProxy.getFederateHandle());

        federateProxy.getFederateChannel().write(new ObjectInstanceNameReservationSucceeded(objectInstanceName));
      }
      else
      {
        log.debug(LogMessages.RESERVE_OBJECT_INSTANCE_NAME_FAILED_NAME_ALREADY_RESERVED,
                  objectInstanceName, reservingFederateHandle);

        federateProxy.getFederateChannel().write(new ObjectInstanceNameReservationFailed(objectInstanceName));
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

  public void reserveMultipleObjectInstanceName(FederateProxy federateProxy, Collection<String> objectInstanceNames)
  {
    objectInstanceNamesLock.lock();
    try
    {
      Map<String, FederateHandle> alreadyReservedObjectInstanceNames = new HashMap<>();
      Map<String, FederateHandle> reservedObjectInstanceNames = new HashMap<>();
      for (String objectInstanceName : objectInstanceNames)
      {
        FederateHandle reservingFederateHandle = this.reservedObjectInstanceNames.get(objectInstanceName);
        if (reservingFederateHandle == null)
        {
          reservedObjectInstanceNames.put(objectInstanceName, federateProxy.getFederateHandle());
        }
        else
        {
          alreadyReservedObjectInstanceNames.put(objectInstanceName, reservingFederateHandle);
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
        log.debug(LogMessages.RESERVE_OBJECT_INSTANCE_NAMES_FAILED_NAME_ALREADY_RESERVED,
                  alreadyReservedObjectInstanceNames);

        federateProxy.getFederateChannel().write(new MultipleObjectInstanceNameReservationFailed(objectInstanceNames));
      }
    }
    finally
    {
      objectInstanceNamesLock.unlock();
    }
  }

  public void releaseMultipleObjectInstanceName(FederateProxy federateProxy, Collection<String> names)
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
    String objectInstanceName, AttributeHandleSet publishedAttributeHandles,
    AttributeSetRegionSetPairList attributesAndRegions)
  {
    ObjectClass objectClass = federationExecution.getFDD().getObjectClassSafely(objectClassHandle);

    assert objectInstanceName.startsWith("HLA") ||
           federateProxy.getFederateHandle().equals(reservedObjectInstanceNames.get(objectInstanceName));

    FederationExecutionObjectInstance objectInstance = new FederationExecutionObjectInstance(
      objectInstanceHandle, objectClass, objectInstanceName, publishedAttributeHandles, attributesAndRegions,
      federateProxy);

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

  public void updateAttributeValues(FederateProxy producingFederateProxy, UpdateAttributeValues updateAttributeValues)
  {
    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance = objects.get(updateAttributeValues.getObjectInstanceHandle());
      if (objectInstance != null)
      {
        objectInstance.updateAttributeValues(producingFederateProxy, updateAttributeValues);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public boolean reflectAttributeValues(
    FederateProxy receivingFederateProxy, FederateHandle producingFederateHandle,
    UpdateAttributeValues updateAttributeValues, OrderType orderType)
  {
    boolean delivered;

    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance = objects.get(updateAttributeValues.getObjectInstanceHandle());
      if (objectInstance == null)
      {
        // TODO: ignore, log

        delivered = false;
      }
      else
      {
        delivered = receivingFederateProxy.reflectAttributeValuesNow(
          producingFederateHandle, updateAttributeValues, orderType, objectInstance);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
    return delivered;
  }

  public void unconditionalAttributeOwnershipDivestiture(
    FederateProxy owner, ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance = objects.get(objectInstanceHandle);
      if (objectInstance == null)
      {
        log.trace(LogMessages.DROPPING_UNCONDITIONAL_ATTRIBUTE_OWNERSHIP_DIVESTITURE_OBJECT_INSTANCE_OBJECT_DELETED,
                  objectInstanceHandle);
      }
      else
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
        log.trace(LogMessages.DROPPING_NEGOTIATED_ATTRIBUTE_OWNERSHIP_DIVESTITURE_OBJECT_INSTANCE_OBJECT_DELETED,
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
        log.trace(LogMessages.DROPPING_CONFIRM_DIVESTITURE_INSTANCE_OBJECT_DELETED, objectInstanceHandle);
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
        log.trace(LogMessages.DROPPING_ATTRIBUTE_OWNERSHIP_ACQUISITION_OBJECT_INSTANCE_OBJECT_DELETED,
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
        log.trace(LogMessages.DROPPING_UNCONDITIONAL_ATTRIBUTE_OWNERSHIP_DIVESTITURE_IF_AVAILABLE_OBJECT_INSTANCE_OBJECT_DELETED,
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

  public Map<AttributeHandle, FederationExecutionAttributeInstance.Divestiture> attributeOwnershipDivestitureIfWanted(
    FederateProxy owner, ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
  {
    Map<AttributeHandle, FederationExecutionAttributeInstance.Divestiture> divestitures;

    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance = objects.get(objectInstanceHandle);
      if (objectInstance == null)
      {
        log.trace(LogMessages.DROPPING_ATTRIBUTE_OWNERSHIP_DIVESTITURE_IF_WANTED_OBJECT_INSTANCE_OBJECT_DELETED,
                  objectInstanceHandle);

        divestitures = Collections.emptyMap();
      }
      else
      {
        divestitures = objectInstance.attributeOwnershipDivestitureIfWanted(
          federationExecution, owner, attributeHandles);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }

    return divestitures;
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
        log.trace(LogMessages.DROPPING_QUERY_ATTRIBUTE_OWNERSHIP_OBJECT_INSTANCE_OBJECT_DELETED, objectInstanceHandle);
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
        log.trace(LogMessages.DROPPING_ASSOCIATE_REGIONS_FOR_UPDATES_OBJECT_INSTANCE_OBJECT_DELETED,
                  objectInstanceHandle);

        federateProxy.getFederateChannel().write(new AssociateRegionsForUpdatesResponse(
          associateRegionsForUpdates.getRequestId(),
          FederateMessageProtos.AssociateRegionsForUpdatesResponse.Failure.Cause.OBJECT_INSTANCE_NOT_KNOWN));
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
          unassociateRegionsForUpdates.getRequestId(),
          FederateMessageProtos.UnassociateRegionsForUpdatesResponse.Failure.Cause.OBJECT_INSTANCE_NOT_KNOWN));
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

  public void getUpdateRateValueForAttribute(
    FederateProxy federateProxy, GetUpdateRateValueForAttribute getUpdateRateValueForAttribute)
  {
    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance =
        objects.get(getUpdateRateValueForAttribute.getObjectInstanceHandle());
      if (objectInstance == null)
      {
        // the object was deleted after an associate was issued...

        federateProxy.getFederateChannel().write(new GetUpdateRateValueForAttributeResponse(
          getUpdateRateValueForAttribute.getRequestId(),
          FederateMessageProtos.GetUpdateRateValueForAttributeResponse.Failure.Cause.OBJECT_INSTANCE_NOT_KNOWN));
      }
      else
      {
        objectInstance.getUpdateRateValueForAttribute(federateProxy, getUpdateRateValueForAttribute);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void galtUpdated(LogicalTime galt)
  {
    // expire all the scheduled deletes up to (and including) galt
    //
    scheduledDeletes.expire(galt);
  }

  public void galtUndefined()
  {
    // expire all the scheduled deletes (will remove all the objects immediately)
    //
    scheduledDeletes.expireAll();
  }

  public void saveState(CodedOutputStream out)
    throws IOException
  {
    FederationExecutionObjectManagerState.Builder objectManagerState = FederationExecutionObjectManagerState.newBuilder();

    objectManagerState.setReservedObjectInstanceNameCount(reservedObjectInstanceNames.size());
    objectManagerState.setObjectInstanceStateCount(objects.size());

    out.writeMessageNoTag(objectManagerState.build());

    for (Map.Entry<String, FederateHandle> entry : reservedObjectInstanceNames.entrySet())
    {
      FederationExecutionObjectManagerState.ReservedObjectInstanceName.Builder reservedObjectInstanceName =
        FederationExecutionObjectManagerState.ReservedObjectInstanceName.newBuilder();

      reservedObjectInstanceName.setObjectInstanceName(entry.getKey());
      reservedObjectInstanceName.setFederateHandle(FederateHandles.convert(entry.getValue()));

      out.writeMessageNoTag(reservedObjectInstanceName.build());
    }

    for (FederationExecutionObjectInstance federationExecutionObjectInstance : objects.values())
    {
      out.writeMessageNoTag(federationExecutionObjectInstance.saveState().build());
    }
  }

  public void restoreState(CodedInputStream in)
    throws IOException
  {
    FederationExecutionObjectManagerState objectManagerState =
      in.readMessage(FederationExecutionObjectManagerState.PARSER, null);

    reservedObjectInstanceNames.clear();
    for (int reservedObjectInstanceNameCount = objectManagerState.getReservedObjectInstanceNameCount();
         reservedObjectInstanceNameCount > 0; --reservedObjectInstanceNameCount)
    {
      FederationExecutionObjectManagerState.ReservedObjectInstanceName reservedObjectInstanceName =
        in.readMessage(FederationExecutionObjectManagerState.ReservedObjectInstanceName.PARSER, null);

      reservedObjectInstanceNames.put(reservedObjectInstanceName.getObjectInstanceName(),
                                      FederateHandles.convert(reservedObjectInstanceName.getFederateHandle()));
    }

    objects.clear();
    for (int objectInstanceStateCount = objectManagerState.getObjectInstanceStateCount(); objectInstanceStateCount > 0;
         --objectInstanceStateCount)
    {
      FederationExecutionObjectManagerState.FederationExecutionObjectInstanceState objectInstanceState =
        in.readMessage(FederationExecutionObjectManagerState.FederationExecutionObjectInstanceState.PARSER, null);

      FederationExecutionObjectInstance federationExecutionObjectInstance =
        new FederationExecutionObjectInstance(objectInstanceState, federationExecution);
      objects.put(federationExecutionObjectInstance.getObjectInstanceHandle(), federationExecutionObjectInstance);
    }
  }

  private class ScheduledDelete
    extends Retractable
  {
    private final FederationExecutionObjectInstance federationExecutionObjectInstance;

    private ScheduledDelete(
      MessageRetractionHandle messageRetractionHandle, LogicalTime time,
      FederationExecutionObjectInstance federationExecutionObjectInstance)
    {
      super(messageRetractionHandle, time);

      this.federationExecutionObjectInstance = federationExecutionObjectInstance;
    }

    @Override
    public void retract()
    {
      super.retract();

      federationExecutionObjectInstance.setDeleteObjectInstance(null);
    }

    @Override
    public void expire()
    {
      super.expire();

      objectsLock.writeLock().lock();
      try
      {
        objects.remove(federationExecutionObjectInstance.getObjectInstanceHandle());
      }
      finally
      {
        objectsLock.writeLock().unlock();
      }
    }
  }
}
