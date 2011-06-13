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
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandle;
import net.sf.ohla.rti.messages.MessageType;
import net.sf.ohla.rti.messages.ObjectInstanceAttributeMessage;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.exceptions.FederateInternalError;

public class InformAttributeOwnership
  extends ObjectInstanceAttributeMessage
  implements Callback
{
  private final FederateHandle federateHandle;

  public InformAttributeOwnership(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle, FederateHandle federateHandle)
  {
    super(MessageType.INFORM_ATTRIBUTE_OWNERSHIP, objectInstanceHandle, attributeHandle);

    this.federateHandle = federateHandle;

    IEEE1516eFederateHandle.encode(buffer, federateHandle);

    encodingFinished();
  }

  public InformAttributeOwnership(ChannelBuffer buffer)
  {
    super(buffer);

    federateHandle = IEEE1516eFederateHandle.decode(buffer);
  }

  public FederateHandle getFederateHandle()
  {
    return federateHandle;
  }

  public MessageType getType()
  {
    return MessageType.INFORM_ATTRIBUTE_OWNERSHIP;
  }

  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    federateAmbassador.informAttributeOwnership(objectInstanceHandle, attributeHandle, federateHandle);
  }
}
