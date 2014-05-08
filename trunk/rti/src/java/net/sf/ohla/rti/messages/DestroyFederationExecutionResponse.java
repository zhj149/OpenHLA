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

import java.util.Collection;

import net.sf.ohla.rti.messages.proto.ConnectedMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;

public class DestroyFederationExecutionResponse
  extends AbstractMessage<ConnectedMessageProtos.DestroyFederationExecutionResponse, ConnectedMessageProtos.DestroyFederationExecutionResponse.Builder>
  implements Response
{
  public DestroyFederationExecutionResponse(long requestId)
  {
    super(ConnectedMessageProtos.DestroyFederationExecutionResponse.newBuilder());

    builder.setRequestId(requestId);
  }

  public DestroyFederationExecutionResponse(
    long requestId, ConnectedMessageProtos.DestroyFederationExecutionResponse.Failure.Cause cause)
  {
    this(requestId);

    builder.setFailure(ConnectedMessageProtos.DestroyFederationExecutionResponse.Failure.newBuilder().setCause(cause));
  }

  public DestroyFederationExecutionResponse(long requestId, Collection<String> currentlyJoinedFederates)
  {
    this(requestId);

    builder.setFailure(ConnectedMessageProtos.DestroyFederationExecutionResponse.Failure.newBuilder().setCause(
      ConnectedMessageProtos.DestroyFederationExecutionResponse.Failure.Cause.FEDERATES_CURRENTLY_JOINED).addAllCurrentlyJoinedFederates(
      currentlyJoinedFederates));
  }

  public DestroyFederationExecutionResponse(CodedInputStream in)
    throws IOException
  {
    super(ConnectedMessageProtos.DestroyFederationExecutionResponse.newBuilder(), in);
  }

  public Collection<String> getCurrentlyJoinedFederates()
  {
    return builder.getFailure().getCurrentlyJoinedFederatesList();
  }

  public ConnectedMessageProtos.DestroyFederationExecutionResponse.Failure getFailure()
  {
    return builder.getFailure();
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.DESTROY_FEDERATION_EXECUTION_RESPONSE;
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
