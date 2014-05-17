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
import hla.rti1516e.AttributeSetRegionSetPairList;
import hla.rti1516e.ObjectInstanceHandle;

public class UnassociateRegionsForUpdates
  extends AbstractRequest<FederationExecutionMessageProtos.UnassociateRegionsForUpdates, FederationExecutionMessageProtos.UnassociateRegionsForUpdates.Builder, UnassociateRegionsForUpdatesResponse>
  implements FederationExecutionMessage
{
  public UnassociateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle, AttributeSetRegionSetPairList attributesAndRegions)
  {
    super(FederationExecutionMessageProtos.UnassociateRegionsForUpdates.newBuilder());

    builder.setObjectInstanceHandle(ObjectInstanceHandles.convert(objectInstanceHandle));
    builder.addAllAttributeRegionAssociations(AttributeHandles.convert(attributesAndRegions));
  }

  public UnassociateRegionsForUpdates(CodedInputStream in) throws IOException
  {
    super(FederationExecutionMessageProtos.UnassociateRegionsForUpdates.newBuilder(), in);
  }

  public ObjectInstanceHandle getObjectInstanceHandle()
  {
    return ObjectInstanceHandles.convert(builder.getObjectInstanceHandle());
  }

  public AttributeSetRegionSetPairList getAttributesAndRegions()
  {
    return AttributeHandles.convert(builder.getAttributeRegionAssociationsList());
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.UNASSOCIATE_REGIONS_FOR_UPDATES;
  }

  @Override
  public long getRequestId()
  {
    return builder.getRequestId();
  }

  @Override
  public void setRequestId(long requestId)
  {
    builder.setRequestId(requestId);
  }

  @Override
  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.unassociateRegionsForUpdates(federateProxy, this);
  }
}
