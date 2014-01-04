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
   * The {@code ChannelBuffer} containing the serialized data for this message.
   */
  protected final ChannelBuffer buffer;

  /**
   * A utility constructor that creates an {@code AbstractMessage} of the specified {@link MessageType} and with a
   * dynamic {@code ChannelBuffer}.
   * <p/>
   * This constructor is identical to {@code AbstractMessage(MessageType, ChannelBuffers.dynamicBuffer())}.
   * <p/>
   * <b>WARNING</b><br/>
   * Messages created this way have will have the header (length of the body) written in the first {@code 4} bytes of
   * {@link AbstractMessage#buffer}.
   *
   * @param messageType the {@link MessageType} of this {@code AbstractMessage}
   */
  protected AbstractMessage(MessageType messageType)
  {
    this(messageType, ChannelBuffers.dynamicBuffer());
  }

  /**
   * A utility constructor that creates an {@code AbstractMessage} of the specified {@link MessageType} and with a
   * {@code ChannelBuffer} of the specified {@code capacity}. The {@code ChannelBuffer} will by dynamic if
   * {@code dynamic} is {@code true}); otherwise it will be static.
   * <p/>
   * This constructor is identical to {@code AbstractMessage(MessageType, dynamic ? ChannelBuffers.dynamicBuffer(capacity) : ChannelBuffers.buffer(capacity))}.
   * <p/>
   * <b>WARNING</b><br/>
   * Messages created this way have will have the header (length of the body) written in the first {@code 4} bytes of
   * {@link AbstractMessage#buffer}.
   *
   * @param messageType the {@link MessageType} of this {@code AbstractMessage}
   * @param capacity the (initial if {@code dynamic} is {@code true}) capacity of {@link AbstractMessage#buffer}
   * @param dynamic {@code true} if the {@link AbstractMessage#buffer} is to be dynamic; otherwise it will be static
   */
  protected AbstractMessage(MessageType messageType, int capacity, boolean dynamic)
  {
    this(messageType, dynamic ? ChannelBuffers.dynamicBuffer(capacity) : ChannelBuffers.buffer(capacity));
  }

  /**
   * This is the primary constructor used by subclasses when they are created for sending ({@code new Message()}). It
   * saves room for the header by moving the {@code ChannelBuffer#writerIndex(int)} of the specified
   * {@code ChannelBuffer} to {@code 4}. It then writes a {@code short} the {@link MessageType#ordinal()} in next
   * {@code 2} bytes.
   * <p/>
   * <b>WARNING</b><br/>
   * Messages created this way have will have the header (length of the body) written in the first 4 bytes of
   * {@link AbstractMessage#buffer}.
   *
   * @param messageType the {@link MessageType} of this {@code AbstractMessage}
   * @param buffer the {@code ChannelBuffer} that will hold the serialized data for this {@code AbstractMessage}
   */
  protected AbstractMessage(MessageType messageType, ChannelBuffer buffer)
  {
    this.buffer = buffer;

    // save room for the length
    //
    buffer.writerIndex(4);

    buffer.writeShort(messageType.ordinal());
  }

  /**
   * This is the primary constructor used by subclasses when they are created for reading
   * ({@code MessageFactory.createMessage()}). It simply assigns the specified {@code ChannelBuffer} to
   * {@link AbstractMessage#buffer}.
   * <p/>
   * <b>WARNING</b><br/>
   * Messages created this way have will have no header (length of the body) written in the first {@code 4} bytes of
   * {@link AbstractMessage#buffer}. The first {@code 2} bytes will be the {@link MessageType#ordinal()}.
   *
   * @param buffer the {@code ChannelBuffer} that will hold the serialized data for this {@code AbstractMessage}
   */
  protected AbstractMessage(ChannelBuffer buffer)
  {
    this.buffer = buffer;
  }

  /**
   * Returns the {@code ChannelBuffer} containing the serialized data of this {@code AbstractMessage}. This method
   * always resets the {@code ChannelBuffer.readerIndex()} to {@code 0} so the returned {@code ChannelBuffer} is ready
   * for reading.
   * <p/>
   * <b>WARNING:</b><br/>
   * When an {@code AbstractMessage} is created explicitly ({@code new AbstractMessage()}, the first {@code 4} bytes of
   * the buffer acts as a header and contains the length of the message body.<br/>
   * When an {@code AbstractMessage} is created from the {@link MessageFactory} the header is not there, just the
   * message body.
   *
   * @return the {@link ChannelBuffer} containing the serialized data
   */
  public ChannelBuffer getBuffer()
  {
    buffer.readerIndex(0);

    return buffer;
  }

  /**
   * Called to indicate that a subclass has fully encoded it's data into {@link AbstractMessage#buffer}. This method
   * then writes the size of the message body in the first {@code 4} bytes as the header.
   */
  protected void encodingFinished()
  {
    buffer.setInt(0, buffer.readableBytes() - 4);
  }
}
