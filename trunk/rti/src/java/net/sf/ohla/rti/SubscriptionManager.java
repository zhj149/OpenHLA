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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sf.ohla.rti.fdd.InteractionClass;
import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eParameterHandleValueMap;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.AttributeRegionAssociation;
import hla.rti1516e.AttributeSetRegionSetPairList;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RegionHandle;
import hla.rti1516e.RegionHandleSet;

public class SubscriptionManager
{
  protected Map<ObjectClassHandle, ObjectClassSubscription> subscribedObjectClasses =
    new HashMap<ObjectClassHandle, ObjectClassSubscription>();

  protected Map<InteractionClassHandle, InteractionClassSubscription> subscribedInteractionClasses =
    new HashMap<InteractionClassHandle, InteractionClassSubscription>();

  /**
   * Returns {@code true} if the specified {@code ObjectClassHandle} is explicitly subscribed. This method does
   * <b>not</b> test for any subscriptions to any ancestors of the specified object class.
   *
   * @param objectClassHandle the specified {@code ObjectClassHandle} to test for subscription
   * @return {@code true} if the specified {@code ObjectClassHandle} is explicitly subscribed; {@code false} otherwise
   */
  public boolean isObjectClassSubscribed(ObjectClassHandle objectClassHandle)
  {
    return subscribedObjectClasses.containsKey(objectClassHandle);
  }

  public void subscribeObjectClassAttributes(
    ObjectClass objectClass, AttributeHandleSet attributeHandles, boolean passive)
  {
    ObjectClassSubscription objectClassSubscription = subscribedObjectClasses.get(objectClass.getObjectClassHandle());
    if (objectClassSubscription == null)
    {
      subscribedObjectClasses.put(
        objectClass.getObjectClassHandle(), new ObjectClassSubscription(objectClass, attributeHandles, passive));
    }
    else
    {
      objectClassSubscription.subscribe(attributeHandles, passive);
    }
  }

  public void subscribeObjectClassAttributes(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions, boolean passive)
  {
    ObjectClassSubscription objectClassSubscription = subscribedObjectClasses.get(objectClassHandle);
    if (objectClassSubscription != null)
    {
      objectClassSubscription.subscribe(attributesAndRegions, passive);
    }
  }

  public void unsubscribeObjectClass(ObjectClassHandle objectClassHandle)
  {
    subscribedObjectClasses.remove(objectClassHandle);
  }

