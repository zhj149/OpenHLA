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

public class AbortFederationRestoreResponse
  extends EnumResponse<AbortFederationRestoreResponse.Response>
{
  public enum Response
  {
    SUCCESS, RESTORE_NOT_IN_PROGRESS
  }

  public AbortFederationRestoreResponse(long id, Response response)
  {
    super(MessageType.ABORT_FEDERATION_RESTORE_RESPONSE, id, response);

    encodingFinished();
  }

  public AbortFederationRestoreResponse(ChannelBuffer buffer)
  {
    super(buffer, Response.values());
  }

  public MessageType getType()
  {
    return MessageType.ABORT_FEDERATION_RESTORE_RESPONSE;
  }
}
