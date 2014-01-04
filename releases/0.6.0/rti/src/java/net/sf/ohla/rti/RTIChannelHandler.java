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

package net.sf.ohla.rti;

import java.util.concurrent.Executor;

import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.messages.CreateFederationExecution;
import net.sf.ohla.rti.messages.DestroyFederationExecution;
import net.sf.ohla.rti.messages.FederationExecutionMessage;
import net.sf.ohla.rti.messages.JoinFederationExecution;
import net.sf.ohla.rti.messages.ListFederationExecutions;
import net.sf.ohla.rti.messages.Message;
import net.sf.ohla.rti.messages.MessageChannelHandler;

import org.jboss.netty.channel.ChannelHandlerContext;

public class RTIChannelHandler
  extends MessageChannelHandler
{
  public static final String NAME = "RTIChannelHandler";

  private final RTI rti;

  private FederateProxy federateProxy;

  public RTIChannelHandler(Executor executor, RTI rti)
  {
    super(executor);

    this.rti = rti;
  }

  public void setFederateProxy(FederateProxy federateProxy)
  {
    this.federateProxy = federateProxy;
  }

  @Override
  protected void messageReceived(ChannelHandlerContext context, Message message)
  {
    if (message instanceof FederationExecutionMessage)
    {
      assert federateProxy != null;

      ((FederationExecutionMessage) message).execute(federateProxy.getFederationExecution(), federateProxy);
    }
    else
    {
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
          // TODO: error?
      }
    }
  }
}
