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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.sf.ohla.rti1516.fdd.Dimension;

import hla.rti.ArrayIndexOutOfBounds;

import hla.rti1516.RangeBounds;
import hla.rti1516.RegionHandle;

public class Extent
  implements Serializable
{
  protected RegionHandle regionHandle;
  protected List<RangeBounds> rangeBounds;

  public Extent(Extent extent)
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

  public Extent(RegionHandle regionHandle, List<Dimension> dimensions)
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
