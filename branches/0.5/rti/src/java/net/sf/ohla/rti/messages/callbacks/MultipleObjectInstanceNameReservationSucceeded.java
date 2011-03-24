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

package net.sf.ohla.rti.messages.callbacks;

import java.util.Set;

import net.sf.ohla.rti.federate.Callback;
import net.sf.ohla.rti.federate.Federate;
import net.sf.ohla.rti.messages.FederateMessage;
import net.sf.ohla.rti.messages.MessageType;
import net.sf.ohla.rti.messages.StringsMessage;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.exceptions.FederateInternalError;

public class MultipleObjectInstanceNameReservationSucceeded
  extends StringsMessage
  implements Callback, FederateMessage
{
  private Federate federate;

  public MultipleObjectInstanceNameReservationSucceeded(Set<String> objectInstanceNames)
  {
    super(MessageType.MULTIPLE_OBJECT_INSTANCE_NAME_RESERVATION_SUCCEEDED, objectInstanceNames);

    encodingFinished();
  }

  public MultipleObjectInstanceNameReservationSucceeded(ChannelBuffer buffer)
  {
    super(buffer);
  }

  public Set<String> getObjectInstanceNames()
  {
    return strings;
  }

  public MessageType getType()
  {
    return MessageType.MULTIPLE_OBJECT_INSTANCE_NAME_RESERVATION_SUCCEEDED;
  }

  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    federate.multipleObjectInstanceNameReservationSucceeded(strings);
  }

  public void execute(Federate federate)
  {
    this.federate = federate;

    federate.callbackReceived(this);
  }
}
