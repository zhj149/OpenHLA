/*
 * Copyright (c) 2006-2007, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.messages;

import java.util.concurrent.CountDownLatch;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelFuture;

import hla.rti1516e.exceptions.RTIinternalError;

public abstract class AbstractRequest<R>
  extends AbstractMessage
  implements Request<R>
{
  protected long id;

  protected R response;
  protected Throwable cause;

  protected final CountDownLatch responseLatch = new CountDownLatch(1);

  protected AbstractRequest(MessageType messageType)
  {
    super(messageType);

    buffer.writerIndex(buffer.writerIndex() + 8);
  }

  protected AbstractRequest(MessageType messageType, int capacity, boolean dynamic)
  {
    super(messageType, capacity, dynamic);

    buffer.writerIndex(buffer.writerIndex() + 8);
  }

  protected AbstractRequest(MessageType messageType, ChannelBuffer buffer)
  {
    super(messageType, buffer);

    buffer.writerIndex(buffer.writerIndex() + 8);
  }

  protected AbstractRequest(ChannelBuffer buffer)
  {
    super(buffer);

    id = buffer.readLong();
  }

  public long getId()
  {
    return id;
  }

  public void setId(long id)
  {
    buffer.setLong(6, id);
  }

  public R getResponse()
    throws RTIinternalError
  {
    boolean done = false;
    do
    {
      try
      {
        responseLatch.await();
        done = true;
      }
      catch (InterruptedException ie)
      {
      }
    } while (!done);

    if (response == null)
    {
      throw new RTIinternalError("connection closed", cause);
    }
    return response;
  }

  @SuppressWarnings("unchecked")
  public void setResponse(Object response)
  {
    this.response = (R) response;

    responseLatch.countDown();
  }

  public void operationComplete(ChannelFuture future)
    throws Exception
  {
    if (!future.isSuccess())
    {
      cause = future.getCause();

      responseLatch.countDown();
    }
  }
}
