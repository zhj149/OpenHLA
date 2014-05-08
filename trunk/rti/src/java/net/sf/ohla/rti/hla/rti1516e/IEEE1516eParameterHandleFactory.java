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

import net.sf.ohla.rti.util.ParameterHandles;

import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleFactory;
import hla.rti1516e.exceptions.CouldNotDecode;

public class IEEE1516eParameterHandleFactory
  implements ParameterHandleFactory
{
  public static final IEEE1516eParameterHandleFactory INSTANCE = new IEEE1516eParameterHandleFactory();

  private IEEE1516eParameterHandleFactory()
  {
  }

  public ParameterHandle decode(byte[] buffer, int offset)
    throws CouldNotDecode
  {
    try
    {
      return ParameterHandles.convert(ByteBuffer.wrap(buffer, offset, 4).getInt());
    }
    catch (Throwable t)
    {
      throw new CouldNotDecode(t.getMessage(), t);
    }
  }

  private Object readResolve()
  {
    return INSTANCE;
  }
}
