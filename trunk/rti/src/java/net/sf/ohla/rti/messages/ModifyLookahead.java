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

import net.sf.ohla.rti.util.LogicalTimeIntervals;
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.messages.proto.FederationExecutionMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.LogicalTimeInterval;

public class ModifyLookahead
  extends AbstractMessage<FederationExecutionMessageProtos.ModifyLookahead, FederationExecutionMessageProtos.ModifyLookahead.Builder>
  implements FederationExecutionMessage
{
  private LogicalTimeInterval lookahead;

  public ModifyLookahead(LogicalTimeInterval lookahead)
  {
    super(FederationExecutionMessageProtos.ModifyLookahead.newBuilder());

    builder.setLookahead(LogicalTimeIntervals.convert(lookahead));
  }

  public ModifyLookahead(CodedInputStream in)
    throws IOException
  {
    super(FederationExecutionMessageProtos.ModifyLookahead.newBuilder(), in);
  }

  public LogicalTimeInterval getLookahead()
  {
    return lookahead;
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.MODIFY_LOOKAHEAD;
  }

  @Override
  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    lookahead = LogicalTimeIntervals.convert(
      federationExecution.getTimeManager().getLogicalTimeFactory(), builder.getLookahead());

    federationExecution.modifyLookahead(federateProxy, this);
  }
}
