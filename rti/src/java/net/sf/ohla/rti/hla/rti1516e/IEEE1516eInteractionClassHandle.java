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

import hla.rti1516e.InteractionClassHandle;

public class IEEE1516eInteractionClassHandle
  extends IntegerHandle
  implements InteractionClassHandle
{
  private static final IEEE1516eInteractionClassHandle[] cache;
  static
  {
    // TODO: get cache size from properties

    cache = new IEEE1516eInteractionClassHandle[1024];

    for (int i = 1; i < cache.length; i++)
    {
      cache[i] = new IEEE1516eInteractionClassHandle(i);
    }
  }

  public IEEE1516eInteractionClassHandle(int handle)
  {
    super(handle);
  }

  public static void encode(ChannelBuffer buffer, InteractionClassHandle interactionClassHandle)
  {
    ((IEEE1516eInteractionClassHandle) interactionClassHandle).encode(buffer);
  }

  public static IEEE1516eInteractionClassHandle decode(DataInput in)
    throws IOException
  {
    int handle = decodeHandle(in);
    return handle < cache.length ? cache[handle] : new IEEE1516eInteractionClassHandle(handle);
  }

  public static IEEE1516eInteractionClassHandle decode(ChannelBuffer buffer)
  {
    int handle = decodeHandle(buffer);
    return handle < cache.length ? cache[handle] : new IEEE1516eInteractionClassHandle(handle);
  }

  public static IEEE1516eInteractionClassHandle decode(byte[] buffer, int offset)
  {
    int handle = decodeHandle(buffer, offset);
    return handle < cache.length ? cache[handle] : new IEEE1516eInteractionClassHandle(handle);
  }
}
