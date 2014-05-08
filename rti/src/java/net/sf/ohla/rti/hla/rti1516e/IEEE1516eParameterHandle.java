/*
 * Copyright (c) 2005-2010, Michael Newcomb
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

package net.sf.ohla.rti.hla.rti1516e;

import java.nio.ByteBuffer;

import hla.rti1516e.ParameterHandle;

public class IEEE1516eParameterHandle
  implements ParameterHandle
{
  public final int handle;

  public IEEE1516eParameterHandle(int handle)
  {
    this.handle = handle;
  }

  public int getHandle()
  {
    return handle;
  }

  public int encodedLength()
  {
    return 4;
  }

  public void encode(byte[] buffer, int offset)
  {
    ByteBuffer.wrap(buffer, offset, 4).putInt(handle);
  }

  @Override
  public boolean equals(Object rhs)
  {
    return this == rhs ||
           (rhs instanceof IEEE1516eParameterHandle && handle == ((IEEE1516eParameterHandle) rhs).handle);
  }

  @Override
  public int hashCode()
  {
    return handle;
  }

  @Override
  public String toString()
  {
    return Integer.toString(handle);
  }
}
