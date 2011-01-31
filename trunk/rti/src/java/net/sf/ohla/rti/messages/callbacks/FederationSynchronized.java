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
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandleSet;
import net.sf.ohla.rti.messages.FederateMessage;
import net.sf.ohla.rti.messages.MessageType;
import net.sf.ohla.rti.messages.StringMessage;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.FederateHandleSet;
import hla.rti1516e.exceptions.FederateInternalError;

public class FederationSynchronized
  extends StringMessage
  implements Callback, FederateMessage
{
  private final FederateHandleSet failedToSynchronize;

  private Federate federate;

  public FederationSynchronized(String label, FederateHandleSet failedToSynchronize)
  {
    super(MessageType.FEDERATION_SYNCHRONIZED, label);

    this.failedToSynchronize = failedToSynchronize;

    IEEE1516eFederateHandleSet.encode(buffer, failedToSynchronize);

    encodingFinished();
  }

  public FederationSynchronized(ChannelBuffer buffer)
  {
    super(buffer);

    failedToSynchronize = IEEE1516eFederateHandleSet.decode(buffer);
  }

  public MessageType getType()
  {
    return MessageType.FEDERATION_SYNCHRONIZED;
  }

  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    federate.federationSynchronized(s, failedToSynchronize);
  }

  public void execute(Federate federate)
  {
    this.federate = federate;

    federate.callbackReceived(this);
  }
}
