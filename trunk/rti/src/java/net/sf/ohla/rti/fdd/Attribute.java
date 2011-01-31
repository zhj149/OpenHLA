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

package net.sf.ohla.rti.fdd;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandle;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.DimensionHandleSet;
import hla.rti1516e.OrderType;
import hla.rti1516e.TransportationTypeHandle;

public class Attribute
{
  public static final Attribute HLA_PRIVILEGE_TO_DELETE_OBJECT = new Attribute(
    new IEEE1516eAttributeHandle(1), "HLAprivilegeToDeleteObject", null,
    TransportationType.HLA_RELIABLE.getTransportationTypeHandle(), OrderType.TIMESTAMP);

  private final AttributeHandle attributeHandle;
  private final String name;

  private final DimensionHandleSet dimensions;

  private TransportationTypeHandle transportationTypeHandle;
  private OrderType orderType;

  public Attribute(AttributeHandle attributeHandle, String name, DimensionHandleSet dimensions,
                   TransportationTypeHandle transportationTypeHandle, OrderType orderType)
  {
    this.attributeHandle = attributeHandle;
    this.name = name;
    this.dimensions = dimensions;
    this.transportationTypeHandle = transportationTypeHandle;
    this.orderType = orderType;
  }

  public AttributeHandle getAttributeHandle()
  {
    return attributeHandle;
  }

  public String getName()
  {
    return name;
  }

  public DimensionHandleSet getDimensions()
  {
    return dimensions;
  }

  public OrderType getOrderType()
  {
    return orderType;
  }

  public void setOrderType(OrderType orderType)
  {
    this.orderType = orderType;
  }

  public TransportationTypeHandle getTransportationTypeHandle()
  {
    return transportationTypeHandle;
  }

  public void setTransportationTypeHandle(TransportationTypeHandle transportationTypeHandle)
  {
    this.transportationTypeHandle = transportationTypeHandle;
  }

  @Override
  public int hashCode()
  {
    return attributeHandle.hashCode();
  }

  @Override
  public String toString()
  {
    return name;
  }
}
