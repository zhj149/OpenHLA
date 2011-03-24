/*
 * Copyright (c) 2005-2011, Michael Newcomb
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

package net.sf.ohla.rti.federate;

import net.sf.ohla.rti.messages.Message;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.MessageEvent;

public class CallbackManagerChannelUpstreamHandler
  implements ChannelUpstreamHandler
{
  public static final String NAME = "CallbackManagerChannelUpstreamHandler";

  private final CallbackManager callbackManager;

  public CallbackManagerChannelUpstreamHandler(CallbackManager callbackManager)
  {
    this.callbackManager = callbackManager;
  }

  public void handleUpstream(ChannelHandlerContext context, ChannelEvent event)
    throws Exception
  {
    if (event instanceof MessageEvent)
    {
      assert ((MessageEvent) event).getMessage() instanceof Message;

      Message message = (Message) ((MessageEvent) event).getMessage();

      if (message instanceof Callback)
      {
        callbackManager.add((Callback) message);
      }
      else
      {
        context.sendUpstream(event);
      }
    }
    else
    {
      context.sendUpstream(event);
    }
  }
}