  public void unsubscribeObjectClassAttributes(ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
  {
    ObjectClassSubscription objectClassSubscription = subscribedObjectClasses.get(objectClassHandle);
    if (objectClassSubscription != null)
    {
      if (objectClassSubscription.unsubscribe(attributeHandles))
      {
        subscribedObjectClasses.remove(objectClassHandle);
      }
    }
  }

  public void unsubscribeObjectClassAttributes(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions)
  {
    ObjectClassSubscription objectClassSubscription = subscribedObjectClasses.get(objectClassHandle);
    if (objectClassSubscription != null)
    {
      if (objectClassSubscription.unsubscribe(attributesAndRegions))
      {
        subscribedObjectClasses.remove(objectClassHandle);
      }
    }
  }

  /**
   * Returns {@code true} if the specified {@code InteractionClassHandle} is explicitly subscribed. This method does
   * <b>not</b> test for any subscriptions to any ancestors of the specified interaction class.
   *
   * @param interactionClassHandle the specified {@code InteractionClassHandle} to test for subscription
   * @return {@code true} if the specified {@code InteractionClassHandle} is explicitly subscribed; {@code false}
   *         otherwise
   */
  public boolean isInteractionClassSubscribed(InteractionClassHandle interactionClassHandle)
  {
    return subscribedInteractionClasses.containsKey(interactionClassHandle);
  }

  public void subscribeInteractionClass(InteractionClass interactionClass, boolean passive)
  {
    InteractionClassSubscription interactionClassSubscription =
      subscribedInteractionClasses.get(interactionClass.getInteractionClassHandle());
    if (interactionClassSubscription == null)
    {
      subscribedInteractionClasses.put(
        interactionClass.getInteractionClassHandle(), new InteractionClassSubscription(interactionClass, passive));
    }
    else
    {
      interactionClassSubscription.subscribe(passive);
    }
  }

  public void subscribeInteractionClass(
    InteractionClass interactionClass, RegionHandleSet regionHandles, boolean passive)
  {
    InteractionClassSubscription interactionClassSubscription =
      subscribedInteractionClasses.get(interactionClass.getInteractionClassHandle());
    if (interactionClassSubscription == null)
    {
      subscribedInteractionClasses.put(
        interactionClass.getInteractionClassHandle(),
        new InteractionClassSubscription(interactionClass, regionHandles, passive));
    }
    else
    {
      interactionClassSubscription.subscribe(passive);
    }
  }

  public void unsubscribeInteractionClass(InteractionClassHandle interactionClassHandle)
  {
    subscribedInteractionClasses.remove(interactionClassHandle);
  }

  public void unsubscribeInteractionClass(InteractionClassHandle interactionClassHandle, RegionHandleSet regionHandles)
  {
    InteractionClassSubscription interactionClassSubscription =
      subscribedInteractionClasses.get(interactionClassHandle);
    if (interactionClassSubscription != null)
    {
      interactionClassSubscription.unsubscribe(regionHandles);

      if (interactionClassSubscription.getSubscribedRegionHandles().isEmpty())
      {
        // the interaction is unsubscribed when there are no longer any subscribed regions
        //
        subscribedInteractionClasses.remove(interactionClassHandle);
      }
    }
  }

  public ObjectClass getSubscribedObjectClass(ObjectClass objectClass)
  {
    boolean subscribed = isObjectClassSubscribed(objectClass.getObjectClassHandle());

    if (!subscribed && objectClass.hasSuperObjectClass())
    {
      // see if an anscestor of the object class is subscribed

      do
      {
        objectClass = objectClass.getSuperObjectClass();

        subscribed = isObjectClassSubscribed(objectClass.getObjectClassHandle());
      } while (!subscribed && objectClass.hasSuperObjectClass());
    }

    return subscribed ? objectClass : null;
  }

  public InteractionClass getSubscribedInteractionClass(InteractionClass interactionClass)
  {
    boolean subscribed = isInteractionClassSubscribed(interactionClass.getInteractionClassHandle());

    if (!subscribed && interactionClass.hasSuperInteractionClass())
    {
      // see if an anscestor of the interaction class is subscribed

      do
      {
        interactionClass = interactionClass.getSuperInteractionClass();

        subscribed = isInteractionClassSubscribed(interactionClass.getInteractionClassHandle());
      } while (!subscribed && interactionClass.hasSuperInteractionClass());
    }

    return subscribed ? interactionClass : null;
  }

  public void trim(AttributeHandleValueMap attributeValues, ObjectClassHandle objectClassHandle)
  {
    ObjectClassSubscription objectClassSubscription = subscribedObjectClasses.get(objectClassHandle);
    assert objectClassSubscription != null;

    objectClassSubscription.trim(attributeValues);
  }

  protected ObjectClassSubscription getSubscribedObjectClassSubscription(ObjectClass objectClass)
  {
    ObjectClassSubscription objectClassSubscription =
      subscribedObjectClasses.get(objectClass.getObjectClassHandle());
    if (objectClassSubscription == null && objectClass.hasSuperObjectClass())
    {
      // see if an anscestor of the object class is subscribed

      do
      {
        objectClass = objectClass.getSuperObjectClass();

        objectClassSubscription = subscribedObjectClasses.get(objectClass.getObjectClassHandle());
      } while (objectClassSubscription == null && objectClass.hasSuperObjectClass());
    }
    return objectClassSubscription;
  }

  protected InteractionClassSubscription getSubscribedInteractionClassSubscription(InteractionClass interactionClass)
  {
    InteractionClassSubscription interactionClassSubscription =
      subscribedInteractionClasses.get(interactionClass.getInteractionClassHandle());
    if (interactionClassSubscription == null && interactionClass.hasSuperInteractionClass())
    {
      // see if an anscestor of the interaction class is subscribed

      do
      {
        interactionClass = interactionClass.getSuperInteractionClass();

        interactionClassSubscription = subscribedInteractionClasses.get(interactionClass.getInteractionClassHandle());

      } while (interactionClassSubscription == null && interactionClass.hasSuperInteractionClass());
    }
    return interactionClassSubscription;
  }

  protected boolean containsAny(final Collection lhs, final Collection rhs)
  {
    boolean containsAny = false;

    if (lhs.size() < rhs.size())
    {
      for (Iterator i = lhs.iterator(); !containsAny && i.hasNext();)
      {
        containsAny = rhs.contains(i.next());
      }
    }
    else
    {
      for (Iterator i = rhs.iterator(); !containsAny && i.hasNext();)
      {
        containsAny = lhs.contains(i.next());
      }
    }

    return containsAny;
  }

  protected class ObjectClassSubscription
  {
    private final ObjectClass objectClass;

    private final Map<AttributeHandle, AttributeSubscription> attributeSubscriptions =
      new HashMap<AttributeHandle, AttributeSubscription>();

    private ObjectClassSubscription(ObjectClass objectClass, AttributeHandleSet attributeHandles, boolean passive)
    {
      this.objectClass = objectClass;

      subscribe(attributeHandles, passive);
    }

    private ObjectClassSubscription(
      ObjectClass objectClass, AttributeSetRegionSetPairList attributesAndRegions, boolean passive)
    {
      this.objectClass = objectClass;

      subscribe(attributesAndRegions, passive);
    }

    public void subscribe(AttributeHandleSet attributeHandles, boolean passive)
    {
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        AttributeSubscription attributeSubscription = attributeSubscriptions.get(attributeHandle);
        if (attributeSubscription == null)
        {
          attributeSubscriptions.put(attributeHandle, new AttributeSubscription(attributeHandle, passive));
        }
        else
        {
          attributeSubscription.subscribe(passive);
        }
      }
    }

    public void subscribe(AttributeSetRegionSetPairList attributesAndRegions, boolean passive)
    {
      for (AttributeRegionAssociation attributeRegionAssociation : attributesAndRegions)
      {
        for (AttributeHandle attributeHandle : attributeRegionAssociation.ahset)
        {
          AttributeSubscription attributeSubscription = attributeSubscriptions.get(attributeHandle);
          if (attributeSubscription == null)
          {
            attributeSubscriptions.put(
              attributeHandle, new AttributeSubscription(attributeHandle, attributeRegionAssociation.rhset, passive));
          }
          else
          {
            attributeSubscription.subscribe(attributeRegionAssociation.rhset, passive);
          }
        }
      }
    }

    /**
     * Unsubscribes from the specified {@code attributeHandles}. Returns {@code true} if there are no more attribute
     * subscriptions.
     *
     * @param attributeHandles the {@code attributeHandles} to unsubscribe from
     * @return {@code true} if there are no more attribute subscriptions; {@code false} otherwise
     */
    public boolean unsubscribe(AttributeHandleSet attributeHandles)
    {
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        attributeSubscriptions.remove(attributeHandle);
      }

      return attributeSubscriptions.isEmpty();
    }

