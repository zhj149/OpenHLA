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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sf.ohla.rti.fdd.Dimension;
import net.sf.ohla.rti.fdd.FDD;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandleSet;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eRegionHandle;

import hla.rti1516e.DimensionHandle;
import hla.rti1516e.DimensionHandleSet;
import hla.rti1516e.RangeBounds;
import hla.rti1516e.RegionHandle;

public class FederationExecutionRegion
{
  private final RegionHandle regionHandle;
  private final DimensionHandleSet dimensionHandles;

  private final Map<DimensionHandle, RangeBounds> rangeBounds = new HashMap<DimensionHandle, RangeBounds>();

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

  public FederationExecutionRegion(DataInput in)
    throws IOException
  {
    regionHandle = IEEE1516eRegionHandle.decode(in);

    dimensionHandles = new IEEE1516eDimensionHandleSet();
    for (int count = in.readInt(); count > 0; count--)
    {
      DimensionHandle dimensionHandle = IEEE1516eDimensionHandle.decode(in);
      dimensionHandles.add(dimensionHandle);

      long lower = in.readLong();
      long upper = in.readLong();
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
    Map<DimensionHandle, RangeBounds> rangeBounds = new HashMap<DimensionHandle, RangeBounds>();
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

  public void writeTo(DataOutput out)
    throws IOException
  {
    ((IEEE1516eRegionHandle) regionHandle).writeTo(out);

    out.writeInt(dimensionHandles.size());
    for (DimensionHandle dimensionHandle : dimensionHandles)
    {
      ((IEEE1516eDimensionHandle) dimensionHandle).writeTo(out);

      RangeBounds rangeBounds = this.rangeBounds.get(dimensionHandle);
      assert rangeBounds != null;

      out.writeLong(rangeBounds.lower);
      out.writeLong(rangeBounds.upper);
    }
  }

  protected boolean intersects(RangeBounds lhs, RangeBounds rhs)
  {
    return (lhs.lower < rhs.upper && rhs.lower < lhs.upper) || lhs.lower == rhs.lower;
  }
}
