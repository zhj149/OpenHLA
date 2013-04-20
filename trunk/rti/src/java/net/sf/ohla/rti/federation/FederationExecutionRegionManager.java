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

import java.io.DataOutput;
import java.io.IOException;
import java.io.DataInput;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.fdd.Attribute;
import net.sf.ohla.rti.fdd.InteractionClass;
import net.sf.ohla.rti.messages.CommitRegionModifications;
import net.sf.ohla.rti.messages.CreateRegion;
import net.sf.ohla.rti.messages.DeleteRegion;

import hla.rti1516e.DimensionHandle;
import hla.rti1516e.RangeBounds;
import hla.rti1516e.RegionHandle;

public class FederationExecutionRegionManager
{
  private final FederationExecution federationExecution;

  private final ReadWriteLock regionsLock = new ReentrantReadWriteLock(true);
  private final Map<RegionHandle, FederationExecutionRegion> regions = new HashMap<RegionHandle, FederationExecutionRegion>();

  public FederationExecutionRegionManager(FederationExecution federationExecution)
  {
    this.federationExecution = federationExecution;
  }

  public ReadWriteLock getRegionsLock()
  {
    return regionsLock;
  }

  public Map<RegionHandle, FederationExecutionRegion> getRegions()
  {
    return regions;
  }

  public void createRegion(FederateProxy federateProxy, CreateRegion createRegion)
  {
    regionsLock.writeLock().lock();
    try
    {
      regions.put(createRegion.getRegionHandle(), new FederationExecutionRegion(
        createRegion.getRegionHandle(), createRegion.getDimensionHandles(), federationExecution.getFDD()));
    }
    finally
    {
      regionsLock.writeLock().unlock();
    }
  }

  public void commitRegionModifications(
    FederateProxy federateProxy, CommitRegionModifications commitRegionModifications)
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
    }
    finally
    {
      regionsLock.writeLock().unlock();
    }
  }

  public void deleteRegion(FederateProxy federateProxy, DeleteRegion deleteRegion)
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
  }

  public Map<RegionHandle, Map<DimensionHandle, RangeBounds>> intersects(
    Set<RegionHandle> subscribedRegionHandles, Set<RegionHandle> regionHandles, InteractionClass interactionClass)
  {
    Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regions;

    boolean intersects = false;
    for (Iterator<RegionHandle> i = subscribedRegionHandles.iterator(); !intersects && i.hasNext();)
    {
      FederationExecutionRegion subscribedRegion = this.regions.get(i.next());
      if (subscribedRegion != null)
      {
        for (Iterator<RegionHandle> j = regionHandles.iterator(); !intersects && j.hasNext();)
        {
          FederationExecutionRegion region = this.regions.get(j.next());
          if (region != null)
          {
            intersects = subscribedRegion.intersects(region, interactionClass.getDimensionHandles());
          }
        }
      }
    }
    if (intersects)
    {
      regions = new HashMap<RegionHandle, Map<DimensionHandle, RangeBounds>>();
      for (RegionHandle regionHandle : regionHandles)
      {
        FederationExecutionRegion region = this.regions.get(regionHandle);
        assert region != null;

        regions.put(regionHandle, region.copyRangeBounds());
      }
    }
    else
    {
      regions = null;
    }

    return regions;
  }

  public boolean intersects(
    Set<RegionHandle> subscribedRegionHandles, Set<RegionHandle> regionHandles,
    Attribute attribute, Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regions)
  {
    boolean intersects = false;

    for (Iterator<RegionHandle> i = subscribedRegionHandles.iterator(); !intersects && i.hasNext();)
    {
      FederationExecutionRegion subscribedRegion = this.regions.get(i.next());
      if (subscribedRegion != null)
      {
        for (Iterator<RegionHandle> j = regionHandles.iterator(); !intersects && j.hasNext();)
        {
          FederationExecutionRegion region = this.regions.get(j.next());
          if (region != null)
          {
            intersects = subscribedRegion.intersects(region, attribute.getDimensionHandles());
          }
        }
      }
    }
    if (intersects)
    {
      for (RegionHandle regionHandle : regionHandles)
      {
        FederationExecutionRegion region = this.regions.get(regionHandle);
        assert region != null;

        if (!regions.containsKey(regionHandle))
        {
          // only copy the range bounds if they have not already been copied
          //
          regions.put(regionHandle, region.copyRangeBounds());
        }
      }
    }

    return intersects;
  }

  public boolean intersectsOnly(
    Set<RegionHandle> subscribedRegionHandles, Set<RegionHandle> regionHandles, Attribute attribute)
  {
    return intersectsOnly(subscribedRegionHandles, regionHandles, attribute.getDimensionHandles());
  }

  public boolean intersectsOnly(
    Set<RegionHandle> subscribedRegionHandles, Set<RegionHandle> regionHandles, InteractionClass interactionClass)
  {
    return intersectsOnly(subscribedRegionHandles, regionHandles, interactionClass.getDimensionHandles());
  }

  public boolean intersectsOnly(
    Set<RegionHandle> subscribedRegionHandles, Set<RegionHandle> regionHandles, Set<DimensionHandle> dimensionHandles)
  {
    boolean intersects = false;
    for (Iterator<RegionHandle> i = subscribedRegionHandles.iterator(); !intersects && i.hasNext();)
    {
      FederationExecutionRegion subscribedRegion = this.regions.get(i.next());
      if (subscribedRegion != null)
      {
        for (Iterator<RegionHandle> j = regionHandles.iterator(); !intersects && j.hasNext();)
        {
          FederationExecutionRegion region = this.regions.get(j.next());
          if (region != null)
          {
            intersects = subscribedRegion.intersects(region, dimensionHandles);
          }
        }
      }
    }
    return intersects;
  }

  public void saveState(DataOutput out)
    throws IOException
  {
    out.writeInt(regions.size());
    for (FederationExecutionRegion region : regions.values())
    {
      region.writeTo(out);
    }
  }

  public void restoreState(DataInput in)
    throws IOException
  {
    for (int i = in.readInt(); i > 0; i--)
    {
      FederationExecutionRegion region = new FederationExecutionRegion(in);
      regions.put(region.getRegionHandle(), region);
    }
  }
}
