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
import hla.rti.ReceivedInteraction;

public class HLA13ReceivedInteraction
  implements ReceivedInteraction
{
  private final int[] parameterHandles;
  private final byte[][] parameterValues;

  private final int orderType;
  private final int transportationType;

  private final HLA13Region region;

  public HLA13ReceivedInteraction(
    int[] parameterHandles, byte[][] parameterValues, int orderType, int transportationType)
  {
    this(parameterHandles, parameterValues, orderType, transportationType, null);
  }

  public HLA13ReceivedInteraction(
    int[] parameterHandles, byte[][] parameterValues, int orderType, int transportationType, HLA13Region region)
  {
    this.parameterHandles = parameterHandles;
    this.parameterValues = parameterValues;
    this.orderType = orderType;
    this.transportationType = transportationType;
    this.region = region;
  }

  public int size()
  {
    return parameterHandles.length;
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

    return parameterValues[index];
  }

  public int getParameterHandle(int index)
    throws ArrayIndexOutOfBounds
  {
    checkRange(index);

    return parameterHandles[index];
  }

  public int getOrderType()
  {
    return orderType;
  }

  public int getTransportType()
  {
    return transportationType;
  }

  public HLA13Region getRegion()
  {
    return region;
  }

  private void checkRange(int index)
    throws ArrayIndexOutOfBounds
  {
    if (index < 0 || index >= parameterHandles.length)
    {
      throw new ArrayIndexOutOfBounds(Integer.toString(index));
    }
  }
}
