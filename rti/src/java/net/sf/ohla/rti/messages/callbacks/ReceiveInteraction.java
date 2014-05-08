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

import java.io.IOException;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import net.sf.ohla.rti.util.FederateHandles;
import net.sf.ohla.rti.util.InteractionClassHandles;
import net.sf.ohla.rti.util.LogicalTimes;
import net.sf.ohla.rti.util.MessageRetractionHandles;
import net.sf.ohla.rti.util.OrderTypes;
import net.sf.ohla.rti.util.ParameterValues;
import net.sf.ohla.rti.util.Regions;
import net.sf.ohla.rti.util.TransportationTypeHandles;
import net.sf.ohla.rti.federate.Callback;
import net.sf.ohla.rti.federate.Federate;
import net.sf.ohla.rti.messages.AbstractMessage;
import net.sf.ohla.rti.messages.FederateMessage;
import net.sf.ohla.rti.messages.proto.FederateMessageProtos;
import net.sf.ohla.rti.messages.proto.FederationExecutionMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.DimensionHandle;
import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RangeBounds;
import hla.rti1516e.RegionHandle;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.FederateInternalError;

public class ReceiveInteraction
  extends AbstractMessage<FederateMessageProtos.ReceiveInteraction, FederateMessageProtos.ReceiveInteraction.Builder>
  implements Callback, FederateMessage
{
  private Federate federate;
  private LogicalTime time;
  private MessageRetractionHandle messageRetractionHandle;

  public ReceiveInteraction(
    FederationExecutionMessageProtos.SendInteraction.Builder sendInteraction, ParameterHandleValueMap parameterValues,
    OrderType receivedOrderType, FederateHandle producingFederateHandle,
    Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regions)
  {
    super(FederateMessageProtos.ReceiveInteraction.newBuilder());

    builder.setInteractionClassHandle(sendInteraction.getInteractionClassHandle());
    builder.addAllParameterValues(ParameterValues.convert(parameterValues));

    if (sendInteraction.hasTag())
    {
      builder.setTag(sendInteraction.getTag());
    }

    builder.setSentOrderType(sendInteraction.getSentOrderType());
    builder.setReceivedOrderType(OrderTypes.convert(receivedOrderType));
    builder.setTransportationTypeHandle(sendInteraction.getTransportationTypeHandle());

    if (sendInteraction.hasTime())
    {
      builder.setTime(sendInteraction.getTime());
    }

    if (receivedOrderType == OrderType.TIMESTAMP)
    {
      assert sendInteraction.hasMessageRetractionHandle();

      builder.setMessageRetractionHandle(sendInteraction.getMessageRetractionHandle());
    }

    builder.setProducingFederateHandle(FederateHandles.convert(producingFederateHandle));

    if (regions != null && regions.size() > 0)
    {
      builder.addAllRegions(Regions.convertToProtos(regions.values()));
    }
  }

  public ReceiveInteraction(CodedInputStream in)
    throws IOException
  {
    super(FederateMessageProtos.ReceiveInteraction.newBuilder(), in);
  }

  public InteractionClassHandle getInteractionClassHandle()
  {
    return InteractionClassHandles.convert(builder.getInteractionClassHandle());
  }

  public ParameterHandleValueMap getParameterValues()
  {
    return ParameterValues.convert(builder.getParameterValuesList());
  }

  public byte[] getTag()
  {
    return builder.hasTag() ? builder.getTag().toByteArray() : null;
  }

  public OrderType getSentOrderType()
  {
    return OrderTypes.convert(builder.getSentOrderType());
  }

  public OrderType getReceivedOrderType()
  {
    return OrderTypes.convert(builder.getReceivedOrderType());
  }

  public TransportationTypeHandle getTransportationTypeHandle()
  {
    return TransportationTypeHandles.convert(builder.getTransportationTypeHandle());
  }

  public LogicalTime getTime()
  {
    return time;
  }

  public MessageRetractionHandle getMessageRetractionHandle()
  {
    return messageRetractionHandle;
  }

  public FederateHandle getProducingFederateHandle()
  {
    return FederateHandles.convert(builder.getProducingFederateHandle());
  }

  public boolean hasSentRegions()
  {
    return builder.getRegionsCount() > 0;
  }

  public Collection<Map<DimensionHandle, RangeBounds>> getSentRegions()
  {
    Collection<Map<DimensionHandle, RangeBounds>> regions;
    if (builder.getRegionsCount() == 0)
    {
      regions = Collections.emptyList();
    }
    else
    {
      regions = Regions.convertFromProtos(builder.getRegionsList());
    }
    return regions;
  }

  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.RECEIVE_INTERACTION;
  }

  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    federate.fireReceiveInteraction(this);
  }

  public void execute(Federate federate)
  {
    this.federate = federate;

    if (builder.hasTime())
    {
      time = LogicalTimes.convert(federate.getLogicalTimeFactory(), builder.getTime());
    }

    if (builder.hasMessageRetractionHandle())
    {
      messageRetractionHandle = MessageRetractionHandles.convert(builder.getMessageRetractionHandle());
    }

    federate.receiveInteraction(this);
  }
}
