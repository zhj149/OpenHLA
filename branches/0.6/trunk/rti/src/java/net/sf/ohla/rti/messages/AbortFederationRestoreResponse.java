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

import java.io.IOException;

import net.sf.ohla.rti.messages.proto.FederateMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import org.jboss.netty.buffer.ChannelBuffer;

import com.google.protobuf.CodedInputStream;

public class AbortFederationRestoreResponse
  extends
  AbstractMessage<FederateMessageProtos.AbortFederationRestoreResponse, FederateMessageProtos.AbortFederationRestoreResponse.Builder>
  implements Response
{
  public AbortFederationRestoreResponse(long requestId)
  {
    super(FederateMessageProtos.AbortFederationRestoreResponse.newBuilder());

    builder.setRequestId(requestId);
  }

  public AbortFederationRestoreResponse(
    long requestId, FederateMessageProtos.AbortFederationRestoreResponse.Failure.Cause cause)
  {
    this(requestId);

    builder.setFailure(FederateMessageProtos.AbortFederationRestoreResponse.Failure.newBuilder().setCause(cause));
  }

  public AbortFederationRestoreResponse(CodedInputStream in)
    throws IOException
  {
    super(FederateMessageProtos.AbortFederationRestoreResponse.newBuilder(), in);
  }

  public FederateMessageProtos.AbortFederationRestoreResponse.Failure.Cause getCause()
  {
    return builder.getFailure().getCause();
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.ABORT_FEDERATION_RESTORE_RESPONSE;
  }

  @Override
  public long getRequestId()
  {
    return builder.getRequestId();
  }

  @Override
  public boolean isSuccess()
  {
    return !builder.hasFailure();
  }

  @Override
  public boolean isFailure()
  {
    return builder.hasFailure();
  }
}
