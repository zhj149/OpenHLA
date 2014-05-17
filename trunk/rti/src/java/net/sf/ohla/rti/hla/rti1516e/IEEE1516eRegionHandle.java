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

import hla.rti1516e.FederateHandle;
import hla.rti1516e.RegionHandle;

public class IEEE1516eRegionHandle
  implements RegionHandle
{
  private final FederateHandle federateHandle;

  private final int regionHandle;

  public IEEE1516eRegionHandle(FederateHandle federateHandle, int regionHandle)
  {
    this.federateHandle = federateHandle;
    this.regionHandle = regionHandle;
  }

  public FederateHandle getFederateHandle()
  {
    return federateHandle;
  }

  public int getRegionHandle()
  {
    return regionHandle;
  }

  @Override
  public int hashCode()
  {
    return regionHandle & (federateHandle.hashCode() << 24);
  }

  @Override
  public boolean equals(Object rhs)
  {
    return this == rhs || (rhs instanceof IEEE1516eRegionHandle && equals((IEEE1516eRegionHandle) rhs));
  }

  @Override
  public String toString()
  {
    return new StringBuilder(federateHandle.toString()).append(".").append(regionHandle).toString();
  }

  private boolean equals(IEEE1516eRegionHandle rhs)
  {
    return regionHandle == rhs.regionHandle && federateHandle.equals(rhs.federateHandle);
  }
}
