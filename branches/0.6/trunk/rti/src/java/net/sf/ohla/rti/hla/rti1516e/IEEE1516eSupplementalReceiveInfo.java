/*
 * Copyright (c) 2005-2011, Michael Newcomb
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

import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.RegionHandleSet;

public class IEEE1516eSupplementalReceiveInfo
  implements FederateAmbassador.SupplementalReceiveInfo
{
  private final FederateHandle producingFederate;
  private final RegionHandleSet sentRegions;

  public IEEE1516eSupplementalReceiveInfo(FederateHandle producingFederate, RegionHandleSet sentRegions)
  {
    this.producingFederate = producingFederate;
    this.sentRegions = sentRegions;
  }

  @Override
  public boolean hasProducingFederate()
  {
    return producingFederate != null;
  }

  @Override
  public boolean hasSentRegions()
  {
    return sentRegions != null;
  }

  @Override
  public FederateHandle getProducingFederate()
  {
    return producingFederate;
  }

  @Override
  public RegionHandleSet getSentRegions()
  {
    return sentRegions;
  }
}
