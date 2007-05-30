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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.fdd.Attribute;
import net.sf.ohla.rti.fdd.Dimension;
import net.sf.ohla.rti.fdd.FDD;
import net.sf.ohla.rti.fdd.InteractionClass;
import net.sf.ohla.rti.hla.rti1516.IEEE1516RegionHandle;
import net.sf.ohla.rti.messages.CommitRegionModifications;
import net.sf.ohla.rti.messages.CreateRegion;
import net.sf.ohla.rti.messages.DefaultResponse;
import net.sf.ohla.rti.messages.DeleteRegion;
import net.sf.ohla.rti.messages.GetRangeBounds;

import hla.rti1516.DimensionHandle;
import hla.rti1516.DimensionHandleSet;
import hla.rti1516.InvalidRegion;
import hla.rti1516.RangeBounds;
import hla.rti1516.RegionDoesNotContainSpecifiedDimension;
import hla.rti1516.RegionHandle;
import hla.rti1516.RegionNotCreatedByThisFederate;
import hla.rti1516.AttributeHandle;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.AttributeHandleSet;

public class FederationExecutionRegionManager
{
  protected final FederationExecution federationExecution;

  protected AtomicInteger regionCount = new AtomicInteger(Short.MIN_VALUE);

  protected ReadWriteLock regionsLock = new ReentrantReadWriteLock(true);
  protected Map<RegionHandle, Region> regions =
    new HashMap<RegionHandle, Region>();

  protected Map<RegionHandle, Set<RegionHandle>> intersectingRegions =
    new HashMap<RegionHandle, Set<RegionHandle>>();

  public FederationExecutionRegionManager(
    FederationExecution federationExecution)
  {
    this.federationExecution = federationExecution;
  }

  public void createRegion(FederateProxy federateProxy,
                           CreateRegion createRegion)
  {
    RegionHandle regionHandle = nextRegionHandle();

    regionsLock.writeLock().lock();
    try
    {
      regions.put(regionHandle, new Region(
        regionHandle, createRegion.getDimensionHandles(),
        federationExecution.getFDD()));
    }
    finally
    {
      regionsLock.writeLock().unlock();
    }

    federateProxy.getSession().write(
      new DefaultResponse(createRegion.getId(), regionHandle));
  }

  public void getRangeBounds(FederateProxy federateProxy,
                             GetRangeBounds getRangeBounds)
  {
    regionsLock.readLock().lock();
    try
    {
      Object response;

      Region region = regions.get(getRangeBounds.getRegionHandle());
      if (region == null)
      {
        // region was deleted
        //
        response = new InvalidRegion(getRangeBounds.getRegionHandle());
      }
      else
      {
        try
        {
          response =
            region.getRangeBounds(getRangeBounds.getDimensionHandle());
        }
        catch (RegionDoesNotContainSpecifiedDimension rdncsd)
        {
          response = rdncsd;
        }
      }

      federateProxy.getSession().write(
        new DefaultResponse(getRangeBounds.getId(), response));
    }
    finally
    {
      regionsLock.readLock().unlock();
    }
  }

  public void commitRegionModifications(
    FederateProxy federateProxy,
    CommitRegionModifications commitRegionModifications)
  {
    regionsLock.writeLock().lock();
    try
    {
      // commit all the region modifications
      //
      for (Map.Entry<RegionHandle, Map<DimensionHandle, RangeBounds>> entry :
        commitRegionModifications.getRegionModifications().entrySet())
      {
        regions.get(entry.getKey()).commitRegionModifications(entry.getValue());
      }

      for (Region region : regions.values())
      {
        region.intersects(
          commitRegionModifications.getRegionModifications().keySet());
      }
    }
    finally
    {
      regionsLock.writeLock().unlock();
    }

    federateProxy.getSession().write(
      new DefaultResponse(commitRegionModifications.getId()));
  }

  public void deleteRegion(FederateProxy federateProxy,
                           DeleteRegion deleteRegion)
  {
    regionsLock.writeLock().lock();
    try
    {
      regions.remove(deleteRegion.getRegionHandle()).delete();
    }
    finally
    {
      regionsLock.writeLock().unlock();
    }

    federateProxy.getSession().write(new DefaultResponse(deleteRegion.getId()));
  }

