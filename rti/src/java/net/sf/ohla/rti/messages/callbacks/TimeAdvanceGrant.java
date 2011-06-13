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

import net.sf.ohla.rti.federate.Callback;
import net.sf.ohla.rti.federate.Federate;
import net.sf.ohla.rti.messages.FederateMessage;
import net.sf.ohla.rti.messages.LogicalTimeMessage;
import net.sf.ohla.rti.messages.MessageType;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.exceptions.FederateInternalError;

public class TimeAdvanceGrant
  extends LogicalTimeMessage
  implements Callback, FederateMessage
{
  private Federate federate;

  public TimeAdvanceGrant(LogicalTime time)
  {
    super(MessageType.TIME_ADVANCE_GRANT, time);

    encodingFinished();
  }

  public TimeAdvanceGrant(ChannelBuffer buffer, LogicalTimeFactory logicalTimeFactory)
  {
    super(buffer, logicalTimeFactory);
  }

  public MessageType getType()
  {
    return MessageType.TIME_ADVANCE_GRANT;
  }

  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    federate.timeAdvanceGrant(time);
  }

  public void execute(Federate federate)
  {
    this.federate = federate;

    federate.callbackReceived(this);
  }
}
