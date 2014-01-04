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
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;

import hla.rti1516e.LogicalTimeFactory;

public class MessageDecoder
  implements ChannelUpstreamHandler
{
  public static final String NAME = "MessageDecoder";

  private final ChannelBuffer lengthBuffer = ChannelBuffers.buffer(4);

  private LogicalTimeFactory logicalTimeFactory;

  private ChannelBuffer message;

  public void setLogicalTimeFactory(LogicalTimeFactory logicalTimeFactory)
  {
    this.logicalTimeFactory = logicalTimeFactory;
  }

  public void handleUpstream(ChannelHandlerContext context, ChannelEvent event)
    throws Exception
  {
    if (event instanceof MessageEvent)
    {
      assert ((MessageEvent) event).getMessage() instanceof ChannelBuffer;

      ChannelBuffer buffer = (ChannelBuffer) ((MessageEvent) event).getMessage();
      while (buffer.readable())
      {
        if (message == null)
        {
          decodeLength(context, buffer);
        }
        else
        {
          decodeMessage(context, buffer);
        }
      }
    }
    else
    {
      context.sendUpstream(event);
    }
  }

  private void decodeLength(ChannelHandlerContext context, ChannelBuffer buffer)
  {
    do
    {
      byte b = buffer.readByte();
      lengthBuffer.writeByte(b);

      if (lengthBuffer.readableBytes() == 4)
      {
        int length = lengthBuffer.readInt();
        lengthBuffer.clear();

        message = ChannelBuffers.buffer(length);

        if (buffer.readable())
        {
          decodeMessage(context, buffer);
        }
      }
    } while (message == null && buffer.readable());
  }

  private void decodeMessage(ChannelHandlerContext context, ChannelBuffer buffer)
  {
    int available = buffer.readableBytes();
    if (available < message.writableBytes())
    {
      buffer.readBytes(message, available);
    }
    else
    {
      buffer.readBytes(message);

      try
      {
        Channels.fireMessageReceived(context, MessageFactory.createMessage(message, logicalTimeFactory));
      }
      finally
      {
        message = null;
      }
    }
  }
}
