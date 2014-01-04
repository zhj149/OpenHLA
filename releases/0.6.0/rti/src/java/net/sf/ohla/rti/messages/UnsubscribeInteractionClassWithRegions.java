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

package net.sf.ohla.rti.messages;

import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eRegionHandleSet;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.RegionHandleSet;

public class UnsubscribeInteractionClassWithRegions
  extends InteractionClassMessage
  implements FederationExecutionMessage
{
  private final RegionHandleSet regionHandles;

  public UnsubscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle, RegionHandleSet regionHandles)
  {
    super(MessageType.UNSUBSCRIBE_INTERACTION_CLASS_WITH_REGIONS, interactionClassHandle);

    this.regionHandles = regionHandles;

    IEEE1516eRegionHandleSet.encode(buffer, regionHandles);

    encodingFinished();
  }

  public UnsubscribeInteractionClassWithRegions(ChannelBuffer buffer)
  {
    super(buffer);

    regionHandles = IEEE1516eRegionHandleSet.decode(buffer);
  }

  public RegionHandleSet getRegionHandles()
  {
    return regionHandles;
  }

  public MessageType getType()
  {
    return MessageType.UNSUBSCRIBE_INTERACTION_CLASS_WITH_REGIONS;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.unsubscribeInteractionClassWithRegions(federateProxy, this);
  }
}
