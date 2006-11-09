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

import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.ReflectedAttributes;
import hla.rti.Region;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleValueMap;

public class OHLAReflectedAttributes
  implements ReflectedAttributes
{
  protected AttributeHandleValueMap attributeValues;
  protected List<AttributeHandle> attributeHandles;

  protected int orderType;
  protected int transportationType;

  protected Region region;

  public OHLAReflectedAttributes(AttributeHandleValueMap attributeValues,
                                 int orderType, int transportationType)
  {
    this(attributeValues, orderType, transportationType, null);
  }

  public OHLAReflectedAttributes(
    AttributeHandleValueMap attributeValues,
    int orderType, int transportationType,
    Region region)
  {
    this.attributeValues = attributeValues;

    this.orderType = orderType;
    this.transportationType = transportationType;

    this.region = region;

    // index the attribute handles
    //
    attributeHandles = new ArrayList<AttributeHandle>(attributeValues.size());
    for (AttributeHandle attributeHandle : attributeValues.keySet())
    {
      attributeHandles.add(attributeHandle);
    }
  }

  public int getOrderType()
  {
    return orderType;
  }

  public int getTransportType()
  {
    return transportationType;
  }

  public Region getRegion()
  {
    return region;
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
    try
    {
      return attributeValues.get(attributeHandles.get(index));
    }
    catch (IndexOutOfBoundsException ioobe)
    {
      throw new ArrayIndexOutOfBounds(ioobe);
    }
  }

  public int getAttributeHandle(int index)
    throws ArrayIndexOutOfBounds
  {
    return attributeHandles.get(index).hashCode();
  }

  public Region getRegion(int index)
    throws ArrayIndexOutOfBounds
  {
    // only one region for the entire collection
    //
    return getRegion();
  }

  public int getOrderType(int index)
    throws ArrayIndexOutOfBounds
  {
    // only one type for the entire collection
    //
    return getOrderType();
  }

  public int getTransportType(int index)
    throws ArrayIndexOutOfBounds
  {
    // only one type for the entire collection
    //
    return getTransportType();
  }

  public int size()
  {
    return attributeValues.size();
  }
}
