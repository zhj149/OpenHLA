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
import java.io.IOException;

import net.sf.ohla.rti.IntegerHandle;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.TransportationTypeHandle;

public class IEEE1516eTransportationTypeHandle
  extends IntegerHandle
  implements TransportationTypeHandle
{
  public static final IEEE1516eTransportationTypeHandle HLA_RELIABLE = new IEEE1516eTransportationTypeHandle(0);
  public static final IEEE1516eTransportationTypeHandle HLA_BEST_EFFORT = new IEEE1516eTransportationTypeHandle(1);

  private static final IEEE1516eTransportationTypeHandle[] cache;
  static
  {
    // TODO: get cache size from properties

    cache = new IEEE1516eTransportationTypeHandle[] { HLA_RELIABLE, HLA_BEST_EFFORT };

    for (int i = 1; i < cache.length; i++)
    {
      cache[i] = new IEEE1516eTransportationTypeHandle(i);
    }
  }

  public IEEE1516eTransportationTypeHandle(int handle)
  {
    super(handle);
  }

  public static void encode(ChannelBuffer buffer, TransportationTypeHandle transportationTypeHandle)
  {
    ((IEEE1516eTransportationTypeHandle) transportationTypeHandle).encode(buffer);
  }

  public static TransportationTypeHandle decode(DataInput in)
    throws IOException
  {
    int handle = decodeHandle(in);
    return handle < cache.length ? cache[handle] : new IEEE1516eTransportationTypeHandle(handle);
  }

  public static TransportationTypeHandle decode(ChannelBuffer buffer)
  {
    int handle = decodeHandle(buffer);
    return handle < cache.length ? cache[handle] : new IEEE1516eTransportationTypeHandle(handle);
  }

  public static IEEE1516eTransportationTypeHandle decode(byte[] buffer, int offset)
  {
    int handle = decodeHandle(buffer, offset);
    return handle < cache.length ? cache[handle] : new IEEE1516eTransportationTypeHandle(handle);
  }
}
