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

import org.jboss.netty.buffer.ChannelBuffer;

public class DestroyFederationExecution
  extends AbstractRequest<DestroyFederationExecutionResponse>
{
  private final String federationExecutionName;

  public DestroyFederationExecution(String federationExecutionName)
  {
    super(MessageType.DESTROY_FEDERATION_EXECUTION);

    this.federationExecutionName = federationExecutionName;

    Protocol.encodeString(buffer, federationExecutionName);

    encodingFinished();
  }

  public DestroyFederationExecution(ChannelBuffer buffer)
  {
    super(buffer);

    federationExecutionName = Protocol.decodeString(buffer);
  }

  public String getFederationExecutionName()
  {
    return federationExecutionName;
  }

  public MessageType getType()
  {
    return MessageType.DESTROY_FEDERATION_EXECUTION;
  }
}