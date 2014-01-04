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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.sf.ohla.rti.fdd.Attribute;
import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eRegionHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eTransportationTypeHandle;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.DimensionHandle;
import hla.rti1516e.FederateHandle;
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

  private double updateRate;

  private TransportationTypeHandle transportationTypeHandle;
  private OrderType orderType;

  private FederateHandle owner;

  /**
   * Set if the owner of this attribute is willing to divest ownership.
   */
  private boolean wantsToDivest;

  private byte[] divestingTag;

  /**
   * The 'ownership' line. When federates request ownership of this attribute they are placed into a line and given
   * ownership based upon when they entered the line.
   */
  private final LinkedHashSet<FederateHandle> requestingOwnerships = new LinkedHashSet<FederateHandle>();

  private final Map<RegionHandle, FederationExecutionRegion> regionRealizations =
    new HashMap<RegionHandle, FederationExecutionRegion>();

  private final Map<FederateHandle, Map<RegionHandle, FederationExecutionRegion>> pendingRegionAssociations =
    new HashMap<FederateHandle, Map<RegionHandle, FederationExecutionRegion>>();

  public FederationExecutionAttributeInstance(Attribute attribute)
  {
    this.attribute = attribute;

    transportationTypeHandle = attribute.getTransportationTypeHandle();
    orderType = attribute.getOrderType();
  }

  public FederationExecutionAttributeInstance(DataInput in, ObjectClass objectClass,
                                              FederationExecution federationExecution)
    throws IOException
  {
    attribute = objectClass.getAttributeSafely(IEEE1516eAttributeHandle.decode(in));

    updateRate = in.readDouble();

    transportationTypeHandle = IEEE1516eTransportationTypeHandle.decode(in);
    orderType = OrderType.values()[in.readInt()];

    if (in.readBoolean())
    {
      owner = IEEE1516eFederateHandle.decode(in);
    }

    wantsToDivest = in.readBoolean();

    int length = in.readInt();
    if (length == 0)
    {
      divestingTag = null;
    }
    else
    {
      divestingTag = new byte[length];
      in.readFully(divestingTag);
    }

    for (int count = in.readInt(); count > 0; count--)
    {
      requestingOwnerships.add(IEEE1516eFederateHandle.decode(in));
    }

    for (int count = in.readInt(); count > 0; count--)
    {
      RegionHandle regionHandle = IEEE1516eRegionHandle.decode(in);
      regionRealizations.put(regionHandle, federationExecution.getRegionManager().getRegions().get(regionHandle));
    }

    for (int count = in.readInt(); count > 0; count--)
    {
      Map<RegionHandle, FederationExecutionRegion> pendingRegionAssociation =
        new HashMap<RegionHandle, FederationExecutionRegion>();
      pendingRegionAssociations.put(IEEE1516eFederateHandle.decode(in), pendingRegionAssociation);
      for (int count2 = in.readInt(); count2 > 0; count2--)
      {
        RegionHandle regionHandle = IEEE1516eRegionHandle.decode(in);
        pendingRegionAssociation.put(
          regionHandle, federationExecution.getRegionManager().getRegions().get(regionHandle));
      }
    }
  }

  public Attribute getAttribute()
  {
    return attribute;
  }

  public double getUpdateRate()
  {
    return updateRate;
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
    if (federateProxy.getFederateHandle().equals(owner))
    {
      regionRealizations.put(region.getRegionHandle(), region);
    }
    else
    {
      Map<RegionHandle, FederationExecutionRegion> pendingRegionAssociations =
        this.pendingRegionAssociations.get(federateProxy.getFederateHandle());
      if (pendingRegionAssociations == null)
      {
        pendingRegionAssociations = new HashMap<RegionHandle, FederationExecutionRegion>();
        this.pendingRegionAssociations.put(federateProxy.getFederateHandle(), pendingRegionAssociations);
      }
      pendingRegionAssociations.put(region.getRegionHandle(), region);
    }
  }

  public void unassociateRegionsForUpdates(FederateProxy federateProxy, RegionHandleSet regionHandles)
  {
    Map<RegionHandle, FederationExecutionRegion> associatedRegions;
    if (federateProxy.getFederateHandle().equals(owner))
    {
      associatedRegions = this.regionRealizations;
    }
    else
    {
      associatedRegions = pendingRegionAssociations.get(federateProxy.getFederateHandle());
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
    return (regionHandles.isEmpty() && regionRealizations.isEmpty()) ||
           regionManager.intersectsOnly(regionHandles, regionRealizations.keySet(), attribute);
  }

  public FederateHandle getOwner()
  {
    return owner;
  }

  public void setOwner(FederateProxy owner)
  {
    this.owner = owner.getFederateHandle();
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

      Iterator<FederateHandle> i = requestingOwnerships.iterator();
      owner = i.next();
      i.remove();

      newOwner();
    }

    return owner == null ? null : new Divestiture(owner, divestingTag);
  }

  public boolean attributeOwnershipAcquisitionIfAvailable(FederateProxy acquiree)
  {
    boolean acquired;
    if (owner != null)
    {
      acquired = false;
    }
    else
    {
      acquired = true;

      // acquire this attribute since it is unowned
      //
      owner = acquiree.getFederateHandle();
      wantsToDivest = false;
      divestingTag = null;

      newOwner();
    }
    return acquired;
  }

  public FederateHandle attributeOwnershipAcquisition(FederateProxy acquiree)
  {
    if (!attributeOwnershipAcquisitionIfAvailable(acquiree))
    {
      // get in line
      //
      requestingOwnerships.add(acquiree.getFederateHandle());
    }

    return owner;
  }

  public Divestiture attributeOwnershipDivestitureIfWanted(FederationExecution federationExecution)
  {
    boolean divested = !requestingOwnerships.isEmpty();

    byte[] divestingTag = this.divestingTag;

    // give ownership to the next in line
    //
    if (divested)
    {
      Iterator<FederateHandle> i = requestingOwnerships.iterator();
      owner = i.next();
      i.remove();

      wantsToDivest = false;
      this.divestingTag = null;

      newOwner();
    }

    return divested ? new Divestiture(owner, divestingTag) : null;
  }

  public boolean cancelAttributeOwnershipAcquisition(FederateProxy acquiree)
  {
    return requestingOwnerships.remove(acquiree.getFederateHandle());
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture(FederateProxy owner)
  {
    if (owner.getFederateHandle().equals(this.owner))
    {
      wantsToDivest = false;
      divestingTag = null;
    }
  }

  public void writeTo(DataOutput out)
    throws IOException
  {
    ((IEEE1516eAttributeHandle) attribute.getAttributeHandle()).writeTo(out);

    out.writeDouble(updateRate);

    ((IEEE1516eTransportationTypeHandle) transportationTypeHandle).writeTo(out);
    out.writeInt(orderType.ordinal());

    if (owner == null)
    {
      out.writeBoolean(false);
    }
    else
    {
      out.writeBoolean(true);
      ((IEEE1516eFederateHandle) owner).writeTo(out);
    }

    out.writeBoolean(wantsToDivest);

    if (divestingTag == null)
    {
      // TODO: maybe write -1 for null?

      out.writeInt(0);
    }
    else
    {
      out.writeInt(divestingTag.length);
      out.write(divestingTag);
    }

    out.writeInt(requestingOwnerships.size());
    for (FederateHandle requestingOwnership : requestingOwnerships)
    {
      ((IEEE1516eFederateHandle) requestingOwnership).writeTo(out);
    }

    out.writeInt(regionRealizations.size());
    for (RegionHandle regionHandle : regionRealizations.keySet())
    {
      ((IEEE1516eRegionHandle) regionHandle).writeTo(out);
    }

    out.writeInt(pendingRegionAssociations.size());
    for (Map.Entry<FederateHandle, Map<RegionHandle, FederationExecutionRegion>> entry : pendingRegionAssociations.entrySet())
    {
      ((IEEE1516eFederateHandle) entry.getKey()).writeTo(out);

      out.writeInt(entry.getValue().size());
      for (RegionHandle regionHandle : entry.getValue().keySet())
      {
        ((IEEE1516eRegionHandle) regionHandle).writeTo(out);
      }
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
    public final FederateHandle newOwner;
    public final byte[] tag;

    public Divestiture(FederateHandle newOwner, byte[] tag)
    {
      this.newOwner = newOwner;
      this.tag = tag;
    }
  }
}
