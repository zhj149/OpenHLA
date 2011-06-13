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

package net.sf.ohla.rti.federate;

import java.util.concurrent.Executor;

import net.sf.ohla.rti.messages.FederateMessage;
import net.sf.ohla.rti.messages.Message;
import net.sf.ohla.rti.messages.MessageChannelHandler;

import org.jboss.netty.channel.ChannelHandlerContext;

public class FederateChannelHandler
  extends MessageChannelHandler
{
  public static final String NAME = "FederateChannelHandler";

  private final CallbackManager callbackManager;

  private Federate federate;

  public FederateChannelHandler(Executor executor, CallbackManager callbackManager)
  {
    super(executor);

    this.callbackManager = callbackManager;
  }

  public void setFederate(Federate federate)
  {
    this.federate = federate;
  }

  @Override
  protected void messageReceived(ChannelHandlerContext context, Message message)
  {
    if (message instanceof FederateMessage)
    {
      assert federate != null;

      ((FederateMessage) message).execute(federate);
    }
    else
    {
      assert message instanceof Callback;

      callbackManager.add((Callback) message);
    }
  }
}
