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

import hla.rti1516.FederateAmbassador;
import hla.rti1516.FederateInternalError;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.InteractionClassNotRecognized;
import hla.rti1516.InteractionClassNotSubscribed;
import hla.rti1516.InteractionParameterNotRecognized;
import hla.rti1516.InvalidLogicalTime;
import hla.rti1516.LogicalTime;
import hla.rti1516.MessageRetractionHandle;
import hla.rti1516.OrderType;
import hla.rti1516.ParameterHandleValueMap;
import hla.rti1516.RegionHandleSet;
import hla.rti1516.TransportationType;

public class ReceiveInteraction
  implements Callback
{
  protected InteractionClassHandle interactionClassHandle;
  protected ParameterHandleValueMap parameterValues;
  protected byte[] tag;
  protected OrderType sentOrderType;
  protected TransportationType transportationType;
  protected LogicalTime sendTime;
  protected MessageRetractionHandle messageRetractionHandle;
  protected RegionHandleSet sentRegionHandles;

  protected transient OrderType receivedOrderType;

  public ReceiveInteraction(InteractionClassHandle interactionClassHandle,
                            ParameterHandleValueMap parameterValues, byte[] tag,
                            OrderType sentOrderType,
                            TransportationType transportationType,
                            LogicalTime sendTime,
                            MessageRetractionHandle messageRetractionHandle,
                            RegionHandleSet sentRegionHandles)
  {
    this.interactionClassHandle = interactionClassHandle;
    this.parameterValues = parameterValues;
    this.tag = tag;
    this.sentOrderType = sentOrderType;
    this.transportationType = transportationType;
    this.sendTime = sendTime;
    this.messageRetractionHandle = messageRetractionHandle;
    this.sentRegionHandles = sentRegionHandles;
  }

  public InteractionClassHandle getInteractionClassHandle()
  {
    return interactionClassHandle;
  }

  public ParameterHandleValueMap getParameterValues()
  {
    return parameterValues;
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

  public LogicalTime getSendTime()
  {
    return sendTime;
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
    throws InteractionClassNotRecognized, InteractionClassNotSubscribed,
           InteractionParameterNotRecognized, InvalidLogicalTime,
           FederateInternalError
  {
    if (sendTime == null)
    {
      if (sentRegionHandles == null)
      {
        federateAmbassador.receiveInteraction(
          interactionClassHandle, parameterValues, tag, sentOrderType,
          transportationType);
      }
      else
      {
        federateAmbassador.receiveInteraction(
          interactionClassHandle, parameterValues, tag, sentOrderType,
          transportationType, sentRegionHandles);
      }
    }
    else if (messageRetractionHandle == null)
    {
      if (sentRegionHandles == null)
      {
        federateAmbassador.receiveInteraction(
          interactionClassHandle, parameterValues, tag, sentOrderType,
          transportationType, sendTime, receivedOrderType);
      }
      else
      {
        federateAmbassador.receiveInteraction(
          interactionClassHandle, parameterValues, tag, sentOrderType,
          transportationType, sendTime, receivedOrderType,
          sentRegionHandles);
      }
    }
    else if (sentRegionHandles == null)
    {
      federateAmbassador.receiveInteraction(
        interactionClassHandle, parameterValues, tag, sentOrderType,
        transportationType, sendTime, receivedOrderType,
        messageRetractionHandle);
    }
    else
    {
      federateAmbassador.receiveInteraction(
        interactionClassHandle, parameterValues, tag, sentOrderType,
        transportationType, sendTime, receivedOrderType,
        messageRetractionHandle, sentRegionHandles);
    }
  }
}
