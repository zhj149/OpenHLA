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

import java.io.IOException;

import net.sf.ohla.rti.util.LogicalTimes;
import net.sf.ohla.rti.messages.proto.FederateMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;

public class QueryGALTResponse
  extends AbstractMessage<FederateMessageProtos.QueryGALTResponse, FederateMessageProtos.QueryGALTResponse.Builder>
  implements Response
{
  private volatile LogicalTime galt;

  public QueryGALTResponse(long requestId, LogicalTime galt)
  {
    super(FederateMessageProtos.QueryGALTResponse.newBuilder());

    this.galt = galt;

    builder.setRequestId(requestId);

    if (galt != null)
    {
      builder.setGalt(LogicalTimes.convert(galt));
    }
  }

  public QueryGALTResponse(CodedInputStream in)
    throws IOException
  {
    super(FederateMessageProtos.QueryGALTResponse.newBuilder(), in);
  }

  public LogicalTime getGALT(LogicalTimeFactory logicalTimeFactory)
  {
    if (galt == null && builder.hasGalt())
    {
      galt = LogicalTimes.convert(logicalTimeFactory, builder.getGalt());
    }
    return galt;
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.QUERY_GALT_RESPONSE;
  }

  @Override
  public long getRequestId()
  {
    return builder.getRequestId();
  }

  @Override
  public boolean isSuccess()
  {
    return true;
  }

  @Override
  public boolean isFailure()
  {
    return false;
  }
}
