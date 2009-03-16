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

package net.sf.ohla.rti.federation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.ohla.rti.SubscriptionManager;
import net.sf.ohla.rti.fdd.InteractionClass;
import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.hla.rti1516.IEEE1516AttributeHandleValueMap;
import net.sf.ohla.rti.hla.rti1516.IEEE1516RegionHandleSet;
import net.sf.ohla.rti.messages.FederationExecutionMessage;
import net.sf.ohla.rti.messages.SendInteraction;
import net.sf.ohla.rti.messages.SubscribeInteractionClass;
import net.sf.ohla.rti.messages.SubscribeObjectClassAttributes;
import net.sf.ohla.rti.messages.UnsubscribeInteractionClass;
import net.sf.ohla.rti.messages.UnsubscribeObjectClassAttributes;
import net.sf.ohla.rti.messages.UpdateAttributeValues;
import net.sf.ohla.rti.messages.callbacks.DiscoverObjectInstance;
import net.sf.ohla.rti.messages.callbacks.ReceiveInteraction;
import net.sf.ohla.rti.messages.callbacks.ReflectAttributeValues;

import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.ParameterHandleValueMap;
import hla.rti1516.RegionHandleSet;

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
    if (writeRequest.getMessage() instanceof UpdateAttributeValues)
    {
      ReflectAttributeValues reflectAttributeValues =
        subscriptionManager.transform(
          (UpdateAttributeValues) writeRequest.getMessage());

      if (reflectAttributeValues == null)
      {
        writeRequest.getFuture().setWritten(true);
      }
      else
      {
        writeRequest = reflectAttributeValues == writeRequest.getMessage() ?
          writeRequest :
          new WriteRequest(reflectAttributeValues, writeRequest.getFuture());
        nextFilter.filterWrite(session, writeRequest);
      }
    }
    else if (writeRequest.getMessage() instanceof SendInteraction)
    {
      ReceiveInteraction receiveInteraction =
        subscriptionManager.transform(
          (SendInteraction) writeRequest.getMessage());

      if (receiveInteraction == null)
      {
        writeRequest.getFuture().setWritten(true);
      }
      else
      {
        writeRequest = receiveInteraction == writeRequest.getMessage() ?
          writeRequest :
          new WriteRequest(receiveInteraction, writeRequest.getFuture());
        nextFilter.filterWrite(session, writeRequest);
      }
    }
    else if (writeRequest.getMessage() instanceof DiscoverObjectInstance)
    {
      DiscoverObjectInstance discoverObjectInstance =
        subscriptionManager.transform(
          (DiscoverObjectInstance) writeRequest.getMessage());

      if (discoverObjectInstance == null)
      {
        writeRequest.getFuture().setWritten(true);
      }
      else
      {
        writeRequest = discoverObjectInstance == writeRequest.getMessage() ?
          writeRequest :
          new WriteRequest(discoverObjectInstance, writeRequest.getFuture());
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
    public DiscoverObjectInstance transform(
      DiscoverObjectInstance discoverObjectInstance)
    {
      ObjectClass subscribedObjectClass =
        getSubscribedObjectClass(discoverObjectInstance.getObjectClass());
      if (subscribedObjectClass == null)
      {
        discoverObjectInstance = null;
      }
      else if (!subscribedObjectClass.equals(
        discoverObjectInstance.getObjectClass()))
      {
        discoverObjectInstance = new DiscoverObjectInstance(
          discoverObjectInstance.getObjectInstanceHandle(),
          subscribedObjectClass, discoverObjectInstance.getName());
      }

      return discoverObjectInstance;
    }

    public ReflectAttributeValues transform(
      UpdateAttributeValues updateAttributeValues)
    {
      ReflectAttributeValues reflectAttributeValues = null;

      Map<AttributeHandle, AttributeSubscription> subscriptions =
        getSubscribedAttributeSubscriptions(
          updateAttributeValues.getObjectInstance().getObjectClass());
      if (subscriptions != null)
      {
        AttributeHandleValueMap attributeValues =
          new IEEE1516AttributeHandleValueMap(
            updateAttributeValues.getAttributeValues());

        for (Iterator<AttributeHandle> i =
             attributeValues.keySet().iterator(); i.hasNext();)
        {
          AttributeHandle attributeHandle = i.next();

          AttributeSubscription attributeSubscription =
            subscriptions.get(attributeHandle);
          if (attributeSubscription == null ||
              !updateAttributeValues.getObjectInstance().regionsIntersect(
                attributeHandle, federationExecution.getRegionManager(),
                attributeSubscription.getSubscribedRegionHandles()))
          {
            i.remove();
          }
        }

        if (!attributeValues.isEmpty())
        {
          RegionHandleSet sentRegionHandles = null;
          if (federationExecution.isConveyRegionDesignatorSets())
          {
            sentRegionHandles = new IEEE1516RegionHandleSet();
            Map<AttributeHandle, RegionHandleSet> attributeUpdateRegionHandles =
              new HashMap<AttributeHandle, RegionHandleSet>(
                updateAttributeValues.getAttributeUpdateRegionHandles());
            attributeUpdateRegionHandles.keySet().retainAll(
              attributeValues.keySet());
            for (RegionHandleSet regionHandles :
              attributeUpdateRegionHandles.values())
            {
              sentRegionHandles.addAll(regionHandles);
            }
          }

          reflectAttributeValues = new ReflectAttributeValues(
            updateAttributeValues.getObjectInstanceHandle(),
            attributeValues, updateAttributeValues.getTag(), sentRegionHandles,
            updateAttributeValues.getSentOrderType(),
            updateAttributeValues.getTransportationType(),
            updateAttributeValues.getUpdateTime(),
            updateAttributeValues.getMessageRetractionHandle());
        }
      }

      return reflectAttributeValues;
    }

    public ReceiveInteraction transform(SendInteraction sendInteraction)
    {
      ReceiveInteraction receiveInteraction = null;

      InteractionClass interactionClass = sendInteraction.getInteractionClass();

      InteractionClassSubscription subscription =
        getSubscribedInteractionClassSubscription(interactionClass);

      if (subscription != null &&
          ((subscription.getSubscribedRegionHandles().isEmpty() &&
            (sendInteraction.getSentRegionHandles() == null ||
             sendInteraction.getSentRegionHandles().isEmpty())) ||
           (federationExecution.getRegionManager().intersects(
             subscription.getSubscribedRegionHandles(),
             sendInteraction.getSentRegionHandles(),
             subscription.getInteractionClassHandle()))))
      {
        ParameterHandleValueMap trimmedParameterValues = subscription.trim(
          interactionClass, sendInteraction.getParameterValues());

        if (trimmedParameterValues.size() > 0)
        {
          receiveInteraction = new ReceiveInteraction(
            interactionClass.getInteractionClassHandle(),
            trimmedParameterValues, sendInteraction.getTag(),
            sendInteraction.getSentOrderType(),
            sendInteraction.getTransportationType(),
            sendInteraction.getSendTime(),
            sendInteraction.getMessageRetractionHandle(),
            sendInteraction.getSentRegionHandles());
        }
      }

      return receiveInteraction;
    }
  }
}
