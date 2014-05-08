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

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandleSetFactory;

import hla.rti1516.DimensionHandle;
import hla.rti1516.DimensionHandleSet;

public class IEEE1516DimensionHandleSet
  extends HashSet<DimensionHandle>
  implements DimensionHandleSet
{
  public IEEE1516DimensionHandleSet()
  {
  }

  public IEEE1516DimensionHandleSet(hla.rti1516e.DimensionHandleSet dimensionHandles)
  {
    for (hla.rti1516e.DimensionHandle dimensionHandle : dimensionHandles)
    {
      add(new IEEE1516DimensionHandle(dimensionHandle));
    }
  }

  public static hla.rti1516e.DimensionHandleSet createIEEE1516eDimensionHandleSet(DimensionHandleSet dimensionHandles)
  {
    hla.rti1516e.DimensionHandleSet ieee1516eDimensionHandles = IEEE1516eDimensionHandleSetFactory.INSTANCE.create();
    for (DimensionHandle dimensionHandle : dimensionHandles)
    {
      ieee1516eDimensionHandles.add(((IEEE1516DimensionHandle) dimensionHandle).getDimensionHandle());
    }
    return ieee1516eDimensionHandles;
  }
}
