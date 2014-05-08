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
import net.sf.ohla.rti.util.ObjectInstanceHandles;
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.messages.proto.FederationExecutionMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeSetRegionSetPairList;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;

public class RegisterObjectInstance
  extends AbstractMessage<FederationExecutionMessageProtos.RegisterObjectInstance, FederationExecutionMessageProtos.RegisterObjectInstance.Builder>
  implements FederationExecutionMessage
{
  public RegisterObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle, String objectInstanceName,
    AttributeHandleSet publishedAttributeHandles)
  {
    super(FederationExecutionMessageProtos.RegisterObjectInstance.newBuilder());

    builder.setObjectInstanceHandle(ObjectInstanceHandles.convert(objectInstanceHandle));
    builder.setObjectClassHandle(ObjectClassHandles.convert(objectClassHandle));
    builder.setObjectInstanceName(objectInstanceName);
    builder.addAllPublishedAttributeHandles(AttributeHandles.convert(publishedAttributeHandles));
  }

  public RegisterObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle,
    String objectInstanceName, AttributeHandleSet publishedAttributeHandles,
    AttributeSetRegionSetPairList attributesAndRegions)
  {
    this(objectInstanceHandle, objectClassHandle, objectInstanceName, publishedAttributeHandles);

    builder.addAllAttributeRegionAssociations(AttributeHandles.convert(attributesAndRegions));
  }

  public RegisterObjectInstance(CodedInputStream in)
    throws IOException
  {
    super(FederationExecutionMessageProtos.RegisterObjectInstance.newBuilder(), in);
  }

  public ObjectInstanceHandle getObjectInstanceHandle()
  {
    return ObjectInstanceHandles.convert(builder.getObjectInstanceHandle());
  }

  public ObjectClassHandle getObjectClassHandle()
  {
    return ObjectClassHandles.convert(builder.getObjectClassHandle());
  }

  public AttributeHandleSet getPublishedAttributeHandles()
  {
    return AttributeHandles.convertAttributeHandles(builder.getPublishedAttributeHandlesList());
  }

  public AttributeSetRegionSetPairList getAttributesAndRegions()
  {
    return AttributeHandles.convert(builder.getAttributeRegionAssociationsList());
  }

  public String getObjectInstanceName()
  {
    return builder.getObjectInstanceName();
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.REGISTER_OBJECT_INSTANCE;
  }

  @Override
  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.registerObjectInstance(federateProxy, this);
  }
}
