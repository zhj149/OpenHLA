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

import java.util.Map;

import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.federation.FederationExecutionObjectInstance;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.LogicalTime;
import hla.rti1516.MessageRetractionHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.OrderType;
import hla.rti1516.RegionHandleSet;
import hla.rti1516.TransportationType;

public class UpdateAttributeValues
  implements FederationExecutionMessage
{
  protected final ObjectInstanceHandle objectInstanceHandle;
  protected final AttributeHandleValueMap attributeValues;
  protected final byte[] tag;
  protected final OrderType sentOrderType;
  protected final TransportationType transportationType;
  protected final Map<AttributeHandle, RegionHandleSet> attributeUpdateRegionHandles;
  protected final LogicalTime updateTime;
  protected final MessageRetractionHandle messageRetractionHandle;

  protected transient FederationExecutionObjectInstance objectInstance;

  public UpdateAttributeValues(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleValueMap attributeValues, byte[] tag,
    OrderType sentOrderType, TransportationType transportationType,
    Map<AttributeHandle, RegionHandleSet> attributeUpdateRegionHandles)
  {
    this(objectInstanceHandle, attributeValues, tag, sentOrderType,
         transportationType, attributeUpdateRegionHandles, null, null);
  }

  public UpdateAttributeValues(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleValueMap attributeValues, byte[] tag,
    OrderType sentOrderType, TransportationType transportationType,
    Map<AttributeHandle, RegionHandleSet> attributeUpdateRegionHandles,
    LogicalTime updateTime, MessageRetractionHandle messageRetractionHandle)
  {
    this.objectInstanceHandle = objectInstanceHandle;
    this.attributeValues = attributeValues;
    this.tag = tag;
    this.sentOrderType = sentOrderType;
    this.transportationType = transportationType;
    this.attributeUpdateRegionHandles = attributeUpdateRegionHandles;
    this.updateTime = updateTime;
    this.messageRetractionHandle = messageRetractionHandle;
  }

  public ObjectInstanceHandle getObjectInstanceHandle()
  {
    return objectInstanceHandle;
  }

  public AttributeHandleValueMap getAttributeValues()
  {
    return attributeValues;
  }

  public byte[] getTag()
  {
    return tag;
  }

  public OrderType getSentOrderType()
  {
    return sentOrderType;
  }

  public TransportationType getTransportationType()
  {
    return transportationType;
  }

  public Map<AttributeHandle, RegionHandleSet> getAttributeUpdateRegionHandles()
  {
    return attributeUpdateRegionHandles;
  }

  public LogicalTime getUpdateTime()
  {
    return updateTime;
  }

  public MessageRetractionHandle getMessageRetractionHandle()
  {
    return messageRetractionHandle;
  }

  public FederationExecutionObjectInstance getObjectInstance()
  {
    return objectInstance;
  }

  public void setObjectInstance(
    FederationExecutionObjectInstance objectInstance)
  {
    this.objectInstance = objectInstance;
  }

  public void execute(FederationExecution federationExecution,
                      FederateProxy federateProxy)
  {
    federationExecution.updateAttributeValues(federateProxy, this);
  }
}
