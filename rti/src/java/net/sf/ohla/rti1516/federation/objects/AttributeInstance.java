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

import java.util.Iterator;
import java.util.LinkedHashSet;

import net.sf.ohla.rti1516.OHLARegionHandleSet;
import net.sf.ohla.rti1516.fdd.Attribute;

import hla.rti1516.AttributeHandle;
import hla.rti1516.FederateHandle;
import hla.rti1516.OrderType;
import hla.rti1516.RegionHandleSet;
import hla.rti1516.TransportationType;

public class AttributeInstance
  implements Serializable
{
  protected final Attribute attribute;

  protected TransportationType transportationType;
  protected OrderType orderType;

  protected RegionHandleSet associatedRegions = new OHLARegionHandleSet();

  protected FederateHandle owner;

  /**
   * Set if the owner of this attribute is willing to divest ownership.
   */
  protected boolean wantsToDivest;

  /**
   * The 'ownership' line. When federates request ownership of this attribute
   * they are placed into a line and given ownership based upon when they
   * entered the line.
   */
  protected LinkedHashSet<FederateHandle> requestingOwnership =
    new LinkedHashSet<FederateHandle>();

  public AttributeInstance(Attribute attribute)
  {
    this.attribute = attribute;
  }

  public Attribute getAttribute()
  {
    return attribute;
  }

  public AttributeHandle getAttributeHandle()
  {
    return attribute.getAttributeHandle();
  }

  public TransportationType getTransportationType()
  {
    return transportationType;
  }

  public void setTransportationType(TransportationType transportationType)
  {
    this.transportationType = transportationType;
  }

  public OrderType getOrderType()
  {
    return orderType;
  }

  public void setOrderType(OrderType orderType)
  {
    this.orderType = orderType;
  }

  public RegionHandleSet getAssociatedRegions()
  {
    return associatedRegions;
  }

  public void associateRegionsForUpdates(RegionHandleSet regionHandles)
  {
    associatedRegions.addAll(regionHandles);
  }

  public void unassociateRegionsForUpdates(RegionHandleSet regionHandles)
  {
    associatedRegions.removeAll(regionHandles);
  }

  public FederateHandle getOwner()
  {
    return owner;
  }

  public void setOwner(FederateHandle owner)
  {
    this.owner = owner;
  }

  public boolean wantsToDivest()
  {
    return wantsToDivest;
  }

  public FederateHandle unconditionalAttributeOwnershipDivestiture()
  {
    owner = null;
    wantsToDivest = false;

    // TODO: reset transportation/order types to what's in FDD?

    // give ownership to the next in line
    //
    if (!requestingOwnership.isEmpty())
    {
      Iterator<FederateHandle> i = requestingOwnership.iterator();
      owner = i.next();
      i.remove();
    }

    return owner;
  }

  public boolean negotiatedAttributeOwnershipDivestiture(byte[] tag)
  {
    wantsToDivest = true;

    return !requestingOwnership.isEmpty();
  }

  public FederateHandle confirmDivestiture()
  {
    owner = null;
    wantsToDivest = false;

    // TODO: reset transportation/order types to what's in FDD?

    // give ownership to the next in line
    //
    if (!requestingOwnership.isEmpty())
    {
      Iterator<FederateHandle> i = requestingOwnership.iterator();
      owner = i.next();
      i.remove();
    }

    return owner;
  }

  public FederateHandle attributeOwnershipAcquisition(FederateHandle acquiree)
  {
    if (!attributeOwnershipAcquisitionIfAvailable(acquiree))
    {
      // get in line
      //
      requestingOwnership.add(acquiree);
    }

    return owner;
  }

  public boolean attributeOwnershipAcquisitionIfAvailable(
    FederateHandle acquiree)
  {
    if (owner == null)
    {
      // acquire this attribute if it is unowned
      //
      owner = acquiree;
      wantsToDivest = false;
    }
    return owner == acquiree;
  }

  public FederateHandle attributeOwnershipDivestitureIfWanted()
  {
    boolean divested = !requestingOwnership.isEmpty();

    // give ownership to the next in line
    //
    if (divested)
    {
      Iterator<FederateHandle> i = requestingOwnership.iterator();
      owner = i.next();
      i.remove();

      wantsToDivest = false;

      // TODO: reset transportation/order types to what's in FDD?
    }

    return divested ? owner : null;
  }

  public boolean cancelAttributeOwnershipAcquisition(FederateHandle acquiree)
  {
    return requestingOwnership.remove(acquiree);
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture(
    FederateHandle owner)
  {
    if (owner.equals(this.owner))
    {
      wantsToDivest = false;
    }
  }
}
