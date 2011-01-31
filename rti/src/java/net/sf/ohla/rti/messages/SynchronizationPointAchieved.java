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

import net.sf.ohla.rti.Protocol;
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;

import org.jboss.netty.buffer.ChannelBuffer;

public class SynchronizationPointAchieved
  extends StringMessage
  implements FederationExecutionMessage
{
  private final boolean success;

  public SynchronizationPointAchieved(String label, boolean success)
  {
    super(MessageType.SYNCHRONIZATION_POINT_ACHIEVED, label);

    this.success = success;

    Protocol.encodeBoolean(buffer, success);

    encodingFinished();
  }

  public SynchronizationPointAchieved(ChannelBuffer buffer)
  {
    super(buffer);

    success = Protocol.decodeBoolean(buffer);
  }

  public String getLabel()
  {
    return s;
  }

  public boolean isSuccess()
  {
    return success;
  }

  public MessageType getType()
  {
    return MessageType.SYNCHRONIZATION_POINT_ACHIEVED;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.synchronizationPointAchieved(federateProxy, this);
  }
}
