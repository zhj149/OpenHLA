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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.util.FederateHandles;
import net.sf.ohla.rti.util.ObjectClassHandles;
import net.sf.ohla.rti.util.ObjectInstanceHandles;
import net.sf.ohla.rti.fdd.Attribute;
import net.sf.ohla.rti.fdd.FDD;
import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleSet;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleSetFactory;
import net.sf.ohla.rti.messages.AssociateRegionsForUpdates;
import net.sf.ohla.rti.messages.AssociateRegionsForUpdatesResponse;
import net.sf.ohla.rti.messages.DeleteObjectInstance;
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
import net.sf.ohla.rti.proto.FederationExecutionSaveProtos.FederationExecutionState.FederationExecutionObjectManagerState.FederationExecutionObjectInstanceState;

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
{
  private final ObjectInstanceHandle objectInstanceHandle;
  private final ObjectClass objectClass;
  private final String objectInstanceName;

  private final FederateHandle producingFederateHandle;

  private final ReadWriteLock objectLock = new ReentrantReadWriteLock(true);

  private final Map<AttributeHandle, FederationExecutionAttributeInstance> attributes = new HashMap<>();

  private DeleteObjectInstance deleteObjectInstance;

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

  public FederationExecutionObjectInstance(
    FederationExecutionObjectInstanceState objectInstanceState, FederationExecution federationExecution)
  {
    objectInstanceHandle = ObjectInstanceHandles.convert(objectInstanceState.getObjectInstanceHandle());
    objectClass = federationExecution.getFDD().getObjectClassSafely(
      ObjectClassHandles.convert(objectInstanceState.getObjectClassHandle()));
    objectInstanceName = objectInstanceState.getObjectInstanceName();
    producingFederateHandle = FederateHandles.convert(objectInstanceState.getProducingFederateHandle());

    for (FederationExecutionObjectInstanceState.FederationExecutionAttributeInstanceState attributeInstanceState : objectInstanceState.getAttributeInstanceStatesList())
    {
      FederationExecutionAttributeInstance federationExecutionAttributeInstance =
        new FederationExecutionAttributeInstance(attributeInstanceState, objectClass, federationExecution);
      attributes.put(federationExecutionAttributeInstance.getAttributeHandle(), federationExecutionAttributeInstance);
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

  public boolean isScheduledForDeletion()
  {
    return deleteObjectInstance != null;
  }

  public DeleteObjectInstance getDeleteObjectInstance()
  {
    return deleteObjectInstance;
  }

  public void setDeleteObjectInstance(DeleteObjectInstance deleteObjectInstance)
  {
    this.deleteObjectInstance = deleteObjectInstance;
  }

  public FederateHandle getOwner(AttributeHandle attributeHandle)
  {
    return attributes.get(attributeHandle).getOwner();
  }

  public void updateAttributeValues(FederateProxy producingFederateProxy, UpdateAttributeValues updateAttributeValues)
  {
    objectLock.readLock().lock();
    try
    {
      for (FederateProxy federateProxy : producingFederateProxy.getFederationExecution().getFederates().values())
      {
        if (federateProxy != producingFederateProxy)
        {
          federateProxy.reflectAttributeValues(producingFederateProxy.getFederateHandle(), updateAttributeValues, this);
        }
      }
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void requestObjectInstanceAttributeValueUpdate(
    FederateProxy federateProxy, RequestObjectInstanceAttributeValueUpdate requestObjectInstanceAttributeValueUpdate)
  {
    objectLock.readLock().lock();
    try
    {
      Map<FederateHandle, AttributeHandleSet> provideAttributeValueUpdate = new HashMap<>();

      for (AttributeHandle attributeHandle : requestObjectInstanceAttributeValueUpdate.getAttributeHandles())
      {
        FederationExecutionAttributeInstance attributeInstance = attributes.get(attributeHandle);

        FederateHandle owner = attributeInstance.getOwner();
        if (owner != null && !federateProxy.getFederateHandle().equals(owner))
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

      for (Map.Entry<FederateHandle, AttributeHandleSet> entry : provideAttributeValueUpdate.entrySet())
      {
        federateProxy.getFederationExecution().getFederate(
          entry.getKey()).provideAttributeValueUpdate(new ProvideAttributeValueUpdate(
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
      Map<FederateHandle, AttributeHandleSet> provideAttributeValueUpdate = new HashMap<>();

      for (AttributeHandle attributeHandle : requestObjectClassAttributeValueUpdate.getAttributeHandles())
      {
        FederationExecutionAttributeInstance attributeInstance = attributes.get(attributeHandle);

        FederateHandle owner = attributeInstance.getOwner();
        if (owner != null && !federateProxy.getFederateHandle().equals(owner))
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

      for (Map.Entry<FederateHandle, AttributeHandleSet> entry : provideAttributeValueUpdate.entrySet())
      {
        federateProxy.getFederationExecution().getFederate(
          entry.getKey()).provideAttributeValueUpdate(new ProvideAttributeValueUpdate(
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
      Map<FederateHandle, AttributeHandleSet> provideAttributeValueUpdate = new HashMap<>();

      for (AttributeRegionAssociation attributeRegionAssociation :
        requestObjectClassAttributeValueUpdateWithRegions.getAttributesAndRegions())
      {
        for (AttributeHandle attributeHandle : attributeRegionAssociation.ahset)
        {
          FederationExecutionAttributeInstance attributeInstance = attributes.get(attributeHandle);

          FederateHandle owner = attributeInstance.getOwner();
          if (owner != null && !federateProxy.getFederateHandle().equals(owner))
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

      for (Map.Entry<FederateHandle, AttributeHandleSet> entry : provideAttributeValueUpdate.entrySet())
      {
        federateProxy.getFederationExecution().getFederate(
          entry.getKey()).provideAttributeValueUpdate(new ProvideAttributeValueUpdate(
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
            ownershipCandidates = new HashMap<>();
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

  public void unpublishObjectClass(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    objectLock.writeLock().lock();
    try
    {
      Map<FederateHandle, AttributeHandleSetTagPair> newOwners = new HashMap<>();
      for (FederationExecutionAttributeInstance attributeInstance : attributes.values())
      {
        if (federateProxy.getFederateHandle().equals(attributeInstance.getOwner()))
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
      for (Map.Entry<FederateHandle, AttributeHandleSetTagPair> entry : newOwners.entrySet())
      {
        federateProxy.getFederationExecution().getFederate(
          entry.getKey()).getFederateChannel().write(new AttributeOwnershipAcquisitionNotification(
          objectInstanceHandle, entry.getValue().attributeHandles, entry.getValue().tag));
      }
    }
    finally
    {
      objectLock.writeLock().unlock();
    }
  }

  public void unpublishObjectClassAttributes(FederationExecution federationExecution,
                                             FederateProxy federateProxy, AttributeHandleSet attributeHandles)
  {
    objectLock.writeLock().lock();
    try
    {
      Map<FederateHandle, AttributeHandleSetTagPair> newOwners = new HashMap<>();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        FederationExecutionAttributeInstance attributeInstance = attributes.get(attributeHandle);
        if (federateProxy.getFederateHandle().equals(attributeInstance.getOwner()))
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
            acquiredAttributes.attributeHandles.add(attributeHandle);
          }
        }
      }

      // notify the new owners
      //
      for (Map.Entry<FederateHandle, AttributeHandleSetTagPair> entry : newOwners.entrySet())
      {
        federateProxy.getFederationExecution().getFederate(
          entry.getKey()).getFederateChannel().write(new AttributeOwnershipAcquisitionNotification(
          objectInstanceHandle, entry.getValue().attributeHandles, entry.getValue().tag));
      }
    }
    finally
    {
      objectLock.writeLock().unlock();
    }
  }

  public void unconditionalAttributeOwnershipDivestiture(
    FederateProxy federateProxy, AttributeHandleSet attributeHandles)
  {
    objectLock.writeLock().lock();
    try
    {
      Map<FederateHandle, AttributeHandleSetTagPair> newOwners = new HashMap<>();
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
      for (Map.Entry<FederateHandle, AttributeHandleSetTagPair> entry : newOwners.entrySet())
      {
        federateProxy.getFederationExecution().getFederate(
          entry.getKey()).getFederateChannel().write(new AttributeOwnershipAcquisitionNotification(
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

  public void confirmDivestiture(FederateProxy federateProxy, AttributeHandleSet attributeHandles)
  {
    objectLock.writeLock().lock();
    try
    {
      Map<FederateHandle, AttributeHandleSetTagPair> newOwners = new HashMap<>();
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
      for (Map.Entry<FederateHandle, AttributeHandleSetTagPair> entry : newOwners.entrySet())
      {
        federateProxy.getFederationExecution().getFederate(
          entry.getKey()).getFederateChannel().write(new AttributeOwnershipAcquisitionNotification(
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
      Map<FederateHandle, AttributeHandleSet> federatesThatNeedToConfirmDivestiture = new HashMap<>();
      Map<FederateHandle, AttributeHandleSet> federatesThatNeedToRelease = new HashMap<>();

      for (AttributeHandle attributeHandle : attributeHandles)
      {
        FederationExecutionAttributeInstance federationExecutionAttributeInstance = attributes.get(attributeHandle);

        FederateHandle owner = federationExecutionAttributeInstance.attributeOwnershipAcquisition(acquiree);
        if (acquiree.getFederateHandle().equals(owner))
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

      for (Map.Entry<FederateHandle, AttributeHandleSet> entry : federatesThatNeedToConfirmDivestiture.entrySet())
      {
        acquiree.getFederationExecution().getFederate(
          entry.getKey()).getFederateChannel().write(new RequestDivestitureConfirmation(
          objectInstanceHandle, entry.getValue()));
      }

      for (Map.Entry<FederateHandle, AttributeHandleSet> entry : federatesThatNeedToRelease.entrySet())
      {
        acquiree.getFederationExecution().getFederate(
          entry.getKey()).getFederateChannel().write(new RequestAttributeOwnershipRelease(
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
    FederationExecution federationExecution, FederateProxy owner, AttributeHandleSet attributeHandles)
  {
    Map<AttributeHandle, FederationExecutionAttributeInstance.Divestiture> divestitures = null;
    objectLock.writeLock().lock();
    try
    {
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        FederationExecutionAttributeInstance.Divestiture divestiture =
          attributes.get(attributeHandle).attributeOwnershipDivestitureIfWanted(federationExecution);
        if (divestiture != null)
        {
          if (divestitures == null)
          {
            divestitures = new HashMap<>();
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

      FederateHandle owner = federationExecutionAttributeInstance.getOwner();
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
          objectInstanceHandle, attributeHandle, owner));
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
        associateRegionsForUpdates.getRequestId()));
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
        unassociateRegionsForUpdates.getRequestId()));
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
        getUpdateRateValueForAttribute.getRequestId(),
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

    Map<FederateHandle, AttributeHandleSetTagPair> newOwners = new HashMap<>();
    for (FederationExecutionAttributeInstance attributeInstance : attributes.values())
    {
      if (federateProxy.getFederateHandle().equals(attributeInstance.getOwner()))
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
    for (Map.Entry<FederateHandle, AttributeHandleSetTagPair> entry : newOwners.entrySet())
    {
      federateProxy.getFederationExecution().getFederate(
        entry.getKey()).getFederateChannel().write(new AttributeOwnershipAcquisitionNotification(
        objectInstanceHandle, entry.getValue().attributeHandles, entry.getValue().tag));
    }
  }

  public boolean isOwner(FederateProxy federateProxy)
  {
    // dangerous method, must be called with proper protection

    FederationExecutionAttributeInstance privilegeToDeleteObject =
      attributes.get(objectClass.getAttributeSafely(FDD.HLA_PRIVILEGE_TO_DELETE_OBJECT).getAttributeHandle());
    return federateProxy.getFederateHandle().equals(privilegeToDeleteObject.getOwner());
  }

  public void cancelPendingOwnershipAcquisitions(FederateProxy federateProxy)
  {
    // dangerous method, must be called with proper protection

    for (FederationExecutionAttributeInstance attributeInstance : attributes.values())
    {
      attributeInstance.cancelAttributeOwnershipAcquisition(federateProxy);
    }
  }

  public FederationExecutionObjectInstanceState.Builder saveState()
  {
    FederationExecutionObjectInstanceState.Builder objectInstanceState = FederationExecutionObjectInstanceState.newBuilder();

    objectInstanceState.setObjectInstanceHandle(ObjectInstanceHandles.convert(objectInstanceHandle));
    objectInstanceState.setObjectClassHandle(ObjectClassHandles.convert(objectClass.getObjectClassHandle()));
    objectInstanceState.setObjectInstanceName(objectInstanceName);
    objectInstanceState.setProducingFederateHandle(FederateHandles.convert(producingFederateHandle));

    for (FederationExecutionAttributeInstance federationExecutionAttributeInstance : attributes.values())
    {
      objectInstanceState.addAttributeInstanceStates(federationExecutionAttributeInstance.saveState());
    }

    return objectInstanceState;
  }
}
