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

import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eMessageRetractionHandle;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.MessageRetractionHandle;

public class Retract
  extends AbstractRequest<RetractResponse>
  implements FederationExecutionMessage
{
  private final MessageRetractionHandle messageRetractionHandle;

  public Retract(MessageRetractionHandle messageRetractionHandle)
  {
    super(MessageType.RETRACT);

    this.messageRetractionHandle = messageRetractionHandle;

    IEEE1516eMessageRetractionHandle.encode(buffer, messageRetractionHandle);

    encodingFinished();
  }

  public Retract(ChannelBuffer buffer)
  {
    super(buffer);

    messageRetractionHandle = IEEE1516eMessageRetractionHandle.decode(buffer);
  }

  public MessageRetractionHandle getMessageRetractionHandle()
  {
    return messageRetractionHandle;
  }

  public MessageType getType()
  {
    return MessageType.RETRACT;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.retract(federateProxy, this);
  }
}
