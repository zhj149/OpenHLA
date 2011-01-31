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
import net.sf.ohla.rti.messages.InteractionClassMessage;
import net.sf.ohla.rti.messages.MessageType;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.exceptions.FederateInternalError;

public class TurnInteractionsOn
  extends InteractionClassMessage
  implements Callback, FederateMessage
{
  public TurnInteractionsOn(InteractionClassHandle interactionClassHandle)
  {
    super(MessageType.TURN_INTERACTIONS_ON, interactionClassHandle);

    encodingFinished();
  }

  public TurnInteractionsOn(ChannelBuffer buffer)
  {
    super(buffer);
  }

  public MessageType getType()
  {
    return MessageType.TURN_INTERACTIONS_ON;
  }

  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    federateAmbassador.turnInteractionsOn(interactionClassHandle);
  }

  public void execute(Federate federate)
  {
    federate.callbackReceived(this);
  }
}
