/*
 * Copyright (c) 2006, Michael Newcomb
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

package net.sf.ohla.rti1516.federation.ownership;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti1516.fdd.ObjectClass;
import net.sf.ohla.rti1516.federate.callbacks.DiscoverObjectInstance;
import net.sf.ohla.rti1516.federation.FederationExecution;

import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeSetRegionSetPairList;
import hla.rti1516.FederateHandle;
import hla.rti1516.ObjectInstanceHandle;

public class OwnershipManager
{
  protected FederationExecution federationExecution;

  protected ReadWriteLock objectsLock = new ReentrantReadWriteLock(true);
  protected Map<ObjectInstanceHandle, ObjectInstanceOwnership> objects =
    new HashMap<ObjectInstanceHandle, ObjectInstanceOwnership>();

  public OwnershipManager(FederationExecution federationExecution)
  {
    this.federationExecution = federationExecution;
  }

  public void registerObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, ObjectClass objectClass,
    Set<AttributeHandle> publishedAttributeHandles, FederateHandle owner)
  {
    objectsLock.writeLock().lock();
    try
    {
      objects.put(objectInstanceHandle, new ObjectInstanceOwnership(
        objectInstanceHandle, objectClass, publishedAttributeHandles, owner));
    }
    finally
    {
      objectsLock.writeLock().unlock();
    }
  }

  public void subscribeObjectClassAttributes(
    ObjectClass objectClass, AttributeHandleSet attributeHandles,
    AttributeSetRegionSetPairList attributesAndRegions, IoSession session)
  {
    objectsLock.readLock().lock();
    try
    {
      WriteFuture lastWriteFuture = null;
      for (ObjectInstanceOwnership objectInstanceOwnership : objects.values())
      {
        if (objectClass.isAssignableFrom(
          objectInstanceOwnership.getObjectClass()))
        {
          // TODO: DDM

          lastWriteFuture = session.write(new DiscoverObjectInstance(
            objectInstanceOwnership.getObjectInstanceHandle(),
            objectClass.getObjectClassHandle()));
        }
      }

      if (lastWriteFuture != null)
      {
        // wait until the last discover has been sent
        //
        lastWriteFuture.join();
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void unconditionalAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      ObjectInstanceOwnership objectInstanceOwnership =
        objects.get(objectInstanceHandle);
      if (objectInstanceOwnership != null)
      {
        objectInstanceOwnership.unconditionalAttributeOwnershipDivestiture(
          attributeHandles, federationExecution);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void negotiatedAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag, IoSession session)
  {
    objectsLock.readLock().lock();
    try
    {
      ObjectInstanceOwnership objectInstanceOwnership =
        objects.get(objectInstanceHandle);
      if (objectInstanceOwnership != null)
      {
        objectInstanceOwnership.negotiatedAttributeOwnershipDivestiture(
          attributeHandles, tag, session);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void confirmDivestiture(ObjectInstanceHandle objectInstanceHandle,
                                 AttributeHandleSet attributeHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      ObjectInstanceOwnership objectInstanceOwnership =
        objects.get(objectInstanceHandle);
      if (objectInstanceOwnership != null)
      {
        objectInstanceOwnership.confirmDivestiture(
          attributeHandles, federationExecution);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisition(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag, FederateHandle acquiree,
    IoSession session)
  {
    objectsLock.readLock().lock();
    try
    {
      ObjectInstanceOwnership objectInstanceOwnership =
        objects.get(objectInstanceHandle);
      if (objectInstanceOwnership != null)
      {
        objectInstanceOwnership.attributeOwnershipAcquisition(
          attributeHandles, tag, acquiree, session, federationExecution);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisitionIfAvailable(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, FederateHandle acquiree,
    IoSession session)
  {
    objectsLock.readLock().lock();
    try
    {
      ObjectInstanceOwnership objectInstanceOwnership =
        objects.get(objectInstanceHandle);
      if (objectInstanceOwnership != null)
      {
        objectInstanceOwnership.attributeOwnershipAcquisitionIfAvailable(
          attributeHandles, acquiree, session);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public Map<AttributeHandle, FederateHandle> attributeOwnershipDivestitureIfWanted(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      ObjectInstanceOwnership objectInstanceOwnership =
        objects.get(objectInstanceHandle);
      return objectInstanceOwnership != null ?
        objectInstanceOwnership.attributeOwnershipDivestitureIfWanted(
          attributeHandles) :
        (Map<AttributeHandle, FederateHandle>) Collections.EMPTY_MAP;
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, FederateHandle owner)
  {
    objectsLock.readLock().lock();
    try
    {
      ObjectInstanceOwnership objectInstanceOwnership =
        objects.get(objectInstanceHandle);
      if (objectInstanceOwnership != null)
      {
        objectInstanceOwnership.cancelNegotiatedAttributeOwnershipDivestiture(
          attributeHandles, owner);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void cancelAttributeOwnershipAcquisition(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, FederateHandle acquiree,
    IoSession session)
  {
    objectsLock.readLock().lock();
    try
    {
      ObjectInstanceOwnership objectInstanceOwnership =
        objects.get(objectInstanceHandle);
      if (objectInstanceOwnership != null)
      {
        objectInstanceOwnership.cancelAttributeOwnershipAcquisition(
          attributeHandles, acquiree, session);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void queryAttributeOwnership(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle,
    IoSession session)
  {
    objectsLock.readLock().lock();
    try
    {
      ObjectInstanceOwnership objectInstanceOwnership =
        objects.get(objectInstanceHandle);
      if (objectInstanceOwnership == null)
      {
        // the object was deleted after a query was issued...
      }
      else
      {
        objectInstanceOwnership.queryAttributeOwnership(
          attributeHandle, session);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }
}
