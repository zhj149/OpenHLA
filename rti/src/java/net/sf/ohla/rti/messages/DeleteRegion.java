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

import java.io.IOException;

import net.sf.ohla.rti.util.RegionHandles;
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.messages.proto.FederationExecutionMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.RegionHandle;

public class DeleteRegion
  extends AbstractMessage<FederationExecutionMessageProtos.DeleteRegion, FederationExecutionMessageProtos.DeleteRegion.Builder>
  implements FederationExecutionMessage
{
  public DeleteRegion(RegionHandle regionHandle)
  {
    super(FederationExecutionMessageProtos.DeleteRegion.newBuilder());

    builder.setRegionHandle(RegionHandles.convert(regionHandle));
  }

  public DeleteRegion(CodedInputStream in)
    throws IOException
  {
    super(FederationExecutionMessageProtos.DeleteRegion.newBuilder(), in);
  }

  public RegionHandle getRegionHandle()
  {
    return RegionHandles.convert(builder.getRegionHandle());
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.DELETE_REGION;
  }

  @Override
  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.deleteRegion(federateProxy, this);
  }
}
