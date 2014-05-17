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

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eParameterHandle;

import hla.rti1516e.ParameterHandle;

public class ParameterHandles
{
  private static final IEEE1516eParameterHandle[] cache;
  static
  {
    // TODO: get cache size from properties

    cache = new IEEE1516eParameterHandle[128];

    for (int i = 1; i < cache.length; i++)
    {
      cache[i] = new IEEE1516eParameterHandle(i);
    }
  }

  public static ParameterHandle convert(int attributeHandle)
  {
    return attributeHandle < cache.length ? cache[attributeHandle] : new IEEE1516eParameterHandle(attributeHandle);
  }

  public static int convert(ParameterHandle attributeHandle)
  {
    assert attributeHandle instanceof IEEE1516eParameterHandle;

    return ((IEEE1516eParameterHandle) attributeHandle).handle;
  }
}
