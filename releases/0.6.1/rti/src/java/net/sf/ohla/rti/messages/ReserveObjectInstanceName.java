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

import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.messages.proto.FederationExecutionMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;

public class ReserveObjectInstanceName
  extends AbstractMessage<FederationExecutionMessageProtos.ReserveObjectInstanceName, FederationExecutionMessageProtos.ReserveObjectInstanceName.Builder>
  implements FederationExecutionMessage
{
  public ReserveObjectInstanceName(String objectInstanceName)
  {
    super(FederationExecutionMessageProtos.ReserveObjectInstanceName.newBuilder());

    builder.setObjectInstanceName(objectInstanceName);
  }

  public ReserveObjectInstanceName(CodedInputStream in)
    throws IOException
  {
    super(FederationExecutionMessageProtos.ReserveObjectInstanceName.newBuilder(), in);
  }

  public String getObjectInstanceName()
  {
    return builder.getObjectInstanceName();
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.RESERVE_OBJECT_INSTANCE_NAME;
  }

  @Override
  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.reserveObjectInstanceName(federateProxy, this);
  }
}
