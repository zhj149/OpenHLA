/*
 * Copyright (c) 2006, Michael Newcomb
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

package net.sf.ohla.rti1516.fdd;

import java.io.Serializable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.ohla.rti1516.OHLAInteractionClassHandle;

import org.dom4j.Element;

import hla.rti1516.ErrorReadingFDD;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.InteractionParameterNotDefined;
import hla.rti1516.NameNotFound;
import hla.rti1516.OrderType;
import static hla.rti1516.OrderType.RECEIVE;
import static hla.rti1516.OrderType.TIMESTAMP;
import hla.rti1516.ParameterHandle;
import hla.rti1516.TransportationType;
import static hla.rti1516.TransportationType.HLA_BEST_EFFORT;
import static hla.rti1516.TransportationType.HLA_RELIABLE;

public class InteractionClass
  implements Serializable
{
  public static final String HLA_INTERACTION_ROOT = "HLAinteractionRoot";

  protected InteractionClassHandle interactionClassHandle;

  protected String name;

  protected InteractionClass superInteractionClass;

  protected Map<String, InteractionClass> subInteractionClasses =
    new HashMap<String, InteractionClass>();

  protected String order = "TimeStamp";
  protected OrderType orderType = OrderType.TIMESTAMP;

  protected String transportation = "HLAreliable";
  protected TransportationType transportationType =
    TransportationType.HLA_RELIABLE;

  protected Set<String> dimensions = new HashSet<String>();

  protected Map<ParameterHandle, Parameter> parameters =
    new HashMap<ParameterHandle, Parameter>();
  protected Map<String, Parameter> parametersByName =
    new HashMap<String, Parameter>();

  protected boolean mom;

  public InteractionClass(String name, AtomicInteger interactionClassCount)
  {
    this(name, null, interactionClassCount);
  }

  public InteractionClass(String name, InteractionClass superInteractionClass,
                          AtomicInteger interactionClassCount)
  {
    this.name = name;
    this.superInteractionClass = superInteractionClass;

    if (superInteractionClass != null)
    {
      if (!superInteractionClass.isHLAinteractionRoot())
      {
        // fully qualify the name
        //
        this.name = String.format(
          "%s.%s", superInteractionClass.getName(), name);
      }

      // get a reference to all the super interaction classes attributes
      //
      parameters.putAll(superInteractionClass.parameters);
      parametersByName.putAll(superInteractionClass.parametersByName);
    }

    interactionClassHandle =
      new OHLAInteractionClassHandle(interactionClassCount.incrementAndGet());
  }

  public InteractionClass(Element interactionClass,
                          AtomicInteger interactionClassCount,
                          AtomicInteger parameterCount)
    throws ErrorReadingFDD
  {
    this(interactionClass, null, interactionClassCount, parameterCount);
  }

  public InteractionClass(Element interactionClass,
                          InteractionClass superClass,
                          AtomicInteger interactionClassCount,
                          AtomicInteger parameterCount)
    throws ErrorReadingFDD
  {
    this(((org.dom4j.Attribute) interactionClass.selectSingleNode(
      "@name")).getValue(), superClass, interactionClassCount);

    org.dom4j.Attribute order =
      (org.dom4j.Attribute) interactionClass.selectSingleNode("@order");
    if (order != null)
    {
      setOrder(order.getValue());
    }

    org.dom4j.Attribute transportation =
      (org.dom4j.Attribute) interactionClass.selectSingleNode(
        "@transportation");
    if (transportation != null)
    {
      setTransportation(transportation.getValue());
    }

    org.dom4j.Attribute dimensions =
      (org.dom4j.Attribute) interactionClass.selectSingleNode("@dimensions");
    if (dimensions != null)
    {
      setDimensions(dimensions.getValue());
    }

    List<Element> parameters = interactionClass.selectNodes("parameter");
    for (Element e : parameters)
    {
      add(new Parameter(e, parameterCount));
    }

    List<Element> subInteractions =
      interactionClass.selectNodes("interactionClass");
    for (Element e : subInteractions)
    {
      add(new InteractionClass(e, this, interactionClassCount, parameterCount));
    }
  }

  public InteractionClassHandle getInteractionClassHandle()
  {
    return interactionClassHandle;
  }

  public String getName()
  {
    return name;
  }

  public boolean isHLAinteractionRoot()
  {
    return name.equals(HLA_INTERACTION_ROOT);
  }

  public boolean hasSuperInteractionClass()
  {
    return getSuperInteractionClass() != null;
  }

  public InteractionClass getSuperInteractionClass()
  {
    return superInteractionClass;
  }

  public boolean isAssignableFrom(InteractionClass interactionClass)
  {
    return equals(interactionClass) ||
           (interactionClass.hasSuperInteractionClass() &&
            isAssignableFrom(interactionClass.getSuperInteractionClass()));
  }

  public void add(InteractionClass subInteractionClass)
  {
    subInteractionClasses.put(
      subInteractionClass.getName(), subInteractionClass);
  }

  public Map<String, InteractionClass> getSubInteractionClasses()
  {
    return subInteractionClasses;
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
      orderType = TIMESTAMP;
    }
    else if ("receive".equalsIgnoreCase(this.order))
    {
      orderType = RECEIVE;
    }
    else
    {
      throw new ErrorReadingFDD(String.format("unknown order: %s", this.order));
    }
  }

  public OrderType getOrderType()
  {
    return orderType;
  }

  public void setOrderType(OrderType orderType)
  {
    this.orderType = orderType;
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
      transportationType = HLA_BEST_EFFORT;
    }
    else if ("hlareliable".equalsIgnoreCase(transportation))
    {
      transportationType = HLA_RELIABLE;
    }
    else
    {
      throw new ErrorReadingFDD(
        String.format("unknown transportation: %s", transportation));
    }
  }

  public TransportationType getTransportationType()
  {
    return transportationType;
  }

  public void setTransportationType(TransportationType transportationType)
  {
    this.transportationType = transportationType;
  }

  public Set<String> getDimensions()
  {
    return dimensions;
  }

  public void add(Parameter parameter)
  {
    parameters.put(parameter.getParameterHandle(), parameter);
    parametersByName.put(parameter.getName(), parameter);
  }

  public boolean hasParameter(String name)
  {
    return parametersByName.containsKey(name);
  }

  public boolean hasParameter(ParameterHandle parameterHandle)
  {
    return parameters.containsKey(parameterHandle);
  }

  public Parameter getParameter(String name)
    throws NameNotFound
  {
    Parameter parameter = parametersByName.get(name);
    if (parameter == null)
    {
      throw new NameNotFound(
        String.format("parameter name not found: %s", name));
    }
    return parameter;
  }

  public Parameter getParameter(ParameterHandle parameterHandle)
    throws InteractionParameterNotDefined
  {
    Parameter parameter = parameters.get(parameterHandle);
    if (parameter == null)
    {
      throw new InteractionParameterNotDefined(String.format(
        "interaction parameter not defined: %s", parameterHandle));
    }
    return parameter;
  }

  public Map<ParameterHandle, Parameter> getParameters()
  {
    return parameters;
  }

  public Map<String, Parameter> getParametersByName()
  {
    return parametersByName;
  }

  public boolean isMOM()
  {
    return mom;
  }

  @Override
  public boolean equals(Object rhs)
  {
    return rhs instanceof InteractionClass &&
           interactionClassHandle.equals(
             ((InteractionClass) rhs).interactionClassHandle);
  }

  @Override
  public int hashCode()
  {
    return interactionClassHandle.hashCode();
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
