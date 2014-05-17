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

import net.sf.ohla.rti.util.FederateHandles;
import net.sf.ohla.rti.util.LogicalTimes;
import net.sf.ohla.rti.util.MessageRetractionHandles;
import net.sf.ohla.rti.util.ObjectInstanceHandles;
import net.sf.ohla.rti.util.OrderTypes;
import net.sf.ohla.rti.federate.Callback;
import net.sf.ohla.rti.federate.Federate;
import net.sf.ohla.rti.messages.AbstractMessage;
import net.sf.ohla.rti.messages.FederateMessage;
import net.sf.ohla.rti.messages.proto.FederateMessageProtos;
import net.sf.ohla.rti.messages.proto.FederationExecutionMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;
import net.sf.ohla.rti.proto.OHLAProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.exceptions.FederateInternalError;

public class RemoveObjectInstance
  extends AbstractMessage<FederateMessageProtos.RemoveObjectInstance, FederateMessageProtos.RemoveObjectInstance.Builder>
  implements Callback, FederateMessage
{
  private Federate federate;
  private LogicalTime time;
  private MessageRetractionHandle messageRetractionHandle;

  public RemoveObjectInstance(ObjectInstanceHandle objectInstanceHandle, FederateHandle producingFederateHandle)
  {
    this(objectInstanceHandle, producingFederateHandle, OrderTypes.convert(OrderType.RECEIVE));
  }

  public RemoveObjectInstance(
    FederationExecutionMessageProtos.DeleteObjectInstance.Builder deleteObjectInstance, OrderType receivedOrderType,
    FederateHandle producingFederateHandle)
  {
    super(FederateMessageProtos.RemoveObjectInstance.newBuilder());

    builder.setObjectInstanceHandle(deleteObjectInstance.getObjectInstanceHandle());

    if (deleteObjectInstance.hasTag())
    {
      builder.setTag(deleteObjectInstance.getTag());
    }

    if (deleteObjectInstance.hasTime())
    {
      builder.setTime(deleteObjectInstance.getTime());
    }

    builder.setSentOrderType(deleteObjectInstance.getSentOrderType());
    builder.setReceivedOrderType(OrderTypes.convert(receivedOrderType));

    if (receivedOrderType == OrderType.TIMESTAMP)
    {
      assert deleteObjectInstance.hasMessageRetractionHandle();

      builder.setMessageRetractionHandle(deleteObjectInstance.getMessageRetractionHandle());
    }

    builder.setProducingFederateHandle(FederateHandles.convert(producingFederateHandle));
  }

  private RemoveObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, FederateHandle producingFederateHandle,
    OHLAProtos.OrderType sentOrderType)
  {
    super(FederateMessageProtos.RemoveObjectInstance.newBuilder());

    builder.setObjectInstanceHandle(ObjectInstanceHandles.convert(objectInstanceHandle));
    builder.setProducingFederateHandle(FederateHandles.convert(producingFederateHandle));
    builder.setSentOrderType(sentOrderType);
    builder.setReceivedOrderType(OHLAProtos.OrderType.RECEIVE);
  }

  public RemoveObjectInstance(CodedInputStream in)
    throws IOException
  {
    super(FederateMessageProtos.RemoveObjectInstance.newBuilder(), in);
  }

  public ObjectInstanceHandle getObjectInstanceHandle()
  {
    return ObjectInstanceHandles.convert(builder.getObjectInstanceHandle());
  }

  public byte[] getTag()
  {
    return builder.hasTag() ? builder.getTag().toByteArray() : null;
  }

  public OrderType getSentOrderType()
  {
    return OrderTypes.convert(builder.getSentOrderType());
  }

  public LogicalTime getTime()
  {
    return time;
  }

  public MessageRetractionHandle getMessageRetractionHandle()
  {
    return messageRetractionHandle;
  }

  public OrderType getReceivedOrderType()
  {
    return OrderTypes.convert(builder.getReceivedOrderType());
  }

  public FederateHandle getProducingFederateHandle()
  {
    return FederateHandles.convert(builder.getProducingFederateHandle());
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.REMOVE_OBJECT_INSTANCE;
  }

  @Override
  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    federate.fireRemoveObjectInstance(this);
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

    federate.removeObjectInstance(this);
  }
}
