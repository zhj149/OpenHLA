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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eParameterHandleValueMapFactory;

import hla.rti1516.ParameterHandle;
import hla.rti1516.ParameterHandleValueMap;

public class IEEE1516ParameterHandleValueMap
  extends HashMap<ParameterHandle, byte[]>
  implements ParameterHandleValueMap
{
  public IEEE1516ParameterHandleValueMap(int initialCapacity)
  {
    super(initialCapacity);
  }

  public IEEE1516ParameterHandleValueMap(hla.rti1516e.ParameterHandleValueMap parameterValues)
  {
    for (Map.Entry<hla.rti1516e.ParameterHandle, byte[]> entry : parameterValues.entrySet())
    {
      put(new IEEE1516ParameterHandle(entry.getKey()), entry.getValue());
    }
  }

  @Override
  public boolean equals(Object rhs)
  {
    return this == rhs || (rhs instanceof ParameterHandleValueMap && equals((ParameterHandleValueMap) rhs));
  }

  private boolean equals(ParameterHandleValueMap rhs)
  {
    boolean equals = size() == rhs.size();
    if (equals)
    {
      for (Iterator<Map.Entry<ParameterHandle, byte[]>> i = entrySet().iterator(); i.hasNext() && equals;)
      {
        Map.Entry<ParameterHandle, byte[]> entry = i.next();
        equals = Arrays.equals(entry.getValue(), rhs.get(entry.getKey()));
      }
    }
    return equals;
  }

  public static hla.rti1516e.ParameterHandleValueMap createIEEE1516eParameterHandleValueMap(
    ParameterHandleValueMap parameterValues)
  {
    hla.rti1516e.ParameterHandleValueMap ieee1516eParameterValues =
      IEEE1516eParameterHandleValueMapFactory.INSTANCE.create(parameterValues.size());
    for (Map.Entry<ParameterHandle, byte[]> entry : parameterValues.entrySet())
    {
      ieee1516eParameterValues.put(((IEEE1516ParameterHandle) entry.getKey()).getParameterHandle(), entry.getValue());
    }
    return ieee1516eParameterValues;
  }
}
