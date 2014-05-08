/*
 * Copyright (c) 2005-2010, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import hla.rti1516e.AttributeSetRegionSetPairList;
import hla.rti1516e.ObjectClassHandle;

public class RequestObjectClassAttributeValueUpdateWithRegions
  extends
  AbstractMessage<FederationExecutionMessageProtos.RequestObjectClassAttributeValueUpdateWithRegions, FederationExecutionMessageProtos.RequestObjectClassAttributeValueUpdateWithRegions.Builder>
implements FederationExecutionMessage
{
  public RequestObjectClassAttributeValueUpdateWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions, byte[] tag)
  {
    super(FederationExecutionMessageProtos.RequestObjectClassAttributeValueUpdateWithRegions.newBuilder());

    builder.setObjectClassHandle(ObjectClassHandles.convert(objectClassHandle));
    builder.addAllAttributeRegionAssociations(AttributeHandles.convert(attributesAndRegions));

    if (tag != null)
    {
      builder.setTag(ByteString.copyFrom(tag));
    }
  }

  public RequestObjectClassAttributeValueUpdateWithRegions(CodedInputStream in)
    throws IOException
  {
    super(FederationExecutionMessageProtos.RequestObjectClassAttributeValueUpdateWithRegions.newBuilder(), in);
  }

  public ObjectClassHandle getObjectClassHandle()
  {
    return ObjectClassHandles.convert(builder.getObjectClassHandle());
  }

  public AttributeSetRegionSetPairList getAttributesAndRegions()
  {
    return AttributeHandles.convert(builder.getAttributeRegionAssociationsList());
  }

  public byte[] getTag()
  {
    return builder.hasTag() ? builder.getTag().toByteArray() : null;
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.REQUEST_OBJECT_CLASS_ATTRIBUTE_VALUE_UPDATE_WITH_REGIONS;
  }

  @Override
  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.requestObjectClassAttributeValueUpdateWithRegions(federateProxy, this);
  }
}
