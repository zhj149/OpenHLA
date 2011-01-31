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

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public abstract class AbstractMessage
  implements Message
{
  /**
   * The {@code ChannelBuffer} containing the data for this message.
   */
  protected final ChannelBuffer buffer;

  protected AbstractMessage(MessageType messageType)
  {
    this(messageType, ChannelBuffers.dynamicBuffer());
  }

  protected AbstractMessage(MessageType messageType, int capacity, boolean dynamic)
  {
    this(messageType, dynamic ? ChannelBuffers.dynamicBuffer(capacity) : ChannelBuffers.buffer(capacity));
  }

  protected AbstractMessage(MessageType messageType, ChannelBuffer buffer)
  {
    this(buffer);

    // save room for the length
    //
    buffer.writerIndex(4);

    buffer.writeShort(messageType.ordinal());
  }

  protected AbstractMessage(ChannelBuffer buffer)
  {
    this.buffer = buffer;
  }

  /**
   * Called to indicate that a subclass has fully encoded it's data into {@link AbstractMessage#buffer}. This method
   * then writes the size of the message in the first 4 bytes as the header.
   */
  protected void encodingFinished()
  {
    buffer.setInt(0, buffer.readableBytes() - 4);
  }

  public ChannelBuffer getBuffer()
  {
    return buffer;
  }
}
