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

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eObjectClassHandleFactory;

import hla.rti1516.CouldNotDecode;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectClassHandleFactory;

public class IEEE1516ObjectClassHandleFactory
  implements ObjectClassHandleFactory
{
  public static final IEEE1516ObjectClassHandleFactory INSTANCE = new IEEE1516ObjectClassHandleFactory();

  private IEEE1516ObjectClassHandleFactory()
  {
  }

  public ObjectClassHandle decode(byte[] buffer, int offset)
    throws CouldNotDecode
  {
    try
    {
      return new IEEE1516ObjectClassHandle(IEEE1516eObjectClassHandleFactory.INSTANCE.decode(buffer, offset));
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
