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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.MessageEvent;

public class RequestResponseHandler
  implements ChannelUpstreamHandler, ChannelDownstreamHandler
{
  public static final String NAME = "RequestResponseHandler";

  private final AtomicLong requestCount = new AtomicLong();

  private final ConcurrentMap<Long, Request> requests = new ConcurrentHashMap<Long, Request>();

  public void handleUpstream(ChannelHandlerContext context, ChannelEvent event)
    throws Exception
  {
    if (event instanceof MessageEvent)
    {
      Object message = ((MessageEvent) event).getMessage();
      if (message instanceof Response)
      {
        Request request = requests.remove(((Response) message).getRequestId());

        assert request != null;

        request.setResponse(message);
      }
      else
      {
        context.sendUpstream(event);
      }
    }
  }

  public void handleDownstream(ChannelHandlerContext context, ChannelEvent event)
    throws Exception
  {
    if (event instanceof MessageEvent)
    {
      Object message = ((MessageEvent) event).getMessage();
      if (message instanceof Request)
      {
        Request request = (Request) message;

        long id = requestCount.incrementAndGet();
        request.setId(id);

        requests.put(id, request);
      }
    }

    context.sendDownstream(event);
  }
}
