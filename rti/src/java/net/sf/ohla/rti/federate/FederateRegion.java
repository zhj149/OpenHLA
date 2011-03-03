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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.fdd.Attribute;
import net.sf.ohla.rti.fdd.FDD;
import net.sf.ohla.rti.fdd.InteractionClass;
import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandleSetFactory;
import net.sf.ohla.rti.i18n.ExceptionMessages;
import net.sf.ohla.rti.i18n.I18n;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.DimensionHandle;
import hla.rti1516e.DimensionHandleSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RangeBounds;
import hla.rti1516e.RegionHandle;
import hla.rti1516e.exceptions.InvalidRegionContext;
import hla.rti1516e.exceptions.RegionDoesNotContainSpecifiedDimension;
import hla.rti1516e.exceptions.RegionInUseForUpdateOrSubscription;
import hla.rti1516e.exceptions.RegionNotCreatedByThisFederate;

public class FederateRegion
{
  private final RegionHandle regionHandle;
  private final DimensionHandleSet dimensionHandles;

  private final ReadWriteLock rangeBoundsLock = new ReentrantReadWriteLock(true);
  private final Map<DimensionHandle, RangeBounds> rangeBounds = new HashMap<DimensionHandle, RangeBounds>();
  private Map<DimensionHandle, RangeBounds> uncommittedRangeBounds = new HashMap<DimensionHandle, RangeBounds>();

  private final ReadWriteLock associatedObjectsLock = new ReentrantReadWriteLock(true);
  private final Map<ObjectInstanceHandle, AttributeHandleSet> associatedObjects =
    new HashMap<ObjectInstanceHandle, AttributeHandleSet>();

  private final ReadWriteLock subscriptionLock = new ReentrantReadWriteLock(true);
  private final Map<ObjectClassHandle, AttributeHandleSet> subscribedObjectClasses =
    new HashMap<ObjectClassHandle, AttributeHandleSet>();

  private final Set<InteractionClassHandle> subscribedInteractionClasses =
    new HashSet<InteractionClassHandle>();

  public FederateRegion(RegionHandle regionHandle, DimensionHandleSet dimensionHandles, FDD fdd)
  {
    this.regionHandle = regionHandle;
    this.dimensionHandles = dimensionHandles;

    // initialize our range bounds to the dimension defaults
    //
    for (DimensionHandle dimensionHandle : dimensionHandles)
    {
      rangeBounds.put(dimensionHandle, new RangeBounds(0, fdd.getDimensionSafely(dimensionHandle).getUpperBound()));
    }
  }

  public FederateRegion(RegionHandle regionHandle, Map<DimensionHandle, RangeBounds> rangeBounds)
  {
    this.regionHandle = regionHandle;
    this.rangeBounds.putAll(rangeBounds);

    dimensionHandles = IEEE1516eDimensionHandleSetFactory.INSTANCE.create(rangeBounds.keySet());
  }

  public RegionHandle getRegionHandle()
  {
    return regionHandle;
  }

  public DimensionHandleSet getDimensionHandles()
  {
    return dimensionHandles;
  }

  public RangeBounds getRangeBounds(DimensionHandle dimensionHandle)
    throws RegionDoesNotContainSpecifiedDimension
  {
    rangeBoundsLock.readLock().lock();
    try
    {
      RangeBounds rangeBounds = this.rangeBounds.get(dimensionHandle);
      if (rangeBounds == null)
      {
        throw new RegionDoesNotContainSpecifiedDimension(I18n.getMessage(
          ExceptionMessages.REGION_DOES_NOT_CONTAIN_SPECIFIED_DIMENSION, regionHandle, dimensionHandle));
      }
      return clone(rangeBounds);
    }
    finally
    {
      rangeBoundsLock.readLock().unlock();
    }
  }

  public void setRangeBounds(DimensionHandle dimensionHandle, RangeBounds rangeBounds)
    throws RegionDoesNotContainSpecifiedDimension
  {
    rangeBoundsLock.writeLock().lock();
    try
    {
      // make sure we have this range bound
      //
      if (!this.rangeBounds.containsKey(dimensionHandle))
      {
        throw new RegionDoesNotContainSpecifiedDimension(I18n.getMessage(
          ExceptionMessages.REGION_DOES_NOT_CONTAIN_SPECIFIED_DIMENSION, regionHandle, dimensionHandle));
      }

      // hold onto it until a commit occurs
      //
      uncommittedRangeBounds.put(dimensionHandle, clone(rangeBounds));
    }
    finally
    {
      rangeBoundsLock.writeLock().unlock();
    }
  }

  public AttributeHandleSet getAssociatedAttributeHandles(ObjectInstanceHandle objectInstanceHandle)
  {
    return associatedObjects.get(objectInstanceHandle);
  }

  public Map<DimensionHandle, RangeBounds> commit()
  {
    rangeBoundsLock.writeLock().lock();
    try
    {
      Map<DimensionHandle, RangeBounds> committedRangeBounds = uncommittedRangeBounds;

      rangeBounds.putAll(uncommittedRangeBounds);

      // start fresh
      //
      uncommittedRangeBounds = new HashMap<DimensionHandle, RangeBounds>();

      return committedRangeBounds;
    }
    finally
    {
      rangeBoundsLock.writeLock().unlock();
    }
  }

