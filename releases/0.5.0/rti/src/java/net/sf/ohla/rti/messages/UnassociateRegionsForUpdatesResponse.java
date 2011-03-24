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

import org.jboss.netty.buffer.ChannelBuffer;

public class UnassociateRegionsForUpdatesResponse
  extends EnumResponse<UnassociateRegionsForUpdatesResponse.Response>
{
  public enum Response { SUCCESS, OBJECT_INSTANCE_NOT_KNOWN }

  public UnassociateRegionsForUpdatesResponse(long id, Response response)
  {
    super(MessageType.UNASSOCIATE_REGIONS_FOR_UPDATES_RESPONSE, id, response);

    encodingFinished();
  }

  public UnassociateRegionsForUpdatesResponse(ChannelBuffer buffer)
  {
    super(buffer, Response.values());
  }

  public MessageType getType()
  {
    return MessageType.UNASSOCIATE_REGIONS_FOR_UPDATES_RESPONSE;
  }
}
