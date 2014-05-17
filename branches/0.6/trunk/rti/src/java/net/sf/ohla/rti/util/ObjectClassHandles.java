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

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eObjectClassHandle;

import hla.rti1516e.ObjectClassHandle;

public class ObjectClassHandles
{
  private static final IEEE1516eObjectClassHandle[] cache;
  static
  {
    // TODO: get cache size from properties

    cache = new IEEE1516eObjectClassHandle[1024];

    for (int i = 1; i < cache.length; i++)
    {
      cache[i] = new IEEE1516eObjectClassHandle(i);
    }
  }

  public static ObjectClassHandle convert(int objectClassHandle)
  {
    return objectClassHandle < cache.length ?
      cache[objectClassHandle] : new IEEE1516eObjectClassHandle(objectClassHandle);
  }

  public static int convert(ObjectClassHandle objectClassHandle)
  {
    assert objectClassHandle instanceof IEEE1516eObjectClassHandle;

    return ((IEEE1516eObjectClassHandle) objectClassHandle).handle;
  }
}
