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

package net.sf.ohla.rti.messages;

import java.util.Map;

import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;

import hla.rti1516.DimensionHandle;
import hla.rti1516.RangeBounds;
import hla.rti1516.RegionHandle;

public class CommitRegionModifications
  extends AbstractRequest
  implements FederationExecutionMessage
{
  protected Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regionModifications;

  public CommitRegionModifications(
    Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regionModifications)
  {
    this.regionModifications = regionModifications;
  }

  public Map<RegionHandle, Map<DimensionHandle, RangeBounds>> getRegionModifications()
  {
    return regionModifications;
  }

  public void execute(FederationExecution federationExecution,
                      FederateProxy federateProxy)
  {
    federationExecution.commitRegionModifications(federateProxy, this);
  }
}
