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
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eMessageRetractionHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eParameterHandleValueMap;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eTransportationTypeHandle;
import net.sf.ohla.rti.messages.FederateMessage;
import net.sf.ohla.rti.messages.InteractionClassMessage;
import net.sf.ohla.rti.messages.MessageType;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.DimensionHandle;
import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RangeBounds;
import hla.rti1516e.RegionHandle;
import hla.rti1516e.RegionHandleSet;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.FederateInternalError;

public class ReceiveInteraction
  extends InteractionClassMessage
  implements Callback, FederateMessage, FederateAmbassador.SupplementalReceiveInfo
{
  private final ParameterHandleValueMap parameterValues;
  private final byte[] tag;
  private final OrderType sentOrderType;
  private final TransportationTypeHandle transportationTypeHandle;
  private final LogicalTime time;
  private final MessageRetractionHandle messageRetractionHandle;
  private final FederateHandle producingFederateHandle;
  private final Collection<Map<DimensionHandle, RangeBounds>> regions;

  private RegionHandleSet sentRegions;
  private OrderType receivedOrderType;

  private Federate federate;

  public ReceiveInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, LogicalTime time,
    MessageRetractionHandle messageRetractionHandle, FederateHandle producingFederateHandle,
    Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regions)
  {
    super(MessageType.RECEIVE_INTERACTION, interactionClassHandle);

    this.parameterValues = parameterValues;
    this.tag = tag;
    this.sentOrderType = sentOrderType;
    this.transportationTypeHandle = transportationTypeHandle;
    this.time = time;
    this.messageRetractionHandle = messageRetractionHandle;
    this.producingFederateHandle = producingFederateHandle;
    this.regions = regions == null ? null : regions.values();

    IEEE1516eParameterHandleValueMap.encode(buffer, parameterValues);
    Protocol.encodeBytes(buffer, tag);
    Protocol.encodeEnum(buffer, sentOrderType);
    IEEE1516eTransportationTypeHandle.encode(buffer, transportationTypeHandle);
    Protocol.encodeTime(buffer, time);
    IEEE1516eMessageRetractionHandle.encode(buffer, messageRetractionHandle);
    IEEE1516eFederateHandle.encode(buffer, producingFederateHandle);
    Protocol.encodeRegions(buffer, this.regions);

    encodingFinished();
  }

  public ReceiveInteraction(ChannelBuffer buffer, LogicalTimeFactory logicalTimeFactory)
  {
    super(buffer);

    parameterValues = IEEE1516eParameterHandleValueMap.decode(buffer);
    tag = Protocol.decodeBytes(buffer);
    sentOrderType = Protocol.decodeEnum(buffer, OrderType.values());
    transportationTypeHandle = IEEE1516eTransportationTypeHandle.decode(buffer);
    time = Protocol.decodeTime(buffer, logicalTimeFactory);
    messageRetractionHandle = IEEE1516eMessageRetractionHandle.decode(buffer);
    producingFederateHandle = IEEE1516eFederateHandle.decode(buffer);
    regions = Protocol.decodeRegions(buffer);
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

  public Collection<Map<DimensionHandle, RangeBounds>> getRegions()
  {
    return regions;
  }

  public OrderType getReceivedOrderType()
  {
    return receivedOrderType;
  }

  public void setReceivedOrderType(OrderType receivedOrderType)
  {
    this.receivedOrderType = receivedOrderType;
  }

  public void setSentRegions(RegionHandleSet sentRegions)
  {
    this.sentRegions = sentRegions;
  }

  public MessageType getType()
  {
    return MessageType.RECEIVE_INTERACTION;
  }

  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    federate.fireReceiveInteraction(this);
  }

  public void execute(Federate federate)
  {
    this.federate = federate;

    federate.receiveInteraction(this);
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