  public void checkIfInUse()
    throws RegionInUseForUpdateOrSubscription
  {
    if (!associatedObjects.isEmpty() || !subscribedObjectClasses.isEmpty() || !subscribedInteractionClasses.isEmpty())
    {
      throw new RegionInUseForUpdateOrSubscription(I18n.getMessage(
        ExceptionMessages.REGION_IN_USE_FOR_UPDATE_OR_SUBSCRIPTION, regionHandle));
    }
  }

  public void checkIfInvalidRegionContext(ObjectClass objectClass, AttributeHandleSet attributeHandles)
    throws InvalidRegionContext
  {
    for (AttributeHandle attributeHandle : attributeHandles)
    {
      Attribute attribute = objectClass.getAttributeSafely(attributeHandle);
      if (!attribute.getDimensionHandles().containsAll(dimensionHandles))
      {
        throw new InvalidRegionContext(I18n.getMessage(
          ExceptionMessages.INVALID_REGION_CONTEXT, regionHandle, dimensionHandles, attribute.getDimensionHandles()));
      }
    }
  }

  public void checkIfInvalidRegionContext(InteractionClass interactionClass)
    throws InvalidRegionContext
  {
    if (!interactionClass.getDimensionHandles().containsAll(dimensionHandles))
    {
      throw new InvalidRegionContext(I18n.getMessage(
        ExceptionMessages.INVALID_REGION_CONTEXT, regionHandle, dimensionHandles,
        interactionClass.getDimensionHandles()));
    }
  }

  public void associateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
  {
    associatedObjectsLock.writeLock().lock();
    try
    {
      AttributeHandleSet existingAttributeHandles = associatedObjects.get(objectInstanceHandle);
      if (existingAttributeHandles == null)
      {
        associatedObjects.put(objectInstanceHandle, attributeHandles.clone());
      }
      else
      {
        existingAttributeHandles.addAll(attributeHandles);
      }
    }
    finally
    {
      associatedObjectsLock.writeLock().unlock();
    }
  }

  public void unassociateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
  {
    associatedObjectsLock.writeLock().lock();
    try
    {
      AttributeHandleSet existingAttributeHandles = associatedObjects.get(objectInstanceHandle);
      if (existingAttributeHandles != null)
      {
        existingAttributeHandles.removeAll(attributeHandles);

        if (existingAttributeHandles.isEmpty())
        {
          associatedObjects.remove(objectInstanceHandle);
        }
      }
    }
    finally
    {
      associatedObjectsLock.writeLock().unlock();
    }
  }

  public void subscribe(ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
  {
    subscriptionLock.writeLock().lock();
    try
    {
      AttributeHandleSet existingAttributeHandles = subscribedObjectClasses.get(objectClassHandle);
      if (existingAttributeHandles == null)
      {
        subscribedObjectClasses.put(objectClassHandle, attributeHandles.clone());
      }
      else
      {
        existingAttributeHandles.addAll(attributeHandles);
      }
    }
    finally
    {
      subscriptionLock.writeLock().unlock();
    }
  }

  public void unsubscribe(ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
  {
    subscriptionLock.writeLock().lock();
    try
    {
      AttributeHandleSet existingAttributeHandles = subscribedObjectClasses.get(objectClassHandle);
      if (existingAttributeHandles != null)
      {
        existingAttributeHandles.removeAll(attributeHandles);

        if (existingAttributeHandles.isEmpty())
        {
          subscribedObjectClasses.remove(objectClassHandle);
        }
      }
    }
    finally
    {
      subscriptionLock.writeLock().unlock();
    }
  }

  public void subscribe(InteractionClassHandle interactionClassHandle)
    throws RegionNotCreatedByThisFederate
  {
    subscriptionLock.writeLock().lock();
    try
    {
      subscribedInteractionClasses.add(interactionClassHandle);
    }
    finally
    {
      subscriptionLock.writeLock().unlock();
    }
  }

  public void unsubscribe(InteractionClassHandle interactionClassHandle)
    throws RegionNotCreatedByThisFederate
  {
    subscribedInteractionClasses.remove(interactionClassHandle);
  }

  public boolean intersects(Map<DimensionHandle, RangeBounds> rangeBounds)
  {
    boolean intersect = false;

    for (Iterator<Map.Entry<DimensionHandle, RangeBounds>> i =
      rangeBounds.entrySet().iterator(); i.hasNext() && !intersect;)
    {
      Map.Entry<DimensionHandle, RangeBounds> entry = i.next();

      RangeBounds rb = this.rangeBounds.get(entry.getKey());
      intersect = rb != null && intersects(rb, entry.getValue());
    }

    return intersect;
  }

  private boolean intersects(RangeBounds lhs, RangeBounds rhs)
  {
    return (lhs.lower < rhs.upper && rhs.lower < lhs.upper) ||
           lhs.lower == rhs.lower;
  }

  private RangeBounds clone(RangeBounds rangeBounds)
  {
    return new RangeBounds(rangeBounds.lower, rangeBounds.upper);
  }
}
