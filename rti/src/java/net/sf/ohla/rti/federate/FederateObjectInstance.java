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

import java.io.IOException;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.util.AttributeHandles;
import net.sf.ohla.rti.util.FederateHandles;
import net.sf.ohla.rti.util.ObjectClassHandles;
import net.sf.ohla.rti.util.ObjectInstanceHandles;
import net.sf.ohla.rti.fdd.Attribute;
import net.sf.ohla.rti.fdd.FDD;
import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.fdd.TransportationType;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleSet;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleSetFactory;
import net.sf.ohla.rti.i18n.ExceptionMessages;
import net.sf.ohla.rti.i18n.I18n;
import net.sf.ohla.rti.messages.AssociateRegionsForUpdates;
import net.sf.ohla.rti.messages.AssociateRegionsForUpdatesResponse;
import net.sf.ohla.rti.messages.AttributeOwnershipAcquisition;
import net.sf.ohla.rti.messages.AttributeOwnershipAcquisitionIfAvailable;
import net.sf.ohla.rti.messages.AttributeOwnershipDivestitureIfWanted;
import net.sf.ohla.rti.messages.CancelAttributeOwnershipAcquisition;
import net.sf.ohla.rti.messages.CancelNegotiatedAttributeOwnershipDivestiture;
import net.sf.ohla.rti.messages.ConfirmDivestiture;
import net.sf.ohla.rti.messages.GetUpdateRateValueForAttribute;
import net.sf.ohla.rti.messages.GetUpdateRateValueForAttributeResponse;
import net.sf.ohla.rti.messages.NegotiatedAttributeOwnershipDivestiture;
import net.sf.ohla.rti.messages.QueryAttributeOwnership;
import net.sf.ohla.rti.messages.RequestObjectInstanceAttributeValueUpdate;
import net.sf.ohla.rti.messages.UnassociateRegionsForUpdates;
import net.sf.ohla.rti.messages.UnconditionalAttributeOwnershipDivestiture;
import net.sf.ohla.rti.messages.UpdateAttributeValues;
import net.sf.ohla.rti.proto.FederationExecutionSaveProtos.FederateState.FederateObjectManagerState.FederateObjectInstanceState;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
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
{
  private final ObjectInstanceHandle objectInstanceHandle;
  private final ObjectClass objectClass;
  private final String objectInstanceName;
  private final FederateHandle producingFederateHandle;

  private final Map<AttributeHandle, FederateAttributeInstance> attributes = new HashMap<>();

  private final Set<AttributeHandle> attributeHandlesBeingAcquired = new HashSet<>();
  private final Set<AttributeHandle> attributeHandlesBeingAcquiredIfAvailable = new HashSet<>();

  private final ReadWriteLock objectLock = new ReentrantReadWriteLock(true);

  public FederateObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, ObjectClass objectClass, String objectInstanceName,
    FederateHandle producingFederateHandle)
  {
    this.objectInstanceHandle = objectInstanceHandle;
    this.objectClass = objectClass;
    this.objectInstanceName = objectInstanceName;
    this.producingFederateHandle = producingFederateHandle;
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
    this(objectInstanceHandle, objectClass, objectInstanceName, federate.getFederateHandle());

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
        for (RegionHandle regionHandle : attributeRegionAssociation.rhset)
        {
          FederateRegion region = federate.getRegionManager().getRegionSafely(regionHandle);

          region.associateRegionsForUpdates(objectInstanceHandle, attributeRegionAssociation.ahset);
        }
      }
    }
  }

  public FederateObjectInstance(FDD fdd, CodedInputStream in)
    throws IOException
  {
    FederateObjectInstanceState objectInstanceState = in.readMessage(FederateObjectInstanceState.PARSER, null);

    producingFederateHandle = FederateHandles.convert(objectInstanceState.getProducingFederateHandle());
    objectInstanceHandle = ObjectInstanceHandles.convert(objectInstanceState.getObjectInstanceHandle());
    objectClass = fdd.getObjectClassSafely(ObjectClassHandles.convert(objectInstanceState.getObjectClassHandle()));
    objectInstanceName = objectInstanceState.getObjectInstanceName();

    for (FederateObjectInstanceState.FederateAttributeInstanceState attributeInstanceState : objectInstanceState.getAttributeInstanceStatesList())
    {
      FederateAttributeInstance federateAttributeInstance =
        new FederateAttributeInstance(attributeInstanceState, objectClass);
      attributes.put(federateAttributeInstance.getAttributeHandle(), federateAttributeInstance);
    }

    attributeHandlesBeingAcquired.addAll(
      AttributeHandles.convertAttributeHandles(objectInstanceState.getAttributeHandlesBeingAcquiredList()));
    attributeHandlesBeingAcquiredIfAvailable.addAll(
      AttributeHandles.convertAttributeHandles(objectInstanceState.getAttributeHandlesBeingAcquiredIfAvailableList()));
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
        objectInstanceHandle, attributeValues, TransportationType.HLA_RELIABLE.getTransportationTypeHandle(), tag));
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
        objectInstanceHandle, attributeValues, TransportationType.HLA_RELIABLE.getTransportationTypeHandle(), tag,
        sentOrderType, updateTime, messageRetractionHandle));
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
        throw new FederateOwnsAttributes(I18n.getMessage(
          ExceptionMessages.FEDERATE_OWNS_ATTRIBUTES, this, attributes.values()));
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
      Collection<Attribute> ownedAttributes = null;
      Collection<Attribute> attributesAlreadyBeingAcquired = null;

      for (AttributeHandle attributeHandle : attributeHandles)
      {
        objectClass.checkIfAttributeNotDefined(attributeHandle);

        FederateAttributeInstance attributeInstance = attributes.get(attributeHandle);
        if (attributeInstance != null)
        {
          if (ownedAttributes == null)
          {
            ownedAttributes = new LinkedList<>();
          }
          ownedAttributes.add(attributeInstance.getAttribute());
        }
        else if (attributeHandlesBeingAcquiredIfAvailable.contains(attributeHandle))
        {
          if (attributesAlreadyBeingAcquired == null)
          {
            attributesAlreadyBeingAcquired = new LinkedList<>();
          }
          attributesAlreadyBeingAcquired.add(objectClass.getAttributeSafely(attributeHandle));
        }
      }

      if (ownedAttributes != null)
      {
        throw new FederateOwnsAttributes(I18n.getMessage(
          ExceptionMessages.FEDERATE_OWNS_ATTRIBUTES, this, ownedAttributes));
      }
      else if (attributesAlreadyBeingAcquired != null)
      {
        throw new AttributeAlreadyBeingAcquired(I18n.getMessage(
          ExceptionMessages.ATTRIBUTE_ALREADY_BEING_ACQUIRED, attributesAlreadyBeingAcquired));
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
          throw new AttributeAlreadyOwned(I18n.getMessage(
            ExceptionMessages.ATTRIBUTE_ALREADY_OWNED, attributeInstance.getAttribute()));
        }
        else if (!attributeHandlesBeingAcquired.contains(attributeHandle))
        {
          throw new AttributeAcquisitionWasNotRequested(I18n.getMessage(
            ExceptionMessages.ATTRIBUTE_ACQUISITION_WAS_NOT_REQUESTED,
            objectClass.getAttributeSafely(attributeHandle)));
        }
        else
        {
          objectClass.checkIfAttributeNotDefined(attributeHandle);
        }
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

  public double getUpdateRateValueForAttribute(AttributeHandle attributeHandle, Federate federate)
    throws ObjectInstanceNotKnown, AttributeNotDefined, RTIinternalError
  {
    double updateRate;

    objectLock.readLock().lock();
    try
    {
      FederateAttributeInstance attributeInstance = attributes.get(attributeHandle);
      if (attributeInstance == null)
      {
        // it might not be defined
        //
        objectClass.checkIfAttributeNotDefined(attributeHandle);
      }

      GetUpdateRateValueForAttribute getUpdateRateValueForAttribute =
        new GetUpdateRateValueForAttribute(objectInstanceHandle, attributeHandle);
      federate.getRTIChannel().write(getUpdateRateValueForAttribute);

      GetUpdateRateValueForAttributeResponse response = getUpdateRateValueForAttribute.getResponse();
      if (response.isSuccess())
      {
        updateRate = response.getUpdateRate();
      }
      else
      {
        assert response.isFailure();

        switch (response.getCause())
        {
          case OBJECT_INSTANCE_NOT_KNOWN:
            throw new ObjectInstanceNotKnown(I18n.getMessage(
              ExceptionMessages.OBJECT_INSTANCE_HANDLE_NOT_KNOWN, objectInstanceHandle));
          default:
            throw new RTIinternalError(I18n.getMessage(ExceptionMessages.UNEXPECTED_EXCEPTION));
        }
      }
    }
    finally
    {
      objectLock.readLock().unlock();
    }

    return updateRate;
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

          throw new AttributeNotOwned(I18n.getMessage(
            ExceptionMessages.ATTRIBUTE_NOT_OWNED, objectClass.getAttributeSafely(attributeHandle)));
        }
        else
        {
          // attribute is defined and owned

          attributeInstance.checkIfAttributeAlreadyBeingChanged();
        }
      }

      // TODO: need to provide confirmation call back and execute change after callback
      //
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
          attributeHandle).getTransportationTypeHandle()), false);
      }
      else
      {
        federate.getCallbackManager().add(new ReportAttributeTransportationType(
          objectInstanceHandle, attributeHandle, attributeInstance.getTransportationTypeHandle()), false);
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
      if (response.isFailure())
      {
        switch (response.getCause())
        {
          case OBJECT_INSTANCE_NOT_KNOWN:
            throw new ObjectInstanceNotKnown(
              I18n.getMessage(ExceptionMessages.OBJECT_INSTANCE_HANDLE_NOT_KNOWN, objectInstanceHandle));
          default:
            throw new RTIinternalError(I18n.getMessage(ExceptionMessages.UNEXPECTED_EXCEPTION));
        }
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
      throw new OwnershipAcquisitionPending(I18n.getMessage(
        ExceptionMessages.OWNERSHIP_ACQUISITION_PENDING, this,
        objectClass.getAttributesSafely(attributeHandlesBeingAcquired, attributeHandlesBeingAcquiredIfAvailable)));
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
      throw new OwnershipAcquisitionPending(I18n.getMessage(
        ExceptionMessages.OWNERSHIP_ACQUISITION_PENDING, this,
        objectClass.getAttributeNamesSafely(allAttributeHandlesBeingAcquired)));
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
        throw new FederateOwnsAttributes(I18n.getMessage(
          ExceptionMessages.FEDERATE_OWNS_ATTRIBUTES, this, objectClass.getAttributesSafely(attributes.keySet())));
      }
    }
  }

  public void checkIfFederateOwnsAttributes()
    throws FederateOwnsAttributes
  {
    // dangerous method, must be called with proper protection

    if (!attributes.isEmpty())
    {
      throw new FederateOwnsAttributes(I18n.getMessage(
        ExceptionMessages.FEDERATE_OWNS_ATTRIBUTES, this, objectClass.getAttributesSafely(attributes.keySet())));
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
      throw new DeletePrivilegeNotHeld(I18n.getMessage(ExceptionMessages.DELETE_PRIVILEGE_NOT_HELD, this));
    }
  }

  public void fireReflectAttributeValues(
    FederateAmbassador federateAmbassador, AttributeHandleValueMap attributeValues, byte[] tag, OrderType sentOrderType,
    TransportationTypeHandle transportationTypeHandle, LogicalTime time, OrderType receivedOrderType,
    MessageRetractionHandle messageRetractionHandle, FederateAmbassador.SupplementalReflectInfo supplementalReflectInfo)
  throws FederateInternalError
  {
    objectLock.readLock().lock();
    try
    {
      if (receivedOrderType == OrderType.TIMESTAMP)
      {
        federateAmbassador.reflectAttributeValues(
          objectInstanceHandle, attributeValues, tag, OrderType.TIMESTAMP, transportationTypeHandle, time,
          OrderType.TIMESTAMP, messageRetractionHandle, supplementalReflectInfo);
      }
      else if (time == null)
      {
        federateAmbassador.reflectAttributeValues(
          objectInstanceHandle, attributeValues, tag, sentOrderType, transportationTypeHandle, supplementalReflectInfo);
      }
      else
      {
        federateAmbassador.reflectAttributeValues(
          objectInstanceHandle, attributeValues, tag, sentOrderType, transportationTypeHandle, time, OrderType.RECEIVE,
          supplementalReflectInfo);
      }
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void fireRemoveObjectInstance(
    FederateAmbassador federateAmbassador, byte[] tag, OrderType sentOrderType, LogicalTime time,
    OrderType receivedOrderType, MessageRetractionHandle messageRetractionHandle,
    FederateAmbassador.SupplementalRemoveInfo supplementalRemoveInfo)
    throws FederateInternalError
  {
    objectLock.readLock().lock();
    try
    {
      if (receivedOrderType == OrderType.TIMESTAMP)
      {
        federateAmbassador.removeObjectInstance(
          objectInstanceHandle, tag, OrderType.TIMESTAMP, time, OrderType.TIMESTAMP, messageRetractionHandle,
          supplementalRemoveInfo);
      }
      else if (time == null)
      {
        federateAmbassador.removeObjectInstance(objectInstanceHandle, tag, sentOrderType, supplementalRemoveInfo);
      }
      else
      {
        federateAmbassador.removeObjectInstance(
          objectInstanceHandle, tag, sentOrderType, time, OrderType.RECEIVE, supplementalRemoveInfo);
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

      federateAmbassador.attributeOwnershipAcquisitionNotification(objectInstanceHandle, attributeHandles, tag);
    }
    finally
    {
      objectLock.writeLock().unlock();
    }
  }

  public void saveState(CodedOutputStream out)
    throws IOException
  {
    FederateObjectInstanceState.Builder objectInstanceState = FederateObjectInstanceState.newBuilder();

    objectInstanceState.setProducingFederateHandle(FederateHandles.convert(producingFederateHandle));
    objectInstanceState.setObjectInstanceHandle(ObjectInstanceHandles.convert(objectInstanceHandle));
    objectInstanceState.setObjectClassHandle(ObjectClassHandles.convert(objectClass.getObjectClassHandle()));
    objectInstanceState.setObjectInstanceName(objectInstanceName);

    for (FederateAttributeInstance federateAttributeInstance : attributes.values())
    {
      objectInstanceState.addAttributeInstanceStates(federateAttributeInstance.saveState());
    }

    objectInstanceState.addAllAttributeHandlesBeingAcquired(AttributeHandles.convert(attributeHandlesBeingAcquired));
    objectInstanceState.addAllAttributeHandlesBeingAcquiredIfAvailable(
      AttributeHandles.convert(attributeHandlesBeingAcquiredIfAvailable));

    out.writeMessageNoTag(objectInstanceState.build());
  }

  @Override
  public int hashCode()
  {
    return objectInstanceHandle.hashCode();
  }

  @Override
  public boolean equals(Object rhs)
  {
    return this == rhs || (rhs instanceof FederateObjectInstance && equals((FederateObjectInstance) rhs));
  }

  @Override
  public String toString()
  {
    return new StringBuilder(objectInstanceHandle.toString()).append("/").append(objectInstanceName).append("/").append(
      objectClass.getObjectClassName()).toString();
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

      throw new AttributeNotOwned(I18n.getMessage(
        ExceptionMessages.ATTRIBUTE_NOT_OWNED, objectClass.getAttributeSafely(attributeHandle)));
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

        throw new AttributeNotOwned(I18n.getMessage(
          ExceptionMessages.ATTRIBUTE_NOT_OWNED, objectClass.getAttributeSafely(attributeHandle)));
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
    Collection<Attribute> ownedAttributes = null;
    for (AttributeHandle attributeHandle : attributeHandles)
    {
      FederateAttributeInstance attributeInstance = attributes.get(attributeHandle);
      if (attributeInstance != null)
      {
        if (ownedAttributes == null)
        {
          ownedAttributes = new LinkedList<>();
        }
        ownedAttributes.add(attributeInstance.getAttribute());
      }
    }
    if (ownedAttributes != null)
    {
      throw new FederateOwnsAttributes(I18n.getMessage(
        ExceptionMessages.FEDERATE_OWNS_ATTRIBUTES, this, ownedAttributes));
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
