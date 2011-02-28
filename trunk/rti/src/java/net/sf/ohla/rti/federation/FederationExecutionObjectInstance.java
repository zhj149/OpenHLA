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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.AttributeHandleSetTagPair;
import net.sf.ohla.rti.fdd.Attribute;
import net.sf.ohla.rti.fdd.FDD;
import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleSet;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleSetFactory;
import net.sf.ohla.rti.messages.AssociateRegionsForUpdates;
import net.sf.ohla.rti.messages.AssociateRegionsForUpdatesResponse;
import net.sf.ohla.rti.messages.GetUpdateRateValueForAttribute;
import net.sf.ohla.rti.messages.GetUpdateRateValueForAttributeResponse;
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
import hla.rti1516e.AttributeSetRegionSetPairList;
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
    String objectInstanceName, Set<AttributeHandle> publishedAttributeHandles,
    AttributeSetRegionSetPairList attributesAndRegions, FederateProxy owner)
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

    // TODO: check rules about implicit publishing

    Attribute privilegeToDeleteObjectAttribute =
      objectClass.getAttributeSafely(FDD.HLA_PRIVILEGE_TO_DELETE_OBJECT);
    attributes.get(privilegeToDeleteObjectAttribute.getAttributeHandle()).setOwner(owner);

    if (attributesAndRegions != null)
    {
      Map<RegionHandle, FederationExecutionRegion> regions =
        owner.getFederationExecution().getRegionManager().getRegions();
      for (AttributeRegionAssociation attributeRegionAssociation : attributesAndRegions)
      {
        for (AttributeHandle attributeHandle : attributeRegionAssociation.ahset)
        {
          FederationExecutionAttributeInstance attribute = attributes.get(attributeHandle);
          for (RegionHandle regionHandle : attributeRegionAssociation.rhset)
          {
            attribute.associateRegionForUpdate(owner, regions.get(regionHandle));
          }
        }
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

  public void publishObjectClassAttributes(FederateProxy federateProxy, AttributeHandleSet attributeHandles)
  {
    objectLock.readLock().lock();
    try
    {
      Map<byte[], AttributeHandleSet> ownershipCandidates = null;

      for (AttributeHandle attributeHandle : attributeHandles)
      {
        FederationExecutionAttributeInstance attributeInstance = attributes.get(attributeHandle);
        if (attributeInstance.wantsToDivest() || attributeInstance.isUnowned())
        {
          AttributeHandleSet candidateAttributeHandles;
          if (ownershipCandidates == null)
          {
            ownershipCandidates = new HashMap<byte[], AttributeHandleSet>();
            candidateAttributeHandles = IEEE1516eAttributeHandleSetFactory.INSTANCE.create();
            ownershipCandidates.put(attributeInstance.getDivestingTag(), attributeHandles);
          }
          else
          {
            candidateAttributeHandles = ownershipCandidates.get(attributeInstance.getDivestingTag());
          }
          candidateAttributeHandles.add(attributeHandle);
        }
      }

      if (ownershipCandidates != null)
      {
        for (Map.Entry<byte[], AttributeHandleSet> entry : ownershipCandidates.entrySet())
        {
          federateProxy.requestAttributeOwnershipAssumption(objectInstanceHandle, entry.getValue(), entry.getKey());
        }
      }
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void unconditionalAttributeOwnershipDivestiture(FederateProxy owner, AttributeHandleSet attributeHandles)
  {
    objectLock.writeLock().lock();
    try
    {
      Map<FederateProxy, AttributeHandleSetTagPair> newOwners = new HashMap<FederateProxy, AttributeHandleSetTagPair>();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        FederationExecutionAttributeInstance.Divestiture divestiture =
          attributes.get(attributeHandle).unconditionalAttributeOwnershipDivestiture();
        if (divestiture != null)
        {
          AttributeHandleSetTagPair acquiredAttributes = newOwners.get(divestiture.newOwner);
          if (acquiredAttributes == null)
          {
            acquiredAttributes = new AttributeHandleSetTagPair(divestiture.tag);
            newOwners.put(divestiture.newOwner, acquiredAttributes);
          }
          acquiredAttributes.attributeHandles.add(attributeHandle);
        }
      }

      // notify the new owners
      //
      for (Map.Entry<FederateProxy, AttributeHandleSetTagPair> entry : newOwners.entrySet())
      {
        entry.getKey().getFederateChannel().write(new AttributeOwnershipAcquisitionNotification(
          objectInstanceHandle, entry.getValue().attributeHandles, entry.getValue().tag));
      }
    }
    finally
    {
      objectLock.writeLock().unlock();
    }
  }

  public void negotiatedAttributeOwnershipDivestiture(
    FederateProxy owner, AttributeHandleSet attributeHandles, byte[] tag)
  {
    objectLock.writeLock().lock();
    try
    {
      AttributeHandleSet attributeHandlesThatCanBeImmediatelyDivested = null;

      for (Iterator<AttributeHandle> i = attributeHandles.iterator(); i.hasNext();)
      {
        AttributeHandle attributeHandle = i.next();
        if (attributes.get(attributeHandle).negotiatedAttributeOwnershipDivestiture(tag))
        {
          i.remove();

          if (attributeHandlesThatCanBeImmediatelyDivested == null)
          {
            attributeHandlesThatCanBeImmediatelyDivested = IEEE1516eAttributeHandleSetFactory.INSTANCE.create();
          }
          attributeHandlesThatCanBeImmediatelyDivested.add(attributeHandle);
        }
      }

      // notify the divesting federate of the attributes that can be divested
      //
      if (attributeHandlesThatCanBeImmediatelyDivested != null)
      {
        owner.getFederateChannel().write(new RequestDivestitureConfirmation(
          objectInstanceHandle, attributeHandlesThatCanBeImmediatelyDivested));
      }

      // notify other federates that some attributes are available for acquisition
      //
      for (FederateProxy federateProxy : owner.getFederationExecution().getFederates().values())
      {
        if (federateProxy != owner)
        {
          federateProxy.negotiatedAttributeOwnershipDivestiture(this, attributeHandles, tag);
        }
      }
    }
    finally
    {
      objectLock.writeLock().unlock();
    }
  }

  public void confirmDivestiture(FederateProxy owner, AttributeHandleSet attributeHandles)
  {
    objectLock.writeLock().lock();
    try
    {
      Map<FederateProxy, AttributeHandleSetTagPair> newOwners = new HashMap<FederateProxy, AttributeHandleSetTagPair>();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        FederationExecutionAttributeInstance.Divestiture divestiture =
          attributes.get(attributeHandle).confirmDivestiture();
        if (divestiture != null)
        {
          AttributeHandleSetTagPair acquiredAttributes = newOwners.get(divestiture.newOwner);
          if (acquiredAttributes == null)
          {
            acquiredAttributes = new AttributeHandleSetTagPair(divestiture.tag);
            newOwners.put(divestiture.newOwner, acquiredAttributes);
          }
          acquiredAttributes.attributeHandles.add(attributeHandle);
        }
      }

      // notify the new owners
      //
      for (Map.Entry<FederateProxy, AttributeHandleSetTagPair> entry : newOwners.entrySet())
      {
        entry.getKey().getFederateChannel().write(new AttributeOwnershipAcquisitionNotification(
          objectInstanceHandle, entry.getValue().attributeHandles, entry.getValue().tag));
      }
    }
    finally
    {
      objectLock.writeLock().unlock();
    }
  }

  public void attributeOwnershipAcquisition(
    FederateProxy acquiree, AttributeHandleSet attributeHandles, byte[] tag)
  {
    objectLock.writeLock().lock();
    try
    {
      AttributeHandleSet acquiredAttributeHandles = IEEE1516eAttributeHandleSetFactory.INSTANCE.create();
      Map<FederateProxy, AttributeHandleSet> federatesThatNeedToConfirmDivestiture =
        new HashMap<FederateProxy, AttributeHandleSet>();
      Map<FederateProxy, AttributeHandleSet> federatesThatNeedToRelease =
        new HashMap<FederateProxy, AttributeHandleSet>();

      for (AttributeHandle attributeHandle : attributeHandles)
      {
        FederationExecutionAttributeInstance federationExecutionAttributeInstance = attributes.get(attributeHandle);

        FederateProxy owner = federationExecutionAttributeInstance.attributeOwnershipAcquisition(acquiree);
        if (acquiree == owner)
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
            divestingAttributeHandles = IEEE1516eAttributeHandleSetFactory.INSTANCE.create();
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
            releasingAttributeHandles = IEEE1516eAttributeHandleSetFactory.INSTANCE.create();
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
      objectLock.writeLock().unlock();
    }
  }

  public void attributeOwnershipAcquisitionIfAvailable(FederateProxy acquiree, AttributeHandleSet attributeHandles)
  {
    objectLock.writeLock().lock();
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
      objectLock.writeLock().unlock();
    }
  }

  public Map<AttributeHandle, FederationExecutionAttributeInstance.Divestiture> attributeOwnershipDivestitureIfWanted(
    FederateProxy owner, AttributeHandleSet attributeHandles)
  {
    Map<AttributeHandle, FederationExecutionAttributeInstance.Divestiture> divestitures = null;
    objectLock.writeLock().lock();
    try
    {
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        FederationExecutionAttributeInstance.Divestiture divestiture =
          attributes.get(attributeHandle).attributeOwnershipDivestitureIfWanted();
        if (divestiture != null)
        {
          if (divestitures == null)
          {
            divestitures = new HashMap<AttributeHandle, FederationExecutionAttributeInstance.Divestiture>();
          }
          divestitures.put(attributeHandle, divestiture);
        }
      }
    }
    finally
    {
      objectLock.writeLock().unlock();
    }
    return divestitures == null ?
      Collections.<AttributeHandle, FederationExecutionAttributeInstance.Divestiture>emptyMap() : divestitures;
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture(FederateProxy owner, AttributeHandleSet attributeHandles)
  {
    objectLock.writeLock().lock();
    try
    {
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        attributes.get(attributeHandle).cancelNegotiatedAttributeOwnershipDivestiture(owner);
      }
    }
    finally
    {
      objectLock.writeLock().unlock();
    }
  }

  public void cancelAttributeOwnershipAcquisition(FederateProxy acquiree, AttributeHandleSet attributeHandles)
  {
    objectLock.writeLock().lock();
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
      objectLock.writeLock().unlock();
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

  public void getUpdateRateValueForAttribute(
    FederateProxy federateProxy, GetUpdateRateValueForAttribute getUpdateRateValueForAttribute)
  {
    objectLock.readLock().lock();
    try
    {
      federateProxy.getFederateChannel().write(new GetUpdateRateValueForAttributeResponse(
        getUpdateRateValueForAttribute.getId(),
        attributes.get(getUpdateRateValueForAttribute.getAttributeHandle()).getUpdateRate()));
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public boolean regionsIntersect(
    AttributeHandle attributeHandle, FederationExecutionRegionManager regionManager, Set<RegionHandle> regionHandles,
    Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regions)
  {
    // dangerous method, must be called with proper protection

    return attributes.get(attributeHandle).regionsIntersect(regionManager, regionHandles, regions);
  }

  public boolean regionsIntersect(
    AttributeHandle attributeHandle, FederationExecutionRegionManager regionManager, Set<RegionHandle> regionHandles)
  {
    // dangerous method, must be called with proper protection

    return attributes.get(attributeHandle).regionsIntersect(regionManager, regionHandles);
  }

  public void unconditionallyDivestAttributes(FederateProxy federateProxy)
  {
    // dangerous method, must be called with proper protection

    Map<FederateProxy, AttributeHandleSetTagPair> newOwners = new HashMap<FederateProxy, AttributeHandleSetTagPair>();
    for (FederationExecutionAttributeInstance attributeInstance : attributes.values())
    {
      if (attributeInstance.getOwner() == federateProxy)
      {
        FederationExecutionAttributeInstance.Divestiture divestiture =
          attributeInstance.unconditionalAttributeOwnershipDivestiture();
        if (divestiture != null)
        {
          AttributeHandleSetTagPair acquiredAttributes = newOwners.get(divestiture.newOwner);
          if (acquiredAttributes == null)
          {
            acquiredAttributes = new AttributeHandleSetTagPair(divestiture.tag);
            newOwners.put(divestiture.newOwner, acquiredAttributes);
          }
          acquiredAttributes.attributeHandles.add(attributeInstance.getAttributeHandle());
        }
      }
    }

    // notify the new owners
    //
    for (Map.Entry<FederateProxy, AttributeHandleSetTagPair> entry : newOwners.entrySet())
    {
      entry.getKey().getFederateChannel().write(new AttributeOwnershipAcquisitionNotification(
        objectInstanceHandle, entry.getValue().attributeHandles, entry.getValue().tag));
    }
  }

  public boolean isOwner(FederateProxy federateProxy)
  {
    // dangerous method, must be called with proper protection

    FederationExecutionAttributeInstance privilegeToDeleteObject =
      attributes.get(objectClass.getAttributeSafely(FDD.HLA_PRIVILEGE_TO_DELETE_OBJECT).getAttributeHandle());
    return privilegeToDeleteObject.getOwner() == federateProxy;
  }

  public void cancelPendingOwnershipAcquisitions(FederateProxy federateProxy)
  {
    // dangerous method, must be called with proper protection

    for (FederationExecutionAttributeInstance attributeInstance : attributes.values())
    {
      attributeInstance.cancelAttributeOwnershipAcquisition(federateProxy);
    }
  }
}
