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

import net.sf.ohla.rti.util.InteractionClassHandles;
import net.sf.ohla.rti.util.LogicalTimes;
import net.sf.ohla.rti.util.MessageRetractionHandles;
import net.sf.ohla.rti.util.OrderTypes;
import net.sf.ohla.rti.util.ParameterValues;
import net.sf.ohla.rti.util.RegionHandles;
import net.sf.ohla.rti.util.TransportationTypeHandles;
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.messages.proto.FederationExecutionMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RegionHandleSet;
import hla.rti1516e.TransportationTypeHandle;

public class SendInteraction
  extends AbstractMessage<FederationExecutionMessageProtos.SendInteraction, FederationExecutionMessageProtos.SendInteraction.Builder>
  implements FederationExecutionMessage, TimeStampOrderedMessage
{
  private volatile OrderType sentOrderType;
  private volatile LogicalTime time;
  private volatile MessageRetractionHandle messageRetractionHandle;
  private volatile RegionHandleSet sentRegionHandles;

  public SendInteraction(FederationExecutionMessageProtos.SendInteraction messageLite)
  {
    super(messageLite);
  }

  public SendInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues,
    TransportationTypeHandle transportationTypeHandle, byte[] tag)
  {
    super(FederationExecutionMessageProtos.SendInteraction.newBuilder());

    builder.setInteractionClassHandle(InteractionClassHandles.convert(interactionClassHandle));
    builder.addAllParameterValues(ParameterValues.convert(parameterValues));
    builder.setTransportationTypeHandle(TransportationTypeHandles.convert(transportationTypeHandle));

    if (tag != null)
    {
      builder.setTag(ByteString.copyFrom(tag));
    }
  }

  public SendInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues,
    TransportationTypeHandle transportationTypeHandle, byte[] tag, OrderType sentOrderType, LogicalTime time,
    MessageRetractionHandle messageRetractionHandle)
  {
    this(interactionClassHandle, parameterValues, transportationTypeHandle, tag);

    builder.setSentOrderType(OrderTypes.convert(sentOrderType));
    builder.setTime(LogicalTimes.convert(time));
    builder.setMessageRetractionHandle(MessageRetractionHandles.convert(messageRetractionHandle));
  }

  public SendInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
    TransportationTypeHandle transportationTypeHandle, RegionHandleSet sentRegionHandles)
  {
    this(interactionClassHandle, parameterValues, transportationTypeHandle, tag);

    builder.addAllSentRegionHandles(RegionHandles.convertToProto(sentRegionHandles));
  }

  public SendInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, LogicalTime time,
    MessageRetractionHandle messageRetractionHandle, RegionHandleSet sentRegionHandles)
  {
    this(interactionClassHandle, parameterValues, transportationTypeHandle, tag, sentOrderType, time,
         messageRetractionHandle);

    builder.addAllSentRegionHandles(RegionHandles.convertToProto(sentRegionHandles));
  }

  public SendInteraction(CodedInputStream in)
    throws IOException
  {
    super(FederationExecutionMessageProtos.SendInteraction.newBuilder(), in);
  }

  public InteractionClassHandle getInteractionClassHandle()
  {
    return InteractionClassHandles.convert(builder.getInteractionClassHandle());
  }

  public ParameterHandleValueMap getParameterValues()
  {
    return ParameterValues.convert(builder.getParameterValuesList());
  }

  public OrderType getSentOrderType()
  {
    if (sentOrderType == null)
    {
      sentOrderType = OrderType.values()[builder.getSentOrderType().ordinal()];
    }
    return sentOrderType;
  }

  public LogicalTime getTime(LogicalTimeFactory logicalTimeFactory)
  {
    if (time == null && builder.hasTime())
    {
      time = LogicalTimes.convert(logicalTimeFactory, builder.getTime());
    }
    return time;
  }

  public MessageRetractionHandle getMessageRetractionHandle()
  {
    if (messageRetractionHandle == null && builder.hasMessageRetractionHandle())
    {
      messageRetractionHandle = MessageRetractionHandles.convert(builder.getMessageRetractionHandle());
    }
    return messageRetractionHandle;
  }

  public RegionHandleSet getSentRegionHandles()
  {
    if (sentRegionHandles == null && builder.getSentRegionHandlesCount() > 0)
    {
      sentRegionHandles = RegionHandles.convertFromProto(builder.getSentRegionHandlesList());
    }
    return sentRegionHandles;
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.SEND_INTERACTION;
  }

  @Override
  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.sendInteraction(federateProxy, this);
  }
}
