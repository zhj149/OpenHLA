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

import net.sf.ohla.rti.Protocol;
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleValueMap;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eMessageRetractionHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eTransportationTypeHandle;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.TransportationTypeHandle;

public class UpdateAttributeValues
  extends ObjectInstanceMessage
  implements FederationExecutionMessage
{
  private final AttributeHandleValueMap attributeValues;
  private final byte[] tag;
  private final OrderType sentOrderType;
  private final TransportationTypeHandle transportationTypeHandle;
  private final LogicalTime time;
  private final MessageRetractionHandle messageRetractionHandle;

  public UpdateAttributeValues(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues, byte[] tag,
    TransportationTypeHandle transportationTypeHandle)
  {
    super(MessageType.UPDATE_ATTRIBUTE_VALUES, objectInstanceHandle);

    this.attributeValues = attributeValues;
    this.tag = tag;
    this.transportationTypeHandle = transportationTypeHandle;

    sentOrderType = OrderType.RECEIVE;
    time = null;
    messageRetractionHandle = null;

    IEEE1516eAttributeHandleValueMap.encode(buffer, attributeValues);
    Protocol.encodeBytes(buffer, tag);
    Protocol.encodeEnum(buffer, sentOrderType);
    IEEE1516eTransportationTypeHandle.encode(buffer, transportationTypeHandle);
    Protocol.encodeNullTime(buffer);
    IEEE1516eMessageRetractionHandle.encode(buffer, messageRetractionHandle);

    encodingFinished();
  }

  public UpdateAttributeValues(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, LogicalTime time,
    MessageRetractionHandle messageRetractionHandle)
  {
    super(MessageType.UPDATE_ATTRIBUTE_VALUES, objectInstanceHandle);

    this.attributeValues = attributeValues;
    this.tag = tag;
    this.sentOrderType = sentOrderType;
    this.transportationTypeHandle = transportationTypeHandle;
    this.time = time;
    this.messageRetractionHandle = messageRetractionHandle;

    IEEE1516eAttributeHandleValueMap.encode(buffer, attributeValues);
    Protocol.encodeBytes(buffer, tag);
    Protocol.encodeEnum(buffer, sentOrderType);
    IEEE1516eTransportationTypeHandle.encode(buffer, transportationTypeHandle);
    Protocol.encodeTime(buffer, time);
    IEEE1516eMessageRetractionHandle.encode(buffer, messageRetractionHandle);

    encodingFinished();
  }

  public UpdateAttributeValues(ChannelBuffer buffer, LogicalTimeFactory factory)
  {
    super(buffer);

    attributeValues = IEEE1516eAttributeHandleValueMap.decode(buffer);
    tag = Protocol.decodeBytes(buffer);
    sentOrderType = Protocol.decodeEnum(buffer, OrderType.values());
    transportationTypeHandle = IEEE1516eTransportationTypeHandle.decode(buffer);
    time = Protocol.decodeTime(buffer, factory);
    messageRetractionHandle = IEEE1516eMessageRetractionHandle.decode(buffer);
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

  public LogicalTime getTime()
  {
    return time;
  }

  public MessageRetractionHandle getMessageRetractionHandle()
  {
    return messageRetractionHandle;
  }

  public MessageType getType()
  {
    return MessageType.UPDATE_ATTRIBUTE_VALUES;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.updateAttributeValues(federateProxy, this);
  }
}
