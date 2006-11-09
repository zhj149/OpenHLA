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

package net.sf.ohla.rti1516.handles;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import hla.rti1516.CouldNotDecode;

public class IntegerHandle
  implements Handle
{
  private static final int ENCODED_LENGTH = Integer.SIZE / 8;

  protected final int handle;

  public IntegerHandle(int handle)
  {
    this.handle = handle;
  }

  public IntegerHandle(byte[] buffer, int offset)
    throws CouldNotDecode
  {
    try
    {
      handle = ByteBuffer.wrap(buffer, offset, ENCODED_LENGTH).getInt();
    }
    catch (BufferUnderflowException bue)
    {
      throw new CouldNotDecode(bue);
    }
  }

  public int getHandle()
  {
    return handle;
  }

  public int encodedLength()
  {
    return ENCODED_LENGTH;
  }

  public void encode(byte[] buffer, int offset)
  {
    ByteBuffer.wrap(buffer, offset, ENCODED_LENGTH).putInt(handle);
  }

  @Override
  public boolean equals(Object rhs)
  {
    return rhs instanceof IntegerHandle &&
           handle == ((IntegerHandle) rhs).handle;
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
