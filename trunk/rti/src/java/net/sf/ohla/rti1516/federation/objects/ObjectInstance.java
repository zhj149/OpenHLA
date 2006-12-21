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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.ohla.rti1516.fdd.ObjectClass;
import net.sf.ohla.rti1516.fdd.Attribute;
import net.sf.ohla.rti1516.federate.callbacks.AttributeOwnershipAcquisitionNotification;
import net.sf.ohla.rti1516.federate.callbacks.AttributeOwnershipUnavailable;
import net.sf.ohla.rti1516.federate.callbacks.ConfirmAttributeOwnershipAcquisitionCancellation;
import net.sf.ohla.rti1516.federate.callbacks.RequestAttributeOwnershipRelease;
import net.sf.ohla.rti1516.federate.callbacks.RequestDivestitureConfirmation;
import net.sf.ohla.rti1516.federate.callbacks.AttributeIsNotOwned;
import net.sf.ohla.rti1516.federate.callbacks.AttributeIsOwnedByRTI;
import net.sf.ohla.rti1516.federate.callbacks.InformAttributeOwnership;
import net.sf.ohla.rti1516.OHLAAttributeHandleSet;
import net.sf.ohla.rti1516.federation.FederationExecution;

import org.apache.mina.common.IoSession;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.FederateHandle;
import hla.rti1516.ObjectInstanceHandle;

public class ObjectInstance
{
  protected ObjectInstanceHandle objectInstanceHandle;
  protected ObjectClass objectClass;

  protected Lock objectLock = new ReentrantLock(true);

  protected Map<AttributeHandle, AttributeInstance> attributes =
    new HashMap<AttributeHandle, AttributeInstance>();

