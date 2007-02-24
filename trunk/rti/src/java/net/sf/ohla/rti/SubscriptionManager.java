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

package net.sf.ohla.rti;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.ohla.rti.fdd.InteractionClass;
import net.sf.ohla.rti.fdd.ObjectClass;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.AttributeRegionAssociation;
import hla.rti1516.AttributeSetRegionSetPairList;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.RegionHandle;
import hla.rti1516.RegionHandleSet;

public class SubscriptionManager
{
  protected Map<ObjectClassHandle, Map<AttributeHandle, AttributeSubscription>>
    subscribedObjectClasses =
    new HashMap<ObjectClassHandle, Map<AttributeHandle, AttributeSubscription>>();

  protected Map<InteractionClassHandle, InteractionClassSubscription>
    subscribedInteractionClasses =
    new HashMap<InteractionClassHandle, InteractionClassSubscription>();

  public boolean isObjectClassSubscribed(ObjectClassHandle objectClassHandle)
  {
    return subscribedObjectClasses.containsKey(objectClassHandle);
  }

  public void subscribeObjectClassAttributes(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles,
    boolean passive)
  {
    Map<AttributeHandle, AttributeSubscription> attributeSubscriptions =
      getAttributeSubscriptions(objectClassHandle, true);

    for (AttributeHandle attributeHandle : attributeHandles)
    {
      AttributeSubscription attributeSubscription =
        attributeSubscriptions.get(attributeHandle);
      if (attributeSubscription == null)
      {
        attributeSubscriptions.put(
          attributeHandle, new AttributeSubscription(attributeHandle, passive));
      }
      else
      {
        attributeSubscription.subscribe(passive);
      }
    }
  }

  public void subscribeObjectClassAttributes(
    ObjectClassHandle objectClassHandle,
    AttributeSetRegionSetPairList attributesAndRegions, boolean passive)
  {
    Map<AttributeHandle, AttributeSubscription> attributeSubscriptions =
      getAttributeSubscriptions(objectClassHandle, true);

    for (AttributeRegionAssociation attributeRegionAssociation : attributesAndRegions)
    {
      for (AttributeHandle attributeHandle : attributeRegionAssociation.attributes)
      {
        AttributeSubscription attributeSubscription =
          attributeSubscriptions.get(attributeHandle);
        if (attributeSubscription == null)
        {
          attributeSubscriptions.put(
            attributeHandle, new AttributeSubscription(
            attributeHandle, attributeRegionAssociation.regions, passive));
        }
        else
        {
          attributeSubscription.subscribe(
            attributeRegionAssociation.regions, passive);
        }
      }
    }
  }

  public void unsubscribeObjectClass(ObjectClassHandle objectClassHandle)
  {
    Map<AttributeHandle, AttributeSubscription> attributeSubscriptions =
      getAttributeSubscriptions(objectClassHandle, false);

    if (attributeSubscriptions != null)
    {
      for (AttributeSubscription attributeSubscription : attributeSubscriptions.values())
      {
        // unsubscribe from the default region
        //
        attributeSubscription.unsubscribe();
      }
    }
  }

  public void unsubscribeObjectClassAttributes(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
  {
    Map<AttributeHandle, AttributeSubscription> attributeSubscriptions =
      getAttributeSubscriptions(objectClassHandle, false);

    if (attributeSubscriptions != null)
    {
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        AttributeSubscription attributeSubscription =
          attributeSubscriptions.get(attributeHandle);
        if (attributeSubscription != null)
        {
          // unsubscribe from the default region
          //
          attributeSubscription.unsubscribe();
        }
      }
    }
  }

