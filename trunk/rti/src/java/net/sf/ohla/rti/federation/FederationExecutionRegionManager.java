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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.hla.rti1516.IEEE1516RegionHandle;
import net.sf.ohla.rti.messages.CommitRegionModifications;
import net.sf.ohla.rti.messages.CreateRegion;
import net.sf.ohla.rti.messages.DefaultResponse;
import net.sf.ohla.rti.messages.DeleteRegion;
import net.sf.ohla.rti.messages.GetRangeBounds;

import hla.rti1516.AttributeHandle;
import hla.rti1516.DimensionHandle;
import hla.rti1516.InvalidRegion;
import hla.rti1516.RangeBounds;
import hla.rti1516.RegionDoesNotContainSpecifiedDimension;
import hla.rti1516.RegionHandle;
import hla.rti1516.RegionNotCreatedByThisFederate;
import hla.rti1516.InteractionClassHandle;

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

      for (FederationExecutionRegion region : regions.values())
      {
        // TODO: implement

        //region.intersects(
        //  commitRegionModifications.getRegionModifications().keySet());
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

  public boolean intersects(Set<RegionHandle> subscribedRegionHandles,
                            InteractionClassHandle interactionClassHandle,
                            Set<RegionHandle> sentRegionHandles)
  {
    boolean intersects = false;

    regionsLock.readLock().lock();
    try
    {
      for (Iterator<RegionHandle> i = subscribedRegionHandles.iterator();
           !intersects && i.hasNext();)
      {
        FederationExecutionRegion region = regions.get(i.next());
        if (region != null)
        {
          intersects =
            region.intersects(interactionClassHandle, sentRegionHandles);
        }
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

  protected FederationExecutionRegion getRegion(RegionHandle regionHandle)
    throws RegionNotCreatedByThisFederate
  {
    FederationExecutionRegion region = regions.get(regionHandle);
    if (region == null)
    {
      throw new RegionNotCreatedByThisFederate(
        String.format("%s", regionHandle));
    }
    return region;
  }
}