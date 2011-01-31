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

package net.sf.ohla.rti.fed;

import java.net.URL;

import java.util.HashMap;
import java.util.Map;

import net.sf.ohla.rti.fdd.Attribute;
import net.sf.ohla.rti.fdd.Dimension;
import net.sf.ohla.rti.fdd.FDD;
import net.sf.ohla.rti.fdd.InteractionClass;
import net.sf.ohla.rti.fdd.ObjectClass;

import hla.rti.AttributeNotDefined;
import hla.rti.DimensionNotDefined;
import hla.rti.ErrorReadingFED;
import hla.rti.FederateNotExecutionMember;
import hla.rti.InteractionClassNotDefined;
import hla.rti.NameNotFound;
import hla.rti.ObjectClassNotDefined;
import hla.rti.RTIinternalError;
import hla.rti.SpaceNotDefined;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;

public class FED
{
  public static final String OBJECT_ROOT = "objectRoot";
  public static final String PRIVILEGE_TO_DELETE = "privilegeToDelete";

  public static final String INTERACTION_ROOT = "interactionRoot";

  public static final String RTI_PRIVATE= "RTIprivate";

  private final FDD fdd;

  private final Map<Integer, RoutingSpace> routingSpaces = new HashMap<Integer, RoutingSpace>();
  private final Map<String, RoutingSpace> routingSpacesByName = new HashMap<String, RoutingSpace>();

  private final Map<InteractionClassHandle, RoutingSpace> interactionClassRoutingSpaces =
    new HashMap<InteractionClassHandle, RoutingSpace>();

  private String fedName;

  public FED(URL source)
  {
    fdd = new FDD(source, this);
  }

  public FED(FDD fdd)
  {
    this.fdd = fdd;
  }

  public FDD getFDD()
  {
    return fdd;
  }

  public String getFEDName()
  {
    return fedName;
  }

  public void setFEDName(String fedName)
  {
    this.fedName = fedName;
  }

  public Attribute addAttribute(
    ObjectClass objectClass, String attributeName, String transportationTypeName, String orderTypeName,
    String routingSpaceName)
    throws ErrorReadingFED
  {
    RoutingSpace routingSpace = routingSpacesByName.get(routingSpaceName);
    if (routingSpace == null)
    {
      throw new ErrorReadingFED("unknown routing space: " + routingSpaceName + " for attribute " + attributeName);
    }

    Attribute attribute = fdd.addAttribute(
      objectClass, attributeName, routingSpace.getDimensionHandles(), transportationTypeName, orderTypeName);

    return attribute;
  }

  public InteractionClass addInteractionClass(
    InteractionClass superInteractionClass, String interactionClassName, String transportationTypeName, String orderTypeName,
    String routingSpaceName)
    throws ErrorReadingFED
  {
    RoutingSpace routingSpace = routingSpacesByName.get(routingSpaceName);
    if (routingSpace == null)
    {
      throw new ErrorReadingFED(
        "unknown routing space: " + routingSpaceName + " for interaction class " + interactionClassName);
    }

    InteractionClass interactionClass = fdd.addInteractionClass(
      interactionClassName, superInteractionClass, routingSpace.getDimensionHandles(),
      transportationTypeName, orderTypeName);

    return interactionClass;
  }

  public RoutingSpace addRoutingSpace(String routingSpaceName)
  {
    RoutingSpace routingSpace = routingSpacesByName.get(routingSpaceName);
    if (routingSpace == null)
    {
      routingSpace = new RoutingSpace(routingSpaceName, routingSpaces.size() + 1);

      routingSpaces.put(routingSpace.getRoutingSpaceHandle(), routingSpace);
      routingSpacesByName.put(routingSpaceName, routingSpace);
    }
    return routingSpace;
  }

  public Dimension addRoutingSpaceDimension(RoutingSpace routingSpace, String dimensionName)
  {
    Dimension dimension = fdd.addDimension(routingSpace.getName() + "." + dimensionName);

    routingSpace.addDimension(dimensionName, dimension);

    return dimension;
  }

  public void setTransport(Attribute attribute, String transportationTypeName)
  {
  }

  public void setTransport(InteractionClass interactionClass, String transportationTypeName)
  {
  }

  public void setOrder(Attribute attribute, String orderTypeName)
  {
  }

  public void setOrder(InteractionClass interactionClass, String orderTypeName)
  {
  }

  public void setRoutingSpace(Attribute attribute, String routingSpaceName)
  {
  }

  public void setRoutingSpace(InteractionClass interactionClass, String routingSpaceName)
  {
  }

  public RoutingSpace getRoutingSpace(String name)
    throws NameNotFound
  {
    RoutingSpace routingSpace = routingSpacesByName.get(name);
    if (routingSpace == null)
    {
      throw new NameNotFound(name);
    }
    return routingSpace;
  }

  public RoutingSpace getRoutingSpace(int routingSpaceHandle)
    throws SpaceNotDefined
  {
    RoutingSpace routingSpace = routingSpaces.get(routingSpaceHandle);
    if (routingSpace == null)
    {
      throw new SpaceNotDefined(Integer.toString(routingSpaceHandle));
    }
    return routingSpace;
  }

  public int getRoutingSpaceHandle(String name)
    throws NameNotFound
  {
    return getRoutingSpace(name).getRoutingSpaceHandle();
  }

  public String getRoutingSpaceName(int routingSpaceHandle)
    throws SpaceNotDefined
  {
    return getRoutingSpace(routingSpaceHandle).getName();
  }

  public int getDimensionHandle(String name, int routingSpaceHandle)
    throws SpaceNotDefined, NameNotFound
  {
    return getRoutingSpace(routingSpaceHandle).getDimensionHandle(name);
  }

  public String getDimensionName(int dimensionHandle, int routingSpaceHandle)
    throws SpaceNotDefined, DimensionNotDefined
  {
    return getRoutingSpace(routingSpaceHandle).getDimensionName(
      dimensionHandle);
  }

  public int getAttributeRoutingSpaceHandle(AttributeHandle attributeHandle, ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      Attribute attribute = fdd.getAttribute(objectClassHandle, attributeHandle);

      if (attribute instanceof FEDAttribute)
      {
        return ((FEDAttribute) attribute).getRoutingSpace().getRoutingSpaceHandle();
      }
      else
      {
        throw new RTIinternalError("HLA 1.3 routing unavailable: federation was not created from FED file");
      }
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516e.exceptions.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
  }

  public int getInteractionRoutingSpaceHandle(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      InteractionClass interactionClass = fdd.getInteractionClass(interactionClassHandle);

      if (interactionClass instanceof FEDInteractionClass)
      {
        return ((FEDInteractionClass) interactionClass).getRoutingSpace().getRoutingSpaceHandle();
      }
      else
      {
        throw new RTIinternalError("HLA 1.3 routing unavailable: federation was not created from FED file");
      }
    }
    catch (hla.rti1516e.exceptions.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
  }
}
