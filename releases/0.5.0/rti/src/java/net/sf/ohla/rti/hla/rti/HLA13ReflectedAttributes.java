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

import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.ReflectedAttributes;
import hla.rti.Region;

public class HLA13ReflectedAttributes
  implements ReflectedAttributes
{
  private final int[] attributeHandles;
  private final byte[][] attributeValues;

  private final int orderType;
  private final int transportationType;

  private final HLA13Region[] regions;

  public HLA13ReflectedAttributes(
    int[] attributeHandles, byte[][] attributeValues, int orderType, int transportationType)
  {
    this(attributeHandles, attributeValues, orderType, transportationType, null);
  }

  public HLA13ReflectedAttributes(
    int[] attributeHandles, byte[][] attributeValues, int orderType, int transportationType, HLA13Region[] regions)
  {
    this.attributeHandles = attributeHandles;
    this.attributeValues = attributeValues;
    this.orderType = orderType;
    this.transportationType = transportationType;
    this.regions = regions;
  }

  public HLA13Region[] getRegions()
  {
    return regions;
  }

  public byte[] getValue(int index)
    throws ArrayIndexOutOfBounds
  {
    return getValueReference(index).clone();
  }

  public int getValueLength(int index)
    throws ArrayIndexOutOfBounds
  {
    return getValueReference(index).length;
  }

  public byte[] getValueReference(int index)
    throws ArrayIndexOutOfBounds
  {
    checkRange(index);

    return attributeValues[index];
  }

  public int getAttributeHandle(int index)
    throws ArrayIndexOutOfBounds
  {
    checkRange(index);

    return attributeHandles[index];
  }

  public Region getRegion(int index)
    throws ArrayIndexOutOfBounds
  {
    if (regions == null)
    {
      throw new ArrayIndexOutOfBounds(Integer.toString(index));
    }

    checkRange(index);

    return regions[index];
  }

  public int getOrderType(int index)
  {
    // only one type for the entire collection
    //
    return orderType;
  }

  public int getTransportType(int index)
  {
    // only one type for the entire collection
    //
    return transportationType;
  }

  public int size()
  {
    return attributeHandles.length;
  }

  private void checkRange(int index)
    throws ArrayIndexOutOfBounds
  {
    if (index < 0 || index >= attributeHandles.length)
    {
      throw new ArrayIndexOutOfBounds(Integer.toString(index));
    }
  }
}
