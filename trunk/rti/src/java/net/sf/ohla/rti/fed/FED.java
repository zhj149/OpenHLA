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

import net.sf.ohla.rti.Protocol;
import net.sf.ohla.rti.fdd.Attribute;
import net.sf.ohla.rti.fdd.Dimension;
import net.sf.ohla.rti.fdd.FDD;
import net.sf.ohla.rti.fdd.InteractionClass;
import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.fdd.Parameter;
import net.sf.ohla.rti.i18n.ExceptionMessages;
import net.sf.ohla.rti.i18n.I18n;

import org.jboss.netty.buffer.ChannelBuffer;

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
import hla.rti1516e.DimensionHandleSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.exceptions.InconsistentFDD;

public class FED
{
  public static final String OBJECT_ROOT = "objectRoot";
  public static final String PRIVILEGE_TO_DELETE = "privilegeToDelete";

  public static final String INTERACTION_ROOT = "interactionRoot";

  public static final String RTI_PRIVATE= "RTIprivate";

  private final FDD fdd;

  private final Map<Integer, RoutingSpace> routingSpaces = new HashMap<Integer, RoutingSpace>();
  private final Map<String, RoutingSpace> routingSpacesByName = new HashMap<String, RoutingSpace>();
  private final Map<Dimension, RoutingSpace> routingSpacesByDimension = new HashMap<Dimension, RoutingSpace>();

  private String fedName;

  public FED(URL source)
  {
    fdd = new FDD(source.toString(), this);
  }

  public FED(FDD fdd)
  {
    this.fdd = fdd;
  }

