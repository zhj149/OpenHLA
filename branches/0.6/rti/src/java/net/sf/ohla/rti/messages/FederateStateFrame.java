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

import net.sf.ohla.rti.Protocol;
import net.sf.ohla.rti.federate.Federate;
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;

import org.jboss.netty.buffer.ChannelBuffer;

public class FederateStateFrame
  implements Message, FederationExecutionMessage, FederateMessage
{
  public static final int HEADER_SIZE = 7;

  private final ChannelBuffer buffer;

  private final boolean last;

  public FederateStateFrame(ChannelBuffer buffer)
  {
    this.buffer = buffer;

    last = Protocol.decodeBoolean(buffer);
  }

  public FederateStateFrame(ChannelBuffer buffer, boolean last)
  {
    this.buffer = buffer;

    this.last = last;

    buffer.setInt(0, buffer.readableBytes() - 4);
    buffer.setShort(4, MessageType.FEDERATE_STATE_FRAME.ordinal());
    buffer.setByte(6, last ? 0 : 1);
  }

  public boolean isLast()
  {
    return last;
  }

  @Override
  public MessageType getType()
  {
    return MessageType.FEDERATE_STATE_FRAME;
  }

  @Override
  public ChannelBuffer getBuffer()
  {
    return buffer;
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
