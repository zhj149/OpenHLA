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
import net.sf.ohla.rti.messages.FederateMessage;
import net.sf.ohla.rti.messages.MessageType;
import net.sf.ohla.rti.messages.ObjectInstanceAttributesMessage;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.exceptions.FederateInternalError;

public class AttributesOutOfScope
  extends ObjectInstanceAttributesMessage
  implements Callback, FederateMessage
{
  public AttributesOutOfScope(ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
  {
    super(MessageType.ATTRIBUTES_OUT_OF_SCOPE, objectInstanceHandle, attributeHandles);

    encodingFinished();
  }

  public AttributesOutOfScope(ChannelBuffer buffer)
  {
    super(buffer);
  }

  public MessageType getType()
  {
    return MessageType.ATTRIBUTES_OUT_OF_SCOPE;
  }

  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    federateAmbassador.attributesOutOfScope(objectInstanceHandle, attributeHandles);
  }

  public void execute(Federate federate)
  {
    federate.callbackReceived(this);
  }
}