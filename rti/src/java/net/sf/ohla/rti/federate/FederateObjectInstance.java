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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.fdd.Attribute;
import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.hla.rti1516.IEEE1516AttributeHandleSet;
import net.sf.ohla.rti.hla.rti1516.IEEE1516RegionHandleSet;
import net.sf.ohla.rti.messages.AttributeOwnershipAcquisition;
import net.sf.ohla.rti.messages.AttributeOwnershipAcquisitionIfAvailable;
import net.sf.ohla.rti.messages.AttributeOwnershipDivestitureIfWanted;
import net.sf.ohla.rti.messages.AttributeOwnershipDivestitureIfWantedResponse;
import net.sf.ohla.rti.messages.CancelAttributeOwnershipAcquisition;
import net.sf.ohla.rti.messages.CancelNegotiatedAttributeOwnershipDivestiture;
import net.sf.ohla.rti.messages.ConfirmDivestiture;
import net.sf.ohla.rti.messages.DefaultResponse;
import net.sf.ohla.rti.messages.NegotiatedAttributeOwnershipDivestiture;
import net.sf.ohla.rti.messages.QueryAttributeOwnership;
import net.sf.ohla.rti.messages.RequestAttributeValueUpdate;
import net.sf.ohla.rti.messages.UnconditionalAttributeOwnershipDivestiture;
import net.sf.ohla.rti.messages.UpdateAttributeValues;

import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;

import hla.rti1516.AttributeAcquisitionWasNotRequested;
import hla.rti1516.AttributeAlreadyBeingAcquired;
import hla.rti1516.AttributeAlreadyBeingDivested;
import hla.rti1516.AttributeAlreadyOwned;
import hla.rti1516.AttributeDivestitureWasNotRequested;
import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.AttributeNotDefined;
import hla.rti1516.AttributeNotOwned;
import hla.rti1516.AttributeNotPublished;
import hla.rti1516.AttributeNotRecognized;
import hla.rti1516.AttributeNotSubscribed;
import hla.rti1516.AttributeRegionAssociation;
import hla.rti1516.CouldNotDiscover;
import hla.rti1516.DeletePrivilegeNotHeld;
import hla.rti1516.FederateAmbassador;
import hla.rti1516.FederateInternalError;
import hla.rti1516.FederateOwnsAttributes;
import hla.rti1516.InvalidLogicalTime;
import hla.rti1516.LogicalTime;
import hla.rti1516.MessageRetractionHandle;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectClassNotRecognized;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ObjectInstanceNotKnown;
import hla.rti1516.OrderType;
import hla.rti1516.OwnershipAcquisitionPending;
import hla.rti1516.RTIinternalError;
import hla.rti1516.RegionHandleSet;
import hla.rti1516.TransportationType;

