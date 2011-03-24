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

import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeSetRegionSetPairList;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eObjectInstanceHandle;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.AttributeSetRegionSetPairList;
import hla.rti1516e.ObjectInstanceHandle;

public class AssociateRegionsForUpdates
  extends AbstractRequest<AssociateRegionsForUpdatesResponse>
  implements FederationExecutionMessage
{
  private final ObjectInstanceHandle objectInstanceHandle;
  private final AttributeSetRegionSetPairList attributesAndRegions;

  public AssociateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle, AttributeSetRegionSetPairList attributesAndRegions)
  {
    super(MessageType.ASSOCIATE_REGIONS_FOR_UPDATES);

    this.objectInstanceHandle = objectInstanceHandle;
    this.attributesAndRegions = attributesAndRegions;

    IEEE1516eObjectInstanceHandle.encode(buffer, objectInstanceHandle);
    IEEE1516eAttributeSetRegionSetPairList.encode(buffer, attributesAndRegions);

    encodingFinished();
  }

  public AssociateRegionsForUpdates(ChannelBuffer buffer)
  {
    super(buffer);

    objectInstanceHandle = IEEE1516eObjectInstanceHandle.decode(buffer);
    attributesAndRegions = IEEE1516eAttributeSetRegionSetPairList.decode(buffer);
  }

  public ObjectInstanceHandle getObjectInstanceHandle()
  {
    return objectInstanceHandle;
  }

  public AttributeSetRegionSetPairList getAttributesAndRegions()
  {
    return attributesAndRegions;
  }

  public MessageType getType()
  {
    return MessageType.ASSOCIATE_REGIONS_FOR_UPDATES;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.associateRegionsForUpdates(federateProxy, this);
  }
}
