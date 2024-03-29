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

package net.sf.ohla.rti.util;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.ohla.rti.fdd.FDD;
import net.sf.ohla.rti.fdd.InteractionClass;
import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eParameterHandleValueMapFactory;
import net.sf.ohla.rti.proto.OHLAProtos;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.AttributeRegionAssociation;
import hla.rti1516e.AttributeSetRegionSetPairList;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RegionHandle;
import hla.rti1516e.RegionHandleSet;

public class SubscriptionManager
{
  protected final Map<ObjectClassHandle, ObjectClassSubscription> subscribedObjectClasses = new HashMap<>();
  protected final Map<InteractionClassHandle, InteractionClassSubscription> subscribedInteractionClasses =
    new HashMap<>();

  public SubscriptionManager()
  {
  }

  public void saveState(CodedOutputStream out)
    throws IOException
  {
    OHLAProtos.SubscriptionManagerState.Builder subscriptionManagerState =
      OHLAProtos.SubscriptionManagerState.newBuilder();

    for (ObjectClassSubscription objectClassSubscription : subscribedObjectClasses.values())
    {
      subscriptionManagerState.addObjectClassSubscriptions(objectClassSubscription.saveState());
    }

    for (InteractionClassSubscription interactionClassSubscription : subscribedInteractionClasses.values())
    {
      subscriptionManagerState.addInteractionClassSubscriptions(interactionClassSubscription.saveState());
    }

    out.writeMessageNoTag(subscriptionManagerState.build());
  }

