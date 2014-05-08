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
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.messages.proto.FederationExecutionMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.FederateHandle;

public class GetFederateName
  extends
  AbstractRequest<FederationExecutionMessageProtos.GetFederateName, FederationExecutionMessageProtos.GetFederateName.Builder, GetFederateNameResponse>
implements FederationExecutionMessage
{
  public GetFederateName(FederateHandle federateHandle)
  {
    super(FederationExecutionMessageProtos.GetFederateName.newBuilder());

    builder.setFederateHandle(FederateHandles.convert(federateHandle));
  }

  public GetFederateName(CodedInputStream in)
    throws IOException
  {
    super(FederationExecutionMessageProtos.GetFederateName.newBuilder(), in);
  }

  public FederateHandle getFederateHandle()
  {
    return FederateHandles.convert(builder.getFederateHandle());
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.GET_FEDERATE_NAME;
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
    federationExecution.getFederateName(federateProxy, this);
  }
}
