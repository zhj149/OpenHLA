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

import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.messages.proto.FederationExecutionMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;

public class GetFederateHandle
  extends
  AbstractRequest<FederationExecutionMessageProtos.GetFederateHandle, FederationExecutionMessageProtos.GetFederateHandle.Builder, GetFederateHandleResponse>
implements FederationExecutionMessage
{
  public GetFederateHandle(String federateName)
  {
    super(FederationExecutionMessageProtos.GetFederateHandle.newBuilder());

    builder.setFederateName(federateName);
  }

  public GetFederateHandle(CodedInputStream in)
    throws IOException
  {
    super(FederationExecutionMessageProtos.GetFederateHandle.newBuilder(), in);
  }

  public String getFederateName()
  {
    return builder.getFederateName();
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.GET_FEDERATE_HANDLE;
  }

  @Override
  public long getRequestId()
  {
    return builder.getRequestId();
  }

  @Override
  public void setRequestId(long requestId)
  {
    builder.setRequestId(requestId);
  }

  @Override
  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.getFederateHandle(federateProxy, this);
  }
}
