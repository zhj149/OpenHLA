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

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eRegionHandleSet;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.RegionHandleSet;

public class SubscribeInteractionClassWithRegions
  extends SubscribeInteractionClass
{
  private final RegionHandleSet regionHandles;

  public SubscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle, RegionHandleSet regionHandles, boolean passive)
  {
    super(MessageType.SUBSCRIBE_INTERACTION_CLASS_WITH_REGIONS, interactionClassHandle, passive, false);

    this.regionHandles = regionHandles;

    IEEE1516eRegionHandleSet.encode(buffer, regionHandles);

    encodingFinished();
  }

  public SubscribeInteractionClassWithRegions(ChannelBuffer buffer)
  {
    super(buffer);

    regionHandles = IEEE1516eRegionHandleSet.decode(buffer);
  }

  public RegionHandleSet getRegionHandles()
  {
    return regionHandles;
  }

  @Override
  public MessageType getType()
  {
    return MessageType.SUBSCRIBE_INTERACTION_CLASS_WITH_REGIONS;
  }
}
