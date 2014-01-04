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

public abstract class StringMessage
  extends AbstractMessage
{
  protected final String s;

  protected StringMessage(MessageType messageType, String s)
  {
    super(messageType);

    this.s = s;

    Protocol.encodeString(buffer, s);
  }

  protected StringMessage(MessageType messageType, int capacity, boolean dynamic, String s)
  {
    super(messageType, capacity, dynamic);

    this.s = s;

    Protocol.encodeString(buffer, s);
  }

  protected StringMessage(MessageType messageType, ChannelBuffer buffer, String s)
  {
    super(messageType, buffer);

    this.s = s;

    Protocol.encodeString(buffer, s);
  }

  protected StringMessage(ChannelBuffer buffer)
  {
    super(buffer);

    s = Protocol.decodeString(buffer);
  }
}
