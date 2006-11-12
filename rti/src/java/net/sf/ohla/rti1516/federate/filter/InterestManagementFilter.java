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

package net.sf.ohla.rti1516.federate.filter;

import java.util.Map;

import net.sf.ohla.rti1516.fdd.InteractionClass;
import net.sf.ohla.rti1516.fdd.ObjectClass;
import net.sf.ohla.rti1516.federate.Federate;
import net.sf.ohla.rti1516.federate.SubscriptionManager;
import net.sf.ohla.rti1516.federate.callbacks.ReceiveInteraction;
import net.sf.ohla.rti1516.federate.callbacks.ReflectAttributeValues;
import net.sf.ohla.rti1516.OHLAAttributeHandleValueMap;
import net.sf.ohla.rti1516.OHLAParameterHandleValueMap;
import net.sf.ohla.rti1516.messages.SubscribeInteractionClass;
import net.sf.ohla.rti1516.messages.SubscribeObjectClassAttributes;
import net.sf.ohla.rti1516.messages.UnsubscribeInteractionClass;
import net.sf.ohla.rti1516.messages.UnsubscribeObjectClassAttributes;
import net.sf.ohla.rti1516.messages.RegionCreated;
import net.sf.ohla.rti1516.messages.RegionModificationsCommitted;
import net.sf.ohla.rti1516.messages.RegionDeleted;

import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ParameterHandleValueMap;

public class InterestManagementFilter
  extends IoFilterAdapter
{
  private static final String SUBSCRIPTION_MANAGER = "SubscriptionManager";

  protected Federate federate;

  public InterestManagementFilter(Federate federate)
  {
    this.federate = federate;
  }

  public void sessionCreated(NextFilter nextFilter, IoSession session)
    throws Exception
  {
    session.setAttribute(SUBSCRIPTION_MANAGER,
                         new InterestManagementFilterSubscriptionManager());

    nextFilter.sessionCreated(session);
  }

  public void messageReceived(NextFilter nextFilter, IoSession session,
                              Object message)
    throws Exception
  {
    if (message instanceof SubscribeObjectClassAttributes)
    {
      SubscribeObjectClassAttributes subscribeObjectClassAttributes =
        (SubscribeObjectClassAttributes) message;

      if (subscribeObjectClassAttributes.getAttributeHandles() != null)
      {
        getSubscriptionManager(session).subscribeObjectClassAttributes(
          subscribeObjectClassAttributes.getObjectClassHandle(),
          subscribeObjectClassAttributes.getAttributeHandles(),
          subscribeObjectClassAttributes.isPassive());

        // TODO: notify the PublicationManager that subsciption interests have changed
      }
      else if (subscribeObjectClassAttributes.getAttributesAndRegions() != null)
      {
        getSubscriptionManager(session).subscribeObjectClassAttributes(
          subscribeObjectClassAttributes.getObjectClassHandle(),
          subscribeObjectClassAttributes.getAttributesAndRegions(),
          subscribeObjectClassAttributes.isPassive());

        // TODO: notify the PublicationManager that subsciption interests have changed
      }
    }
    else if (message instanceof UnsubscribeObjectClassAttributes)
    {
      UnsubscribeObjectClassAttributes unsubscribeObjectClassAttributes =
        (UnsubscribeObjectClassAttributes) message;

      if (unsubscribeObjectClassAttributes.getAttributeHandles() != null)
      {
        getSubscriptionManager(session).unsubscribeObjectClassAttributes(
          unsubscribeObjectClassAttributes.getObjectClassHandle(),
          unsubscribeObjectClassAttributes.getAttributeHandles());

        // TODO: notify the PublicationManager that subsciption interests have changed
      }
      else if (unsubscribeObjectClassAttributes.getAttributesAndRegions() != null)
      {
        getSubscriptionManager(session).unsubscribeObjectClassAttributes(
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
        getSubscriptionManager(session).subscribeInteractionClass(
          subscribeInteractionClass.getInteractionClassHandle(),
          subscribeInteractionClass.isPassive());

        // TODO: notify the PublicationManager that subsciption interests have changed
      }
      else
      {
        getSubscriptionManager(session).subscribeInteractionClass(
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
        getSubscriptionManager(session).unsubscribeInteractionClass(
          unsubscribeInteractionClass.getInteractionClassHandle());

        // TODO: notify the PublicationManager that subsciption interests have changed
      }
      else
      {
        getSubscriptionManager(session).unsubscribeInteractionClass(
          unsubscribeInteractionClass.getInteractionClassHandle(),
          unsubscribeInteractionClass.getRegionHandles());

        // TODO: notify the PublicationManager that subsciption interests have changed
      }
    }
    else if (message instanceof RegionCreated)
    {
    }
    else if (message instanceof RegionModificationsCommitted)
    {
    }
    else if (message instanceof RegionDeleted)
    {
    }
    else
    {
      // pass on the message
      //
      nextFilter.messageReceived(session, message);
    }
  }

  public void filterWrite(NextFilter nextFilter, IoSession session,
                          WriteRequest writeRequest)
    throws Exception
  {
    if (writeRequest.getMessage() instanceof ReflectAttributeValues)
    {
      ReflectAttributeValues reflectAttributeValues =
        getSubscriptionManager(session).transform(
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
        getSubscriptionManager(session).transform(
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
      // pass on the message
      //
      nextFilter.filterWrite(session, writeRequest);
    }
  }

  protected InterestManagementFilterSubscriptionManager getSubscriptionManager(
    IoSession session)
  {
    return (InterestManagementFilterSubscriptionManager) session.getAttribute(
      SUBSCRIPTION_MANAGER);
  }

  protected class InterestManagementFilterSubscriptionManager
    extends SubscriptionManager
  {
    public ReflectAttributeValues transform(
      ReflectAttributeValues reflectAttributeValues)
    {
      ObjectInstanceHandle objectInstanceHandle =
        reflectAttributeValues.getObjectInstanceHandle();
      ObjectClassHandle objectClassHandle =
        reflectAttributeValues.getObjectClassHandle();

      ObjectClass objectClass =
        federate.getFDD().getObjectClasses().get(objectClassHandle);
      assert objectClass != null;

      Map<AttributeHandle, AttributeSubscription> subscriptions =
        getSubscribedAttributeSubscriptions(objectClass);

      if (subscriptions == null)
      {
        reflectAttributeValues = null;
      }
      else
      {
        AttributeHandleValueMap trimmedAttributeValues =
          new OHLAAttributeHandleValueMap(
            reflectAttributeValues.getAttributeValues());
        trimmedAttributeValues.keySet().retainAll(subscriptions.keySet());

        // TODO: DDM

        reflectAttributeValues = new ReflectAttributeValues(
          objectInstanceHandle, objectClassHandle, trimmedAttributeValues,
          reflectAttributeValues.getTag(),
          reflectAttributeValues.getSentOrderType(),
          reflectAttributeValues.getTransportationType());
      }

      return reflectAttributeValues;
    }

    public ReceiveInteraction transform(ReceiveInteraction receiveInteraction)
    {
      InteractionClassHandle interactionClassHandle =
        receiveInteraction.getInteractionClassHandle();

      InteractionClass interactionClass =
        federate.getFDD().getInteractionClasses().get(interactionClassHandle);
      assert interactionClass != null;

      interactionClass = getSubscribedInteractionClass(interactionClass);

      if (interactionClass == null)
      {
        receiveInteraction = null;
      }
      else
      {
        ParameterHandleValueMap trimmedParameterValues =
          new OHLAParameterHandleValueMap(
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
