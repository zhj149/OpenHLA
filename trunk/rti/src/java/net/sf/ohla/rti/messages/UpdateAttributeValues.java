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

import net.sf.ohla.rti.util.AttributeValues;
import net.sf.ohla.rti.util.LogicalTimes;
import net.sf.ohla.rti.util.MessageRetractionHandles;
import net.sf.ohla.rti.util.ObjectInstanceHandles;
import net.sf.ohla.rti.util.TransportationTypeHandles;
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.messages.proto.FederationExecutionMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;
import net.sf.ohla.rti.proto.OHLAProtos;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.TransportationTypeHandle;

public class UpdateAttributeValues
  extends AbstractMessage<FederationExecutionMessageProtos.UpdateAttributeValues, FederationExecutionMessageProtos.UpdateAttributeValues.Builder>
  implements FederationExecutionMessage, TimeStampOrderedMessage
{
  private volatile OrderType sentOrderType;
  private volatile LogicalTime time;
  private volatile MessageRetractionHandle messageRetractionHandle;

  public UpdateAttributeValues(FederationExecutionMessageProtos.UpdateAttributeValues messageLite)
  {
    super(messageLite);
  }

  public UpdateAttributeValues(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues,
    TransportationTypeHandle transportationTypeHandle, byte[] tag)
  {
    super(FederationExecutionMessageProtos.UpdateAttributeValues.newBuilder());

    builder.setObjectInstanceHandle(ObjectInstanceHandles.convert(objectInstanceHandle));
    builder.addAllAttributeValues(AttributeValues.convert(attributeValues));
    builder.setTransportationTypeHandle(TransportationTypeHandles.convert(transportationTypeHandle));

    if (tag != null)
    {
      builder.setTag(ByteString.copyFrom(tag));
    }
  }

  public UpdateAttributeValues(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues,
    TransportationTypeHandle transportationTypeHandle, byte[] tag, OrderType sentOrderType, LogicalTime time,
    MessageRetractionHandle messageRetractionHandle)
  {
    this(objectInstanceHandle, attributeValues, transportationTypeHandle, tag);

    this.time = time;

    builder.setSentOrderType(OHLAProtos.OrderType.values()[sentOrderType.ordinal()]);
    builder.setTime(LogicalTimes.convert(time));
    builder.setMessageRetractionHandle(MessageRetractionHandles.convert(messageRetractionHandle));
  }

  public UpdateAttributeValues(CodedInputStream in)
    throws IOException
  {
    super(FederationExecutionMessageProtos.UpdateAttributeValues.newBuilder(), in);
  }

  public ObjectInstanceHandle getObjectInstanceHandle()
  {
    return ObjectInstanceHandles.convert(builder.getObjectInstanceHandle());
  }

  public AttributeHandleValueMap getAttributeValues()
  {
    return AttributeValues.convert(builder.getAttributeValuesList());
  }

  public byte[] getTag()
  {
    return builder.hasTag() ? builder.getTag().toByteArray() : null;
  }

  public OrderType getSentOrderType()
  {
    if (sentOrderType == null)
    {
      sentOrderType = OrderType.values()[builder.getSentOrderType().ordinal()];
    }
    return sentOrderType;
  }

  public TransportationTypeHandle getTransportationTypeHandle()
  {
    return TransportationTypeHandles.convert(builder.getTransportationTypeHandle());
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

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.UPDATE_ATTRIBUTE_VALUES;
  }

  @Override
  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    if (builder.hasTime())
    {
      time = LogicalTimes.convert(federationExecution.getTimeManager().getLogicalTimeFactory(), builder.getTime());
    }

    federationExecution.updateAttributeValues(federateProxy, this);
  }
}
