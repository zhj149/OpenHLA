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

package net.sf.ohla.rti.federation;

import java.io.Serializable;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.fdd.Attribute;
import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.hla.rti1516.IEEE1516AttributeHandleSet;
import net.sf.ohla.rti.hla.rti1516.IEEE1516ObjectInstanceHandle;
import net.sf.ohla.rti.hla.rti1516.IEEE1516RegionHandleSet;
import net.sf.ohla.rti.messages.callbacks.AttributeIsNotOwned;
import net.sf.ohla.rti.messages.callbacks.AttributeIsOwnedByRTI;
import net.sf.ohla.rti.messages.callbacks.AttributeOwnershipAcquisitionNotification;
import net.sf.ohla.rti.messages.callbacks.AttributeOwnershipUnavailable;
import net.sf.ohla.rti.messages.callbacks.ConfirmAttributeOwnershipAcquisitionCancellation;
import net.sf.ohla.rti.messages.callbacks.DiscoverObjectInstance;
import net.sf.ohla.rti.messages.callbacks.InformAttributeOwnership;
import net.sf.ohla.rti.messages.callbacks.ObjectInstanceNameReservationFailed;
import net.sf.ohla.rti.messages.callbacks.ObjectInstanceNameReservationSucceeded;
import net.sf.ohla.rti.messages.callbacks.ReflectAttributeValues;
import net.sf.ohla.rti.messages.callbacks.RequestAttributeOwnershipRelease;
import net.sf.ohla.rti.messages.callbacks.RequestDivestitureConfirmation;

import org.apache.mina.common.WriteFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import hla.rti1516.AttributeAlreadyBeingDivested;
import hla.rti1516.AttributeDivestitureWasNotRequested;
import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.AttributeSetRegionSetPairList;
import hla.rti1516.LogicalTime;
import hla.rti1516.MessageRetractionHandle;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.OrderType;
import hla.rti1516.RegionHandleSet;
import hla.rti1516.TransportationType;

public class FederationExecutionObjectManager
{
  protected final FederationExecution federationExecution;

  protected AtomicInteger objectInstanceCount =
    new AtomicInteger(Integer.MIN_VALUE);

  protected ConcurrentMap<String, FederateProxy> reservedObjectInstanceNames =
    new ConcurrentHashMap<String, FederateProxy>();

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

  protected final Logger log = LoggerFactory.getLogger(getClass());
  protected final Marker marker;

  public FederationExecutionObjectManager(FederationExecution federationExecution)
  {
    this.federationExecution = federationExecution;

    marker = federationExecution.getMarker();
  }

