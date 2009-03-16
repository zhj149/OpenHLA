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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.hla.rti1516.IEEE1516RegionHandleSet;
import net.sf.ohla.rti.messages.CommitRegionModifications;
import net.sf.ohla.rti.messages.CreateRegion;
import net.sf.ohla.rti.messages.DeleteRegion;
import net.sf.ohla.rti.messages.GetRangeBounds;

import org.apache.mina.common.WriteFuture;

import hla.rti1516.AttributeRegionAssociation;
import hla.rti1516.DimensionHandle;
import hla.rti1516.DimensionHandleSet;
import hla.rti1516.InteractionClassHandle;
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

public class FederateRegionManager
{
  protected Federate federate;

  protected ReadWriteLock regionsLock = new ReentrantReadWriteLock(true);
  protected Map<RegionHandle, FederateRegion> regions =
    new HashMap<RegionHandle, FederateRegion>();

  protected Map<RegionHandle, Set<RegionHandle>> intersectingRegions =
    new HashMap<RegionHandle, Set<RegionHandle>>();

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
    try
    {
      CreateRegion createRegion = new CreateRegion(dimensionHandles);
      WriteFuture writeFuture = federate.getRTISession().write(createRegion);

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      // TODO: set timeout
      //
      Object response = createRegion.getResponse();
      assert response instanceof RegionHandle : "unexpected response";

      RegionHandle regionHandle = (RegionHandle) response;

      regionsLock.writeLock().lock();
      try
      {
        regions.put(regionHandle, new FederateRegion(
          regionHandle, dimensionHandles, federate.getFDD(),
          federate.getAttributeHandleSetFactory()));
      }
      finally
      {
        regionsLock.writeLock().unlock();
      }

      return regionHandle;
    }
    catch (InterruptedException ie)
    {
      throw new RTIinternalError("interrupted awaiting timeout", ie);
    }
    catch (ExecutionException ee)
    {
      throw new RTIinternalError("unable to get response", ee);
    }
  }

  public RegionHandleSet getIntersectingRegions(
    Map<RegionHandle, Map<DimensionHandle, RangeBounds>> rangeBounds)
  {
    RegionHandleSet intersectingRegions = new IEEE1516RegionHandleSet();

    regionsLock.readLock().lock();
    try
    {
      for (FederateRegion region : regions.values())
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
      regionsLock.readLock().unlock();
    }

    return intersectingRegions;
  }

  public RangeBounds getRangeBounds(RegionHandle regionHandle,
                                    DimensionHandle dimensionHandle)
    throws RegionDoesNotContainSpecifiedDimension, RTIinternalError
  {
    RangeBounds rangeBounds = null;

    regionsLock.readLock().lock();
    try
    {
      FederateRegion region = regions.get(regionHandle);
      if (region != null)
      {
        rangeBounds = region.getRangeBounds(dimensionHandle);
      }
      else
      {
        try
        {
          GetRangeBounds getRangeBounds =
            new GetRangeBounds(regionHandle, dimensionHandle);
          WriteFuture writeFuture =
            federate.getRTISession().write(getRangeBounds);

          // TODO: set timeout
          //
          writeFuture.join();

          if (!writeFuture.isWritten())
          {
            throw new RTIinternalError("error communicating with RTI");
          }

          // TODO: set timeout
          //
          Object response = getRangeBounds.getResponse();
          if (response instanceof RangeBounds)
          {
            rangeBounds = (RangeBounds) response;
          }
          else if (response instanceof RegionDoesNotContainSpecifiedDimension)
          {
            throw new RegionDoesNotContainSpecifiedDimension(
              (RegionDoesNotContainSpecifiedDimension) response);
          }
          else
          {
            assert false : String.format("unexpected response: %s", response);
          }
        }
        catch (InterruptedException ie)
        {
          throw new RTIinternalError("interrupted awaiting timeout", ie);
        }
        catch (ExecutionException ee)
        {
          throw new RTIinternalError("unable to get response", ee);
        }
      }
    }
    finally
    {
      regionsLock.readLock().unlock();
    }

    return rangeBounds;
  }

  public void setRangeBounds(RegionHandle regionHandle,
                             DimensionHandle dimensionHandle,
                             RangeBounds rangeBounds)
    throws RegionNotCreatedByThisFederate,
           RegionDoesNotContainSpecifiedDimension
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

  public Map<RegionHandle, Map<DimensionHandle, RangeBounds>> commitRegionModifications(
    RegionHandleSet regionHandles)
    throws InvalidRegion, RegionNotCreatedByThisFederate, RTIinternalError
  {
    Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regionModifications =
      new HashMap<RegionHandle, Map<DimensionHandle, RangeBounds>>();

    regionsLock.readLock().lock();
    try
    {
      for (RegionHandle regionHandle : regionHandles)
      {
        Map<DimensionHandle, RangeBounds> committedRegionModifications =
          getRegion(regionHandle).commit();
        if (!committedRegionModifications.isEmpty())
        {
          regionModifications.put(regionHandle, committedRegionModifications);
        }
      }

      try
      {
        CommitRegionModifications commitRegionModifications =
          new CommitRegionModifications(regionModifications);
        WriteFuture writeFuture = federate.getRTISession().write(
          commitRegionModifications);

        // TODO: set timeout
        //
        writeFuture.join();

        if (!writeFuture.isWritten())
        {
          throw new RTIinternalError("error communicating with RTI");
        }

        // TODO: set timeout
        //
        commitRegionModifications.await();
      }
      catch (InterruptedException ie)
      {
        throw new RTIinternalError("interrupted awaiting timeout", ie);
      }
      catch (ExecutionException ee)
      {
        throw new RTIinternalError("unable to get response", ee);
      }
    }
    finally
    {
      regionsLock.readLock().unlock();
    }

    return regionModifications;
  }

  public void deleteRegion(RegionHandle regionHandle)
    throws InvalidRegion, RegionNotCreatedByThisFederate,
           RegionInUseForUpdateOrSubscription, RTIinternalError
  {
    regionsLock.writeLock().lock();
    try
    {
      FederateRegion region = getRegion(regionHandle);
      region.checkIfInUse();

      DeleteRegion deleteRegion = new DeleteRegion(regionHandle);
      WriteFuture writeFuture = federate.getRTISession().write(deleteRegion);

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      // TODO: set timeout
      //
      deleteRegion.await();

      regions.remove(regionHandle);
    }
    catch (InterruptedException ie)
    {
      throw new RTIinternalError("interrupted awaiting timeout", ie);
    }
    catch (ExecutionException ee)
    {
      throw new RTIinternalError("unable to get response", ee);
    }
    finally
    {
      regionsLock.writeLock().unlock();
    }
  }

  public void unassociateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeRegionAssociation attributeRegionAssociation)
    throws RegionNotCreatedByThisFederate
  {
    regionsLock.readLock().lock();
    try
    {
      checkIfRegionNotCreatedByThisFederate(attributeRegionAssociation.regions);

      for (RegionHandle regionHandle : attributeRegionAssociation.regions)
      {
        regions.get(regionHandle).unassociateRegionsForUpdates(
          objectInstanceHandle, attributeRegionAssociation.attributes);
      }
    }
    finally
    {
      regionsLock.readLock().unlock();
    }
  }

  public void subscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle,
    AttributeRegionAssociation attributeRegionAssociation, boolean passive)
    throws InvalidRegion, RegionNotCreatedByThisFederate
  {
    for (RegionHandle regionHandle : attributeRegionAssociation.regions)
    {
      // TODO: don't forget passive

      getRegion(regionHandle).subscribe(
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
      getRegion(regionHandle).unsubscribe(
        objectClassHandle, attributeRegionAssociation.attributes);
    }
  }

  public void subscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle,
    RegionHandleSet regionHandles, boolean passive)
    throws InvalidRegion, RegionNotCreatedByThisFederate
  {
    for (RegionHandle regionHandle : regionHandles)
    {
      getRegion(regionHandle).subscribe(interactionClassHandle);
    }
  }

  public void unsubscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle,
    RegionHandleSet regionHandles)
    throws InvalidRegion, RegionNotCreatedByThisFederate
  {
    for (RegionHandle regionHandle : regionHandles)
    {
      getRegion(regionHandle).unsubscribe(interactionClassHandle);
    }
  }

  protected FederateRegion getRegion(RegionHandle regionHandle)
    throws RegionNotCreatedByThisFederate
  {
    FederateRegion region = regions.get(regionHandle);
    if (region == null)
    {
      throw new RegionNotCreatedByThisFederate(
        String.format("%s", regionHandle));
    }
    return region;
  }

  protected void checkIfRegionNotCreatedByThisFederate(
    RegionHandle regionHandle)
  throws RegionNotCreatedByThisFederate
  {
    if (!regions.containsKey(regionHandle))
    {
      throw new RegionNotCreatedByThisFederate(
        String.format("%s", regionHandle));
    }
  }

  protected void checkIfRegionNotCreatedByThisFederate(
    RegionHandleSet regionHandles)
  throws RegionNotCreatedByThisFederate
  {
    for (RegionHandle regionHandle : regionHandles)
    {
      checkIfRegionNotCreatedByThisFederate(regionHandle);
    }
  }
}
