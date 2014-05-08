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

import java.util.ArrayList;
import java.util.List;

import net.sf.ohla.rti.RTI;
import net.sf.ohla.rti.fdd.FDD;
import net.sf.ohla.rti.messages.proto.MessageProtos;
import net.sf.ohla.rti.messages.proto.RTIMessageProtos;
import net.sf.ohla.rti.proto.OHLAProtos;

import org.jboss.netty.channel.ChannelHandlerContext;

import com.google.protobuf.CodedInputStream;

public class JoinFederationExecution
  extends AbstractMessage<RTIMessageProtos.JoinFederationExecution, RTIMessageProtos.JoinFederationExecution.Builder>
  implements RTIMessage
{
  private volatile List<FDD> additionalFDDs;

  public JoinFederationExecution(
    String federateName, String federateType, String federationExecutionName, List<FDD> additionalFDDs)
  {
    super(RTIMessageProtos.JoinFederationExecution.newBuilder());

    this.additionalFDDs = additionalFDDs;

    if (federateName != null)
    {
      builder.setFederateName(federateName);
    }

    builder.setFederateType(federateType);
    builder.setFederationExecutionName(federationExecutionName);

    if (additionalFDDs != null)
    {
      for (FDD fdd : additionalFDDs)
      {
        builder.addAdditionalFDDs(fdd.toProto());
      }
    }
  }

  public JoinFederationExecution(CodedInputStream in)
    throws IOException
  {
    super(RTIMessageProtos.JoinFederationExecution.newBuilder(), in);
  }

  public String getFederateName()
  {
    return builder.getFederateName();
  }

  public String getFederateType()
  {
    return builder.getFederateType();
  }

  public String getFederationExecutionName()
  {
    return builder.getFederationExecutionName();
  }

  public List<FDD> getAdditionalFDDs()
  {
    if (additionalFDDs == null)
    {
      List<FDD> additionalFDDs = new ArrayList<>(builder.getAdditionalFDDsCount());
      for (OHLAProtos.FDD fdd : builder.getAdditionalFDDsList())
      {
        additionalFDDs.add(new FDD(fdd));
      }
      this.additionalFDDs = additionalFDDs;
    }
    return additionalFDDs;
  }

  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.JOIN_FEDERATION_EXECUTION;
  }

  @Override
  public void execute(RTI rti, ChannelHandlerContext context)
  {
    rti.joinFederationExecution(context, this);
  }
}
