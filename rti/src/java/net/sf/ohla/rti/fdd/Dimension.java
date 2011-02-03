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

package net.sf.ohla.rti.fdd;

import net.sf.ohla.rti.Protocol;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandle;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.DimensionHandle;
import hla.rti1516e.RangeBounds;
import hla.rti1516e.exceptions.InvalidRangeBound;

public class Dimension
{
  private final DimensionHandle dimensionHandle;
  private final String dimensionName;

  private long upperBound = Long.MAX_VALUE;

  public Dimension(DimensionHandle dimensionHandle, String dimensionName)
  {
    this.dimensionHandle = dimensionHandle;
    this.dimensionName = dimensionName;
  }

  public Dimension(ChannelBuffer buffer)
  {
    dimensionHandle = IEEE1516eDimensionHandle.decode(buffer);
    dimensionName = Protocol.decodeString(buffer);
    upperBound = Protocol.decodeVarLong(buffer);
  }

  public DimensionHandle getDimensionHandle()
  {
    return dimensionHandle;
  }

  public String getDimensionName()
  {
    return dimensionName;
  }

  public long getUpperBound()
  {
    return upperBound;
  }

  public void setUpperBound(long upperBound)
  {
    this.upperBound = upperBound;
  }

  public void validate(RangeBounds rangeBounds)
    throws InvalidRangeBound
  {
    if (rangeBounds.upper > upperBound)
    {
      throw new InvalidRangeBound(String.format(
        "invalid range bound: %d > %d", rangeBounds.upper, upperBound));
    }
  }

  @Override
  public int hashCode()
  {
    return dimensionHandle.hashCode();
  }

  @Override
  public String toString()
  {
    return dimensionName;
  }

  public static void encode(ChannelBuffer buffer, Dimension dimension)
  {
    IEEE1516eDimensionHandle.encode(buffer, dimension.dimensionHandle);
    Protocol.encodeString(buffer, dimension.dimensionName);
    Protocol.encodeVarLong(buffer, dimension.upperBound);
  }

  public static Dimension decode(ChannelBuffer buffer)
  {
    return new Dimension(buffer);
  }
}
