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

import java.util.List;

import net.sf.ohla.rti.Protocol;
import net.sf.ohla.rti.fdd.FDD;

import org.jboss.netty.buffer.ChannelBuffer;

public class JoinFederationExecution
  extends AbstractMessage
{
  private final String federateName;
  private final String federateType;
  private final String federationExecutionName;
  private final List<FDD> additionalFDDs;

  public JoinFederationExecution(
    String federateName, String federateType, String federationExecutionName, List<FDD> additionalFDDs)
  {
    super(MessageType.JOIN_FEDERATION_EXECUTION);

    this.federateName = federateName;
    this.federateType = federateType;
    this.federationExecutionName = federationExecutionName;
    this.additionalFDDs = additionalFDDs;

    Protocol.encodeOptionalString(buffer, federateName);
    Protocol.encodeString(buffer, federateType);
    Protocol.encodeString(buffer, federationExecutionName);
    FDD.encodeList(buffer, additionalFDDs);

    encodingFinished();
  }

  public JoinFederationExecution(ChannelBuffer buffer)
  {
    super(buffer);

    federateName = Protocol.decodeOptionalString(buffer);
    federateType = Protocol.decodeString(buffer);
    federationExecutionName = Protocol.decodeString(buffer);
    additionalFDDs = FDD.decodeList(buffer);
  }

  public String getFederationExecutionName()
  {
    return federationExecutionName;
  }

  public String getFederateName()
  {
    return federateName;
  }

  public String getFederateType()
  {
    return federateType;
  }

  public List<FDD> getAdditionalFDDs()
  {
    return additionalFDDs;
  }

  public MessageType getType()
  {
    return MessageType.JOIN_FEDERATION_EXECUTION;
  }
}
