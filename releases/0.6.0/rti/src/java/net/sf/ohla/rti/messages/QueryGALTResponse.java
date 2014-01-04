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

import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;

public class QueryGALTResponse
  extends AbstractResponse
{
  private final LogicalTime galt;

  public QueryGALTResponse(long requestId, LogicalTime galt)
  {
    super(MessageType.QUERY_GALT_RESPONSE, requestId);

    this.galt = galt;

    Protocol.encodeTime(buffer, galt);

    encodingFinished();
  }

  public QueryGALTResponse(ChannelBuffer buffer, LogicalTimeFactory factory)
  {
    super(buffer);

    galt = Protocol.decodeTime(buffer, factory);
  }

  public LogicalTime getGALT()
  {
    return galt;
  }

  public MessageType getType()
  {
    return MessageType.QUERY_GALT_RESPONSE;
  }
}
