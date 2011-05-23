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

import hla.rti1516.CouldNotDecode;
import hla.rti1516.FederateNotExecutionMember;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ObjectInstanceHandleFactory;

public class IEEE1516ObjectInstanceHandleFactory
  implements ObjectInstanceHandleFactory
{
  public static final IEEE1516ObjectInstanceHandleFactory INSTANCE = new IEEE1516ObjectInstanceHandleFactory();

  private IEEE1516ObjectInstanceHandleFactory()
  {
  }

  public ObjectInstanceHandle decode(byte[] buffer, int offset)
    throws CouldNotDecode, FederateNotExecutionMember
  {
    return new IEEE1516ObjectInstanceHandle(buffer, offset);
  }

  private Object readResolve()
  {
    return INSTANCE;
  }
}