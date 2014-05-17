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
import net.sf.ohla.rti.util.ObjectInstanceHandles;
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.messages.proto.FederationExecutionMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.ObjectInstanceHandle;

public class UnconditionalAttributeOwnershipDivestiture
  extends AbstractMessage<FederationExecutionMessageProtos.UnconditionalAttributeOwnershipDivestiture, FederationExecutionMessageProtos.UnconditionalAttributeOwnershipDivestiture.Builder>
  implements FederationExecutionMessage
{
  public UnconditionalAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
  {
    super(FederationExecutionMessageProtos.UnconditionalAttributeOwnershipDivestiture.newBuilder());

    builder.setObjectInstanceHandle(ObjectInstanceHandles.convert(objectInstanceHandle));
    builder.addAllAttributeHandles(AttributeHandles.convert(attributeHandles));
  }

  public UnconditionalAttributeOwnershipDivestiture(CodedInputStream in)
    throws IOException
  {
    super(FederationExecutionMessageProtos.UnconditionalAttributeOwnershipDivestiture.newBuilder(), in);
  }

  public ObjectInstanceHandle getObjectInstanceHandle()
  {
    return ObjectInstanceHandles.convert(builder.getObjectInstanceHandle());
  }

  public AttributeHandleSet getAttributeHandles()
  {
    return AttributeHandles.convertAttributeHandles(builder.getAttributeHandlesList());
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.UNCONDITIONAL_ATTRIBUTE_OWNERSHIP_DIVESTITURE;
  }

  @Override
  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.unconditionalAttributeOwnershipDivestiture(federateProxy, this);
  }
}
