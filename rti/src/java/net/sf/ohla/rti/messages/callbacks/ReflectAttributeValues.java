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

import java.util.Collection;
import java.util.Map;

import net.sf.ohla.rti.Protocol;
import net.sf.ohla.rti.federate.Callback;
import net.sf.ohla.rti.federate.Federate;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleValueMap;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eMessageRetractionHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eTransportationTypeHandle;
import net.sf.ohla.rti.messages.FederateMessage;
import net.sf.ohla.rti.messages.MessageType;
import net.sf.ohla.rti.messages.ObjectInstanceMessage;
import net.sf.ohla.rti.messages.TimeStampOrderedMessage;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.DimensionHandle;
import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.RangeBounds;
import hla.rti1516e.RegionHandle;
import hla.rti1516e.RegionHandleSet;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.FederateInternalError;

public class ReflectAttributeValues
  extends ObjectInstanceMessage
  implements Callback, FederateMessage, TimeStampOrderedMessage, FederateAmbassador.SupplementalReflectInfo
{
  private final AttributeHandleValueMap attributeValues;
  private final byte[] tag;
  private final OrderType sentOrderType;
  private final TransportationTypeHandle transportationTypeHandle;
  private final LogicalTime time;
  private final MessageRetractionHandle messageRetractionHandle;
  private final FederateHandle producingFederateHandle;
  private final Collection<Map<DimensionHandle, RangeBounds>> regions;

  private RegionHandleSet sentRegions;

  private Federate federate;

  public ReflectAttributeValues(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, LogicalTime time,
    MessageRetractionHandle messageRetractionHandle, FederateHandle producingFederateHandle,
    Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regions)
  {
    this(objectInstanceHandle, attributeValues, tag, sentOrderType, transportationTypeHandle, time,
         messageRetractionHandle, producingFederateHandle, regions == null ? null : regions.values());
  }

  public ReflectAttributeValues(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, LogicalTime time,
    MessageRetractionHandle messageRetractionHandle, FederateHandle producingFederateHandle,
    Collection<Map<DimensionHandle, RangeBounds>> regions)
  {
    super(MessageType.REFLECT_ATTRIBUTE_VALUES, objectInstanceHandle);

    this.attributeValues = attributeValues;
    this.tag = tag;
    this.sentOrderType = sentOrderType;
    this.transportationTypeHandle = transportationTypeHandle;
    this.time = time;
    this.messageRetractionHandle = messageRetractionHandle;
    this.producingFederateHandle = producingFederateHandle;
    this.regions = regions;

    IEEE1516eAttributeHandleValueMap.encode(buffer, attributeValues);
    Protocol.encodeBytes(buffer, tag);
    Protocol.encodeEnum(buffer, sentOrderType);
    IEEE1516eTransportationTypeHandle.encode(buffer, transportationTypeHandle);
    Protocol.encodeTime(buffer, time);
    IEEE1516eMessageRetractionHandle.encode(buffer, messageRetractionHandle);
    IEEE1516eFederateHandle.encode(buffer, producingFederateHandle);
    Protocol.encodeRegions(buffer, this.regions);

    encodingFinished();
  }

  public ReflectAttributeValues(ChannelBuffer buffer, LogicalTimeFactory logicalTimeFactory)
  {
    super(buffer);

    attributeValues = IEEE1516eAttributeHandleValueMap.decode(buffer);
    tag = Protocol.decodeBytes(buffer);
    sentOrderType = Protocol.decodeEnum(buffer, OrderType.values());
    transportationTypeHandle = IEEE1516eTransportationTypeHandle.decode(buffer);
    time = Protocol.decodeTime(buffer, logicalTimeFactory);
    messageRetractionHandle = IEEE1516eMessageRetractionHandle.decode(buffer);
    producingFederateHandle = IEEE1516eFederateHandle.decode(buffer);
    regions = Protocol.decodeRegions(buffer);
  }

  public AttributeHandleValueMap getAttributeValues()
  {
    return attributeValues;
  }

  public byte[] getTag()
  {
    return tag;
  }

  public OrderType getSentOrderType()
  {
    return sentOrderType;
  }

  public TransportationTypeHandle getTransportationTypeHandle()
  {
    return transportationTypeHandle;
  }

  public MessageRetractionHandle getMessageRetractionHandle()
  {
    return messageRetractionHandle;
  }

  public Collection<Map<DimensionHandle, RangeBounds>> getRegions()
  {
    return regions;
  }

  public void setSentRegions(RegionHandleSet sentRegions)
  {
    this.sentRegions = sentRegions;
  }

  public MessageType getType()
  {
    return MessageType.REFLECT_ATTRIBUTE_VALUES;
  }

  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    federate.fireReflectAttributeValues(this);
  }

  public void execute(Federate federate)
  {
    this.federate = federate;

    federate.reflectAttributeValues(this);
  }

  public LogicalTime getTime()
  {
    return time;
  }

  public TimeStampOrderedMessage makeReceiveOrdered()
  {
    return new ReflectAttributeValues(
      objectInstanceHandle, attributeValues, tag, OrderType.RECEIVE, transportationTypeHandle, time, null,
      producingFederateHandle, regions);
  }

  @SuppressWarnings("unchecked")
  public int compareTo(TimeStampOrderedMessage rhs)
  {
    return time.compareTo(rhs.getTime());
  }

  public boolean hasProducingFederate()
  {
    return producingFederateHandle != null;
  }

  public boolean hasSentRegions()
  {
    return regions != null;
  }

  public FederateHandle getProducingFederate()
  {
    return producingFederateHandle;
  }

  public RegionHandleSet getSentRegions()
  {
    return sentRegions;
  }
}
