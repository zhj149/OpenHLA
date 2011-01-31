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

package net.sf.ohla.rti;

import net.sf.ohla.rti.messages.CreateFederationExecution;
import net.sf.ohla.rti.messages.DestroyFederationExecution;
import net.sf.ohla.rti.messages.JoinFederationExecution;
import net.sf.ohla.rti.messages.ListFederationExecutions;
import net.sf.ohla.rti.messages.Message;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.MessageEvent;

@ChannelHandler.Sharable
public class RTIChannelUpstreamHandler
  implements ChannelUpstreamHandler
{
  public static final String NAME = "RTIChannelUpstreamHandler";

  private final RTI rti;

  public RTIChannelUpstreamHandler(RTI rti)
  {
    this.rti = rti;
  }

  public void handleUpstream(ChannelHandlerContext context, ChannelEvent event)
    throws Exception
  {
    if (event instanceof MessageEvent)
    {
      assert ((MessageEvent) event).getMessage() instanceof Message;

      Message message = (Message) ((MessageEvent) event).getMessage();

      switch (message.getType())
      {
        case CREATE_FEDERATION_EXECUTION:
          rti.createFederationExecution(context, (CreateFederationExecution) message);
          break;
        case DESTROY_FEDERATION_EXECUTION:
          rti.destroyFederationExecution(context, (DestroyFederationExecution) message);
          break;
        case JOIN_FEDERATION_EXECUTION:
          rti.joinFederationExecution(context, (JoinFederationExecution) message);
          break;
        case LIST_FEDERATION_EXECUTIONS:
          rti.listFederationExecutions(context, (ListFederationExecutions) message);
          break;
        default:
          context.sendUpstream(event);
      }
    }
    else
    {
      context.sendUpstream(event);
    }
  }
}
