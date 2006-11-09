package net.sf.ohla.rti1516.federate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.ohla.rti1516.fdd.FDD;
import net.sf.ohla.rti1516.OHLARegionHandleSet;

import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeRegionAssociation;
import hla.rti1516.DimensionHandle;
import hla.rti1516.DimensionHandleSet;
import hla.rti1516.FederateHandle;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.InvalidDimensionHandle;
import hla.rti1516.InvalidRegion;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.RTIinternalError;
import hla.rti1516.RangeBounds;
import hla.rti1516.RegionDoesNotContainSpecifiedDimension;
import hla.rti1516.RegionHandle;
import hla.rti1516.RegionHandleSet;
import hla.rti1516.RegionInUseForUpdateOrSubscription;
import hla.rti1516.RegionNotCreatedByThisFederate;

public class RegionManager
{
  protected Federate federate;

  protected Region defaultRegion;

  protected Lock regionsLock = new ReentrantLock(true);
  protected Map<RegionHandle, Region> regions =
    new HashMap<RegionHandle, Region>();

  public RegionManager(Federate federate)
  {
    this.federate = federate;
  }

  public RegionHandleSet getIntersectingRegions(
    Map<RegionHandle, Map<DimensionHandle, RangeBounds>> rangeBounds)
  {
    RegionHandleSet intersectingRegions = new OHLARegionHandleSet();

    regionsLock.lock();
    try
    {
      for (Region region : regions.values())
      {
        for (Map<DimensionHandle, RangeBounds> value : rangeBounds.values())
        {
          if (region.intersects(value))
          {
            intersectingRegions.add(region.getRegionHandle());
          }
        }
      }
    }
    finally
    {
      regionsLock.unlock();
    }

    return intersectingRegions;
  }

  public Map<RegionHandle, Map<DimensionHandle, RangeBounds>> commitRegionModifications(
    RegionHandleSet regionHandles)
    throws InvalidRegion, RegionNotCreatedByThisFederate
  {
    Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regionModifications =
      new HashMap<RegionHandle, Map<DimensionHandle, RangeBounds>>();

    regionsLock.lock();
    try
    {
      for (RegionHandle regionHandle : regionHandles)
      {
        Map<DimensionHandle, RangeBounds> committedRegionModifications =
          getRegionThrowIfNull(regionHandle).commit();
        if (!committedRegionModifications.isEmpty())
        {
          regionModifications.put(regionHandle, committedRegionModifications);
        }
      }
    }
    finally
    {
      regionsLock.unlock();
    }

    return regionModifications;
  }

  public void deleteRegion(RegionHandle regionHandle)
    throws InvalidRegion, RegionNotCreatedByThisFederate,
           RegionInUseForUpdateOrSubscription
  {
    regionsLock.lock();
    try
    {
      Region region = getRegionThrowIfNull(regionHandle);

      region.checkIfInUse();

      regions.remove(regionHandle);
    }
    finally
    {
      regionsLock.unlock();
    }
  }

