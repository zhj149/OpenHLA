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
import java.util.Set;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandleSet;
import net.sf.ohla.rti.util.DimensionHandles;
import net.sf.ohla.rti.util.InteractionClassHandles;
import net.sf.ohla.rti.util.OrderTypes;
import net.sf.ohla.rti.util.TransportationTypeHandles;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandleSetFactory;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eParameterHandle;
import net.sf.ohla.rti.i18n.ExceptionMessages;
import net.sf.ohla.rti.i18n.I18n;
import net.sf.ohla.rti.proto.OHLAProtos;

import hla.rti1516e.DimensionHandle;
import hla.rti1516e.DimensionHandleSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.InconsistentFDD;
import hla.rti1516e.exceptions.InteractionParameterNotDefined;
import hla.rti1516e.exceptions.InvalidParameterHandle;
import hla.rti1516e.exceptions.NameNotFound;

public class InteractionClass
{
  private final FDD fdd;

  private final InteractionClassHandle interactionClassHandle;
  private final String interactionClassName;

  private final InteractionClass superInteractionClass;

  private final LinkedHashMap<InteractionClassHandle, InteractionClass> subInteractionClasses = new LinkedHashMap<>();
  private final LinkedHashMap<String, InteractionClass> subInteractionClassesByName = new LinkedHashMap<>();

  private final DimensionHandleSet dimensionHandles;

  private TransportationTypeHandle transportationTypeHandle;
  private OrderType orderType;

  private final LinkedHashSet<Parameter> declaredParameters = new LinkedHashSet<>();
  private final LinkedHashMap<String, Parameter> declaredParametersByName = new LinkedHashMap<>();

  private final LinkedHashMap<ParameterHandle, Parameter> parameters = new LinkedHashMap<>();
  private final LinkedHashMap<String, Parameter> parametersByName = new LinkedHashMap<>();

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

  public InteractionClass(FDD fdd, OHLAProtos.FDD.InteractionClass interactionClass)
  {
    this.fdd = fdd;

    interactionClassHandle = InteractionClassHandles.convert(interactionClass.getInteractionClassHandle());
    interactionClassName = interactionClass.getInteractionClassName();

    if (interactionClass.hasSuperInteractionClassHandle())
    {
      superInteractionClass = fdd.getInteractionClassSafely(
        InteractionClassHandles.convert(interactionClass.getSuperInteractionClassHandle()));

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

    dimensionHandles = DimensionHandles.convert(interactionClass.getDimensionHandlesList());
    transportationTypeHandle = TransportationTypeHandles.convert(interactionClass.getTransportationTypeHandle());
    orderType = OrderTypes.convert(interactionClass.getOrderType());

    for (OHLAProtos.FDD.InteractionClass.Parameter parameterProto : interactionClass.getParametersList())
    {
      Parameter parameter = new Parameter(parameterProto);

      declaredParameters.add(parameter);
      declaredParametersByName.put(parameter.getParameterName(), parameter);

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
      throw new InconsistentFDD(I18n.getMessage(
        ExceptionMessages.INCONSISTENT_FDD_PARAMETER_ALREADY_DEFINED, this, parameterName));
    }

    ParameterHandle parameterHandle = new IEEE1516eParameterHandle(parameters.size() + 1);

    Parameter parameter = new Parameter(parameterHandle, parameterName);

    declaredParameters.add(parameter);
    declaredParametersByName.put(parameter.getParameterName(), parameter);

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
    declaredParametersByName.put(parameter.getParameterName(), parameter);

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

  public Parameter getParameter(String parameterName)
    throws NameNotFound
  {
    Parameter parameter = parametersByName.get(parameterName);
    if (parameter == null)
    {
      throw new NameNotFound(I18n.getMessage(ExceptionMessages.PARAMETER_NAME_NOT_FOUND, this, parameterName));
    }
    return parameter;
  }

  public Parameter getValidParameter(ParameterHandle parameterHandle)
    throws InvalidParameterHandle
  {
    Parameter parameter = parameters.get(parameterHandle);
    if (parameter == null)
    {
      throw new InvalidParameterHandle(I18n.getMessage(
        ExceptionMessages.INVALID_PARAMETER_HANDLE, this, parameterHandle));
    }
    return parameter;
  }

  public Parameter getParameter(ParameterHandle parameterHandle)
    throws InteractionParameterNotDefined
  {
    Parameter parameter = parameters.get(parameterHandle);
    if (parameter == null)
    {
      throw new InteractionParameterNotDefined(I18n.getMessage(
        ExceptionMessages.INTERACTION_PARAMETER_NOT_DEFINED, this, parameterHandle));
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
    for (Parameter rhsDeclaredParameter : rhsInteractionClass.declaredParameters)
    {
      Parameter lhsDeclaredParameter = declaredParametersByName.get(rhsDeclaredParameter.getParameterName());
      if (lhsDeclaredParameter == null)
      {
        rhsDeclaredParameter.copyTo(this);
      }
      else if (parametersByName.containsKey(rhsDeclaredParameter.getParameterName()))
      {
        throw new InconsistentFDD(I18n.getMessage(
          ExceptionMessages.INCONSISTENT_FDD_PARAMETER_DECLARED_AT_DIFFERENT_LEVELS, lhsDeclaredParameter, this,
          parametersByName.get(rhsDeclaredParameter.getParameterName())));
      }
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

  public void checkIfInteractionParameterNotDefined(Set<ParameterHandle> parameterHandles)
    throws InteractionParameterNotDefined
  {
    for (ParameterHandle parameterHandle : parameterHandles)
    {
      getParameter(parameterHandle);
    }
  }

  public OHLAProtos.FDD.InteractionClass.Builder toProto()
  {
    OHLAProtos.FDD.InteractionClass.Builder interactionClass =
      OHLAProtos.FDD.InteractionClass.newBuilder().setInteractionClassHandle(
        InteractionClassHandles.convert(interactionClassHandle)).setInteractionClassName(
        interactionClassName).setTransportationTypeHandle(
        TransportationTypeHandles.convert(transportationTypeHandle)).setOrderType(
        OrderTypes.convert(orderType));

    if (superInteractionClass != null)
    {
      interactionClass.setSuperInteractionClassHandle(
        InteractionClassHandles.convert(superInteractionClass.interactionClassHandle));
    }

    for (DimensionHandle dimensionHandle : dimensionHandles)
    {
      interactionClass.addDimensionHandles(DimensionHandles.convert(dimensionHandle));
    }

    for (Parameter parameter : declaredParameters)
    {
      interactionClass.addParameters(parameter.toProto());
    }

    return interactionClass;
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
    if (this.dimensionHandles.isEmpty())
    {
      dimensionHandles = IEEE1516eDimensionHandleSet.EMPTY;
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
}
