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

import java.util.ArrayList;
import java.util.List;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleValueMap;

import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.SuppliedAttributes;

import hla.rti1516e.AttributeHandle;

public class HLA13SuppliedAttributes
  extends IEEE1516eAttributeHandleValueMap
  implements SuppliedAttributes
{
  private final List<AttributeHandle> attributeHandles;

  public HLA13SuppliedAttributes()
  {
    super();

    attributeHandles = new ArrayList<AttributeHandle>();
  }

  public HLA13SuppliedAttributes(int initialCapacity)
  {
    super(initialCapacity);

    attributeHandles = new ArrayList<AttributeHandle>(initialCapacity);
  }

  public int getHandle(int index)
    throws ArrayIndexOutOfBounds
  {
    try
    {
      return ((IEEE1516eAttributeHandle) attributeHandles.get(index)).getHandle();
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
      return get(attributeHandles.get(index));
    }
    catch (IndexOutOfBoundsException ioobe)
    {
      throw new ArrayIndexOutOfBounds(ioobe);
    }
  }

  public void add(int attributeHandle, byte[] value)
  {
    AttributeHandle ohlaAttributeHandle = new IEEE1516eAttributeHandle(attributeHandle);
    if (put(ohlaAttributeHandle, value) == null)
    {
      attributeHandles.add(ohlaAttributeHandle);
    }
  }

  public void remove(int attributeHandle)
  {
    remove(attributeHandles.remove(attributeHandle));
  }

  public void removeAt(int index)
    throws ArrayIndexOutOfBounds
  {
    try
    {
      remove(attributeHandles.remove(index));
    }
    catch (IndexOutOfBoundsException ioobe)
    {
      throw new ArrayIndexOutOfBounds(ioobe);
    }
  }

  public void empty()
  {
    clear();
    attributeHandles.clear();
  }
}
