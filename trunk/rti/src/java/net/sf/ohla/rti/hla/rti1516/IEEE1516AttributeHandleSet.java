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

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleSetFactory;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;

public class IEEE1516AttributeHandleSet
  extends HashSet<AttributeHandle>
  implements AttributeHandleSet
{
  public IEEE1516AttributeHandleSet()
  {
  }

  public IEEE1516AttributeHandleSet(hla.rti1516e.AttributeHandleSet attributeHandles)
  {
    for (hla.rti1516e.AttributeHandle attributeHandle : attributeHandles)
    {
      add(new IEEE1516AttributeHandle(attributeHandle));
    }
  }

  public static hla.rti1516e.AttributeHandleSet createIEEE1516eAttributeHandleSet(AttributeHandleSet attributeHandles)
  {
    hla.rti1516e.AttributeHandleSet ieee1516eAttributeHandles = IEEE1516eAttributeHandleSetFactory.INSTANCE.create();
    for (AttributeHandle attributeHandle : attributeHandles)
    {
      ieee1516eAttributeHandles.add(
        new IEEE1516eAttributeHandle(((IEEE1516AttributeHandle) attributeHandle).getHandle()));
    }
    return ieee1516eAttributeHandles;
  }
}
