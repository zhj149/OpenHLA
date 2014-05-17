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

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandleFactory;

import hla.rti1516.CouldNotDecode;
import hla.rti1516.DimensionHandle;
import hla.rti1516.DimensionHandleFactory;

public class IEEE1516DimensionHandleFactory
  implements DimensionHandleFactory
{
  public static final IEEE1516DimensionHandleFactory INSTANCE = new IEEE1516DimensionHandleFactory();

  private IEEE1516DimensionHandleFactory()
  {
  }

  public DimensionHandle decode(byte[] buffer, int offset)
    throws CouldNotDecode
  {
    try
    {
      return new IEEE1516DimensionHandle(IEEE1516eDimensionHandleFactory.INSTANCE.decode(buffer, offset));
    }
    catch (hla.rti1516e.exceptions.CouldNotDecode cnd)
    {
      throw new CouldNotDecode(cnd);
    }
  }

  private Object readResolve()
  {
    return INSTANCE;
  }
}
