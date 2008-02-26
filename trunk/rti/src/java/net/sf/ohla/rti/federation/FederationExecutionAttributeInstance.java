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

import java.io.Serializable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import net.sf.ohla.rti.fdd.Attribute;

import hla.rti1516.AttributeAlreadyBeingDivested;
import hla.rti1516.AttributeDivestitureWasNotRequested;
import hla.rti1516.AttributeHandle;
import hla.rti1516.OrderType;
import hla.rti1516.RegionHandle;
import hla.rti1516.RegionHandleSet;
import hla.rti1516.TransportationType;

public class FederationExecutionAttributeInstance
  implements Serializable
{
  protected final Attribute attribute;

  protected TransportationType transportationType;
  protected OrderType orderType;

  protected final Map<RegionHandle, FederationExecutionRegion> associatedRegions =
    new HashMap<RegionHandle, FederationExecutionRegion>();

  protected FederateProxy owner;

  /**
   * Set if the owner of this attribute is willing to divest ownership.
   */
  protected boolean wantsToDivest;

  /**
   * The 'ownership' line. When federates request ownership of this attribute
   * they are placed into a line and given ownership based upon when they
   * entered the line.
   */
  protected final LinkedHashSet<FederateProxy> requestingOwnerships =
    new LinkedHashSet<FederateProxy>();

  protected final FederationExecutionObjectInstance objectInstance;

  public FederationExecutionAttributeInstance(
    Attribute attribute, FederationExecutionObjectInstance objectInstance)
  {
    this.attribute = attribute;

    transportationType = attribute.getTransportationType();
    orderType = attribute.getOrderType();

    this.objectInstance = objectInstance;
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

  public void associateRegionForUpdate(FederationExecutionRegion region)
  {
    associatedRegions.put(region.getRegionHandle(), region);
  }

  public void unassociateRegionsForUpdates(RegionHandleSet regionHandles)
  {
    associatedRegions.keySet().removeAll(regionHandles);
  }

  public boolean regionIntersects(FederationExecutionRegion region)
  {
    boolean intersects = false;
    for (Iterator<FederationExecutionRegion> i =
      associatedRegions.values().iterator(); !intersects && i.hasNext();)
    {
      intersects = i.next().intersects(
        region, attribute.getDimensions().keySet());
    }
    return intersects;
  }

  public FederateProxy getOwner()
  {
    return owner;
  }

  public void setOwner(FederateProxy owner)
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

  public FederateProxy unconditionalAttributeOwnershipDivestiture()
  {
    owner = null;
    wantsToDivest = false;

    // give ownership to the next in line
    //
    if (!requestingOwnerships.isEmpty())
    {
      Iterator<FederateProxy> i = requestingOwnerships.iterator();
      owner = i.next();
      i.remove();
    }

    return owner;
  }

  public boolean negotiatedAttributeOwnershipDivestiture(byte[] tag)
  {
    wantsToDivest = true;

    return !requestingOwnerships.isEmpty();
  }

  public FederateProxy confirmDivestiture()
  {
    owner = null;
    wantsToDivest = false;

    // give ownership to the next in line
    //
    if (!requestingOwnerships.isEmpty())
    {
      Iterator<FederateProxy> i = requestingOwnerships.iterator();
      owner = i.next();
      i.remove();
    }

    return owner;
  }

  public boolean attributeOwnershipAcquisitionIfAvailable(
    FederateProxy acquiree)
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

  public FederateProxy attributeOwnershipAcquisition(FederateProxy acquiree)
  {
    if (!attributeOwnershipAcquisitionIfAvailable(acquiree))
    {
      // get in line
      //
      requestingOwnerships.add(acquiree);
    }

    return owner;
  }

  public FederateProxy attributeOwnershipDivestitureIfWanted()
  {
    boolean divested = !requestingOwnerships.isEmpty();

    // give ownership to the next in line
    //
    if (divested)
    {
      Iterator<FederateProxy> i = requestingOwnerships.iterator();
      owner = i.next();
      i.remove();

      wantsToDivest = false;
    }

    return divested ? owner : null;
  }

  public boolean cancelAttributeOwnershipAcquisition(FederateProxy acquiree)
  {
    return requestingOwnerships.remove(acquiree);
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture(
    FederateProxy owner)
  {
    if (owner.equals(this.owner))
    {
      wantsToDivest = false;
    }
  }
}
