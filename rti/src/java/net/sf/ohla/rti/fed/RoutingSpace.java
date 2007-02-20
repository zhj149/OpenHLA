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

package net.sf.ohla.rti.fed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.ohla.rti1516.impl.OHLADimensionHandleSet;
import net.sf.ohla.rti1516.fdd.Dimension;

import hla.rti.DimensionNotDefined;
import hla.rti.NameNotFound;

public class RoutingSpace
  implements Serializable
{
  public static final RoutingSpace DEFAULT =
    new RoutingSpace("HLAdefaultRoutingSpace", Integer.MIN_VALUE);

  protected String name;
  protected int routingSpaceHandle;
  protected List<Dimension> dimensions = new ArrayList<Dimension>();
  protected List<String> dimensionHandlesByName = new ArrayList<String>();

  protected transient hla.rti1516.DimensionHandleSet dimensionHandles;

  public RoutingSpace(String name, AtomicInteger routingSpaceCount)
  {
    this(name, routingSpaceCount.incrementAndGet());
  }

  protected RoutingSpace(String name, int routingSpaceHandle)
  {
    this.name = name;
    this.routingSpaceHandle = routingSpaceHandle;
  }

  public boolean isDefault()
  {
    return routingSpaceHandle == DEFAULT.routingSpaceHandle;
  }

  public String getName()
  {
    return name;
  }

  public int getRoutingSpaceHandle()
  {
    return routingSpaceHandle;
  }

  public List<Dimension> getDimensions()
  {
    return dimensions;
  }

  public synchronized hla.rti1516.DimensionHandleSet getDimensionHandles()
  {
    if (dimensionHandles == null)
    {
      dimensionHandles = new OHLADimensionHandleSet();
      for (Dimension dimension : dimensions)
      {
        dimensionHandles.add(dimension.getDimensionHandle());
      }
    }
    return dimensionHandles;
  }

  public void add(String name, Dimension dimension)
  {
    dimensions.add(dimension);
    dimensionHandlesByName.add(name);
  }

  public Dimension getDimension(String name)
    throws NameNotFound
  {
    return dimensions.get(getDimensionHandle(name));
  }

  public Dimension getDimension(int dimensionHandle)
    throws DimensionNotDefined
  {
    if (dimensionHandle < 0 || dimensionHandle >= dimensions.size())
    {
      throw new DimensionNotDefined(Integer.toString(dimensionHandle));
    }
    return dimensions.get(dimensionHandle);
  }

  public int getDimensionHandle(String name)
    throws NameNotFound
  {
    int index = dimensionHandlesByName.indexOf(name);
    if (index == -1)
    {
      throw new NameNotFound(name);
    }
    return index;
  }

  public String getDimensionName(int dimensionHandle)
    throws DimensionNotDefined
  {
    if (dimensionHandle < 0 || dimensionHandle >= dimensionHandlesByName.size())
    {
      throw new DimensionNotDefined(Integer.toString(dimensionHandle));
    }
    return dimensionHandlesByName.get(dimensionHandle);
  }
}
