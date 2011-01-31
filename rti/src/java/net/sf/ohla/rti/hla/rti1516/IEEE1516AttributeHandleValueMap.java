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

import java.util.HashMap;
import java.util.Map;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleValueMapFactory;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleValueMap;

public class IEEE1516AttributeHandleValueMap
  extends HashMap<AttributeHandle, byte[]>
  implements AttributeHandleValueMap
{
  public IEEE1516AttributeHandleValueMap(int initialCapacity)
  {
    super(initialCapacity);
  }

  public IEEE1516AttributeHandleValueMap(hla.rti1516e.AttributeHandleValueMap attributeValues)
  {
    for (Map.Entry<hla.rti1516e.AttributeHandle, byte[]> entry : attributeValues.entrySet())
    {
      put(new IEEE1516AttributeHandle(entry.getKey()), entry.getValue());
    }
  }

  public static hla.rti1516e.AttributeHandleValueMap createIEEE1516eAttributeHandleValueMap(
    AttributeHandleValueMap attributeValues)
  {
    hla.rti1516e.AttributeHandleValueMap ieee1516eAttributeValues =
      IEEE1516eAttributeHandleValueMapFactory.INSTANCE.create(attributeValues.size());
    for (Map.Entry<AttributeHandle, byte[]> entry : attributeValues.entrySet())
    {
      ieee1516eAttributeValues.put(
        new IEEE1516eAttributeHandle(((IEEE1516AttributeHandle) entry.getKey()).getHandle()), entry.getValue());
    }
    return ieee1516eAttributeValues;
  }
}
