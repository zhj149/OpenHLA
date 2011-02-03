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

import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import net.sf.ohla.rti.Protocol;
import net.sf.ohla.rti.fed.FED;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandleSet;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eInteractionClassHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eObjectClassHandle;

import org.jboss.netty.buffer.ChannelBuffer;

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
import hla.rti1516e.exceptions.ErrorReadingFDD;
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
{
  public static final String HLA_OBJECT_ROOT = "HLAobjectRoot";
  public static final String HLA_PRIVILEGE_TO_DELETE_OBJECT = "HLAprivilegeToDeleteObject";

  public static final String HLA_INTERACTION_ROOT = "HLAinteractionRoot";

  public static final String HLA_OBJECT_ROOT_PREFIX = HLA_OBJECT_ROOT + ".";
  public static final String HLA_INTERACTION_ROOT_PREFIX = HLA_INTERACTION_ROOT + ".";

  private static final Map<OrderType, String> orderTypeNames = new EnumMap<OrderType, String>(OrderType.class);
  private static final Map<String, OrderType> orderTypesByName = new HashMap<String, OrderType>();

  static
  {
    orderTypeNames.put(OrderType.TIMESTAMP, "TimeStamp");
    orderTypeNames.put(OrderType.RECEIVE, "Receive");

    orderTypesByName.put("TimeStamp", OrderType.TIMESTAMP);
    orderTypesByName.put("Receive", OrderType.RECEIVE);
  }

  private final List<String> sources = new LinkedList<String>();

  private final Map<ObjectClassHandle, ObjectClass> objectClasses = new HashMap<ObjectClassHandle, ObjectClass>();
  private final Map<String, ObjectClass> objectClassesByName = new HashMap<String, ObjectClass>();

  private final Map<InteractionClassHandle, InteractionClass> interactionClasses =
    new HashMap<InteractionClassHandle, InteractionClass>();
  private final Map<String, InteractionClass> interactionClassesByName =
    new HashMap<String, InteractionClass>();

  private final Map<DimensionHandle, Dimension> dimensions = new HashMap<DimensionHandle, Dimension>();
  private final Map<String, Dimension> dimensionsByName = new HashMap<String, Dimension>();

  private final Map<TransportationTypeHandle, TransportationType> transportationTypes =
    new HashMap<TransportationTypeHandle, TransportationType>();
  private final Map<String, TransportationType> transportationTypesByName = new HashMap<String, TransportationType>();

  private final FED fed;

  public FDD(URL source)
  {
    this(source, null);
  }

  public FDD(URL source, FED fed)
  {
    sources.add(source.toString());

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

  public FDD(ChannelBuffer buffer)
  {
    transportationTypes.put(
      TransportationType.HLA_RELIABLE.getTransportationTypeHandle(), TransportationType.HLA_RELIABLE);
    transportationTypesByName.put(
      TransportationType.HLA_RELIABLE.getName(), TransportationType.HLA_RELIABLE);

    transportationTypes.put(
      TransportationType.HLA_BEST_EFFORT.getTransportationTypeHandle(), TransportationType.HLA_BEST_EFFORT);
    transportationTypesByName.put(
      TransportationType.HLA_BEST_EFFORT.getName(), TransportationType.HLA_BEST_EFFORT);

    for (int sourceCount = Protocol.decodeVarInt(buffer); sourceCount > 0; sourceCount--)
    {
      sources.add(Protocol.decodeString(buffer));
    }

    for (int dimensionCount = Protocol.decodeVarInt(buffer); dimensionCount > 0; dimensionCount--)
    {
      Dimension dimension = Dimension.decode(buffer);

      dimensions.put(dimension.getDimensionHandle(), dimension);
      dimensionsByName.put(dimension.getDimensionName(), dimension);
    }

    for (int objectClassCount = Protocol.decodeVarInt(buffer); objectClassCount > 0; objectClassCount--)
    {
      ObjectClass objectClass = ObjectClass.decode(buffer, objectClasses);

      objectClasses.put(objectClass.getObjectClassHandle(), objectClass);
      objectClassesByName.put(objectClass.getObjectClassName(), objectClass);
    }

    for (int interactionClassCount = Protocol.decodeVarInt(buffer); interactionClassCount > 0; interactionClassCount--)
    {
      InteractionClass interactionClass = InteractionClass.decode(buffer, interactionClasses);

      interactionClasses.put(interactionClass.getInteractionClassHandle(), interactionClass);
      interactionClassesByName.put(interactionClass.getInteractionClassName(), interactionClass);
    }

    fed = FED.decode(buffer, this);
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

  public void merge(FDD fdd)
    throws InconsistentFDD
  {
  }

  public void merge(Collection<FDD> fdds)
    throws InconsistentFDD
  {
  }

  public ObjectClass addObjectClass(String objectClassName, ObjectClass superObjectClass)
    throws ErrorReadingFDD
  {
    if (superObjectClass == null)
    {
      // only one object class has no root

      if (!HLA_OBJECT_ROOT.equals(objectClassName))
      {
        throw new ErrorReadingFDD(objectClassName + " does not have parent ObjectClass");
      }
    }
    else if (!HLA_OBJECT_ROOT.equals(superObjectClass.getObjectClassName()))
    {
      objectClassName = superObjectClass.getObjectClassName() + "." + objectClassName;
    }

    if (objectClassesByName.containsKey(objectClassName))
    {
      throw new ErrorReadingFDD(objectClassName + " already exists");
    }

    ObjectClassHandle objectClassHandle = new IEEE1516eObjectClassHandle(objectClasses.size() + 1);

    ObjectClass objectClass = new ObjectClass(objectClassHandle, objectClassName, superObjectClass);

    objectClasses.put(objectClassHandle, objectClass);
    objectClassesByName.put(objectClassName, objectClass);

    return objectClass;
  }

  public Attribute addAttribute(
    ObjectClass objectClass, String name, Set<String> dimensions, String transportationTypeName, String orderTypeName)
    throws ErrorReadingFDD
  {
    return addAttribute(objectClass, name, getDimensionHandles(dimensions), transportationTypeName, orderTypeName);
  }

  public Attribute addAttribute(
    ObjectClass objectClass, String name, DimensionHandleSet dimensionHandles,
    String transportationTypeName, String orderTypeName)
    throws ErrorReadingFDD
  {
    TransportationType transportationType = transportationTypesByName.get(transportationTypeName);
    if (transportationType == null)
    {
      transportationType = TransportationType.HLA_RELIABLE;
    }

    OrderType orderType = orderTypesByName.get(orderTypeName);
    if (orderType == null)
    {
      orderType = OrderType.RECEIVE;
    }

    return objectClass.addAttribute(
      name, dimensionHandles, transportationType.getTransportationTypeHandle(), orderType);
  }

  public InteractionClass addInteractionClass(
    String name, InteractionClass superInteractionClass, Set<String> dimensions,
    String transportationTypeName, String orderTypeName)
    throws ErrorReadingFDD
  {
    return addInteractionClass(
      name, superInteractionClass, getDimensionHandles(dimensions), transportationTypeName, orderTypeName);
  }

  public InteractionClass addInteractionClass(
    String interactionClassName, InteractionClass superInteractionClass, DimensionHandleSet dimensionHandles,
    String transportationTypeName, String orderTypeName)
    throws ErrorReadingFDD
  {
    if (superInteractionClass == null)
    {
      // only one interaction class has no root

      if (!HLA_INTERACTION_ROOT.equals(interactionClassName))
      {
        throw new ErrorReadingFDD(interactionClassName + " does not have parent InteractionClass");
      }
    }
    else if (!HLA_INTERACTION_ROOT.equals(superInteractionClass.getInteractionClassName()))
    {
      interactionClassName = superInteractionClass.getInteractionClassName() + "." + interactionClassName;
    }

    if (interactionClassesByName.containsKey(interactionClassName))
    {
      throw new ErrorReadingFDD(interactionClassName + " already exists");
    }

    TransportationType transportationType = transportationTypesByName.get(transportationTypeName);
    if (transportationType == null)
    {
      transportationType = TransportationType.HLA_RELIABLE;
    }

    OrderType orderType = orderTypesByName.get(orderTypeName);

    InteractionClassHandle interactionClassHandle =
      new IEEE1516eInteractionClassHandle(interactionClasses.size() + 1);

    InteractionClass interactionClass = new InteractionClass(
      interactionClassHandle, interactionClassName, superInteractionClass, dimensionHandles,
      transportationType.getTransportationTypeHandle(), orderType);

    interactionClasses.put(interactionClassHandle, interactionClass);
    interactionClassesByName.put(interactionClassName, interactionClass);
    return interactionClass;
  }

  public Parameter addParameter(InteractionClass interactionClass, String name)
    throws ErrorReadingFDD
  {
    return interactionClass.addParameter(name);
  }

  public Dimension addDimension(String dimensionName)
  {
    Dimension dimension = dimensionsByName.get(dimensionName);
    if (dimension == null)
    {
      DimensionHandle dimensionHandle = new IEEE1516eDimensionHandle(this.dimensions.size() + 1);
      dimension = new Dimension(dimensionHandle, dimensionName);

      dimensions.put(dimensionHandle, dimension);
      dimensionsByName.put(dimensionName, dimension);
    }
    return dimension;
  }

  public ObjectClass getObjectClass(ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined
  {
    ObjectClass objectClass = objectClasses.get(objectClassHandle);
    if (objectClass == null)
    {
      throw new ObjectClassNotDefined(String.format("object class not defined: %s", objectClassHandle));
    }
    return objectClass;
  }

  public ObjectClass getObjectClassSafely(ObjectClassHandle objectClassHandle)
  {
    ObjectClass objectClass = objectClasses.get(objectClassHandle);
    assert objectClass != null;
    return objectClass;
  }

  public ObjectClass getObjectClass(String name)
    throws NameNotFound
  {
    if (name.startsWith(HLA_OBJECT_ROOT_PREFIX))
    {
      // strip off the root prefix
      //
      name = name.substring(HLA_OBJECT_ROOT_PREFIX.length());
    }

    ObjectClass objectClass = objectClassesByName.get(name);
    if (objectClass == null)
    {
      throw new NameNotFound(String.format("object class name not found: %s", name));
    }
    return objectClass;
  }

  public ObjectClassHandle getObjectClassHandle(String name)
    throws NameNotFound
  {
    return getObjectClass(name).getObjectClassHandle();
  }

  public String getObjectClassName(ObjectClassHandle objectClassHandle)
    throws InvalidObjectClassHandle
  {
    try
    {
      return getObjectClass(objectClassHandle).getObjectClassName();
    }
    catch (ObjectClassNotDefined ocnd)
    {
      throw new InvalidObjectClassHandle(String.format("invalid object class handle: %s", objectClassHandle), ocnd);
    }
  }

  public Attribute getAttribute(ObjectClassHandle objectClassHandle, String name)
    throws NameNotFound, ObjectClassNotDefined
  {
    return getObjectClass(objectClassHandle).getAttribute(name);
  }

  public Attribute getAttribute(ObjectClassHandle objectClassHandle, AttributeHandle attributeHandle)
    throws AttributeNotDefined, ObjectClassNotDefined
  {
    return getObjectClass(objectClassHandle).getAttribute(attributeHandle);
  }

  public AttributeHandle getAttributeHandle(ObjectClassHandle objectClassHandle, String name)
    throws NameNotFound, InvalidObjectClassHandle
  {
    try
    {
      return getAttribute(objectClassHandle, name).getAttributeHandle();
    }
    catch (ObjectClassNotDefined ocnd)
    {
      throw new InvalidObjectClassHandle(String.format("invalid object class handle: %s", objectClassHandle), ocnd);
    }
  }

  public String getAttributeName(ObjectClassHandle objectClassHandle, AttributeHandle attributeHandle)
    throws InvalidAttributeHandle, InvalidObjectClassHandle
  {
    try
    {
      return getAttribute(objectClassHandle, attributeHandle).getAttributeName();
    }
    catch (AttributeNotDefined and)
    {
      throw new InvalidAttributeHandle(String.format("invalid attribute handle: %s", attributeHandle), and);
    }
    catch (ObjectClassNotDefined ocnd)
    {
      throw new InvalidObjectClassHandle(String.format("invalid object class handle: %s", objectClassHandle), ocnd);
    }
  }

  public InteractionClass getInteractionClass(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined
  {
    InteractionClass interactionClass = interactionClasses.get(interactionClassHandle);
    if (interactionClass == null)
    {
      throw new InteractionClassNotDefined(String.format("interaction class not defined: %s", interactionClassHandle));
    }
    return interactionClass;
  }

  public InteractionClass getInteractionClassSafely(InteractionClassHandle interactionClassHandle)
  {
    InteractionClass interactionClass = interactionClasses.get(interactionClassHandle);
    assert interactionClass != null;
    return interactionClass;
  }

  public InteractionClass getInteractionClass(String name)
    throws NameNotFound
  {
    if (name.startsWith(HLA_INTERACTION_ROOT_PREFIX))
    {
      // strip off the root prefix
      //
      name = name.substring(HLA_INTERACTION_ROOT_PREFIX.length());
    }

    InteractionClass interactionClass = interactionClassesByName.get(name);
    if (interactionClass == null)
    {
      throw new NameNotFound(String.format("interaction class name not found: %s", name));
    }
    return interactionClass;
  }

  public InteractionClassHandle getInteractionClassHandle(String name)
    throws NameNotFound
  {
    return getInteractionClass(name).getInteractionClassHandle();
  }

  public String getInteractionClassName(InteractionClassHandle interactionClassHandle)
    throws InvalidInteractionClassHandle
  {
    try
    {
      return getInteractionClass(interactionClassHandle).getInteractionClassName();
    }
    catch (InteractionClassNotDefined icnd)
    {
      throw new InvalidInteractionClassHandle(
        String.format("invalid interaction class handle: %s", interactionClassHandle), icnd);
    }
  }

  public Parameter getParameter(InteractionClassHandle interactionClassHandle, String name)
    throws NameNotFound, InteractionClassNotDefined
  {
    return getInteractionClass(interactionClassHandle).getParameter(name);
  }

  public Parameter getParameter(InteractionClassHandle interactionClassHandle, ParameterHandle parameterHandle)
    throws InteractionParameterNotDefined, InteractionClassNotDefined
  {
    return getInteractionClass(interactionClassHandle).getParameter(parameterHandle);
  }

  public ParameterHandle getParameterHandle(
    InteractionClassHandle interactionClassHandle, String name)
    throws NameNotFound, InvalidInteractionClassHandle
  {
    try
    {
      return getParameter(interactionClassHandle, name).getParameterHandle();
    }
    catch (InteractionClassNotDefined icnd)
    {
      throw new InvalidInteractionClassHandle(
        String.format("invalid interaction class handle: %s", interactionClassHandle), icnd);
    }
  }

  public String getParameterName(InteractionClassHandle interactionClassHandle, ParameterHandle parameterHandle)
    throws InvalidParameterHandle, InvalidInteractionClassHandle
  {
    try
    {
      return getParameter(interactionClassHandle, parameterHandle).getParameterName();
    }
    catch (InteractionParameterNotDefined ipnd)
    {
      throw new InvalidParameterHandle(String.format("invalid parameter handle: %s", parameterHandle), ipnd);
    }
    catch (InteractionClassNotDefined icnd)
    {
      throw new InvalidInteractionClassHandle(
        String.format("invalid interaction class handle: %s", interactionClassHandle), icnd);
    }
  }

  public Dimension getDimension(String name)
    throws NameNotFound
  {
    Dimension dimension = dimensionsByName.get(name);
    if (dimension == null)
    {
      throw new NameNotFound(String.format("dimension name not found: %s", name));
    }
    return dimension;
  }

  public Dimension getDimension(DimensionHandle dimensionHandle)
    throws InvalidDimensionHandle
  {
    Dimension dimension = dimensions.get(dimensionHandle);
    if (dimension == null)
    {
      throw new InvalidDimensionHandle(String.format("invalid dimension handle: %s", dimensionHandle));
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
    try
    {
      return getAttribute(objectClassHandle, attributeHandle).getDimensionHandles();
    }
    catch (ObjectClassNotDefined ocnd)
    {
      throw new InvalidObjectClassHandle(
        String.format("invalid object class handle: %s", objectClassHandle), ocnd);
    }
  }

  public DimensionHandleSet getAvailableDimensionsForInteractionClass(
    InteractionClassHandle interactionClassHandle, DimensionHandleSet dimensionHandles)
    throws InvalidInteractionClassHandle
  {
    try
    {
      return getInteractionClass(interactionClassHandle).getDimensionHandles();
    }
    catch (InteractionClassNotDefined icnd)
    {
      throw new InvalidInteractionClassHandle(
        String.format("invalid interaction class handle: %s", interactionClassHandle));
    }
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

  public TransportationTypeHandle getTransportationTypeHandle(String name)
    throws InvalidTransportationName
  {
    TransportationType transportationType = transportationTypesByName.get(name);
    if (transportationType == null)
    {
      throw new InvalidTransportationName(name);
    }
    return transportationType.getTransportationTypeHandle();
  }

  public String getTransportationTypeName(TransportationTypeHandle transportationTypeHandle)
    throws InvalidTransportationType
  {
    TransportationType transportationType = transportationTypes.get(transportationTypeHandle);
    if (transportationType == null)
    {
      throw new InvalidTransportationType(transportationTypeHandle.toString());
    }
    return transportationType.getName();
  }

  public OrderType getOrderType(String name)
    throws InvalidOrderName
  {
    OrderType orderType = orderTypesByName.get(name);
    if (orderType == null)
    {
      throw new InvalidOrderName(name);
    }
    return orderType;
  }

  public String getOrderName(OrderType orderType)
    throws InvalidOrderType
  {
    String name = orderTypeNames.get(orderType);
    if (name == null)
    {
      throw new InvalidOrderType(orderType.toString());
    }
    return name;
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
      throw new InvalidRangeBound("", idh);
    }
  }

  public void checkIfInvalidTransportationType(TransportationTypeHandle transportationTypeHandle)
    throws InvalidTransportationType
  {
    getTransportationTypeName(transportationTypeHandle);
  }

  private Dimension addDimension(DimensionHandle dimensionHandle, String dimensionName)
  {
    if (dimensionHandle == null)
    {
      dimensionHandle = new IEEE1516eDimensionHandle(this.dimensions.size() + 1);
    }

    Dimension dimension = new Dimension(dimensionHandle, dimensionName);

    dimensions.put(dimensionHandle, dimension);
    dimensionsByName.put(dimensionName, dimension);

    return dimension;
  }

  private DimensionHandleSet getDimensionHandles(Set<String> dimensions)
  {
    DimensionHandleSet dimensionHandles;
    if (dimensions == null || dimensions.isEmpty())
    {
      dimensionHandles = null;
    }
    else
    {
      dimensionHandles = new IEEE1516eDimensionHandleSet();

      for (String dimensionName : dimensions)
      {
        dimensionHandles.add(addDimension(dimensionName).getDimensionHandle());
      }
    }
    return dimensionHandles;
  }

  public static void encode(ChannelBuffer buffer, FDD fdd)
  {
    Protocol.encodeVarInt(buffer, fdd.sources.size());
    for (String source : fdd.sources)
    {
      Protocol.encodeString(buffer, source);
    }

    Protocol.encodeVarInt(buffer, fdd.dimensions.size());
    for (Dimension dimension : fdd.dimensions.values())
    {
      Dimension.encode(buffer, dimension);
    }

    // sort by handle so all parents are before their children
    //
    SortedMap<ObjectClassHandle, ObjectClass> sortedObjectClasses =
      new TreeMap<ObjectClassHandle, ObjectClass>(fdd.objectClasses);
    Protocol.encodeVarInt(buffer, fdd.objectClasses.size());
    for (ObjectClass objectClass : sortedObjectClasses.values())
    {
      ObjectClass.encode(buffer, objectClass);
    }

    // sort by handle so all parents are before their children
    //
    SortedMap<InteractionClassHandle, InteractionClass> sortedInteractionClasses =
      new TreeMap<InteractionClassHandle, InteractionClass>(fdd.interactionClasses);
    Protocol.encodeVarInt(buffer, fdd.interactionClasses.size());
    for (InteractionClass interactionClass : sortedInteractionClasses.values())
    {
      InteractionClass.encode(buffer, interactionClass);
    }

    FED.encode(buffer, fdd.fed);
  }

  public static FDD decode(ChannelBuffer buffer)
  {
    return new FDD(buffer);
  }

  public static void encodeList(ChannelBuffer buffer, List<FDD> fdds)
  {
    if (fdds == null || fdds.isEmpty())
    {
      Protocol.encodeVarInt(buffer, 0);
    }
    else
    {
      Protocol.encodeVarInt(buffer, fdds.size());
      for (FDD fdd : fdds)
      {
        encode(buffer, fdd);
      }
    }
  }

  public static List<FDD> decodeList(ChannelBuffer buffer)
  {
    List<FDD> fdds;

    int size = Protocol.decodeVarInt(buffer);
    if (size == 0)
    {
      fdds = Collections.emptyList();
    }
    else
    {
      fdds = new ArrayList<FDD>(size);
      for (; size > 0; size--)
      {
        fdds.add(FDD.decode(buffer));
      }
    }
    return fdds;
  }
}
