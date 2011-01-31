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
import net.sf.ohla.rti.fdd.FDD;

import org.jboss.netty.buffer.ChannelBuffer;

public class CreateFederationExecution
  extends AbstractRequest<CreateFederationExecutionResponse>
{
  private final String federationExecutionName;
  private final FDD fdd;
  private final String logicalTimeImplementationName;

  public CreateFederationExecution(String federationExecutionName, FDD fdd, String logicalTimeImplementationName)
  {
    super(MessageType.CREATE_FEDERATION_EXECUTION);

    this.federationExecutionName = federationExecutionName;
    this.fdd = fdd;
    this.logicalTimeImplementationName = logicalTimeImplementationName;

    Protocol.encodeString(buffer, federationExecutionName);
    FDD.encode(buffer, fdd);
    Protocol.encodeString(buffer, logicalTimeImplementationName);

    encodingFinished();
  }

  public CreateFederationExecution(ChannelBuffer buffer)
  {
    super(buffer);

    federationExecutionName = Protocol.decodeString(buffer);
    fdd = FDD.decode(buffer);
    logicalTimeImplementationName = Protocol.decodeString(buffer);
  }

  public String getFederationExecutionName()
  {
    return federationExecutionName;
  }

  public FDD getFDD()
  {
    return fdd;
  }

  public String getLogicalTimeImplementationName()
  {
    return logicalTimeImplementationName;
  }

  public MessageType getType()
  {
    return MessageType.CREATE_FEDERATION_EXECUTION;
  }
}