  public void subscribeObjectClassAttributes(
    FederateProxy federateProxy, ObjectClass objectClass,
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

          lastWriteFuture = federateProxy.discoverObjectInstance(
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

  public void reserveObjectInstanceName(FederateProxy federateProxy, String name)
  {
    FederateProxy reservingFederateProxy =
      reservedObjectInstanceNames.putIfAbsent(name, federateProxy);
    if (reservingFederateProxy != null)
    {
      log.debug(marker, "object instance name already reserved: {} by {}", name,
                reservingFederateProxy);

      federateProxy.getSession().write(
        new ObjectInstanceNameReservationFailed(name));
    }
    else
    {
      log.debug(marker, "object instance name reserved: {} by {}",
                name, federateProxy);

      federateProxy.getSession().write(
        new ObjectInstanceNameReservationSucceeded(name));
    }
  }

  public ObjectInstanceHandle registerObjectInstance(
    FederateProxy federateProxy, ObjectClassHandle objectClassHandle,
    Set<AttributeHandle> publishedAttributeHandles, String name)
  {
    ObjectInstanceHandle objectInstanceHandle = nextObjectInstanceHandle();

    ObjectClass objectClass =
      federationExecution.getFDD().getObjectClasses().get(objectClassHandle);
    assert objectClass != null;

    assert name == null || federateProxy.equals(
      reservedObjectInstanceNames.get(name));

    ObjectInstance objectInstance = new ObjectInstance(
      objectInstanceHandle, objectClass, name,
      publishedAttributeHandles, federateProxy);

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
    FederateProxy federateProxy, ObjectInstanceHandle objectInstanceHandle,
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
          for (FederateProxy f : federationExecution.getFederates().values())
          {
            if (f != federateProxy)
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
    FederateProxy owner, ObjectInstanceHandle objectInstanceHandle,
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
    FederateProxy owner, ObjectInstanceHandle objectInstanceHandle,
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
    FederateProxy owner, ObjectInstanceHandle objectInstanceHandle,
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
    FederateProxy acquiree, ObjectInstanceHandle objectInstanceHandle,
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
    FederateProxy acquiree, ObjectInstanceHandle objectInstanceHandle,
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

  public Map<AttributeHandle, FederateProxy> attributeOwnershipDivestitureIfWanted(
    FederateProxy owner, ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
  {
    objectsLock.readLock().lock();
    try
    {
      ObjectInstance objectInstance = objects.get(objectInstanceHandle);
      return objectInstance != null ?
        objectInstance.attributeOwnershipDivestitureIfWanted(
          owner, attributeHandles) :
        (Map<AttributeHandle, FederateProxy>) Collections.EMPTY_MAP;
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture(
    FederateProxy owner, ObjectInstanceHandle objectInstanceHandle,
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
    FederateProxy acquiree, ObjectInstanceHandle objectInstanceHandle,
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
    FederateProxy federateProxy, ObjectInstanceHandle objectInstanceHandle,
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
        objectInstance.queryAttributeOwnership(federateProxy, attributeHandle);
      }
    }
    finally
    {
      objectsLock.readLock().unlock();
    }
  }

  protected ObjectInstanceHandle nextObjectInstanceHandle()
  {
    return new IEEE1516ObjectInstanceHandle(objectInstanceCount.incrementAndGet());
  }

  public static class ObjectInstance
    implements Serializable
  {
    protected final ObjectInstanceHandle objectInstanceHandle;
    protected final ObjectClass objectClass;

    protected final String name;

    protected Lock objectLock = new ReentrantLock(true);

    protected Map<AttributeHandle, AttributeInstance> attributes =
      new HashMap<AttributeHandle, AttributeInstance>();

    public ObjectInstance(
      ObjectInstanceHandle objectInstanceHandle, ObjectClass objectClass,
      String name, Set<AttributeHandle> publishedAttributeHandles,
      FederateProxy owner)
    {
      this.objectInstanceHandle = objectInstanceHandle;
      this.objectClass = objectClass;
      this.name = name;

      for (Attribute attribute : objectClass.getAttributes().values())
      {
        AttributeInstance attributeInstance = new AttributeInstance(attribute);
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

    public FederateProxy getOwner(AttributeHandle attributeHandle)
    {
      return attributes.get(attributeHandle).getOwner();
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
          AttributeInstance attributeInstance =
            attributes.get(attributeHandle);

          FederateProxy owner =
            attributeInstance.attributeOwnershipAcquisition(acquiree);
          if (acquiree.equals(owner))
          {
            // the attribute was unowned and therefore immediately acquired
            //
            acquiredAttributeHandles.add(attributeHandle);
          }
          else if (attributeInstance.wantsToDivest())
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
        AttributeInstance attributeInstance =
          attributes.get(attributeHandle);
        assert attributeInstance != null;

        FederateProxy owner = attributeInstance.getOwner();
        if (owner == null)
        {
          federateProxy.getSession().write(new AttributeIsNotOwned(
            objectInstanceHandle, attributeHandle));
        }
        else if (attributeInstance.getAttribute().isMOM())
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

    public static class AttributeInstance
      implements Serializable
    {
      protected final Attribute attribute;

      protected TransportationType transportationType;
      protected OrderType orderType;

      protected RegionHandleSet associatedRegions = new IEEE1516RegionHandleSet();

      protected FederateProxy owner;

      /**
       * Set if the owner of this attribute is willing to divest ownership.
       */
      protected boolean wantsToDivest;

      /**
       * The 'ownership' line. When federates request ownership of this attribute
       * they are placed into a line and given ownership based upon when they
       * entered the line.
       */
      protected LinkedHashSet<FederateProxy> requestingOwnerships =
        new LinkedHashSet<FederateProxy>();

      public AttributeInstance(Attribute attribute)
      {
        this.attribute = attribute;

        transportationType = attribute.getTransportationType();
        orderType = attribute.getOrderType();
      }

      public Attribute getAttribute()
      {
        return attribute;
      }

      public AttributeHandle getAttributeHandle()
      {
        return attribute.getAttributeHandle();
      }

      public TransportationType getTransportationType()
      {
        return transportationType;
      }

      public void setTransportationType(TransportationType transportationType)
      {
        this.transportationType = transportationType;
      }

      public OrderType getOrderType()
      {
        return orderType;
      }

      public void setOrderType(OrderType orderType)
      {
        this.orderType = orderType;
      }

      public RegionHandleSet getAssociatedRegions()
      {
        return associatedRegions;
      }

      public void associateRegionsForUpdates(RegionHandleSet regionHandles)
      {
        associatedRegions.addAll(regionHandles);
      }

      public void unassociateRegionsForUpdates(RegionHandleSet regionHandles)
      {
        associatedRegions.removeAll(regionHandles);
      }

      public FederateProxy getOwner()
      {
        return owner;
      }

      public void setOwner(FederateProxy owner)
      {
        this.owner = owner;
      }

      public boolean wantsToDivest()
      {
        return wantsToDivest;
      }

      public void checkIfAttributeDivestitureWasNotRequested()
        throws AttributeDivestitureWasNotRequested
      {
        // TODO: check status
      }

      public void checkIfAttributeAlreadyBeingDivested()
        throws AttributeAlreadyBeingDivested
      {
        // TODO: check status
      }

      public FederateProxy unconditionalAttributeOwnershipDivestiture()
      {
        owner = null;
        wantsToDivest = false;

        // give ownership to the next in line
        //
        if (!requestingOwnerships.isEmpty())
        {
          Iterator<FederateProxy> i = requestingOwnerships.iterator();
          owner = i.next();
          i.remove();
        }

        return owner;
      }

      public boolean negotiatedAttributeOwnershipDivestiture(byte[] tag)
      {
        wantsToDivest = true;

        return !requestingOwnerships.isEmpty();
      }

      public FederateProxy confirmDivestiture()
      {
        owner = null;
        wantsToDivest = false;

        // give ownership to the next in line
        //
        if (!requestingOwnerships.isEmpty())
        {
          Iterator<FederateProxy> i = requestingOwnerships.iterator();
          owner = i.next();
          i.remove();
        }

        return owner;
      }

      public boolean attributeOwnershipAcquisitionIfAvailable(
        FederateProxy acquiree)
      {
        if (owner == null)
        {
          // acquire this attribute if it is unowned
          //
          owner = acquiree;
          wantsToDivest = false;
        }
        return owner == acquiree;
      }

      public FederateProxy attributeOwnershipAcquisition(FederateProxy acquiree)
      {
        if (!attributeOwnershipAcquisitionIfAvailable(acquiree))
        {
          // get in line
          //
          requestingOwnerships.add(acquiree);
        }

        return owner;
      }

      public FederateProxy attributeOwnershipDivestitureIfWanted()
      {
        boolean divested = !requestingOwnerships.isEmpty();

        // give ownership to the next in line
        //
        if (divested)
        {
          Iterator<FederateProxy> i = requestingOwnerships.iterator();
          owner = i.next();
          i.remove();

          wantsToDivest = false;
        }

        return divested ? owner : null;
      }

      public boolean cancelAttributeOwnershipAcquisition(FederateProxy acquiree)
      {
        return requestingOwnerships.remove(acquiree);
      }

      public void cancelNegotiatedAttributeOwnershipDivestiture(
        FederateProxy owner)
      {
        if (owner.equals(this.owner))
        {
          wantsToDivest = false;
        }
      }
    }
  }
}
