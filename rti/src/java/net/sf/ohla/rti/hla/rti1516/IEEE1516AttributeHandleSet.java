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

package net.sf.ohla.rti.hla.rti1516;

import java.util.HashSet;
import java.util.Set;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;

public class IEEE1516AttributeHandleSet
  extends HashSet<AttributeHandle>
  implements AttributeHandleSet
{
  public IEEE1516AttributeHandleSet()
  {
  }

  public IEEE1516AttributeHandleSet(int initialCapacity)
  {
    super(initialCapacity);
  }

  public IEEE1516AttributeHandleSet(Set<AttributeHandle> attributeHandles)
  {
    super(attributeHandles);
  }

  public IEEE1516AttributeHandleSet(IEEE1516AttributeHandleSet attributeHandles)
  {
    super(attributeHandles);
  }
}
