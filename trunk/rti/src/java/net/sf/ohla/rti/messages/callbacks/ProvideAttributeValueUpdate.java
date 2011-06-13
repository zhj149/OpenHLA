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

import net.sf.ohla.rti.Protocol;
import net.sf.ohla.rti.federate.Callback;
import net.sf.ohla.rti.messages.MessageType;
import net.sf.ohla.rti.messages.ObjectInstanceAttributesMessage;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.exceptions.FederateInternalError;

public class ProvideAttributeValueUpdate
  extends ObjectInstanceAttributesMessage
  implements Callback
{
  private final byte[] tag;

  public ProvideAttributeValueUpdate(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
  {
    super(MessageType.PROVIDE_ATTRIBUTE_VALUE_UPDATE, objectInstanceHandle, attributeHandles);

    this.tag = tag;

    Protocol.encodeBytes(buffer, tag);

    encodingFinished();
  }

  public ProvideAttributeValueUpdate(ChannelBuffer buffer)
  {
    super(buffer);

    tag = Protocol.decodeBytes(buffer);
  }

  public MessageType getType()
  {
    return MessageType.PROVIDE_ATTRIBUTE_VALUE_UPDATE;
  }

  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    federateAmbassador.provideAttributeValueUpdate(objectInstanceHandle, attributeHandles, tag);
  }
}
