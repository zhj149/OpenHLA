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

import java.util.HashSet;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandleSetFactory;

import hla.rti1516.FederateHandle;
import hla.rti1516.FederateHandleSet;

public class IEEE1516FederateHandleSet
  extends HashSet<FederateHandle>
  implements FederateHandleSet
{
  public IEEE1516FederateHandleSet()
  {
  }

  public IEEE1516FederateHandleSet(hla.rti1516e.FederateHandleSet federateHandles)
  {
    for (hla.rti1516e.FederateHandle federateHandle : federateHandles)
    {
      add(new IEEE1516FederateHandle(federateHandle));
    }
  }

  public static hla.rti1516e.FederateHandleSet createIEEE1516eFederateHandleSet(FederateHandleSet federateHandles)
  {
    hla.rti1516e.FederateHandleSet ieee1516eFederateHandles = IEEE1516eFederateHandleSetFactory.INSTANCE.create();
    for (FederateHandle federateHandle : federateHandles)
    {
      ieee1516eFederateHandles.add(((IEEE1516FederateHandle) federateHandle).getFederateHandle());
    }
    return ieee1516eFederateHandles;
  }
}
