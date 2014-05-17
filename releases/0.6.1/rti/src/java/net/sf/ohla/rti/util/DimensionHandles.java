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

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandleSet;

import hla.rti1516e.DimensionHandle;
import hla.rti1516e.DimensionHandleSet;

public class DimensionHandles
{
  private static final IEEE1516eDimensionHandle[] cache;
  static
  {
    // TODO: get cache size from properties

    cache = new IEEE1516eDimensionHandle[128];

    for (int i = 1; i < cache.length; i++)
    {
      cache[i] = new IEEE1516eDimensionHandle(i);
    }
  }

  public static DimensionHandle convert(int dimensionHandle)
  {
    return dimensionHandle < cache.length ? cache[dimensionHandle] : new IEEE1516eDimensionHandle(dimensionHandle);
  }

  public static int convert(DimensionHandle dimensionHandle)
  {
    assert dimensionHandle instanceof IEEE1516eDimensionHandle;

    return ((IEEE1516eDimensionHandle) dimensionHandle).handle;
  }

  public static DimensionHandleSet convert(Collection<Integer> dimensionHandles)
  {
    DimensionHandleSet ieee1516eDimensionHandles;
    if (dimensionHandles.isEmpty())
    {
      ieee1516eDimensionHandles = IEEE1516eDimensionHandleSet.EMPTY;
    }
    else
    {
      ieee1516eDimensionHandles = new IEEE1516eDimensionHandleSet(dimensionHandles.size());
      for (int dimensionHandle : dimensionHandles)
      {
        ieee1516eDimensionHandles.add(convert(dimensionHandle));
      }
    }
    return ieee1516eDimensionHandles;
  }

  public static Collection<Integer> convert(DimensionHandleSet dimensionHandleSet)
  {
    List<Integer> dimensionHandles = new ArrayList<>(dimensionHandleSet.size());
    for (DimensionHandle dimensionHandle : dimensionHandleSet)
    {
      dimensionHandles.add(convert(dimensionHandle));
    }
    return dimensionHandles;
  }
}
