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

import net.sf.ohla.rti.Protocol;

import org.jboss.netty.buffer.ChannelBuffer;

public class GetFederateNameResponse
  extends EnumResponse<GetFederateNameResponse.Response>
{
  public enum Response { SUCCESS, FEDERATE_HANDLE_NOT_KNOWN }

  private final String federateName;

  public GetFederateNameResponse(long id, Response response)
  {
    super(MessageType.GET_FEDERATE_NAME_RESPONSE, id, response);

    assert response != Response.SUCCESS;

    federateName = null;

    encodingFinished();
  }

  public GetFederateNameResponse(long id, String federateName)
  {
    super(MessageType.GET_FEDERATE_NAME_RESPONSE, id, Response.SUCCESS);

    assert federateName != null;

    this.federateName = federateName;

    Protocol.encodeString(buffer, federateName);

    encodingFinished();
  }

  public GetFederateNameResponse(ChannelBuffer buffer)
  {
    super(buffer, Response.values());

    federateName = response == Response.SUCCESS ? Protocol.decodeString(buffer) : null;
  }

  public String getFederateName()
  {
    return federateName;
  }

  public MessageType getType()
  {
    return MessageType.GET_FEDERATE_NAME_RESPONSE;
  }
}
