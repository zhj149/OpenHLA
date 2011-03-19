/*
 * Copyright (c) 2005-2010, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.hla.rti1516e;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.ohla.rti.Protocol;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.encoding.ByteWrapper;

public class IEEE1516eAttributeHandleValueMap
  extends HashMap<AttributeHandle, byte[]>
  implements AttributeHandleValueMap
{
  public IEEE1516eAttributeHandleValueMap()
  {
  }

  public IEEE1516eAttributeHandleValueMap(int initialCapacity)
  {
    super(initialCapacity);
  }

  public IEEE1516eAttributeHandleValueMap(Map<AttributeHandle, byte[]> clonee)
  {
    super(clonee);
  }

  public ByteWrapper getValueReference(AttributeHandle attributeHandle)
  {
    byte[] buffer = get(attributeHandle);
    return buffer == null ? null : new ByteWrapper(buffer);
  }

  public ByteWrapper getValueReference(AttributeHandle attributeHandle, ByteWrapper byteWrapper)
  {
    byte[] buffer = get(attributeHandle);
    if (buffer == null)
    {
      byteWrapper = null;
    }
    else
    {
      byteWrapper.put(buffer);
    }
    return byteWrapper;
  }

  @Override
  public boolean equals(Object rhs)
  {
    return this == rhs || (rhs instanceof AttributeHandleValueMap && equals((AttributeHandleValueMap) rhs));
  }

  private boolean equals(AttributeHandleValueMap rhs)
  {
    boolean equals = size() == rhs.size();
    if (equals)
    {
      for (Iterator<Map.Entry<AttributeHandle, byte[]>> i = entrySet().iterator(); i.hasNext() && equals;)
      {
        Map.Entry<AttributeHandle, byte[]> entry = i.next();
        equals = Arrays.equals(entry.getValue(), rhs.get(entry.getKey()));
      }
    }
    return equals;
  }

  public static void encode(ChannelBuffer buffer, AttributeHandleValueMap attributeHandles)
  {
    if (attributeHandles == null)
    {
      Protocol.encodeVarInt(buffer, 0);
    }
    else
    {
      Protocol.encodeVarInt(buffer, attributeHandles.size());

      for (Map.Entry<AttributeHandle, byte[]> entry : attributeHandles.entrySet())
      {
        IEEE1516eAttributeHandle.encode(buffer, entry.getKey());
        Protocol.encodeBytes(buffer, entry.getValue());
      }
    }
  }

  public static IEEE1516eAttributeHandleValueMap decode(ChannelBuffer buffer)
  {
    IEEE1516eAttributeHandleValueMap attributeHandles;

    int size = Protocol.decodeVarInt(buffer);
    if (size == 0)
    {
      attributeHandles = null;
    }
    else
    {
      attributeHandles = new IEEE1516eAttributeHandleValueMap(size);

      for (; size > 0; size--)
      {
        attributeHandles.put(IEEE1516eAttributeHandle.decode(buffer), Protocol.decodeBytes(buffer));
      }
    }

    return attributeHandles;
  }
}
