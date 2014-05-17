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

import net.sf.ohla.rti.util.LogicalTimes;
import net.sf.ohla.rti.util.MessageRetractionHandles;
import net.sf.ohla.rti.util.ObjectInstanceHandles;
import net.sf.ohla.rti.util.OrderTypes;
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.messages.proto.FederationExecutionMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;
import net.sf.ohla.rti.proto.OHLAProtos;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;

public class DeleteObjectInstance
  extends AbstractMessage<FederationExecutionMessageProtos.DeleteObjectInstance, FederationExecutionMessageProtos.DeleteObjectInstance.Builder>
  implements FederationExecutionMessage, TimeStampOrderedMessage
{
  private volatile OrderType sentOrderType;
  private volatile LogicalTime time;
  private volatile MessageRetractionHandle messageRetractionHandle;

  public DeleteObjectInstance(FederationExecutionMessageProtos.DeleteObjectInstance messageLite)
  {
    super(messageLite);
  }

  public DeleteObjectInstance(ObjectInstanceHandle objectInstanceHandle, byte[] tag)
  {
    this(objectInstanceHandle, tag, OHLAProtos.OrderType.RECEIVE);
  }

  public DeleteObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag, OrderType sentOrderType, LogicalTime time,
    MessageRetractionHandle messageRetractionHandle)
  {
    this(objectInstanceHandle, tag, OrderTypes.convert(sentOrderType));

    builder.setTime(LogicalTimes.convert(time));
    builder.setMessageRetractionHandle(MessageRetractionHandles.convert(messageRetractionHandle));
  }

  public DeleteObjectInstance(CodedInputStream in)
    throws IOException
  {
    super(FederationExecutionMessageProtos.DeleteObjectInstance.newBuilder(), in);
  }

  private DeleteObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag, OHLAProtos.OrderType sentOrderType)
  {
    super(FederationExecutionMessageProtos.DeleteObjectInstance.newBuilder());

    builder.setObjectInstanceHandle(ObjectInstanceHandles.convert(objectInstanceHandle));
    builder.setSentOrderType(sentOrderType);

    if (tag != null)
    {
      builder.setTag(ByteString.copyFrom(tag));
    }
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

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.DELETE_OBJECT_INSTANCE;
  }

  @Override
  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    if (builder.hasTime())
    {
      time = LogicalTimes.convert(federationExecution.getTimeManager().getLogicalTimeFactory(), builder.getTime());
    }

    federationExecution.deleteObjectInstance(federateProxy, this);
  }
}
