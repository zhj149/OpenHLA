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

package net.sf.ohla.rti;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;

public class IntegerHandle
  implements Comparable<IntegerHandle>
{
  protected final int handle;

  public IntegerHandle(IntegerHandle handle)
  {
    this.handle = handle.handle;
  }

  public IntegerHandle(int handle)
  {
    this.handle = handle;
  }

  public IntegerHandle(ChannelBuffer buffer)
  {
    handle = decodeHandle(buffer);
  }

  public IntegerHandle(byte[] buffer, int offset)
  {
    handle = decodeHandle(buffer, offset);
  }

  public IntegerHandle(DataInput in)
    throws IOException
  {
    this(in.readInt());
  }

  public int getHandle()
  {
    return handle;
  }

  public int encodedLength()
  {
    return Protocol.encodedVarIntSize(handle);
  }

  public void encode(byte[] buffer, int offset)
  {
    Protocol.encodeVarInt(buffer, offset, handle);
  }

  public void encode(ChannelBuffer buffer)
  {
    Protocol.encodeVarInt(buffer, handle);
  }

  public void writeTo(DataOutput out)
    throws IOException
  {
    out.writeInt(handle);
  }

  public int compareTo(IntegerHandle rhs)
  {
    return handle - rhs.handle;
  }

  @Override
  public boolean equals(Object rhs)
  {
    return this == rhs || (rhs instanceof IntegerHandle && handle == ((IntegerHandle) rhs).handle);
  }

  @Override
  public int hashCode()
  {
    return handle;
  }

  @Override
  public String toString()
  {
    return Integer.toString(handle);
  }

  public static int decodeHandle(DataInput in)
    throws IOException
  {
    return in.readInt();
  }

  public static int decodeHandle(ChannelBuffer buffer)
  {
    return Protocol.decodeVarInt(buffer);
  }

  public static int decodeHandle(byte[] buffer, int offset)
  {
    return Protocol.decodeVarInt(buffer, offset);
  }
}
