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

public abstract class EnumResponse<E extends Enum>
  extends AbstractResponse
{
  protected final E response;

  protected EnumResponse(MessageType messageType, long id, E response)
  {
    super(messageType, id);

    this.response = response;

    Protocol.encodeEnum(buffer, response);
  }

  protected EnumResponse(MessageType messageType, int capacity, boolean dynamic, long id, E response)
  {
    super(messageType, capacity, dynamic, id);

    this.response = response;

    Protocol.encodeEnum(buffer, response);
  }

  protected EnumResponse(MessageType messageType, ChannelBuffer buffer, long id, E response)
  {
    super(messageType, buffer, id);

    this.response = response;

    Protocol.encodeEnum(buffer, response);
  }

  protected EnumResponse(ChannelBuffer buffer, E[] values)
  {
    super(buffer);

    response = Protocol.decodeEnum(buffer, values);
  }

  public E getResponse()
  {
    return response;
  }
}
