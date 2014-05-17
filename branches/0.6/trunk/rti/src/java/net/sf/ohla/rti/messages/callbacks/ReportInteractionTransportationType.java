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

import java.io.IOException;

import net.sf.ohla.rti.util.FederateHandles;
import net.sf.ohla.rti.util.InteractionClassHandles;
import net.sf.ohla.rti.util.TransportationTypeHandles;
import net.sf.ohla.rti.federate.Callback;
import net.sf.ohla.rti.messages.AbstractMessage;
import net.sf.ohla.rti.messages.proto.FederateMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.FederateInternalError;

public class ReportInteractionTransportationType
  extends
  AbstractMessage<FederateMessageProtos.ReportInteractionTransportationType, FederateMessageProtos.ReportInteractionTransportationType.Builder>
  implements Callback
{
  public ReportInteractionTransportationType(
    InteractionClassHandle interactionClassHandle, FederateHandle federateHandle,
    TransportationTypeHandle transportationTypeHandle)
  {
    super(FederateMessageProtos.ReportInteractionTransportationType.newBuilder());

    builder.setInteractionClassHandle(InteractionClassHandles.convert(interactionClassHandle));
    builder.setFederateHandle(FederateHandles.convert(federateHandle));
    builder.setTransportationTypeHandle(TransportationTypeHandles.convert(transportationTypeHandle));
  }

  public ReportInteractionTransportationType(CodedInputStream in)
    throws IOException
  {
    super(FederateMessageProtos.ReportInteractionTransportationType.newBuilder(), in);
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.REPORT_INTERACTION_TRANSPORTATION_TYPE;
  }

  @Override
  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    federateAmbassador.reportInteractionTransportationType(
      FederateHandles.convert(builder.getFederateHandle()),
      InteractionClassHandles.convert(builder.getInteractionClassHandle()),
      TransportationTypeHandles.convert(builder.getTransportationTypeHandle()));
  }
}
