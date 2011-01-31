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

import java.util.HashMap;
import java.util.Map;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eInteractionClassHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eParameterHandle;

import hla.rti1516e.DimensionHandleSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.InteractionParameterNotDefined;
import hla.rti1516e.exceptions.NameNotFound;

public class InteractionClass
{
  public static final InteractionClass HLA_INTERACTION_ROOT = new InteractionClass(
    new IEEE1516eInteractionClassHandle(1), "HLAinteractionRoot", null, null,
    TransportationType.HLA_RELIABLE.getTransportationTypeHandle(), OrderType.TIMESTAMP);

  private final InteractionClassHandle interactionClassHandle;
  private final String name;

  private final InteractionClass superInteractionClass;

  private final DimensionHandleSet dimensions;

  protected Map<ParameterHandle, Parameter> parameters = new HashMap<ParameterHandle, Parameter>();
  protected Map<String, Parameter> parametersByName = new HashMap<String, Parameter>();

  private TransportationTypeHandle transportationTypeHandle;
  private OrderType orderType;

  public InteractionClass(
    InteractionClassHandle interactionClassHandle, String name, InteractionClass superInteractionClass,
    DimensionHandleSet dimensions, TransportationTypeHandle transportationTypeHandle, OrderType orderType)
  {
    this.interactionClassHandle = interactionClassHandle;
    this.name = name;
    this.superInteractionClass = superInteractionClass;
    this.dimensions = dimensions;
    this.transportationTypeHandle = transportationTypeHandle;
    this.orderType = orderType;

    if (superInteractionClass != null)
    {
      // get a reference to all the super interaction classes attributes
      //
      parameters.putAll(superInteractionClass.parameters);
      parametersByName.putAll(superInteractionClass.parametersByName);
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

  public Parameter addParameter(String name)
  {
    Parameter parameter = parametersByName.get(name);
    if (parameter == null)
    {
      ParameterHandle parameterHandle = new IEEE1516eParameterHandle(parameters.size() + 1);

      parameter = new Parameter(parameterHandle, name);

      parameters.put(parameterHandle, parameter);
      parametersByName.put(name, parameter);
    }
    return parameter;
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
}
