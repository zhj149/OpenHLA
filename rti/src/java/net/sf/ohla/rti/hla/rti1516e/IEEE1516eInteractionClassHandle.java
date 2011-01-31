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

import net.sf.ohla.rti.Protocol;
import net.sf.ohla.rti.IntegerHandle;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.InteractionClassHandle;

public class IEEE1516eInteractionClassHandle
  extends IntegerHandle
  implements InteractionClassHandle
{
  public IEEE1516eInteractionClassHandle(int handle)
  {
    super(handle);
  }

  public IEEE1516eInteractionClassHandle(ChannelBuffer buffer)
  {
    super(buffer);
  }

  public static void encode(ChannelBuffer buffer, InteractionClassHandle interactionClassHandle)
  {
    ((IEEE1516eInteractionClassHandle) interactionClassHandle).encode(buffer);
  }

  public static IEEE1516eInteractionClassHandle decode(ChannelBuffer buffer)
  {
    return new IEEE1516eInteractionClassHandle(Protocol.decodeVarInt(buffer));
  }
}
