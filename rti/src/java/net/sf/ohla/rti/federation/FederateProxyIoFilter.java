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

import java.util.Iterator;
import java.util.Map;

import net.sf.ohla.rti.SubscriptionManager;
import net.sf.ohla.rti.fdd.InteractionClass;
import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.hla.rti1516.IEEE1516AttributeHandleValueMap;
import net.sf.ohla.rti.hla.rti1516.IEEE1516ParameterHandleValueMap;
import net.sf.ohla.rti.messages.FederationExecutionMessage;
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
import hla.rti1516.InteractionClassHandle;
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
    federationExecution.getFederatesLock().readLock().lock();
    try
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
      else if (writeRequest.getMessage() instanceof ReceiveInteraction)
      {
        ReceiveInteraction receiveInteraction =
          subscriptionManager.transform(
            (ReceiveInteraction) writeRequest.getMessage());

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
    finally
    {
      federationExecution.getFederatesLock().readLock().unlock();
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
        AttributeHandleValueMap trimmedAttributeValues =
          new IEEE1516AttributeHandleValueMap(
            updateAttributeValues.getAttributeValues());

        for (Iterator<AttributeHandle> i =
             trimmedAttributeValues.keySet().iterator(); i.hasNext();)
        {
          AttributeHandle attributeHandle = i.next();

          AttributeSubscription attributeSubscription =
            subscriptions.get(attributeHandle);
          if (attributeSubscription == null ||
              (!attributeSubscription.getSubscribedRegionHandles().isEmpty() &&
               !updateAttributeValues.getObjectInstance().regionsIntersect(
                 attributeSubscription.getSubscribedRegionHandles())))
          {
            i.remove();
          }
        }

        if (!trimmedAttributeValues.isEmpty())
        {
          reflectAttributeValues = new ReflectAttributeValues(
            updateAttributeValues.getObjectInstanceHandle(),
            trimmedAttributeValues,
            updateAttributeValues.getTag(),
            updateAttributeValues.getSentRegionHandles(),
            updateAttributeValues.getSentOrderType(),
            updateAttributeValues.getTransportationType(),
            updateAttributeValues.getUpdateTime(),
            updateAttributeValues.getMessageRetractionHandle());
        }
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

      InteractionClassSubscription subscription =
        getSubscribedInteractionClassSubscription(interactionClass);

      if (subscription == null)
      {
        receiveInteraction = null;
      }
      else if (!federationExecution.getRegionManager().intersect(
        subscription.getSubscribedRegionHandles(),
        receiveInteraction.getSentRegionHandles()))
      {
        receiveInteraction = null;
      }
      else
      {
        if (!subscription.getInteractionClassHandle().equals(
          interactionClassHandle))
        {
          ParameterHandleValueMap trimmedParameterValues =
            new IEEE1516ParameterHandleValueMap(
              receiveInteraction.getParameterValues());
          trimmedParameterValues.keySet().retainAll(
            interactionClass.getParameters().keySet());

          receiveInteraction = new ReceiveInteraction(
            interactionClass.getInteractionClassHandle(),
            trimmedParameterValues, receiveInteraction.getTag(),
            receiveInteraction.getSentOrderType(),
            receiveInteraction.getTransportationType(),
            receiveInteraction.getSendTime(),
            receiveInteraction.getMessageRetractionHandle(),
            receiveInteraction.getSentRegionHandles());
        }
      }

      return receiveInteraction;
    }
  }
}
