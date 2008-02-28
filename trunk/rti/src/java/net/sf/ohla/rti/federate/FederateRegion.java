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

import net.sf.ohla.rti.fdd.FDD;

import hla.rti1516.AttributeHandleSet;
import hla.rti1516.DimensionHandle;
import hla.rti1516.DimensionHandleSet;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.InvalidDimensionHandle;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.RTIinternalError;
import hla.rti1516.RangeBounds;
import hla.rti1516.RegionDoesNotContainSpecifiedDimension;
import hla.rti1516.RegionHandle;
import hla.rti1516.RegionInUseForUpdateOrSubscription;
import hla.rti1516.RegionNotCreatedByThisFederate;
import hla.rti1516.AttributeHandleSetFactory;

public class FederateRegion
{
  protected final RegionHandle regionHandle;
  protected final DimensionHandleSet dimensionHandles;

  protected final AttributeHandleSetFactory attributeHandleSetFactory;

  protected final ReadWriteLock rangeBoundsLock =
    new ReentrantReadWriteLock(true);
  protected final Map<DimensionHandle, RangeBounds> rangeBounds =
    new HashMap<DimensionHandle, RangeBounds>();
  protected Map<DimensionHandle, RangeBounds> uncommittedRangeBounds =
    new HashMap<DimensionHandle, RangeBounds>();

  protected final ReadWriteLock associatedObjectsLock =
    new ReentrantReadWriteLock(true);
  protected final Map<ObjectInstanceHandle, AttributeHandleSet> associatedObjects =
    new HashMap<ObjectInstanceHandle, AttributeHandleSet>();

  protected final ReadWriteLock subscriptionLock =
    new ReentrantReadWriteLock(true);

  protected final Map<ObjectClassHandle, AttributeHandleSet> subscribedObjectClasses =
    new HashMap<ObjectClassHandle, AttributeHandleSet>();

  protected final Set<InteractionClassHandle> subscribedInteractionClasses =
    new HashSet<InteractionClassHandle>();

  public FederateRegion(RegionHandle regionHandle, DimensionHandleSet dimensionHandles,
                FDD fdd, AttributeHandleSetFactory attributeHandleSetFactory)
    throws RTIinternalError
  {
    this.regionHandle = regionHandle;
    this.dimensionHandles = dimensionHandles;
    this.attributeHandleSetFactory = attributeHandleSetFactory;

    // initialize our range bounds to the dimension defaults
    //
    for (DimensionHandle dimensionHandle : dimensionHandles)
    {
      try
      {
        rangeBounds.put(dimensionHandle, fdd.getRangeBounds(dimensionHandle));
      }
      catch (InvalidDimensionHandle idh)
      {
        // should never occur
        //
        throw new RTIinternalError("unexpected exception", idh);
      }
    }
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
        throw new RegionDoesNotContainSpecifiedDimension(dimensionHandle);
      }
      return clone(rangeBounds);
    }
    finally
    {
      rangeBoundsLock.readLock().unlock();
    }
  }

  public void setRangeBounds(DimensionHandle dimensionHandle,
                             RangeBounds rangeBounds)
    throws RegionDoesNotContainSpecifiedDimension
  {
    // make sure we have this range bound
    //
    getRangeBounds(dimensionHandle);

    // hold onto it until a commit occurs
    //
    uncommittedRangeBounds.put(dimensionHandle, clone(rangeBounds));
  }

  public AttributeHandleSet getAssociatedAttributeHandles(
    ObjectInstanceHandle objectInstanceHandle)
  {
    return associatedObjects.get(objectInstanceHandle);
  }

  public Map<DimensionHandle, RangeBounds> commit()
  {
    Map<DimensionHandle, RangeBounds> committedRangeBounds =
      uncommittedRangeBounds;

    update(uncommittedRangeBounds);

    // start fresh
    //
    uncommittedRangeBounds = new HashMap<DimensionHandle, RangeBounds>();

    return committedRangeBounds;
  }

  public void update(Map<DimensionHandle, RangeBounds> rangeBounds)
  {
    this.rangeBounds.putAll(rangeBounds);
  }

  public void checkIfInUse()
    throws RegionInUseForUpdateOrSubscription
  {
    if (!associatedObjects.isEmpty() || !subscribedObjectClasses.isEmpty() ||
        !subscribedInteractionClasses.isEmpty())
    {
      throw new RegionInUseForUpdateOrSubscription(String.format(
        "associated objects(%d), subscribed object classes(%d), subscribed interaction classes(%d)",
        associatedObjects.size(), subscribedObjectClasses.size(),
        subscribedInteractionClasses.size()));
    }
  }

  public void associateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
  {
    AttributeHandleSet existingAttributeHandles =
      associatedObjects.get(objectInstanceHandle);
    if (existingAttributeHandles == null)
    {
      existingAttributeHandles = attributeHandleSetFactory.create();
      associatedObjects.put(objectInstanceHandle, existingAttributeHandles);
    }

    existingAttributeHandles.addAll(attributeHandles);
  }

  public void unassociateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
  {
    AttributeHandleSet existingAttributeHandles =
      associatedObjects.get(objectInstanceHandle);
    if (existingAttributeHandles != null)
    {
      existingAttributeHandles.removeAll(attributeHandles);

      if (existingAttributeHandles.isEmpty())
      {
        associatedObjects.remove(objectInstanceHandle);
      }
    }
  }

  public void subscribe(ObjectClassHandle objectClassHandle,
                        AttributeHandleSet attributeHandles)
    throws RegionNotCreatedByThisFederate
  {
    AttributeHandleSet existingAttributeHandles =
      subscribedObjectClasses.get(objectClassHandle);
    if (existingAttributeHandles == null)
    {
      existingAttributeHandles = attributeHandleSetFactory.create();
      subscribedObjectClasses.put(
        objectClassHandle, existingAttributeHandles);
    }

    existingAttributeHandles.addAll(attributeHandles);
  }

  public void unsubscribe(ObjectClassHandle objectClassHandle,
                          AttributeHandleSet attributeHandles)
    throws RegionNotCreatedByThisFederate
  {
    AttributeHandleSet existingAttributeHandles =
      subscribedObjectClasses.get(objectClassHandle);
    if (existingAttributeHandles != null)
    {
      existingAttributeHandles.removeAll(attributeHandles);

      if (existingAttributeHandles.isEmpty())
      {
        subscribedObjectClasses.remove(objectClassHandle);
      }
    }
  }

  public void subscribe(InteractionClassHandle interactionClassHandle)
    throws RegionNotCreatedByThisFederate
  {
    subscribedInteractionClasses.add(interactionClassHandle);
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

  protected boolean intersects(RangeBounds lhs, RangeBounds rhs)
  {
    return (lhs.lower < rhs.upper && rhs.lower < lhs.upper) ||
           lhs.lower == rhs.lower;
  }

  protected RangeBounds clone(RangeBounds rangeBounds)
  {
    RangeBounds newRangeBounds = new RangeBounds();
    newRangeBounds.lower = rangeBounds.lower;
    newRangeBounds.upper = rangeBounds.upper;
    return newRangeBounds;
  }
}
