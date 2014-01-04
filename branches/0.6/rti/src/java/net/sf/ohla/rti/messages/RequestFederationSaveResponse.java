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

import net.sf.ohla.rti.Protocol;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;

public class RequestFederationSaveResponse
  extends EnumResponse<RequestFederationSaveResponse.Response>
{
  public enum Response
  {
    SUCCESS, LOGICAL_TIME_ALREADY_PASSED, FEDERATE_UNABLE_TO_USE_TIME, SAVE_IN_PROGRESS, RESTORE_IN_PROGRESS,
    RTI_INTERNAL_ERROR
  }

  private final LogicalTime time;

  public RequestFederationSaveResponse(long id)
  {
    super(MessageType.REQUEST_FEDERATION_SAVE_RESPONSE, id, Response.SUCCESS);

    time = null;

    Protocol.encodeNullTime(buffer);

    encodingFinished();
  }

  public RequestFederationSaveResponse(long id, Response response)
  {
    super(MessageType.REQUEST_FEDERATION_SAVE_RESPONSE, id, response);

    time = null;

    Protocol.encodeNullTime(buffer);

    encodingFinished();
  }

  public RequestFederationSaveResponse(long id, LogicalTime time)
  {
    super(MessageType.REQUEST_FEDERATION_SAVE_RESPONSE, id, Response.LOGICAL_TIME_ALREADY_PASSED);

    this.time = time;

    Protocol.encodeTime(buffer, time);

    encodingFinished();
  }

  public RequestFederationSaveResponse(ChannelBuffer buffer, LogicalTimeFactory logicalTimeFactory)
  {
    super(buffer, Response.values());

    time = Protocol.decodeTime(buffer, logicalTimeFactory);
  }

  public LogicalTime getTime()
  {
    return time;
  }

  public MessageType getType()
  {
    return MessageType.REQUEST_FEDERATION_SAVE_RESPONSE;
  }
}
