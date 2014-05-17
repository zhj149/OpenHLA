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

import net.sf.ohla.rti.messages.proto.FederateMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;

public class GetFederateNameResponse
  extends
  AbstractMessage<FederateMessageProtos.GetFederateNameResponse, FederateMessageProtos.GetFederateNameResponse.Builder>
  implements Response
{
  public GetFederateNameResponse(long requestId, String federateName)
  {
    super(FederateMessageProtos.GetFederateNameResponse.newBuilder());

    builder.setRequestId(requestId);
    builder.setSuccess(
      FederateMessageProtos.GetFederateNameResponse.Success.newBuilder().setFederateName(federateName));
  }

  public GetFederateNameResponse(long requestId, FederateMessageProtos.GetFederateNameResponse.Failure.Cause cause)
  {
    super(FederateMessageProtos.GetFederateNameResponse.newBuilder());

    builder.setRequestId(requestId);
    builder.setFailure(FederateMessageProtos.GetFederateNameResponse.Failure.newBuilder().setCause(cause));
  }

  public GetFederateNameResponse(CodedInputStream in)
    throws IOException
  {
    super(FederateMessageProtos.GetFederateNameResponse.newBuilder(), in);
  }

  public String getFederateName()
  {
    return builder.getSuccess().getFederateName();
  }

  public FederateMessageProtos.GetFederateNameResponse.Failure.Cause getCause()
  {
    return builder.getFailure().getCause();
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.GET_FEDERATE_NAME_RESPONSE;
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
