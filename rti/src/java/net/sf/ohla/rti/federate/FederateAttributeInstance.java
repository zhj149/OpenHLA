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

package net.sf.ohla.rti.federate;

import java.io.Serializable;

import net.sf.ohla.rti.fdd.Attribute;
import net.sf.ohla.rti.hla.rti1516.IEEE1516RegionHandleSet;

import hla.rti1516.AttributeAlreadyBeingDivested;
import hla.rti1516.AttributeDivestitureWasNotRequested;
import hla.rti1516.AttributeHandle;
import hla.rti1516.OrderType;
import hla.rti1516.RegionHandleSet;
import hla.rti1516.TransportationType;

public class FederateAttributeInstance
  implements Serializable
{
  protected final Attribute attribute;

  protected TransportationType transportationType;
  protected OrderType orderType;

  protected RegionHandleSet associatedRegions = new IEEE1516RegionHandleSet();

  public FederateAttributeInstance(Attribute attribute)
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

  public void negotiatedAttributeOwnershipDivestiture()
  {
  }

  public void confirmDivestiture()
  {
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture()
  {
  }

  public void cancelAttributeOwnershipAcquisition()
  {
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
}
