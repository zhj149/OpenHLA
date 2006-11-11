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

import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.AttributeNotRecognized;
import hla.rti1516.AttributeNotSubscribed;
import hla.rti1516.FederateAmbassador;
import hla.rti1516.FederateInternalError;
import hla.rti1516.InvalidLogicalTime;
import hla.rti1516.LogicalTime;
import hla.rti1516.MessageRetractionHandle;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ObjectInstanceNotKnown;
import hla.rti1516.OrderType;
import hla.rti1516.RegionHandleSet;
import hla.rti1516.TransportationType;

public class ReflectAttributeValues
  implements Callback
{
  protected ObjectInstanceHandle objectInstanceHandle;
  protected ObjectClassHandle objectClassHandle;
  protected AttributeHandleValueMap attributeValues;
  protected byte[] tag;
  protected OrderType sentOrderType;
  protected TransportationType transportationType;
  protected LogicalTime updateTime;
  protected MessageRetractionHandle messageRetractionHandle;
  protected RegionHandleSet sentRegionHandles;

  protected transient OrderType receivedOrderType;

  public ReflectAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                                ObjectClassHandle objectClassHandle,
                                AttributeHandleValueMap attributeValues,
                                byte[] tag, OrderType sentOrderType,
                                TransportationType transportationType)
  {
    this.objectInstanceHandle = objectInstanceHandle;
    this.objectClassHandle = objectClassHandle;
    this.attributeValues = attributeValues;
    this.tag = tag;
    this.sentOrderType = sentOrderType;
    this.transportationType = transportationType;
  }

  public ReflectAttributeValues(
    ObjectInstanceHandle objectInstanceHandle,
    ObjectClassHandle objectClassHandle,
    AttributeHandleValueMap attributeValues, byte[] tag,
    OrderType sentOrderType, TransportationType transportationType,
    LogicalTime updateTime, MessageRetractionHandle messageRetractionHandle,
    RegionHandleSet regionHandles)
  {
    this(objectInstanceHandle, objectClassHandle, attributeValues, tag,
         sentOrderType, transportationType);

    this.updateTime = updateTime;
    this.messageRetractionHandle = messageRetractionHandle;
    this.sentRegionHandles = regionHandles;
  }

  public ObjectInstanceHandle getObjectInstanceHandle()
  {
    return objectInstanceHandle;
  }

  public ObjectClassHandle getObjectClassHandle()
  {
    return objectClassHandle;
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

  public LogicalTime getUpdateTime()
  {
    return updateTime;
  }

  public MessageRetractionHandle getMessageRetractionHandle()
  {
    return messageRetractionHandle;
  }

  public RegionHandleSet getSentRegionHandles()
  {
    return sentRegionHandles;
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
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, InvalidLogicalTime, FederateInternalError
  {
    if (updateTime == null)
    {
      if (sentRegionHandles == null)
      {
        federateAmbassador.reflectAttributeValues(
          objectInstanceHandle, attributeValues, tag, sentOrderType,
          transportationType);
      }
      else
      {
        federateAmbassador.reflectAttributeValues(
          objectInstanceHandle, attributeValues, tag, sentOrderType,
          transportationType, sentRegionHandles);
      }
    }
    else if (messageRetractionHandle == null)
    {
      if (sentRegionHandles == null)
      {
        federateAmbassador.reflectAttributeValues(
          objectInstanceHandle, attributeValues, tag, sentOrderType,
          transportationType, updateTime, receivedOrderType);
      }
      else
      {
        federateAmbassador.reflectAttributeValues(
          objectInstanceHandle, attributeValues, tag, sentOrderType,
          transportationType, updateTime, receivedOrderType, sentRegionHandles);
      }
    }
    else if (sentRegionHandles == null)
    {
      federateAmbassador.reflectAttributeValues(
        objectInstanceHandle, attributeValues, tag, sentOrderType,
        transportationType, updateTime, receivedOrderType,
        messageRetractionHandle);
    }
    else
    {
      federateAmbassador.reflectAttributeValues(
        objectInstanceHandle, attributeValues, tag, sentOrderType,
        transportationType, updateTime, receivedOrderType,
        messageRetractionHandle, sentRegionHandles);
    }
  }

  public String toString()
  {
    return String.format("%s - %s", objectInstanceHandle, attributeValues);
  }
}