public class FederateObjectInstance
  implements Serializable
{
  protected final ObjectInstanceHandle objectInstanceHandle;
  protected final ObjectClass objectClass;
  protected final String name;

  protected Map<AttributeHandle, FederateAttributeInstance> attributes =
    new HashMap<AttributeHandle, FederateAttributeInstance>();

  protected Set<AttributeHandle> attributeHandlesBeingAcquired =
    new HashSet<AttributeHandle>();
  protected Set<AttributeHandle> attributeHandlesBeingAcquiredIfAvailable =
    new HashSet<AttributeHandle>();

  protected ReadWriteLock objectLock = new ReentrantReadWriteLock(true);

  public FederateObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                                ObjectClass objectClass, String name)
  {
    this.objectInstanceHandle = objectInstanceHandle;
    this.objectClass = objectClass;
    this.name = name;
  }

  public FederateObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                                ObjectClass objectClass, String name,
                                Set<AttributeHandle> publishedAttributeHandles)
  {
    this(objectInstanceHandle, objectClass, name);

    for (AttributeHandle attributeHandle : publishedAttributeHandles)
    {
      Attribute attribute = objectClass.getAttributes().get(attributeHandle);
      assert attribute != null;

      // create an attribute instance for each of the published attributes
      //
      attributes.put(attributeHandle, new FederateAttributeInstance(attribute));
    }

    Attribute attribute = objectClass.getAttributesByName().get(
      Attribute.HLA_PRIVILEGE_TO_DELETE_OBJECT);
    assert attribute != null;
    if (!attributes.containsKey(attribute.getAttributeHandle()))
    {
      // create an attribute instance for the HLA privilege to delete
      // object attribute
      //
      attributes.put(attribute.getAttributeHandle(),
                     new FederateAttributeInstance(attribute));
    }
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

  public String getName()
  {
    return name;
  }

  public WriteFuture updateAttributeValues(
    AttributeHandleValueMap attributeValues, byte[] tag, IoSession rtiSession)
    throws AttributeNotDefined, AttributeNotOwned
  {
    objectLock.readLock().lock();
    try
    {
      checkIfAttributeNotOwned(attributeValues.keySet());

      RegionHandleSet sentRegionHandles = new IEEE1516RegionHandleSet();
      for (AttributeHandle attributeHandle : attributeValues.keySet())
      {
        sentRegionHandles.addAll(
          getAttributeInstance(attributeHandle).getAssociatedRegions());
      }

      return rtiSession.write(new UpdateAttributeValues(
        objectInstanceHandle, attributeValues, tag, sentRegionHandles,
        OrderType.RECEIVE, TransportationType.HLA_RELIABLE));
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public WriteFuture updateAttributeValues(
    AttributeHandleValueMap attributeValues, byte[] tag,
    LogicalTime updateTime, MessageRetractionHandle messageRetractionHandle,
    OrderType sentOrderType, IoSession rtiSession)
    throws AttributeNotDefined, AttributeNotOwned
  {
    objectLock.readLock().lock();
    try
    {
      if (sentOrderType == OrderType.TIMESTAMP)
      {
        // TODO: divide attributes by order type
      }

      RegionHandleSet sentRegionHandles = new IEEE1516RegionHandleSet();
      for (AttributeHandle attributeHandle : attributeValues.keySet())
      {
        sentRegionHandles.addAll(
          getAttributeInstance(attributeHandle).getAssociatedRegions());
      }

      return rtiSession.write(new UpdateAttributeValues(
        objectInstanceHandle, attributeValues, tag, sentRegionHandles,
        sentOrderType, TransportationType.HLA_RELIABLE, updateTime,
        messageRetractionHandle));
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void deleteObjectInstance(byte[] tag)
    throws DeletePrivilegeNotHeld
  {
    objectLock.readLock().lock();
    try
    {
      Attribute attribute = objectClass.getAttributesByName().get(
        Attribute.HLA_PRIVILEGE_TO_DELETE_OBJECT);
      assert attribute != null;
      if (!attributes.containsKey(attribute.getAttributeHandle()))
      {
        throw new DeletePrivilegeNotHeld(objectInstanceHandle.toString());
      }
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
        throw new FederateOwnsAttributes(String.format(
          "%s - %s", objectInstanceHandle, attributes.keySet()));
      }
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void unconditionalAttributeOwnershipDivestiture(
    AttributeHandleSet attributeHandles, IoSession rtiSession)
    throws AttributeNotDefined, AttributeNotOwned, RTIinternalError
  {
    WriteFuture writeFuture;

    objectLock.writeLock().lock();
    try
    {
      checkIfAttributeNotOwned(attributeHandles);

      attributes.keySet().removeAll(attributeHandles);

      writeFuture = rtiSession.write(
        new UnconditionalAttributeOwnershipDivestiture(
          objectInstanceHandle, attributeHandles));
    }
    finally
    {
      objectLock.writeLock().unlock();
    }

    // TODO: set timeout
    //
    writeFuture.join();

    if (!writeFuture.isWritten())
    {
      throw new RTIinternalError("error communicating with RTI");
    }
  }

  public void negotiatedAttributeOwnershipDivestiture(
    AttributeHandleSet attributeHandles, byte[] tag, IoSession rtiSession)
    throws AttributeNotDefined, AttributeNotOwned,
           AttributeAlreadyBeingDivested, RTIinternalError
  {
    WriteFuture writeFuture;

    objectLock.writeLock().lock();
    try
    {
      checkIfAttributeAlreadyBeingDivested(attributeHandles);

      for (AttributeHandle attributeHandle : attributeHandles)
      {
        getAttributeInstance(
          attributeHandle).negotiatedAttributeOwnershipDivestiture();
      }

      writeFuture = rtiSession.write(
        new NegotiatedAttributeOwnershipDivestiture(
          objectInstanceHandle, attributeHandles, tag));
    }
    finally
    {
      objectLock.writeLock().unlock();
    }

    // TODO: set timeout
    //
    writeFuture.join();

    if (!writeFuture.isWritten())
    {
      throw new RTIinternalError("error communicating with RTI");
    }
  }

  public void confirmDivestiture(AttributeHandleSet attributeHandles,
                                 byte[] tag, IoSession rtiSession)
    throws AttributeNotDefined, AttributeNotOwned,
           AttributeDivestitureWasNotRequested, RTIinternalError
  {
    WriteFuture writeFuture;

    objectLock.writeLock().lock();
    try
    {
      checkIfAttributeDivestitureWasNotRequested(attributeHandles);

      for (AttributeHandle attributeHandle : attributeHandles)
      {
        getAttributeInstance(attributeHandle).confirmDivestiture();
        attributes.remove(attributeHandle);
      }

      writeFuture = rtiSession.write(
        new ConfirmDivestiture(objectInstanceHandle, attributeHandles, tag));
    }
    finally
    {
      objectLock.writeLock().unlock();
    }

    // TODO: set timeout
    //
    writeFuture.join();

    if (!writeFuture.isWritten())
    {
      throw new RTIinternalError("error communicating with RTI");
    }
  }

  public void attributeOwnershipAcquisition(
    AttributeHandleSet attributeHandles, byte[] tag, IoSession rtiSession)
    throws FederateOwnsAttributes, RTIinternalError
  {
    WriteFuture writeFuture;

    objectLock.writeLock().lock();
    try
    {
      checkIfFederateOwnsAttributes(attributeHandles);

      attributeHandlesBeingAcquired.addAll(attributeHandles);

      writeFuture =
        rtiSession.write(new AttributeOwnershipAcquisition(
          objectInstanceHandle, attributeHandles, tag));
    }
    finally
    {
      objectLock.writeLock().unlock();
    }

    // TODO: set timeout
    //
    writeFuture.join();

    if (!writeFuture.isWritten())
    {
      throw new RTIinternalError("error communicating with RTI");
    }
  }

  public void attributeOwnershipAcquisitionIfAvailable(
    AttributeHandleSet attributeHandles, IoSession rtiSession)
    throws AttributeNotDefined, FederateOwnsAttributes,
           AttributeAlreadyBeingAcquired, RTIinternalError
  {
    WriteFuture writeFuture;

    objectLock.writeLock().lock();
    try
    {
      Set<AttributeHandle> ownedAttributeHandles =
        new HashSet<AttributeHandle>();
      Set<AttributeHandle> attributeHandlesAlreadyBeingAcquired =
        new HashSet<AttributeHandle>();

      for (AttributeHandle attributeHandle : attributeHandles)
      {
        objectClass.checkIfAttributeNotDefined(attributeHandle);

        if (attributes.containsKey(attributeHandle))
        {
          ownedAttributeHandles.add(attributeHandle);
        }
        else if (attributeHandlesBeingAcquiredIfAvailable.contains(
          attributeHandle))
        {
          attributeHandlesAlreadyBeingAcquired.add(attributeHandle);
        }
      }

      if (!ownedAttributeHandles.isEmpty())
      {
        throw new FederateOwnsAttributes(String.format(
          "%s - %s", objectInstanceHandle, ownedAttributeHandles));
      }
      else if (!attributeHandlesAlreadyBeingAcquired.isEmpty())
      {
        throw new AttributeAlreadyBeingAcquired(
          attributeHandlesAlreadyBeingAcquired.toString());
      }

      attributeHandlesBeingAcquiredIfAvailable.addAll(attributeHandles);

      writeFuture =
        rtiSession.write(new AttributeOwnershipAcquisitionIfAvailable(
          objectInstanceHandle, attributeHandles));
    }
    finally
    {
      objectLock.writeLock().unlock();
    }

    // TODO: set timeout
    //
    writeFuture.join();

    if (!writeFuture.isWritten())
    {
      throw new RTIinternalError("error communicating with RTI");
    }
  }

  public AttributeHandleSet attributeOwnershipDivestitureIfWanted(
    AttributeHandleSet attributeHandles, IoSession rtiSession)
    throws AttributeNotDefined, AttributeNotOwned, RTIinternalError
  {
    objectLock.writeLock().lock();
    try
    {
      checkIfAttributeNotOwned(attributeHandles);

      AttributeOwnershipDivestitureIfWanted
        attributeOwnershipDivestitureIfWanted =
        new AttributeOwnershipDivestitureIfWanted(
          objectInstanceHandle, attributeHandles);
      WriteFuture writeFuture =
        rtiSession.write(attributeOwnershipDivestitureIfWanted);

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      try
      {
        // TODO: set timeout
        //
        Object response = attributeOwnershipDivestitureIfWanted.getResponse();

        assert response instanceof AttributeOwnershipDivestitureIfWantedResponse :
          String.format("unexpected response: %s", response);

        AttributeOwnershipDivestitureIfWantedResponse
          attributeOwnershipDivestitureIfWantedResponse =
          (AttributeOwnershipDivestitureIfWantedResponse) response;

        AttributeHandleSet divestedAttributeHandles =
          attributeOwnershipDivestitureIfWantedResponse.getAttributeHandles();

        attributes.keySet().removeAll(divestedAttributeHandles);

        // confirm that the attributes have been divested
        //
        writeFuture = rtiSession.write(new DefaultResponse(
          attributeOwnershipDivestitureIfWantedResponse.getId()));

        if (!writeFuture.isWritten())
        {
          throw new RTIinternalError("error communicating with RTI");
        }

        return divestedAttributeHandles;
      }
      catch (InterruptedException ie)
      {
        throw new RTIinternalError("interrupted awaitng response", ie);
      }
      catch (ExecutionException ee)
      {
        throw new RTIinternalError("unable to get response", ee);
      }
    }
    finally
    {
      objectLock.writeLock().unlock();
    }
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture(
    AttributeHandleSet attributeHandles, IoSession rtiSession)
    throws AttributeNotDefined, AttributeNotOwned,
           AttributeDivestitureWasNotRequested, RTIinternalError
  {
    WriteFuture writeFuture;

    objectLock.readLock().lock();
    try
    {
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        getAttributeInstance(
          attributeHandle).checkIfAttributeDivestitureWasNotRequested();
      }

      for (AttributeHandle attributeHandle : attributeHandles)
      {
        getAttributeInstance(
          attributeHandle).cancelNegotiatedAttributeOwnershipDivestiture();
      }

      writeFuture = rtiSession.write(
        new CancelNegotiatedAttributeOwnershipDivestiture(
          objectInstanceHandle, attributeHandles));
    }
    finally
    {
      objectLock.readLock().unlock();
    }

    // TODO: set timeout
    //
    writeFuture.join();

    if (!writeFuture.isWritten())
    {
      throw new RTIinternalError("error communicating with RTI");
    }
  }

  public void cancelAttributeOwnershipAcquisition(
    AttributeHandleSet attributeHandles, IoSession rtiSession)
    throws AttributeNotDefined, AttributeAlreadyOwned,
           AttributeAcquisitionWasNotRequested, RTIinternalError
  {
    WriteFuture writeFuture;

    objectLock.readLock().lock();
    try
    {
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        FederateAttributeInstance attributeInstance = attributes.get(attributeHandle);
        if (attributeInstance != null)
        {
          throw new AttributeAlreadyOwned(String.format("%s", attributeHandle));
        }
        else if (!attributeHandlesBeingAcquired.contains(attributeHandle))
        {
          throw new AttributeAcquisitionWasNotRequested(
            String.format("%s", attributeHandle));
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

      writeFuture = rtiSession.write(
        new CancelAttributeOwnershipAcquisition(
          objectInstanceHandle, attributeHandles));
    }
    finally
    {
      objectLock.readLock().unlock();
    }

    // TODO: set timeout
    //
    writeFuture.join();

    if (!writeFuture.isWritten())
    {
      throw new RTIinternalError("error communicating with RTI");
    }
  }

  public void queryAttributeOwnership(AttributeHandle attributeHandle,
                                      IoSession rtiSession)
    throws AttributeNotDefined, RTIinternalError
  {
    objectClass.checkIfAttributeNotDefined(attributeHandle);

    WriteFuture writeFuture;

    objectLock.readLock().lock();
    try
    {
      writeFuture = rtiSession.write(
        new QueryAttributeOwnership(objectInstanceHandle, attributeHandle));
    }
    finally
    {
      objectLock.readLock().unlock();
    }

    // TODO: set timeout
    //
    writeFuture.join();

    if (!writeFuture.isWritten())
    {
      throw new RTIinternalError("error communicating with RTI");
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

  public void changeAttributeOrderType(Set<AttributeHandle> attributeHandles,
                                       OrderType orderType)
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

  public void changeAttributeTransportationType(
    Set<AttributeHandle> attributeHandles,
    TransportationType transportationType)
    throws AttributeNotDefined, AttributeNotOwned
  {
    objectLock.readLock().lock();
    try
    {
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        getAttributeInstance(attributeHandle).setTransportationType(
          transportationType);
      }
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public WriteFuture requestAttributeValueUpdate(
    AttributeHandleSet attributeHandles, byte[] tag, IoSession rtiSession)
    throws AttributeNotDefined
  {
    objectClass.checkIfAttributeNotDefined(attributeHandles);

    attributeHandles = new IEEE1516AttributeHandleSet(attributeHandles);

    objectLock.readLock().lock();
    try
    {
      // only request updates for un-owned attributes
      //
      attributeHandles.removeAll(attributes.keySet());

      return rtiSession.write(new RequestAttributeValueUpdate(
        objectInstanceHandle, attributeHandles, tag));
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void associateRegionsForUpdates(
    AttributeRegionAssociation attributeRegionAssociation)
  {
    objectLock.readLock().lock();
    try
    {
      for (AttributeHandle attributeHandle : attributeRegionAssociation.attributes)
      {
        FederateAttributeInstance attribute = attributes.get(attributeHandle);
        if (attribute != null)
        {
          attribute.associateRegionsForUpdates(
            attributeRegionAssociation.regions);
        }
      }
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void unassociateRegionsForUpdates(
    AttributeRegionAssociation attributeRegionAssociation)
  {
    objectLock.readLock().lock();
    try
    {
      for (AttributeHandle attributeHandle : attributeRegionAssociation.attributes)
      {
        FederateAttributeInstance attribute = attributes.get(attributeHandle);
        if (attribute != null)
        {
          attribute.unassociateRegionsForUpdates(
            attributeRegionAssociation.regions);
        }
      }
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void unpublishObjectClassAttributes(
    AttributeHandleSet attributeHandles, IoSession rtiSession)
    throws RTIinternalError
  {
    WriteFuture writeFuture;

    objectLock.writeLock().lock();
    try
    {
      attributes.keySet().removeAll(attributeHandles);

      writeFuture = rtiSession.write(
        new UnconditionalAttributeOwnershipDivestiture(
          objectInstanceHandle, attributeHandles));
    }
    finally
    {
      objectLock.writeLock().unlock();
    }

    // TODO: set timeout
    //
    writeFuture.join();

    if (!writeFuture.isWritten())
    {
      throw new RTIinternalError("error communicating with RTI");
    }
  }

  public void checkIfOwnershipAcquisitionPending()
    throws OwnershipAcquisitionPending
  {
    objectLock.writeLock().lock();
    try
    {
      if (!attributeHandlesBeingAcquired.isEmpty() ||
          !attributeHandlesBeingAcquiredIfAvailable.isEmpty())
      {
        AttributeHandleSet allAttributeHandlesBeingAcquired =
          new IEEE1516AttributeHandleSet(attributeHandlesBeingAcquired);
        allAttributeHandlesBeingAcquired.addAll(
          attributeHandlesBeingAcquiredIfAvailable);
        throw new OwnershipAcquisitionPending(
          allAttributeHandlesBeingAcquired.toString());
      }
    }
    finally
    {
      objectLock.writeLock().unlock();
    }
  }

  public void checkIfOwnershipAcquisitionPending(
    Set<AttributeHandle> attributeHandles)
    throws OwnershipAcquisitionPending
  {
    objectLock.writeLock().lock();
    try
    {
      AttributeHandleSet allAttributeHandlesBeingAcquired =
        new IEEE1516AttributeHandleSet();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        if (attributeHandlesBeingAcquired.contains(attributeHandle) ||
            attributeHandlesBeingAcquiredIfAvailable.contains(attributeHandle))
        {
          allAttributeHandlesBeingAcquired.add(attributeHandle);
        }
      }

      if (!allAttributeHandlesBeingAcquired.isEmpty())
      {
        throw new OwnershipAcquisitionPending(
          allAttributeHandlesBeingAcquired.toString());
      }
    }
    finally
    {
      objectLock.writeLock().unlock();
    }
  }

  public void checkIfFederateOwnsAttributes()
    throws FederateOwnsAttributes
  {
    objectLock.readLock().lock();
    try
    {
      if (!attributes.isEmpty())
      {
        throw new FederateOwnsAttributes(String.format(
          "%s - %s", objectInstanceHandle, attributes.keySet()));
      }
    }
    finally
    {
      objectLock.readLock().unlock();
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
    return String.format("%s (%s, %s)", objectInstanceHandle, name,
                         objectClass);
  }

  protected boolean equals(FederateObjectInstance rhs)
  {
    return objectInstanceHandle.equals(rhs.objectInstanceHandle);
  }

  protected FederateAttributeInstance getAttributeInstance(
    AttributeHandle attributeHandle)
    throws AttributeNotDefined, AttributeNotOwned
  {
    FederateAttributeInstance attributeInstance = attributes.get(attributeHandle);
    if (attributeInstance == null)
    {
      // it might not be defined
      //
      objectClass.checkIfAttributeNotDefined(attributeHandle);

      throw new AttributeNotOwned(String.format("%s", attributeHandle));
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

  protected void checkIfAttributeAlreadyBeingDivested(
    Set<AttributeHandle> attributeHandles)
    throws AttributeNotDefined, AttributeNotOwned, AttributeAlreadyBeingDivested
  {
    for (AttributeHandle attributeHandle : attributeHandles)
    {
      getAttributeInstance(
        attributeHandle).checkIfAttributeAlreadyBeingDivested();
    }
  }

  protected void checkIfAttributeDivestitureWasNotRequested(
    Set<AttributeHandle> attributeHandles)
    throws AttributeNotDefined, AttributeNotOwned,
           AttributeDivestitureWasNotRequested
  {
    for (AttributeHandle attributeHandle : attributeHandles)
    {
      getAttributeInstance(
        attributeHandle).checkIfAttributeDivestitureWasNotRequested();
    }
  }

  protected void checkIfFederateOwnsAttributes(
    Set<AttributeHandle> attributeHandles)
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

  public void discoverObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                                     ObjectClassHandle objectClassHandle,
                                     String name,
                                     FederateAmbassador federateAmbassador)
    throws ObjectClassNotRecognized, CouldNotDiscover, FederateInternalError
  {
    objectLock.readLock().lock();
    try
    {
      federateAmbassador.discoverObjectInstance(
        objectInstanceHandle, objectClassHandle, name);
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void reflectAttributeValues(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleValueMap attributeValues, byte[] tag,
    OrderType sentOrderType, TransportationType transportationType,
    LogicalTime updateTime, OrderType receivedOrderType,
    MessageRetractionHandle messageRetractionHandle,
    RegionHandleSet sentRegionHandles, FederateAmbassador federateAmbassador)
    throws AttributeNotSubscribed, InvalidLogicalTime, FederateInternalError,
           AttributeNotRecognized, ObjectInstanceNotKnown
  {
    objectLock.readLock().lock();
    try
    {
      if (updateTime == null)
      {
        if (sentRegionHandles == null)
        {
          federateAmbassador.reflectAttributeValues(
            objectInstanceHandle, attributeValues, tag, sentOrderType,
            transportationType);
        }
        else
        {
          federateAmbassador.reflectAttributeValues(
            objectInstanceHandle, attributeValues, tag, sentOrderType,
            transportationType, sentRegionHandles);
        }
      }
      else if (messageRetractionHandle == null)
      {
        if (sentRegionHandles == null)
        {
          federateAmbassador.reflectAttributeValues(
            objectInstanceHandle, attributeValues, tag, sentOrderType,
            transportationType, updateTime, receivedOrderType);
        }
        else
        {
          federateAmbassador.reflectAttributeValues(
            objectInstanceHandle, attributeValues, tag, sentOrderType,
            transportationType, updateTime, receivedOrderType,
            sentRegionHandles);
        }
      }
      else if (sentRegionHandles == null)
      {
        federateAmbassador.reflectAttributeValues(
          objectInstanceHandle, attributeValues, tag, sentOrderType,
          transportationType, updateTime, receivedOrderType,
          messageRetractionHandle);
      }
      else
      {
        federateAmbassador.reflectAttributeValues(
          objectInstanceHandle, attributeValues, tag, sentOrderType,
          transportationType, updateTime, receivedOrderType,
          messageRetractionHandle, sentRegionHandles);
      }
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void removeObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag,
    OrderType sentOrderType, LogicalTime deleteTime,
    OrderType receivedOrderType,
    MessageRetractionHandle messageRetractionHandle,
    FederateAmbassador federateAmbassador)
    throws InvalidLogicalTime, FederateInternalError, ObjectInstanceNotKnown
  {
    objectLock.readLock().lock();
    try
    {
      Attribute attribute = objectClass.getAttributesByName().get(
        Attribute.HLA_PRIVILEGE_TO_DELETE_OBJECT);
      assert attribute != null;
      if (!attributes.containsKey(attribute.getAttributeHandle()))
      {
        if (deleteTime == null)
        {
          federateAmbassador.removeObjectInstance(
            objectInstanceHandle, tag, sentOrderType);
        }
        else if (messageRetractionHandle == null)
        {
          federateAmbassador.removeObjectInstance(
            objectInstanceHandle, tag, sentOrderType, deleteTime,
            receivedOrderType);
        }
        else
        {
          federateAmbassador.removeObjectInstance(
            objectInstanceHandle, tag, sentOrderType, deleteTime,
            receivedOrderType, messageRetractionHandle);
        }
      }
    }
    finally
    {
      objectLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisitionNotification(
    AttributeHandleSet attributeHandles, byte[] tag,
    FederateAmbassador federateAmbassador)
    throws AttributeAlreadyOwned, FederateInternalError,
           AttributeNotRecognized, ObjectInstanceNotKnown,
           AttributeNotPublished, AttributeAcquisitionWasNotRequested
  {
    objectLock.writeLock().lock();
    try
    {
      attributeHandlesBeingAcquired.removeAll(attributeHandles);

      for (AttributeHandle attributeHandle : attributeHandles)
      {
        Attribute attribute = objectClass.getAttributes().get(attributeHandle);
        assert attribute != null;

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
}
