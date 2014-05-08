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

public class AssociateRegionsForUpdates
  extends
  AbstractRequest<FederationExecutionMessageProtos.AssociateRegionsForUpdates, FederationExecutionMessageProtos.AssociateRegionsForUpdates.Builder, AssociateRegionsForUpdatesResponse>
implements FederationExecutionMessage
{
  private volatile ObjectInstanceHandle objectInstanceHandle;
  private volatile AttributeSetRegionSetPairList attributesAndRegions;

  public AssociateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle, AttributeSetRegionSetPairList attributesAndRegions)
  {
    super(FederationExecutionMessageProtos.AssociateRegionsForUpdates.newBuilder());

    this.objectInstanceHandle = objectInstanceHandle;
    this.attributesAndRegions = attributesAndRegions;

    builder.setObjectInstanceHandle(ObjectInstanceHandles.convert(objectInstanceHandle));
    builder.addAllAttributeRegionAssociations(AttributeHandles.convert(attributesAndRegions));
  }

  public AssociateRegionsForUpdates(CodedInputStream in)
    throws IOException
  {
    super(FederationExecutionMessageProtos.AssociateRegionsForUpdates.newBuilder(), in);
  }

  public ObjectInstanceHandle getObjectInstanceHandle()
  {
    if (objectInstanceHandle == null)
    {
      objectInstanceHandle = ObjectInstanceHandles.convert(builder.getObjectInstanceHandle());
    }
    return objectInstanceHandle;
  }

  public AttributeSetRegionSetPairList getAttributesAndRegions()
  {
    if (attributesAndRegions == null)
    {
      attributesAndRegions = AttributeHandles.convert(builder.getAttributeRegionAssociationsList());
    }
    return attributesAndRegions;
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.ASSOCIATE_REGIONS_FOR_UPDATES;
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
    federationExecution.associateRegionsForUpdates(federateProxy, this);
  }
}