  public void associateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeRegionAssociation attributeRegionAssociation)
  {
  }

  public void unassociateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeRegionAssociation attributeRegionAssociation)
  {
  }

  public void subscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle,
    AttributeRegionAssociation attributeRegionAssociation, boolean passive)
    throws InvalidRegion, RegionNotCreatedByThisFederate
  {
    for (RegionHandle regionHandle : attributeRegionAssociation.regions)
    {
      // TODO: don't forget passive

      getRegionThrowIfNull(regionHandle).subscribe(
        objectClassHandle, attributeRegionAssociation.attributes);
    }
  }

  public void unsubscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle,
    AttributeRegionAssociation attributeRegionAssociation)
    throws InvalidRegion, RegionNotCreatedByThisFederate
  {
    for (RegionHandle regionHandle : attributeRegionAssociation.regions)
    {
      getRegionThrowIfNull(regionHandle).unsubscribe(
        objectClassHandle, attributeRegionAssociation.attributes);
    }
  }

  public void checkIfRegionNotCreatedByThisFederate(RegionHandle regionHandle)
    throws InvalidRegion, RegionNotCreatedByThisFederate
  {
    getRegionThrowIfNull(regionHandle).checkIfRegionNotCreatedByThisFederate();
  }

  protected Region getRegion(RegionHandle regionHandle)
  {
    regionsLock.lock();
    try
    {
      return regions.get(regionHandle);
    }
    finally
    {
      regionsLock.unlock();
    }
  }

  protected Region getRegionThrowIfNull(RegionHandle regionHandle)
    throws InvalidRegion
  {
    Region region = getRegion(regionHandle);
    if (region == null)
    {
      throw new InvalidRegion(String.format("%s", regionHandle));
    }
    return region;
  }

  public void subscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle,
    RegionHandleSet regionHandles, boolean passive)
    throws InvalidRegion, RegionNotCreatedByThisFederate
  {
    for (RegionHandle regionHandle : regionHandles)
    {
      getRegionThrowIfNull(regionHandle).subscribe(interactionClassHandle);
    }
  }

  public void unsubscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle,
    RegionHandleSet regionHandles)
    throws InvalidRegion, RegionNotCreatedByThisFederate
  {
    for (RegionHandle regionHandle : regionHandles)
    {
      getRegionThrowIfNull(regionHandle).unsubscribe(interactionClassHandle);
    }
  }

  protected class Region
  {
    protected RegionHandle regionHandle;
    protected DimensionHandleSet dimensionHandles;

    protected FederateHandle creator;

    protected Map<DimensionHandle, RangeBounds> rangeBounds =
      new HashMap<DimensionHandle, RangeBounds>();

    protected Map<DimensionHandle, RangeBounds> uncommittedRangeBounds =
      new HashMap<DimensionHandle, RangeBounds>();

    protected Map<ObjectInstanceHandle, AttributeHandleSet> associatedObjects =
      new HashMap<ObjectInstanceHandle, AttributeHandleSet>();

    protected Map<ObjectClassHandle, AttributeHandleSet> subscribedObjectClasses =
      new HashMap<ObjectClassHandle, AttributeHandleSet>();

    protected Set<InteractionClassHandle> subscribedInteractionClasses =
      new HashSet<InteractionClassHandle>();

    public Region(RegionHandle regionHandle,
                  DimensionHandleSet dimensionHandles, FDD fdd)
      throws RTIinternalError
    {
      this.regionHandle = regionHandle;
      this.dimensionHandles = dimensionHandles;

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

    public Map<DimensionHandle, RangeBounds> getRangeBounds()
    {
      return rangeBounds;
    }

    public RangeBounds getRangeBounds(DimensionHandle dimensionHandle)
      throws RegionDoesNotContainSpecifiedDimension
    {
      RangeBounds rangeBounds = this.rangeBounds.get(dimensionHandle);
      if (rangeBounds == null)
      {
        throw new RegionDoesNotContainSpecifiedDimension(
          String.format("%s", dimensionHandle));
      }
      return clone(rangeBounds);
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

    public void associate(ObjectInstanceHandle objectInstanceHandle,
                          AttributeHandleSet attributeHandles)
    {
      AttributeHandleSet existingAttributeHandles =
        associatedObjects.get(objectInstanceHandle);
      if (existingAttributeHandles == null)
      {
        existingAttributeHandles =
          federate.getAttributeHandleSetFactory().create();
        associatedObjects.put(objectInstanceHandle, existingAttributeHandles);
      }

      existingAttributeHandles.addAll(attributeHandles);
    }

    public void unassociate(ObjectInstanceHandle objectInstanceHandle,
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
      checkIfRegionNotCreatedByThisFederate();

      AttributeHandleSet existingAttributeHandles =
        subscribedObjectClasses.get(objectClassHandle);
      if (existingAttributeHandles == null)
      {
        existingAttributeHandles =
          federate.getAttributeHandleSetFactory().create();
        subscribedObjectClasses.put(objectClassHandle,
                                    existingAttributeHandles);
      }

      existingAttributeHandles.addAll(attributeHandles);
    }

    public void unsubscribe(ObjectClassHandle objectClassHandle,
                            AttributeHandleSet attributeHandles)
      throws RegionNotCreatedByThisFederate
    {
      checkIfRegionNotCreatedByThisFederate();

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
      checkIfRegionNotCreatedByThisFederate();

      subscribedInteractionClasses.add(interactionClassHandle);
    }

    public void unsubscribe(InteractionClassHandle interactionClassHandle)
      throws RegionNotCreatedByThisFederate
    {
      checkIfRegionNotCreatedByThisFederate();

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

    public void checkIfRegionNotCreatedByThisFederate()
      throws RegionNotCreatedByThisFederate
    {
      if (creator.equals(federate.getFederateHandle()))
      {
        throw new RegionNotCreatedByThisFederate(
          String.format("creator: %s", creator));
      }
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
}
