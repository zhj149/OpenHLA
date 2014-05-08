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

import hla.rti1516.ObjectClassHandle;

public class IEEE1516ObjectClassHandle
  implements ObjectClassHandle
{
  public final hla.rti1516e.ObjectClassHandle objectClassHandle;

  public IEEE1516ObjectClassHandle(hla.rti1516e.ObjectClassHandle objectClassHandle)
  {
    this.objectClassHandle = objectClassHandle;
  }

  public hla.rti1516e.ObjectClassHandle getObjectClassHandle()
  {
    return objectClassHandle;
  }

  public int encodedLength()
  {
    return objectClassHandle.encodedLength();
  }

  public void encode(byte[] buffer, int offset)
  {
    objectClassHandle.encode(buffer, offset);
  }

  @Override
  public boolean equals(Object rhs)
  {
    return this == rhs ||
           (rhs instanceof IEEE1516ObjectClassHandle &&
            objectClassHandle.equals(((IEEE1516ObjectClassHandle) rhs).objectClassHandle));
  }

  @Override
  public int hashCode()
  {
    return objectClassHandle.hashCode();
  }

  @Override
  public String toString()
  {
    return objectClassHandle.toString();
  }
}
