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

import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;

public class RequestFederationSave
  extends StringRequest<RequestFederationSaveResponse>
  implements FederationExecutionMessage
{
  private final LogicalTime time;

  public RequestFederationSave(String label)
  {
    super(MessageType.REQUEST_FEDERATION_SAVE, label);

    time = null;

    encodingFinished();
  }

  public RequestFederationSave(String label, LogicalTime time)
  {
    super(MessageType.REQUEST_FEDERATION_SAVE, label);

    this.time = time;

    Protocol.encodeTime(buffer, time);

    encodingFinished();
  }

  public RequestFederationSave(ChannelBuffer buffer, LogicalTimeFactory factory)
  {
    super(buffer);

    time = Protocol.decodeTime(buffer, factory);
  }

  public String getLabel()
  {
    return s;
  }

  public LogicalTime getTime()
  {
    return time;
  }

  public MessageType getType()
  {
    return MessageType.REQUEST_FEDERATION_SAVE;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.requestFederationSave(federateProxy, this);
  }
}
