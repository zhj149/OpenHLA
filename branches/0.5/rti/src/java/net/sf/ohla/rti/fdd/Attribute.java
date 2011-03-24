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

import net.sf.ohla.rti.Protocol;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandleSet;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandleSetFactory;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eTransportationTypeHandle;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.DimensionHandle;
import hla.rti1516e.DimensionHandleSet;
import hla.rti1516e.OrderType;
import hla.rti1516e.TransportationTypeHandle;

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

  public Attribute(ChannelBuffer buffer, ObjectClass objectClass)
  {
    this.objectClass = objectClass;

    attributeHandle = IEEE1516eAttributeHandle.decode(buffer);
    attributeName = Protocol.decodeString(buffer);
    dimensionHandles = IEEE1516eDimensionHandleSet.decode(buffer);
    transportationTypeHandle = IEEE1516eTransportationTypeHandle.decode(buffer);
    orderType = Protocol.decodeEnum(buffer, OrderType.values());
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
    if (this.dimensionHandles == null || this.dimensionHandles.isEmpty())
    {
      dimensionHandles = null;
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

  public static void encode(ChannelBuffer buffer, Attribute attribute)
  {
    IEEE1516eAttributeHandle.encode(buffer, attribute.attributeHandle);
    Protocol.encodeString(buffer, attribute.attributeName);
    IEEE1516eDimensionHandleSet.encode(buffer, attribute.dimensionHandles);
    IEEE1516eTransportationTypeHandle.encode(buffer, attribute.transportationTypeHandle);
    Protocol.encodeEnum(buffer, attribute.orderType);
  }

  public static Attribute decode(ChannelBuffer buffer, ObjectClass objectClass)
  {
    return new Attribute(buffer, objectClass);
  }
}
