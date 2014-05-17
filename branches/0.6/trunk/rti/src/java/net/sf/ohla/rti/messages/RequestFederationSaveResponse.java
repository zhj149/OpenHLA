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

import com.google.protobuf.CodedInputStream;

public class RequestFederationSaveResponse
  extends
  AbstractMessage<FederateMessageProtos.RequestFederationSaveResponse, FederateMessageProtos.RequestFederationSaveResponse.Builder>
  implements Response
{
  public RequestFederationSaveResponse(long requestId)
  {
    super(FederateMessageProtos.RequestFederationSaveResponse.newBuilder());

    builder.setRequestId(requestId);
  }

  public RequestFederationSaveResponse(long requestId,
                                       FederateMessageProtos.RequestFederationSaveResponse.Failure.Cause cause)
  {
    this(requestId);

    builder.setFailure(FederateMessageProtos.RequestFederationSaveResponse.Failure.newBuilder().setCause(cause));
  }

  public RequestFederationSaveResponse(CodedInputStream in)
    throws IOException
  {
    super(FederateMessageProtos.RequestFederationSaveResponse.newBuilder(), in);
  }

  public FederateMessageProtos.RequestFederationSaveResponse.Failure.Cause getCause()
  {
    return builder.getFailure().getCause();
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.REQUEST_FEDERATION_SAVE_RESPONSE;
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
