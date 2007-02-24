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

package net.sf.ohla.rti.hla.rti;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

import net.sf.ohla.rti.fdd.Dimension;
import net.sf.ohla.rti.hla.rti1516.IEEE1516RegionHandleSet;

import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.Region;

import hla.rti1516.RangeBounds;
import hla.rti1516.RegionHandle;
import hla.rti1516.RegionHandleSet;

public class HLA13Region
  implements Region
{
  protected int token;
  protected int routingSpaceHandle;
  protected List<Extent> extents;

  protected transient RegionHandleSet regionHandles;

  public HLA13Region(int token, int routingSpaceHandle,
                    List<Dimension> dimensions,
                    RegionHandleSet regionHandles)
  {
    this.token = token;
    this.routingSpaceHandle = routingSpaceHandle;
    this.regionHandles = regionHandles;

    extents = new ArrayList<Extent>(regionHandles.size());
    for (RegionHandle regionHandle : regionHandles)
    {
      extents.add(new Extent(regionHandle, dimensions));
    }
  }

  protected HLA13Region(HLA13Region region)
  {
    token = region.token;
    routingSpaceHandle = region.routingSpaceHandle;
    extents = new ArrayList<Extent>(region.extents.size());
    for (Extent extent : region.extents)
    {
      extents.add(new Extent(extent));
    }
  }

  public int getToken()
  {
    return token;
  }

  public synchronized RegionHandleSet getRegionHandles()
  {
    if (regionHandles == null)
    {
      regionHandles = new IEEE1516RegionHandleSet();
      for (Extent extent : extents)
      {
        regionHandles.add(extent.getRegionHandle());
      }
    }
    return regionHandles;
  }

  public List<Extent> getExtents()
  {
    return extents;
  }

  public Extent getExtent(int extentIndex)
    throws ArrayIndexOutOfBounds
  {
    try
    {
      return extents.get(extentIndex);
    }
    catch (IndexOutOfBoundsException ioobe)
    {
      throw new ArrayIndexOutOfBounds(ioobe);
    }
  }

  public int getSpaceHandle()
  {
    return routingSpaceHandle;
  }

  public long getNumberOfExtents()
  {
    return extents.size();
  }

  public long getRangeLowerBound(int extentIndex, int dimensionHandle)
    throws ArrayIndexOutOfBounds
  {
    return getExtent(extentIndex).getRangeLowerBound(dimensionHandle);
  }

  public long getRangeUpperBound(int extentIndex, int dimensionHandle)
    throws ArrayIndexOutOfBounds
  {
    return getExtent(extentIndex).getRangeUpperBound(dimensionHandle);
  }

  public void setRangeLowerBound(int extentIndex, int dimensionHandle,
                                 long lowerBound)
    throws ArrayIndexOutOfBounds
  {
    getExtent(extentIndex).setRangeLowerBound(dimensionHandle, lowerBound);
  }

  public void setRangeUpperBound(int extentIndex, int dimensionHandle,
                                 long upperBound)
    throws ArrayIndexOutOfBounds
  {
    getExtent(extentIndex).setRangeUpperBound(dimensionHandle, upperBound);
  }

  public class Extent
    implements Serializable
  {
    protected RegionHandle regionHandle;
    protected List<RangeBounds> rangeBounds;

    protected Extent(Extent extent)
    {
      regionHandle = extent.regionHandle;
      rangeBounds = new ArrayList<RangeBounds>(extent.rangeBounds.size());
      for (RangeBounds rangeBounds : extent.rangeBounds)
      {
        RangeBounds tempRangeBounds = new RangeBounds();
        tempRangeBounds.lower = rangeBounds.lower;
        tempRangeBounds.upper = rangeBounds.upper;
        this.rangeBounds.add(tempRangeBounds);
      }
    }

    protected Extent(RegionHandle regionHandle, List<Dimension> dimensions)
    {
      this.regionHandle = regionHandle;

      rangeBounds = new ArrayList<RangeBounds>(dimensions.size());

      // initialize all dimensions to default range bounds
      //
      for (Dimension dimension : dimensions)
      {
        rangeBounds.add(new RangeBounds());
      }
    }

    public RegionHandle getRegionHandle()
    {
      return regionHandle;
    }

    public List<RangeBounds> getRangeBounds()
    {
      return rangeBounds;
    }

    public RangeBounds getRangeBounds(int dimensionHandle)
      throws ArrayIndexOutOfBounds
    {
      if (dimensionHandle >= rangeBounds.size())
      {
        throw new ArrayIndexOutOfBounds(Integer.toString(dimensionHandle));
      }

      return rangeBounds.get(dimensionHandle);
    }

    public long getRangeLowerBound(int dimensionHandle)
      throws ArrayIndexOutOfBounds
    {
      return getRangeBounds(dimensionHandle).lower;
    }

    public long getRangeUpperBound(int dimensionHandle)
      throws ArrayIndexOutOfBounds
    {
      return getRangeBounds(dimensionHandle).upper;
    }

    public void setRangeLowerBound(int dimensionHandle, long lowerBound)
      throws ArrayIndexOutOfBounds
    {
      getRangeBounds(dimensionHandle).lower = lowerBound;
    }

    public void setRangeUpperBound(int dimensionHandle, long upperBound)
      throws ArrayIndexOutOfBounds
    {
      getRangeBounds(dimensionHandle).upper = upperBound;
    }
  }
}