  public void unsubscribeObjectClassAttributes(
    ObjectClassHandle objectClassHandle,
    AttributeSetRegionSetPairList attributesAndRegions)
  {
    Map<AttributeHandle, AttributeSubscription> attributeSubscriptions =
      getAttributeSubscriptions(objectClassHandle, false);

    if (attributeSubscriptions != null)
    {
      for (AttributeRegionAssociation attributeRegionAssociation : attributesAndRegions)
      {
        for (AttributeHandle attributeHandle : attributeRegionAssociation.attributes)
        {
          AttributeSubscription attributeSubscription =
            attributeSubscriptions.get(attributeHandle);
          if (attributeSubscription != null)
          {
            // unsubscribe from the specified regions
            //
            attributeSubscription.unsubscribe(
              attributeRegionAssociation.regions);
          }
        }
      }
    }
  }

  public boolean isInteractionClassSubscribed(
    InteractionClassHandle interactionClassHandle)
  {
    return subscribedInteractionClasses.containsKey(interactionClassHandle);
  }

  public void subscribeInteractionClass(
    InteractionClassHandle interactionClassHandle, boolean passive)
  {
    InteractionClassSubscription interactionClassSubscription =
      subscribedInteractionClasses.get(interactionClassHandle);
    if (interactionClassSubscription == null)
    {
      subscribedInteractionClasses.put(
        interactionClassHandle,
        new InteractionClassSubscription(interactionClassHandle, passive));
    }
    else
    {
      interactionClassSubscription.subscribe(passive);
    }
  }

  public void subscribeInteractionClass(
    InteractionClassHandle interactionClassHandle,
    RegionHandleSet regionHandles, boolean passive)
  {
    InteractionClassSubscription interactionClassSubscription =
      subscribedInteractionClasses.get(interactionClassHandle);
    if (interactionClassSubscription == null)
    {
      subscribedInteractionClasses.put(
        interactionClassHandle,
        new InteractionClassSubscription(interactionClassHandle, passive));
    }
    else
    {
      interactionClassSubscription.subscribe(passive);
    }
  }

  public void unsubscribeInteractionClass(
    InteractionClassHandle interactionClassHandle)
  {
    InteractionClassSubscription interactionClassSubscription =
      subscribedInteractionClasses.get(interactionClassHandle);
    if (interactionClassSubscription != null)
    {
      interactionClassSubscription.unsubscribe();
    }
  }

  public void unsubscribeInteractionClass(
    InteractionClassHandle interactionClassHandle,
    RegionHandleSet regionHandles)
  {
    InteractionClassSubscription interactionClassSubscription =
      subscribedInteractionClasses.get(interactionClassHandle);
    if (interactionClassSubscription != null)
    {
      interactionClassSubscription.unsubscribe(regionHandles);
    }
  }

  public ObjectClass getSubscribedObjectClass(ObjectClass objectClass)
  {
    boolean subscribed = isObjectClassSubscribed(
      objectClass.getObjectClassHandle());

    if (!subscribed && objectClass.hasSuperObjectClass())
    {
      // see if an anscestor of the object class is subscribed

      do
      {
        objectClass = objectClass.getSuperObjectClass();

        subscribed = isObjectClassSubscribed(
          objectClass.getObjectClassHandle());
      } while (!subscribed && objectClass.hasSuperObjectClass());
    }

    return subscribed ? objectClass : null;
  }

  public InteractionClass getSubscribedInteractionClass(
    InteractionClass interactionClass)
  {
    boolean subscribed = isInteractionClassSubscribed(
      interactionClass.getInteractionClassHandle());

    if (!subscribed && interactionClass.hasSuperInteractionClass())
    {
      // see if an anscestor of the interaction class is subscribed

      do
      {
        interactionClass = interactionClass.getSuperInteractionClass();

        subscribed = isInteractionClassSubscribed(
          interactionClass.getInteractionClassHandle());
      } while (!subscribed && interactionClass.hasSuperInteractionClass());
    }

    return subscribed ? interactionClass : null;
  }

  public void trim(AttributeHandleValueMap attributeValues,
                   ObjectClassHandle objectClassHandle)
  {
    Map<AttributeHandle, AttributeSubscription> attributeSubscriptions =
      subscribedObjectClasses.get(objectClassHandle);
    assert attributeSubscriptions != null;

    attributeValues.keySet().retainAll(attributeSubscriptions.keySet());
  }

