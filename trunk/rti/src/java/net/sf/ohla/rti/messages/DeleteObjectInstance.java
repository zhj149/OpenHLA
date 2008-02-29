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

import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;

import hla.rti1516.LogicalTime;
import hla.rti1516.MessageRetractionHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.OrderType;

public class DeleteObjectInstance
  implements FederationExecutionMessage
{
  protected ObjectInstanceHandle objectInstanceHandle;
  protected byte[] tag;
  protected OrderType sentOrderType;
  protected LogicalTime deleteTime;
  protected MessageRetractionHandle messageRetractionHandle;

  public DeleteObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                              byte[] tag, OrderType sentOrderType)
  {
    this.objectInstanceHandle = objectInstanceHandle;
    this.tag = tag;
    this.sentOrderType = sentOrderType;
  }

  public DeleteObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                              byte[] tag, OrderType sentOrderType,
                              LogicalTime deleteTime,
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

  public void execute(FederationExecution federationExecution,
                      FederateProxy federateProxy)
  {
    federationExecution.deleteObjectInstance(federateProxy, this);
  }
}