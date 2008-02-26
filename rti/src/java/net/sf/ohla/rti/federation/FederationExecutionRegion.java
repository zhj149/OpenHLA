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

package net.sf.ohla.rti.federation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sf.ohla.rti.fdd.Attribute;
import net.sf.ohla.rti.fdd.Dimension;
import net.sf.ohla.rti.fdd.InteractionClass;

import hla.rti1516.AttributeHandle;
import hla.rti1516.DimensionHandle;
import hla.rti1516.DimensionHandleSet;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.RangeBounds;
import hla.rti1516.RegionDoesNotContainSpecifiedDimension;
import hla.rti1516.RegionHandle;

public class FederationExecutionRegion
{
  protected final RegionHandle regionHandle;
  protected final DimensionHandleSet dimensionHandles;

  protected final Map<DimensionHandle, RangeBounds> rangeBounds =
    new HashMap<DimensionHandle, RangeBounds>();

  protected final Map<AttributeHandle, AttributeRegionRealization> attributeRegionRealizations =
    new HashMap<AttributeHandle, AttributeRegionRealization>();
  protected final Map<InteractionClassHandle, InteractionClassRegionRealization> interactionClassRegionRealizations =
    new HashMap<InteractionClassHandle, InteractionClassRegionRealization>();

  protected final FederationExecutionRegionManager regionManager;

  public FederationExecutionRegion(
    RegionHandle regionHandle, DimensionHandleSet dimensionHandles,
    FederationExecutionRegionManager regionManager)
  {
    this.regionHandle = regionHandle;
    this.dimensionHandles = dimensionHandles;

    // initialize our range bounds to the dimension defaults
    //
    for (DimensionHandle dimensionHandle : dimensionHandles)
    {
      Dimension dimension =
        regionManager.getFederationExecution().getFDD().getDimensions().get(
          dimensionHandle);
      assert dimension != null;

      rangeBounds.put(dimensionHandle, dimension.getRangeBounds());
    }

    this.regionManager = regionManager;
  }

  public RegionHandle getRegionHandle()
  {
    return regionHandle;
  }

  public DimensionHandleSet getDimensionHandles()
  {
    return dimensionHandles;
  }

  public RangeBounds getRangeBounds(DimensionHandle dimensionHandle)
    throws RegionDoesNotContainSpecifiedDimension
  {
    RangeBounds rangeBounds = this.rangeBounds.get(dimensionHandle);
    if (rangeBounds == null)
    {
      throw new RegionDoesNotContainSpecifiedDimension(dimensionHandle);
    }
    return rangeBounds;
  }

  public FederationExecutionRegionManager getRegionManager()
  {
    return regionManager;
  }

  public void commitRegionModifications(
    Map<DimensionHandle, RangeBounds> rangeBounds)
  {
    this.rangeBounds.putAll(rangeBounds);
  }

  public void delete()
  {
    // TODO: notify all our intersecting regions
  }

  public boolean intersects(FederationExecutionRegion region,
                            Set<DimensionHandle> dimensionhandles)
  {
    boolean intersects = false;

    for (Iterator<DimensionHandle> i = dimensionHandles.iterator();
         !intersects && i.hasNext();)
    {
      DimensionHandle dimensionHandle = i.next();

      RangeBounds lhs = rangeBounds.get(dimensionHandle);
      RangeBounds rhs = region.rangeBounds.get(dimensionHandle);

      if (lhs != null && rhs != null)
      {
        intersects = intersects(lhs, rhs);
      }
    }

    return intersects;
  }

  public boolean intersects(InteractionClassHandle interactionClassHandle,
                            Set<RegionHandle> regionHandles)
  {
    boolean intersects = false;

    InteractionClassRegionRealization interactionClassRegionRealization =
      interactionClassRegionRealizations.get(interactionClassHandle);
    if (interactionClassRegionRealization != null)
    {
      intersects = interactionClassRegionRealization.intersects(regionHandles);
    }

    return intersects;
  }

  protected boolean intersects(RangeBounds lhs, RangeBounds rhs)
  {
    return (lhs.lower < rhs.upper && rhs.lower < lhs.upper) ||
           lhs.lower == rhs.lower;
  }

  protected abstract class RegionRealization
  {
    protected Map<RegionHandle, FederationExecutionRegion> intersectingRegions =
      new HashMap<RegionHandle, FederationExecutionRegion>();

    public boolean intersects(Set<RegionHandle> regionHandles)
    {
      boolean intersects = false;

      if (regionHandles.size() < intersectingRegions.size())
      {
        for (Iterator<RegionHandle> i = regionHandles.iterator();
             !intersects && i.hasNext();)
        {
          intersects = intersectingRegions.containsKey(i.next());
        }
      }
      else
      {
        for (Iterator<RegionHandle> i = intersectingRegions.keySet().iterator();
             !intersects && i.hasNext();)
        {
          intersects = regionHandles.contains(i.next());
        }
      }

      return intersects;
    }
  }

  protected class AttributeRegionRealization
    extends RegionRealization
  {
    protected final Attribute attribute;

    public AttributeRegionRealization(Attribute attribute)
    {
      this.attribute = attribute;
    }

    public Attribute getAttribute()
    {
      return attribute;
    }
  }

  protected class InteractionClassRegionRealization
    extends RegionRealization
  {
    protected final InteractionClass interactionClass;

    public InteractionClassRegionRealization(InteractionClass interactionClass)
    {
      this.interactionClass = interactionClass;
    }

    public InteractionClass getInteractionClass()
    {
      return interactionClass;
    }
  }
}
