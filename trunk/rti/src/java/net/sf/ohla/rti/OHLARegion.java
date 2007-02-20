/*
 * Copyright (c) 2006, Michael Newcomb
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

package net.sf.ohla.rti;

import java.util.ArrayList;
import java.util.List;

import net.sf.ohla.rti.Extent;
import net.sf.ohla.rti1516.fdd.Dimension;
import net.sf.ohla.rti1516.impl.OHLARegionHandleSet;

import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.Region;

import hla.rti1516.RegionHandle;
import hla.rti1516.RegionHandleSet;

public class OHLARegion
  implements Region
{
  protected int token;
  protected int routingSpaceHandle;
  protected List<Extent> extents;

  protected transient RegionHandleSet regionHandles;

  public OHLARegion(int token, int routingSpaceHandle,
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

  protected OHLARegion(OHLARegion region)
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
      regionHandles = new OHLARegionHandleSet();
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
}