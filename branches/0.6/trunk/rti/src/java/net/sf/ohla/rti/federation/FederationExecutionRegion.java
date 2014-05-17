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

import net.sf.ohla.rti.util.DimensionHandles;
import net.sf.ohla.rti.util.RegionHandles;
import net.sf.ohla.rti.fdd.Dimension;
import net.sf.ohla.rti.fdd.FDD;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandleSetFactory;
import net.sf.ohla.rti.proto.FederationExecutionSaveProtos;
import net.sf.ohla.rti.proto.FederationExecutionSaveProtos.FederationExecutionState.FederationExecutionRegionManagerState.FederationExecutionRegionState;
import net.sf.ohla.rti.proto.OHLAProtos;

import hla.rti1516e.DimensionHandle;
import hla.rti1516e.DimensionHandleSet;
import hla.rti1516e.RangeBounds;
import hla.rti1516e.RegionHandle;

public class FederationExecutionRegion
{
  private final RegionHandle regionHandle;
  private final DimensionHandleSet dimensionHandles;

  private final Map<DimensionHandle, RangeBounds> rangeBounds = new HashMap<>();

  public FederationExecutionRegion(RegionHandle regionHandle, DimensionHandleSet dimensionHandles, FDD fdd)
  {
    this.regionHandle = regionHandle;
    this.dimensionHandles = dimensionHandles;

    // initialize our range bounds to the dimension defaults
    //
    for (DimensionHandle dimensionHandle : dimensionHandles)
    {
      Dimension dimension = fdd.getDimensionSafely(dimensionHandle);

      rangeBounds.put(dimensionHandle, new RangeBounds(0L, dimension.getUpperBound()));
    }
  }

  public FederationExecutionRegion(FederationExecutionSaveProtos.FederationExecutionState.FederationExecutionRegionManagerState.FederationExecutionRegionState regionState)
  {
    regionHandle = RegionHandles.convert(regionState.getRegionHandle());

    dimensionHandles = IEEE1516eDimensionHandleSetFactory.INSTANCE.create();
    for (OHLAProtos.DimensionRangeBound dimensionRangeBound : regionState.getRangeBoundsList())
    {
      DimensionHandle dimensionHandle = DimensionHandles.convert(dimensionRangeBound.getDimensionHandle());
      dimensionHandles.add(dimensionHandle);

      long lower = dimensionRangeBound.getLowerBound();
      long upper = dimensionRangeBound.getUpperBound();
      rangeBounds.put(dimensionHandle, new RangeBounds(lower, upper));
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
  {
    return rangeBounds.get(dimensionHandle);
  }

  public Map<DimensionHandle, RangeBounds> copyRangeBounds()
  {
    Map<DimensionHandle, RangeBounds> rangeBounds = new HashMap<>();
    for (Map.Entry<DimensionHandle, RangeBounds> entry : this.rangeBounds.entrySet())
    {
      rangeBounds.put(entry.getKey(), entry.getValue());
    }
    return rangeBounds;
  }

  public void commitRegionModifications(Map<DimensionHandle, RangeBounds> rangeBounds)
  {
    this.rangeBounds.putAll(rangeBounds);
  }

  public void delete()
  {
    // TODO: notify all our intersecting regions
  }

  public boolean intersects(FederationExecutionRegion region, Set<DimensionHandle> dimensionHandles)
  {
    boolean intersects = false;

    for (Iterator<DimensionHandle> i = dimensionHandles.iterator(); !intersects && i.hasNext();)
    {
      DimensionHandle dimensionHandle = i.next();

      RangeBounds lhs = rangeBounds.get(dimensionHandle);
      RangeBounds rhs = region.rangeBounds.get(dimensionHandle);

      if (lhs != null && rhs != null)
      {
        intersects = intersects(lhs, rhs);
      }
    }

    return intersects;
  }

  public FederationExecutionRegionState.Builder saveState()
  {
    FederationExecutionRegionState.Builder regionState = FederationExecutionRegionState.newBuilder();

    regionState.setRegionHandle(RegionHandles.convert(regionHandle));

    for (DimensionHandle dimensionHandle : dimensionHandles)
    {
      OHLAProtos.DimensionRangeBound.Builder dimensionRangeBound = OHLAProtos.DimensionRangeBound.newBuilder();

      dimensionRangeBound.setDimensionHandle(DimensionHandles.convert(dimensionHandle));

      RangeBounds rangeBounds = this.rangeBounds.get(dimensionHandle);
      assert rangeBounds != null;

      dimensionRangeBound.setLowerBound(rangeBounds.lower);
      dimensionRangeBound.setUpperBound(rangeBounds.upper);
    }

    return regionState;
  }

  protected boolean intersects(RangeBounds lhs, RangeBounds rhs)
  {
    return (lhs.lower < rhs.upper && rhs.lower < lhs.upper) || lhs.lower == rhs.lower;
  }
}
