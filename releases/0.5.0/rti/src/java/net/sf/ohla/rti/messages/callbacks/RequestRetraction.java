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

package net.sf.ohla.rti.messages.callbacks;

import net.sf.ohla.rti.federate.Callback;
import net.sf.ohla.rti.federate.Federate;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eMessageRetractionHandle;
import net.sf.ohla.rti.messages.AbstractMessage;
import net.sf.ohla.rti.messages.FederateMessage;
import net.sf.ohla.rti.messages.MessageType;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.exceptions.FederateInternalError;

public class RequestRetraction
  extends AbstractMessage
  implements Callback, FederateMessage
{
  private final MessageRetractionHandle messageRetractionHandle;

  public RequestRetraction(MessageRetractionHandle messageRetractionHandle)
  {
    super(MessageType.REQUEST_RETRACTION);

    this.messageRetractionHandle = messageRetractionHandle;

    IEEE1516eMessageRetractionHandle.encode(buffer, messageRetractionHandle);

    encodingFinished();
  }

  public RequestRetraction(ChannelBuffer buffer)
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
    return MessageType.REQUEST_RETRACTION;
  }

  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    federateAmbassador.requestRetraction(messageRetractionHandle);
  }

  public void execute(Federate federate)
  {
    federate.callbackReceived(this);
  }
}