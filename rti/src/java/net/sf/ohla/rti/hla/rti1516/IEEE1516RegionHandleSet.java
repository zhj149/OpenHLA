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

package net.sf.ohla.rti.hla.rti1516;

import java.util.HashSet;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eRegionHandleSetFactory;

import hla.rti1516.RegionHandle;
import hla.rti1516.RegionHandleSet;

public class IEEE1516RegionHandleSet
  extends HashSet<RegionHandle>
  implements RegionHandleSet
{
  public IEEE1516RegionHandleSet()
  {
  }

  public IEEE1516RegionHandleSet(hla.rti1516e.RegionHandleSet regionHandles)
  {
    for (hla.rti1516e.RegionHandle regionHandle : regionHandles)
    {
      add(new IEEE1516RegionHandle(regionHandle));
    }
  }

  public static hla.rti1516e.RegionHandleSet createIEEE1516eRegionHandleSet(RegionHandleSet regionHandles)
  {
    hla.rti1516e.RegionHandleSet ieee1516eRegionHandleSet = IEEE1516eRegionHandleSetFactory.INSTANCE.create();
    for (RegionHandle regionHandle : regionHandles)
    {
      ieee1516eRegionHandleSet.add(((IEEE1516RegionHandle) regionHandle).getIEEE1516eRegionHandle());
    }
    return ieee1516eRegionHandleSet;
  }
}
