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

import java.io.IOException;

import net.sf.ohla.rti.util.AttributeHandles;
import net.sf.ohla.rti.util.ObjectClassHandles;
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.messages.proto.FederationExecutionMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.ObjectClassHandle;

public class UnsubscribeObjectClassAttributes
  extends AbstractMessage<FederationExecutionMessageProtos.UnsubscribeObjectClassAttributes, FederationExecutionMessageProtos.UnsubscribeObjectClassAttributes.Builder>
  implements FederationExecutionMessage
{
  public UnsubscribeObjectClassAttributes(ObjectClassHandle objectClassHandle)
  {
    super(FederationExecutionMessageProtos.UnsubscribeObjectClassAttributes.newBuilder());

    builder.setObjectClassHandle(ObjectClassHandles.convert(objectClassHandle));
  }

  public UnsubscribeObjectClassAttributes(ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
  {
    this(objectClassHandle);

    builder.addAllAttributeHandles(AttributeHandles.convert(attributeHandles));
  }

  public UnsubscribeObjectClassAttributes(CodedInputStream in)
    throws IOException
  {
    super(FederationExecutionMessageProtos.UnsubscribeObjectClassAttributes.newBuilder(), in);
  }

  public ObjectClassHandle getObjectClassHandle()
  {
    return ObjectClassHandles.convert(builder.getObjectClassHandle());
  }

  public AttributeHandleSet getAttributeHandles()
  {
    return AttributeHandles.convertAttributeHandles(builder.getAttributeHandlesList());
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.UNSUBSCRIBE_OBJECT_CLASS_ATTRIBUTES;
  }

  @Override
  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.unsubscribeObjectClassAttributes(federateProxy, this);
  }
}
