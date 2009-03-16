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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.hla.rti1516.IEEE1516RegionHandle;
import net.sf.ohla.rti.messages.CommitRegionModifications;
import net.sf.ohla.rti.messages.CreateRegion;
import net.sf.ohla.rti.messages.DefaultResponse;
import net.sf.ohla.rti.messages.DeleteRegion;
import net.sf.ohla.rti.messages.GetRangeBounds;

import hla.rti1516.DimensionHandle;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.InvalidRegion;
import hla.rti1516.RangeBounds;
import hla.rti1516.RegionDoesNotContainSpecifiedDimension;
import hla.rti1516.RegionHandle;

public class FederationExecutionRegionManager
{
  protected final FederationExecution federationExecution;

  protected AtomicInteger regionCount = new AtomicInteger(Short.MIN_VALUE);

  protected ReadWriteLock regionsLock = new ReentrantReadWriteLock(true);
  protected Map<RegionHandle, FederationExecutionRegion> regions =
    new HashMap<RegionHandle, FederationExecutionRegion>();

  public FederationExecutionRegionManager(
    FederationExecution federationExecution)
  {
    this.federationExecution = federationExecution;
  }

  public FederationExecution getFederationExecution()
  {
    return federationExecution;
  }

  public Map<RegionHandle, FederationExecutionRegion> getRegions()
  {
    return regions;
  }

  public void createRegion(FederateProxy federateProxy,
                           CreateRegion createRegion)
  {
    RegionHandle regionHandle = nextRegionHandle();

    regionsLock.writeLock().lock();
    try
    {
      regions.put(regionHandle, new FederationExecutionRegion(
        regionHandle, createRegion.getDimensionHandles(), this));
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

      FederationExecutionRegion region =
        regions.get(getRangeBounds.getRegionHandle());
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

  public boolean intersects(Set<RegionHandle> lhs, Set<RegionHandle> rhs,
                            InteractionClassHandle interactionClassHandle)
  {
    return intersects(
      lhs, rhs, federationExecution.getFDD().getInteractionClasses().get(
      interactionClassHandle).getDimensions().keySet());
  }

  public boolean intersects(Set<RegionHandle> lhs, Set<RegionHandle> rhs,
                            Set<DimensionHandle> dimensionHandles)
  {
    boolean intersects = false;
    if (lhs.isEmpty())
    {
      intersects = rhs.isEmpty();
    }
    else if (rhs.size() > 0)
    {
      regionsLock.readLock().lock();
      try
      {
        for (Iterator<RegionHandle> i = lhs.iterator();
             !intersects && i.hasNext();)
        {
          FederationExecutionRegion lhsRegion = regions.get(i.next());
          if (lhsRegion != null)
          {
            for (Iterator<RegionHandle> j = rhs.iterator();
                 !intersects && i.hasNext();)
            {
              FederationExecutionRegion rhsRegion = regions.get(i.next());
              if (rhsRegion != null)
              {
                intersects = lhsRegion.intersects(rhsRegion, dimensionHandles);
              }
            }
          }
        }
      }
      finally
      {
        regionsLock.readLock().unlock();
      }
    }
    return intersects;
  }

  protected RegionHandle nextRegionHandle()
  {
    return new IEEE1516RegionHandle(regionCount.incrementAndGet());
  }
}
