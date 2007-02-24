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

package net.sf.ohla.rti.federation;

import java.util.Map;

import net.sf.ohla.rti.SubscriptionManager;
import net.sf.ohla.rti.fdd.InteractionClass;
import net.sf.ohla.rti.hla.rti1516.IEEE1516AttributeHandleValueMap;
import net.sf.ohla.rti.hla.rti1516.IEEE1516ParameterHandleValueMap;
import net.sf.ohla.rti.messages.FederationExecutionMessage;
import net.sf.ohla.rti.messages.SubscribeInteractionClass;
import net.sf.ohla.rti.messages.SubscribeObjectClassAttributes;
import net.sf.ohla.rti.messages.UnsubscribeInteractionClass;
import net.sf.ohla.rti.messages.UnsubscribeObjectClassAttributes;
import net.sf.ohla.rti.messages.callbacks.ReceiveInteraction;
import net.sf.ohla.rti.messages.callbacks.ReflectAttributeValues;

import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ParameterHandleValueMap;

public class FederateProxyIoFilter
  extends IoFilterAdapter
{
  protected final FederateProxy federateProxy;
  protected final FederationExecution federationExecution;

  protected final FederateIoFilterSubscriptionManager subscriptionManager =
    new FederateIoFilterSubscriptionManager();

  public FederateProxyIoFilter(FederateProxy federateProxy,
                          FederationExecution federationExecution)
  {
    this.federateProxy = federateProxy;
    this.federationExecution = federationExecution;
  }

  @Override
  public void sessionClosed(NextFilter nextFilter, IoSession session)
    throws Exception
  {
    super.sessionClosed(nextFilter, session);
  }

  @Override
  public void messageReceived(NextFilter nextFilter, IoSession session,
                              Object message)
    throws Exception
  {
    if (message instanceof FederationExecutionMessage)
    {
      ((FederationExecutionMessage) message).execute(
        federationExecution, federateProxy);
    }
    else if (message instanceof SubscribeObjectClassAttributes)
    {
      SubscribeObjectClassAttributes subscribeObjectClassAttributes =
        (SubscribeObjectClassAttributes) message;

      if (subscribeObjectClassAttributes.getAttributeHandles() != null)
      {
        subscriptionManager.subscribeObjectClassAttributes(
          subscribeObjectClassAttributes.getObjectClassHandle(),
          subscribeObjectClassAttributes.getAttributeHandles(),
          subscribeObjectClassAttributes.isPassive());

        // TODO: notify the PublicationManager that subsciption interests have changed
      }
      else if (subscribeObjectClassAttributes.getAttributesAndRegions() != null)
      {
        subscriptionManager.subscribeObjectClassAttributes(
          subscribeObjectClassAttributes.getObjectClassHandle(),
          subscribeObjectClassAttributes.getAttributesAndRegions(),
          subscribeObjectClassAttributes.isPassive());

        // TODO: notify the PublicationManager that subsciption interests have changed
      }

      federationExecution.subscribeObjectClassAttributes(
        federateProxy, subscribeObjectClassAttributes);
    }
    else if (message instanceof UnsubscribeObjectClassAttributes)
    {
      UnsubscribeObjectClassAttributes unsubscribeObjectClassAttributes =
        (UnsubscribeObjectClassAttributes) message;

      if (unsubscribeObjectClassAttributes.getAttributeHandles() != null)
      {
        subscriptionManager.unsubscribeObjectClassAttributes(
          unsubscribeObjectClassAttributes.getObjectClassHandle(),
          unsubscribeObjectClassAttributes.getAttributeHandles());

        // TODO: notify the PublicationManager that subsciption interests have changed
      }
      else
      if (unsubscribeObjectClassAttributes.getAttributesAndRegions() != null)
      {
        subscriptionManager.unsubscribeObjectClassAttributes(
          unsubscribeObjectClassAttributes.getObjectClassHandle(),
          unsubscribeObjectClassAttributes.getAttributesAndRegions());

        // TODO: notify the PublicationManager that subsciption interests have changed
      }
    }
    else if (message instanceof SubscribeInteractionClass)
    {
      SubscribeInteractionClass subscribeInteractionClass =
        (SubscribeInteractionClass) message;

      if (subscribeInteractionClass.getRegionHandles() == null)
      {
        subscriptionManager.subscribeInteractionClass(
          subscribeInteractionClass.getInteractionClassHandle(),
          subscribeInteractionClass.isPassive());

        // TODO: notify the PublicationManager that subsciption interests have changed
      }
      else
      {
        subscriptionManager.subscribeInteractionClass(
          subscribeInteractionClass.getInteractionClassHandle(),
          subscribeInteractionClass.getRegionHandles(),
          subscribeInteractionClass.isPassive());

        // TODO: notify the PublicationManager that subsciption interests have changed
      }
    }
    else if (message instanceof UnsubscribeInteractionClass)
    {
      UnsubscribeInteractionClass unsubscribeInteractionClass =
        (UnsubscribeInteractionClass) message;

      if (unsubscribeInteractionClass.getRegionHandles() == null)
      {
        subscriptionManager.unsubscribeInteractionClass(
          unsubscribeInteractionClass.getInteractionClassHandle());

        // TODO: notify the PublicationManager that subsciption interests have changed
      }
      else
      {
        subscriptionManager.unsubscribeInteractionClass(
          unsubscribeInteractionClass.getInteractionClassHandle(),
          unsubscribeInteractionClass.getRegionHandles());

        // TODO: notify the PublicationManager that subsciption interests have changed
      }
    }
    else
    {
      // pass on to the next filter
      //
      nextFilter.messageReceived(session, message);
    }
  }

  @Override
  public void filterWrite(NextFilter nextFilter, IoSession session,
                          WriteRequest writeRequest)
    throws Exception
  {
    if (writeRequest.getMessage() instanceof ReflectAttributeValues)
    {
      ReflectAttributeValues reflectAttributeValues =
        subscriptionManager.transform(
          (ReflectAttributeValues) writeRequest.getMessage());

      if (reflectAttributeValues != null)
      {
        writeRequest = reflectAttributeValues == writeRequest.getMessage() ?
          writeRequest : new WriteRequest(reflectAttributeValues);
        nextFilter.filterWrite(session, writeRequest);
      }
    }
    else if (writeRequest.getMessage() instanceof ReceiveInteraction)
    {
      ReceiveInteraction receiveInteraction =
        subscriptionManager.transform(
          (ReceiveInteraction) writeRequest.getMessage());

      if (receiveInteraction != null)
      {
        writeRequest = receiveInteraction == writeRequest.getMessage() ?
          writeRequest : new WriteRequest(receiveInteraction);
        nextFilter.filterWrite(session, writeRequest);
      }
    }
    else
    {
      // pass on to the next filter
      //
      nextFilter.filterWrite(session, writeRequest);
    }
  }

  protected class FederateIoFilterSubscriptionManager
    extends SubscriptionManager
  {
    public ReflectAttributeValues transform(
      ReflectAttributeValues reflectAttributeValues)
    {
      ObjectInstanceHandle objectInstanceHandle =
        reflectAttributeValues.getObjectInstanceHandle();

      Map<AttributeHandle, AttributeSubscription> subscriptions =
        getSubscribedAttributeSubscriptions(
          reflectAttributeValues.getObjectClass());
      if (subscriptions == null)
      {
        reflectAttributeValues = null;
      }
      else
      {
        AttributeHandleValueMap trimmedAttributeValues =
          new IEEE1516AttributeHandleValueMap(
            reflectAttributeValues.getAttributeValues());
        trimmedAttributeValues.keySet().retainAll(subscriptions.keySet());

        // TODO: DDM

        reflectAttributeValues = new ReflectAttributeValues(
          objectInstanceHandle, trimmedAttributeValues,
          reflectAttributeValues.getTag(),
          reflectAttributeValues.getSentRegionHandles(),
          reflectAttributeValues.getSentOrderType(),
          reflectAttributeValues.getTransportationType(),
          reflectAttributeValues.getUpdateTime(),
          reflectAttributeValues.getMessageRetractionHandle());
      }

      return reflectAttributeValues;
    }

    public ReceiveInteraction transform(ReceiveInteraction receiveInteraction)
    {
      InteractionClassHandle interactionClassHandle =
        receiveInteraction.getInteractionClassHandle();

      InteractionClass interactionClass =
        federationExecution.getFDD().getInteractionClasses().get(interactionClassHandle);
      assert interactionClass != null;

      interactionClass = getSubscribedInteractionClass(interactionClass);

      if (interactionClass == null)
      {
        receiveInteraction = null;
      }
      else
      {
        ParameterHandleValueMap trimmedParameterValues =
          new IEEE1516ParameterHandleValueMap(
            receiveInteraction.getParameterValues());
        trimmedParameterValues.keySet().retainAll(
          interactionClass.getParameters().keySet());

        // TODO: DDM

        receiveInteraction = new ReceiveInteraction(
          interactionClass.getInteractionClassHandle(),
          trimmedParameterValues, receiveInteraction.getTag(),
          receiveInteraction.getSentOrderType(),
          receiveInteraction.getTransportationType(),
          receiveInteraction.getSendTime(),
          receiveInteraction.getMessageRetractionHandle(),
          receiveInteraction.getSentRegionHandles());
      }

      return receiveInteraction;
    }
  }
}
