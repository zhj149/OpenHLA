/*
 * Copyright (c) 2005-2010, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.messages;

import java.io.IOException;

import net.sf.ohla.rti.util.FederateHandles;
import net.sf.ohla.rti.messages.proto.FederateMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.FederateHandle;

public class GetFederateHandleResponse
  extends
  AbstractMessage<FederateMessageProtos.GetFederateHandleResponse, FederateMessageProtos.GetFederateHandleResponse.Builder>
  implements Response
{
  public GetFederateHandleResponse(long requestId, FederateHandle federateHandle)
  {
    super(FederateMessageProtos.GetFederateHandleResponse.newBuilder());

    builder.setRequestId(requestId);
    builder.setSuccess(FederateMessageProtos.GetFederateHandleResponse.Success.newBuilder().setFederateHandle(
      FederateHandles.convert(federateHandle)));
  }

  public GetFederateHandleResponse(long requestId, FederateMessageProtos.GetFederateHandleResponse.Failure.Cause cause)
  {
    super(FederateMessageProtos.GetFederateHandleResponse.newBuilder());

    builder.setRequestId(requestId);
    builder.setFailure(FederateMessageProtos.GetFederateHandleResponse.Failure.newBuilder().setCause(cause));
  }

  public GetFederateHandleResponse(CodedInputStream in)
    throws IOException
  {
    super(FederateMessageProtos.GetFederateHandleResponse.newBuilder(), in);
  }

  public FederateHandle getFederateHandle()
  {
    return FederateHandles.convert(builder.getSuccess().getFederateHandle());
  }

  public FederateMessageProtos.GetFederateHandleResponse.Failure.Cause getCause()
  {
    return builder.getFailure().getCause();
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.GET_FEDERATE_HANDLE_RESPONSE;
  }

  @Override
  public long getRequestId()
  {
    return builder.getRequestId();
  }

  @Override
  public boolean isSuccess()
  {
    return builder.hasSuccess();
  }

  @Override
  public boolean isFailure()
  {
    return builder.hasFailure();
  }
}
