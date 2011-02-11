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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import net.sf.ohla.rti.Protocol;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandleSet;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandleSetFactory;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eInteractionClassHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eParameterHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eTransportationTypeHandle;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.DimensionHandle;
import hla.rti1516e.DimensionHandleSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.InconsistentFDD;
import hla.rti1516e.exceptions.InteractionParameterNotDefined;
import hla.rti1516e.exceptions.NameNotFound;

public class InteractionClass
{
  private final FDD fdd;

  private final InteractionClassHandle interactionClassHandle;
  private final String interactionClassName;

  private final InteractionClass superInteractionClass;

  private final LinkedHashMap<InteractionClassHandle, InteractionClass> subInteractionClasses =
    new LinkedHashMap<InteractionClassHandle, InteractionClass>();
  private final LinkedHashMap<String, InteractionClass> subInteractionClassesByName =
    new LinkedHashMap<String, InteractionClass>();

  private final DimensionHandleSet dimensionHandles;

  private TransportationTypeHandle transportationTypeHandle;
  private OrderType orderType;

  private final LinkedHashSet<Parameter> declaredParameters = new LinkedHashSet<Parameter>();

  private final LinkedHashMap<ParameterHandle, Parameter> parameters = new LinkedHashMap<ParameterHandle, Parameter>();
  private final LinkedHashMap<String, Parameter> parametersByName = new LinkedHashMap<String, Parameter>();

  public InteractionClass(
    FDD fdd, InteractionClassHandle interactionClassHandle, String interactionClassName,
    InteractionClass superInteractionClass, DimensionHandleSet dimensionHandles,
    TransportationTypeHandle transportationTypeHandle, OrderType orderType)
  {
    this.fdd = fdd;
    this.interactionClassHandle = interactionClassHandle;
    this.interactionClassName = interactionClassName;
    this.superInteractionClass = superInteractionClass;
    this.dimensionHandles = dimensionHandles;
    this.transportationTypeHandle = transportationTypeHandle;
    this.orderType = orderType;

    if (superInteractionClass != null)
    {
      // get a reference to all the super interaction classes parameters
      //
      parameters.putAll(superInteractionClass.parameters);
      parametersByName.putAll(superInteractionClass.parametersByName);

      superInteractionClass.subInteractionClasses.put(interactionClassHandle, this);
      superInteractionClass.subInteractionClassesByName.put(interactionClassName, this);
    }
  }

  public InteractionClass(ChannelBuffer buffer, FDD fdd)
  {
    this.fdd = fdd;
    interactionClassHandle = IEEE1516eInteractionClassHandle.decode(buffer);
    interactionClassName = Protocol.decodeString(buffer);

    if (Protocol.decodeBoolean(buffer))
    {
      superInteractionClass = fdd.getInteractionClassSafely(IEEE1516eInteractionClassHandle.decode(buffer));

      // get a reference to all the super interaction classes parameters
      //
      parameters.putAll(superInteractionClass.parameters);
      parametersByName.putAll(superInteractionClass.parametersByName);

      superInteractionClass.subInteractionClasses.put(interactionClassHandle, this);
      superInteractionClass.subInteractionClassesByName.put(interactionClassName, this);
    }
    else
    {
      superInteractionClass = null;
    }

    dimensionHandles = IEEE1516eDimensionHandleSet.decode(buffer);
    transportationTypeHandle = IEEE1516eTransportationTypeHandle.decode(buffer);
    orderType = Protocol.decodeEnum(buffer, OrderType.values());

    for (int declaredParameterCount = Protocol.decodeVarInt(buffer); declaredParameterCount > 0;
         declaredParameterCount--)
    {
      Parameter parameter = Parameter.decode(buffer);

      declaredParameters.add(parameter);

      parameters.put(parameter.getParameterHandle(), parameter);
      parametersByName.put(parameter.getParameterName(), parameter);
    }
  }

  public InteractionClassHandle getInteractionClassHandle()
  {
    return interactionClassHandle;
  }

