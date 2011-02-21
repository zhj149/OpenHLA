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

package net.sf.ohla.rti.federate;

import java.io.Serializable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.fdd.Attribute;
import net.sf.ohla.rti.fdd.FDD;
import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.fdd.TransportationType;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleSet;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleSetFactory;
import net.sf.ohla.rti.messages.AssociateRegionsForUpdates;
import net.sf.ohla.rti.messages.AssociateRegionsForUpdatesResponse;
import net.sf.ohla.rti.messages.AttributeOwnershipAcquisition;
import net.sf.ohla.rti.messages.AttributeOwnershipAcquisitionIfAvailable;
import net.sf.ohla.rti.messages.AttributeOwnershipDivestitureIfWanted;
import net.sf.ohla.rti.messages.CancelAttributeOwnershipAcquisition;
import net.sf.ohla.rti.messages.CancelNegotiatedAttributeOwnershipDivestiture;
import net.sf.ohla.rti.messages.ConfirmDivestiture;
import net.sf.ohla.rti.messages.NegotiatedAttributeOwnershipDivestiture;
import net.sf.ohla.rti.messages.QueryAttributeOwnership;
import net.sf.ohla.rti.messages.RequestObjectInstanceAttributeValueUpdate;
import net.sf.ohla.rti.messages.UnassociateRegionsForUpdates;
import net.sf.ohla.rti.messages.UnconditionalAttributeOwnershipDivestiture;
import net.sf.ohla.rti.messages.UpdateAttributeValues;
import net.sf.ohla.rti.messages.callbacks.ReflectAttributeValues;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.AttributeRegionAssociation;
import hla.rti1516e.AttributeSetRegionSetPairList;
import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.RegionHandle;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.AttributeAcquisitionWasNotRequested;
import hla.rti1516e.exceptions.AttributeAlreadyBeingAcquired;
import hla.rti1516e.exceptions.AttributeAlreadyBeingChanged;
import hla.rti1516e.exceptions.AttributeAlreadyBeingDivested;
import hla.rti1516e.exceptions.AttributeAlreadyOwned;
import hla.rti1516e.exceptions.AttributeDivestitureWasNotRequested;
import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.AttributeNotOwned;
import hla.rti1516e.exceptions.DeletePrivilegeNotHeld;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.FederateOwnsAttributes;
import hla.rti1516e.exceptions.ObjectInstanceNotKnown;
import hla.rti1516e.exceptions.OwnershipAcquisitionPending;
import hla.rti1516e.exceptions.RTIinternalError;

