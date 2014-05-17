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
import net.sf.ohla.rti.util.ObjectInstanceHandles;
import net.sf.ohla.rti.federate.Callback;
import net.sf.ohla.rti.federate.Federate;
import net.sf.ohla.rti.messages.AbstractMessage;
import net.sf.ohla.rti.messages.FederateMessage;
import net.sf.ohla.rti.messages.proto.FederateMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.exceptions.FederateInternalError;

public class AttributeOwnershipAcquisitionNotification
  extends
  AbstractMessage<FederateMessageProtos.AttributeOwnershipAcquisitionNotification, FederateMessageProtos.AttributeOwnershipAcquisitionNotification.Builder>
  implements Callback, FederateMessage
{
  private Federate federate;

  public AttributeOwnershipAcquisitionNotification(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
  {
    super(FederateMessageProtos.AttributeOwnershipAcquisitionNotification.newBuilder());

    builder.setObjectInstanceHandle(ObjectInstanceHandles.convert(objectInstanceHandle));
    builder.addAllAttributeHandles(AttributeHandles.convert(attributeHandles));

    if (tag != null)
    {
      builder.setTag(ByteString.copyFrom(tag));
    }
  }

  public AttributeOwnershipAcquisitionNotification(CodedInputStream in)
    throws IOException
  {
    super(FederateMessageProtos.AttributeOwnershipAcquisitionNotification.newBuilder(), in);
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.ATTRIBUTE_OWNERSHIP_ACQUISITION_NOTIFICATION;
  }

  @Override
  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    federate.attributeOwnershipAcquisitionNotification(ObjectInstanceHandles.convert(builder.getObjectInstanceHandle()),
                                                       AttributeHandles.convertAttributeHandles(
                                                         builder.getAttributeHandlesList()),
                                                       builder.hasTag() ? builder.getTag().toByteArray() : null);
  }

  @Override
  public void execute(Federate federate)
  {
    this.federate = federate;

    federate.getCallbackManager().add(this, false);
  }
}
