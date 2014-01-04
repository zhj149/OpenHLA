/*
 * Copyright (c) 2005-2011, Michael Newcomb
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

import java.io.OutputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;

public class FederateStateOutputStream
  extends OutputStream
{
  private final int bufferSize;
  private final Channel channel;

  private ChannelBuffer buffer;

  public FederateStateOutputStream(int bufferSize, Channel channel)
  {
    this.bufferSize = bufferSize;
    this.channel = channel;

    createBuffer();
  }

  @Override
  public void write(int b)
  {
    if (buffer.writableBytes() < 1)
    {
      sendBufferAndReset();
    }

    buffer.writeByte(b);
  }

  @Override
  public void write(byte[] b, int offset, int length)
  {
    int writableBytes = buffer.writableBytes();
    if (writableBytes < length)
    {
      int bytesToWrite = writableBytes;
      do
      {
        buffer.writeBytes(b, offset, bytesToWrite);

        sendBufferAndReset();

        offset += bytesToWrite;
        length -= bytesToWrite;

        bytesToWrite = Math.min(length, buffer.writableBytes());
      } while (length > 0);
    }
    else
    {
      buffer.writeBytes(b, offset, length);
    }
  }

  @Override
  public void flush()
  {
    sendBufferAndReset();
  }

  @Override
  public void close()
  {
    sendBuffer(true);
  }

  private void sendBufferAndReset()
  {
    sendBuffer(false);

    createBuffer();
  }

  private void createBuffer()
  {
    buffer = ChannelBuffers.buffer(bufferSize);

    // skip the header
    //
    buffer.writerIndex(FederateStateFrame.HEADER_SIZE);
  }

  private void sendBuffer(boolean last)
  {
    channel.write(new FederateStateFrame(buffer, last));
  }
}
