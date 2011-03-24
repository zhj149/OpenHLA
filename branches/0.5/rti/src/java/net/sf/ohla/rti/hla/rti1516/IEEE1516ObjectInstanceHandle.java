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

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eObjectInstanceHandleFactory;

import hla.rti1516.CouldNotDecode;
import hla.rti1516.ObjectInstanceHandle;

public class IEEE1516ObjectInstanceHandle
  implements ObjectInstanceHandle
{
  private final hla.rti1516e.ObjectInstanceHandle objectInstanceHandle;

  public IEEE1516ObjectInstanceHandle(hla.rti1516e.ObjectInstanceHandle objectInstanceHandle)
  {
    this.objectInstanceHandle = objectInstanceHandle;
  }

  public IEEE1516ObjectInstanceHandle(byte[] buffer, int offset)
    throws CouldNotDecode
  {
    try
    {
      objectInstanceHandle = IEEE1516eObjectInstanceHandleFactory.INSTANCE.decode(buffer, offset);
    }
    catch (hla.rti1516e.exceptions.CouldNotDecode cnd)
    {
      throw new CouldNotDecode(cnd);
    }
  }

  public hla.rti1516e.ObjectInstanceHandle getIEEE1516eObjectInstanceHandle()
  {
    return objectInstanceHandle;
  }

  public int encodedLength()
  {
    return objectInstanceHandle.encodedLength();
  }

  public void encode(byte[] buffer, int offset)
  {
    objectInstanceHandle.encode(buffer, offset);
  }

  @Override
  public int hashCode()
  {
    return objectInstanceHandle.hashCode();
  }

  @Override
  public boolean equals(Object rhs)
  {
    return this == rhs || (rhs instanceof IEEE1516ObjectInstanceHandle &&
                           objectInstanceHandle.equals(((IEEE1516ObjectInstanceHandle) rhs).objectInstanceHandle));
  }

  @Override
  public String toString()
  {
    return objectInstanceHandle.toString();
  }
}
