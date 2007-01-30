/*
 * Copyright (c) 2007, Michael Newcomb
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

package net.sf.ohla.rti1516.messages;

import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.LogicalTime;
import hla.rti1516.MessageRetractionHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.OrderType;
import hla.rti1516.RegionHandleSet;
import hla.rti1516.TransportationType;

public class UpdateAttributeValues
  implements Message
{
  protected ObjectInstanceHandle objectInstanceHandle;
  protected AttributeHandleValueMap attributeValues;
  protected byte[] tag;
  protected RegionHandleSet sentRegionHandles;
  protected OrderType sentOrderType;
  protected TransportationType transportationType;
  protected LogicalTime updateTime;
  protected MessageRetractionHandle messageRetractionHandle;

  public UpdateAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                               AttributeHandleValueMap attributeValues,
                               byte[] tag, RegionHandleSet sentRegionHandles,
                               OrderType sentOrderType,
                               TransportationType transportationType)
  {
    this.objectInstanceHandle = objectInstanceHandle;
    this.attributeValues = attributeValues;
    this.tag = tag;
    this.sentRegionHandles = sentRegionHandles;
    this.sentOrderType = sentOrderType;
    this.transportationType = transportationType;
  }


  public UpdateAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                               AttributeHandleValueMap attributeValues,
                               byte[] tag,
                               RegionHandleSet sentRegionHandles, OrderType sentOrderType,
                               TransportationType transportationType,
                               LogicalTime updateTime,
                               MessageRetractionHandle messageRetractionHandle)
  {
    this(objectInstanceHandle, attributeValues, tag, sentRegionHandles,
         sentOrderType, transportationType);

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

  public RegionHandleSet getSentRegionHandles()
  {
    return sentRegionHandles;
  }

  public OrderType getSentOrderType()
  {
    return sentOrderType;
  }

  public TransportationType getTransportationType()
  {
    return transportationType;
  }

  public LogicalTime getUpdateTime()
  {
    return updateTime;
  }

  public MessageRetractionHandle getMessageRetractionHandle()
  {
    return messageRetractionHandle;
  }
}
