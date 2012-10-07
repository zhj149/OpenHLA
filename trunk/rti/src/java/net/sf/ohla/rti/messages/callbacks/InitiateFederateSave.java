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

import net.sf.ohla.rti.Protocol;
import net.sf.ohla.rti.federate.Callback;
import net.sf.ohla.rti.federate.Federate;
import net.sf.ohla.rti.messages.FederateMessage;
import net.sf.ohla.rti.messages.MessageType;
import net.sf.ohla.rti.messages.StringMessage;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.exceptions.FederateInternalError;

public class InitiateFederateSave
  extends StringMessage
  implements Callback, FederateMessage
{
  private final LogicalTime time;

  private Federate federate;

  public InitiateFederateSave(String label)
  {
    super(MessageType.INITIATE_FEDERATE_SAVE, label);

    time = null;

    Protocol.encodeNullTime(buffer);

    encodingFinished();
  }

  public InitiateFederateSave(String label, LogicalTime time)
  {
    super(MessageType.INITIATE_FEDERATE_SAVE, label);

    this.time = time;

    Protocol.encodeTime(buffer, time);

    encodingFinished();
  }

  public InitiateFederateSave(ChannelBuffer buffer, LogicalTimeFactory logicalTimeFactory)
  {
    super(buffer);

    time = Protocol.decodeTime(buffer, logicalTimeFactory);
  }

  public String getLabel()
  {
    return s;
  }

  public LogicalTime getTime()
  {
    return time;
  }

  public MessageType getType()
  {
    return MessageType.INITIATE_FEDERATE_SAVE;
  }

  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    federate.fireInitiateFederateSave(s, time);
  }

  public void execute(Federate federate)
  {
    this.federate = federate;

    federate.getCallbackManager().add(this, false);
  }
}
