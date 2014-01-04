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
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eRegionHandle;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.RegionHandle;

public class DeleteRegion
  extends AbstractMessage
  implements FederationExecutionMessage
{
  private final RegionHandle regionHandle;

  public DeleteRegion(RegionHandle regionHandle)
  {
    super(MessageType.DELETE_REGION);

    this.regionHandle = regionHandle;

    IEEE1516eRegionHandle.encode(buffer, regionHandle);

    encodingFinished();
  }

  public DeleteRegion(ChannelBuffer buffer)
  {
    super(buffer);

    regionHandle = IEEE1516eRegionHandle.decode(buffer);
  }

  public RegionHandle getRegionHandle()
  {
    return regionHandle;
  }

  public MessageType getType()
  {
    return MessageType.DELETE_REGION;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.deleteRegion(federateProxy, this);
  }
}
