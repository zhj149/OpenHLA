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

import java.io.Serializable;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.ohla.rti.hla.rti1516.IEEE1516AttributeHandle;

import org.dom4j.Element;

import hla.rti1516.AttributeHandle;
import hla.rti1516.ErrorReadingFDD;
import hla.rti1516.OrderType;
import hla.rti1516.TransportationType;

public class Attribute
  implements Serializable
{
  public static final String HLA_PRIVILEGE_TO_DELETE_OBJECT =
    "HLAprivilegeToDeleteObject";

  protected AttributeHandle attributeHandle;
  protected String name;

  protected String order = "TimeStamp";
  protected OrderType orderType = OrderType.TIMESTAMP;

  protected String transportation = "HLAreliable";
  protected TransportationType transportationType =
    TransportationType.HLA_RELIABLE;

  protected Set<String> dimensions = new HashSet<String>();

  protected boolean mom;

  public Attribute(String name, AtomicInteger attributeCount)
  {
    this.name = name;

    attributeHandle = new IEEE1516AttributeHandle(attributeCount.incrementAndGet());
  }

  public Attribute(Element attribute, AtomicInteger attributeCount)
    throws ErrorReadingFDD
  {
    this(((org.dom4j.Attribute) attribute.selectSingleNode(
      "@name")).getValue(), attributeCount);

    org.dom4j.Attribute order =
      (org.dom4j.Attribute) attribute.selectSingleNode("@order");
    if (order != null)
    {
      setOrder(order.getValue());
    }

    org.dom4j.Attribute transportation =
      (org.dom4j.Attribute) attribute.selectSingleNode("@transportation");
    if (transportation != null)
    {
      setTransportation(transportation.getValue());
    }

    org.dom4j.Attribute dimensions =
      (org.dom4j.Attribute) attribute.selectSingleNode("@dimensions");
    if (dimensions != null)
    {
      setDimensions(dimensions.getValue());
    }
  }

  public AttributeHandle getAttributeHandle()
  {
    return attributeHandle;
  }

  public String getName()
  {
    return name;
  }

  public String getOrder()
  {
    return order;
  }

  public void setOrder(String order)
    throws ErrorReadingFDD
  {
    this.order = order;

    if ("timestamp".equalsIgnoreCase(order))
    {
      orderType = OrderType.TIMESTAMP;
    }
    else if ("receive".equalsIgnoreCase(this.order))
    {
      orderType = OrderType.RECEIVE;
    }
    else
    {
      throw new ErrorReadingFDD(String.format("unknown order: %s", order));
    }
  }

  public OrderType getOrderType()
  {
    return orderType;
  }

  public String getTransportation()
  {
    return transportation;
  }

  public void setTransportation(String transportation)
    throws ErrorReadingFDD
  {
    this.transportation = transportation;

    if ("hlabesteffort".equalsIgnoreCase(transportation))
    {
      transportationType = TransportationType.HLA_BEST_EFFORT;
    }
    else if ("hlareliable".equalsIgnoreCase(transportation))
    {
      transportationType = TransportationType.HLA_RELIABLE;
    }
    else
    {
      throw new ErrorReadingFDD(String.format(
        "unknown transportation: %s", transportation));
    }
  }

  public TransportationType getTransportationType()
  {
    return transportationType;
  }

  public Set<String> getDimensions()
  {
    return dimensions;
  }

  public boolean isMOM()
  {
    return mom;
  }

  @Override
  public boolean equals(Object rhs)
  {
    return rhs instanceof Attribute &&
           attributeHandle.equals(((Attribute) rhs).attributeHandle);
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

  protected void setDimensions(String dimensions)
    throws ErrorReadingFDD
  {
    for (StringTokenizer tokenizer = new StringTokenizer(dimensions, ",");
         tokenizer.hasMoreTokens();)
    {
      String dimension = tokenizer.nextToken().trim();
      if (dimension.length() > 0 && !"NA".equals(dimension))
      {
        this.dimensions.add(dimension);
      }
    }
  }
}
