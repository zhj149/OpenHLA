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

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeNotDefined;
import hla.rti1516.CouldNotOpenFDD;
import hla.rti1516.DimensionHandle;
import hla.rti1516.DimensionHandleSet;
import hla.rti1516.ErrorReadingFDD;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.InteractionClassNotDefined;
import hla.rti1516.InteractionParameterNotDefined;
import hla.rti1516.InvalidAttributeHandle;
import hla.rti1516.InvalidDimensionHandle;
import hla.rti1516.InvalidInteractionClassHandle;
import hla.rti1516.InvalidObjectClassHandle;
import hla.rti1516.InvalidOrderName;
import hla.rti1516.InvalidOrderType;
import hla.rti1516.InvalidParameterHandle;
import hla.rti1516.InvalidRangeBound;
import hla.rti1516.InvalidTransportationName;
import hla.rti1516.InvalidTransportationType;
import hla.rti1516.NameNotFound;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectClassNotDefined;
import hla.rti1516.OrderType;
import static hla.rti1516.OrderType.RECEIVE;
import static hla.rti1516.OrderType.TIMESTAMP;
import hla.rti1516.ParameterHandle;
import hla.rti1516.RangeBounds;
import hla.rti1516.TransportationType;
import static hla.rti1516.TransportationType.HLA_BEST_EFFORT;
import static hla.rti1516.TransportationType.HLA_RELIABLE;

