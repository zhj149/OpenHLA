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

import net.sf.ohla.rti.Protocol;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.FederateHandle;
import hla.rti1516e.RegionHandle;

public class IEEE1516eRegionHandle
  implements RegionHandle
{
  private final FederateHandle federateHandle;

  private final int regionHandle;

  public IEEE1516eRegionHandle(FederateHandle federateHandle, int regionHandle)
  {
    this.federateHandle = federateHandle;
    this.regionHandle = regionHandle;
  }

  public IEEE1516eRegionHandle(DataInput in)
    throws IOException
  {
    this(IEEE1516eFederateHandle.decode(in), in.readInt());
  }

  public int encodedLength()
  {
    return federateHandle.encodedLength() + Protocol.encodedVarIntSize(regionHandle);
  }

  public void encode(byte[] buffer, int offset)
  {
    Protocol.encodeVarInt(buffer, offset, regionHandle);
    federateHandle.encode(buffer, offset + Protocol.encodedVarIntSize(regionHandle));
  }

  public void writeTo(DataOutput out)
    throws IOException
  {
    ((IEEE1516eFederateHandle) federateHandle).writeTo(out);

    out.writeInt(regionHandle);
  }

  @Override
  public int hashCode()
  {
    return regionHandle & (federateHandle.hashCode() << 24);
  }

  @Override
  public boolean equals(Object rhs)
  {
    return this == rhs || (rhs instanceof IEEE1516eRegionHandle && equals((IEEE1516eRegionHandle) rhs));
  }

  @Override
  public String toString()
  {
    return new StringBuilder(federateHandle.toString()).append(".").append(regionHandle).toString();
  }

  private boolean equals(IEEE1516eRegionHandle rhs)
  {
    return regionHandle == rhs.regionHandle && federateHandle.equals(rhs.federateHandle);
  }

  public static void encode(ChannelBuffer buffer, RegionHandle regionHandle)
  {
    if (regionHandle == null)
    {
      // encode 0 if the handle was null
      //
      Protocol.encodeVarInt(buffer, 0);
    }
    else
    {
      // encode the handle first, saves space when the RegionHandle is null
      //
      Protocol.encodeVarInt(buffer, ((IEEE1516eRegionHandle) regionHandle).regionHandle);

      IEEE1516eFederateHandle.encode(buffer, ((IEEE1516eRegionHandle) regionHandle).federateHandle);
    }
  }

  public static IEEE1516eRegionHandle decode(DataInput in)
    throws IOException
  {
    // decode the handle first
    //
    int regionHandle = in.readInt();

    // a 0 indicates the RegionHandle was null
    //
    return regionHandle == 0 ? null : new IEEE1516eRegionHandle(IEEE1516eFederateHandle.decode(in), regionHandle);
  }

  public static IEEE1516eRegionHandle decode(ChannelBuffer buffer)
  {
    // decode the handle first
    //
    int regionHandle = Protocol.decodeVarInt(buffer);

    // a 0 indicates the RegionHandle was null
    //
    return regionHandle == 0 ? null : new IEEE1516eRegionHandle(IEEE1516eFederateHandle.decode(buffer), regionHandle);
  }
}
