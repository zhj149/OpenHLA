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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eRegionHandle;
import net.sf.ohla.rti.messages.CommitRegionModifications;
import net.sf.ohla.rti.messages.CreateRegion;
import net.sf.ohla.rti.messages.DeleteRegion;
import net.sf.ohla.rti.messages.SubscribeInteractionClassWithRegions;
import net.sf.ohla.rti.messages.SubscribeObjectClassAttributesWithRegions;
import net.sf.ohla.rti.messages.UnsubscribeInteractionClassWithRegions;
import net.sf.ohla.rti.messages.UnsubscribeObjectClassAttributesWithRegions;

import hla.rti1516e.AttributeRegionAssociation;
import hla.rti1516e.AttributeSetRegionSetPairList;
import hla.rti1516e.DimensionHandle;
import hla.rti1516e.DimensionHandleSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RangeBounds;
import hla.rti1516e.RegionHandle;
import hla.rti1516e.RegionHandleSet;
import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.InteractionClassNotDefined;
import hla.rti1516e.exceptions.InteractionClassNotPublished;
import hla.rti1516e.exceptions.InteractionParameterNotDefined;
import hla.rti1516e.exceptions.InvalidRegion;
import hla.rti1516e.exceptions.InvalidRegionContext;
import hla.rti1516e.exceptions.ObjectClassNotDefined;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RegionDoesNotContainSpecifiedDimension;
import hla.rti1516e.exceptions.RegionInUseForUpdateOrSubscription;
import hla.rti1516e.exceptions.RegionNotCreatedByThisFederate;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;

public class FederateRegionManager
{
  private final Federate federate;

  private final ReadWriteLock regionsLock = new ReentrantReadWriteLock(true);
  private final Map<RegionHandle, FederateRegion> regions = new HashMap<RegionHandle, FederateRegion>();

  private final AtomicInteger regionCount = new AtomicInteger();

  public FederateRegionManager(Federate federate)
  {
    this.federate = federate;
  }

  public ReadWriteLock getRegionsLock()
  {
    return regionsLock;
  }

  public Map<RegionHandle, FederateRegion> getRegions()
  {
    return regions;
  }

  public RegionHandle createRegion(DimensionHandleSet dimensionHandles)
    throws RTIinternalError
  {
    IEEE1516eRegionHandle regionHandle = new IEEE1516eRegionHandle(
      federate.getFederateHandle(), regionCount.incrementAndGet());

    FederateRegion federateRegion = new FederateRegion(regionHandle, dimensionHandles, federate.getFDD());
    regionsLock.writeLock().lock();
    try
    {
      regions.put(regionHandle, federateRegion);
    }
    finally
    {
      regionsLock.writeLock().unlock();
    }

    federate.getRTIChannel().write(new CreateRegion(regionHandle, dimensionHandles));

    return regionHandle;
  }

  public RegionHandleSet createTemporaryRegions(Collection<Map<DimensionHandle, RangeBounds>> regions)
  {
    RegionHandleSet regionHandles = federate.getRegionHandleSetFactory().create();

    regionsLock.writeLock().lock();
    try
    {
      for (Map<DimensionHandle, RangeBounds> region : regions)
      {
        IEEE1516eRegionHandle regionHandle = new IEEE1516eRegionHandle(
          federate.getFederateHandle(), regionCount.incrementAndGet());
        this.regions.put(regionHandle, new FederateRegion(regionHandle, region));
        regionHandles.add(regionHandle);
      }
    }
    finally
    {
      regionsLock.writeLock().unlock();
    }

    return regionHandles;
  }

  public void deleteTemporaryRegions(RegionHandleSet regionHandles)
  {
    regionsLock.writeLock().lock();
    try
    {
      regions.keySet().removeAll(regionHandles);
    }
    finally
    {
      regionsLock.writeLock().unlock();
    }
  }

