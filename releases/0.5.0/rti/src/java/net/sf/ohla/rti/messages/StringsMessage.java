/*
 * Copyright (c) 2005-2010, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.messages;

import java.util.Set;

import net.sf.ohla.rti.Protocol;

import org.jboss.netty.buffer.ChannelBuffer;

public abstract class StringsMessage
  extends AbstractMessage
{
  protected final Set<String> strings;

  protected StringsMessage(MessageType messageType, Set<String> strings)
  {
    super(messageType);

    this.strings = strings;

    Protocol.encodeStrings(buffer, strings);
  }

  protected StringsMessage(MessageType messageType, int capacity, boolean dynamic, Set<String> strings)
  {
    super(messageType, capacity, dynamic);

    this.strings = strings;

    Protocol.encodeStrings(buffer, strings);
  }

  protected StringsMessage(MessageType messageType, ChannelBuffer buffer, Set<String> strings)
  {
    super(messageType, buffer);

    this.strings = strings;

    Protocol.encodeStrings(buffer, strings);
  }

  protected StringsMessage(ChannelBuffer buffer)
  {
    super(buffer);

    strings = Protocol.decodeStringSet(buffer);
  }
}
