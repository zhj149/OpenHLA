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

import hla.rti1516e.DimensionHandle;
import hla.rti1516e.DimensionHandleSet;

public class IEEE1516eDimensionHandleSet
  extends HashSet<DimensionHandle>
  implements DimensionHandleSet
{
  public IEEE1516eDimensionHandleSet()
  {
  }

  public IEEE1516eDimensionHandleSet(int initialCapacity)
  {
    super(initialCapacity);
  }

  public IEEE1516eDimensionHandleSet(Set<DimensionHandle> dimensionHandles)
  {
    super(dimensionHandles);
  }

  public IEEE1516eDimensionHandleSet(IEEE1516eDimensionHandleSet dimensionHandles)
  {
    super(dimensionHandles);
  }
  public IEEE1516eDimensionHandleSet(DataInput in)
    throws IOException
  {
    for (int count = in.readInt(); count > 0; count--)
    {
      add(IEEE1516eDimensionHandle.decode(in));
    }
  }

  public void writeTo(DataOutput out)
    throws IOException
  {
    out.writeInt(size());
    for (DimensionHandle dimensionHandle : this)
    {
      ((IEEE1516eDimensionHandle) dimensionHandle).writeTo(out);
    }
  }

  @Override
  public IEEE1516eDimensionHandleSet clone()
  {
    return new IEEE1516eDimensionHandleSet(this);
  }

  public static void encode(ChannelBuffer buffer, DimensionHandleSet dimensionHandles)
  {
    if (dimensionHandles == null)
    {
      Protocol.encodeVarInt(buffer, 0);
    }
    else
    {
      Protocol.encodeVarInt(buffer, dimensionHandles.size());

      for (DimensionHandle dimensionHandle : dimensionHandles)
      {
        IEEE1516eDimensionHandle.encode(buffer, dimensionHandle);
      }
    }
  }

  public static IEEE1516eDimensionHandleSet decode(ChannelBuffer buffer)
  {
    IEEE1516eDimensionHandleSet dimensionHandles;

    int size = Protocol.decodeVarInt(buffer);
    if (size == 0)
    {
      dimensionHandles = null;
    }
    else
    {
      dimensionHandles = new IEEE1516eDimensionHandleSet();

      for (; size > 0; size--)
      {
        dimensionHandles.add(IEEE1516eDimensionHandle.decode(buffer));
      }
    }

    return dimensionHandles;
  }
}
