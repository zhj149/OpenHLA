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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.ohla.rti.fed.FED;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandleSet;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandleSetFactory;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eInteractionClassHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eObjectClassHandle;
import net.sf.ohla.rti.i18n.ExceptionMessages;
import net.sf.ohla.rti.i18n.I18n;
import net.sf.ohla.rti.proto.OHLAProtos;

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.DimensionHandle;
import hla.rti1516e.DimensionHandleSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.RangeBounds;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.InconsistentFDD;
import hla.rti1516e.exceptions.InteractionClassNotDefined;
import hla.rti1516e.exceptions.InteractionParameterNotDefined;
import hla.rti1516e.exceptions.InvalidAttributeHandle;
import hla.rti1516e.exceptions.InvalidDimensionHandle;
import hla.rti1516e.exceptions.InvalidInteractionClassHandle;
import hla.rti1516e.exceptions.InvalidObjectClassHandle;
import hla.rti1516e.exceptions.InvalidOrderName;
import hla.rti1516e.exceptions.InvalidOrderType;
import hla.rti1516e.exceptions.InvalidParameterHandle;
import hla.rti1516e.exceptions.InvalidRangeBound;
import hla.rti1516e.exceptions.InvalidTransportationName;
import hla.rti1516e.exceptions.InvalidTransportationType;
import hla.rti1516e.exceptions.NameNotFound;
import hla.rti1516e.exceptions.ObjectClassNotDefined;

