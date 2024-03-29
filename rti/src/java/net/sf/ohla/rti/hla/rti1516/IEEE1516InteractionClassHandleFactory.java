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

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eInteractionClassHandleFactory;

import hla.rti1516.CouldNotDecode;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.InteractionClassHandleFactory;

public class IEEE1516InteractionClassHandleFactory
  implements InteractionClassHandleFactory
{
  public static final IEEE1516InteractionClassHandleFactory INSTANCE = new IEEE1516InteractionClassHandleFactory();

  private IEEE1516InteractionClassHandleFactory()
  {
  }

  public InteractionClassHandle decode(byte[] buffer, int offset)
    throws CouldNotDecode
  {
    try
    {
      return new IEEE1516InteractionClassHandle(IEEE1516eInteractionClassHandleFactory.INSTANCE.decode(buffer, offset));
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
