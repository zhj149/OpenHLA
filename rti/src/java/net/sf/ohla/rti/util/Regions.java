/*
 * Copyright (c) 2005-2011, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sf.ohla.rti.messages.proto.FederateMessageProtos;
import net.sf.ohla.rti.proto.OHLAProtos;

import hla.rti1516e.DimensionHandle;
import hla.rti1516e.RangeBounds;

public class Regions
{
  public static Collection<FederateMessageProtos.Region> convertToProtos(
    Collection<Map<DimensionHandle, RangeBounds>> regions)
  {
    Collection<FederateMessageProtos.Region> regionProtos = new ArrayList<>(regions.size());
    for (Map<DimensionHandle, RangeBounds> region : regions)
    {
      FederateMessageProtos.Region.Builder regionProto = FederateMessageProtos.Region.newBuilder();
      for (Map.Entry<DimensionHandle, RangeBounds> entry : region.entrySet())
      {
        regionProto.addDimensionRangeBounds(
          OHLAProtos.DimensionRangeBound.newBuilder().setDimensionHandle(
            DimensionHandles.convert(entry.getKey())).setLowerBound(
            entry.getValue().lower).setUpperBound(
            entry.getValue().upper));
      }
      regionProtos.add(regionProto.build());
    }
    return regionProtos;
  }

  public static Collection<Map<DimensionHandle, RangeBounds>> convertFromProtos(
    Collection<FederateMessageProtos.Region> regionProtos)
  {
    Collection<Map<DimensionHandle, RangeBounds>> regions = new ArrayList<>(regionProtos.size());
    for (FederateMessageProtos.Region regionProto : regionProtos)
    {
      Map<DimensionHandle, RangeBounds> region = new HashMap<>();
      for (OHLAProtos.DimensionRangeBound dimensionRangeBound : regionProto.getDimensionRangeBoundsList())
      {
        region.put(DimensionHandles.convert(dimensionRangeBound.getDimensionHandle()),
                   new RangeBounds(dimensionRangeBound.getLowerBound(), dimensionRangeBound.getUpperBound()));
      }
      regions.add(region);
    }
    return regions;
  }
}
