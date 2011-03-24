/*
 * Copyright (c) 2005-2010, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.federation;

import java.util.HashMap;
import java.util.Map;

import net.sf.ohla.rti.SubscriptionManager;
import net.sf.ohla.rti.fdd.InteractionClass;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleValueMapFactory;
import net.sf.ohla.rti.messages.SendInteraction;
import net.sf.ohla.rti.messages.UpdateAttributeValues;
import net.sf.ohla.rti.messages.callbacks.DiscoverObjectInstance;
import net.sf.ohla.rti.messages.callbacks.ReceiveInteraction;
import net.sf.ohla.rti.messages.callbacks.ReflectAttributeValues;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.DimensionHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RangeBounds;
import hla.rti1516e.RegionHandle;

public class FederateProxySubscriptionManager
  extends SubscriptionManager
{
  public DiscoverObjectInstance discoverObjectInstance(FederationExecutionObjectInstance objectInstance)
  {
    DiscoverObjectInstance discoverObjectInstance;

    ObjectClassSubscription objectClassSubscription =
      getSubscribedObjectClassSubscription(objectInstance.getObjectClass());
    if (objectClassSubscription == null)
    {
      discoverObjectInstance = null;
    }
    else
    {
      discoverObjectInstance = new DiscoverObjectInstance(
        objectInstance.getObjectInstanceHandle(), objectInstance.getObjectClass().getObjectClassHandle(),
        objectInstance.getObjectInstanceName(), objectInstance.getProducingFederateHandle());
    }

    return discoverObjectInstance;
  }

  public ReflectAttributeValues reflectAttributeValues(
    FederateProxy federateProxy, FederationExecutionObjectInstance objectInstance,
    UpdateAttributeValues updateAttributeValues)
  {
    ReflectAttributeValues reflectAttributeValues;

    ObjectClassSubscription objectClassSubscription =
      getSubscribedObjectClassSubscription(objectInstance.getObjectClass());
    if (objectClassSubscription == null)
    {
      reflectAttributeValues = null;
    }
    else
    {
      Map<RegionHandle, Map<DimensionHandle, RangeBounds>> sentRegions = null;

      AttributeHandleValueMap trimmedAttributeValues = null;

      for (Map.Entry<AttributeHandle, byte[]> entry : updateAttributeValues.getAttributeValues().entrySet())
      {
        AttributeHandle attributeHandle = entry.getKey();

        AttributeSubscription attributeSubscription = objectClassSubscription.getAttributeSubscription(attributeHandle);
        if (attributeSubscription != null)
        {
          if (federateProxy.isConveyRegionDesignatorSets() &&
              attributeSubscription.getSubscribedRegionHandles().size() > 0)
          {
            if (sentRegions == null)
            {
              sentRegions = new HashMap<RegionHandle, Map<DimensionHandle, RangeBounds>>();
            }

            // copy all the regions

            if (objectInstance.regionsIntersect(
              attributeHandle, federateProxy.getFederationExecution().getRegionManager(),
              attributeSubscription.getSubscribedRegionHandles(), sentRegions))
            {
              if (trimmedAttributeValues == null)
              {
                trimmedAttributeValues = IEEE1516eAttributeHandleValueMapFactory.INSTANCE.create(
                  updateAttributeValues.getAttributeValues().size());
              }
              trimmedAttributeValues.put(attributeHandle, entry.getValue());
            }
          }
          else
          {
            // just check for intersection

            if (objectInstance.regionsIntersect(
              attributeHandle, federateProxy.getFederationExecution().getRegionManager(),
              attributeSubscription.getSubscribedRegionHandles()))
            {
              if (trimmedAttributeValues == null)
              {
                trimmedAttributeValues = IEEE1516eAttributeHandleValueMapFactory.INSTANCE.create(
                  updateAttributeValues.getAttributeValues().size());
              }
              trimmedAttributeValues.put(attributeHandle, entry.getValue());
            }
          }
        }
      }

      if (trimmedAttributeValues == null)
      {
        reflectAttributeValues = null;
      }
      else
      {
        reflectAttributeValues = new ReflectAttributeValues(
          updateAttributeValues.getObjectInstanceHandle(), trimmedAttributeValues, updateAttributeValues.getTag(),
          updateAttributeValues.getSentOrderType(), updateAttributeValues.getTransportationTypeHandle(),
          updateAttributeValues.getTime(), updateAttributeValues.getMessageRetractionHandle(),
          federateProxy.getFederateHandle(), sentRegions);
      }
    }

    return reflectAttributeValues;
  }

  public ReceiveInteraction receiveInteraction(
    FederateProxy federateProxy, InteractionClass interactionClass, SendInteraction sendInteraction)
  {
    ReceiveInteraction receiveInteraction;

    InteractionClassSubscription interactionClassSubscription =
      getSubscribedInteractionClassSubscription(interactionClass);
    if (interactionClassSubscription == null)
    {
      receiveInteraction = null;
    }
    else
    {
      ParameterHandleValueMap trimmedParameterValues;
      Map<RegionHandle, Map<DimensionHandle, RangeBounds>> sentRegions;

      if (sendInteraction.getSentRegionHandles() == null)
      {
        sentRegions = null;

        if (interactionClassSubscription.getSubscribedRegionHandles().isEmpty())
        {
          trimmedParameterValues = interactionClassSubscription.trim(
            interactionClass, sendInteraction.getParameterValues());
        }
        else
        {
          trimmedParameterValues = null;
        }
      }
      else if (interactionClassSubscription.getSubscribedRegionHandles().isEmpty())
      {
        trimmedParameterValues = null;
        sentRegions = null;
      }
      else if (federateProxy.isConveyRegionDesignatorSets())
      {
        sentRegions = federateProxy.getFederationExecution().getRegionManager().intersects(
          interactionClassSubscription.getSubscribedRegionHandles(), sendInteraction.getSentRegionHandles(),
          interactionClassSubscription.getInteractionClass());
        if (sentRegions == null)
        {
          trimmedParameterValues = null;
        }
        else
        {
          trimmedParameterValues =
            interactionClassSubscription.trim(interactionClass, sendInteraction.getParameterValues());
        }
      }
      else
      {
        sentRegions = null;

        if (federateProxy.getFederationExecution().getRegionManager().intersectsOnly(
          interactionClassSubscription.getSubscribedRegionHandles(), sendInteraction.getSentRegionHandles(),
          interactionClassSubscription.getInteractionClass()))
        {
          trimmedParameterValues = null;
        }
        else
        {
          trimmedParameterValues =
            interactionClassSubscription.trim(interactionClass, sendInteraction.getParameterValues());
        }
      }

      if (trimmedParameterValues == null || trimmedParameterValues.isEmpty())
      {
        receiveInteraction = null;
      }
      else
      {
        receiveInteraction = new ReceiveInteraction(
          interactionClass.getInteractionClassHandle(), trimmedParameterValues, sendInteraction.getTag(),
          sendInteraction.getSentOrderType(), sendInteraction.getTransportationTypeHandle(), sendInteraction.getTime(),
          sendInteraction.getMessageRetractionHandle(), federateProxy.getFederateHandle(), sentRegions);
      }
    }

    return receiveInteraction;
  }
}
