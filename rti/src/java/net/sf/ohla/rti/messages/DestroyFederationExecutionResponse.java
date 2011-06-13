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

import java.util.Collections;
import java.util.Set;

import net.sf.ohla.rti.Protocol;

import org.jboss.netty.buffer.ChannelBuffer;

public class DestroyFederationExecutionResponse
  extends EnumResponse<DestroyFederationExecutionResponse.Response>
{
  public enum Response
  {
    SUCCESS, FEDERATES_CURRENTLY_JOINED, FEDERATION_EXECUTION_DOES_NOT_EXIST
  }

  private final Set<String> currentlyJoinedFederates;

  public DestroyFederationExecutionResponse(long id, Response response)
  {
    super(MessageType.DESTROY_FEDERATION_EXECUTION_RESPONSE, id, response);

    assert response != DestroyFederationExecutionResponse.Response.FEDERATES_CURRENTLY_JOINED;

    currentlyJoinedFederates = null;

    encodingFinished();
  }

  public DestroyFederationExecutionResponse(long id, Set<String> currentlyJoinedFederates)
  {
    super(MessageType.DESTROY_FEDERATION_EXECUTION_RESPONSE, id, Response.FEDERATES_CURRENTLY_JOINED);

    this.currentlyJoinedFederates = currentlyJoinedFederates;

    Protocol.encodeStrings(buffer, currentlyJoinedFederates);

    encodingFinished();
  }

  public DestroyFederationExecutionResponse(ChannelBuffer buffer)
  {
    super(buffer, Response.values());

    if (response == Response.FEDERATES_CURRENTLY_JOINED)
    {
      currentlyJoinedFederates = Protocol.decodeStringSet(buffer);
    }
    else
    {
      currentlyJoinedFederates = Collections.emptySet();
    }
  }

  public Set<String> getCurrentlyJoinedFederates()
  {
    return currentlyJoinedFederates;
  }

  public MessageType getType()
  {
    return MessageType.DESTROY_FEDERATION_EXECUTION_RESPONSE;
  }
}
