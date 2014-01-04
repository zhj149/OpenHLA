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

import net.sf.ohla.rti.IntegerHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandle;

import hla.rti1516.CouldNotDecode;
import hla.rti1516.FederateHandle;

public class IEEE1516FederateHandle
  extends IntegerHandle
  implements FederateHandle
{
  public IEEE1516FederateHandle(byte[] buffer, int offset)
    throws CouldNotDecode
  {
    super(buffer, offset);
  }

  public IEEE1516FederateHandle(hla.rti1516e.FederateHandle federateHandle)
  {
    super((IEEE1516eFederateHandle) federateHandle);
  }
}
