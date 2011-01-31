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

import net.sf.ohla.rti.Protocol;

import hla.rti1516e.DimensionHandle;
import hla.rti1516e.DimensionHandleFactory;
import hla.rti1516e.exceptions.CouldNotDecode;

public class IEEE1516eDimensionHandleFactory
  implements DimensionHandleFactory
{
  public static final IEEE1516eDimensionHandleFactory INSTANCE = new IEEE1516eDimensionHandleFactory();

  private IEEE1516eDimensionHandleFactory()
  {
  }

  public DimensionHandle decode(byte[] buffer, int offset)
    throws CouldNotDecode
  {
    try
    {
      return new IEEE1516eDimensionHandle(Protocol.decodeVarInt(buffer, offset));
    }
    catch (Throwable t)
    {
      throw new CouldNotDecode("", t);
    }
  }

  private Object readResolve()
  {
    return INSTANCE;
  }
}
