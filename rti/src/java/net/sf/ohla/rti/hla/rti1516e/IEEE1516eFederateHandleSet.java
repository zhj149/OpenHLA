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

import java.io.DataOutput;
import java.io.IOException;
import java.io.DataInput;

import java.util.Collection;
import java.util.HashSet;

import net.sf.ohla.rti.Protocol;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateHandleSet;

public class IEEE1516eFederateHandleSet
  extends HashSet<FederateHandle>
  implements FederateHandleSet
{
  public static final FederateHandleSet EMPTY = new IEEE1516eFederateHandleSet();

  public IEEE1516eFederateHandleSet()
  {
  }

  public IEEE1516eFederateHandleSet(int initialCapacity)
  {
    super(initialCapacity);
  }

  public IEEE1516eFederateHandleSet(Collection<? extends FederateHandle> c)
  {
    super(c);
  }

  public IEEE1516eFederateHandleSet(IEEE1516eFederateHandleSet federateHandleSet)
  {
    super(federateHandleSet);
  }

  public IEEE1516eFederateHandleSet(DataInput in)
    throws IOException
  {
    for (int count = in.readInt(); count > 0; count--)
    {
      add(IEEE1516eFederateHandle.decode(in));
    }
  }

  public void writeTo(DataOutput out)
    throws IOException
  {
    out.writeInt(size());
    for (FederateHandle federateHandle : this)
    {
      ((IEEE1516eFederateHandle) federateHandle).writeTo(out);
    }
  }

  public static void encode(ChannelBuffer buffer, FederateHandleSet federateHandles)
  {
    if (federateHandles == null)
    {
      Protocol.encodeVarInt(buffer, 0);
    }
    else
    {
      Protocol.encodeVarInt(buffer, federateHandles.size());
      for (FederateHandle federateHandle : federateHandles)
      {
        IEEE1516eFederateHandle.encode(buffer, federateHandle);
      }
    }
  }

  public static IEEE1516eFederateHandleSet decode(ChannelBuffer buffer)
  {
    IEEE1516eFederateHandleSet federateHandles;

    int size = Protocol.decodeVarInt(buffer);
    if (size == 0)
    {
      federateHandles = null;
    }
    else
    {
      federateHandles = new IEEE1516eFederateHandleSet();

      for (; size > 0; size--)
      {
        federateHandles.add(IEEE1516eFederateHandle.decode(buffer));
      }
    }

    return federateHandles;
  }
}
