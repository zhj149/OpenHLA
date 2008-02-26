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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.hla.rti1516.IEEE1516ObjectInstanceHandle;
import net.sf.ohla.rti.messages.UpdateAttributeValues;
import net.sf.ohla.rti.messages.callbacks.DiscoverObjectInstance;
import net.sf.ohla.rti.messages.callbacks.ObjectInstanceNameReservationFailed;
import net.sf.ohla.rti.messages.callbacks.ObjectInstanceNameReservationSucceeded;

import org.apache.mina.common.WriteFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeSetRegionSetPairList;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.OrderType;

public class FederationExecutionObjectManager
{
  protected final FederationExecution federationExecution;

  protected AtomicInteger objectInstanceCount =
    new AtomicInteger(Integer.MIN_VALUE);

  protected ConcurrentMap<String, FederateProxy> reservedObjectInstanceNames =
    new ConcurrentHashMap<String, FederateProxy>();

  protected Lock retiredObjectInstanceNamesLock = new ReentrantLock(true);
  protected Set<String> retiredObjectInstanceNames = new HashSet<String>();

  protected ReadWriteLock objectsLock = new ReentrantReadWriteLock(true);
  protected Map<ObjectInstanceHandle, FederationExecutionObjectInstance> objects =
    new HashMap<ObjectInstanceHandle, FederationExecutionObjectInstance>();
  protected Map<String, FederationExecutionObjectInstance> objectsByName =
    new HashMap<String, FederationExecutionObjectInstance>();
  protected Map<ObjectClassHandle, Set<ObjectInstanceHandle>> objectsByObjectClassHandle =
    new HashMap<ObjectClassHandle, Set<ObjectInstanceHandle>>();
  protected Set<ObjectInstanceHandle> removedObjects =
    new HashSet<ObjectInstanceHandle>();

  protected final Logger log = LoggerFactory.getLogger(getClass());
  protected final Marker marker;

  public FederationExecutionObjectManager(
    FederationExecution federationExecution)
  {
    this.federationExecution = federationExecution;

    marker = federationExecution.getMarker();
  }

  public FederationExecution getFederationExecution()
  {
    return federationExecution;
  }

