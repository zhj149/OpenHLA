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

package net.sf.ohla.rti.messages;

import net.sf.ohla.rti.Protocol;

import org.jboss.netty.buffer.ChannelBuffer;

public abstract class EnumMessage<E extends Enum>
  extends AbstractMessage
{
  protected final E e;

  protected EnumMessage(MessageType messageType, E e)
  {
    super(messageType);

    this.e = e;

    Protocol.encodeEnum(buffer, e);
  }

  protected EnumMessage(MessageType messageType, int capacity, boolean dynamic, E e)
  {
    super(messageType, capacity, dynamic);

    this.e = e;

    Protocol.encodeEnum(buffer, e);
  }

  protected EnumMessage(MessageType messageType, ChannelBuffer buffer, E e)
  {
    super(messageType, buffer);

    this.e = e;

    Protocol.encodeEnum(buffer, e);
  }

  protected EnumMessage(ChannelBuffer buffer, E[] values)
  {
    super(buffer);

    e = Protocol.decodeEnum(buffer, values);
  }
}
