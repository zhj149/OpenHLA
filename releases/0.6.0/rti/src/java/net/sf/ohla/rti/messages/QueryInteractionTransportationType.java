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

import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandle;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.FederateHandle;
import hla.rti1516e.InteractionClassHandle;

public class QueryInteractionTransportationType
  extends InteractionClassMessage
  implements FederationExecutionMessage
{
  private final FederateHandle federateHandle;

  public QueryInteractionTransportationType(
    InteractionClassHandle interactionClassHandle, FederateHandle federateHandle)
  {
    super(MessageType.QUERY_INTERACTION_TRANSPORTATION_TYPE, interactionClassHandle);

    this.federateHandle = federateHandle;

    IEEE1516eFederateHandle.encode(buffer, federateHandle);

    encodingFinished();
  }

  public QueryInteractionTransportationType(ChannelBuffer buffer)
  {
    super(buffer);

    federateHandle = IEEE1516eFederateHandle.decode(buffer);
  }

  public FederateHandle getFederateHandle()
  {
    return federateHandle;
  }

  public MessageType getType()
  {
    return MessageType.QUERY_INTERACTION_TRANSPORTATION_TYPE;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.queryInteractionTransportationType(federateProxy, this);
  }
}
