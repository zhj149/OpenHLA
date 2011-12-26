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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.sf.ohla.rti.fdd.Attribute;
import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eTransportationTypeHandle;
import net.sf.ohla.rti.i18n.ExceptionMessages;
import net.sf.ohla.rti.i18n.I18n;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.AttributeAlreadyBeingChanged;
import hla.rti1516e.exceptions.AttributeAlreadyBeingDivested;
import hla.rti1516e.exceptions.AttributeDivestitureWasNotRequested;

public class FederateAttributeInstance
{
  private final Attribute attribute;

  private TransportationTypeHandle transportationTypeHandle;
  private OrderType orderType;

  private boolean divesting;

  public FederateAttributeInstance(Attribute attribute)
  {
    this.attribute = attribute;

    transportationTypeHandle = attribute.getTransportationTypeHandle();
    orderType = attribute.getOrderType();
  }

  public FederateAttributeInstance(DataInput in, ObjectClass objectClass)
    throws IOException
  {
    attribute = objectClass.getAttributeSafely(IEEE1516eAttributeHandle.decode(in));

    transportationTypeHandle = IEEE1516eTransportationTypeHandle.decode(in);
    orderType = OrderType.values()[in.readInt()];

    divesting = in.readBoolean();
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

  public void negotiatedAttributeOwnershipDivestiture()
  {
    divesting = true;
  }

  public void confirmDivestiture()
  {
    divesting = false;
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture()
  {
    divesting = false;
  }

  public void checkIfAttributeDivestitureWasNotRequested()
    throws AttributeDivestitureWasNotRequested
  {
    if (!divesting)
    {
      throw new AttributeDivestitureWasNotRequested(I18n.getMessage(
        ExceptionMessages.ATTRIBUTE_DIVESTITURE_WAS_NOT_REQUESTED, attribute));
    }
  }

  public void checkIfAttributeAlreadyBeingDivested()
    throws AttributeAlreadyBeingDivested
  {
    if (divesting)
    {
      throw new AttributeAlreadyBeingDivested(I18n.getMessage(
        ExceptionMessages.ATTRIBUTE_ALREADY_BEING_DIVESTED, attribute));
    }
  }

  public void checkIfAttributeAlreadyBeingChanged()
    throws AttributeAlreadyBeingChanged
  {
    // TODO: check status
  }

  public void writeTo(DataOutput out)
    throws IOException
  {
    ((IEEE1516eAttributeHandle) attribute.getAttributeHandle()).writeTo(out);

    ((IEEE1516eTransportationTypeHandle) transportationTypeHandle).writeTo(out);
    out.writeInt(orderType.ordinal());

    out.writeBoolean(divesting);
  }

  @Override
  public String toString()
  {
    return attribute.toString();
  }
}
