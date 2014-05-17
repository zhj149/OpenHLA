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

import net.sf.ohla.rti.federate.Federate;
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;

public class FederateStateFrame
  extends AbstractMessage<MessageProtos.FederateStateFrame, MessageProtos.FederateStateFrame.Builder>
  implements FederationExecutionMessage, FederateMessage
{
  public FederateStateFrame(byte[] buffer, int offset, int length, boolean last)
  {
    super(MessageProtos.FederateStateFrame.newBuilder());

    builder.setPayload(ByteString.copyFrom(buffer, offset, length));
    builder.setLast(last);
  }

  public FederateStateFrame(CodedInputStream in)
    throws IOException
  {
    super(MessageProtos.FederateStateFrame.newBuilder(), in);
  }

  public ByteString getPayload()
  {
    return builder.getPayload();
  }

  public boolean isLast()
  {
    return builder.getLast();
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.FEDERATE_STATE_FRAME;
  }

  @Override
  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.handleFederateStateFrame(federateProxy, this);
  }

  @Override
  public void execute(Federate federate)
  {
    federate.handleFederateStateFrame(this);
  }
}
