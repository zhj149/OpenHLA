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

import net.sf.ohla.rti.Protocol;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.RegionHandle;
import hla.rti1516e.RegionHandleSet;

public class IEEE1516eRegionHandleSet
  extends HashSet<RegionHandle>
  implements RegionHandleSet
{
  public IEEE1516eRegionHandleSet()
  {
  }

  public IEEE1516eRegionHandleSet(DataInput in)
    throws IOException
  {
    for (int count = in.readInt(); count > 0; count--)
    {
      add(IEEE1516eRegionHandle.decode(in));
    }
  }

  public void writeTo(DataOutput out)
    throws IOException
  {
    out.writeInt(size());
    for (RegionHandle federateHandle : this)
    {
      ((IEEE1516eRegionHandle) federateHandle).writeTo(out);
    }
  }

  public static void encode(ChannelBuffer buffer, RegionHandleSet regionHandles)
  {
    if (regionHandles == null)
    {
      Protocol.encodeVarInt(buffer, 0);
    }
    else
    {
      Protocol.encodeVarInt(buffer, regionHandles.size());
      for (RegionHandle regionHandle : regionHandles)
      {
        IEEE1516eRegionHandle.encode(buffer, regionHandle);
      }
    }
  }

  public static IEEE1516eRegionHandleSet decode(ChannelBuffer buffer)
  {
    IEEE1516eRegionHandleSet regionHandles;

    int size = Protocol.decodeVarInt(buffer);
    if (size == 0)
    {
      regionHandles = null;
    }
    else
    {
      regionHandles = new IEEE1516eRegionHandleSet();

      for (; size > 0; size--)
      {
        regionHandles.add(IEEE1516eRegionHandle.decode(buffer));
      }
    }

    return regionHandles;
  }
}