public class FDD
  implements Serializable
{
  public static final String HLA_OBJECT_ROOT_PREFIX =
    ObjectClass.HLA_OBJECT_ROOT + ".";
  public static final String HLA_INTERACTION_ROOT_PREFIX =
    InteractionClass.HLA_INTERACTION_ROOT + ".";

  protected static Map<OrderType, String> orderTypeNames =
    new EnumMap<OrderType, String>(OrderType.class);
  protected static Map<String, OrderType> orderTypesByName =
    new HashMap<String, OrderType>();

  static
  {
    orderTypeNames.put(TIMESTAMP, "TimeStamp");
    orderTypeNames.put(RECEIVE, "Receive");

    orderTypesByName.put("TimeStamp", TIMESTAMP);
    orderTypesByName.put("Receive", RECEIVE);
  }

  protected static Map<TransportationType, String> transportationTypeNames =
    new EnumMap<TransportationType, String>(TransportationType.class);
  protected static Map<String, TransportationType> transportationTypesByName =
    new HashMap<String, TransportationType>();

  static
  {
    transportationTypeNames.put(HLA_RELIABLE, "HLAreliable");
    transportationTypeNames.put(HLA_BEST_EFFORT, "HLAbestEffort");

    transportationTypesByName.put("HLAreliable", HLA_RELIABLE);
    transportationTypesByName.put("HLAbestEffort", HLA_BEST_EFFORT);
  }

  protected Map<ObjectClassHandle, ObjectClass> objectClasses =
    new HashMap<ObjectClassHandle, ObjectClass>();
  protected Map<String, ObjectClass> objectClassesByName =
    new HashMap<String, ObjectClass>();

  protected Map<InteractionClassHandle, InteractionClass> interactionClasses =
    new HashMap<InteractionClassHandle, InteractionClass>();
  protected Map<String, InteractionClass> interactionClassesByName =
    new HashMap<String, InteractionClass>();

  protected Map<DimensionHandle, Dimension> dimensions =
    new HashMap<DimensionHandle, Dimension>();
  protected Map<String, Dimension> dimensionsByName =
    new HashMap<String, Dimension>();

  protected FDD()
  {
  }

  protected FDD(FDD clonee)
  {
    objectClasses.putAll(clonee.objectClasses);
    objectClassesByName.putAll(clonee.objectClassesByName);

    interactionClasses.putAll(clonee.interactionClasses);
    interactionClassesByName.putAll(clonee.interactionClassesByName);

    dimensions.putAll(clonee.dimensions);
    dimensionsByName.putAll(clonee.dimensionsByName);
  }

  public FDD(URL url)
    throws CouldNotOpenFDD, ErrorReadingFDD
  {
    if (url == null)
    {
      throw new CouldNotOpenFDD(String.format("could not open FDD: %s", url));
    }

    try
    {
      process(new SAXReader().read(url.openStream()));
    }
    catch (IOException ioe)
    {
      throw new CouldNotOpenFDD("could not open FDD", ioe);
    }
    catch (DocumentException de)
    {
      throw new ErrorReadingFDD("error reading FDD", de);
    }
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

  public void add(ObjectClass objectClass)
  {
    objectClasses.put(objectClass.getObjectClassHandle(), objectClass);
    objectClassesByName.put(objectClass.getName(), objectClass);

    for (ObjectClass subClass : objectClass.getSubObjectClasses().values())
    {
      add(subClass);
    }
  }

  public void add(InteractionClass interactionClass)
  {
    interactionClasses.put(interactionClass.getInteractionClassHandle(),
                           interactionClass);
    interactionClassesByName.put(interactionClass.getName(), interactionClass);

    for (InteractionClass subInteraction :
      interactionClass.getSubInteractionClasses().values())
    {
      add(subInteraction);
    }
  }

  public void add(Dimension dimension)
  {
    this.dimensions.put(dimension.getDimensionHandle(), dimension);
    dimensionsByName.put(dimension.getName(), dimension);
  }

  public ObjectClass getObjectClass(ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined
  {
    ObjectClass objectClass = objectClasses.get(objectClassHandle);
    if (objectClass == null)
    {
      throw new ObjectClassNotDefined(
        String.format("object class not defined: %s", objectClassHandle));
    }
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
      throw new NameNotFound(
        String.format("object class name not found: %s", name));
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
      return getObjectClass(objectClassHandle).getName();
    }
    catch (ObjectClassNotDefined ocnd)
    {
      throw new InvalidObjectClassHandle(
        String.format("invalid object class handle: %s", objectClassHandle),
        ocnd);
    }
  }

  public Attribute getAttribute(ObjectClassHandle objectClassHandle,
                                String name)
    throws NameNotFound, ObjectClassNotDefined
  {
    return getObjectClass(objectClassHandle).getAttribute(name);
  }

  public Attribute getAttribute(ObjectClassHandle objectClassHandle,
                                AttributeHandle attributeHandle)
    throws AttributeNotDefined, ObjectClassNotDefined
  {
    return getObjectClass(objectClassHandle).getAttribute(attributeHandle);
  }

  public AttributeHandle getAttributeHandle(ObjectClassHandle objectClassHandle,
                                            String name)
    throws NameNotFound, InvalidObjectClassHandle
  {
    try
    {
      return getAttribute(objectClassHandle, name).getAttributeHandle();
    }
    catch (ObjectClassNotDefined ocnd)
    {
      throw new InvalidObjectClassHandle(
        String.format("invalid object class handle: %s", objectClassHandle),
        ocnd);
    }
  }

  public String getAttributeName(ObjectClassHandle objectClassHandle,
                                 AttributeHandle attributeHandle)
    throws InvalidAttributeHandle, InvalidObjectClassHandle
  {
    try
    {
      return getAttribute(objectClassHandle, attributeHandle).getName();
    }
    catch (AttributeNotDefined and)
    {
      throw new InvalidAttributeHandle(
        String.format("invalid attribute handle: ", attributeHandle), and);
    }
    catch (ObjectClassNotDefined ocnd)
    {
      throw new InvalidObjectClassHandle(
        String.format("invalid object class handle: %s", objectClassHandle),
        ocnd);
    }
  }

  public InteractionClass getInteractionClass(
    InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined
  {
    InteractionClass interactionClass =
      interactionClasses.get(interactionClassHandle);
    if (interactionClass == null)
    {
      throw new InteractionClassNotDefined(String.format(
        "interaction class not defined: %s", interactionClassHandle));
    }
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
      throw new NameNotFound(
        String.format("interaction class name not found: %s", name));
    }
    return interactionClass;
  }

  public InteractionClassHandle getInteractionClassHandle(String name)
    throws NameNotFound
  {
    return getInteractionClass(name).getInteractionClassHandle();
  }

  public String getInteractionClassName(
    InteractionClassHandle interactionClassHandle)
    throws InvalidInteractionClassHandle
  {
    try
    {
      return getInteractionClass(interactionClassHandle).getName();
    }
    catch (InteractionClassNotDefined icnd)
    {
      throw new InvalidInteractionClassHandle(
        String.format("invalid interaction class handle: %s",
                      interactionClassHandle), icnd);
    }
  }

  public Parameter getParameter(InteractionClassHandle interactionClassHandle,
                                String name)
    throws NameNotFound, InteractionClassNotDefined
  {
    return getInteractionClass(interactionClassHandle).getParameter(name);
  }

  public Parameter getParameter(InteractionClassHandle interactionClassHandle,
                                ParameterHandle parameterHandle)
    throws InteractionParameterNotDefined, InteractionClassNotDefined
  {
    return getInteractionClass(interactionClassHandle).getParameter(
      parameterHandle);
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
        String.format("invalid interaction class handle: %s",
                      interactionClassHandle), icnd);
    }
  }

  public String getParameterName(InteractionClassHandle interactionClassHandle,
                                 ParameterHandle parameterHandle)
    throws InvalidParameterHandle, InvalidInteractionClassHandle
  {
    try
    {
      return getParameter(interactionClassHandle, parameterHandle).getName();
    }
    catch (InteractionParameterNotDefined ipnd)
    {
      throw new InvalidParameterHandle(
        String.format("invalid parameter handle: %s", parameterHandle), ipnd);
    }
    catch (InteractionClassNotDefined icnd)
    {
      throw new InvalidInteractionClassHandle(
        String.format("invalid interaction class handle: ",
                      interactionClassHandle), icnd);
    }
  }

  public Dimension getDimension(String name)
    throws NameNotFound
  {
    Dimension dimension = dimensionsByName.get(name);
    if (dimension == null)
    {
      throw new NameNotFound(
        String.format("dimension name not found: %s", name));
    }
    return dimension;
  }

  public Dimension getDimension(DimensionHandle dimensionHandle)
    throws InvalidDimensionHandle
  {
    Dimension dimension = dimensions.get(dimensionHandle);
    if (dimension == null)
    {
      throw new InvalidDimensionHandle(
        String.format("invalid dimension handle: %s", dimensionHandle));
    }
    return dimension;
  }

  public Collection<Dimension> getDimensions(
    DimensionHandleSet dimensionHandles)
    throws InvalidDimensionHandle
  {
    Collection<Dimension> dimensions =
      new ArrayList<Dimension>(dimensionHandles.size());

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
    return getDimension(dimensionHandle).getName();
  }

  public RangeBounds getRangeBounds(DimensionHandle dimensionHandle)
    throws InvalidDimensionHandle
  {
    return getDimension(dimensionHandle).getRangeBounds();
  }

  public long getDimensionUpperBound(DimensionHandle dimensionHandle)
    throws InvalidDimensionHandle
  {
    return getRangeBounds(dimensionHandle).upper;
  }

  public DimensionHandleSet getAvailableDimensionsForClassAttribute(
    ObjectClassHandle objectClassHandle, AttributeHandle attributeHandle,
    DimensionHandleSet dimensionHandles)
    throws InvalidObjectClassHandle, InvalidAttributeHandle, AttributeNotDefined
  {
    try
    {
      for (String dimension :
        getAttribute(objectClassHandle, attributeHandle).getDimensions())
      {
        try
        {
          dimensionHandles.add(getDimensionHandle(dimension));
        }
        catch (NameNotFound nnf)
        {
        }
      }

      return dimensionHandles;
    }
    catch (ObjectClassNotDefined ocnd)
    {
      throw new InvalidObjectClassHandle(
        String.format("invalid object class handle: ", objectClassHandle),
        ocnd);
    }
  }

  public DimensionHandleSet getAvailableDimensionsForInteractionClass(
    InteractionClassHandle interactionClassHandle,
    DimensionHandleSet dimensionHandles)
    throws InvalidInteractionClassHandle
  {
    try
    {
      for (String dimension :
        getInteractionClass(interactionClassHandle).getDimensions())
      {
        try
        {
          dimensionHandles.add(getDimensionHandle(dimension));
        }
        catch (NameNotFound nnf)
        {
        }
      }

      return dimensionHandles;
    }
    catch (InteractionClassNotDefined icnd)
    {
      throw new InvalidInteractionClassHandle(
        String.format("invalid interaction class handle: %s",
                      interactionClassHandle));
    }
  }

  public void changeInteractionOrderType(InteractionClassHandle interactionClassHandle,
                           OrderType orderType)
    throws InteractionClassNotDefined
  {
    getInteractionClass(interactionClassHandle).setOrderType(orderType);
  }

  public void changeInteractionTransportationType(
    InteractionClassHandle interactionClassHandle,
    TransportationType transportationType)
    throws InteractionClassNotDefined
  {
    getInteractionClass(interactionClassHandle).setTransportationType(
      transportationType);
  }

  public TransportationType getTransportationType(String name)
    throws InvalidTransportationName
  {
    TransportationType transportationType = transportationTypesByName.get(name);
    if (transportationType == null)
    {
      throw new InvalidTransportationName(name);
    }
    return transportationType;
  }

  public String getTransportationName(TransportationType transportationType)
    throws InvalidTransportationType
  {
    String name = transportationTypeNames.get(transportationType);
    if (name == null)
    {
      throw new InvalidTransportationType(
        String.format("%s", transportationType));
    }
    return name;
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
      throw new InvalidOrderType(String.format("%s", orderType));
    }
    return name;
  }

  public void checkIfObjectClassNotDefined(ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined
  {
    getObjectClass(objectClassHandle);
  }

  public void checkIfAttributeNotDefined(ObjectClassHandle objectClassHandle,
                                         AttributeHandle attributeHandle)
    throws ObjectClassNotDefined, AttributeNotDefined
  {
    getObjectClass(objectClassHandle).getAttribute(attributeHandle);
  }

  public void checkIfAttributeNotDefined(ObjectClassHandle objectClassHandle,
                                         Set<AttributeHandle> attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined
  {
    ObjectClass objectClass = getObjectClass(objectClassHandle);
    for (AttributeHandle attributeHandle : attributeHandles)
    {
      objectClass.getAttribute(attributeHandle);
    }
  }

  public void checkIfInteractionParameterNotDefined(
    InteractionClassHandle interactionClassHandle,
    Set<ParameterHandle> parameterHandles)
    throws InteractionClassNotDefined, InteractionParameterNotDefined
  {
    InteractionClass interactionClass =
      getInteractionClass(interactionClassHandle);
    for (ParameterHandle parameterHandle : parameterHandles)
    {
      interactionClass.getParameter(parameterHandle);
    }
  }

  public void checkIfInteractionClassNotDefined(
    InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined
  {
    getInteractionClass(interactionClassHandle);
  }

  public void checkIfInvalidDimensionHandle(
    Set<DimensionHandle> dimensionHandles)
    throws InvalidDimensionHandle
  {
    for (DimensionHandle dimensionHandle : dimensionHandles)
    {
      getDimension(dimensionHandle);
    }
  }

  public void checkIfInvalidRangeBound(DimensionHandle dimensionHandle,
                                       RangeBounds rangeBounds)
    throws InvalidRangeBound
  {
    try
    {
      getDimension(dimensionHandle).validate(rangeBounds);
    }
    catch (InvalidDimensionHandle idh)
    {
      throw new InvalidRangeBound(idh);
    }
  }

  protected void process(Document fdd)
    throws ErrorReadingFDD
  {
    AtomicInteger objectClassCount = new AtomicInteger();
    AtomicInteger attributeCount = new AtomicInteger();

    ObjectClass hlaObjectRoot;

    // select all the HLA object root classes
    //
    List<Element> hlaObjectRoots = fdd.selectNodes(
      String.format("//objectModel/objects/objectClass[@name='%s']",
                    ObjectClass.HLA_OBJECT_ROOT));
    if (hlaObjectRoots.isEmpty())
    {
      // no HLA object root specified in the FDD
      //
      hlaObjectRoot =
        new ObjectClass(ObjectClass.HLA_OBJECT_ROOT, objectClassCount);
      hlaObjectRoot.add(new Attribute(
        Attribute.HLA_PRIVILEGE_TO_DELETE_OBJECT, attributeCount));
    }
    else if (hlaObjectRoots.size() == 1)
    {
      // the HLA object root is specified in the FDD
      //
      hlaObjectRoot = new ObjectClass(
        hlaObjectRoots.get(0), objectClassCount, attributeCount);
    }
    else
    {
      throw new ErrorReadingFDD(
        String.format("only 1 %s allowed", ObjectClass.HLA_OBJECT_ROOT));
    }

    add(hlaObjectRoot);

    List<Element> nonHLAObjectRoots = fdd.selectNodes(
      String.format("//objectModel/objects/objectClass[@name!='%s']",
                    ObjectClass.HLA_OBJECT_ROOT));
    for (Element objectClass : nonHLAObjectRoots)
    {
      // root object classes that are not the HLA object root will be subclassed
      // to the HLA object root
      //
      add(new ObjectClass(
        objectClass, hlaObjectRoot, objectClassCount, attributeCount));
    }

    AtomicInteger interactionClassCount = new AtomicInteger();
    AtomicInteger parameterCount = new AtomicInteger();

    InteractionClass hlaInteractionRoot;

    // select all the HLA interaction root classes
    //
    List<Element> hlaInteractionRoots = fdd.selectNodes(
      String.format("//objectModel/interactions/interactionClass[@name='%s']",
                    InteractionClass.HLA_INTERACTION_ROOT));
    if (hlaInteractionRoots.isEmpty())
    {
      // no HLA interaction root specified in the FDD
      //
      hlaInteractionRoot = new InteractionClass(
        InteractionClass.HLA_INTERACTION_ROOT, interactionClassCount);
    }
    else if (hlaInteractionRoots.size() == 1)
    {
      // the HLA interaction root is specified in the FDD
      //
      hlaInteractionRoot = new InteractionClass(
        hlaInteractionRoots.get(0), interactionClassCount, parameterCount);
    }
    else
    {
      throw new ErrorReadingFDD(String.format(
        "only 1 %s allowed", InteractionClass.HLA_INTERACTION_ROOT));
    }

    add(hlaInteractionRoot);

    List<Element> nonHLAInteractionRoots = fdd.selectNodes(
      String.format("//objectModel/interactions/interactionClass[@name='%s']",
                    InteractionClass.HLA_INTERACTION_ROOT));
    for (Element interactionClass : nonHLAInteractionRoots)
    {
      // root interaction classes that are not the HLA interaction root will be
      // subclassed to the HLA interaction root
      //
      add(new InteractionClass(interactionClass, hlaInteractionRoot,
                               interactionClassCount, parameterCount));
    }

    List<Element> dimensions =
      fdd.selectNodes("//objectModel/dimensions/dimension");
    for (Element dimension : dimensions)
    {
      add(new Dimension(dimension));
    }
  }
}
