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

package net.sf.ohla.rti.handles;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import hla.rti1516.CouldNotDecode;

public class StringHandle
  implements Handle
{
  private static final int BYTES_PER_CHARARACTER = Character.SIZE / 8;
  private static final int BYTES_FOR_LENGTH = 2;

  protected final String handle;

  public StringHandle(String handle)
  {
    this.handle = handle;
  }

  public StringHandle(byte[] buffer, int offset)
    throws CouldNotDecode
  {
    try
    {
      DataInputStream dis = new DataInputStream(
        new ByteArrayInputStream(buffer, offset, buffer.length));

      // read the length of the string
      //
      int length = dis.readUnsignedShort();

      handle = ByteBuffer.wrap(
        buffer, offset + BYTES_FOR_LENGTH, length).asCharBuffer().toString();
    }
    catch (IOException ioe)
    {
      throw new CouldNotDecode(ioe);
    }
    catch (BufferUnderflowException bue)
    {
      throw new CouldNotDecode(bue);
    }
  }

  public int encodedLength()
  {
    return BYTES_FOR_LENGTH + (handle.length() * BYTES_PER_CHARARACTER);
  }

  public void encode(byte[] buffer, int offset)
  {
    CharBuffer charBuffer =
      ByteBuffer.wrap(buffer, offset, encodedLength()).asCharBuffer();

    // write the length of the string as an unsigned short
    //
    charBuffer.put((char) handle.length());

    // write the string
    //
    charBuffer.put(handle);
  }

  @Override
  public boolean equals(Object rhs)
  {
    return rhs instanceof StringHandle &&
           handle.equals(((StringHandle) rhs).handle);
  }

  @Override
  public int hashCode()
  {
    return handle.hashCode();
  }

  @Override
  public String toString()
  {
    return handle;
  }
}
