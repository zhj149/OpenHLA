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

import net.sf.ohla.rti.Protocol;
import net.sf.ohla.rti.federate.Callback;
import net.sf.ohla.rti.federate.Federate;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eMessageRetractionHandle;
import net.sf.ohla.rti.messages.FederateMessage;
import net.sf.ohla.rti.messages.MessageType;
import net.sf.ohla.rti.messages.ObjectInstanceMessage;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.exceptions.FederateInternalError;

public class RemoveObjectInstance
  extends ObjectInstanceMessage
  implements Callback, FederateMessage, FederateAmbassador.SupplementalRemoveInfo
{
  private final byte[] tag;
  private final OrderType sentOrderType;
  private final LogicalTime time;
  private final MessageRetractionHandle messageRetractionHandle;

  private Federate federate;

  private OrderType receivedOrderType;
  private FederateHandle producingFederateHandle;

  public RemoveObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag, OrderType sentOrderType, LogicalTime time,
    MessageRetractionHandle messageRetractionHandle)
  {
    super(MessageType.REMOVE_OBJECT_INSTANCE, objectInstanceHandle);

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

  public RemoveObjectInstance(ChannelBuffer buffer, LogicalTimeFactory logicalTimeFactory)
  {
    super(buffer);

    tag = Protocol.decodeBytes(buffer);
    sentOrderType = Protocol.decodeEnum(buffer, OrderType.values());
    time = Protocol.decodeTime(buffer, logicalTimeFactory);
    messageRetractionHandle = IEEE1516eMessageRetractionHandle.decode(buffer);
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

  public void setReceivedOrderType(OrderType receivedOrderType)
  {
    this.receivedOrderType = receivedOrderType;
  }

  public void setProducingFederateHandle(FederateHandle producingFederateHandle)
  {
    this.producingFederateHandle = producingFederateHandle;
  }

  public MessageType getType()
  {
    return MessageType.REMOVE_OBJECT_INSTANCE;
  }

  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    federate.removeObjectInstance(
      objectInstanceHandle, tag, sentOrderType, time, receivedOrderType, messageRetractionHandle, this);
  }

  public void execute(Federate federate)
  {
    this.federate = federate;

    federate.removeObjectInstance(this);
  }

  public boolean hasProducingFederate()
  {
    return producingFederateHandle != null;
  }

  public FederateHandle getProducingFederate()
  {
    return producingFederateHandle;
  }
}
