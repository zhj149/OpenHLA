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
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleSet;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleSetFactory;
import net.sf.ohla.rti.messages.AssociateRegionsForUpdates;
import net.sf.ohla.rti.messages.AssociateRegionsForUpdatesResponse;
import net.sf.ohla.rti.messages.RequestObjectClassAttributeValueUpdate;
import net.sf.ohla.rti.messages.RequestObjectClassAttributeValueUpdateWithRegions;
import net.sf.ohla.rti.messages.RequestObjectInstanceAttributeValueUpdate;
import net.sf.ohla.rti.messages.UnassociateRegionsForUpdates;
import net.sf.ohla.rti.messages.UnassociateRegionsForUpdatesResponse;
import net.sf.ohla.rti.messages.UpdateAttributeValues;
import net.sf.ohla.rti.messages.callbacks.AttributeIsNotOwned;
import net.sf.ohla.rti.messages.callbacks.AttributeOwnershipAcquisitionNotification;
import net.sf.ohla.rti.messages.callbacks.AttributeOwnershipUnavailable;
import net.sf.ohla.rti.messages.callbacks.ConfirmAttributeOwnershipAcquisitionCancellation;
import net.sf.ohla.rti.messages.callbacks.InformAttributeOwnership;
import net.sf.ohla.rti.messages.callbacks.ProvideAttributeValueUpdate;
import net.sf.ohla.rti.messages.callbacks.RequestAttributeOwnershipRelease;
import net.sf.ohla.rti.messages.callbacks.RequestDivestitureConfirmation;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeRegionAssociation;
import hla.rti1516e.DimensionHandle;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RangeBounds;
import hla.rti1516e.RegionHandle;

