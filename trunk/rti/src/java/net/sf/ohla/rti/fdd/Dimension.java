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

import java.io.Serializable;

import net.sf.ohla.rti.hla.rti1516.IEEE1516DimensionHandle;

import org.dom4j.Element;

import hla.rti1516.DimensionHandle;
import hla.rti1516.ErrorReadingFDD;
import hla.rti1516.InvalidRangeBound;
import hla.rti1516.RangeBounds;

public class Dimension
  implements Serializable
{
  protected DimensionHandle dimensionHandle;

  protected String name;
  protected String upperBound;

  protected RangeBounds rangeBounds = new RangeBounds();

  public Dimension(String name)
  {
    this.name = name;

    dimensionHandle = new IEEE1516DimensionHandle(name);
  }

  public Dimension(Element dimension)
    throws ErrorReadingFDD
  {
    this(
      ((org.dom4j.Attribute) dimension.selectSingleNode("@name")).getValue());

    org.dom4j.Attribute attribute =
      (org.dom4j.Attribute) dimension.selectSingleNode("@upperBound");
    if (attribute != null)
    {
      upperBound = attribute.getValue();

      try
      {
        rangeBounds.upper = Long.parseLong(upperBound);
      }
      catch (NumberFormatException nfe)
      {
        throw new ErrorReadingFDD(String.format(
          "invalid upper bound: %s", upperBound), nfe);
      }
    }
  }

  public DimensionHandle getDimensionHandle()
  {
    return dimensionHandle;
  }

  public String getName()
  {
    return name;
  }

  public String getUpperBound()
  {
    return upperBound;
  }

  public RangeBounds getRangeBounds()
  {
    RangeBounds rangeBounds = new RangeBounds();
    rangeBounds.lower = this.rangeBounds.lower;
    rangeBounds.upper = this.rangeBounds.upper;
    return rangeBounds;
  }

  public void validate(RangeBounds rangeBounds)
    throws InvalidRangeBound
  {
    if (rangeBounds.lower < this.rangeBounds.lower)
    {
      throw new InvalidRangeBound(String.format(
        "invalid range bound: %d < %d", rangeBounds.lower,
        this.rangeBounds.lower));
    }
    else if (rangeBounds.upper > this.rangeBounds.upper)
    {
      throw new InvalidRangeBound(String.format(
        "invalid range bound: %d > %d", rangeBounds.upper,
        this.rangeBounds.upper));
    }
    else if ((rangeBounds.upper - rangeBounds.lower) < 1)
    {
      throw new InvalidRangeBound(String.format(
        "invalid range bound: %d <= %d", rangeBounds.upper, rangeBounds.lower));
    }
  }

  @Override
  public boolean equals(Object rhs)
  {
    return rhs instanceof Dimension &&
           dimensionHandle.equals(((Dimension) rhs).dimensionHandle);
  }

  @Override
  public int hashCode()
  {
    return dimensionHandle.hashCode();
  }

  @Override
  public String toString()
  {
    return name;
  }
}
