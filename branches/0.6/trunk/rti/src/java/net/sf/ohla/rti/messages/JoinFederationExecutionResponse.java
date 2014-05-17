/*
 * Copyright (c) 2006-2007, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import net.sf.ohla.rti.fdd.FDD;
import net.sf.ohla.rti.federate.Federate;
import net.sf.ohla.rti.messages.proto.FederateMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.FederateHandle;

public class JoinFederationExecutionResponse
  extends
  AbstractMessage<FederateMessageProtos.JoinFederationExecutionResponse, FederateMessageProtos.JoinFederationExecutionResponse.Builder>
implements FederateMessage
{
  private volatile FDD fdd;

  public JoinFederationExecutionResponse(
    String federateName, FederateHandle federateHandle, FDD fdd, String logicalTimeImplementationName)
  {
    super(FederateMessageProtos.JoinFederationExecutionResponse.newBuilder());

    this.fdd = fdd;

    builder.setSuccess(FederateMessageProtos.JoinFederationExecutionResponse.Success.newBuilder().setFederateName(
      federateName).setFederateHandle(
      FederateHandles.convert(federateHandle)).setFdd(
      fdd.toProto()).setLogicalTimeImplementationName(
      logicalTimeImplementationName));
  }

  public JoinFederationExecutionResponse(FederateMessageProtos.JoinFederationExecutionResponse.Failure.Cause cause)
  {
    super(FederateMessageProtos.JoinFederationExecutionResponse.newBuilder());

    builder.setFailure(FederateMessageProtos.JoinFederationExecutionResponse.Failure.newBuilder().setCause(cause));
  }

  public JoinFederationExecutionResponse(CodedInputStream in)
    throws IOException
  {
    super(FederateMessageProtos.JoinFederationExecutionResponse.newBuilder(), in);
  }

  public String getFederateName()
  {
    return builder.getSuccess().getFederateName();
  }

  public FederateHandle getFederateHandle()
  {
    return FederateHandles.convert(builder.getSuccess().getFederateHandle());
  }

  public FDD getFDD()
  {
    if (fdd == null)
    {
      fdd = new FDD(builder.getSuccess().getFdd());
    }
    return fdd;
  }

  public String getLogicalTimeImplementationName()
  {
    return builder.getSuccess().getLogicalTimeImplementationName();
  }

  public FederateMessageProtos.JoinFederationExecutionResponse.Failure.Cause getCause()
  {
    return builder.getFailure().getCause();
  }

  public boolean isSuccess()
  {
    return builder.hasSuccess();
  }

  public boolean isFailure()
  {
    return builder.hasFailure();
  }

  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.JOIN_FEDERATION_EXECUTION_RESPONSE;
  }

  public void execute(Federate federate)
  {
    federate.joinFederationExecutionResponse(this);
  }
}
