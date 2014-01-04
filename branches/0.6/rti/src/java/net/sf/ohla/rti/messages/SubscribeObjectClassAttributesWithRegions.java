/*
 * Copyright (c) 2005-2010, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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

public class SubscribeObjectClassAttributesWithRegions
  extends ObjectClassMessage
  implements FederationExecutionMessage
{
  private final AttributeSetRegionSetPairList attributesAndRegions;
  private final boolean passive;
  private final String updateRateDesignator;

  public SubscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions, boolean passive)
  {
    this(objectClassHandle, attributesAndRegions, passive, null);
  }

  public SubscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions,
    boolean passive, String updateRateDesignator)
  {
    super(MessageType.SUBSCRIBE_OBJECT_CLASS_ATTRIBUTES_WITH_REGIONS, objectClassHandle);

    this.attributesAndRegions = attributesAndRegions;
    this.passive = passive;
    this.updateRateDesignator = updateRateDesignator;

    IEEE1516eAttributeSetRegionSetPairList.encode(buffer, attributesAndRegions);
    Protocol.encodeBoolean(buffer, passive);
    Protocol.encodeOptionalString(buffer, updateRateDesignator);

    encodingFinished();
  }

  public SubscribeObjectClassAttributesWithRegions(ChannelBuffer buffer)
  {
    super(buffer);

    attributesAndRegions = IEEE1516eAttributeSetRegionSetPairList.decode(buffer);
    passive = Protocol.decodeBoolean(buffer);
    updateRateDesignator = Protocol.decodeOptionalString(buffer);
  }

  public AttributeSetRegionSetPairList getAttributesAndRegions()
  {
    return attributesAndRegions;
  }

  public boolean isPassive()
  {
    return passive;
  }

  public String getUpdateRateDesignator()
  {
    return updateRateDesignator;
  }

  public MessageType getType()
  {
    return MessageType.SUBSCRIBE_OBJECT_CLASS_ATTRIBUTES_WITH_REGIONS;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.subscribeObjectClassAttributesWithRegions(federateProxy, this);
  }
}
