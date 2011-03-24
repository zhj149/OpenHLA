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

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;

public class DeleteObjectInstance
  extends ObjectInstanceMessage
  implements FederationExecutionMessage
{
  private final byte[] tag;
  private final OrderType sentOrderType;
  private final LogicalTime time;
  private final MessageRetractionHandle messageRetractionHandle;

  public DeleteObjectInstance(ObjectInstanceHandle objectInstanceHandle, byte[] tag)
  {
    super(MessageType.DELETE_OBJECT_INSTANCE, objectInstanceHandle);

    this.tag = tag;

    sentOrderType = OrderType.RECEIVE;
    time = null;
    messageRetractionHandle = null;

    Protocol.encodeBytes(buffer, tag);
    Protocol.encodeEnum(buffer, sentOrderType);
    Protocol.encodeNullTime(buffer);
    IEEE1516eMessageRetractionHandle.encode(buffer, messageRetractionHandle);

    encodingFinished();
  }

  public DeleteObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag, OrderType sentOrderType, LogicalTime time,
    MessageRetractionHandle messageRetractionHandle)
  {
    super(MessageType.DELETE_OBJECT_INSTANCE, objectInstanceHandle);

    this.tag = tag;
    this.sentOrderType = sentOrderType;
    this.time = time;
    this.messageRetractionHandle = messageRetractionHandle;

    Protocol.encodeBytes(buffer, tag);
    Protocol.encodeEnum(buffer, sentOrderType);
    Protocol.encodeTime(buffer, time);
    IEEE1516eMessageRetractionHandle.encode(buffer, messageRetractionHandle);

    encodingFinished();
  }

  public DeleteObjectInstance(ChannelBuffer buffer, LogicalTimeFactory logicalTimeFactory)
  {
    super(buffer);

    tag = Protocol.decodeBytes(buffer);
    sentOrderType = Protocol.decodeEnum(buffer, OrderType.values());
    time = Protocol.decodeTime(buffer, logicalTimeFactory);
    messageRetractionHandle = IEEE1516eMessageRetractionHandle.decode(buffer);
  }

  public ObjectInstanceHandle getObjectInstanceHandle()
  {
    return objectInstanceHandle;
  }

  public byte[] getTag()
  {
    return tag;
  }

  public OrderType getSentOrderType()
  {
    return sentOrderType;
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
    return MessageType.DELETE_OBJECT_INSTANCE;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.deleteObjectInstance(federateProxy, this);
  }
}