  public String getInteractionClassName()
  {
    return interactionClassName;
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

  public Parameter addParameter(String parameterName)
    throws InconsistentFDD
  {
    if (parametersByName.containsKey(parameterName))
    {
      throw new InconsistentFDD(parameterName + " already exists in " + interactionClassName);
    }

    ParameterHandle parameterHandle = new IEEE1516eParameterHandle(parameters.size() + 1);

    Parameter parameter = new Parameter(parameterHandle, parameterName);

    declaredParameters.add(parameter);

    parameters.put(parameterHandle, parameter);
    parametersByName.put(parameterName, parameter);

    return parameter;
  }

  public Parameter addParameterSafely(String parameterName)
  {
    assert !parametersByName.containsKey(parameterName);

    ParameterHandle parameterHandle = new IEEE1516eParameterHandle(parameters.size() + 1);

    Parameter parameter = new Parameter(parameterHandle, parameterName);

    declaredParameters.add(parameter);

    parameters.put(parameterHandle, parameter);
    parametersByName.put(parameterName, parameter);

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

  public void merge(InteractionClass rhsInteractionClass, FDD fdd)
    throws InconsistentFDD
  {
    if (rhsInteractionClass.declaredParameters.size() > 0 &&
        !declaredParameters.equals(rhsInteractionClass.declaredParameters))
    {
      throw new InconsistentFDD("inconsistent InteractionClass: " + rhsInteractionClass.interactionClassName);
    }

    for (InteractionClass rhsSubInteractionClass : rhsInteractionClass.subInteractionClasses.values())
    {
      InteractionClass lhsSubInteractionClass =
        subInteractionClassesByName.get(rhsSubInteractionClass.interactionClassName);
      if (lhsSubInteractionClass == null)
      {
        rhsSubInteractionClass.copyTo(fdd, this);
      }
      else
      {
        lhsSubInteractionClass.merge(rhsSubInteractionClass, fdd);
      }
    }
  }

  @Override
  public int hashCode()
  {
    return interactionClassHandle.hashCode();
  }

  @Override
  public String toString()
  {
    return interactionClassName;
  }

  private void copyTo(FDD fdd, InteractionClass superInteractionClass)
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
        Dimension oldDimension = this.fdd.getDimensionSafely(oldDimensionHandle);
        Dimension newDimension = fdd.getDimensionSafely(oldDimension.getDimensionName());

        dimensionHandles.add(newDimension.getDimensionHandle());
      }
    }

    InteractionClass interactionClass = fdd.addInteractionClassSafely(
      interactionClassName, superInteractionClass, dimensionHandles, transportationTypeHandle, orderType);

    for (Parameter parameter : declaredParameters)
    {
      parameter.copyTo(interactionClass);
    }

    for (InteractionClass subInteractionClass : subInteractionClasses.values())
    {
      subInteractionClass.copyTo(fdd, interactionClass);
    }
  }

  public static void encode(ChannelBuffer buffer, InteractionClass interactionClass)
  {
    IEEE1516eInteractionClassHandle.encode(buffer, interactionClass.interactionClassHandle);
    Protocol.encodeString(buffer, interactionClass.interactionClassName);

    if (interactionClass.superInteractionClass == null)
    {
      Protocol.encodeBoolean(buffer, false);
    }
    else
    {
      Protocol.encodeBoolean(buffer, true);
      IEEE1516eInteractionClassHandle.encode(buffer, interactionClass.superInteractionClass.interactionClassHandle);
    }

    IEEE1516eDimensionHandleSet.encode(buffer, interactionClass.dimensionHandles);
    IEEE1516eTransportationTypeHandle.encode(buffer, interactionClass.transportationTypeHandle);
    Protocol.encodeEnum(buffer, interactionClass.orderType);

    Protocol.encodeVarInt(buffer, interactionClass.declaredParameters.size());
    for (Parameter parameter : interactionClass.declaredParameters)
    {
      Parameter.encode(buffer, parameter);
    }
  }

  public static InteractionClass decode(ChannelBuffer buffer, FDD fdd)
  {
    return new InteractionClass(buffer, fdd);
  }
}