  public RangeBounds getRangeBounds(RegionHandle regionHandle, DimensionHandle dimensionHandle)
    throws InvalidRegion, RegionDoesNotContainSpecifiedDimension, RTIinternalError
  {
    RangeBounds rangeBounds;

    regionsLock.readLock().lock();
    try
    {
      FederateRegion region = regions.get(regionHandle);
      if (region == null)
      {
        throw new InvalidRegion(regionHandle.toString());
      }
      else
      {
        rangeBounds = region.getRangeBounds(dimensionHandle);
      }
    }
    finally
    {
      regionsLock.readLock().unlock();
    }

    return rangeBounds;
  }

  public void setRangeBounds(RegionHandle regionHandle, DimensionHandle dimensionHandle, RangeBounds rangeBounds)
    throws RegionNotCreatedByThisFederate, RegionDoesNotContainSpecifiedDimension
  {
    regionsLock.readLock().lock();
    try
    {
      getRegion(regionHandle).setRangeBounds(dimensionHandle, rangeBounds);
    }
    finally
    {
      regionsLock.readLock().unlock();
    }
  }

  public Map<RegionHandle, Map<DimensionHandle, RangeBounds>> commitRegionModifications(RegionHandleSet regionHandles)
    throws InvalidRegion, RegionNotCreatedByThisFederate, RTIinternalError
  {
    regionsLock.writeLock().lock();
    try
    {
      checkIfRegionNotCreatedByThisFederate(regionHandles);

      Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regionModifications =
        new HashMap<RegionHandle, Map<DimensionHandle, RangeBounds>>();
      for (RegionHandle regionHandle : regionHandles)
      {
        Map<DimensionHandle, RangeBounds> committedRegionModifications = regions.get(regionHandle).commit();
        if (!committedRegionModifications.isEmpty())
        {
          regionModifications.put(regionHandle, committedRegionModifications);
        }
      }

      federate.getRTIChannel().write(new CommitRegionModifications(regionModifications));

      return regionModifications;
    }
    finally
    {
      regionsLock.writeLock().unlock();
    }
  }

  public void deleteRegion(RegionHandle regionHandle)
    throws InvalidRegion, RegionNotCreatedByThisFederate, RegionInUseForUpdateOrSubscription, RTIinternalError
  {
    regionsLock.writeLock().lock();
    try
    {
      FederateRegion region = getRegion(regionHandle);
      region.checkIfInUse();

      regions.remove(regionHandle);

      federate.getRTIChannel().write(new DeleteRegion(regionHandle));
    }
    finally
    {
      regionsLock.writeLock().unlock();
    }
  }

