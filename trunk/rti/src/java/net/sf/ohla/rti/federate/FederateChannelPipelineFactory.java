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

import java.util.concurrent.Executor;

import net.sf.ohla.rti.messages.MessageDecoder;
import net.sf.ohla.rti.messages.MessageEncoder;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

public class FederateChannelPipelineFactory
  implements ChannelPipelineFactory
{
  private final Executor executor;
  private final CallbackManager callbackManager;
  private final FederateChannelHandlerFactory federateChannelHandlerFactory;

  public FederateChannelPipelineFactory(Executor executor, CallbackManager callbackManager,
                                        FederateChannelHandlerFactory federateChannelHandlerFactory)
  {
    this.executor = executor;
    this.callbackManager = callbackManager;
    this.federateChannelHandlerFactory = federateChannelHandlerFactory;
  }

  public ChannelPipeline getPipeline()
    throws Exception
  {
    ChannelPipeline pipeline = Channels.pipeline();

    pipeline.addLast(MessageEncoder.NAME, new MessageEncoder());
    pipeline.addLast(MessageDecoder.NAME, new MessageDecoder());
    pipeline.addLast(FederateChannelHandler.NAME, federateChannelHandlerFactory.create(executor, callbackManager));

    return pipeline;
  }
}
