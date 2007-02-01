/*
 * Copyright (c) 2007, Michael Newcomb
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

package net.sf.ohla.rti1516.federation.objects;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.ohla.rti1516.OHLAObjectInstanceHandle;
import net.sf.ohla.rti1516.fdd.ObjectClass;
import net.sf.ohla.rti1516.messages.callbacks.DiscoverObjectInstance;
import net.sf.ohla.rti1516.messages.callbacks.ObjectInstanceNameReservationFailed;
import net.sf.ohla.rti1516.messages.callbacks.ObjectInstanceNameReservationSucceeded;
import net.sf.ohla.rti1516.messages.callbacks.ReflectAttributeValues;
import net.sf.ohla.rti1516.federation.Federate;
import net.sf.ohla.rti1516.federation.FederationExecution;

import org.apache.mina.common.WriteFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeSetRegionSetPairList;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.RegionHandleSet;
import hla.rti1516.OrderType;
import hla.rti1516.TransportationType;
import hla.rti1516.LogicalTime;
import hla.rti1516.MessageRetractionHandle;

public class ObjectManager
{
  protected final FederationExecution federationExecution;

  protected AtomicInteger objectInstanceCount =
    new AtomicInteger(Integer.MIN_VALUE);

  protected ConcurrentMap<String, Federate> reservedObjectInstanceNames =
    new ConcurrentHashMap<String, Federate>();

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

  protected final Logger log;

  public ObjectManager(
    FederationExecution federationExecution)
  {
    this.federationExecution = federationExecution;

    log = LoggerFactory.getLogger(
      String.format("%s.%s", getClass(), federationExecution.getName()));
  }

  public void subscribeObjectClassAttributes(
    Federate federate, ObjectClass objectClass,
    AttributeHandleSet attributeHandles,
    AttributeSetRegionSetPairList attributesAndRegions)
  {
    WriteFuture lastWriteFuture = null;

    objectsLock.readLock().lock();
    try
    {
      for (ObjectInstance objectInstance : objects.values())
      {
        if (objectClass.isAssignableFrom(objectInstance.getObjectClass()))
        {
          // TODO: DDM

          lastWriteFuture = federate.discoverObjectInstance(
            new DiscoverObjectInstance(
              objectInstance.getObjectInstanceHandle(),
              objectClass.getObjectClassHandle(), objectInstance.getName()));
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

  public void reserveObjectInstanceName(Federate federate, String name)
  {
    Federate reservingFederate =
      reservedObjectInstanceNames.putIfAbsent(name, federate);
    if (reservingFederate != null)
    {
      log.debug("object instance name already reserved: {} by {}", name,
                reservingFederate);

      federate.getSession().write(
        new ObjectInstanceNameReservationFailed(name));
    }
    else
    {
      log.debug("object instance name reserved: {} by {}", name, federate);

      federate.getSession().write(
        new ObjectInstanceNameReservationSucceeded(name));
    }
  }

  public ObjectInstanceHandle registerObjectInstance(
    Federate federate, ObjectClassHandle objectClassHandle,
    Set<AttributeHandle> publishedAttributeHandles, String name)
  {
    ObjectInstanceHandle objectInstanceHandle = nextObjectInstanceHandle();

    ObjectClass objectClass =
      federationExecution.getFDD().getObjectClasses().get(objectClassHandle);
    assert objectClass != null;

    assert name == null || federate.equals(
      reservedObjectInstanceNames.get(name));

    ObjectInstance objectInstance = new ObjectInstance(
      objectInstanceHandle, objectClass, name,
      publishedAttributeHandles, federate);

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
    Federate federate, ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleValueMap attributeValues, byte[] tag,
    RegionHandleSet sentRegionHandles, OrderType sentOrderType,
    TransportationType transportationType, LogicalTime updateTime,
    MessageRetractionHandle messageRetractionHandle)
  {
    objectsLock.readLock().lock();
    try
    {
      ObjectInstance objectInstance = objects.get(objectInstanceHandle);
      if (objectInstance != null)
      {
        if (sentOrderType == OrderType.TIMESTAMP)
        {
          // TODO: track for future federates
        }

        ReflectAttributeValues reflectAttributeValues =
          new ReflectAttributeValues(objectInstanceHandle, attributeValues,
            tag, sentRegionHandles, sentOrderType, transportationType,
            updateTime, messageRetractionHandle,
            objectInstance.getObjectClass());

        federationExecution.getFederatesLock().lock();
        try
        {
          for (Federate f : federationExecution.getFederates().values())
          {
            if (f != federate)
            {
              f.reflectAttributeValues(reflectAttributeValues);
            }
          }
        }
        finally
        {
          federationExecution.getFederatesLock().unlock();
        }
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void unconditionalAttributeOwnershipDivestiture(
    Federate owner, ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      ObjectInstance objectInstance = objects.get(objectInstanceHandle);
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
    Federate owner, ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
  {
    objectsLock.readLock().lock();
    try
    {
      ObjectInstance objectInstance = objects.get(objectInstanceHandle);
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
    Federate owner, ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      ObjectInstance objectInstance = objects.get(objectInstanceHandle);
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
    Federate acquiree, ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
  {
    objectsLock.readLock().lock();
    try
    {
      ObjectInstance objectInstance = objects.get(objectInstanceHandle);
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
    Federate acquiree, ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      ObjectInstance objectInstance = objects.get(objectInstanceHandle);
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

  public Map<AttributeHandle, Federate> attributeOwnershipDivestitureIfWanted(
    Federate owner, ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      ObjectInstance objectInstance = objects.get(objectInstanceHandle);
      return objectInstance != null ?
        objectInstance.attributeOwnershipDivestitureIfWanted(
          owner, attributeHandles) :
        (Map<AttributeHandle, Federate>) Collections.EMPTY_MAP;
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture(
    Federate owner, ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      ObjectInstance objectInstance = objects.get(objectInstanceHandle);
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
    Federate acquiree, ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      ObjectInstance objectInstance = objects.get(objectInstanceHandle);
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
    Federate federate, ObjectInstanceHandle objectInstanceHandle,
    AttributeHandle attributeHandle)
  {
    objectsLock.readLock().lock();
    try
    {
      ObjectInstance objectInstance = objects.get(objectInstanceHandle);
      if (objectInstance == null)
      {
        // the object was deleted after a query was issued...
      }
      else
      {
        objectInstance.queryAttributeOwnership(federate, attributeHandle);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  protected ObjectInstanceHandle nextObjectInstanceHandle()
  {
    return new OHLAObjectInstanceHandle(objectInstanceCount.incrementAndGet());
  }
}
