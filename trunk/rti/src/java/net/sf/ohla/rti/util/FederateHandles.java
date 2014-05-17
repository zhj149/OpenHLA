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

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandleSet;

import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateHandleSet;

public class FederateHandles
{
  private static final IEEE1516eFederateHandle[] cache;
  static
  {
    // TODO: get cache size from properties

    cache = new IEEE1516eFederateHandle[128];

    for (int i = 1; i < cache.length; i++)
    {
      cache[i] = new IEEE1516eFederateHandle(i);
    }
  }

  public static FederateHandle convert(int federateHandle)
  {
    return federateHandle < cache.length ? cache[federateHandle] : new IEEE1516eFederateHandle(federateHandle);
  }

  public static int convert(FederateHandle federateHandle)
  {
    assert federateHandle instanceof IEEE1516eFederateHandle;

    return ((IEEE1516eFederateHandle) federateHandle).handle;
  }

  public static FederateHandleSet convertFromProto(Collection<Integer> federateHandles)
  {
    FederateHandleSet convertedFederateHandles = new IEEE1516eFederateHandleSet(federateHandles.size());
    for (int federateHandle : federateHandles)
    {
      convertedFederateHandles.add(convert(federateHandle));
    }
    return convertedFederateHandles;
  }

  public static Collection<Integer> convertToProto(Collection<FederateHandle> federateHandles)
  {
    List<Integer> convertedFederateHandles = new ArrayList<>(federateHandles.size());
    for (FederateHandle federateHandle : federateHandles)
    {
      convertedFederateHandles.add(convert(federateHandle));
    }
    return convertedFederateHandles;
  }
}
