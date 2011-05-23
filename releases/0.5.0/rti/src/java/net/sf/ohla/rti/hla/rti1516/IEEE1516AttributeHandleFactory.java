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

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleFactory;
import hla.rti1516.CouldNotDecode;
import hla.rti1516.FederateNotExecutionMember;

public class IEEE1516AttributeHandleFactory
  implements AttributeHandleFactory
{
  public static final IEEE1516AttributeHandleFactory INSTANCE = new IEEE1516AttributeHandleFactory();

  private IEEE1516AttributeHandleFactory()
  {
  }

  public AttributeHandle decode(byte[] buffer, int offset)
    throws CouldNotDecode
  {
    return new IEEE1516AttributeHandle(buffer, offset);
  }

  private Object readResolve()
  {
    return INSTANCE;
  }
}