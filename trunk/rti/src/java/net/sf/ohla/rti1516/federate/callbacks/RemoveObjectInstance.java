/*
 * Copyright (c) 2006, Michael Newcomb
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

package net.sf.ohla.rti1516.federate.callbacks;

import hla.rti1516.FederateAmbassador;
import hla.rti1516.FederateInternalError;
import hla.rti1516.InvalidLogicalTime;
import hla.rti1516.LogicalTime;
import hla.rti1516.MessageRetractionHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ObjectInstanceNotKnown;
import hla.rti1516.OrderType;

public class RemoveObjectInstance
  implements Callback
{
  protected ObjectInstanceHandle objectInstanceHandle;
  protected byte[] tag;
  protected OrderType sentOrderType;
  protected LogicalTime deleteTime;
  protected MessageRetractionHandle messageRetractionHandle;

  protected OrderType receivedOrderType;

  public RemoveObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                              byte[] tag, OrderType sentOrderType)
  {
    this.objectInstanceHandle = objectInstanceHandle;
    this.tag = tag;
    this.sentOrderType = sentOrderType;
  }

  public RemoveObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag,
    OrderType sentOrderType, LogicalTime deleteTime,
    MessageRetractionHandle messageRetractionHandle)
  {
    this(objectInstanceHandle, tag, sentOrderType);

    this.deleteTime = deleteTime;
    this.messageRetractionHandle = messageRetractionHandle;
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

  public LogicalTime getDeleteTime()
  {
    return deleteTime;
  }

  public MessageRetractionHandle getMessageRetractionHandle()
  {
    return messageRetractionHandle;
  }

  public OrderType getReceivedOrderType()
  {
    return receivedOrderType;
  }

  public void setReceivedOrderType(OrderType receivedOrderType)
  {
    this.receivedOrderType = receivedOrderType;
  }

  public void execute(FederateAmbassador federateAmbassador)
    throws ObjectInstanceNotKnown, InvalidLogicalTime, FederateInternalError
  {
    if (deleteTime == null)
    {
      federateAmbassador.removeObjectInstance(
        objectInstanceHandle, tag, sentOrderType);
    }
    else if (messageRetractionHandle == null)
    {
      federateAmbassador.removeObjectInstance(
        objectInstanceHandle, tag, sentOrderType, deleteTime,
        receivedOrderType);
    }
    else
    {
      federateAmbassador.removeObjectInstance(
        objectInstanceHandle, tag, sentOrderType, deleteTime,
        receivedOrderType, messageRetractionHandle);
    }
  }
}
