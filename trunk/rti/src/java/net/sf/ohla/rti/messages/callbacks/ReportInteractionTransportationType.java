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

import net.sf.ohla.rti.federate.Callback;
import net.sf.ohla.rti.federate.Federate;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eTransportationTypeHandle;
import net.sf.ohla.rti.messages.FederateMessage;
import net.sf.ohla.rti.messages.InteractionClassMessage;
import net.sf.ohla.rti.messages.MessageType;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.FederateInternalError;

public class ReportInteractionTransportationType
  extends InteractionClassMessage
  implements Callback, FederateMessage
{
  private final FederateHandle federateHandle;
  private final TransportationTypeHandle transportationTypeHandle;

  public ReportInteractionTransportationType(
    InteractionClassHandle interactionClassHandle, FederateHandle federateHandle,
    TransportationTypeHandle transportationTypeHandle)
  {
    super(MessageType.REPORT_INTERACTION_TRANSPORTATION_TYPE, interactionClassHandle);

    this.federateHandle = federateHandle;
    this.transportationTypeHandle = transportationTypeHandle;

    IEEE1516eFederateHandle.encode(buffer, federateHandle);
    IEEE1516eTransportationTypeHandle.encode(buffer, transportationTypeHandle);

    encodingFinished();
  }

  public ReportInteractionTransportationType(ChannelBuffer buffer)
  {
    super(buffer);

    federateHandle = IEEE1516eFederateHandle.decode(buffer);
    transportationTypeHandle = IEEE1516eTransportationTypeHandle.decode(buffer);
  }

  public MessageType getType()
  {
    return MessageType.REPORT_INTERACTION_TRANSPORTATION_TYPE;
  }

  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    federateAmbassador.reportInteractionTransportationType(
      federateHandle, interactionClassHandle, transportationTypeHandle);
  }

  public void execute(Federate federate)
  {
    federate.callbackReceived(this);
  }
}