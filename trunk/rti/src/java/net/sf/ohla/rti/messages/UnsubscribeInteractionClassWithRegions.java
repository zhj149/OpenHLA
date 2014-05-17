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

import java.io.IOException;

import net.sf.ohla.rti.util.InteractionClassHandles;
import net.sf.ohla.rti.util.RegionHandles;
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.messages.proto.FederationExecutionMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.RegionHandleSet;

public class UnsubscribeInteractionClassWithRegions
  extends AbstractMessage<FederationExecutionMessageProtos.UnsubscribeInteractionClassWithRegions, FederationExecutionMessageProtos.UnsubscribeInteractionClassWithRegions.Builder>
  implements FederationExecutionMessage
{
  public UnsubscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle, RegionHandleSet regionHandles)
  {
    super(FederationExecutionMessageProtos.UnsubscribeInteractionClassWithRegions.newBuilder());

    builder.setInteractionClassHandle(InteractionClassHandles.convert(interactionClassHandle));
    builder.addAllRegionHandles(RegionHandles.convertToProto(regionHandles));
  }

  public UnsubscribeInteractionClassWithRegions(CodedInputStream in)
    throws IOException
  {
    super(FederationExecutionMessageProtos.UnsubscribeInteractionClassWithRegions.newBuilder(), in);
  }

  public InteractionClassHandle getInteractionClassHandle()
  {
    return InteractionClassHandles.convert(builder.getInteractionClassHandle());
  }

  public RegionHandleSet getRegionHandles()
  {
    return RegionHandles.convertFromProto(builder.getRegionHandlesList());
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.UNSUBSCRIBE_INTERACTION_CLASS_WITH_REGIONS;
  }

  @Override
  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.unsubscribeInteractionClassWithRegions(federateProxy, this);
  }
}
