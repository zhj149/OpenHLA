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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.sf.ohla.rti.fdd.Attribute;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.DimensionHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.RangeBounds;
import hla.rti1516e.RegionHandle;
import hla.rti1516e.RegionHandleSet;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.AttributeAlreadyBeingDivested;
import hla.rti1516e.exceptions.AttributeDivestitureWasNotRequested;

public class FederationExecutionAttributeInstance
{
  private final Attribute attribute;

  private TransportationTypeHandle transportationTypeHandle;
  private OrderType orderType;

  private FederateProxy owner;

  /**
   * Set if the owner of this attribute is willing to divest ownership.
   */
  private boolean wantsToDivest;

  private byte[] divestingTag;

  /**
   * The 'ownership' line. When federates request ownership of this attribute they are placed into a line and given
   * ownership based upon when they entered the line.
   */
  private final LinkedHashSet<FederateProxy> requestingOwnerships = new LinkedHashSet<FederateProxy>();

  private final Map<RegionHandle, FederationExecutionRegion> regionRealizations =
    new HashMap<RegionHandle, FederationExecutionRegion>();

  private final Map<FederateProxy, Map<RegionHandle, FederationExecutionRegion>> pendingRegionAssociations =
    new HashMap<FederateProxy, Map<RegionHandle, FederationExecutionRegion>>();

  public FederationExecutionAttributeInstance(Attribute attribute)
  {
    this.attribute = attribute;

    transportationTypeHandle = attribute.getTransportationTypeHandle();
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

  public TransportationTypeHandle getTransportationTypeHandle()
  {
    return transportationTypeHandle;
  }

  public void setTransportationTypeHandle(TransportationTypeHandle transportationTypeHandle)
  {
    this.transportationTypeHandle = transportationTypeHandle;
  }

  public OrderType getOrderType()
  {
    return orderType;
  }

  public void setOrderType(OrderType orderType)
  {
    this.orderType = orderType;
  }

  public Map<RegionHandle, FederationExecutionRegion> getRegionRealizations()
  {
    return regionRealizations;
  }

  public void associateRegionForUpdate(FederateProxy federateProxy, FederationExecutionRegion region)
  {
    if (federateProxy == owner)
    {
      regionRealizations.put(region.getRegionHandle(), region);
    }
    else
    {
      Map<RegionHandle, FederationExecutionRegion> pendingRegionAssociations =
        this.pendingRegionAssociations.get(federateProxy);
      if (pendingRegionAssociations == null)
      {
        pendingRegionAssociations = new HashMap<RegionHandle, FederationExecutionRegion>();
        this.pendingRegionAssociations.put(federateProxy, pendingRegionAssociations);
      }
      pendingRegionAssociations.put(region.getRegionHandle(), region);
    }
  }

  public void unassociateRegionsForUpdates(FederateProxy federateProxy, RegionHandleSet regionHandles)
  {
    Map<RegionHandle, FederationExecutionRegion> associatedRegions;
    if (federateProxy == owner)
    {
      associatedRegions = this.regionRealizations;
    }
    else
    {
      associatedRegions = pendingRegionAssociations.get(federateProxy);
      if (associatedRegions == null)
      {
        associatedRegions = Collections.emptyMap();
      }
    }
    associatedRegions.keySet().removeAll(regionHandles);
  }

  public boolean regionsIntersect(
    FederationExecutionRegionManager regionManager, Set<RegionHandle> regionHandles,
    Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regions)
  {
    return regionManager.intersects(regionHandles, regionRealizations.keySet(), attribute, regions);
  }

  public boolean regionsIntersect(FederationExecutionRegionManager regionManager, Set<RegionHandle> regionHandles)
  {
    return regionManager.intersectsOnly(regionHandles, regionRealizations.keySet(), attribute);
  }

  public FederateProxy getOwner()
  {
    return owner;
  }

  public void setOwner(FederateProxy owner)
  {
    this.owner = owner;
  }

  public boolean isUnowned()
  {
    return owner == null;
  }

  public boolean wantsToDivest()
  {
    return wantsToDivest;
  }

  public byte[] getDivestingTag()
  {
    return divestingTag;
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

  public Divestiture unconditionalAttributeOwnershipDivestiture()
  {
    // same behavior
    //
    return confirmDivestiture();
  }

  /**
   * Puts this {@code FederationExecutionAttributeInstance} into the divesting state. Returns {@code true} if there are
   * federates willing to take ownership of this attribute; {@code false} otherwise.
   *
   * @param tag data specified by the divesting federate
   * @return {@code true} if there are federates willing to take ownership of this attribute; {@code false} otherwise
   */
  public boolean negotiatedAttributeOwnershipDivestiture(byte[] tag)
  {
    wantsToDivest = true;
    divestingTag = tag;

    return !requestingOwnerships.isEmpty();
  }

  public Divestiture confirmDivestiture()
  {
    byte[] divestingTag = this.divestingTag;

    wantsToDivest = false;
    this.divestingTag = null;
    regionRealizations.clear();

    if (requestingOwnerships.isEmpty())
    {
      owner = null;
    }
    else
    {
      // give ownership to the next in line

      Iterator<FederateProxy> i = requestingOwnerships.iterator();
      owner = i.next();
      i.remove();

      newOwner();
    }

    return owner == null ? null : new Divestiture(owner, divestingTag);
  }

  public boolean attributeOwnershipAcquisitionIfAvailable(FederateProxy acquiree)
  {
    if (owner == null)
    {
      // acquire this attribute if it is unowned
      //
      owner = acquiree;
      wantsToDivest = false;
      divestingTag = null;

      newOwner();
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
      divestingTag = null;

      newOwner();
    }

    return divested ? owner : null;
  }

  public boolean cancelAttributeOwnershipAcquisition(FederateProxy acquiree)
  {
    return requestingOwnerships.remove(acquiree);
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture(FederateProxy owner)
  {
    if (this.owner == owner)
    {
      wantsToDivest = false;
      divestingTag = null;
    }
  }

  /**
   * Fired when the attribute gets a new owner.
   */
  protected void newOwner()
  {
    // associate any pending regions
    //
    Map<RegionHandle, FederationExecutionRegion> associatedRegions = pendingRegionAssociations.get(owner);
    if (associatedRegions != null)
    {
      this.regionRealizations.putAll(associatedRegions);
    }
  }

  public static class Divestiture
  {
    public final FederateProxy newOwner;
    public final byte[] tag;

    public Divestiture(FederateProxy newOwner, byte[] tag)
    {
      this.newOwner = newOwner;
      this.tag = tag;
    }
  }
}
