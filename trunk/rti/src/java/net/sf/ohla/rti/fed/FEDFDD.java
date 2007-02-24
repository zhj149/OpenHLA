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

import java.util.HashMap;
import java.util.Map;

import net.sf.ohla.rti.fdd.Attribute;
import net.sf.ohla.rti.fdd.FDD;
import net.sf.ohla.rti.fdd.InteractionClass;

import hla.rti.AttributeNotDefined;
import hla.rti.DimensionNotDefined;
import hla.rti.FederateNotExecutionMember;
import hla.rti.InteractionClassNotDefined;
import hla.rti.NameNotFound;
import hla.rti.ObjectClassNotDefined;
import hla.rti.RTIinternalError;
import hla.rti.SpaceNotDefined;

import hla.rti1516.AttributeHandle;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.ObjectClassHandle;

public class FEDFDD
  extends FDD
{
  protected String fedName;
  protected String fedDIFVersionNumber;

  protected Map<Integer, RoutingSpace> routingSpaces =
    new HashMap<Integer, RoutingSpace>();
  protected Map<String, RoutingSpace> routingSpacesByName =
    new HashMap<String, RoutingSpace>();

  public FEDFDD()
  {
  }

  public FEDFDD(FDD clonee)
  {
    super(clonee);
  }

  public String getFEDName()
  {
    return fedName;
  }

  public void setFEDName(String fedName)
  {
    this.fedName = fedName;
  }

  public String getFEDDIFVersionNumber()
  {
    return fedDIFVersionNumber;
  }

  public void setFEDDIFVersionNumber(String fedDIFVersionNumber)
  {
    this.fedDIFVersionNumber = fedDIFVersionNumber;
  }

  public void add(RoutingSpace routingSpace)
  {
    routingSpaces.put(routingSpace.getRoutingSpaceHandle(), routingSpace);
    routingSpacesByName.put(routingSpace.getName(), routingSpace);
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

  public int getAttributeRoutingSpaceHandle(AttributeHandle attributeHandle,
                                            ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined, AttributeNotDefined,
           FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      Attribute attribute = getAttribute(objectClassHandle, attributeHandle);

      if (attribute instanceof FEDAttribute)
      {
        return ((FEDAttribute) attribute).getRoutingSpace().getRoutingSpaceHandle();
      }
      else
      {
        throw new RTIinternalError("HLA 1.3 routing unavailable: federation was not created from FED file");
      }
    }
    catch (hla.rti1516.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
  }

  public int getInteractionRoutingSpaceHandle(
    InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember,
           RTIinternalError
  {
    try
    {
      InteractionClass interactionClass =
        getInteractionClass(interactionClassHandle);

      if (interactionClass instanceof FEDInteractionClass)
      {
        return ((FEDInteractionClass) interactionClass).getRoutingSpace().getRoutingSpaceHandle();
      }
      else
      {
        throw new RTIinternalError("HLA 1.3 routing unavailable: federation was not created from FED file");
      }
    }
    catch (hla.rti1516.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
  }
}