  public void unassociateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle, AttributeRegionAssociation attributeRegionAssociation)
    throws RegionNotCreatedByThisFederate
  {
    regionsLock.readLock().lock();
    try
    {
      checkIfRegionNotCreatedByThisFederate(attributeRegionAssociation.rhset);

      for (RegionHandle regionHandle : attributeRegionAssociation.rhset)
      {
        regions.get(regionHandle).unassociateRegionsForUpdates(
          objectInstanceHandle, attributeRegionAssociation.ahset);
      }
    }
    finally
    {
      regionsLock.readLock().unlock();
    }
  }

  public void subscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions, boolean passive)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
           InvalidRegionContext, RTIinternalError
  {
    regionsLock.readLock().lock();
    try
    {
      checkIfAttributeNotDefinedOrRegionNotCreatedByThisFederate(objectClassHandle, attributesAndRegions);

      for (AttributeRegionAssociation attributeRegionAssociation : attributesAndRegions)
      {
        for (RegionHandle regionHandle : attributeRegionAssociation.rhset)
        {
          // TODO: don't forget passive

          regions.get(regionHandle).subscribe(objectClassHandle, attributeRegionAssociation.ahset);
        }
      }

      federate.getRTIChannel().write(
        new SubscribeObjectClassAttributesWithRegions(objectClassHandle, attributesAndRegions, passive));
    }
    finally
    {
      regionsLock.readLock().unlock();
    }
  }

  public void unsubscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion, RegionNotCreatedByThisFederate, RTIinternalError
  {
    regionsLock.readLock().lock();
    try
    {
      checkIfAttributeNotDefinedOrRegionNotCreatedByThisFederate(objectClassHandle, attributesAndRegions);

      for (AttributeRegionAssociation attributeRegionAssociation : attributesAndRegions)
      {
        for (RegionHandle regionHandle : attributeRegionAssociation.rhset)
        {
          regions.get(regionHandle).unsubscribe(objectClassHandle, attributeRegionAssociation.ahset);
        }
      }

      federate.getRTIChannel().write(
        new UnsubscribeObjectClassAttributesWithRegions(objectClassHandle, attributesAndRegions));
    }
    finally
    {
      regionsLock.readLock().unlock();
    }
  }

  public void subscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle, RegionHandleSet regionHandles, boolean passive)
    throws InvalidRegion, RegionNotCreatedByThisFederate
  {
    regionsLock.readLock().lock();
    try
    {
      checkIfRegionNotCreatedByThisFederate(regionHandles);

      for (RegionHandle regionHandle : regionHandles)
      {
        // TODO: don't forget passive

        regions.get(regionHandle).subscribe(interactionClassHandle);
      }

      federate.getRTIChannel().write(
        new SubscribeInteractionClassWithRegions(interactionClassHandle, regionHandles, passive));
    }
    finally
    {
      regionsLock.readLock().unlock();
    }
  }

  public void unsubscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle, RegionHandleSet regionHandles)
    throws InvalidRegion, RegionNotCreatedByThisFederate
  {
    regionsLock.readLock().lock();
    try
    {
      checkIfRegionNotCreatedByThisFederate(regionHandles);

      for (RegionHandle regionHandle : regionHandles)
      {
        regions.get(regionHandle).unsubscribe(interactionClassHandle);
      }

      federate.getRTIChannel().write(
        new UnsubscribeInteractionClassWithRegions(interactionClassHandle, regionHandles));
    }
    finally
    {
      regionsLock.readLock().unlock();
    }
  }

  public void sendInteractionWithRegions(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues,
    RegionHandleSet regionHandles, byte[] tag)
    throws InteractionClassNotDefined, InteractionClassNotPublished, InteractionParameterNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext, SaveInProgress, RestoreInProgress, RTIinternalError
  {
  }

  public FederateRegion getRegionSafely(RegionHandle regionHandle)
  {
    FederateRegion region = regions.get(regionHandle);
    assert region != null;
    return region;
  }

  protected FederateRegion getRegion(RegionHandle regionHandle)
    throws RegionNotCreatedByThisFederate
  {
    FederateRegion region = regions.get(regionHandle);
    if (region == null)
    {
      throw new RegionNotCreatedByThisFederate(regionHandle.toString());
    }
    return region;
  }

  protected void checkIfRegionNotCreatedByThisFederate(RegionHandle regionHandle)
    throws RegionNotCreatedByThisFederate
  {
    if (!regions.containsKey(regionHandle))
    {
      throw new RegionNotCreatedByThisFederate(regionHandle.toString());
    }
  }

  protected void checkIfRegionNotCreatedByThisFederate(RegionHandleSet regionHandles)
    throws RegionNotCreatedByThisFederate
  {
    for (RegionHandle regionHandle : regionHandles)
    {
      checkIfRegionNotCreatedByThisFederate(regionHandle);
    }
  }

  private void checkIfAttributeNotDefinedOrRegionNotCreatedByThisFederate(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions)
    throws AttributeNotDefined, ObjectClassNotDefined, RegionNotCreatedByThisFederate
  {
    for (AttributeRegionAssociation attributeRegionAssociation : attributesAndRegions)
    {
      federate.getFDD().checkIfAttributeNotDefined(objectClassHandle, attributeRegionAssociation.ahset);
      checkIfRegionNotCreatedByThisFederate(attributeRegionAssociation.rhset);
    }
  }
}
