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

import java.io.IOException;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.MessageLite;
import hla.rti1516e.exceptions.RTIinternalError;

public abstract class AbstractRequest<ML extends MessageLite, B extends MessageLite.Builder, R>
  extends AbstractMessage<ML, B>
  implements Request<R>, ChannelFutureListener
{
  private final SettableFuture<R> future;

  protected AbstractRequest(B builder)
  {
    super(builder);

    future = SettableFuture.create();
  }

  protected AbstractRequest(B builder, CodedInputStream in)
    throws IOException
  {
    super(builder, in);

    future = null;
  }

  public R getResponse()
    throws RTIinternalError
  {
    return Futures.get(future, RTIinternalError.class);
  }

  @SuppressWarnings("unchecked")
  public void setResponse(Object response)
  {
    future.set((R) response);
  }

  public void operationComplete(ChannelFuture future)
    throws Exception
  {
    if (!future.isSuccess())
    {
      this.future.setException(future.getCause());
    }
  }
}
