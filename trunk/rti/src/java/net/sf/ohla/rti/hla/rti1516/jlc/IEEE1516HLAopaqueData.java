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

package net.sf.ohla.rti.hla.rti1516.jlc;

import java.util.Arrays;
import java.util.Iterator;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.HLAopaqueData;

public class IEEE1516HLAopaqueData
  extends IEEE1516DataElement
  implements HLAopaqueData
{
  protected byte[] bytes;

  public IEEE1516HLAopaqueData()
  {
    bytes = new byte[0];
  }

  public IEEE1516HLAopaqueData(byte[] bytes)
  {
    this.bytes = bytes;
  }

  public int size()
  {
    return bytes.length;
  }

  public byte get(int index)
  {
    return bytes[index];
  }

  public Iterator<Byte> iterator()
  {
    return new Iterator()
    {
      protected int index = 0;

      public void remove()
      {
        throw new UnsupportedOperationException();
      }

      public boolean hasNext()
      {
        return index < bytes.length;
      }

      public Object next()
      {
        return bytes[index++];
      }
    };
  }

  public void encode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(4);
    byteWrapper.putInt(bytes.length);
    byteWrapper.put(bytes);
  }

  public void decode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(4);
    bytes = new byte[byteWrapper.getInt()];
    byteWrapper.get(bytes);
  }

  public int getEncodedLength()
  {
    return 4 + bytes.length;
  }

  public int getOctetBoundary()
  {
    return 4;
  }

  public byte[] getValue()
  {
    return bytes;
  }

  @Override
  public int hashCode()
  {
    return Arrays.hashCode(bytes);
  }

  @Override
  public boolean equals(Object rhs)
  {
    return rhs != null && rhs.getClass().isArray() &&
           rhs.getClass().getComponentType().equals(byte.class) &&
           Arrays.equals(bytes, (byte[]) rhs);
  }

  @Override
  public String toString()
  {
    return Arrays.toString(bytes);
  }
}
