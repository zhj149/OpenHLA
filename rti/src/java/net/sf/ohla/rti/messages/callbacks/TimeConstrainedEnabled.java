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

package net.sf.ohla.rti.messages.callbacks;

import java.io.IOException;

import net.sf.ohla.rti.util.LogicalTimes;
import net.sf.ohla.rti.federate.Callback;
import net.sf.ohla.rti.federate.Federate;
import net.sf.ohla.rti.messages.AbstractMessage;
import net.sf.ohla.rti.messages.FederateMessage;
import net.sf.ohla.rti.messages.proto.FederateMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.exceptions.FederateInternalError;

public class TimeConstrainedEnabled
  extends
  AbstractMessage<FederateMessageProtos.TimeConstrainedEnabled, FederateMessageProtos.TimeConstrainedEnabled.Builder>
implements Callback, FederateMessage
{
  private Federate federate;
  private LogicalTime time;

  public TimeConstrainedEnabled(LogicalTime time)
  {
    super(FederateMessageProtos.TimeConstrainedEnabled.newBuilder());

    this.time = time;

    builder.setTime(LogicalTimes.convert(time));
  }

  public TimeConstrainedEnabled(CodedInputStream in)
    throws IOException
  {
    super(FederateMessageProtos.TimeConstrainedEnabled.newBuilder(), in);
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.TIME_CONSTRAINED_ENABLED;
  }

  @Override
  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    federate.timeConstrainedEnabled(time);
  }

  @Override
  public void execute(Federate federate)
  {
    this.federate = federate;

    time = LogicalTimes.convert(federate.getLogicalTimeFactory(), builder.getTime());

    federate.getCallbackManager().add(this, false);
  }
}
