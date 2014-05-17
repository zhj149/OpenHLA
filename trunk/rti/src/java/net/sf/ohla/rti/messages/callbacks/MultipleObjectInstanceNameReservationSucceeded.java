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

import java.util.Collection;
import java.util.HashSet;

import net.sf.ohla.rti.federate.Callback;
import net.sf.ohla.rti.federate.Federate;
import net.sf.ohla.rti.messages.AbstractMessage;
import net.sf.ohla.rti.messages.FederateMessage;
import net.sf.ohla.rti.messages.proto.FederateMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.exceptions.FederateInternalError;

public class MultipleObjectInstanceNameReservationSucceeded
  extends
  AbstractMessage<FederateMessageProtos.MultipleObjectInstanceNameReservationSucceeded, FederateMessageProtos.MultipleObjectInstanceNameReservationSucceeded.Builder>
  implements Callback, FederateMessage
{
  private Federate federate;

  public MultipleObjectInstanceNameReservationSucceeded(Collection<String> objectInstanceNames)
  {
    super(FederateMessageProtos.MultipleObjectInstanceNameReservationSucceeded.newBuilder());

    builder.addAllObjectInstanceNames(objectInstanceNames);
  }

  public MultipleObjectInstanceNameReservationSucceeded(CodedInputStream in)
    throws IOException
  {
    super(FederateMessageProtos.MultipleObjectInstanceNameReservationSucceeded.newBuilder(), in);
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.MULTIPLE_OBJECT_INSTANCE_NAME_RESERVATION_SUCCEEDED;
  }

  @Override
  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    federate.multipleObjectInstanceNameReservationSucceeded(new HashSet<>(builder.getObjectInstanceNamesList()));
  }

  @Override
  public void execute(Federate federate)
  {
    this.federate = federate;

    federate.getCallbackManager().add(this, false);
  }
}
