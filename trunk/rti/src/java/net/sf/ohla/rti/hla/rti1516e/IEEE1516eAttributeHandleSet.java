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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.HashSet;
import java.util.Set;

import net.sf.ohla.rti.Protocol;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;

public class IEEE1516eAttributeHandleSet
  extends HashSet<AttributeHandle>
  implements AttributeHandleSet
{
  public IEEE1516eAttributeHandleSet()
  {
  }

  public IEEE1516eAttributeHandleSet(int initialCapacity)
  {
    super(initialCapacity);
  }

  public IEEE1516eAttributeHandleSet(Set<AttributeHandle> attributeHandles)
  {
    super(attributeHandles);
  }

  public IEEE1516eAttributeHandleSet(IEEE1516eAttributeHandleSet attributeHandles)
  {
    super(attributeHandles);
  }

  public IEEE1516eAttributeHandleSet(DataInput in)
    throws IOException
  {
    for (int count = in.readInt(); count > 0; count--)
    {
      add(IEEE1516eAttributeHandle.decode(in));
    }
  }

  public void writeTo(DataOutput out)
    throws IOException
  {
    out.writeInt(size());
    for (AttributeHandle attributeHandle : this)
    {
      ((IEEE1516eAttributeHandle) attributeHandle).writeTo(out);
    }
  }

  @Override
  public IEEE1516eAttributeHandleSet clone()
  {
    return new IEEE1516eAttributeHandleSet(this);
  }

  public static void encode(ChannelBuffer buffer, AttributeHandleSet attributeHandles)
  {
    if (attributeHandles == null)
    {
      Protocol.encodeVarInt(buffer, 0);
    }
    else
    {
      Protocol.encodeVarInt(buffer, attributeHandles.size());

      for (AttributeHandle attributeHandle : attributeHandles)
      {
        IEEE1516eAttributeHandle.encode(buffer, attributeHandle);
      }
    }
  }

  public static IEEE1516eAttributeHandleSet decode(ChannelBuffer buffer)
  {
    IEEE1516eAttributeHandleSet attributeHandles;

    int size = Protocol.decodeVarInt(buffer);
    if (size == 0)
    {
      attributeHandles = null;
    }
    else
    {
      attributeHandles = new IEEE1516eAttributeHandleSet();

      for (; size > 0; size--)
      {
        attributeHandles.add(IEEE1516eAttributeHandle.decode(buffer));
      }
    }

    return attributeHandles;
  }
}
