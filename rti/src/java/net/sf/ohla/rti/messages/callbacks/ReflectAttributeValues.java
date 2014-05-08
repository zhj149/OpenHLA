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

import net.sf.ohla.rti.util.AttributeValues;
import net.sf.ohla.rti.util.FederateHandles;
import net.sf.ohla.rti.util.LogicalTimes;
import net.sf.ohla.rti.util.MessageRetractionHandles;
import net.sf.ohla.rti.util.ObjectInstanceHandles;
import net.sf.ohla.rti.util.OrderTypes;
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
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.DimensionHandle;
import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.RangeBounds;
import hla.rti1516e.RegionHandle;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.FederateInternalError;

public class ReflectAttributeValues
  extends AbstractMessage<FederateMessageProtos.ReflectAttributeValues, FederateMessageProtos.ReflectAttributeValues.Builder>
  implements Callback, FederateMessage
{
  private Federate federate;
  private LogicalTime time;
  private MessageRetractionHandle messageRetractionHandle;

  public ReflectAttributeValues(
    FederationExecutionMessageProtos.UpdateAttributeValues.Builder updateAttributeValues,
    AttributeHandleValueMap attributeValues, OrderType receivedOrderType, FederateHandle producingFederateHandle,
    Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regions)
  {
    super(FederateMessageProtos.ReflectAttributeValues.newBuilder());

    builder.setObjectInstanceHandle(updateAttributeValues.getObjectInstanceHandle());
    builder.addAllAttributeValues(AttributeValues.convert(attributeValues));

    if (updateAttributeValues.hasTag())
    {
      builder.setTag(updateAttributeValues.getTag());
    }

    builder.setSentOrderType(updateAttributeValues.getSentOrderType());
    builder.setReceivedOrderType(OrderTypes.convert(receivedOrderType));
    builder.setTransportationTypeHandle(updateAttributeValues.getTransportationTypeHandle());

    if (updateAttributeValues.hasTime())
    {
      builder.setTime(updateAttributeValues.getTime());
    }

    if (receivedOrderType == OrderType.TIMESTAMP)
    {
      assert updateAttributeValues.hasMessageRetractionHandle();

      builder.setMessageRetractionHandle(updateAttributeValues.getMessageRetractionHandle());
    }

    builder.setProducingFederateHandle(FederateHandles.convert(producingFederateHandle));

    if (regions != null && regions.size() > 0)
    {
      builder.addAllRegions(Regions.convertToProtos(regions.values()));
    }
  }

  public ReflectAttributeValues(CodedInputStream in)
    throws IOException
  {
    super(FederateMessageProtos.ReflectAttributeValues.newBuilder(), in);
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
    return OrderType.values()[builder.getSentOrderType().ordinal()];
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

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.REFLECT_ATTRIBUTE_VALUES;
  }

  @Override
  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    federate.fireReflectAttributeValues(this);
  }

  @Override
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

    federate.reflectAttributeValues(this);
  }
}
