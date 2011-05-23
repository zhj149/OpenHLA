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

import org.jboss.netty.buffer.ChannelBuffer;

public class RequestFederationSaveResponse
  extends EnumResponse<RequestFederationSaveResponse.Response>
{
  public enum Response { SUCCESS, LOGICAL_TIME_ALREADY_PASSED, SAVE_IN_PROGRESS, RESTORE_IN_PROGRESS }

  public RequestFederationSaveResponse(long id)
  {
    super(MessageType.REQUEST_FEDERATION_SAVE_RESPONSE, id, Response.SUCCESS);

    encodingFinished();
  }

  public RequestFederationSaveResponse(long id, Response response)
  {
    super(MessageType.REQUEST_FEDERATION_SAVE_RESPONSE, id, response);

    encodingFinished();
  }

  public RequestFederationSaveResponse(ChannelBuffer buffer)
  {
    super(buffer, Response.values());
  }

  public MessageType getType()
  {
    return MessageType.REQUEST_FEDERATION_SAVE_RESPONSE;
  }
}