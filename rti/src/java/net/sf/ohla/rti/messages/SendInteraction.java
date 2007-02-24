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

import hla.rti1516.InteractionClassHandle;
import hla.rti1516.LogicalTime;
import hla.rti1516.MessageRetractionHandle;
import hla.rti1516.OrderType;
import hla.rti1516.ParameterHandleValueMap;
import hla.rti1516.RegionHandleSet;
import hla.rti1516.TransportationType;

public class SendInteraction
  implements FederationExecutionMessage
{
  protected InteractionClassHandle interactionClassHandle;
  protected ParameterHandleValueMap parameterValues;
  protected byte[] tag;
  protected OrderType sentOrderType;
  protected TransportationType transportationType;
  protected LogicalTime sendTime;
  protected MessageRetractionHandle messageRetractionHandle;
  protected RegionHandleSet sentRegionHandles;

  public SendInteraction(InteractionClassHandle interactionClassHandle,
                         ParameterHandleValueMap parameterValues, byte[] tag,
                         OrderType sentOrderType,
                         TransportationType transportationType)
  {
    this.interactionClassHandle = interactionClassHandle;
    this.parameterValues = parameterValues;
    this.tag = tag;
    this.sentOrderType = sentOrderType;
    this.transportationType = transportationType;
  }

  public SendInteraction(InteractionClassHandle interactionClassHandle,
                         ParameterHandleValueMap parameterValues, byte[] tag,
                         OrderType sentOrderType,
                         TransportationType transportationType,
                         LogicalTime sendTime,
                         MessageRetractionHandle messageRetractionHandle)
  {
    this(interactionClassHandle, parameterValues, tag, sentOrderType,
         transportationType);

    this.sendTime = sendTime;
    this.messageRetractionHandle = messageRetractionHandle;
  }

  public SendInteraction(InteractionClassHandle interactionClassHandle,
                         ParameterHandleValueMap parameterValues, byte[] tag,
                         OrderType sentOrderType,
                         TransportationType transportationType,
                         RegionHandleSet sentRegionHandles)
  {
    this(interactionClassHandle, parameterValues, tag, sentOrderType,
         transportationType);

    this.sentRegionHandles = sentRegionHandles;
  }

  public SendInteraction(
    InteractionClassHandle interactionClassHandle,
    ParameterHandleValueMap parameterValues, byte[] tag,
    OrderType sentOrderType, TransportationType transportationType,
    LogicalTime sendTime, MessageRetractionHandle messageRetractionHandle,
    RegionHandleSet regionHandles)
  {
    this(interactionClassHandle, parameterValues, tag, sentOrderType,
         transportationType, sendTime, messageRetractionHandle);

    this.sentRegionHandles = regionHandles;
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

  public void execute(FederationExecution federationExecution,
                      FederateProxy federateProxy)
  {
    federationExecution.sendInteraction(federateProxy, this);
  }
}
