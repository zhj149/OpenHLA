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
import net.sf.ohla.rti.proto.OHLAProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.ResignAction;

public class SetAutomaticResignDirective
  extends AbstractMessage<FederationExecutionMessageProtos.SetAutomaticResignDirective, FederationExecutionMessageProtos.SetAutomaticResignDirective.Builder>
  implements FederationExecutionMessage
{
  public SetAutomaticResignDirective(ResignAction resignAction)
  {
    super(FederationExecutionMessageProtos.SetAutomaticResignDirective.newBuilder());

    builder.setResignAction(OHLAProtos.ResignAction.values()[resignAction.ordinal()]);
  }

  public SetAutomaticResignDirective(CodedInputStream in)
    throws IOException
  {
    super(FederationExecutionMessageProtos.SetAutomaticResignDirective.newBuilder(), in);
  }

  public ResignAction getResignAction()
  {
    return ResignAction.values()[builder.getResignAction().ordinal()];
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.SET_AUTOMATIC_RESIGN_DIRECTIVE;
  }

  @Override
  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.setAutomaticResignDirective(federateProxy, this);
  }
}