public class FederateObjectInstance
  implements Serializable
{
  private final FederateHandle producingFederateHandle;
  private final ObjectInstanceHandle objectInstanceHandle;
  private final ObjectClass objectClass;
  private final String objectInstanceName;

  private final Map<AttributeHandle, FederateAttributeInstance> attributes =
    new HashMap<AttributeHandle, FederateAttributeInstance>();

  private final Set<AttributeHandle> attributeHandlesBeingAcquired =
    new HashSet<AttributeHandle>();
  private final Set<AttributeHandle> attributeHandlesBeingAcquiredIfAvailable =
    new HashSet<AttributeHandle>();

  private final ReadWriteLock objectLock = new ReentrantReadWriteLock(true);

  public FederateObjectInstance(
    FederateHandle producingFederateHandle, ObjectInstanceHandle objectInstanceHandle, ObjectClass objectClass,
    String objectInstanceName)
  {
    this.producingFederateHandle = producingFederateHandle;
    this.objectInstanceHandle = objectInstanceHandle;
    this.objectClass = objectClass;
    this.objectInstanceName = objectInstanceName;
  }

  public FederateObjectInstance(
    Federate federate, ObjectInstanceHandle objectInstanceHandle, ObjectClass objectClass,
    AttributeHandleSet publishedAttributeHandles)
  {
    this(federate, objectInstanceHandle, "HLA-" + objectInstanceHandle, objectClass, publishedAttributeHandles, null);
  }

  public FederateObjectInstance(
    Federate federate, ObjectInstanceHandle objectInstanceHandle, String objectInstanceName,
    ObjectClass objectClass, AttributeHandleSet publishedAttributeHandles)
  {
    this(federate, objectInstanceHandle, objectInstanceName, objectClass, publishedAttributeHandles, null);
  }

  public FederateObjectInstance(
    Federate federate, ObjectInstanceHandle objectInstanceHandle, ObjectClass objectClass,
    AttributeHandleSet publishedAttributeHandles, AttributeSetRegionSetPairList attributesAndRegions)
  {
    this(federate, objectInstanceHandle, "HLA-" + objectInstanceHandle, objectClass, publishedAttributeHandles,
         attributesAndRegions);
  }

  public FederateObjectInstance(
    Federate federate, ObjectInstanceHandle objectInstanceHandle, String objectInstanceName,
    ObjectClass objectClass, AttributeHandleSet publishedAttributeHandles,
    AttributeSetRegionSetPairList attributesAndRegions)
  {
    this(federate.getFederateHandle(), objectInstanceHandle, objectClass, objectInstanceName);

    for (AttributeHandle attributeHandle : publishedAttributeHandles)
    {
      Attribute attribute = objectClass.getAttributeSafely(attributeHandle);
      assert attribute != null;

      // create an attribute instance for each of the published attributes
      //
      attributes.put(attributeHandle, new FederateAttributeInstance(attribute));
    }

    // TODO: check rules about implicit publishing

    Attribute privilegeToDeleteObjectAttribute =
      objectClass.getAttributeSafely(FDD.HLA_PRIVILEGE_TO_DELETE_OBJECT);
    if (!attributes.containsKey(privilegeToDeleteObjectAttribute.getAttributeHandle()))
    {
      // create an attribute instance for the HLA privilege to delete object attribute
      //
      attributes.put(privilegeToDeleteObjectAttribute.getAttributeHandle(),
                     new FederateAttributeInstance(privilegeToDeleteObjectAttribute));
    }

    if (attributesAndRegions != null)
    {
      for (AttributeRegionAssociation attributeRegionAssociation : attributesAndRegions)
      {
        for (AttributeHandle attributeHandle : attributeRegionAssociation.ahset)
        {
          FederateAttributeInstance attribute = attributes.get(attributeHandle);
          if (attribute != null)
          {
            attribute.associateRegionsForUpdates(attributeRegionAssociation.rhset);
          }
        }

        for (RegionHandle regionHandle : attributeRegionAssociation.rhset)
        {
          FederateRegion region = federate.getRegionManager().getRegionSafely(regionHandle);

          region.associateRegionsForUpdates(objectInstanceHandle, attributeRegionAssociation.ahset);
        }
      }
    }
  }

  public FederateHandle getProducingFederateHandle()
  {
    return producingFederateHandle;
  }

  public ObjectInstanceHandle getObjectInstanceHandle()
  {
    return objectInstanceHandle;
  }

  public ObjectClassHandle getObjectClassHandle()
  {
    return objectClass.getObjectClassHandle();
  }

  public ObjectClass getObjectClass()
  {
    return objectClass;
  }

  public String getObjectInstanceName()
  {
    return objectInstanceName;
  }

  public void updateAttributeValues(AttributeHandleValueMap attributeValues, byte[] tag, Federate federate)
    throws AttributeNotDefined, AttributeNotOwned
  {
    objectLock.readLock().lock();
    try
    {
      checkIfAttributeNotOwned(attributeValues.keySet());

      federate.getRTIChannel().write(new UpdateAttributeValues(
        objectInstanceHandle, attributeValues, tag, TransportationType.HLA_RELIABLE.getTransportationTypeHandle()));
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void updateAttributeValues(
    AttributeHandleValueMap attributeValues, byte[] tag, LogicalTime updateTime,
    MessageRetractionHandle messageRetractionHandle, OrderType sentOrderType, Federate federate)
    throws AttributeNotDefined, AttributeNotOwned, RTIinternalError
  {
    objectLock.readLock().lock();
    try
    {
      if (sentOrderType == OrderType.TIMESTAMP)
      {
        // TODO: divide attributes by order type
      }

      federate.getRTIChannel().write(new UpdateAttributeValues(
        objectInstanceHandle, attributeValues, tag, sentOrderType,
        TransportationType.HLA_RELIABLE.getTransportationTypeHandle(), updateTime, messageRetractionHandle));
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void localDeleteObjectInstance()
    throws FederateOwnsAttributes
  {
    objectLock.readLock().lock();
    try
    {
      if (!attributes.isEmpty())
      {
        throw new FederateOwnsAttributes(String.format("%s - %s", objectInstanceHandle, attributes.keySet()));
      }
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void unconditionalAttributeOwnershipDivestiture(AttributeHandleSet attributeHandles, Federate federate)
    throws AttributeNotDefined, AttributeNotOwned, RTIinternalError
  {
    objectLock.writeLock().lock();
    try
    {
      checkIfAttributeNotOwned(attributeHandles);

      attributes.keySet().removeAll(attributeHandles);

      federate.getRTIChannel().write(new UnconditionalAttributeOwnershipDivestiture(
        objectInstanceHandle, attributeHandles));
    }
    finally
    {
      objectLock.writeLock().unlock();
    }
  }

  public void unconditionalAttributeOwnershipDivestitureSafely(AttributeHandleSet attributeHandles)
  {
    attributes.keySet().removeAll(attributeHandles);
  }

  public boolean ownsAny(AttributeHandleSet attributeHandles)
  {
    boolean ownsAny;
    if (attributes.isEmpty())
    {
      ownsAny = false;
    }
    else
    {
      Iterator<AttributeHandle> i = attributes.keySet().iterator();
      do
      {
        ownsAny = attributeHandles.contains(i.next());
      } while (!ownsAny && i.hasNext());
    }
    return ownsAny;
  }

  public void negotiatedAttributeOwnershipDivestiture(
    AttributeHandleSet attributeHandles, byte[] tag, Federate federate)
    throws AttributeAlreadyBeingDivested, AttributeNotOwned, AttributeNotDefined, RTIinternalError
  {
    objectLock.writeLock().lock();
    try
    {
      checkIfAttributeAlreadyBeingDivested(attributeHandles);

      for (AttributeHandle attributeHandle : attributeHandles)
      {
        getAttributeInstance(attributeHandle).negotiatedAttributeOwnershipDivestiture();
      }

      federate.getRTIChannel().write(
        new NegotiatedAttributeOwnershipDivestiture(objectInstanceHandle, attributeHandles, tag));
    }
    finally
    {
      objectLock.writeLock().unlock();
    }
  }

  public void confirmDivestiture(AttributeHandleSet attributeHandles, byte[] tag, Federate federate)
    throws AttributeNotDefined, AttributeNotOwned, AttributeDivestitureWasNotRequested, RTIinternalError
  {
    objectLock.writeLock().lock();
    try
    {
      checkIfAttributeDivestitureWasNotRequested(attributeHandles);

      for (AttributeHandle attributeHandle : attributeHandles)
      {
        getAttributeInstance(attributeHandle).confirmDivestiture();
        attributes.remove(attributeHandle);
      }

      federate.getRTIChannel().write(new ConfirmDivestiture(objectInstanceHandle, attributeHandles, tag));
    }
    finally
    {
      objectLock.writeLock().unlock();
    }
  }

  public void attributeOwnershipAcquisition(AttributeHandleSet attributeHandles, byte[] tag, Federate federate)
    throws FederateOwnsAttributes, RTIinternalError
  {
    objectLock.writeLock().lock();
    try
    {
      checkIfFederateOwnsAttributes(attributeHandles);

      attributeHandlesBeingAcquired.addAll(attributeHandles);

      federate.getRTIChannel().write(new AttributeOwnershipAcquisition(objectInstanceHandle, attributeHandles, tag));
    }
    finally
    {
      objectLock.writeLock().unlock();
    }
  }

  public void attributeOwnershipAcquisitionIfAvailable(AttributeHandleSet attributeHandles, Federate federate)
    throws AttributeNotDefined, FederateOwnsAttributes, AttributeAlreadyBeingAcquired, RTIinternalError
  {
    objectLock.writeLock().lock();
    try
    {
      Set<AttributeHandle> ownedAttributeHandles = new HashSet<AttributeHandle>();
      Set<AttributeHandle> attributeHandlesAlreadyBeingAcquired = new HashSet<AttributeHandle>();

      for (AttributeHandle attributeHandle : attributeHandles)
      {
        objectClass.checkIfAttributeNotDefined(attributeHandle);

        if (attributes.containsKey(attributeHandle))
        {
          ownedAttributeHandles.add(attributeHandle);
        }
        else if (attributeHandlesBeingAcquiredIfAvailable.contains(attributeHandle))
        {
          attributeHandlesAlreadyBeingAcquired.add(attributeHandle);
        }
      }

      if (!ownedAttributeHandles.isEmpty())
      {
        throw new FederateOwnsAttributes(String.format("%s - %s", objectInstanceHandle, ownedAttributeHandles));
      }
      else if (!attributeHandlesAlreadyBeingAcquired.isEmpty())
      {
        throw new AttributeAlreadyBeingAcquired(attributeHandlesAlreadyBeingAcquired.toString());
      }

      attributeHandlesBeingAcquiredIfAvailable.addAll(attributeHandles);

      federate.getRTIChannel().write(new AttributeOwnershipAcquisitionIfAvailable(
        objectInstanceHandle, attributeHandles));
    }
    finally
    {
      objectLock.writeLock().unlock();
    }
  }

  public void attributeOwnershipReleaseDenied(AttributeHandleSet attributeHandles, Federate federate)
    throws AttributeNotOwned, AttributeNotDefined, RTIinternalError
  {
  }

  public AttributeHandleSet attributeOwnershipDivestitureIfWanted(
    AttributeHandleSet attributeHandles, Federate federate)
    throws AttributeNotDefined, AttributeNotOwned, RTIinternalError
  {
    objectLock.writeLock().lock();
    try
    {
      checkIfAttributeNotOwned(attributeHandles);

      AttributeOwnershipDivestitureIfWanted attributeOwnershipDivestitureIfWanted =
        new AttributeOwnershipDivestitureIfWanted(objectInstanceHandle, attributeHandles);
      federate.getRTIChannel().write(attributeOwnershipDivestitureIfWanted);

      AttributeHandleSet divestedAttributeHandles =
        attributeOwnershipDivestitureIfWanted.getResponse().getAttributeHandles();

      if (divestedAttributeHandles == null)
      {
        divestedAttributeHandles = federate.getAttributeHandleSetFactory().create();
      }
      else
      {
        attributes.keySet().removeAll(divestedAttributeHandles);
      }

      return divestedAttributeHandles;
    }
    finally
    {
      objectLock.writeLock().unlock();
    }
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture(AttributeHandleSet attributeHandles, Federate federate)
    throws AttributeNotDefined, AttributeNotOwned, AttributeDivestitureWasNotRequested, RTIinternalError
  {
    objectLock.readLock().lock();
    try
    {
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        getAttributeInstance(attributeHandle).checkIfAttributeDivestitureWasNotRequested();
      }

      for (AttributeHandle attributeHandle : attributeHandles)
      {
        getAttributeInstance(attributeHandle).cancelNegotiatedAttributeOwnershipDivestiture();
      }

      federate.getRTIChannel().write(
        new CancelNegotiatedAttributeOwnershipDivestiture(objectInstanceHandle, attributeHandles));
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void cancelAttributeOwnershipAcquisition(AttributeHandleSet attributeHandles, Federate federate)
    throws AttributeNotDefined, AttributeAlreadyOwned, AttributeAcquisitionWasNotRequested, RTIinternalError
  {
    objectLock.readLock().lock();
    try
    {
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        FederateAttributeInstance attributeInstance = attributes.get(attributeHandle);
        if (attributeInstance != null)
        {
          throw new AttributeAlreadyOwned(objectClass.getAttributeName(attributeHandle));
        }
        else if (!attributeHandlesBeingAcquired.contains(attributeHandle))
        {
          throw new AttributeAcquisitionWasNotRequested(objectClass.getAttributeName(attributeHandle));
        }
        else
        {
          objectClass.checkIfAttributeNotDefined(attributeHandle);
        }
      }

      for (AttributeHandle attributeHandle : attributeHandles)
      {
        attributes.get(attributeHandle).cancelAttributeOwnershipAcquisition();
      }

      federate.getRTIChannel().write(new CancelAttributeOwnershipAcquisition(objectInstanceHandle, attributeHandles));
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void queryAttributeOwnership(AttributeHandle attributeHandle, Federate federate)
    throws AttributeNotDefined, RTIinternalError
  {
    objectClass.checkIfAttributeNotDefined(attributeHandle);

    objectLock.readLock().lock();
    try
    {
      federate.getRTIChannel().write(new QueryAttributeOwnership(objectInstanceHandle, attributeHandle));
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public boolean isAttributeOwnedByFederate(AttributeHandle attributeHandle)
    throws AttributeNotDefined
  {
    boolean owned;

    objectLock.readLock().lock();
    try
    {
      owned = attributes.get(attributeHandle) != null;
    }
    finally
    {
      objectLock.readLock().unlock();
    }

    if (!owned)
    {
      objectClass.checkIfAttributeNotDefined(attributeHandle);
    }

    return owned;
  }

  public void changeAttributeOrderType(Set<AttributeHandle> attributeHandles, OrderType orderType)
    throws AttributeNotDefined, AttributeNotOwned
  {
    objectLock.readLock().lock();
    try
    {
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        getAttributeInstance(attributeHandle).setOrderType(orderType);
      }
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void requestAttributeTransportationTypeChange(
    AttributeHandleSet attributeHandles, TransportationTypeHandle transportationTypeHandle)
    throws AttributeAlreadyBeingChanged, AttributeNotDefined, AttributeNotOwned
  {
    objectLock.writeLock().lock();
    try
    {
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        FederateAttributeInstance attributeInstance = attributes.get(attributeHandle);
        if (attributeInstance == null)
        {
          // it might not be defined
          //
          objectClass.checkIfAttributeNotDefined(attributeHandle);

          throw new AttributeNotOwned(attributeHandle.toString());
        }
        else
        {
          // attribute is defined and owned

          attributeInstance.checkIfAttributeAlreadyBeingChanged();
        }
      }

      for (AttributeHandle attributeHandle : attributeHandles)
      {
        getAttributeInstance(attributeHandle).setTransportationTypeHandle(transportationTypeHandle);
      }
    }
    finally
    {
      objectLock.writeLock().unlock();
    }
  }

  public void queryAttributeTransportationType(AttributeHandle attributeHandle, Federate federate)
    throws AttributeNotDefined, RTIinternalError
  {
    objectLock.readLock().lock();
    try
    {
      FederateAttributeInstance attributeInstance = attributes.get(attributeHandle);
      if (attributeInstance == null)
      {
        federate.getCallbackManager().add(new ReportAttributeTransportationType(
          objectInstanceHandle, attributeHandle, objectClass.getAttribute(
          attributeHandle).getTransportationTypeHandle()));
      }
      else
      {
        federate.getCallbackManager().add(new ReportAttributeTransportationType(
          objectInstanceHandle, attributeHandle, attributeInstance.getTransportationTypeHandle()));
      }
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void requestAttributeValueUpdate(AttributeHandleSet attributeHandles, byte[] tag, Federate federate)
    throws AttributeNotDefined
  {
    objectClass.checkIfAttributeNotDefined(attributeHandles);

    attributeHandles = new IEEE1516eAttributeHandleSet(attributeHandles);

    objectLock.readLock().lock();
    try
    {
      // only request updates for un-owned attributes
      //
      attributeHandles.removeAll(attributes.keySet());

      federate.getRTIChannel().write(new RequestObjectInstanceAttributeValueUpdate(
        objectInstanceHandle, attributeHandles, tag));
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void associateRegionsForUpdates(AttributeSetRegionSetPairList attributesAndRegions, Federate federate)
    throws ObjectInstanceNotKnown, RTIinternalError
  {
    objectLock.writeLock().lock();
    try
    {
      for (AttributeRegionAssociation attributeRegionAssociation : attributesAndRegions)
      {
        for (AttributeHandle attributeHandle : attributeRegionAssociation.ahset)
        {
          FederateAttributeInstance attribute = attributes.get(attributeHandle);
          if (attribute != null)
          {
            attribute.associateRegionsForUpdates(attributeRegionAssociation.rhset);
          }
        }

        for (RegionHandle regionHandle : attributeRegionAssociation.rhset)
        {
          FederateRegion region = federate.getRegionManager().getRegionSafely(regionHandle);

          region.associateRegionsForUpdates(objectInstanceHandle, attributeRegionAssociation.ahset);
        }
      }

      AssociateRegionsForUpdates associateRegionsForUpdates =
        new AssociateRegionsForUpdates(objectInstanceHandle, attributesAndRegions);
      federate.getRTIChannel().write(associateRegionsForUpdates);

      AssociateRegionsForUpdatesResponse response = associateRegionsForUpdates.getResponse();
      switch (response.getResponse())
      {
        case OBJECT_INSTANCE_NOT_KNOWN:
          throw new ObjectInstanceNotKnown(objectInstanceHandle.toString());
        case SUCCESS:
          break;
        default:
          throw new RTIinternalError("");
      }
    }
    finally
    {
      objectLock.writeLock().unlock();
    }
  }

  public void unassociateRegionsForUpdates(AttributeSetRegionSetPairList attributesAndRegions, Federate federate)
    throws RTIinternalError
  {
    objectLock.readLock().lock();
    try
    {
      for (AttributeRegionAssociation attributeRegionAssociation : attributesAndRegions)
      {
        for (AttributeHandle attributeHandle : attributeRegionAssociation.ahset)
        {
          FederateAttributeInstance attribute = attributes.get(attributeHandle);
          if (attribute != null)
          {
            attribute.unassociateRegionsForUpdates(attributeRegionAssociation.rhset);
          }
        }

        for (RegionHandle regionHandle : attributeRegionAssociation.rhset)
        {
          FederateRegion region = federate.getRegionManager().getRegions().get(regionHandle);
          assert region != null;

          region.unassociateRegionsForUpdates(objectInstanceHandle, attributeRegionAssociation.ahset);
        }
      }

      federate.getRTIChannel().write(new UnassociateRegionsForUpdates(objectInstanceHandle, attributesAndRegions));
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void unpublishObjectClassAttributes(AttributeHandleSet attributeHandles, Federate federate)
  {
    // dangerous method, must be called with proper protection

    attributes.keySet().removeAll(attributeHandles);
  }

  public void checkIfOwnershipAcquisitionPending()
    throws OwnershipAcquisitionPending
  {
    // dangerous method, must be called with proper protection

    if (attributeHandlesBeingAcquired.size() > 0 || attributeHandlesBeingAcquiredIfAvailable.size() > 0)
    {
      AttributeHandleSet allAttributeHandlesBeingAcquired =
        IEEE1516eAttributeHandleSetFactory.INSTANCE.create();
      allAttributeHandlesBeingAcquired.addAll(attributeHandlesBeingAcquired);
      allAttributeHandlesBeingAcquired.addAll(attributeHandlesBeingAcquiredIfAvailable);
      throw new OwnershipAcquisitionPending(allAttributeHandlesBeingAcquired.toString());
    }
  }

  public void checkIfOwnershipAcquisitionPending(AttributeHandleSet attributeHandles)
    throws OwnershipAcquisitionPending
  {
    // dangerous method, must be called with proper protection

    AttributeHandleSet allAttributeHandlesBeingAcquired = null;
    for (AttributeHandle attributeHandle : attributeHandles)
    {
      if (attributeHandlesBeingAcquired.contains(attributeHandle) ||
          attributeHandlesBeingAcquiredIfAvailable.contains(attributeHandle))
      {
        if (allAttributeHandlesBeingAcquired == null)
        {
          allAttributeHandlesBeingAcquired = IEEE1516eAttributeHandleSetFactory.INSTANCE.create();
        }
        allAttributeHandlesBeingAcquired.add(attributeHandle);
      }
    }

    if (allAttributeHandlesBeingAcquired != null)
    {
      throw new OwnershipAcquisitionPending(allAttributeHandlesBeingAcquired.toString());
    }
  }

  public void checkIfFederateOwnsAttributesWithoutOwningPrivilegeToDelete()
    throws FederateOwnsAttributes
  {
    // dangerous method, must be called with proper protection

    if (attributes.size() > 0)
    {
      Attribute privilegeToDeleteObjectAttribute =
        objectClass.getAttributeSafely(FDD.HLA_PRIVILEGE_TO_DELETE_OBJECT);
      if (!attributes.containsKey(privilegeToDeleteObjectAttribute.getAttributeHandle()) && attributes.size() > 1)
      {
        throw new FederateOwnsAttributes(this + " - " + attributes.keySet());
      }
    }
  }

  public void checkIfFederateOwnsAttributes()
    throws FederateOwnsAttributes
  {
    // dangerous method, must be called with proper protection

    if (!attributes.isEmpty())
    {
      throw new FederateOwnsAttributes(this + " - " + attributes.keySet());
    }
  }

  public void checkIfDeletePrivilegeNotHeld()
    throws DeletePrivilegeNotHeld
  {
    // dangerous method, must be called with proper protection

    Attribute privilegeToDeleteObjectAttribute =
      objectClass.getAttributeSafely(FDD.HLA_PRIVILEGE_TO_DELETE_OBJECT);
    if (!attributes.containsKey(privilegeToDeleteObjectAttribute.getAttributeHandle()))
    {
      throw new DeletePrivilegeNotHeld(objectInstanceHandle.toString());
    }
  }

  public int hashCode()
  {
    return objectInstanceHandle.hashCode();
  }

  public boolean equals(Object rhs)
  {
    return rhs instanceof FederateObjectInstance &&
           equals((FederateObjectInstance) rhs);
  }

  public String toString()
  {
    return String.format("%s (%s, %s)", objectInstanceHandle, objectInstanceName, objectClass);
  }

  protected boolean equals(FederateObjectInstance rhs)
  {
    return objectInstanceHandle.equals(rhs.objectInstanceHandle);
  }

  protected FederateAttributeInstance getAttributeInstance(AttributeHandle attributeHandle)
    throws AttributeNotDefined, AttributeNotOwned
  {
    FederateAttributeInstance attributeInstance = attributes.get(attributeHandle);
    if (attributeInstance == null)
    {
      // it might not be defined
      //
      objectClass.checkIfAttributeNotDefined(attributeHandle);

      throw new AttributeNotOwned(objectClass.getAttributeName(attributeHandle));
    }
    return attributeInstance;
  }

  protected void checkIfAttributeNotOwned(Set<AttributeHandle> attributeHandles)
    throws AttributeNotDefined, AttributeNotOwned
  {
    for (AttributeHandle attributeHandle : attributeHandles)
    {
      if (!attributes.containsKey(attributeHandle))
      {
        // it might not be defined
        //
        objectClass.checkIfAttributeNotDefined(attributeHandle);

        throw new AttributeNotOwned(String.format("%s", attributeHandle));
      }
    }
  }

  protected void checkIfAttributeAlreadyBeingDivested(Set<AttributeHandle> attributeHandles)
    throws AttributeAlreadyBeingDivested, AttributeNotOwned, AttributeNotDefined
  {
    for (AttributeHandle attributeHandle : attributeHandles)
    {
      getAttributeInstance(attributeHandle).checkIfAttributeAlreadyBeingDivested();
    }
  }

  protected void checkIfAttributeDivestitureWasNotRequested(Set<AttributeHandle> attributeHandles)
    throws AttributeNotDefined, AttributeNotOwned, AttributeDivestitureWasNotRequested
  {
    for (AttributeHandle attributeHandle : attributeHandles)
    {
      getAttributeInstance(attributeHandle).checkIfAttributeDivestitureWasNotRequested();
    }
  }

  protected void checkIfFederateOwnsAttributes(Set<AttributeHandle> attributeHandles)
    throws FederateOwnsAttributes
  {
    Set<AttributeHandle> ownedAttributeHandles = null;
    for (AttributeHandle attributeHandle : attributeHandles)
    {
      if (attributes.containsKey(attributeHandle))
      {
        if (ownedAttributeHandles == null)
        {
          ownedAttributeHandles = new HashSet<AttributeHandle>();
        }
        ownedAttributeHandles.add(attributeHandle);
      }
    }
    if (ownedAttributeHandles != null)
    {
      throw new FederateOwnsAttributes(String.format(
        "%s - %s", objectInstanceHandle, ownedAttributeHandles));
    }
  }

  public void fireReflectAttributeValues(
    ReflectAttributeValues reflectAttributeValues, FederateAmbassador federateAmbassador,
    FederateRegionManager regionManager)
    throws FederateInternalError
  {
    objectLock.readLock().lock();
    try
    {
      if (reflectAttributeValues.hasSentRegions())
      {
        reflectAttributeValues.setSentRegions(
          regionManager.createTemporaryRegions(reflectAttributeValues.getRegions()));
      }

      AttributeHandleValueMap attributeValues = reflectAttributeValues.getAttributeValues();
      byte[] tag = reflectAttributeValues.getTag();
      OrderType sentOrderType = reflectAttributeValues.getSentOrderType();
      TransportationTypeHandle transportationTypeHandle = reflectAttributeValues.getTransportationTypeHandle();
      LogicalTime time = reflectAttributeValues.getTime();

      // TODO: might have to check attributes against the owned attributes in case an attribute has been acquired

      if (time == null)
      {
        federateAmbassador.reflectAttributeValues(
          objectInstanceHandle, attributeValues, tag, sentOrderType, transportationTypeHandle, reflectAttributeValues);
      }
      else
      {
        OrderType receivedOrderType = reflectAttributeValues.getReceivedOrderType();
        MessageRetractionHandle messageRetractionHandle = reflectAttributeValues.getMessageRetractionHandle();
        if (messageRetractionHandle == null)
        {
          federateAmbassador.reflectAttributeValues(
            objectInstanceHandle, attributeValues, tag, sentOrderType, transportationTypeHandle, time,
            receivedOrderType, reflectAttributeValues);
        }
        else
        {
          federateAmbassador.reflectAttributeValues(
            objectInstanceHandle, attributeValues, tag, sentOrderType, transportationTypeHandle, time,
            receivedOrderType, messageRetractionHandle, reflectAttributeValues);
        }
      }
    }
    finally
    {
      objectLock.readLock().unlock();

      if (reflectAttributeValues.hasSentRegions())
      {
        regionManager.deleteTemporaryRegions(reflectAttributeValues.getSentRegions());
      }
    }
  }

  public void removeObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag, OrderType sentOrderType, LogicalTime time,
    OrderType receivedOrderType, MessageRetractionHandle messageRetractionHandle,
    FederateAmbassador.SupplementalRemoveInfo supplementalRemoveInfo, FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    objectLock.readLock().lock();
    try
    {
      if (time == null)
      {
        federateAmbassador.removeObjectInstance(objectInstanceHandle, tag, sentOrderType, supplementalRemoveInfo);
      }
      else if (messageRetractionHandle == null)
      {
        federateAmbassador.removeObjectInstance(
          objectInstanceHandle, tag, sentOrderType, time, receivedOrderType, supplementalRemoveInfo);
      }
      else
      {
        federateAmbassador.removeObjectInstance(
          objectInstanceHandle, tag, sentOrderType, time, receivedOrderType, messageRetractionHandle,
          supplementalRemoveInfo);
      }
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisitionNotification(
    AttributeHandleSet attributeHandles, byte[] tag, FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    objectLock.writeLock().lock();
    try
    {
      attributeHandlesBeingAcquired.removeAll(attributeHandles);

      for (AttributeHandle attributeHandle : attributeHandles)
      {
        Attribute attribute = objectClass.getAttributeSafely(attributeHandle);

        // create an attribute instance for each of the published attributes
        //
        attributes.put(attributeHandle, new FederateAttributeInstance(attribute));
      }

      federateAmbassador.attributeOwnershipAcquisitionNotification(
        objectInstanceHandle, attributeHandles, tag);
    }
    finally
    {
      objectLock.writeLock().unlock();
    }
  }

  private static class ReportAttributeTransportationType
  implements Callback
  {
    private final ObjectInstanceHandle objectInstanceHandle;
    private final AttributeHandle attributeHandle;
    private final TransportationTypeHandle transportationTypeHandle;

    private ReportAttributeTransportationType(
      ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle,
      TransportationTypeHandle transportationTypeHandle)
    {
      this.objectInstanceHandle = objectInstanceHandle;
      this.attributeHandle = attributeHandle;
      this.transportationTypeHandle = transportationTypeHandle;
    }

    public void execute(FederateAmbassador federateAmbassador)
      throws FederateInternalError
    {
      federateAmbassador.reportAttributeTransportationType(
        objectInstanceHandle, attributeHandle, transportationTypeHandle);
    }
  }
}