    /**
     * Unsubscribes from the specified {@code attributesAndRegions}. Returns {@code true} if there are no more attribute
     * subscriptions.
     *
     * @param attributesAndRegions the {@code attributesAndRegions} to unsubscribe from
     * @return {@code true} if there are no more attribute subscriptions; {@code false} otherwise
     */
    public boolean unsubscribe(AttributeSetRegionSetPairList attributesAndRegions)
    {
      for (AttributeRegionAssociation attributeRegionAssociation : attributesAndRegions)
      {
        for (AttributeHandle attributeHandle : attributeRegionAssociation.ahset)
        {
          AttributeSubscription attributeSubscription = attributeSubscriptions.get(attributeHandle);
          if (attributeSubscription != null)
          {
            if (attributeSubscription.unsubscribe(attributeRegionAssociation.rhset))
            {
              attributeSubscriptions.remove(attributeHandle);
            }
          }
        }
      }

      return attributeSubscriptions.isEmpty();
    }

    public void trim(AttributeHandleValueMap attributeValues)
    {
      attributeValues.keySet().retainAll(attributeSubscriptions.keySet());
    }

    public AttributeSubscription getAttributeSubscription(AttributeHandle attributeHandle)
    {
      return attributeSubscriptions.get(attributeHandle);
    }
  }

  protected abstract class RegionSubscription
  {
    protected boolean defaultRegionSubscribed;
    protected boolean defaultRegionPassive;

    protected final Map<RegionHandle, Boolean> subscribedRegionHandles =
      new HashMap<RegionHandle, Boolean>();

    public RegionSubscription(boolean passive)
    {
      subscribe(passive);
    }

    public RegionSubscription(RegionHandleSet regionHandles, boolean passive)
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

    public void subscribe(RegionHandleSet regionHandles, boolean passive)
    {
      for (RegionHandle regionHandle : regionHandles)
      {
        subscribedRegionHandles.put(regionHandle, passive);
      }
    }

    public boolean unsubscribe(RegionHandleSet regionHandles)
    {
      for (RegionHandle regionHandle : regionHandles)
      {
        subscribedRegionHandles.remove(regionHandle);
      }
      return subscribedRegionHandles.isEmpty();
    }
  }

  protected class AttributeSubscription
    extends RegionSubscription
  {
    protected final AttributeHandle attributeHandle;

    public AttributeSubscription(AttributeHandle attributeHandle, boolean passive)
    {
      super(passive);

      this.attributeHandle = attributeHandle;
    }

    public AttributeSubscription(AttributeHandle attributeHandle, RegionHandleSet regionHandles, boolean passive)
    {
      super(regionHandles, passive);

      this.attributeHandle = attributeHandle;
    }

    public AttributeHandle getAttributeHandle()
    {
      return attributeHandle;
    }
  }

  protected class InteractionClassSubscription
    extends RegionSubscription
  {
    protected final InteractionClass interactionClass;

    public InteractionClassSubscription(InteractionClass interactionClass, boolean passive)
    {
      super(passive);

      this.interactionClass = interactionClass;
    }

    public InteractionClassSubscription(
      InteractionClass interactionClass, RegionHandleSet regionHandles, boolean passive)
    {
      super(regionHandles, passive);

      this.interactionClass = interactionClass;
    }

    public InteractionClass getInteractionClass()
    {
      return interactionClass;
    }

    public InteractionClassHandle getInteractionClassHandle()
    {
      return interactionClass.getInteractionClassHandle();
    }

    public ParameterHandleValueMap trim(InteractionClass interactionClass, ParameterHandleValueMap parameterValues)
    {
      ParameterHandleValueMap trimmedParameterValues;
      if (this.interactionClass == interactionClass)
      {
        trimmedParameterValues = parameterValues;
      }
      else
      {
        trimmedParameterValues = new IEEE1516eParameterHandleValueMap(parameterValues);

        // keep the parameters only at the interaction level that is subscribed
        //
        trimmedParameterValues.keySet().retainAll(this.interactionClass.getParameters().keySet());
      }
      return trimmedParameterValues;
    }
  }
}
