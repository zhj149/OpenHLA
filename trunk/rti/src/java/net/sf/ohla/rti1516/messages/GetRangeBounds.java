/*
 * Copyright (c) 2006, Michael Newcomb
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

package net.sf.ohla.rti1516.messages;

import hla.rti1516.RegionHandle;
import hla.rti1516.DimensionHandle;

public class GetRangeBounds
  extends AbstractRequest
{
  protected RegionHandle regionHandle;
  protected DimensionHandle dimensionHandle;

  public GetRangeBounds(RegionHandle regionHandle, DimensionHandle dimensionHandle)
  {
    this.regionHandle = regionHandle;
    this.dimensionHandle = dimensionHandle;
  }

  public RegionHandle getRegionHandle()
  {
    return regionHandle;
  }

  public DimensionHandle getDimensionHandle()
  {
    return dimensionHandle;
  }
}