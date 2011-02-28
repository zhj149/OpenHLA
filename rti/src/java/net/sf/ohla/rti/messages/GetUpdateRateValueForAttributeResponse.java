/*
 * Copyright (c) 2005-2011, Michael Newcomb
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

public class GetUpdateRateValueForAttributeResponse
  extends EnumResponse<GetUpdateRateValueForAttributeResponse.Response>
{
  public enum Response { SUCCESS, OBJECT_INSTANCE_NOT_KNOWN }

  private final double updateRate;

  public GetUpdateRateValueForAttributeResponse(long requestId, double updateRate)
  {
    super(MessageType.GET_UPDATE_RATE_VALUE_FOR_ATTRIBUTE_RESPONSE, requestId, Response.SUCCESS);

    this.updateRate = updateRate;

    Protocol.encodeDouble(buffer, updateRate);

    encodingFinished();
  }

  public GetUpdateRateValueForAttributeResponse(long id, Response response)
  {
    super(MessageType.GET_UPDATE_RATE_VALUE_FOR_ATTRIBUTE_RESPONSE, id, response);

    assert response != GetUpdateRateValueForAttributeResponse.Response.SUCCESS;

    encodingFinished();

    updateRate = 0.0;
  }

  public GetUpdateRateValueForAttributeResponse(ChannelBuffer buffer)
  {
    super(buffer, Response.values());

    updateRate = response == GetUpdateRateValueForAttributeResponse.Response.SUCCESS ?
      Protocol.decodeDouble(buffer) : 0.0;
  }

  public double getUpdateRate()
  {
    return updateRate;
  }

  public MessageType getType()
  {
    return MessageType.GET_UPDATE_RATE_VALUE_FOR_ATTRIBUTE_RESPONSE;
  }
}
