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

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.ohla.rti.fdd.Attribute;
import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.hla.rti1516.IEEE1516AttributeHandleSet;
import net.sf.ohla.rti.messages.UpdateAttributeValues;
import net.sf.ohla.rti.messages.callbacks.AttributeIsNotOwned;
import net.sf.ohla.rti.messages.callbacks.AttributeIsOwnedByRTI;
import net.sf.ohla.rti.messages.callbacks.AttributeOwnershipAcquisitionNotification;
import net.sf.ohla.rti.messages.callbacks.AttributeOwnershipUnavailable;
import net.sf.ohla.rti.messages.callbacks.ConfirmAttributeOwnershipAcquisitionCancellation;
import net.sf.ohla.rti.messages.callbacks.InformAttributeOwnership;
import net.sf.ohla.rti.messages.callbacks.RequestAttributeOwnershipRelease;
import net.sf.ohla.rti.messages.callbacks.RequestDivestitureConfirmation;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.RegionHandle;

public class FederationExecutionObjectInstance
  implements Serializable
{
  protected final ObjectInstanceHandle objectInstanceHandle;
  protected final ObjectClass objectClass;

  protected final String name;

  protected final Lock objectLock = new ReentrantLock(true);

  protected final Map<AttributeHandle, FederationExecutionAttributeInstance> attributes =
    new HashMap<AttributeHandle, FederationExecutionAttributeInstance>();

  protected final FederationExecutionObjectManager objectManager;

  public FederationExecutionObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, ObjectClass objectClass,
    String name, Set<AttributeHandle> publishedAttributeHandles,
    FederationExecutionObjectManager objectManager, FederateProxy owner)
  {
    this.objectInstanceHandle = objectInstanceHandle;
    this.objectClass = objectClass;
    this.name = name;
    this.objectManager = objectManager;

    for (Attribute attribute : objectClass.getAttributes().values())
    {
      FederationExecutionAttributeInstance attributeInstance =
        new FederationExecutionAttributeInstance(attribute, this);
      attributes.put(attribute.getAttributeHandle(), attributeInstance);

      if (publishedAttributeHandles.contains(attribute.getAttributeHandle()))
      {
        attributeInstance.setOwner(owner);
      }
    }
  }

  public ObjectInstanceHandle getObjectInstanceHandle()
  {
    return objectInstanceHandle;
  }

  public ObjectClass getObjectClass()
  {
    return objectClass;
  }

  public String getName()
  {
    return name;
  }

  public FederationExecutionObjectManager getObjectManager()
  {
    return objectManager;
  }

  public FederateProxy getOwner(AttributeHandle attributeHandle)
  {
    return attributes.get(attributeHandle).getOwner();
  }

  public void updateAttributeValues(
    FederationExecution federationExecution, FederateProxy federateProxy,
    UpdateAttributeValues updateAttributeValues)
  {
    updateAttributeValues.setObjectInstance(this);

    federationExecution.getFederatesLock().writeLock().lock();
    try
    {
      for (FederateProxy f : federationExecution.getFederates().values())
      {
        if (f != federateProxy)
        {
          f.reflectAttributeValues(updateAttributeValues);
        }
      }
    }
    finally
    {
      federationExecution.getFederatesLock().writeLock().unlock();
    }
  }

  public void unconditionalAttributeOwnershipDivestiture(
    FederateProxy owner, AttributeHandleSet attributeHandles)
  {
    objectLock.lock();
    try
    {
      Map<FederateProxy, AttributeHandleSet> newOwners =
        new HashMap<FederateProxy, AttributeHandleSet>();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        FederateProxy newOwner =
          attributes.get(
            attributeHandle).unconditionalAttributeOwnershipDivestiture();
        if (newOwner != null)
        {
          AttributeHandleSet acquiredAttributes = newOwners.get(newOwner);
          if (acquiredAttributes == null)
          {
            acquiredAttributes = new IEEE1516AttributeHandleSet();
            newOwners.put(newOwner, acquiredAttributes);
          }
          acquiredAttributes.add(attributeHandle);
        }
      }

      // notify the new owners
      //
      for (Map.Entry<FederateProxy, AttributeHandleSet> entry : newOwners.entrySet())
      {
        entry.getKey().getSession().write(
          new AttributeOwnershipAcquisitionNotification(
            objectInstanceHandle, entry.getValue()));
      }
    }
    finally
    {
      objectLock.unlock();
    }
  }

  public void negotiatedAttributeOwnershipDivestiture(
    FederateProxy owner, AttributeHandleSet attributeHandles, byte[] tag)
  {
    objectLock.lock();
    try
    {
      AttributeHandleSet divestableAttributeHandles =
        new IEEE1516AttributeHandleSet();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        if (attributes.get(
          attributeHandle).negotiatedAttributeOwnershipDivestiture(tag))
        {
          divestableAttributeHandles.add(attributeHandle);
        }
      }

      if (!divestableAttributeHandles.isEmpty())
      {
        owner.getSession().write(new RequestDivestitureConfirmation(
          objectInstanceHandle, divestableAttributeHandles));
      }
    }
    finally
    {
      objectLock.unlock();
    }
  }

  public void confirmDivestiture(
    FederateProxy owner, AttributeHandleSet attributeHandles)
  {
    objectLock.lock();
    try
    {
      Map<FederateProxy, AttributeHandleSet> newOwners =
        new HashMap<FederateProxy, AttributeHandleSet>();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        FederateProxy newOwner =
          attributes.get(attributeHandle).confirmDivestiture();
        if (newOwner != null)
        {
          AttributeHandleSet acquiredAttributes = newOwners.get(newOwner);
          if (acquiredAttributes == null)
          {
            acquiredAttributes = new IEEE1516AttributeHandleSet();
            newOwners.put(newOwner, acquiredAttributes);
          }
          acquiredAttributes.add(attributeHandle);
        }
      }

      // notify the new owners
      //
      for (Map.Entry<FederateProxy, AttributeHandleSet> entry : newOwners.entrySet())
      {
        entry.getKey().getSession().write(
          new AttributeOwnershipAcquisitionNotification(
            objectInstanceHandle, entry.getValue()));
      }
    }
    finally
    {
      objectLock.unlock();
    }
  }

  public void attributeOwnershipAcquisition(
    FederateProxy acquiree, AttributeHandleSet attributeHandles, byte[] tag)
  {
    objectLock.lock();
    try
    {
      AttributeHandleSet acquiredAttributeHandles =
        new IEEE1516AttributeHandleSet();
      Map<FederateProxy, AttributeHandleSet> federatesThatNeedToConfirmDivestiture =
        new HashMap<FederateProxy, AttributeHandleSet>();
      Map<FederateProxy, AttributeHandleSet> federatesThatNeedToRelease =
        new HashMap<FederateProxy, AttributeHandleSet>();

      for (AttributeHandle attributeHandle : attributeHandles)
      {
        FederationExecutionAttributeInstance FederationExecutionAttributeInstance =
          attributes.get(attributeHandle);

        FederateProxy owner =
          FederationExecutionAttributeInstance.attributeOwnershipAcquisition(acquiree);
        if (acquiree.equals(owner))
        {
          // the attribute was unowned and therefore immediately acquired
          //
          acquiredAttributeHandles.add(attributeHandle);
        }
        else if (FederationExecutionAttributeInstance.wantsToDivest())
        {
          // the attribute is owned but the owner is willing to divest
          //
          AttributeHandleSet divestingAttributeHandles =
            federatesThatNeedToConfirmDivestiture.get(owner);
          if (divestingAttributeHandles == null)
          {
            divestingAttributeHandles = new IEEE1516AttributeHandleSet();
            federatesThatNeedToConfirmDivestiture.put(
              owner, divestingAttributeHandles);
          }
          divestingAttributeHandles.add(attributeHandle);
        }
        else
        {
          // the attribute is owned but the owner is unwilling to divest
          //
          AttributeHandleSet releasingAttributeHandles =
            federatesThatNeedToRelease.get(owner);
          if (releasingAttributeHandles == null)
          {
            releasingAttributeHandles = new IEEE1516AttributeHandleSet();
            federatesThatNeedToRelease.put(owner, releasingAttributeHandles);
          }
          releasingAttributeHandles.add(attributeHandle);
        }
      }

      if (!acquiredAttributeHandles.isEmpty())
      {
        acquiree.getSession().write(
          new AttributeOwnershipAcquisitionNotification(
            objectInstanceHandle, acquiredAttributeHandles, tag));
      }

      for (Map.Entry<FederateProxy, AttributeHandleSet> entry :
        federatesThatNeedToConfirmDivestiture.entrySet())
      {
        entry.getKey().getSession().write(new RequestDivestitureConfirmation(
          objectInstanceHandle, entry.getValue()));
      }

      for (Map.Entry<FederateProxy, AttributeHandleSet> entry :
        federatesThatNeedToRelease.entrySet())
      {
        entry.getKey().getSession().write(new RequestAttributeOwnershipRelease(
          objectInstanceHandle, entry.getValue(), tag));
      }
    }
    finally
    {
      objectLock.unlock();
    }
  }

  public void attributeOwnershipAcquisitionIfAvailable(
    FederateProxy acquiree, AttributeHandleSet attributeHandles)
  {
    objectLock.lock();
    try
    {
      AttributeHandleSet acquiredAttributeHandles =
        new IEEE1516AttributeHandleSet();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        if (attributes.get(
          attributeHandle).attributeOwnershipAcquisitionIfAvailable(acquiree))
        {
          acquiredAttributeHandles.add(attributeHandle);
        }
      }

      if (!acquiredAttributeHandles.isEmpty())
      {
        acquiree.getSession().write(
          new AttributeOwnershipAcquisitionNotification(
            objectInstanceHandle, acquiredAttributeHandles));
      }

      AttributeHandleSet unacquiredAttributeHandles =
        new IEEE1516AttributeHandleSet(attributeHandles);
      unacquiredAttributeHandles.removeAll(acquiredAttributeHandles);

      if (!unacquiredAttributeHandles.isEmpty())
      {
        acquiree.getSession().write(new AttributeOwnershipUnavailable(
          objectInstanceHandle, unacquiredAttributeHandles));
      }
    }
    finally
    {
      objectLock.unlock();
    }
  }

  public Map<AttributeHandle, FederateProxy> attributeOwnershipDivestitureIfWanted(
    FederateProxy owner, AttributeHandleSet attributeHandles)
  {
    objectLock.lock();
    try
    {
      Map<AttributeHandle, FederateProxy> newOwners =
        new HashMap<AttributeHandle, FederateProxy>();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        FederateProxy newOwner = attributes.get(
          attributeHandle).attributeOwnershipDivestitureIfWanted();
        if (newOwner != null)
        {
          newOwners.put(attributeHandle, newOwner);
        }
      }

      return newOwners;
    }
    finally
    {
      objectLock.unlock();
    }
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture(
    FederateProxy owner, AttributeHandleSet attributeHandles)
  {
    objectLock.lock();
    try
    {
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        attributes.get(
          attributeHandle).cancelNegotiatedAttributeOwnershipDivestiture(owner);
      }
    }
    finally
    {
      objectLock.unlock();
    }
  }

  public void cancelAttributeOwnershipAcquisition(
    FederateProxy acquiree, AttributeHandleSet attributeHandles)
  {
    objectLock.lock();
    try
    {
      AttributeHandleSet canceledAcquisitionAttributeHandles =
        new IEEE1516AttributeHandleSet();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        if (attributes.get(
          attributeHandle).cancelAttributeOwnershipAcquisition(acquiree))
        {
          canceledAcquisitionAttributeHandles.add(attributeHandle);
        }
      }

      if (!canceledAcquisitionAttributeHandles.isEmpty())
      {
        acquiree.getSession().write(
          new ConfirmAttributeOwnershipAcquisitionCancellation(
            objectInstanceHandle, canceledAcquisitionAttributeHandles));
      }
    }
    finally
    {
      objectLock.unlock();
    }
  }

  public void queryAttributeOwnership(
    FederateProxy federateProxy, AttributeHandle attributeHandle)
  {
    objectLock.lock();
    try
    {
      FederationExecutionAttributeInstance FederationExecutionAttributeInstance =
        attributes.get(attributeHandle);
      assert FederationExecutionAttributeInstance != null;

      FederateProxy owner = FederationExecutionAttributeInstance.getOwner();
      if (owner == null)
      {
        federateProxy.getSession().write(new AttributeIsNotOwned(
          objectInstanceHandle, attributeHandle));
      }
      else if (FederationExecutionAttributeInstance.getAttribute().isMOM())
      {
        federateProxy.getSession().write(new AttributeIsOwnedByRTI(
          objectInstanceHandle, attributeHandle));
      }
      else
      {
        federateProxy.getSession().write(new InformAttributeOwnership(
          objectInstanceHandle, attributeHandle, owner.getFederateHandle()));
      }
    }
    finally
    {
      objectLock.unlock();
    }
  }

  public boolean regionsIntersect(Set<RegionHandle> regionHandles,
                                  AttributeHandle attributeHandle)
  {
    boolean intersects = true;

    FederationExecutionAttributeInstance attributeInstance =
      attributes.get(attributeHandle);
    if (attributeInstance != null)
    {
      // TODO; implement

      // intersects = attributeInstance.regionIntersects(regionHandles);
    }

    return intersects;
  }
}
