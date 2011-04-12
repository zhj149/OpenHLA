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

import net.sf.ohla.rti.IntegerHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandle;

import hla.rti1516.AttributeHandle;
import hla.rti1516.CouldNotDecode;

public class IEEE1516AttributeHandle
  extends IntegerHandle
  implements AttributeHandle
{
  public IEEE1516AttributeHandle(byte[] buffer, int offset)
    throws CouldNotDecode
  {
    super(buffer, offset);
  }

  public IEEE1516AttributeHandle(hla.rti1516e.AttributeHandle attributeHandle)
  {
    super((IEEE1516eAttributeHandle) attributeHandle);
  }
}