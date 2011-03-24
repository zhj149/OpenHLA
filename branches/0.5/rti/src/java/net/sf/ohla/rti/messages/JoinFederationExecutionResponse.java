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
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandle;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.FederateHandle;

public class JoinFederationExecutionResponse
  extends EnumResponse<JoinFederationExecutionResponse.Response>
{
  public enum Response { SUCCESS, FEDERATION_EXECUTION_DOES_NOT_EXIST, SAVE_IN_PROGRESS, RESTORE_IN_PROGRESS,
    INCONSISTENT_FDD }

  private final FederateHandle federateHandle;
  private final FDD fdd;
  private final String logicalTimeImplementationName;

  public JoinFederationExecutionResponse(long id, Response response)
  {
    super(MessageType.JOIN_FEDERATION_EXECUTION_RESPONSE, id, response);

    assert response != Response.SUCCESS;

    federateHandle = null;
    fdd = null;
    logicalTimeImplementationName = null;

    encodingFinished();
  }

  public JoinFederationExecutionResponse(
    long id, FederateHandle federateHandle, FDD fdd, String logicalTimeImplementationName)
  {
    super(MessageType.JOIN_FEDERATION_EXECUTION_RESPONSE, id, Response.SUCCESS);

    this.federateHandle = federateHandle;
    this.fdd = fdd;
    this.logicalTimeImplementationName = logicalTimeImplementationName;

    IEEE1516eFederateHandle.encode(buffer, federateHandle);
    FDD.encode(buffer, fdd);
    Protocol.encodeString(buffer, logicalTimeImplementationName);

    encodingFinished();
  }

  public JoinFederationExecutionResponse(ChannelBuffer buffer)
  {
    super(buffer, Response.values());

    if (response == Response.SUCCESS)
    {
      federateHandle = IEEE1516eFederateHandle.decode(buffer);
      fdd = FDD.decode(buffer);
      logicalTimeImplementationName = Protocol.decodeString(buffer);
    }
    else
    {
      federateHandle = null;
      fdd = null;
      logicalTimeImplementationName = null;
    }
  }

  public FederateHandle getFederateHandle()
  {
    return federateHandle;
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
    return MessageType.JOIN_FEDERATION_EXECUTION_RESPONSE;
  }
}
