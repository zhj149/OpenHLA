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

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandle;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.FederateHandle;

public class GetFederateHandleResponse
  extends EnumResponse<GetFederateHandleResponse.Response>
{
  public enum Response
  {
    SUCCESS, NAME_NOT_FOUND
  }

  private final FederateHandle federateHandle;

  public GetFederateHandleResponse(long id, Response response)
  {
    super(MessageType.GET_FEDERATE_HANDLE_RESPONSE, id, response);

    assert response != Response.SUCCESS;

    federateHandle = null;

    encodingFinished();
  }

  public GetFederateHandleResponse(long id, FederateHandle federateHandle)
  {
    super(MessageType.GET_FEDERATE_HANDLE_RESPONSE, id, Response.SUCCESS);

    assert federateHandle != null;

    this.federateHandle = federateHandle;

    IEEE1516eFederateHandle.encode(buffer, federateHandle);

    encodingFinished();
  }

  public GetFederateHandleResponse(ChannelBuffer buffer)
  {
    super(buffer, Response.values());

    federateHandle = response == Response.SUCCESS ? IEEE1516eFederateHandle.decode(buffer) : null;
  }

  public FederateHandle getFederateHandle()
  {
    return federateHandle;
  }

  public MessageType getType()
  {
    return MessageType.GET_FEDERATE_HANDLE_RESPONSE;
  }
}
