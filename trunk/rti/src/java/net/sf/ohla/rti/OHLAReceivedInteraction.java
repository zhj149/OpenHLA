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
import hla.rti.ReceivedInteraction;
import hla.rti.Region;

import hla.rti1516.ParameterHandle;
import hla.rti1516.ParameterHandleValueMap;

public class OHLAReceivedInteraction
  implements ReceivedInteraction
{
  protected ParameterHandleValueMap parameterValues;
  protected List<ParameterHandle> parameterHandles;

  protected int orderType;
  protected int transportationType;

  protected Region region;

  public OHLAReceivedInteraction(ParameterHandleValueMap parameterValues,
                                 int orderType, int transportationType)
  {
    this(parameterValues, orderType, transportationType, null);
  }

  public OHLAReceivedInteraction(ParameterHandleValueMap parameterValues,
                                 int orderType, int transportationType,
                                 Region region)
  {
    this.parameterValues = parameterValues;

    this.orderType = orderType;
    this.transportationType = transportationType;

    this.region = region;

    // index the parameter handles
    //
    parameterHandles = new ArrayList<ParameterHandle>(parameterValues.size());
    for (ParameterHandle parameterHandle : parameterValues.keySet())
    {
      parameterHandles.add(parameterHandle);
    }
  }

  public int size()
  {
    return parameterValues.size();
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
      return parameterValues.get(parameterHandles.get(index));
    }
    catch (IndexOutOfBoundsException ioobe)
    {
      throw new ArrayIndexOutOfBounds(ioobe);
    }
  }

  public int getParameterHandle(int index)
    throws ArrayIndexOutOfBounds
  {
    return parameterHandles.get(index).hashCode();
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
}
