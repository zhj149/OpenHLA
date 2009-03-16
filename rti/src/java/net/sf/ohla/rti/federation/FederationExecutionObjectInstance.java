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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.fdd.Attribute;
import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.hla.rti1516.IEEE1516AttributeHandleSet;
import net.sf.ohla.rti.messages.AssociateRegionsForUpdates;
import net.sf.ohla.rti.messages.DefaultResponse;
import net.sf.ohla.rti.messages.UnassociateRegionsForUpdates;
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
import hla.rti1516.AttributeRegionAssociation;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.RegionHandle;

public class FederationExecutionObjectInstance
  implements Serializable
{
  protected final ObjectInstanceHandle objectInstanceHandle;
  protected final ObjectClass objectClass;

  protected final String name;

  protected final ReadWriteLock objectLock = new ReentrantReadWriteLock(true);

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

    for (FederateProxy f : federationExecution.getFederates().values())
    {
      if (f != federateProxy)
      {
        f.reflectAttributeValues(updateAttributeValues);
      }
    }
  }

  public void unconditionalAttributeOwnershipDivestiture(
    FederateProxy owner, AttributeHandleSet attributeHandles)
  {
    objectLock.readLock().lock();
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
      objectLock.readLock().unlock();
    }
  }

  public void negotiatedAttributeOwnershipDivestiture(
    FederateProxy owner, AttributeHandleSet attributeHandles, byte[] tag)
  {
    objectLock.readLock().lock();
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
      objectLock.readLock().unlock();
    }
  }

  public void confirmDivestiture(
    FederateProxy owner, AttributeHandleSet attributeHandles)
  {
    objectLock.readLock().lock();
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
      objectLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisition(
    FederateProxy acquiree, AttributeHandleSet attributeHandles, byte[] tag)
  {
    objectLock.readLock().lock();
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
      objectLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisitionIfAvailable(
    FederateProxy acquiree, AttributeHandleSet attributeHandles)
  {
    objectLock.readLock().lock();
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
      objectLock.readLock().unlock();
    }
  }

  public Map<AttributeHandle, FederateProxy> attributeOwnershipDivestitureIfWanted(
    FederateProxy owner, AttributeHandleSet attributeHandles)
  {
    objectLock.readLock().lock();
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
      objectLock.readLock().unlock();
    }
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture(
    FederateProxy owner, AttributeHandleSet attributeHandles)
  {
    objectLock.readLock().lock();
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
      objectLock.readLock().unlock();
    }
  }

  public void cancelAttributeOwnershipAcquisition(
    FederateProxy acquiree, AttributeHandleSet attributeHandles)
  {
    objectLock.readLock().lock();
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
      objectLock.readLock().unlock();
    }
  }

  public void queryAttributeOwnership(
    FederateProxy federateProxy, AttributeHandle attributeHandle)
  {
    objectLock.readLock().lock();
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
      objectLock.readLock().unlock();
    }
  }

  public void associateRegionsForUpdates(
    FederateProxy federateProxy,
    AssociateRegionsForUpdates associateRegionsForUpdates)
  {
    Map<RegionHandle, FederationExecutionRegion> regions =
      objectManager.getFederationExecution().getRegionManager().getRegions();

    objectLock.writeLock().lock();
    try
    {
      for (AttributeRegionAssociation attributeRegionAssociation :
        associateRegionsForUpdates.getAttributesAndRegions())
      {
        for (AttributeHandle attributeHandle :
          attributeRegionAssociation.attributes)
        {
          FederationExecutionAttributeInstance attribute =
            attributes.get(attributeHandle);
          for (RegionHandle regionHandle : attributeRegionAssociation.regions)
          {
            attribute.associateRegionForUpdate(
              federateProxy, regions.get(regionHandle));
          }
        }
      }

      federateProxy.getSession().write(
        new DefaultResponse(associateRegionsForUpdates.getId()));
    }
    finally
    {
      objectLock.writeLock().unlock();
    }
  }

  public void unassociateRegionsForUpdates(
    FederateProxy federateProxy,
    UnassociateRegionsForUpdates unassociateRegionsForUpdates)
  {
    objectLock.writeLock().lock();
    try
    {
      for (AttributeRegionAssociation attributeRegionAssociation :
        unassociateRegionsForUpdates.getAttributesAndRegions())
      {
        for (AttributeHandle attributeHandle :
          attributeRegionAssociation.attributes)
        {
          attributes.get(attributeHandle).unassociateRegionsForUpdates(
            federateProxy, attributeRegionAssociation.regions);
        }
      }

      federateProxy.getSession().write(
        new DefaultResponse(unassociateRegionsForUpdates.getId()));
    }
    finally
    {
      objectLock.writeLock().unlock();
    }
  }

  public boolean regionsIntersect(
    AttributeHandle attributeHandle,
    FederationExecutionRegionManager regionManager,
    Set<RegionHandle> regionHandles)
  {
    boolean intersects = false;

    objectLock.readLock().lock();
    try
    {
      FederationExecutionAttributeInstance attributeInstance =
        attributes.get(attributeHandle);
      if (attributeInstance != null)
      {
        intersects = attributeInstance.regionIntersects(
          regionManager, regionHandles);
      }
    }
    finally
    {
      objectLock.readLock().unlock();
    }

    return intersects;
  }
}
