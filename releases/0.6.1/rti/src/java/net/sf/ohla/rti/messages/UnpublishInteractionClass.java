/*
 * Copyright (c) 2005-2011, Michael Newcomb
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

import net.sf.ohla.rti.util.InteractionClassHandles;
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.messages.proto.FederationExecutionMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.InteractionClassHandle;

public class UnpublishInteractionClass
  extends AbstractMessage<FederationExecutionMessageProtos.UnpublishInteractionClass, FederationExecutionMessageProtos.UnpublishInteractionClass.Builder>
  implements FederationExecutionMessage
{
  public UnpublishInteractionClass(InteractionClassHandle interactionClassHandle)
  {
    super(FederationExecutionMessageProtos.UnpublishInteractionClass.newBuilder());

    builder.setInteractionClassHandle(InteractionClassHandles.convert(interactionClassHandle));
  }

  public UnpublishInteractionClass(CodedInputStream in)
    throws IOException
  {
    super(FederationExecutionMessageProtos.UnpublishInteractionClass.newBuilder(), in);
  }

  public InteractionClassHandle getInteractionClassHandle()
  {
    return InteractionClassHandles.convert(builder.getInteractionClassHandle());
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.UNPUBLISH_INTERACTION_CLASS;
  }

  @Override
  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.unpublishInteractionClass(federateProxy, this);
  }
}
