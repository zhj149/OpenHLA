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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.ohla.rti.util.DimensionHandles;
import net.sf.ohla.rti.fdd.Dimension;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandleSetFactory;
import net.sf.ohla.rti.i18n.ExceptionMessages;
import net.sf.ohla.rti.i18n.I18n;
import net.sf.ohla.rti.proto.OHLAProtos;

import hla.rti.DimensionNotDefined;
import hla.rti.NameNotFound;

import hla.rti1516e.DimensionHandle;
import hla.rti1516e.DimensionHandleSet;

public class RoutingSpace
{
  public static final RoutingSpace DEFAULT = new RoutingSpace(Integer.MIN_VALUE, "HLAdefaultRoutingSpace");

  private final int routingSpaceHandle;
  private final String routingSpaceName;

  private DimensionHandleSet dimensionHandles = IEEE1516eDimensionHandleSetFactory.INSTANCE.create();

  private final List<Dimension> dimensions;
  private final List<String> aliases;

  public RoutingSpace(int routingSpaceHandle, String routingSpaceName)
  {
    this.routingSpaceHandle = routingSpaceHandle;
    this.routingSpaceName = routingSpaceName;

    dimensions = new ArrayList<>();
    aliases = new ArrayList<>();
  }

  public RoutingSpace(OHLAProtos.FDD.FED.RoutingSpace routingSpace, Map<DimensionHandle, Dimension> dimensions)
  {
    routingSpaceHandle = routingSpace.getRoutingSpaceHandle();
    routingSpaceName = routingSpace.getRoutingSpaceName();

    this.dimensions = new ArrayList<>(routingSpace.getDimensionHandlesCount());
    for (int dimensionHandle : routingSpace.getDimensionHandlesList())
    {
      Dimension dimension = dimensions.get(DimensionHandles.convert(dimensionHandle));

      this.dimensions.add(dimension);
      dimensionHandles.add(dimension.getDimensionHandle());
    }

    aliases = new ArrayList<>(routingSpace.getAliasesList());
  }

  public boolean isDefault()
  {
    return this == DEFAULT;
  }

  public String getRoutingSpaceName()
  {
    return routingSpaceName;
  }

  public int getRoutingSpaceHandle()
  {
    return routingSpaceHandle;
  }

  public List<Dimension> getDimensions()
  {
    return dimensions;
  }

  public DimensionHandleSet getDimensionHandles()
  {
    return dimensionHandles;
  }

  public void addDimension(String alias, Dimension dimension)
  {
    dimensions.add(dimension);
    dimensionHandles.add(dimension.getDimensionHandle());
    aliases.add(alias);
  }

  public Dimension getDimension(String alias)
    throws NameNotFound
  {
    return dimensions.get(getDimensionHandle(alias));
  }

  public Dimension getDimension(int dimensionHandle)
    throws DimensionNotDefined
  {
    if (dimensionHandle < 0 || dimensionHandle >= dimensions.size())
    {
      throw new DimensionNotDefined(I18n.getMessage(ExceptionMessages.DIMENSION_NOT_DEFINED, dimensionHandle));
    }
    return dimensions.get(dimensionHandle);
  }

  public int getDimensionHandle(String alias)
    throws NameNotFound
  {
    int index = aliases.indexOf(alias);
    if (index == -1)
    {
      throw new NameNotFound(I18n.getMessage(ExceptionMessages.ROUTING_SPACE_DIMENSION_NAME_NOT_FOUND, this, alias));
    }
    return index;
  }

  public String getDimensionAlias(int dimensionHandle)
    throws DimensionNotDefined
  {
    if (dimensionHandle < 0 || dimensionHandle >= aliases.size())
    {
      throw new DimensionNotDefined(I18n.getMessage(
        ExceptionMessages.ROUTING_SPACE_DIMENSION_NOT_DEFINED, dimensionHandle));
    }
    return aliases.get(dimensionHandle);
  }

  public OHLAProtos.FDD.FED.RoutingSpace.Builder toProto()
  {
    OHLAProtos.FDD.FED.RoutingSpace.Builder routingSpace =
      OHLAProtos.FDD.FED.RoutingSpace.newBuilder().setRoutingSpaceHandle(
        routingSpaceHandle).setRoutingSpaceName(
        routingSpaceName).addAllAliases(
        aliases);

    for (Dimension dimension : dimensions)
    {
      routingSpace.addDimensionHandles(((IEEE1516eDimensionHandle) dimension.getDimensionHandle()).getHandle());
    }

    return routingSpace;
  }
}
