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

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.MessageLite;

public class MessageEncoder
  implements ChannelDownstreamHandler
{
  public static final String NAME = MessageEncoder.class.getSimpleName();

  public void handleDownstream(ChannelHandlerContext context, ChannelEvent event)
    throws IOException
  {
    if (event instanceof MessageEvent)
    {
      Message message = (Message) ((MessageEvent) event).getMessage();

      MessageLite messageLite = message.getMessageLite();

      // compute the message length
      //
      int length = messageLite.getSerializedSize();
      length += CodedOutputStream.computeRawVarint32Size(message.getMessageType().ordinal());

      // add 4 for the length field
      //
      ChannelBuffer channelBuffer = ChannelBuffers.buffer(length + 4);

      // write the length as a normal 4-byte integer
      //
      channelBuffer.writeInt(length);

      CodedOutputStream out = CodedOutputStream.newInstance(
        channelBuffer.array(), channelBuffer.arrayOffset() + channelBuffer.writerIndex(), length);

      out.writeRawVarint32(message.getMessageType().ordinal());

      message.getMessageLite().writeTo(out);

      // move the writer index because the CodedOutputStream wrote directly to the ChannelBuffer's array
      //
      channelBuffer.writerIndex(channelBuffer.capacity());

      Channels.write(context, event.getFuture(), channelBuffer);
    }
    else
    {
      context.sendDownstream(event);
    }
  }
}
