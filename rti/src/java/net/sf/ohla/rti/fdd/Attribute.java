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

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandleSet;
import net.sf.ohla.rti.i18n.ExceptionMessages;
import net.sf.ohla.rti.i18n.I18n;
import net.sf.ohla.rti.util.AttributeHandles;
import net.sf.ohla.rti.util.DimensionHandles;
import net.sf.ohla.rti.util.OrderTypes;
import net.sf.ohla.rti.util.TransportationTypeHandles;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandleSetFactory;
import net.sf.ohla.rti.proto.OHLAProtos;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.DimensionHandle;
import hla.rti1516e.DimensionHandleSet;
import hla.rti1516e.OrderType;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.InconsistentFDD;

public class Attribute
{
  private final ObjectClass objectClass;

  private final AttributeHandle attributeHandle;
  private final String attributeName;

  private final DimensionHandleSet dimensionHandles;

  private TransportationTypeHandle transportationTypeHandle;
  private OrderType orderType;

  public Attribute(
    ObjectClass objectClass, AttributeHandle attributeHandle, String attributeName, DimensionHandleSet dimensionHandles,
    TransportationTypeHandle transportationTypeHandle, OrderType orderType)
  {
    this.objectClass = objectClass;
    this.attributeHandle = attributeHandle;
    this.attributeName = attributeName;
    this.dimensionHandles = dimensionHandles;
    this.transportationTypeHandle = transportationTypeHandle;
    this.orderType = orderType;
  }

  public Attribute(ObjectClass objectClass, OHLAProtos.FDD.ObjectClass.Attribute attribute)
  {
    this.objectClass = objectClass;

    attributeHandle = AttributeHandles.convert(attribute.getAttributeHandle());
    attributeName = attribute.getAttributeName();
    dimensionHandles = DimensionHandles.convert(attribute.getDimensionHandlesList());
    transportationTypeHandle = TransportationTypeHandles.convert(attribute.getTransportationTypeHandle());
    orderType = OrderTypes.convert(attribute.getOrderType());
  }

  public ObjectClass getObjectClass()
  {
    return objectClass;
  }

  public AttributeHandle getAttributeHandle()
  {
    return attributeHandle;
  }

  public String getAttributeName()
  {
    return attributeName;
  }

  public DimensionHandleSet getDimensionHandles()
  {
    return dimensionHandles;
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

  public void copyTo(FDD fdd, ObjectClass objectClass)
  {
    DimensionHandleSet dimensionHandles;
    if (this.dimensionHandles.isEmpty())
    {
      dimensionHandles = IEEE1516eDimensionHandleSet.EMPTY;
    }
    else
    {
      dimensionHandles = IEEE1516eDimensionHandleSetFactory.INSTANCE.create();
      for (DimensionHandle oldDimensionHandle : this.dimensionHandles)
      {
        Dimension oldDimension = this.objectClass.getFDD().getDimensionSafely(oldDimensionHandle);
        Dimension newDimension = fdd.getDimensionSafely(oldDimension.getDimensionName());

        dimensionHandles.add(newDimension.getDimensionHandle());
      }
    }

    objectClass.addAttributeSafely(attributeName, dimensionHandles, transportationTypeHandle, orderType);
  }

  public void checkForInconsistentFDD(Attribute attribute)
    throws InconsistentFDD
  {
    if (transportationTypeHandle != attribute.transportationTypeHandle)
    {
      throw new InconsistentFDD(I18n.getMessage(
        ExceptionMessages.INCONSISTENT_FDD_ATTRIBUTE_MISMATCH, objectClass, this, transportationTypeHandle,
        attribute.transportationTypeHandle));
    }
    else if (orderType != attribute.orderType)
    {
      throw new InconsistentFDD(I18n.getMessage(
        ExceptionMessages.INCONSISTENT_FDD_ATTRIBUTE_MISMATCH, objectClass, this, orderType, attribute.orderType));
    }
    else if (!dimensionHandles.equals(attribute.dimensionHandles))
    {
      throw new InconsistentFDD(I18n.getMessage(
        ExceptionMessages.INCONSISTENT_FDD_ATTRIBUTE_MISMATCH, objectClass, this, dimensionHandles,
        attribute.dimensionHandles));
    }
  }

  public OHLAProtos.FDD.ObjectClass.Attribute.Builder toProto()
  {
    OHLAProtos.FDD.ObjectClass.Attribute.Builder attribute =
      OHLAProtos.FDD.ObjectClass.Attribute.newBuilder().setAttributeHandle(
        AttributeHandles.convert(attributeHandle)).setAttributeName(
        attributeName).setTransportationTypeHandle(
        TransportationTypeHandles.convert(transportationTypeHandle)).setOrderType(
        OrderTypes.convert(orderType));

    for (DimensionHandle dimensionHandle : dimensionHandles)
    {
      attribute.addDimensionHandles(((IEEE1516eDimensionHandle) dimensionHandle).getHandle());
    }

    return attribute;
  }

  @Override
  public int hashCode()
  {
    return attributeHandle.hashCode();
  }

  @Override
  public String toString()
  {
    return attributeName;
  }
}
