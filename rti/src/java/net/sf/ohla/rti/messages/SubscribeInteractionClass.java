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

import net.sf.ohla.rti.Protocol;
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.InteractionClassHandle;

public class SubscribeInteractionClass
  extends InteractionClassMessage
  implements FederationExecutionMessage
{
  private final boolean passive;

  public SubscribeInteractionClass(InteractionClassHandle interactionClassHandle, boolean passive)
  {
    this(MessageType.SUBSCRIBE_INTERACTION_CLASS, interactionClassHandle, passive, true);
  }

  public SubscribeInteractionClass(ChannelBuffer buffer)
  {
    super(buffer);

    passive = Protocol.decodeBoolean(buffer);
  }

  protected SubscribeInteractionClass(
    MessageType messageType, InteractionClassHandle interactionClassHandle, boolean passive, boolean encodingFinished)
  {
    super(messageType, interactionClassHandle);

    this.passive = passive;

    Protocol.encodeBoolean(buffer, passive);

    if (encodingFinished)
    {
      encodingFinished();
    }
  }

  public boolean isPassive()
  {
    return passive;
  }

  public MessageType getType()
  {
    return MessageType.SUBSCRIBE_INTERACTION_CLASS;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.subscribeInteractionClass(federateProxy, this);
  }
}
