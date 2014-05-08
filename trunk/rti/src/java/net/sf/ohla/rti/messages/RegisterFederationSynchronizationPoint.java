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
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.messages.proto.FederationExecutionMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import hla.rti1516e.FederateHandleSet;

public class RegisterFederationSynchronizationPoint
  extends AbstractMessage<FederationExecutionMessageProtos.RegisterFederationSynchronizationPoint, FederationExecutionMessageProtos.RegisterFederationSynchronizationPoint.Builder>
  implements FederationExecutionMessage
{
  private volatile FederateHandleSet federateHandles;

  public RegisterFederationSynchronizationPoint(String label, byte[] tag, FederateHandleSet federateHandles)
  {
    super(FederationExecutionMessageProtos.RegisterFederationSynchronizationPoint.newBuilder());

    this.federateHandles = federateHandles;

    builder.setLabel(label);

    if (federateHandles != null)
    {
      builder.addAllFederateHandles(FederateHandles.convertToProto(federateHandles));
    }

    if (tag != null)
    {
      builder.setTag(ByteString.copyFrom(tag));
    }
  }

  public RegisterFederationSynchronizationPoint(CodedInputStream in)
    throws IOException
  {
    super(FederationExecutionMessageProtos.RegisterFederationSynchronizationPoint.newBuilder(), in);
  }

  public String getLabel()
  {
    return builder.getLabel();
  }

  public byte[] getTag()
  {
    return builder.hasTag() ? builder.getTag().toByteArray() : null;
  }

  public FederateHandleSet getFederateHandles()
  {
    if (federateHandles == null)
    {
      federateHandles = FederateHandles.convertFromProto(builder.getFederateHandlesList());
    }
    return federateHandles;
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.REGISTER_FEDERATION_SYNCHRONIZATION_POINT;
  }

  @Override
  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.registerFederationSynchronizationPoint(federateProxy, this);
  }
}
