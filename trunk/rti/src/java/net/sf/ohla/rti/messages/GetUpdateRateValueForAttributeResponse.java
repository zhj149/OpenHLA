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

import net.sf.ohla.rti.messages.proto.FederateMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;

public class GetUpdateRateValueForAttributeResponse
  extends
  AbstractMessage<FederateMessageProtos.GetUpdateRateValueForAttributeResponse, FederateMessageProtos.GetUpdateRateValueForAttributeResponse.Builder>
  implements Response
{
  public GetUpdateRateValueForAttributeResponse(long requestId, double updateRate)
  {
    super(FederateMessageProtos.GetUpdateRateValueForAttributeResponse.newBuilder());

    builder.setRequestId(requestId);
    builder.setSuccess(
      FederateMessageProtos.GetUpdateRateValueForAttributeResponse.Success.newBuilder().setUpdateRate(updateRate));
  }

  public GetUpdateRateValueForAttributeResponse(
    long requestId, FederateMessageProtos.GetUpdateRateValueForAttributeResponse.Failure.Cause cause)
  {
    super(FederateMessageProtos.GetUpdateRateValueForAttributeResponse.newBuilder());

    builder.setRequestId(requestId);
    builder.setFailure(
      FederateMessageProtos.GetUpdateRateValueForAttributeResponse.Failure.newBuilder().setCause(cause));
  }

  public GetUpdateRateValueForAttributeResponse(CodedInputStream in)
    throws IOException
  {
    super(FederateMessageProtos.GetUpdateRateValueForAttributeResponse.newBuilder(), in);
  }

  public double getUpdateRate()
  {
    return builder.getSuccess().getUpdateRate();
  }

  public FederateMessageProtos.GetUpdateRateValueForAttributeResponse.Failure.Cause getCause()
  {
    return builder.getFailure().getCause();
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.GET_UPDATE_RATE_VALUE_FOR_ATTRIBUTE_RESPONSE;
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