  public boolean intersects(Set<RegionHandle> regionHandles,
                            AttributeHandle attributeHandle,
                            Set<RegionHandle> regionHandles)
  {
    boolean intersects = false;

    regionsLock.readLock().lock();
    try
    {
        Region region = regions.get(i.next());
        if (region != null)
        {
          intersects = region.intersects(rhs);
        }
    }
    finally
    {
      regionsLock.readLock().unlock();
    }

    return intersects;
  }

  protected RegionHandle nextRegionHandle()
  {
    return new IEEE1516RegionHandle(regionCount.incrementAndGet());
  }

  protected Region getRegion(RegionHandle regionHandle)
    throws RegionNotCreatedByThisFederate
  {
    Region region = regions.get(regionHandle);
    if (region == null)
    {
      throw new RegionNotCreatedByThisFederate(
        String.format("%s", regionHandle));
    }
    return region;
  }

  protected class Region
  {
    protected final RegionHandle regionHandle;
    protected final DimensionHandleSet dimensionHandles;

    protected Map<DimensionHandle, RangeBounds> rangeBounds =
      new HashMap<DimensionHandle, RangeBounds>();

    protected Map<AttributeHandle, AttributeRegionRealization> attributeRegionRealizations =
      new HashMap<AttributeHandle, AttributeRegionRealization>();
    protected Map<InteractionClassHandle, InteractionClassRegionRealization> interactionClassRegionRealizations =
      new HashMap<InteractionClassHandle, InteractionClassRegionRealization>();

    public Region(RegionHandle regionHandle,
                  DimensionHandleSet dimensionHandles, FDD fdd)
    {
      this.regionHandle = regionHandle;
      this.dimensionHandles = dimensionHandles;

      // initialize our range bounds to the dimension defaults
      //
      for (DimensionHandle dimensionHandle : dimensionHandles)
      {
        Dimension dimension = fdd.getDimensions().get(dimensionHandle);
        assert dimension != null;

        rangeBounds.put(dimensionHandle, dimension.getRangeBounds());
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
      RangeBounds rangeBounds = this.rangeBounds.get(dimensionHandle);
      if (rangeBounds == null)
      {
        throw new RegionDoesNotContainSpecifiedDimension(dimensionHandle);
      }
      return rangeBounds;
    }

    public void commitRegionModifications(
      Map<DimensionHandle, RangeBounds> rangeBounds)
    {
      this.rangeBounds.putAll(rangeBounds);
    }

    public void delete()
    {
    }

    public void intersects(Set<RegionHandle> regionHandles)
    {
      for (RegionHandle regionHandle : regionHandles)
      {
        Region region = regions.get(regionHandle);
        if (region != null)
        {

        }
      }
    }

    public boolean intersects(AttributeHandle attributeHandle,
                              Set<RegionHandle> regionHandles)
    {
      boolean intersects = false;

      AttributeRegionRealization attributeRegionRealization =
        attributeRegionRealizations.get(attributeHandle);
      if (attributeRegionRealization != null)
      {
        for (Iterator<RegionHandle> i = regionHandles.iterator();
             i.hasNext() && !intersects;)
        {
          intersects =
            attributeRegionRealization.intersectingRegions.contains(i.next());
        }
      }

      return intersects;
    }

    public boolean intersects(InteractionClassHandle interactionClassHandle,
                              Set<RegionHandle> regionHandles)
    {
      boolean intersects = false;

      InteractionClassRegionRealization interactionClassRegionRealization =
        interactionClassRegionRealizations.get(interactionClassHandle);
      if (interactionClassRegionRealization != null)
      {
        for (Iterator<RegionHandle> i = regionHandles.iterator();
             i.hasNext() && !intersects;)
        {
          intersects =
            interactionClassRegionRealization.intersectingRegions.contains(i.next());
        }
      }

      return intersects;
    }

    protected boolean intersects(RangeBounds lhs, RangeBounds rhs)
    {
      return (lhs.lower < rhs.upper && rhs.lower < lhs.upper) ||
             lhs.lower == rhs.lower;
    }

    protected class AttributeRegionRealization
    {
      protected final Attribute attribute;

      protected Set<RegionHandle> intersectingRegions =
        new HashSet<RegionHandle>();

      public AttributeRegionRealization(Attribute attribute)
      {
        this.attribute = attribute;
      }
    }

    protected class InteractionClassRegionRealization
    {
      protected final InteractionClass interactionClass;

      protected Set<RegionHandle> intersectingRegions =
        new HashSet<RegionHandle>();

      public InteractionClassRegionRealization(InteractionClass interactionClass)
      {
        this.interactionClass = interactionClass;
      }
    }
  }
}
