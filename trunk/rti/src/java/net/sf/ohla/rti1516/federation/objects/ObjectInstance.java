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

package net.sf.ohla.rti1516.federation.objects;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.ohla.rti1516.OHLAAttributeHandleSet;
import net.sf.ohla.rti1516.fdd.Attribute;
import net.sf.ohla.rti1516.fdd.ObjectClass;
import net.sf.ohla.rti1516.messages.callbacks.AttributeIsNotOwned;
import net.sf.ohla.rti1516.messages.callbacks.AttributeIsOwnedByRTI;
import net.sf.ohla.rti1516.messages.callbacks.AttributeOwnershipAcquisitionNotification;
import net.sf.ohla.rti1516.messages.callbacks.AttributeOwnershipUnavailable;
import net.sf.ohla.rti1516.messages.callbacks.ConfirmAttributeOwnershipAcquisitionCancellation;
import net.sf.ohla.rti1516.messages.callbacks.InformAttributeOwnership;
import net.sf.ohla.rti1516.messages.callbacks.RequestAttributeOwnershipRelease;
import net.sf.ohla.rti1516.messages.callbacks.RequestDivestitureConfirmation;
import net.sf.ohla.rti1516.federation.Federate;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.ObjectInstanceHandle;

public class ObjectInstance
  implements Serializable
{
  protected final ObjectInstanceHandle objectInstanceHandle;
  protected final ObjectClass objectClass;

  protected final String name;

  protected Lock objectLock = new ReentrantLock(true);

  protected Map<AttributeHandle, AttributeInstance> attributes =
    new HashMap<AttributeHandle, AttributeInstance>();

  public ObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, ObjectClass objectClass,
    String name, Set<AttributeHandle> publishedAttributeHandles, Federate owner)
  {
    this.objectInstanceHandle = objectInstanceHandle;
    this.objectClass = objectClass;
    this.name = name;

    for (Attribute attribute : objectClass.getAttributes().values())
    {
      AttributeInstance attributeInstance = new AttributeInstance(attribute);
      attributes.put(attribute.getAttributeHandle(), attributeInstance);

      if (publishedAttributeHandles.contains(attribute.getAttributeHandle()))
      {
        attributeInstance.setOwner(owner);
      }
    }
  }

  public ObjectInstanceHandle getObjectInstanceHandle()
  {
    return objectInstanceHandle;
  }

  public ObjectClass getObjectClass()
  {
    return objectClass;
  }

  public String getName()
  {
    return name;
  }

  public Federate getOwner(AttributeHandle attributeHandle)
  {
    return attributes.get(attributeHandle).getOwner();
  }

  public void unconditionalAttributeOwnershipDivestiture(
    Federate owner, AttributeHandleSet attributeHandles)
  {
    objectLock.lock();
    try
    {
      Map<Federate, AttributeHandleSet> newOwners =
        new HashMap<Federate, AttributeHandleSet>();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        Federate newOwner =
          attributes.get(
            attributeHandle).unconditionalAttributeOwnershipDivestiture();
        if (newOwner != null)
        {
          AttributeHandleSet acquiredAttributes = newOwners.get(newOwner);
          if (acquiredAttributes == null)
          {
            acquiredAttributes = new OHLAAttributeHandleSet();
            newOwners.put(newOwner, acquiredAttributes);
          }
          acquiredAttributes.add(attributeHandle);
        }
      }

      // notify the new owners
      //
      for (Map.Entry<Federate, AttributeHandleSet> entry : newOwners.entrySet())
      {
        entry.getKey().getSession().write(
          new AttributeOwnershipAcquisitionNotification(
            objectInstanceHandle, entry.getValue()));
      }
    }
    finally
    {
      objectLock.unlock();
    }
  }

  public void negotiatedAttributeOwnershipDivestiture(
    Federate owner, AttributeHandleSet attributeHandles, byte[] tag)
  {
    objectLock.lock();
    try
    {
      AttributeHandleSet divestableAttributeHandles =
        new OHLAAttributeHandleSet();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        if (attributes.get(
          attributeHandle).negotiatedAttributeOwnershipDivestiture(tag))
        {
          divestableAttributeHandles.add(attributeHandle);
        }
      }

      if (!divestableAttributeHandles.isEmpty())
      {
        owner.getSession().write(new RequestDivestitureConfirmation(
          objectInstanceHandle, divestableAttributeHandles));
      }
    }
    finally
    {
      objectLock.unlock();
    }
  }

  public void confirmDivestiture(
    Federate owner, AttributeHandleSet attributeHandles)
  {
    objectLock.lock();
    try
    {
      Map<Federate, AttributeHandleSet> newOwners =
        new HashMap<Federate, AttributeHandleSet>();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        Federate newOwner =
          attributes.get(attributeHandle).confirmDivestiture();
        if (newOwner != null)
        {
          AttributeHandleSet acquiredAttributes = newOwners.get(newOwner);
          if (acquiredAttributes == null)
          {
            acquiredAttributes = new OHLAAttributeHandleSet();
            newOwners.put(newOwner, acquiredAttributes);
          }
          acquiredAttributes.add(attributeHandle);
          System.out.printf("%s - %s\n", newOwner, acquiredAttributes);
        }
      }

      // notify the new owners
      //
      for (Map.Entry<Federate, AttributeHandleSet> entry : newOwners.entrySet())
      {
        entry.getKey().getSession().write(
          new AttributeOwnershipAcquisitionNotification(
            objectInstanceHandle, entry.getValue()));
      }
    }
    finally
    {
      objectLock.unlock();
    }
  }

  public void attributeOwnershipAcquisition(
    Federate acquiree, AttributeHandleSet attributeHandles, byte[] tag)
  {
    objectLock.lock();
    try
    {
      AttributeHandleSet acquiredAttributeHandles =
        new OHLAAttributeHandleSet();
      Map<Federate, AttributeHandleSet> federatesThatNeedToConfirmDivestiture =
        new HashMap<Federate, AttributeHandleSet>();
      Map<Federate, AttributeHandleSet> federatesThatNeedToRelease =
        new HashMap<Federate, AttributeHandleSet>();

      for (AttributeHandle attributeHandle : attributeHandles)
      {
        AttributeInstance attributeInstance =
          attributes.get(attributeHandle);

        Federate owner =
          attributeInstance.attributeOwnershipAcquisition(acquiree);
        if (acquiree.equals(owner))
        {
          // the attribute was unowned and therefore immediately acquired
          //
          acquiredAttributeHandles.add(attributeHandle);
        }
        else if (attributeInstance.wantsToDivest())
        {
          // the attribute is owned but the owner is willing to divest
          //
          AttributeHandleSet divestingAttributeHandles =
            federatesThatNeedToConfirmDivestiture.get(owner);
          if (divestingAttributeHandles == null)
          {
            divestingAttributeHandles = new OHLAAttributeHandleSet();
            federatesThatNeedToConfirmDivestiture.put(
              owner, divestingAttributeHandles);
          }
          divestingAttributeHandles.add(attributeHandle);
        }
        else
        {
          // the attribute is owned but the owner is unwilling to divest
          //
          AttributeHandleSet releasingAttributeHandles =
            federatesThatNeedToRelease.get(owner);
          if (releasingAttributeHandles == null)
          {
            releasingAttributeHandles = new OHLAAttributeHandleSet();
            federatesThatNeedToRelease.put(owner, releasingAttributeHandles);
          }
          releasingAttributeHandles.add(attributeHandle);
        }
      }

      if (!acquiredAttributeHandles.isEmpty())
      {
        acquiree.getSession().write(
          new AttributeOwnershipAcquisitionNotification(
            objectInstanceHandle, acquiredAttributeHandles, tag));
      }

      for (Map.Entry<Federate, AttributeHandleSet> entry :
        federatesThatNeedToConfirmDivestiture.entrySet())
      {
        entry.getKey().getSession().write(new RequestDivestitureConfirmation(
          objectInstanceHandle, entry.getValue()));
      }

      for (Map.Entry<Federate, AttributeHandleSet> entry :
        federatesThatNeedToRelease.entrySet())
      {
        entry.getKey().getSession().write(new RequestAttributeOwnershipRelease(
          objectInstanceHandle, entry.getValue(), tag));
      }
    }
    finally
    {
      objectLock.unlock();
    }
  }

  public void attributeOwnershipAcquisitionIfAvailable(
    Federate acquiree, AttributeHandleSet attributeHandles)
  {
    objectLock.lock();
    try
    {
      AttributeHandleSet acquiredAttributeHandles =
        new OHLAAttributeHandleSet();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        if (attributes.get(
          attributeHandle).attributeOwnershipAcquisitionIfAvailable(acquiree))
        {
          acquiredAttributeHandles.add(attributeHandle);
        }
      }

      if (!acquiredAttributeHandles.isEmpty())
      {
        acquiree.getSession().write(
          new AttributeOwnershipAcquisitionNotification(
            objectInstanceHandle, acquiredAttributeHandles));
      }

      AttributeHandleSet unacquiredAttributeHandles =
        new OHLAAttributeHandleSet(attributeHandles);
      unacquiredAttributeHandles.removeAll(acquiredAttributeHandles);

      if (!unacquiredAttributeHandles.isEmpty())
      {
        acquiree.getSession().write(new AttributeOwnershipUnavailable(
          objectInstanceHandle, unacquiredAttributeHandles));
      }
    }
    finally
    {
      objectLock.unlock();
    }
  }

  public Map<AttributeHandle, Federate> attributeOwnershipDivestitureIfWanted(
    Federate owner, AttributeHandleSet attributeHandles)
  {
    objectLock.lock();
    try
    {
      Map<AttributeHandle, Federate> newOwners =
        new HashMap<AttributeHandle, Federate>();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        Federate newOwner = attributes.get(
          attributeHandle).attributeOwnershipDivestitureIfWanted();
        if (newOwner != null)
        {
          newOwners.put(attributeHandle, newOwner);
        }
      }

      return newOwners;
    }
    finally
    {
      objectLock.unlock();
    }
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture(
    Federate owner, AttributeHandleSet attributeHandles)
  {
    objectLock.lock();
    try
    {
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        attributes.get(
          attributeHandle).cancelNegotiatedAttributeOwnershipDivestiture(owner);
      }
    }
    finally
    {
      objectLock.unlock();
    }
  }

  public void cancelAttributeOwnershipAcquisition(
    Federate acquiree, AttributeHandleSet attributeHandles)
  {
    objectLock.lock();
    try
    {
      AttributeHandleSet canceledAcquisitionAttributeHandles =
        new OHLAAttributeHandleSet();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        if (attributes.get(
          attributeHandle).cancelAttributeOwnershipAcquisition(acquiree))
        {
          canceledAcquisitionAttributeHandles.add(attributeHandle);
        }
      }

      if (!canceledAcquisitionAttributeHandles.isEmpty())
      {
        acquiree.getSession().write(
          new ConfirmAttributeOwnershipAcquisitionCancellation(
            objectInstanceHandle, canceledAcquisitionAttributeHandles));
      }
    }
    finally
    {
      objectLock.unlock();
    }
  }

  public void queryAttributeOwnership(
    Federate federate, AttributeHandle attributeHandle)
  {
    objectLock.lock();
    try
    {
      AttributeInstance attributeInstance =
        attributes.get(attributeHandle);
      assert attributeInstance != null;

      Federate owner = attributeInstance.getOwner();
      if (owner == null)
      {
        federate.getSession().write(new AttributeIsNotOwned(
          objectInstanceHandle, attributeHandle));
      }
      else if (attributeInstance.getAttribute().isMOM())
      {
        federate.getSession().write(new AttributeIsOwnedByRTI(
          objectInstanceHandle, attributeHandle));
      }
      else
      {
        federate.getSession().write(new InformAttributeOwnership(
          objectInstanceHandle, attributeHandle, owner.getFederateHandle()));
      }
    }
    finally
    {
      objectLock.unlock();
    }
  }
}
