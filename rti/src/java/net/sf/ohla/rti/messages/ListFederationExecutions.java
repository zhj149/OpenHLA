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

import net.sf.ohla.rti.RTI;
import net.sf.ohla.rti.messages.proto.MessageProtos;
import net.sf.ohla.rti.messages.proto.RTIMessageProtos;

import org.jboss.netty.channel.ChannelHandlerContext;

import com.google.protobuf.CodedInputStream;

public class ListFederationExecutions
  extends AbstractMessage<RTIMessageProtos.ListFederationExecutions, RTIMessageProtos.ListFederationExecutions.Builder>
  implements RTIMessage
{
  public ListFederationExecutions()
  {
    super(RTIMessageProtos.ListFederationExecutions.newBuilder());
  }

  public ListFederationExecutions(CodedInputStream in)
    throws IOException
  {
    super(RTIMessageProtos.ListFederationExecutions.newBuilder(), in);
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.LIST_FEDERATION_EXECUTIONS;
  }

  @Override
  public void execute(RTI rti, ChannelHandlerContext context)
  {
    rti.listFederationExecutions(context, this);
  }
}
