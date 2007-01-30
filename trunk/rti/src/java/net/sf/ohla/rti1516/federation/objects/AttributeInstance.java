/*
 * Copyright (c) 2007, Michael Newcomb
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
import net.sf.ohla.rti1516.federation.Federate;

import hla.rti1516.AttributeAlreadyBeingDivested;
import hla.rti1516.AttributeDivestitureWasNotRequested;
import hla.rti1516.AttributeHandle;
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

  protected Federate owner;

  /**
   * Set if the owner of this attribute is willing to divest ownership.
   */
  protected boolean wantsToDivest;

  /**
   * The 'ownership' line. When federates request ownership of this attribute
   * they are placed into a line and given ownership based upon when they
   * entered the line.
   */
  protected LinkedHashSet<Federate> requestingOwnership =
    new LinkedHashSet<Federate>();

  public AttributeInstance(Attribute attribute)
  {
    this.attribute = attribute;

    transportationType = attribute.getTransportationType();
    orderType = attribute.getOrderType();
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

  public Federate getOwner()
  {
    return owner;
  }

  public void setOwner(Federate owner)
  {
    this.owner = owner;
  }

  public boolean wantsToDivest()
  {
    return wantsToDivest;
  }

  public void checkIfAttributeDivestitureWasNotRequested()
    throws AttributeDivestitureWasNotRequested
  {
    // TODO: check status
  }

  public void checkIfAttributeAlreadyBeingDivested()
    throws AttributeAlreadyBeingDivested
  {
    // TODO: check status
  }

  public Federate unconditionalAttributeOwnershipDivestiture()
  {
    owner = null;
    wantsToDivest = false;

    // give ownership to the next in line
    //
    if (!requestingOwnership.isEmpty())
    {
      Iterator<Federate> i = requestingOwnership.iterator();
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

  public Federate confirmDivestiture()
  {
    owner = null;
    wantsToDivest = false;

    // give ownership to the next in line
    //
    if (!requestingOwnership.isEmpty())
    {
      Iterator<Federate> i = requestingOwnership.iterator();
      owner = i.next();
      i.remove();
    }

    return owner;
  }

  public boolean attributeOwnershipAcquisitionIfAvailable(Federate acquiree)
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

  public Federate attributeOwnershipAcquisition(Federate acquiree)
  {
    if (!attributeOwnershipAcquisitionIfAvailable(acquiree))
    {
      // get in line
      //
      requestingOwnership.add(acquiree);
    }

    return owner;
  }

  public Federate attributeOwnershipDivestitureIfWanted()
  {
    boolean divested = !requestingOwnership.isEmpty();

    // give ownership to the next in line
    //
    if (divested)
    {
      Iterator<Federate> i = requestingOwnership.iterator();
      owner = i.next();
      i.remove();

      wantsToDivest = false;
    }

    return divested ? owner : null;
  }

  public boolean cancelAttributeOwnershipAcquisition(Federate acquiree)
  {
    return requestingOwnership.remove(acquiree);
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture(Federate owner)
  {
    if (owner.equals(this.owner))
    {
      wantsToDivest = false;
    }
  }
}
