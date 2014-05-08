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

import java.io.IOException;

import net.sf.ohla.rti.util.AttributeHandles;
import net.sf.ohla.rti.util.FederateHandles;
import net.sf.ohla.rti.util.ObjectInstanceHandles;
import net.sf.ohla.rti.federate.Callback;
import net.sf.ohla.rti.messages.AbstractMessage;
import net.sf.ohla.rti.messages.proto.FederateMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.exceptions.FederateInternalError;

public class InformAttributeOwnership
  extends
  AbstractMessage<FederateMessageProtos.InformAttributeOwnership, FederateMessageProtos.InformAttributeOwnership.Builder>
  implements Callback
{
  public InformAttributeOwnership(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle, FederateHandle federateHandle)
  {
    super(FederateMessageProtos.InformAttributeOwnership.newBuilder());

    builder.setObjectInstanceHandle(ObjectInstanceHandles.convert(objectInstanceHandle));
    builder.setAttributeHandle(AttributeHandles.convert(attributeHandle));
    builder.setFederateHandle(FederateHandles.convert(federateHandle));
  }

  public InformAttributeOwnership(CodedInputStream in)
    throws IOException
  {
    super(FederateMessageProtos.InformAttributeOwnership.newBuilder(), in);
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.INFORM_ATTRIBUTE_OWNERSHIP;
  }

  @Override
  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    federateAmbassador.informAttributeOwnership(
      ObjectInstanceHandles.convert(builder.getObjectInstanceHandle()),
      AttributeHandles.convert(builder.getAttributeHandle()),
      FederateHandles.convert(builder.getFederateHandle()));
  }
}