  protected Map<AttributeHandle, AttributeSubscription> getSubscribedAttributeSubscriptions(
    ObjectClass objectClass)
  {
    Map<AttributeHandle, AttributeSubscription> subscriptions =
      subscribedObjectClasses.get(objectClass.getObjectClassHandle());
    if (subscriptions == null && objectClass.hasSuperObjectClass())
    {
      // see if an anscestor of the object class is subscribed

      do
      {
        objectClass = objectClass.getSuperObjectClass();

        subscriptions = subscribedObjectClasses.get(
          objectClass.getObjectClassHandle());

      } while (subscriptions == null && objectClass.hasSuperObjectClass());
    }
    return subscriptions;
  }

  protected Map<AttributeHandle, AttributeSubscription> getAttributeSubscriptions(
    ObjectClassHandle objectClassHandle, boolean addIfMissing)
  {
    Map<AttributeHandle, AttributeSubscription> attributeSubscriptions =
      subscribedObjectClasses.get(objectClassHandle);
    if (attributeSubscriptions == null && addIfMissing)
    {
      attributeSubscriptions =
        new HashMap<AttributeHandle, AttributeSubscription>();
      subscribedObjectClasses.put(objectClassHandle,
                                  attributeSubscriptions);
    }
    return attributeSubscriptions;
  }

  protected abstract class AbstractSubscription
  {
    protected boolean defaultRegionSubscribed;
    protected boolean defaultRegionPassive;

    protected Map<RegionHandle, Boolean> subscribedRegionHandles =
      new HashMap<RegionHandle, Boolean>();

    public AbstractSubscription(boolean passive)
    {
      subscribe(passive);
    }

    public AbstractSubscription(RegionHandleSet regionHandles, boolean passive)
    {
      subscribe(regionHandles, passive);
    }

    public boolean isDefaultRegionSubscribed()
    {
      return defaultRegionSubscribed;
    }

    public boolean isDefaultRegionPassive()
    {
      return defaultRegionPassive;
    }

    public Set<RegionHandle> getSubscribedRegionHandles()
    {
      return subscribedRegionHandles.keySet();
    }

    public void subscribe(boolean passive)
    {
      defaultRegionSubscribed = true;
      defaultRegionPassive = passive;
    }

    public void unsubscribe()
    {
      defaultRegionSubscribed = false;
    }

    public void subscribe(RegionHandleSet regionHandles, boolean passive)
    {
      for (RegionHandle regionHandle : regionHandles)
      {
        subscribedRegionHandles.put(regionHandle, passive);
      }
    }

    public void unsubscribe(RegionHandleSet regionHandles)
    {
      for (RegionHandle regionHandle : regionHandles)
      {
        subscribedRegionHandles.remove(regionHandle);
      }
    }
  }

  protected class AttributeSubscription
    extends AbstractSubscription
  {
    protected final AttributeHandle attributeHandle;

    public AttributeSubscription(AttributeHandle attributeHandle,
                                 boolean passive)
    {
      super(passive);

      this.attributeHandle = attributeHandle;
    }

    public AttributeSubscription(AttributeHandle attributeHandle,
                                 RegionHandleSet regionHandles, boolean passive)
    {
      super(regionHandles, passive);

      this.attributeHandle = attributeHandle;
    }
  }

  protected class InteractionClassSubscription
    extends AbstractSubscription
  {
    protected final InteractionClassHandle interactionClassHandle;

    public InteractionClassSubscription(
      InteractionClassHandle interactionClassHandle, boolean passive)
    {
      super(passive);

      this.interactionClassHandle = interactionClassHandle;
    }

    public InteractionClassSubscription(
      InteractionClassHandle interactionClassHandle,
      RegionHandleSet regionHandles, boolean passive)
    {
      super(regionHandles, passive);

      this.interactionClassHandle = interactionClassHandle;
    }
  }
}
