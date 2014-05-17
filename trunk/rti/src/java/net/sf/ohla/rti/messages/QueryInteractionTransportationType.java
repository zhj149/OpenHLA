/*
 * Copyright (c) 2005-2010, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.messages;

import java.io.IOException;

import net.sf.ohla.rti.util.FederateHandles;
import net.sf.ohla.rti.util.InteractionClassHandles;
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.messages.proto.FederationExecutionMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.InteractionClassHandle;

public class QueryInteractionTransportationType
  extends AbstractMessage<FederationExecutionMessageProtos.QueryInteractionTransportationType, FederationExecutionMessageProtos.QueryInteractionTransportationType.Builder>
  implements FederationExecutionMessage
{
  public QueryInteractionTransportationType(
    InteractionClassHandle interactionClassHandle, FederateHandle federateHandle)
  {
    super(FederationExecutionMessageProtos.QueryInteractionTransportationType.newBuilder());

    builder.setInteractionClassHandle(InteractionClassHandles.convert(interactionClassHandle));
    builder.setFederateHandle(FederateHandles.convert(federateHandle));
  }

  public QueryInteractionTransportationType(CodedInputStream in)
    throws IOException
  {
    super(FederationExecutionMessageProtos.QueryInteractionTransportationType.newBuilder(), in);
  }

  public InteractionClassHandle getInteractionClassHandle()
  {
    return InteractionClassHandles.convert(builder.getInteractionClassHandle());
  }

  public FederateHandle getFederateHandle()
  {
    return FederateHandles.convert(builder.getFederateHandle());
  }

  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.QUERY_INTERACTION_TRANSPORTATION_TYPE;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.queryInteractionTransportationType(federateProxy, this);
  }
}
