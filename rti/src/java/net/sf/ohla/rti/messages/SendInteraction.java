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
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eMessageRetractionHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eParameterHandleValueMap;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eRegionHandleSet;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eTransportationTypeHandle;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RegionHandleSet;
import hla.rti1516e.TransportationTypeHandle;

public class SendInteraction
  extends InteractionClassMessage
  implements FederationExecutionMessage
{
  private final ParameterHandleValueMap parameterValues;
  private final byte[] tag;
  private final OrderType sentOrderType;
  private final TransportationTypeHandle transportationTypeHandle;
  private final LogicalTime time;
  private final MessageRetractionHandle messageRetractionHandle;
  private final RegionHandleSet sentRegionHandles;

  public SendInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
    TransportationTypeHandle transportationTypeHandle)
  {
    super(MessageType.SEND_INTERACTION, interactionClassHandle);

    this.parameterValues = parameterValues;
    this.tag = tag;
    this.transportationTypeHandle = transportationTypeHandle;

    sentOrderType = OrderType.RECEIVE;
    time = null;
    messageRetractionHandle = null;
    sentRegionHandles = null;

    IEEE1516eParameterHandleValueMap.encode(buffer, parameterValues);
    Protocol.encodeBytes(buffer, tag);
    Protocol.encodeEnum(buffer, sentOrderType);
    IEEE1516eTransportationTypeHandle.encode(buffer, transportationTypeHandle);
    Protocol.encodeNullTime(buffer);
    IEEE1516eMessageRetractionHandle.encode(buffer, messageRetractionHandle);
    IEEE1516eRegionHandleSet.encode(buffer, sentRegionHandles);

    encodingFinished();
  }

  public SendInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, LogicalTime time,
    MessageRetractionHandle messageRetractionHandle)
  {
    super(MessageType.SEND_INTERACTION, interactionClassHandle);

    this.parameterValues = parameterValues;
    this.tag = tag;
    this.sentOrderType = sentOrderType;
    this.transportationTypeHandle = transportationTypeHandle;
    this.time = time;
    this.messageRetractionHandle = messageRetractionHandle;

    sentRegionHandles = null;

    IEEE1516eParameterHandleValueMap.encode(buffer, parameterValues);
    Protocol.encodeBytes(buffer, tag);
    Protocol.encodeEnum(buffer, sentOrderType);
    IEEE1516eTransportationTypeHandle.encode(buffer, transportationTypeHandle);
    Protocol.encodeTime(buffer, time);
    IEEE1516eMessageRetractionHandle.encode(buffer, messageRetractionHandle);
    IEEE1516eRegionHandleSet.encode(buffer, sentRegionHandles);

    encodingFinished();
  }

  public SendInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
    TransportationTypeHandle transportationTypeHandle, RegionHandleSet sentRegionHandles)
  {
    super(MessageType.SEND_INTERACTION, interactionClassHandle);

    this.parameterValues = parameterValues;
    this.tag = tag;
    this.transportationTypeHandle = transportationTypeHandle;
    this.sentRegionHandles = sentRegionHandles;

    sentOrderType = OrderType.RECEIVE;
    time = null;
    messageRetractionHandle = null;

    IEEE1516eParameterHandleValueMap.encode(buffer, parameterValues);
    Protocol.encodeBytes(buffer, tag);
    Protocol.encodeEnum(buffer, sentOrderType);
    IEEE1516eTransportationTypeHandle.encode(buffer, transportationTypeHandle);
    Protocol.encodeNullTime(buffer);
    IEEE1516eMessageRetractionHandle.encode(buffer, messageRetractionHandle);
    IEEE1516eRegionHandleSet.encode(buffer, sentRegionHandles);

    encodingFinished();
  }

  public SendInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, LogicalTime time,
    MessageRetractionHandle messageRetractionHandle, RegionHandleSet sentRegionHandles)
  {
    super(MessageType.SEND_INTERACTION, interactionClassHandle);

    this.parameterValues = parameterValues;
    this.tag = tag;
    this.sentOrderType = sentOrderType;
    this.transportationTypeHandle = transportationTypeHandle;
    this.time = time;
    this.messageRetractionHandle = messageRetractionHandle;
    this.sentRegionHandles = sentRegionHandles;

    IEEE1516eParameterHandleValueMap.encode(buffer, parameterValues);
    Protocol.encodeBytes(buffer, tag);
    Protocol.encodeEnum(buffer, sentOrderType);
    IEEE1516eTransportationTypeHandle.encode(buffer, transportationTypeHandle);
    Protocol.encodeTime(buffer, time);
    IEEE1516eMessageRetractionHandle.encode(buffer, messageRetractionHandle);
    IEEE1516eRegionHandleSet.encode(buffer, sentRegionHandles);

    encodingFinished();
  }

  public SendInteraction(ChannelBuffer buffer, LogicalTimeFactory timeFactory)
  {
    super(buffer);

    parameterValues = IEEE1516eParameterHandleValueMap.decode(buffer);
    tag = Protocol.decodeBytes(buffer);
    sentOrderType = Protocol.decodeEnum(buffer, OrderType.values());
    transportationTypeHandle = IEEE1516eTransportationTypeHandle.decode(buffer);
    time = Protocol.decodeTime(buffer, timeFactory);
    messageRetractionHandle = IEEE1516eMessageRetractionHandle.decode(buffer);
    sentRegionHandles = IEEE1516eRegionHandleSet.decode(buffer);
  }

  public ParameterHandleValueMap getParameterValues()
  {
    return parameterValues;
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

  public RegionHandleSet getSentRegionHandles()
  {
    return sentRegionHandles;
  }

  public MessageType getType()
  {
    return MessageType.SEND_INTERACTION;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.sendInteraction(federateProxy, this);
  }
}
