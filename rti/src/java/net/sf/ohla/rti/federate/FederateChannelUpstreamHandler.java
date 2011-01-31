/*
 * Copyright (c) 2005-2010, Michael Newcomb
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

import net.sf.ohla.rti.messages.FederateMessage;
import net.sf.ohla.rti.messages.Message;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.MessageEvent;

public class FederateChannelUpstreamHandler
  implements ChannelUpstreamHandler
{
  public static final String NAME = "FederateChannelUpstreamHandler";

  private final Federate federate;

  public FederateChannelUpstreamHandler(Federate federate)
  {
    this.federate = federate;
  }

  public void handleUpstream(ChannelHandlerContext context, ChannelEvent event)
    throws Exception
  {
    if (event instanceof MessageEvent)
    {
      assert ((MessageEvent) event).getMessage() instanceof Message;

      Message message = (Message) ((MessageEvent) event).getMessage();

      if (message instanceof FederateMessage)
      {
        ((FederateMessage) message).execute(federate);
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