public class FederationExecutionObjectInstance
  implements Serializable
{
  private final ObjectInstanceHandle objectInstanceHandle;
  private final ObjectClass objectClass;
  private final String objectInstanceName;

  private final FederateHandle producingFederateHandle;

  private final ReadWriteLock objectLock = new ReentrantReadWriteLock(true);

  private final Map<AttributeHandle, FederationExecutionAttributeInstance> attributes =
    new HashMap<AttributeHandle, FederationExecutionAttributeInstance>();

  public FederationExecutionObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, ObjectClass objectClass,
    String objectInstanceName, Set<AttributeHandle> publishedAttributeHandles, FederateProxy owner)
  {
    this.objectInstanceHandle = objectInstanceHandle;
    this.objectClass = objectClass;
    this.objectInstanceName = objectInstanceName;

    producingFederateHandle = owner.getFederateHandle();

    for (Attribute attribute : objectClass.getAttributes().values())
    {
      FederationExecutionAttributeInstance attributeInstance = new FederationExecutionAttributeInstance(attribute);
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

  public String getObjectInstanceName()
  {
    return objectInstanceName;
  }

  public FederateHandle getProducingFederateHandle()
  {
    return producingFederateHandle;
  }

  public FederateProxy getOwner(AttributeHandle attributeHandle)
  {
    return attributes.get(attributeHandle).getOwner();
  }

  public void updateAttributeValues(FederateProxy federateProxy, UpdateAttributeValues updateAttributeValues)
  {
    objectLock.readLock().lock();
    federateProxy.getFederationExecution().getRegionManager().getRegionsLock().readLock().lock();
    try
    {
      for (FederateProxy f : federateProxy.getFederationExecution().getFederates().values())
      {
        if (f != federateProxy)
        {
          f.reflectAttributeValues(federateProxy, this, updateAttributeValues);
        }
      }
    }
    finally
    {
      federateProxy.getFederationExecution().getRegionManager().getRegionsLock().readLock().unlock();
      objectLock.readLock().unlock();
    }
  }

  public void requestObjectInstanceAttributeValueUpdate(
    FederateProxy federateProxy, RequestObjectInstanceAttributeValueUpdate requestObjectInstanceAttributeValueUpdate)
  {
    objectLock.readLock().lock();
    try
    {
      Map<FederateProxy, AttributeHandleSet> provideAttributeValueUpdate =
        new HashMap<FederateProxy, AttributeHandleSet>();

      for (AttributeHandle attributeHandle : requestObjectInstanceAttributeValueUpdate.getAttributeHandles())
      {
        FederationExecutionAttributeInstance attributeInstance = attributes.get(attributeHandle);

        FederateProxy owner = attributeInstance.getOwner();
        if (owner != null && owner != federateProxy)
        {
          AttributeHandleSet attributeHandles = provideAttributeValueUpdate.get(owner);
          if (attributeHandles == null)
          {
            attributeHandles = IEEE1516eAttributeHandleSetFactory.INSTANCE.create();
            provideAttributeValueUpdate.put(owner, attributeHandles);
          }
          attributeHandles.add(attributeHandle);
        }
      }

      for (Map.Entry<FederateProxy, AttributeHandleSet> entry : provideAttributeValueUpdate.entrySet())
      {
        entry.getKey().provideAttributeValueUpdate(new ProvideAttributeValueUpdate(
          objectInstanceHandle, entry.getValue(), requestObjectInstanceAttributeValueUpdate.getTag()));
      }
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void requestObjectClassAttributeValueUpdate(
    FederateProxy federateProxy, RequestObjectClassAttributeValueUpdate requestObjectClassAttributeValueUpdate)
  {
    objectLock.readLock().lock();
    try
    {
      Map<FederateProxy, AttributeHandleSet> provideAttributeValueUpdate =
        new HashMap<FederateProxy, AttributeHandleSet>();

      for (AttributeHandle attributeHandle : requestObjectClassAttributeValueUpdate.getAttributeHandles())
      {
        FederationExecutionAttributeInstance attributeInstance = attributes.get(attributeHandle);

        FederateProxy owner = attributeInstance.getOwner();
        if (owner != null && owner != federateProxy)
        {
          AttributeHandleSet attributeHandles = provideAttributeValueUpdate.get(owner);
          if (attributeHandles == null)
          {
            attributeHandles = IEEE1516eAttributeHandleSetFactory.INSTANCE.create();
            provideAttributeValueUpdate.put(owner, attributeHandles);
          }
          attributeHandles.add(attributeHandle);
        }
      }

      for (Map.Entry<FederateProxy, AttributeHandleSet> entry : provideAttributeValueUpdate.entrySet())
      {
        entry.getKey().provideAttributeValueUpdate(new ProvideAttributeValueUpdate(
          objectInstanceHandle, entry.getValue(), requestObjectClassAttributeValueUpdate.getTag()));
      }
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void requestObjectClassAttributeValueUpdateWithRegions(
    FederateProxy federateProxy,
    RequestObjectClassAttributeValueUpdateWithRegions requestObjectClassAttributeValueUpdateWithRegions)
  {
    objectLock.readLock().lock();
    federateProxy.getFederationExecution().getRegionManager().getRegionsLock().readLock().lock();
    try
    {
      Map<FederateProxy, AttributeHandleSet> provideAttributeValueUpdate =
        new HashMap<FederateProxy, AttributeHandleSet>();

      for (AttributeRegionAssociation attributeRegionAssociation :
        requestObjectClassAttributeValueUpdateWithRegions.getAttributesAndRegions())
      {
        for (AttributeHandle attributeHandle : attributeRegionAssociation.ahset)
        {
          FederationExecutionAttributeInstance attributeInstance = attributes.get(attributeHandle);

          FederateProxy owner = attributeInstance.getOwner();
          if (owner != null && owner != federateProxy)
          {
            AttributeHandleSet attributeHandles = provideAttributeValueUpdate.get(owner);
            if (attributeHandles == null)
            {
              if (attributeInstance.regionsIntersect(
                federateProxy.getFederationExecution().getRegionManager(), attributeRegionAssociation.rhset))
              {
                attributeHandles = IEEE1516eAttributeHandleSetFactory.INSTANCE.create();
                provideAttributeValueUpdate.put(owner, attributeHandles);

                attributeHandles.add(attributeHandle);
              }
            }
            else if (!attributeHandles.contains(attributeHandle) && attributeInstance.regionsIntersect(
              federateProxy.getFederationExecution().getRegionManager(), attributeRegionAssociation.rhset))
            {
              attributeHandles.add(attributeHandle);
            }
          }
        }
      }

      for (Map.Entry<FederateProxy, AttributeHandleSet> entry : provideAttributeValueUpdate.entrySet())
      {
        entry.getKey().provideAttributeValueUpdate(new ProvideAttributeValueUpdate(
          objectInstanceHandle, entry.getValue(), requestObjectClassAttributeValueUpdateWithRegions.getTag()));
      }
    }
    finally
    {
      federateProxy.getFederationExecution().getRegionManager().getRegionsLock().readLock().unlock();
      objectLock.readLock().unlock();
    }
  }

  public void unconditionalAttributeOwnershipDivestiture(FederateProxy owner, AttributeHandleSet attributeHandles)
  {
    objectLock.readLock().lock();
    try
    {
      Map<FederateProxy, AttributeHandleSet> newOwners = new HashMap<FederateProxy, AttributeHandleSet>();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        FederateProxy newOwner = attributes.get(attributeHandle).unconditionalAttributeOwnershipDivestiture();
        if (newOwner != null)
        {
          AttributeHandleSet acquiredAttributes = newOwners.get(newOwner);
          if (acquiredAttributes == null)
          {
            acquiredAttributes = new IEEE1516eAttributeHandleSet();
            newOwners.put(newOwner, acquiredAttributes);
          }
          acquiredAttributes.add(attributeHandle);
        }
      }

      // notify the new owners
      //
      for (Map.Entry<FederateProxy, AttributeHandleSet> entry : newOwners.entrySet())
      {
        entry.getKey().getFederateChannel().write(
          new AttributeOwnershipAcquisitionNotification(objectInstanceHandle, entry.getValue(), null));
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
      AttributeHandleSet divestableAttributeHandles = new IEEE1516eAttributeHandleSet();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        if (attributes.get(attributeHandle).negotiatedAttributeOwnershipDivestiture(tag))
        {
          divestableAttributeHandles.add(attributeHandle);
        }
      }

      if (!divestableAttributeHandles.isEmpty())
      {
        owner.getFederateChannel().write(new RequestDivestitureConfirmation(
          objectInstanceHandle, divestableAttributeHandles));
      }
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void confirmDivestiture(FederateProxy owner, AttributeHandleSet attributeHandles)
  {
    objectLock.readLock().lock();
    try
    {
      Map<FederateProxy, AttributeHandleSet> newOwners = new HashMap<FederateProxy, AttributeHandleSet>();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        FederateProxy newOwner = attributes.get(attributeHandle).confirmDivestiture();
        if (newOwner != null)
        {
          AttributeHandleSet acquiredAttributes = newOwners.get(newOwner);
          if (acquiredAttributes == null)
          {
            acquiredAttributes = new IEEE1516eAttributeHandleSet();
            newOwners.put(newOwner, acquiredAttributes);
          }
          acquiredAttributes.add(attributeHandle);
        }
      }

      // notify the new owners
      //
      for (Map.Entry<FederateProxy, AttributeHandleSet> entry : newOwners.entrySet())
      {
        entry.getKey().getFederateChannel().write(
          new AttributeOwnershipAcquisitionNotification(objectInstanceHandle, entry.getValue(), null));
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
      AttributeHandleSet acquiredAttributeHandles = new IEEE1516eAttributeHandleSet();
      Map<FederateProxy, AttributeHandleSet> federatesThatNeedToConfirmDivestiture =
        new HashMap<FederateProxy, AttributeHandleSet>();
      Map<FederateProxy, AttributeHandleSet> federatesThatNeedToRelease =
        new HashMap<FederateProxy, AttributeHandleSet>();

      for (AttributeHandle attributeHandle : attributeHandles)
      {
        FederationExecutionAttributeInstance federationExecutionAttributeInstance = attributes.get(attributeHandle);

        FederateProxy owner = federationExecutionAttributeInstance.attributeOwnershipAcquisition(acquiree);
        if (acquiree.equals(owner))
        {
          // the attribute was unowned and therefore immediately acquired
          //
          acquiredAttributeHandles.add(attributeHandle);
        }
        else if (federationExecutionAttributeInstance.wantsToDivest())
        {
          // the attribute is owned but the owner is willing to divest
          //
          AttributeHandleSet divestingAttributeHandles = federatesThatNeedToConfirmDivestiture.get(owner);
          if (divestingAttributeHandles == null)
          {
            divestingAttributeHandles = new IEEE1516eAttributeHandleSet();
            federatesThatNeedToConfirmDivestiture.put(owner, divestingAttributeHandles);
          }
          divestingAttributeHandles.add(attributeHandle);
        }
        else
        {
          // the attribute is owned but the owner is unwilling to divest
          //
          AttributeHandleSet releasingAttributeHandles = federatesThatNeedToRelease.get(owner);
          if (releasingAttributeHandles == null)
          {
            releasingAttributeHandles = new IEEE1516eAttributeHandleSet();
            federatesThatNeedToRelease.put(owner, releasingAttributeHandles);
          }
          releasingAttributeHandles.add(attributeHandle);
        }
      }

      if (!acquiredAttributeHandles.isEmpty())
      {
        acquiree.getFederateChannel().write(
          new AttributeOwnershipAcquisitionNotification(objectInstanceHandle, acquiredAttributeHandles, tag));
      }

      for (Map.Entry<FederateProxy, AttributeHandleSet> entry : federatesThatNeedToConfirmDivestiture.entrySet())
      {
        entry.getKey().getFederateChannel().write(new RequestDivestitureConfirmation(
          objectInstanceHandle, entry.getValue()));
      }

      for (Map.Entry<FederateProxy, AttributeHandleSet> entry : federatesThatNeedToRelease.entrySet())
      {
        entry.getKey().getFederateChannel().write(new RequestAttributeOwnershipRelease(
          objectInstanceHandle, entry.getValue(), tag));
      }
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisitionIfAvailable(FederateProxy acquiree, AttributeHandleSet attributeHandles)
  {
    objectLock.readLock().lock();
    try
    {
      AttributeHandleSet acquiredAttributeHandles = new IEEE1516eAttributeHandleSet();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        if (attributes.get(attributeHandle).attributeOwnershipAcquisitionIfAvailable(acquiree))
        {
          acquiredAttributeHandles.add(attributeHandle);
        }
      }

      if (!acquiredAttributeHandles.isEmpty())
      {
        acquiree.getFederateChannel().write(
          new AttributeOwnershipAcquisitionNotification(objectInstanceHandle, acquiredAttributeHandles, null));
      }

      AttributeHandleSet unacquiredAttributeHandles = new IEEE1516eAttributeHandleSet(attributeHandles);
      unacquiredAttributeHandles.removeAll(acquiredAttributeHandles);

      if (!unacquiredAttributeHandles.isEmpty())
      {
        acquiree.getFederateChannel().write(new AttributeOwnershipUnavailable(
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
      Map<AttributeHandle, FederateProxy> newOwners = new HashMap<AttributeHandle, FederateProxy>();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        FederateProxy newOwner = attributes.get(attributeHandle).attributeOwnershipDivestitureIfWanted();
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

  public void cancelNegotiatedAttributeOwnershipDivestiture(FederateProxy owner, AttributeHandleSet attributeHandles)
  {
    objectLock.readLock().lock();
    try
    {
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        attributes.get(attributeHandle).cancelNegotiatedAttributeOwnershipDivestiture(owner);
      }
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void cancelAttributeOwnershipAcquisition(FederateProxy acquiree, AttributeHandleSet attributeHandles)
  {
    objectLock.readLock().lock();
    try
    {
      AttributeHandleSet canceledAcquisitionAttributeHandles = new IEEE1516eAttributeHandleSet();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        if (attributes.get(attributeHandle).cancelAttributeOwnershipAcquisition(acquiree))
        {
          canceledAcquisitionAttributeHandles.add(attributeHandle);
        }
      }

      if (!canceledAcquisitionAttributeHandles.isEmpty())
      {
        acquiree.getFederateChannel().write(new ConfirmAttributeOwnershipAcquisitionCancellation(
          objectInstanceHandle, canceledAcquisitionAttributeHandles));
      }
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void queryAttributeOwnership(FederateProxy federateProxy, AttributeHandle attributeHandle)
  {
    objectLock.readLock().lock();
    try
    {
      FederationExecutionAttributeInstance federationExecutionAttributeInstance = attributes.get(attributeHandle);
      assert federationExecutionAttributeInstance != null;

      FederateProxy owner = federationExecutionAttributeInstance.getOwner();
      if (owner == null)
      {
        federateProxy.getFederateChannel().write(new AttributeIsNotOwned(objectInstanceHandle, attributeHandle));
      }
//      else if (federationExecutionAttributeInstance.getAttribute().isMOM())
//      {
//        federateProxy.getFederateChannel().write(new AttributeIsOwnedByRTI(objectInstanceHandle, attributeHandle));
//      }
      else
      {
        federateProxy.getFederateChannel().write(new InformAttributeOwnership(
          objectInstanceHandle, attributeHandle, owner.getFederateHandle()));
      }
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void associateRegionsForUpdates(
    FederateProxy federateProxy, AssociateRegionsForUpdates associateRegionsForUpdates)
  {
    Map<RegionHandle, FederationExecutionRegion> regions =
      federateProxy.getFederationExecution().getRegionManager().getRegions();

    objectLock.writeLock().lock();
    try
    {
      for (AttributeRegionAssociation attributeRegionAssociation : associateRegionsForUpdates.getAttributesAndRegions())
      {
        for (AttributeHandle attributeHandle : attributeRegionAssociation.ahset)
        {
          FederationExecutionAttributeInstance attribute = attributes.get(attributeHandle);
          for (RegionHandle regionHandle : attributeRegionAssociation.rhset)
          {
            attribute.associateRegionForUpdate(federateProxy, regions.get(regionHandle));
          }
        }
      }

      federateProxy.getFederateChannel().write(new AssociateRegionsForUpdatesResponse(
        associateRegionsForUpdates.getId(), AssociateRegionsForUpdatesResponse.Response.SUCCESS));
    }
    finally
    {
      objectLock.writeLock().unlock();
    }
  }

  public void unassociateRegionsForUpdates(
    FederateProxy federateProxy, UnassociateRegionsForUpdates unassociateRegionsForUpdates)
  {
    objectLock.writeLock().lock();
    try
    {
      for (AttributeRegionAssociation attributeRegionAssociation :
        unassociateRegionsForUpdates.getAttributesAndRegions())
      {
        for (AttributeHandle attributeHandle : attributeRegionAssociation.ahset)
        {
          attributes.get(attributeHandle).unassociateRegionsForUpdates(
            federateProxy, attributeRegionAssociation.rhset);
        }
      }

      federateProxy.getFederateChannel().write(new UnassociateRegionsForUpdatesResponse(
        unassociateRegionsForUpdates.getId(), UnassociateRegionsForUpdatesResponse.Response.SUCCESS));
    }
    finally
    {
      objectLock.writeLock().unlock();
    }
  }

  public boolean regionsIntersect(
    AttributeHandle attributeHandle, FederationExecutionRegionManager regionManager, Set<RegionHandle> regionHandles,
    Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regions)
  {
    FederationExecutionAttributeInstance attributeInstance = attributes.get(attributeHandle);
    return attributeInstance != null && attributeInstance.regionsIntersect(regionManager, regionHandles, regions);
  }

  public boolean regionsIntersect(
    AttributeHandle attributeHandle, FederationExecutionRegionManager regionManager, Set<RegionHandle> regionHandles)
  {
    FederationExecutionAttributeInstance attributeInstance = attributes.get(attributeHandle);
    return attributeInstance != null && attributeInstance.regionsIntersect(regionManager, regionHandles);
  }
}
