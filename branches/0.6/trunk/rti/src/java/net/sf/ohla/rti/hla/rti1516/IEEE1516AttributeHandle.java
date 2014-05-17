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

import hla.rti1516.AttributeHandle;

public class IEEE1516AttributeHandle
  implements AttributeHandle
{
  public final hla.rti1516e.AttributeHandle attributeHandle;

  public IEEE1516AttributeHandle(hla.rti1516e.AttributeHandle attributeHandle)
  {
    this.attributeHandle = attributeHandle;
  }

  public hla.rti1516e.AttributeHandle getAttributeHandle()
  {
    return attributeHandle;
  }

  public int encodedLength()
  {
    return attributeHandle.encodedLength();
  }

  public void encode(byte[] buffer, int offset)
  {
    attributeHandle.encode(buffer, offset);
  }

  @Override
  public boolean equals(Object rhs)
  {
    return this == rhs || (rhs instanceof IEEE1516AttributeHandle &&
                           attributeHandle.equals(((IEEE1516AttributeHandle) rhs).attributeHandle));
  }

  @Override
  public int hashCode()
  {
    return attributeHandle.hashCode();
  }

  @Override
  public String toString()
  {
    return attributeHandle.toString();
  }
}