  public ObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                        ObjectClass objectClass,
                        Set<AttributeHandle> publishedAttributeHandles,
                        FederateHandle owner)
  {
    this.objectInstanceHandle = objectInstanceHandle;
    this.objectClass = objectClass;

    for (Attribute attribute : objectClass.getAttributes().values())
    {
      AttributeInstance attributeInstance =
        new AttributeInstance(attribute);
      attributes.put(attribute.getAttributeHandle(),
                     attributeInstance);

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

  public FederateHandle getOwner(AttributeHandle attributeHandle)
  {
    return attributes.get(attributeHandle).getOwner();
  }

  public void unconditionalAttributeOwnershipDivestiture(
    AttributeHandleSet attributeHandles,
    FederationExecution federationExecution)
  {
    objectLock.lock();
    try
    {
      Map<FederateHandle, AttributeHandleSet> newOwners =
        new HashMap<FederateHandle, AttributeHandleSet>();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        FederateHandle newOwner =
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
      for (Map.Entry<FederateHandle, AttributeHandleSet> entry :
        newOwners.entrySet())
      {
        IoSession ownerSession =
          federationExecution.getFederateSession(entry.getKey());
        if (ownerSession != null)
        {
          ownerSession.write(new AttributeOwnershipAcquisitionNotification(
            objectInstanceHandle, entry.getValue()));
        }
      }
    }
    finally
    {
      objectLock.unlock();
    }
  }

  public void negotiatedAttributeOwnershipDivestiture(
    AttributeHandleSet attributeHandles, byte[] tag, IoSession session)
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
        session.write(new RequestDivestitureConfirmation(
          objectInstanceHandle, divestableAttributeHandles));
      }
    }
    finally
    {
      objectLock.unlock();
    }
  }

  public void confirmDivestiture(AttributeHandleSet attributeHandles,
                                 FederationExecution federationExecution)
  {
    objectLock.lock();
    try
    {
      Map<FederateHandle, AttributeHandleSet> newOwners =
        new HashMap<FederateHandle, AttributeHandleSet>();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        FederateHandle newOwner =
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
      for (Map.Entry<FederateHandle, AttributeHandleSet> entry :
        newOwners.entrySet())
      {
        IoSession ownerSession =
          federationExecution.getFederateSession(entry.getKey());
        if (ownerSession != null)
        {
          ownerSession.write(new AttributeOwnershipAcquisitionNotification(
            objectInstanceHandle, entry.getValue()));
        }
      }
    }
    finally
    {
      objectLock.unlock();
    }
  }

  public void attributeOwnershipAcquisition(
    AttributeHandleSet attributeHandles, byte[] tag, FederateHandle acquiree,
    IoSession session, FederationExecution federationExecution)
  {
    objectLock.lock();
    try
    {
      AttributeHandleSet acquiredAttributeHandles =
        new OHLAAttributeHandleSet();
      Map<FederateHandle, AttributeHandleSet> federatesThatNeedToConfirmDivestiture =
        new HashMap<FederateHandle, AttributeHandleSet>();
      Map<FederateHandle, AttributeHandleSet> federatesThatNeedToRelease =
        new HashMap<FederateHandle, AttributeHandleSet>();

      for (AttributeHandle attributeHandle : attributeHandles)
      {
        AttributeInstance attributeInstance =
          attributes.get(attributeHandle);

        FederateHandle owner =
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
        session.write(new AttributeOwnershipAcquisitionNotification(
          objectInstanceHandle, acquiredAttributeHandles, tag));
      }

      for (Map.Entry<FederateHandle, AttributeHandleSet> entry :
        federatesThatNeedToConfirmDivestiture.entrySet())
      {
        IoSession federateSession =
          federationExecution.getFederateSession(entry.getKey());
        if (federateSession != null)
        {
          federateSession.write(new RequestDivestitureConfirmation(
            objectInstanceHandle, entry.getValue()));
        }
      }

      for (Map.Entry<FederateHandle, AttributeHandleSet> entry :
        federatesThatNeedToRelease.entrySet())
      {
        IoSession federateSession =
          federationExecution.getFederateSession(entry.getKey());
        if (federateSession != null)
        {
          federateSession.write(new RequestAttributeOwnershipRelease(
            objectInstanceHandle, entry.getValue(), tag));
        }
      }
    }
    finally
    {
      objectLock.unlock();
    }
  }

  public void attributeOwnershipAcquisitionIfAvailable(
    AttributeHandleSet attributeHandles, FederateHandle acquiree,
    IoSession session)
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
        session.write(new AttributeOwnershipAcquisitionNotification(
          objectInstanceHandle, acquiredAttributeHandles));
      }

      AttributeHandleSet unacquiredAttributeHandles =
        new OHLAAttributeHandleSet(attributeHandles);
      unacquiredAttributeHandles.removeAll(acquiredAttributeHandles);

      if (!unacquiredAttributeHandles.isEmpty())
      {
        session.write(new AttributeOwnershipUnavailable(
          objectInstanceHandle, unacquiredAttributeHandles));
      }
    }
    finally
    {
      objectLock.unlock();
    }
  }

  public Map<AttributeHandle, FederateHandle> attributeOwnershipDivestitureIfWanted(
    AttributeHandleSet attributeHandles)
  {
    objectLock.lock();
    try
    {
      Map<AttributeHandle, FederateHandle> newOwners =
        new HashMap<AttributeHandle, FederateHandle>();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        FederateHandle newOwner = attributes.get(
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
    AttributeHandleSet attributeHandles, FederateHandle owner)
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
    AttributeHandleSet attributeHandles, FederateHandle acquiree,
    IoSession session)
  {
    objectLock.lock();
    try
    {
      AttributeHandleSet canceledOwnershipAcquisitionAttributeHandles =
        new OHLAAttributeHandleSet();
      for (AttributeHandle attributeHandle : attributeHandles)
      {
        if (attributes.get(
          attributeHandle).cancelAttributeOwnershipAcquisition(acquiree))
        {
          canceledOwnershipAcquisitionAttributeHandles.add(attributeHandle);
        }
      }

      if (!canceledOwnershipAcquisitionAttributeHandles.isEmpty())
      {
        session.write(new ConfirmAttributeOwnershipAcquisitionCancellation(
          objectInstanceHandle, canceledOwnershipAcquisitionAttributeHandles));
      }
    }
    finally
    {
      objectLock.unlock();
    }
  }

  public void queryAttributeOwnership(AttributeHandle attributeHandle,
                                      IoSession session)
  {
    objectLock.lock();
    try
    {
      AttributeInstance attributeInstance =
        attributes.get(attributeHandle);
      assert attributeInstance != null;

      FederateHandle owner = attributeInstance.getOwner();
      if (owner == null)
      {
        session.write(new AttributeIsNotOwned(
          objectInstanceHandle, attributeHandle));
      }
      else if (attributeInstance.getAttribute().isMOM())
      {
        session.write(new AttributeIsOwnedByRTI(
          objectInstanceHandle, attributeHandle));
      }
      else
      {
        session.write(new InformAttributeOwnership(
          objectInstanceHandle, attributeHandle, owner));
      }
    }
    finally
    {
      objectLock.unlock();
    }
  }
}
