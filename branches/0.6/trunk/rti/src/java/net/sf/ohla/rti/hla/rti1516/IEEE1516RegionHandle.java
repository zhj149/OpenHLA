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

import hla.rti1516.RegionHandle;

public class IEEE1516RegionHandle
  implements RegionHandle
{
  private final hla.rti1516e.RegionHandle regionHandle;

  public IEEE1516RegionHandle(hla.rti1516e.RegionHandle regionHandle)
  {
    this.regionHandle = regionHandle;
  }

  public hla.rti1516e.RegionHandle getIEEE1516eRegionHandle()
  {
    return regionHandle;
  }

  @Override
  public int hashCode()
  {
    return regionHandle.hashCode();
  }

  @Override
  public boolean equals(Object rhs)
  {
    return this == rhs || (rhs instanceof IEEE1516RegionHandle &&
                           regionHandle.equals(((IEEE1516RegionHandle) rhs).regionHandle));
  }

  @Override
  public String toString()
  {
    return regionHandle.toString();
  }
}