public class FDD
  implements Cloneable
{
  public static final String HLA_OBJECT_ROOT = "HLAobjectRoot";
  public static final String HLA_PRIVILEGE_TO_DELETE_OBJECT = "HLAprivilegeToDeleteObject";

  public static final String HLA_INTERACTION_ROOT = "HLAinteractionRoot";

  public static final String HLA_OBJECT_ROOT_PREFIX = HLA_OBJECT_ROOT + ".";
  public static final String HLA_INTERACTION_ROOT_PREFIX = HLA_INTERACTION_ROOT + ".";

  private static final BiMap<OrderType, String> orderTypeNames = EnumHashBiMap.create(OrderType.class);

  static
  {
    orderTypeNames.put(OrderType.TIMESTAMP, "TimeStamp");
    orderTypeNames.put(OrderType.RECEIVE, "Receive");
  }

  private final List<String> sources = new LinkedList<>();

  private final LinkedHashMap<ObjectClassHandle, ObjectClass> objectClasses = new LinkedHashMap<>();
  private final LinkedHashMap<String, ObjectClass> objectClassesByName = new LinkedHashMap<>();

  private final LinkedHashMap<InteractionClassHandle, InteractionClass> interactionClasses = new LinkedHashMap<>();
  private final LinkedHashMap<String, InteractionClass> interactionClassesByName = new LinkedHashMap<>();

  private final LinkedHashMap<DimensionHandle, Dimension> dimensions = new LinkedHashMap<>();
  private final LinkedHashMap<String, Dimension> dimensionsByName = new LinkedHashMap<>();

  private final LinkedHashMap<TransportationTypeHandle, TransportationType> transportationTypes = new LinkedHashMap<>();
  private final LinkedHashMap<String, TransportationType> transportationTypesByName = new LinkedHashMap<>();

  private final FED fed;

  public FDD(String source)
  {
    this(source, null);
  }

  public FDD(String source, FED fed)
  {
    sources.add(source);

    this.fed = fed == null ? new FED(this) : fed;

    transportationTypes.put(
      TransportationType.HLA_RELIABLE.getTransportationTypeHandle(), TransportationType.HLA_RELIABLE);
    transportationTypesByName.put(
      TransportationType.HLA_RELIABLE.getName(), TransportationType.HLA_RELIABLE);

    transportationTypes.put(
      TransportationType.HLA_BEST_EFFORT.getTransportationTypeHandle(), TransportationType.HLA_BEST_EFFORT);
    transportationTypesByName.put(
      TransportationType.HLA_BEST_EFFORT.getName(), TransportationType.HLA_BEST_EFFORT);
  }

  public FDD(OHLAProtos.FDD fdd)
  {
    transportationTypes.put(
      TransportationType.HLA_RELIABLE.getTransportationTypeHandle(), TransportationType.HLA_RELIABLE);
    transportationTypesByName.put(
      TransportationType.HLA_RELIABLE.getName(), TransportationType.HLA_RELIABLE);

    transportationTypes.put(
      TransportationType.HLA_BEST_EFFORT.getTransportationTypeHandle(), TransportationType.HLA_BEST_EFFORT);
    transportationTypesByName.put(
      TransportationType.HLA_BEST_EFFORT.getName(), TransportationType.HLA_BEST_EFFORT);

    for (String source : fdd.getSourcesList())
    {
      sources.add(source);
    }

    for (OHLAProtos.FDD.ObjectClass objectClassProto : fdd.getObjectClassesList())
    {
      ObjectClass objectClass = new ObjectClass(this, objectClassProto);

      objectClasses.put(objectClass.getObjectClassHandle(), objectClass);
      objectClassesByName.put(objectClass.getObjectClassName(), objectClass);
    }

    for (OHLAProtos.FDD.InteractionClass interactionClassProto : fdd.getInteractionClassesList())
    {
      InteractionClass interactionClass = new InteractionClass(this, interactionClassProto);

      interactionClasses.put(interactionClass.getInteractionClassHandle(), interactionClass);
      interactionClassesByName.put(interactionClass.getInteractionClassName(), interactionClass);
    }

    for (OHLAProtos.FDD.Dimension dimensionProto : fdd.getDimensionsList())
    {
      Dimension dimension = new Dimension(this, dimensionProto);

      dimensions.put(dimension.getDimensionHandle(), dimension);
      dimensionsByName.put(dimension.getDimensionName(), dimension);
    }

    fed = new FED(this, fdd.getFed());
  }

  public List<String> getSources()
  {
    return sources;
  }

  public FED getFED()
  {
    return fed;
  }

  public Map<ObjectClassHandle, ObjectClass> getObjectClasses()
  {
    return objectClasses;
  }

  public Map<String, ObjectClass> getObjectClassesByName()
  {
    return objectClassesByName;
  }

  public Map<InteractionClassHandle, InteractionClass> getInteractionClasses()
  {
    return interactionClasses;
  }

  public Map<String, InteractionClass> getInteractionClassesByName()
  {
    return interactionClassesByName;
  }

  public Map<DimensionHandle, Dimension> getDimensions()
  {
    return dimensions;
  }

  public Map<String, Dimension> getDimensionsByName()
  {
    return dimensionsByName;
  }

  public FDD merge(FDD fdd)
    throws InconsistentFDD
  {
    FDD mergedFDD = clone();

    mergedFDD.sources.addAll(fdd.sources);

    // merge dimensions first so they are available when merging attributes and interaction classes

    for (Dimension rhsDimension : fdd.dimensions.values())
    {
      Dimension lhsDimension = mergedFDD.dimensionsByName.get(rhsDimension.getDimensionName());
      if (lhsDimension == null)
      {
        Dimension dimension = mergedFDD.addDimension(rhsDimension.getDimensionName());
        dimension.setUpperBound(rhsDimension.getUpperBound());
      }
      else
      {
        lhsDimension.checkForInconsistentFDD(rhsDimension);
      }
    }

    ObjectClass lhsRootObject = mergedFDD.objectClassesByName.get(FDD.HLA_OBJECT_ROOT);
    assert lhsRootObject != null;

    ObjectClass rhsRootObject = fdd.objectClassesByName.get(FDD.HLA_OBJECT_ROOT);
    assert rhsRootObject != null;

    lhsRootObject.merge(rhsRootObject, mergedFDD);

    InteractionClass lhsRootInteraction = mergedFDD.interactionClassesByName.get(FDD.HLA_INTERACTION_ROOT);
    assert lhsRootInteraction != null;

    InteractionClass rhsRootInteraction = fdd.interactionClassesByName.get(FDD.HLA_INTERACTION_ROOT);
    assert rhsRootInteraction != null;

    lhsRootInteraction.merge(rhsRootInteraction, mergedFDD);

    return mergedFDD;
  }

  public FDD merge(List<FDD> fdds)
    throws InconsistentFDD
  {
    FDD mergedFDD = this;

    for (FDD fdd : fdds)
    {
      mergedFDD = mergedFDD.merge(fdd);
    }

    return mergedFDD;
  }

  public ObjectClass addObjectClass(String objectClassName, ObjectClass superObjectClass)
    throws InconsistentFDD
  {
    if (superObjectClass != null && !HLA_OBJECT_ROOT.equals(superObjectClass.getObjectClassName()))
    {
      objectClassName = superObjectClass.getObjectClassName() + "." + objectClassName;
    }

    if (objectClassesByName.containsKey(objectClassName))
    {
      throw new InconsistentFDD(I18n.getMessage(
        ExceptionMessages.INCONSISTENT_FDD_OBJECT_CLASS_ALREADY_DEFINED, objectClassName));
    }

    ObjectClassHandle objectClassHandle = new IEEE1516eObjectClassHandle(objectClasses.size() + 1);

    ObjectClass objectClass = new ObjectClass(this, objectClassHandle, objectClassName, superObjectClass);

    objectClasses.put(objectClassHandle, objectClass);
    objectClassesByName.put(objectClassName, objectClass);

    return objectClass;
  }

  public ObjectClass addObjectClassSafely(String objectClassName, ObjectClass superObjectClass)
  {
    assert !objectClassesByName.containsKey(objectClassName);

    ObjectClassHandle objectClassHandle = new IEEE1516eObjectClassHandle(objectClasses.size() + 1);

    ObjectClass objectClass = new ObjectClass(this, objectClassHandle, objectClassName, superObjectClass);

    objectClasses.put(objectClassHandle, objectClass);
    objectClassesByName.put(objectClassName, objectClass);

    return objectClass;
  }

  public Attribute addAttribute(
    ObjectClass objectClass, String name, Set<String> dimensions, String transportationTypeName, String orderTypeName)
    throws InconsistentFDD
  {
    return addAttribute(objectClass, name, getDimensionHandles(dimensions), transportationTypeName, orderTypeName);
  }

  public Attribute addAttribute(
    ObjectClass objectClass, String attributeName, DimensionHandleSet dimensionHandles,
    String transportationTypeName, String orderTypeName)
    throws InconsistentFDD
  {
    TransportationType transportationType = transportationTypesByName.get(transportationTypeName);
    if (transportationType == null)
    {
      transportationType = TransportationType.HLA_RELIABLE;
    }

    OrderType orderType = orderTypeNames.inverse().get(orderTypeName);
    if (orderType == null)
    {
      orderType = OrderType.RECEIVE;
    }

    return objectClass.addAttribute(
      attributeName, dimensionHandles, transportationType.getTransportationTypeHandle(), orderType);
  }

  public InteractionClass addInteractionClass(
    String name, InteractionClass superInteractionClass, Set<String> dimensions,
    String transportationTypeName, String orderTypeName)
    throws InconsistentFDD
  {
    return addInteractionClass(
      name, superInteractionClass, getDimensionHandles(dimensions), transportationTypeName, orderTypeName);
  }

  public InteractionClass addInteractionClass(
    String interactionClassName, InteractionClass superInteractionClass, DimensionHandleSet dimensionHandles,
    String transportationTypeName, String orderTypeName)
    throws InconsistentFDD
  {
    if (superInteractionClass != null && !HLA_INTERACTION_ROOT.equals(superInteractionClass.getInteractionClassName()))
    {
      interactionClassName = superInteractionClass.getInteractionClassName() + "." + interactionClassName;
    }

    if (interactionClassesByName.containsKey(interactionClassName))
    {
      throw new InconsistentFDD(I18n.getMessage(
        ExceptionMessages.INCONSISTENT_FDD_INTERACTION_CLASS_ALREADY_DEFINED, interactionClassName));
    }

    TransportationType transportationType = transportationTypesByName.get(transportationTypeName);
    if (transportationType == null)
    {
      transportationType = TransportationType.HLA_RELIABLE;
    }

    OrderType orderType = orderTypeNames.inverse().get(orderTypeName);

    InteractionClassHandle interactionClassHandle =
      new IEEE1516eInteractionClassHandle(interactionClasses.size() + 1);

    InteractionClass interactionClass = new InteractionClass(
      this, interactionClassHandle, interactionClassName, superInteractionClass, dimensionHandles,
      transportationType.getTransportationTypeHandle(), orderType);

    interactionClasses.put(interactionClassHandle, interactionClass);
    interactionClassesByName.put(interactionClassName, interactionClass);
    return interactionClass;
  }

  public InteractionClass addInteractionClassSafely(
    String interactionClassName, InteractionClass superInteractionClass, DimensionHandleSet dimensionHandles,
    TransportationTypeHandle transportationTypeHandle, OrderType orderType)
  {
    assert !interactionClassesByName.containsKey(interactionClassName);

    InteractionClassHandle interactionClassHandle =
      new IEEE1516eInteractionClassHandle(interactionClasses.size() + 1);

    InteractionClass interactionClass = new InteractionClass(
      this, interactionClassHandle, interactionClassName, superInteractionClass, dimensionHandles,
      transportationTypeHandle, orderType);

    interactionClasses.put(interactionClassHandle, interactionClass);
    interactionClassesByName.put(interactionClassName, interactionClass);

    return interactionClass;
  }

  public Parameter addParameter(InteractionClass interactionClass, String interactionClassName)
    throws InconsistentFDD
  {
    return interactionClass.addParameter(interactionClassName);
  }

  public Dimension addDimension(String dimensionName)
  {
    Dimension dimension = dimensionsByName.get(dimensionName);
    if (dimension == null)
    {
      DimensionHandle dimensionHandle = new IEEE1516eDimensionHandle(this.dimensions.size() + 1);
      dimension = new Dimension(this, dimensionHandle, dimensionName);

      dimensions.put(dimensionHandle, dimension);
      dimensionsByName.put(dimensionName, dimension);
    }
    return dimension;
  }

  public ObjectClass getValidObjectClass(ObjectClassHandle objectClassHandle)
    throws InvalidObjectClassHandle
  {
    ObjectClass objectClass = objectClasses.get(objectClassHandle);
    if (objectClass == null)
    {
      throw new InvalidObjectClassHandle(I18n.getMessage(
        ExceptionMessages.INVALID_OBJECT_CLASS_HANDLE, objectClassHandle));
    }
    return objectClass;
  }

  public ObjectClass getObjectClass(ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined
  {
    ObjectClass objectClass = objectClasses.get(objectClassHandle);
    if (objectClass == null)
    {
      throw new ObjectClassNotDefined(I18n.getMessage(ExceptionMessages.OBJECT_CLASS_NOT_DEFINED, objectClassHandle));
    }
    return objectClass;
  }

  public ObjectClass getObjectClassSafely(ObjectClassHandle objectClassHandle)
  {
    ObjectClass objectClass = objectClasses.get(objectClassHandle);
    assert objectClass != null;
    return objectClass;
  }

  public ObjectClass getObjectClass(String objectClassName)
    throws NameNotFound
  {
    if (objectClassName.startsWith(HLA_OBJECT_ROOT_PREFIX))
    {
      // strip off the root prefix
      //
      objectClassName = objectClassName.substring(HLA_OBJECT_ROOT_PREFIX.length());
    }

    ObjectClass objectClass = objectClassesByName.get(objectClassName);
    if (objectClass == null)
    {
      throw new NameNotFound(I18n.getMessage(ExceptionMessages.OBJECT_CLASS_NAME_NOT_FOUND, objectClassName));
    }
    return objectClass;
  }

  public ObjectClassHandle getObjectClassHandle(String objectClassName)
    throws NameNotFound
  {
    return getObjectClass(objectClassName).getObjectClassHandle();
  }

  public String getObjectClassName(ObjectClassHandle objectClassHandle)
    throws InvalidObjectClassHandle
  {
    return getValidObjectClass(objectClassHandle).getObjectClassName();
  }

  public Attribute getAttribute(ObjectClassHandle objectClassHandle, String attributeName)
    throws NameNotFound, ObjectClassNotDefined
  {
    return getObjectClass(objectClassHandle).getAttribute(attributeName);
  }

  public Attribute getAttribute(ObjectClassHandle objectClassHandle, AttributeHandle attributeHandle)
    throws AttributeNotDefined, ObjectClassNotDefined
  {
    return getObjectClass(objectClassHandle).getAttribute(attributeHandle);
  }

  public AttributeHandle getAttributeHandle(ObjectClassHandle objectClassHandle, String attributeName)
    throws NameNotFound, InvalidObjectClassHandle
  {
    return getValidObjectClass(objectClassHandle).getAttribute(attributeName).getAttributeHandle();
  }

  public String getAttributeName(ObjectClassHandle objectClassHandle, AttributeHandle attributeHandle)
    throws InvalidAttributeHandle, InvalidObjectClassHandle
  {
    return getValidObjectClass(objectClassHandle).getValidAttribute(attributeHandle).getAttributeName();
  }

  public InteractionClass getValidInteractionClass(InteractionClassHandle interactionClassHandle)
    throws InvalidInteractionClassHandle
  {
    InteractionClass interactionClass = interactionClasses.get(interactionClassHandle);
    if (interactionClass == null)
    {
      throw new InvalidInteractionClassHandle(I18n.getMessage(
        ExceptionMessages.INVALID_INTERACTION_CLASS_HANDLE, interactionClassHandle));
    }
    return interactionClass;
  }

  public InteractionClass getInteractionClass(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined
  {
    InteractionClass interactionClass = interactionClasses.get(interactionClassHandle);
    if (interactionClass == null)
    {
      throw new InteractionClassNotDefined(I18n.getMessage(
        ExceptionMessages.INTERACTION_CLASS_NOT_DEFINED, interactionClassHandle));
    }
    return interactionClass;
  }

  public InteractionClass getInteractionClassSafely(InteractionClassHandle interactionClassHandle)
  {
    InteractionClass interactionClass = interactionClasses.get(interactionClassHandle);
    assert interactionClass != null;
    return interactionClass;
  }

  public InteractionClass getInteractionClass(String interactionClassName)
    throws NameNotFound
  {
    if (interactionClassName.startsWith(HLA_INTERACTION_ROOT_PREFIX))
    {
      // strip off the root prefix
      //
      interactionClassName = interactionClassName.substring(HLA_INTERACTION_ROOT_PREFIX.length());
    }

    InteractionClass interactionClass = interactionClassesByName.get(interactionClassName);
    if (interactionClass == null)
    {
      throw new NameNotFound(I18n.getMessage(ExceptionMessages.INTERACTION_CLASS_NAME_NOT_FOUND, interactionClassName));
    }
    return interactionClass;
  }

  public InteractionClassHandle getInteractionClassHandle(String interactionClassName)
    throws NameNotFound
  {
    return getInteractionClass(interactionClassName).getInteractionClassHandle();
  }

  public String getInteractionClassName(InteractionClassHandle interactionClassHandle)
    throws InvalidInteractionClassHandle
  {
    return getValidInteractionClass(interactionClassHandle).getInteractionClassName();
  }

  public Parameter getParameter(InteractionClassHandle interactionClassHandle, String parameterName)
    throws NameNotFound, InteractionClassNotDefined
  {
    return getInteractionClass(interactionClassHandle).getParameter(parameterName);
  }

  public Parameter getParameter(InteractionClassHandle interactionClassHandle, ParameterHandle parameterHandle)
    throws InteractionParameterNotDefined, InteractionClassNotDefined
  {
    return getInteractionClass(interactionClassHandle).getParameter(parameterHandle);
  }

  public ParameterHandle getParameterHandle(InteractionClassHandle interactionClassHandle, String parameterName)
    throws NameNotFound, InvalidInteractionClassHandle
  {
    return getValidInteractionClass(interactionClassHandle).getParameter(parameterName).getParameterHandle();
  }

  public String getParameterName(InteractionClassHandle interactionClassHandle, ParameterHandle parameterHandle)
    throws InvalidParameterHandle, InvalidInteractionClassHandle
  {
    return getValidInteractionClass(interactionClassHandle).getValidParameter(parameterHandle).getParameterName();
  }

  public Dimension getDimension(String dimensionName)
    throws NameNotFound
  {
    Dimension dimension = dimensionsByName.get(dimensionName);
    if (dimension == null)
    {
      throw new NameNotFound(I18n.getMessage(ExceptionMessages.DIMENSION_NAME_NOT_FOUND, dimensionName));
    }
    return dimension;
  }

  public Dimension getDimensionSafely(String dimensionName)
  {
    Dimension dimension = dimensionsByName.get(dimensionName);
    assert dimension != null;
    return dimension;
  }

  public Dimension getDimension(DimensionHandle dimensionHandle)
    throws InvalidDimensionHandle
  {
    Dimension dimension = dimensions.get(dimensionHandle);
    if (dimension == null)
    {
      throw new InvalidDimensionHandle(I18n.getMessage(ExceptionMessages.INVALID_DIMENSION_HANDLE, dimensionHandle));
    }
    return dimension;
  }

  public Dimension getDimensionSafely(DimensionHandle dimensionHandle)
  {
    Dimension dimension = dimensions.get(dimensionHandle);
    assert dimension != null;
    return dimension;
  }

  public Collection<Dimension> getDimensions(DimensionHandleSet dimensionHandles)
    throws InvalidDimensionHandle
  {
    Collection<Dimension> dimensions = new ArrayList<Dimension>(dimensionHandles.size());

    for (DimensionHandle dimensionHandle : dimensionHandles)
    {
      dimensions.add(getDimension(dimensionHandle));
    }
    return dimensions;
  }

  public DimensionHandle getDimensionHandle(String name)
    throws NameNotFound
  {
    return getDimension(name).getDimensionHandle();
  }

  public String getDimensionName(DimensionHandle dimensionHandle)
    throws InvalidDimensionHandle
  {
    return getDimension(dimensionHandle).getDimensionName();
  }

  public long getDimensionUpperBound(DimensionHandle dimensionHandle)
    throws InvalidDimensionHandle
  {
    return getDimension(dimensionHandle).getUpperBound();
  }

  public DimensionHandleSet getAvailableDimensionsForClassAttribute(
    ObjectClassHandle objectClassHandle, AttributeHandle attributeHandle)
    throws InvalidObjectClassHandle, InvalidAttributeHandle, AttributeNotDefined
  {
    return getValidObjectClass(objectClassHandle).getValidAttribute(attributeHandle).getDimensionHandles();
  }

  public DimensionHandleSet getAvailableDimensionsForInteractionClass(InteractionClassHandle interactionClassHandle)
    throws InvalidInteractionClassHandle
  {
    return getValidInteractionClass(interactionClassHandle).getDimensionHandles();
  }

  public void changeInteractionOrderType(InteractionClassHandle interactionClassHandle, OrderType orderType)
    throws InteractionClassNotDefined
  {
    getInteractionClass(interactionClassHandle).setOrderType(orderType);
  }

  public void changeInteractionTransportationType(
    InteractionClassHandle interactionClassHandle, TransportationTypeHandle transportationTypeHandle)
    throws InteractionClassNotDefined
  {
    getInteractionClass(interactionClassHandle).setTransportationTypeHandle(transportationTypeHandle);
  }

  public TransportationTypeHandle getTransportationTypeHandle(String transportationTypeName)
    throws InvalidTransportationName
  {
    TransportationType transportationType = transportationTypesByName.get(transportationTypeName);
    if (transportationType == null)
    {
      throw new InvalidTransportationName(I18n.getMessage(
        ExceptionMessages.INVALID_TRANSPORTATION_NAME, transportationTypeName));
    }
    return transportationType.getTransportationTypeHandle();
  }

  public String getTransportationTypeName(TransportationTypeHandle transportationTypeHandle)
    throws InvalidTransportationType
  {
    TransportationType transportationType = transportationTypes.get(transportationTypeHandle);
    if (transportationType == null)
    {
      throw new InvalidTransportationType(I18n.getMessage(
        ExceptionMessages.INVALID_TRANSPORTATION_TYPE_HANDLE, transportationTypeHandle));
    }
    return transportationType.getName();
  }

  public OrderType getOrderType(String orderName)
    throws InvalidOrderName
  {
    OrderType orderType = orderTypeNames.inverse().get(orderName);
    if (orderType == null)
    {
      throw new InvalidOrderName(I18n.getMessage(ExceptionMessages.INVALID_ORDER_NAME, orderName));
    }
    return orderType;
  }

  public String getOrderName(OrderType orderType)
    throws InvalidOrderType
  {
    String orderName = orderTypeNames.get(orderType);
    if (orderName == null)
    {
      throw new InvalidOrderType(I18n.getMessage(ExceptionMessages.INVALID_ORDER_TYPE, orderType));
    }
    return orderName;
  }

  public void checkIfObjectClassNotDefined(ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined
  {
    getObjectClass(objectClassHandle);
  }

  public void checkIfAttributeNotDefined(ObjectClassHandle objectClassHandle, AttributeHandle attributeHandle)
    throws ObjectClassNotDefined, AttributeNotDefined
  {
    getObjectClass(objectClassHandle).getAttribute(attributeHandle);
  }

  public void checkIfAttributeNotDefined(ObjectClassHandle objectClassHandle, Set<AttributeHandle> attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined
  {
    ObjectClass objectClass = getObjectClass(objectClassHandle);
    for (AttributeHandle attributeHandle : attributeHandles)
    {
      objectClass.getAttribute(attributeHandle);
    }
  }

  public void checkIfInteractionParameterNotDefined(
    InteractionClassHandle interactionClassHandle, Set<ParameterHandle> parameterHandles)
    throws InteractionClassNotDefined, InteractionParameterNotDefined
  {
    InteractionClass interactionClass = getInteractionClass(interactionClassHandle);
    for (ParameterHandle parameterHandle : parameterHandles)
    {
      interactionClass.getParameter(parameterHandle);
    }
  }

  public void checkIfInteractionClassNotDefined(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined
  {
    getInteractionClass(interactionClassHandle);
  }

  public void checkIfInvalidDimensionHandle(Set<DimensionHandle> dimensionHandles)
    throws InvalidDimensionHandle
  {
    for (DimensionHandle dimensionHandle : dimensionHandles)
    {
      getDimension(dimensionHandle);
    }
  }

  public void checkIfInvalidRangeBound(DimensionHandle dimensionHandle, RangeBounds rangeBounds)
    throws InvalidRangeBound
  {
    try
    {
      getDimension(dimensionHandle).validate(rangeBounds);
    }
    catch (InvalidDimensionHandle idh)
    {
      throw new InvalidRangeBound(idh.getMessage(), idh);
    }
  }

  public void checkIfInvalidTransportationType(TransportationTypeHandle transportationTypeHandle)
    throws InvalidTransportationType
  {
    getTransportationTypeName(transportationTypeHandle);
  }

  public OHLAProtos.FDD.Builder toProto()
  {
    OHLAProtos.FDD.Builder fdd = OHLAProtos.FDD.newBuilder().addAllSources(sources);

    for (ObjectClass objectClass : objectClasses.values())
    {
      fdd.addObjectClasses(objectClass.toProto());
    }

    for (InteractionClass interactionClass : interactionClasses.values())
    {
      fdd.addInteractionClasses(interactionClass.toProto());
    }

    for (Dimension dimension : dimensions.values())
    {
      fdd.addDimensions(dimension.toProto());
    }

    fdd.setFed(fed.toProto());

    return fdd;
  }

  @Override
  protected FDD clone()
  {
    return new FDD(toProto().build());
  }

  private DimensionHandleSet getDimensionHandles(Set<String> dimensions)
  {
    DimensionHandleSet dimensionHandles;
    if (dimensions.isEmpty())
    {
      dimensionHandles = IEEE1516eDimensionHandleSet.EMPTY;
    }
    else
    {
      dimensionHandles = IEEE1516eDimensionHandleSetFactory.INSTANCE.create();
      for (String dimensionName : dimensions)
      {
        dimensionHandles.add(addDimension(dimensionName).getDimensionHandle());
      }
    }
    return dimensionHandles;
  }
}
