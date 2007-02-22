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

package net.sf.ohla.rti1516.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleValueMap;

public class OHLAAttributeHandleValueMap
  extends HashMap<AttributeHandle, byte[]>
  implements AttributeHandleValueMap
{
  public OHLAAttributeHandleValueMap()
  {
  }

  public OHLAAttributeHandleValueMap(int initialCapacity)
  {
    super(initialCapacity);
  }

  public OHLAAttributeHandleValueMap(Map<AttributeHandle, byte[]> clonee)
  {
    super(clonee);
  }

  public boolean equals(Object rhs)
  {
    return rhs instanceof OHLAAttributeHandleValueMap &&
           equals((OHLAAttributeHandleValueMap) rhs);
  }

  public boolean equals(OHLAAttributeHandleValueMap rhs)
  {
    boolean equals = keySet().equals(rhs.keySet());
    if (equals)
    {
      for (
        Iterator<byte[]> i = values().iterator(), j = rhs.values().iterator();
        equals && i.hasNext();)
      {
        equals = Arrays.equals(i.next(), j.next());
      }
    }
    return equals;
  }
}
