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

import net.sf.ohla.rti.Protocol;
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeSetRegionSetPairList;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.AttributeSetRegionSetPairList;
import hla.rti1516e.ObjectClassHandle;

public class RequestObjectClassAttributeValueUpdateWithRegions
  extends ObjectClassMessage
  implements FederationExecutionMessage
{
  private final AttributeSetRegionSetPairList attributesAndRegions;
  private final byte[] tag;

  public RequestObjectClassAttributeValueUpdateWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions, byte[] tag)
  {
    super(MessageType.REQUEST_OBJECT_CLASS_ATTRIBUTE_VALUE_UPDATE_WITH_REGIONS, objectClassHandle);

    this.attributesAndRegions = attributesAndRegions;
    this.tag = tag;

    IEEE1516eAttributeSetRegionSetPairList.encode(buffer, attributesAndRegions);
    Protocol.encodeBytes(buffer, tag);

    encodingFinished();
  }

  public RequestObjectClassAttributeValueUpdateWithRegions(ChannelBuffer buffer)
  {
    super(buffer);

    attributesAndRegions = IEEE1516eAttributeSetRegionSetPairList.decode(buffer);
    tag = Protocol.decodeBytes(buffer);
  }

  public AttributeSetRegionSetPairList getAttributesAndRegions()
  {
    return attributesAndRegions;
  }

  public byte[] getTag()
  {
    return tag;
  }

  public MessageType getType()
  {
    return MessageType.REQUEST_OBJECT_CLASS_ATTRIBUTE_VALUE_UPDATE_WITH_REGIONS;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.requestObjectClassAttributeValueUpdateWithRegions(federateProxy, this);
  }
}