  public void restoreState(FDD fdd, CodedInputStream in)
    throws IOException
  {
    OHLAProtos.SubscriptionManagerState subscriptionManagerState =
      in.readMessage(OHLAProtos.SubscriptionManagerState.PARSER, null);

    subscribedObjectClasses.clear();
    for (OHLAProtos.ObjectClassSubscription objectClassSubscriptionProto : subscriptionManagerState.getObjectClassSubscriptionsList())
    {
      ObjectClassSubscription objectClassSubscription = new ObjectClassSubscription(objectClassSubscriptionProto, fdd);
      subscribedObjectClasses.put(objectClassSubscription.getObjectClassHandle(), objectClassSubscription);
    }

    subscribedInteractionClasses.clear();
    for (OHLAProtos.InteractionClassSubscription interactionClassSubscriptionProto : subscriptionManagerState.getInteractionClassSubscriptionsList())
    {
      InteractionClassSubscription interactionClassSubscription =
        new InteractionClassSubscription(interactionClassSubscriptionProto, fdd);
      subscribedInteractionClasses.put(
        interactionClassSubscription.getInteractionClassHandle(), interactionClassSubscription);
    }
  }

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
    ObjectClass objectClass, AttributeSetRegionSetPairList attributesAndRegions, boolean passive)
  {
    ObjectClassSubscription objectClassSubscription = subscribedObjectClasses.get(objectClass.getObjectClassHandle());
    if (objectClassSubscription == null)
    {
      subscribedObjectClasses.put(
        objectClass.getObjectClassHandle(), new ObjectClassSubscription(objectClass, attributesAndRegions, passive));
    }
    else
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
      interactionClassSubscription.subscribe(regionHandles, passive);
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

  public boolean trim(AttributeHandleValueMap attributeValues, ObjectClassHandle objectClassHandle)
  {
    ObjectClassSubscription objectClassSubscription = subscribedObjectClasses.get(objectClassHandle);
    assert objectClassSubscription != null;

    return objectClassSubscription.trim(attributeValues);
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

  protected class ObjectClassSubscription
  {
    private final ObjectClass objectClass;

    private final Map<AttributeHandle, AttributeSubscription> attributeSubscriptions = new HashMap<>();

    private ObjectClassSubscription(OHLAProtos.ObjectClassSubscription objectClassSubscription, FDD fdd)
    {
      objectClass = fdd.getObjectClassSafely(
        ObjectClassHandles.convert(objectClassSubscription.getObjectClassHandle()));

      for (OHLAProtos.AttributeSubscription attributeSubscriptionProto : objectClassSubscription.getAttributeSubscriptionList())
      {
        AttributeSubscription attributeSubscription = new AttributeSubscription(attributeSubscriptionProto);
        attributeSubscriptions.put(attributeSubscription.getAttributeHandle(), attributeSubscription);
      }
    }

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

    public ObjectClass getObjectClass()
    {
      return objectClass;
    }

    public ObjectClassHandle getObjectClassHandle()
    {
      return objectClass.getObjectClassHandle();
    }

    public Map<AttributeHandle, AttributeSubscription> getAttributeSubscriptions()
    {
      return attributeSubscriptions;
    }

    public OHLAProtos.ObjectClassSubscription.Builder saveState()
    {
      OHLAProtos.ObjectClassSubscription.Builder objectClassSubscription =
        OHLAProtos.ObjectClassSubscription.newBuilder();

      objectClassSubscription.setObjectClassHandle(ObjectClassHandles.convert(objectClass.getObjectClassHandle()));

      for (AttributeSubscription attributeSubscription : attributeSubscriptions.values())
      {
        objectClassSubscription.addAttributeSubscription(attributeSubscription.saveState());
      }

      return objectClassSubscription;
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
      if (attributeHandles == null)
      {
        attributeSubscriptions.clear();
      }
      else
      {
        for (AttributeHandle attributeHandle : attributeHandles)
        {
          attributeSubscriptions.remove(attributeHandle);
        }
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

    public boolean trim(AttributeHandleValueMap attributeValues)
    {
      return attributeValues.keySet().retainAll(attributeSubscriptions.keySet());
    }

    public AttributeSubscription getAttributeSubscription(AttributeHandle attributeHandle)
    {
      return attributeSubscriptions.get(attributeHandle);
    }
  }

  protected abstract class RegionSubscription
  {
    protected boolean defaultRegionSubscribed;
    protected boolean defaultRegionSubscriptionPassive;

    protected final Map<RegionHandle, Boolean> subscribedRegionHandles = new HashMap<>();

    protected RegionSubscription()
    {
    }

    protected RegionSubscription(boolean passive)
    {
      subscribe(passive);
    }

    protected RegionSubscription(RegionHandleSet regionHandles, boolean passive)
    {
      subscribe(regionHandles, passive);
    }

    public boolean isDefaultRegionSubscribed()
    {
      return defaultRegionSubscribed;
    }

    public boolean isDefaultRegionSubscriptionPassive()
    {
      return defaultRegionSubscriptionPassive;
    }

    public Set<RegionHandle> getSubscribedRegionHandles()
    {
      return subscribedRegionHandles.keySet();
    }

    public void subscribe(boolean passive)
    {
      defaultRegionSubscribed = true;
      defaultRegionSubscriptionPassive = passive;
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
    private final AttributeHandle attributeHandle;

    private AttributeSubscription(OHLAProtos.AttributeSubscription attributeSubscription)
    {
      attributeHandle = AttributeHandles.convert(attributeSubscription.getAttributeHandle());

      defaultRegionSubscribed = attributeSubscription.getDefaultRegionSubscribed();
      defaultRegionSubscriptionPassive = attributeSubscription.getDefaultRegionSubscriptionPassive();

      for (OHLAProtos.RegionSubscription regionSubscription : attributeSubscription.getRegionSubscriptionsList())
      {
        subscribedRegionHandles.put(
          RegionHandles.convert(regionSubscription.getRegionHandle()), regionSubscription.getPassive());
      }
    }

    private AttributeSubscription(AttributeHandle attributeHandle, boolean passive)
    {
      super(passive);

      this.attributeHandle = attributeHandle;
    }

    private AttributeSubscription(AttributeHandle attributeHandle, RegionHandleSet regionHandles, boolean passive)
    {
      super(regionHandles, passive);

      this.attributeHandle = attributeHandle;
    }

    public AttributeHandle getAttributeHandle()
    {
      return attributeHandle;
    }

    public OHLAProtos.AttributeSubscription.Builder saveState()
    {
      OHLAProtos.AttributeSubscription.Builder attributeSubscription =
        OHLAProtos.AttributeSubscription.newBuilder();

      attributeSubscription.setAttributeHandle(AttributeHandles.convert(attributeHandle));
      attributeSubscription.setDefaultRegionSubscribed(defaultRegionSubscribed);
      attributeSubscription.setDefaultRegionSubscriptionPassive(defaultRegionSubscriptionPassive);

      for (Map.Entry<RegionHandle, Boolean> entry : subscribedRegionHandles.entrySet())
      {
        attributeSubscription.addRegionSubscriptions(
          OHLAProtos.RegionSubscription.newBuilder().setRegionHandle(
            RegionHandles.convert(entry.getKey())).setPassive(
            entry.getValue()));
      }

      return attributeSubscription;
    }
  }

  protected class InteractionClassSubscription
    extends RegionSubscription
  {
    private final InteractionClass interactionClass;

    private InteractionClassSubscription(
      OHLAProtos.InteractionClassSubscription interactionClassSubscription, FDD fdd)
    {
      interactionClass = fdd.getInteractionClassSafely(
        InteractionClassHandles.convert(interactionClassSubscription.getInteractionClassHandle()));

      defaultRegionSubscribed = interactionClassSubscription.getDefaultRegionSubscribed();
      defaultRegionSubscriptionPassive = interactionClassSubscription.getDefaultRegionSubscriptionPassive();

      for (OHLAProtos.RegionSubscription regionSubscription : interactionClassSubscription.getRegionSubscriptionsList())
      {
        subscribedRegionHandles.put(
          RegionHandles.convert(regionSubscription.getRegionHandle()), regionSubscription.getPassive());
      }
    }

    private InteractionClassSubscription(InteractionClass interactionClass, boolean passive)
    {
      super(passive);

      this.interactionClass = interactionClass;
    }

    private InteractionClassSubscription(
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

    public OHLAProtos.InteractionClassSubscription.Builder saveState()
    {
      OHLAProtos.InteractionClassSubscription.Builder interactionClassSubscription =
        OHLAProtos.InteractionClassSubscription.newBuilder();

      interactionClassSubscription.setInteractionClassHandle(
        InteractionClassHandles.convert(interactionClass.getInteractionClassHandle()));
      interactionClassSubscription.setDefaultRegionSubscribed(defaultRegionSubscribed);
      interactionClassSubscription.setDefaultRegionSubscriptionPassive(defaultRegionSubscriptionPassive);

      for (Map.Entry<RegionHandle, Boolean> entry : subscribedRegionHandles.entrySet())
      {
        interactionClassSubscription.addRegionSubscriptions(
          OHLAProtos.RegionSubscription.newBuilder().setRegionHandle(
            RegionHandles.convert(entry.getKey())).setPassive(
            entry.getValue()));
      }

      return interactionClassSubscription;
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
        trimmedParameterValues = IEEE1516eParameterHandleValueMapFactory.INSTANCE.create(parameterValues.size());

        // keep the parameters only at the interaction level that is subscribed
        //
        for (ParameterHandle parameterHandle : this.interactionClass.getParameters().keySet())
        {
          trimmedParameterValues.put(parameterHandle, parameterValues.get(parameterHandle));
        }
      }
      return trimmedParameterValues;
    }
  }
}
