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

public abstract class AbstractResponse
  extends AbstractMessage
  implements Response
{
  protected final long requestId;

  protected AbstractResponse(MessageType messageType, long requestId)
  {
    super(messageType);

    this.requestId = requestId;

    buffer.writeLong(requestId);
  }

  protected AbstractResponse(MessageType messageType, int capacity, boolean dynamic, long requestId)
  {
    super(messageType, capacity, dynamic);

    this.requestId = requestId;

    buffer.writeLong(requestId);
  }

  protected AbstractResponse(MessageType messageType, ChannelBuffer buffer, long requestId)
  {
    super(messageType, buffer);

    this.requestId = requestId;

    buffer.writeLong(requestId);
  }

  protected AbstractResponse(ChannelBuffer buffer)
  {
    super(buffer);

    requestId = buffer.readLong();
  }

  public long getRequestId()
  {
    return requestId;
  }
}
