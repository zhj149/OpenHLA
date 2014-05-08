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
import net.sf.ohla.rti.proto.OHLAProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.ResignAction;

public class ResignFederationExecution
  extends
  AbstractMessage<FederationExecutionMessageProtos.ResignFederationExecution, FederationExecutionMessageProtos.ResignFederationExecution.Builder>
implements FederationExecutionMessage
{
  public ResignFederationExecution(ResignAction resignAction)
  {
    super(FederationExecutionMessageProtos.ResignFederationExecution.newBuilder());

    builder.setResignAction(OHLAProtos.ResignAction.values()[resignAction.ordinal()]);
  }

  public ResignFederationExecution(CodedInputStream in)
    throws IOException
  {
    super(FederationExecutionMessageProtos.ResignFederationExecution.newBuilder(), in);
  }

  public ResignAction getResignAction()
  {
    return ResignAction.values()[builder.getResignAction().ordinal()];
  }

  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.RESIGN_FEDERATION_EXECUTION;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.resignFederationExecution(federateProxy, this);
  }
}
