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

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class FederateStateInputStream
  extends InputStream
{
  private final BlockingQueue<ChannelBuffer> buffers = new LinkedBlockingQueue<ChannelBuffer>();

  private ChannelBuffer buffer = ChannelBuffers.EMPTY_BUFFER;

  public void addFrame(ChannelBuffer buffer)
  {
    assert buffer.readable();

    buffers.offer(buffer);
  }

  public void done()
  {
    buffers.offer(ChannelBuffers.EMPTY_BUFFER);
  }

  @Override
  public int read()
    throws IOException
  {
    return buffer.readable() || nextBuffer() ? buffer.readByte() : -1;
  }

  @Override
  public int read(byte[] b, int offset, int length)
    throws IOException
  {
    return buffer.readable() || nextBuffer() ? readAvailable(b, offset, length) : -1;
  }

  @Override
  public long skip(long n)
    throws IOException
  {
    return super.skip(n);
  }

  @Override
  public int available()
    throws IOException
  {
    return super.available();
  }

  private int readAvailable(byte[] b, int offset, int length)
  {
    int read;

    int readableBytes = buffer.readableBytes();
    if (readableBytes < length)
    {
      read = readableBytes;

      buffer.readBytes(b, offset, read);

      if (nextBufferIfAvailable())
      {
        read += readAvailable(b, offset + readableBytes, length - readableBytes);
      }
    }
    else
    {
      read = length;

      buffer.readBytes(b, offset, read);
    }

    return read;
  }

  private boolean nextBuffer()
    throws InterruptedIOException
  {
    try
    {
      buffer = buffers.take();
    }
    catch (InterruptedException ie)
    {
      throw new InterruptedIOException();
    }

    boolean available;
    if (buffer == ChannelBuffers.EMPTY_BUFFER)
    {
      // ChannelBuffers.EMPTY_BUFFER marks the fact that the end of the stream has been reached

      available = false;

      // put it back on to ensure it triggers the end of stream
      //
      buffers.offer(ChannelBuffers.EMPTY_BUFFER);
    }
    else
    {
      available = true;
    }
    return available;
  }

  private boolean nextBufferIfAvailable()
  {
    buffer = buffers.poll();

    boolean available;
    if (buffer == null)
    {
      // no more buffers are available right now

      available = false;

      // don't let buffer be null
      //
      buffer = ChannelBuffers.EMPTY_BUFFER;
    }
    else if (buffer == ChannelBuffers.EMPTY_BUFFER)
    {
      // ChannelBuffers.EMPTY_BUFFER marks the fact that the end of the stream has been reached

      available = false;

      // put it back on to ensure it triggers the end of stream
      //
      buffers.offer(ChannelBuffers.EMPTY_BUFFER);
    }
    else
    {
      available = true;
    }
    return available;
  }
}