  public void subscribeObjectClassAttributes(
    FederateProxy federateProxy, ObjectClass objectClass,
    AttributeHandleSet attributeHandles,
    AttributeSetRegionSetPairList attributesAndRegions)
  {
    WriteFuture lastWriteFuture = null;

    objectsLock.readLock().lock();
    try
    {
      for (FederationExecutionObjectInstance objectInstance : objects.values())
      {
        if (objectClass.isAssignableFrom(objectInstance.getObjectClass()))
        {
          // TODO: DDM

          lastWriteFuture = federateProxy.discoverObjectInstance(
            new DiscoverObjectInstance(
              objectInstance.getObjectInstanceHandle(),
              objectClass, objectInstance.getName()));
        }
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }

    if (lastWriteFuture != null)
    {
      // wait until the last discover has been sent
      //
      lastWriteFuture.join();
    }
  }

  public void reserveObjectInstanceName(FederateProxy federateProxy, String name)
  {
    FederateProxy reservingFederateProxy =
      reservedObjectInstanceNames.putIfAbsent(name, federateProxy);
    if (reservingFederateProxy != null)
    {
      log.debug(marker, "object instance name already reserved: {} by {}", name,
                reservingFederateProxy);

      federateProxy.getSession().write(
        new ObjectInstanceNameReservationFailed(name));
    }
    else
    {
      log.debug(marker, "object instance name reserved: {} by {}",
                name, federateProxy);

      federateProxy.getSession().write(
        new ObjectInstanceNameReservationSucceeded(name));
    }
  }

  public ObjectInstanceHandle registerObjectInstance(
    FederateProxy federateProxy, ObjectClassHandle objectClassHandle,
    Set<AttributeHandle> publishedAttributeHandles, String name)
  {
    ObjectInstanceHandle objectInstanceHandle = nextObjectInstanceHandle();

    ObjectClass objectClass =
      federationExecution.getFDD().getObjectClasses().get(objectClassHandle);
    assert objectClass != null;

    assert name == null || federateProxy.equals(
      reservedObjectInstanceNames.get(name));

    FederationExecutionObjectInstance objectInstance =
      new FederationExecutionObjectInstance(
      objectInstanceHandle, objectClass, name,
      publishedAttributeHandles, this, federateProxy);

    objectsLock.writeLock().lock();
    try
    {
      objects.put(objectInstanceHandle, objectInstance);
    }
    finally
    {
      objectsLock.writeLock().unlock();
    }

    return objectInstanceHandle;
  }

  public void updateAttributeValues(
    FederateProxy federateProxy, UpdateAttributeValues updateAttributeValues)
  {
    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance =
        objects.get(updateAttributeValues.getObjectInstanceHandle());
      if (objectInstance != null)
      {
        if (updateAttributeValues.getSentOrderType() == OrderType.TIMESTAMP)
        {
          // TODO: track for future federates
        }

        objectInstance.updateAttributeValues(
          federationExecution, federateProxy, updateAttributeValues);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void unconditionalAttributeOwnershipDivestiture(
    FederateProxy owner, ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance =
        objects.get(objectInstanceHandle);
      if (objectInstance != null)
      {
        objectInstance.unconditionalAttributeOwnershipDivestiture(
          owner, attributeHandles);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void negotiatedAttributeOwnershipDivestiture(
    FederateProxy owner, ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
  {
    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance =
        objects.get(objectInstanceHandle);
      if (objectInstance != null)
      {
        objectInstance.negotiatedAttributeOwnershipDivestiture(
          owner, attributeHandles, tag);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void confirmDivestiture(
    FederateProxy owner, ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance =
        objects.get(objectInstanceHandle);
      if (objectInstance != null)
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
    FederateProxy acquiree, ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
  {
    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance =
        objects.get(objectInstanceHandle);
      if (objectInstance != null)
      {
        objectInstance.attributeOwnershipAcquisition(
          acquiree, attributeHandles, tag);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisitionIfAvailable(
    FederateProxy acquiree, ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance =
        objects.get(objectInstanceHandle);
      if (objectInstance != null)
      {
        objectInstance.attributeOwnershipAcquisitionIfAvailable(
          acquiree, attributeHandles);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public Map<AttributeHandle, FederateProxy> attributeOwnershipDivestitureIfWanted(
    FederateProxy owner, ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance =
        objects.get(objectInstanceHandle);
      return objectInstance != null ?
        objectInstance.attributeOwnershipDivestitureIfWanted(
          owner, attributeHandles) :
        (Map<AttributeHandle, FederateProxy>) Collections.EMPTY_MAP;
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture(
    FederateProxy owner, ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance =
        objects.get(objectInstanceHandle);
      if (objectInstance != null)
      {
        objectInstance.cancelNegotiatedAttributeOwnershipDivestiture(
          owner, attributeHandles);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void cancelAttributeOwnershipAcquisition(
    FederateProxy acquiree, ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance =
        objects.get(objectInstanceHandle);
      if (objectInstance != null)
      {
        objectInstance.cancelAttributeOwnershipAcquisition(
          acquiree, attributeHandles);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void queryAttributeOwnership(
    FederateProxy federateProxy, ObjectInstanceHandle objectInstanceHandle,
    AttributeHandle attributeHandle)
  {
    objectsLock.readLock().lock();
    try
    {
      FederationExecutionObjectInstance objectInstance =
        objects.get(objectInstanceHandle);
      if (objectInstance == null)
      {
        // the object was deleted after a query was issued...
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

  protected ObjectInstanceHandle nextObjectInstanceHandle()
  {
    return new IEEE1516ObjectInstanceHandle(objectInstanceCount.incrementAndGet());
  }
}
