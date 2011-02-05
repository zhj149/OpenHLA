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

import net.sf.ohla.rti.IntegerHandle;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.DimensionHandle;

public class IEEE1516eDimensionHandle
  extends IntegerHandle
  implements DimensionHandle
{
  private static final IEEE1516eDimensionHandle[] cache;
  static
  {
    // TODO: get cache size from properties

    cache = new IEEE1516eDimensionHandle[32];

    for (int i = 1; i < cache.length; i++)
    {
      cache[i] = new IEEE1516eDimensionHandle(i);
    }
  }

  public IEEE1516eDimensionHandle(int handle)
  {
    super(handle);
  }

  public static void encode(ChannelBuffer buffer, DimensionHandle attributeHandle)
  {
    ((IEEE1516eDimensionHandle) attributeHandle).encode(buffer);
  }

  public static IEEE1516eDimensionHandle decode(ChannelBuffer buffer)
  {
    int handle = decodeHandle(buffer);
    return handle < cache.length ? cache[handle] : new IEEE1516eDimensionHandle(handle);
  }

  public static IEEE1516eDimensionHandle decode(byte[] buffer, int offset)
  {
    int handle = decodeHandle(buffer, offset);
    return handle < cache.length ? cache[handle] : new IEEE1516eDimensionHandle(handle);
  }
}
