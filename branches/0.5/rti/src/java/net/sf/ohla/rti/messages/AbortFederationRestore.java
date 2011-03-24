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

import org.jboss.netty.buffer.ChannelBuffer;

public class AbortFederationRestore
  extends AbstractRequest<AbortFederationRestoreResponse>
  implements FederationExecutionMessage
{
  public AbortFederationRestore()
  {
    super(MessageType.ABORT_FEDERATION_RESTORE);

    encodingFinished();
  }

  public AbortFederationRestore(ChannelBuffer buffer)
  {
    super(buffer);
  }

  public MessageType getType()
  {
    return MessageType.ABORT_FEDERATION_RESTORE;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.abortFederationRestore(federateProxy, this);
  }
}