  public FED(ChannelBuffer buffer, FDD fdd)
  {
    this(fdd);

    fedName = Protocol.decodeOptionalString(buffer);

    for (int routingSpaceCount = Protocol.decodeVarInt(buffer); routingSpaceCount > 0; routingSpaceCount--)
    {
      RoutingSpace routingSpace = RoutingSpace.decode(buffer, fdd.getDimensions());

      routingSpaces.put(routingSpace.getRoutingSpaceHandle(), routingSpace);
      routingSpacesByName.put(routingSpace.getRoutingSpaceName(), routingSpace);
    }
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

  public ObjectClass addObjectClass(String objectClassName, ObjectClass superObjectClass)
    throws ErrorReadingFED
  {
    try
    {
      return fdd.addObjectClass(objectClassName, superObjectClass);
    }
    catch (InconsistentFDD ifdd)
    {
      throw new ErrorReadingFED(ifdd.getMessage(), ifdd);
    }
  }

  public Attribute addAttribute(
    ObjectClass objectClass, String attributeName, String transportationTypeName, String orderTypeName,
    String routingSpaceName)
    throws ErrorReadingFED
  {
    DimensionHandleSet dimensionHandles;

    if (routingSpaceName == null)
    {
      dimensionHandles = null;
    }
    else
    {
      RoutingSpace routingSpace = routingSpacesByName.get(routingSpaceName);
      if (routingSpace == null)
      {
        throw new ErrorReadingFED(I18n.getMessage(
          ExceptionMessages.ERROR_READING_FED_UNKNOWN_ATTRIBUTE_ROUTING_SPACE, objectClass, attributeName,
          routingSpaceName));
      }
      dimensionHandles = routingSpace.getDimensionHandles();
    }

    try
    {
      return fdd.addAttribute(objectClass, attributeName, dimensionHandles, transportationTypeName, orderTypeName);
    }
    catch (InconsistentFDD ifdd)
    {
      throw new ErrorReadingFED(ifdd.getMessage(), ifdd);
    }
  }

  public InteractionClass addInteractionClass(
    InteractionClass superInteractionClass, String interactionClassName, String transportationTypeName, String orderTypeName,
    String routingSpaceName)
    throws ErrorReadingFED
  {
    DimensionHandleSet dimensionHandles;

    if (routingSpaceName == null)
    {
      dimensionHandles = null;
    }
    else
    {
      RoutingSpace routingSpace = routingSpacesByName.get(routingSpaceName);
      if (routingSpace == null)
      {
        throw new ErrorReadingFED(I18n.getMessage(
          ExceptionMessages.ERROR_READING_FED_UNKNOWN_INTERACTION_CLASS_ROUTING_SPACE, interactionClassName,
          routingSpaceName));
      }
      dimensionHandles = routingSpace.getDimensionHandles();
    }

    try
    {
      return fdd.addInteractionClass(
        interactionClassName, superInteractionClass, dimensionHandles, transportationTypeName, orderTypeName);
    }
    catch (InconsistentFDD ifdd)
    {
      throw new ErrorReadingFED(ifdd.getMessage(), ifdd);
    }
  }

  public Parameter addParameter(InteractionClass interactionClass, String parameterName)
    throws ErrorReadingFED
  {
    try
    {
      return fdd.addParameter(interactionClass, parameterName);
    }
    catch (InconsistentFDD ifdd)
    {
      throw new ErrorReadingFED(ifdd.getMessage(), ifdd);
    }
  }

  public RoutingSpace addRoutingSpace(String routingSpaceName)
  {
    RoutingSpace routingSpace = routingSpacesByName.get(routingSpaceName);
    if (routingSpace == null)
    {
      routingSpace = new RoutingSpace(routingSpaces.size() + 1, routingSpaceName);

      routingSpaces.put(routingSpace.getRoutingSpaceHandle(), routingSpace);
      routingSpacesByName.put(routingSpaceName, routingSpace);
    }
    return routingSpace;
  }

  public Dimension addRoutingSpaceDimension(RoutingSpace routingSpace, String dimensionName)
  {
    Dimension dimension = fdd.addDimension(routingSpace.getRoutingSpaceName() + "." + dimensionName);

    routingSpace.addDimension(dimensionName, dimension);

    routingSpacesByDimension.put(dimension, routingSpace);

    return dimension;
  }

  public RoutingSpace getRoutingSpace(String routingSpaceName)
    throws NameNotFound
  {
    RoutingSpace routingSpace = routingSpacesByName.get(routingSpaceName);
    if (routingSpace == null)
    {
      throw new NameNotFound(I18n.getMessage(ExceptionMessages.ROUTING_SPACE_NAME_NOT_FOUND, routingSpaceName));
    }
    return routingSpace;
  }

  public RoutingSpace getRoutingSpace(int routingSpaceHandle)
    throws SpaceNotDefined
  {
    RoutingSpace routingSpace = routingSpaces.get(routingSpaceHandle);
    if (routingSpace == null)
    {
      throw new SpaceNotDefined(I18n.getMessage(ExceptionMessages.SPACE_NOT_DEFINED, routingSpaceHandle));
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
    return getRoutingSpace(routingSpaceHandle).getRoutingSpaceName();
  }

  public int getDimensionHandle(String name, int routingSpaceHandle)
    throws SpaceNotDefined, NameNotFound
  {
    return getRoutingSpace(routingSpaceHandle).getDimensionHandle(name);
  }

  public String getDimensionName(int dimensionHandle, int routingSpaceHandle)
    throws SpaceNotDefined, DimensionNotDefined
  {
    return getRoutingSpace(routingSpaceHandle).getDimensionAlias(dimensionHandle);
  }

  public int getAttributeRoutingSpaceHandle(AttributeHandle attributeHandle, ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      ObjectClass objectClass = fdd.getObjectClass(objectClassHandle);
      Attribute attribute = objectClass.getAttribute(attributeHandle);

      if (attribute.getDimensionHandles().isEmpty())
      {
        throw new RTIinternalError(I18n.getMessage(
          ExceptionMessages.NO_ROUTING_SPACE_DEFINED_FOR_ATTRIBUTE, objectClass, attribute));
      }
      else
      {
        RoutingSpace routingSpace = routingSpacesByDimension.get(
          fdd.getDimensionSafely(attribute.getDimensionHandles().iterator().next()));
        if (routingSpace == null)
        {
          throw new RTIinternalError(I18n.getMessage(
            ExceptionMessages.NO_ROUTING_SPACE_DEFINED_FOR_ATTRIBUTE, objectClass, attribute));
        }
        else
        {
          return routingSpace.getRoutingSpaceHandle();
        }
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

      if (interactionClass.getDimensionHandles().isEmpty())
      {
        throw new RTIinternalError(I18n.getMessage(
          ExceptionMessages.NO_ROUTING_SPACE_DEFINED_FOR_INTERACTION_CLASS, interactionClass));
      }
      else
      {
        RoutingSpace routingSpace = routingSpacesByDimension.get(
          fdd.getDimensionSafely(interactionClass.getDimensionHandles().iterator().next()));
        if (routingSpace == null)
        {
          throw new RTIinternalError(I18n.getMessage(
            ExceptionMessages.NO_ROUTING_SPACE_DEFINED_FOR_INTERACTION_CLASS, interactionClass));
        }
        else
        {
          return routingSpace.getRoutingSpaceHandle();
        }
      }
    }
    catch (hla.rti1516e.exceptions.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
  }

  public static void encode(ChannelBuffer buffer, FED fed)
  {
    Protocol.encodeOptionalString(buffer, fed.fedName);

    Protocol.encodeVarInt(buffer, fed.routingSpaces.size());
    for (RoutingSpace routingSpace : fed.routingSpaces.values())
    {
      RoutingSpace.encode(buffer, routingSpace);
    }
  }

  public static FED decode(ChannelBuffer buffer, FDD fdd)
  {
    return new FED(buffer, fdd);
  }
}
