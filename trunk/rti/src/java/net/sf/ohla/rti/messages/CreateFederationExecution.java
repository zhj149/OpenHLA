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

import net.sf.ohla.rti.RTI;
import net.sf.ohla.rti.fdd.FDD;
import net.sf.ohla.rti.messages.proto.MessageProtos;
import net.sf.ohla.rti.messages.proto.RTIMessageProtos;

import org.jboss.netty.channel.ChannelHandlerContext;

import com.google.protobuf.CodedInputStream;

public class CreateFederationExecution
  extends
  AbstractRequest<RTIMessageProtos.CreateFederationExecution, RTIMessageProtos.CreateFederationExecution.Builder, CreateFederationExecutionResponse>
  implements RTIMessage
{
  private volatile FDD fdd;

  public CreateFederationExecution(String federationExecutionName, FDD fdd, String logicalTimeImplementationName)
  {
    super(RTIMessageProtos.CreateFederationExecution.newBuilder());

    this.fdd = fdd;

    builder.setFederationExecutionName(federationExecutionName);
    builder.setFdd(fdd.toProto());
    builder.setLogicalTimeImplementationName(logicalTimeImplementationName);
  }

  public CreateFederationExecution(CodedInputStream in)
    throws IOException
  {
    super(RTIMessageProtos.CreateFederationExecution.newBuilder(), in);
  }

  public String getFederationExecutionName()
  {
    return builder.getFederationExecutionName();
  }

  public FDD getFDD()
  {
    if (fdd == null)
    {
      fdd = new FDD(builder.getFdd());
    }
    return fdd;
  }

  public String getLogicalTimeImplementationName()
  {
    return builder.getLogicalTimeImplementationName();
  }

  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.CREATE_FEDERATION_EXECUTION;
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
  public void execute(RTI rti, ChannelHandlerContext context)
  {
    rti.createFederationExecution(context, this);
  }
}
