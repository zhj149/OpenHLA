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

package net.sf.ohla.rti.messages;

import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandleSet;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eRegionHandle;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.DimensionHandleSet;
import hla.rti1516e.RegionHandle;

public class CreateRegion
  extends AbstractMessage
  implements FederationExecutionMessage
{
  private final RegionHandle regionHandle;
  private final DimensionHandleSet dimensionHandles;

  public CreateRegion(RegionHandle regionHandle, DimensionHandleSet dimensionHandles)
  {
    super(MessageType.CREATE_REGION);

    this.regionHandle = regionHandle;
    this.dimensionHandles = dimensionHandles;

    IEEE1516eRegionHandle.encode(buffer, regionHandle);
    IEEE1516eDimensionHandleSet.encode(buffer, dimensionHandles);

    encodingFinished();
  }

  public CreateRegion(ChannelBuffer buffer)
  {
    super(buffer);

    regionHandle = IEEE1516eRegionHandle.decode(buffer);
    dimensionHandles = IEEE1516eDimensionHandleSet.decode(buffer);
  }

  public RegionHandle getRegionHandle()
  {
    return regionHandle;
  }

  public DimensionHandleSet getDimensionHandles()
  {
    return dimensionHandles;
  }

  public MessageType getType()
  {
    return MessageType.CREATE_REGION;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.createRegion(federateProxy, this);
  }
}
