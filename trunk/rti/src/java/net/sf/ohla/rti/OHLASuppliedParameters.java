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

import net.sf.ohla.rti1516.impl.OHLAParameterHandle;
import net.sf.ohla.rti1516.impl.OHLAParameterHandleValueMap;

import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.SuppliedParameters;

import hla.rti1516.ParameterHandle;

public class OHLASuppliedParameters
  extends OHLAParameterHandleValueMap
  implements SuppliedParameters
{
  protected List<ParameterHandle> parameterHandles =
    new ArrayList<ParameterHandle>();

  public OHLASuppliedParameters()
  {
    super();
  }

  public OHLASuppliedParameters(int initialCapacity)
  {
    super(initialCapacity);

    parameterHandles = new ArrayList<ParameterHandle>(initialCapacity);
  }

  public int getHandle(int index)
    throws ArrayIndexOutOfBounds
  {
    try
    {
      return parameterHandles.get(index).hashCode();
    }
    catch (IndexOutOfBoundsException ioobe)
    {
      throw new ArrayIndexOutOfBounds(ioobe);
    }
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
      return get(parameterHandles.get(index));
    }
    catch (IndexOutOfBoundsException ioobe)
    {
      throw new ArrayIndexOutOfBounds(ioobe);
    }
  }

  public void add(int parameterHandle, byte[] value)
  {
    ParameterHandle ohlaParameterHandle =
      new OHLAParameterHandle(parameterHandle);

    if (put(ohlaParameterHandle, value) == null)
    {
      parameterHandles.add(ohlaParameterHandle);
    }
  }

  public void remove(int parameterHandle)
  {
    remove(parameterHandles.remove(parameterHandle));
  }

  public void removeAt(int index)
    throws ArrayIndexOutOfBounds
  {
    try
    {
      remove(parameterHandles.remove(index));
    }
    catch (IndexOutOfBoundsException ioobe)
    {
      throw new ArrayIndexOutOfBounds(ioobe);
    }
  }

  public void empty()
  {
    clear();
    parameterHandles.clear();
  }
}
