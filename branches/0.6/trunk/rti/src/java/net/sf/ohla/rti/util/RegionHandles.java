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
import java.util.List;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eRegionHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eRegionHandleSet;
import net.sf.ohla.rti.proto.OHLAProtos;

import hla.rti1516e.RegionHandle;
import hla.rti1516e.RegionHandleSet;

public class RegionHandles
{
  public static RegionHandle convert(OHLAProtos.RegionHandle regionHandle)
  {
    return new IEEE1516eRegionHandle(
      FederateHandles.convert(regionHandle.getFederateHandle()), regionHandle.getRegionHandle());
  }

  public static OHLAProtos.RegionHandle.Builder convert(RegionHandle regionHandle)
  {
    assert regionHandle instanceof IEEE1516eRegionHandle;
    IEEE1516eRegionHandle ieee1516eRegionHandle = (IEEE1516eRegionHandle) regionHandle;
    return OHLAProtos.RegionHandle.newBuilder().setFederateHandle(
      FederateHandles.convert(ieee1516eRegionHandle.getFederateHandle())).setRegionHandle(
      ieee1516eRegionHandle.getRegionHandle());
  }

  public static RegionHandleSet convertFromProto(List<OHLAProtos.RegionHandle> regionHandles)
  {
    RegionHandleSet convertedRegionHandles = new IEEE1516eRegionHandleSet(regionHandles.size());
    for (OHLAProtos.RegionHandle regionHandle : regionHandles)
    {
      convertedRegionHandles.add(convert(regionHandle));
    }
    return convertedRegionHandles;
  }

  public static Collection<OHLAProtos.RegionHandle> convertToProto(Collection<RegionHandle> regionHandles)
  {
    Collection<OHLAProtos.RegionHandle> convertedRegionHandles = new ArrayList<>(regionHandles.size());
    for (RegionHandle regionHandle : regionHandles)
    {
      convertedRegionHandles.add(convert(regionHandle).build());
    }
    return convertedRegionHandles;
  }
}
