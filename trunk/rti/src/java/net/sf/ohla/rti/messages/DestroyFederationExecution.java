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
import net.sf.ohla.rti.messages.proto.MessageProtos;
import net.sf.ohla.rti.messages.proto.RTIMessageProtos;

import org.jboss.netty.channel.ChannelHandlerContext;

import com.google.protobuf.CodedInputStream;

public class DestroyFederationExecution
  extends
  AbstractRequest<RTIMessageProtos.DestroyFederationExecution, RTIMessageProtos.DestroyFederationExecution.Builder, DestroyFederationExecutionResponse>
  implements RTIMessage
{
  public DestroyFederationExecution(String federationExecutionName)
  {
    super(RTIMessageProtos.DestroyFederationExecution.newBuilder());

    builder.setFederationExecutionName(federationExecutionName);
  }

  public DestroyFederationExecution(CodedInputStream in)
    throws IOException
  {
    super(RTIMessageProtos.DestroyFederationExecution.newBuilder(), in);
  }

  public String getFederationExecutionName()
  {
    return builder.getFederationExecutionName();
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.DESTROY_FEDERATION_EXECUTION;
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
    rti.destroyFederationExecution(context, this);
  }
}
