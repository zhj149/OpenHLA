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

import net.sf.ohla.rti.federate.Callback;
import net.sf.ohla.rti.messages.AbstractMessage;
import net.sf.ohla.rti.messages.proto.FederateMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.exceptions.FederateInternalError;

public class RequestFederationRestoreFailed
  extends
  AbstractMessage<FederateMessageProtos.RequestFederationRestoreFailed, FederateMessageProtos.RequestFederationRestoreFailed.Builder>
  implements Callback
{
  public RequestFederationRestoreFailed(String label)
  {
    super(FederateMessageProtos.RequestFederationRestoreFailed.newBuilder());

    builder.setLabel(label);
  }

  public RequestFederationRestoreFailed(CodedInputStream in)
    throws IOException
  {
    super(FederateMessageProtos.RequestFederationRestoreFailed.newBuilder(), in);
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.REQUEST_FEDERATION_RESTORE_FAILED;
  }

  @Override
  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    federateAmbassador.requestFederationRestoreFailed(builder.getLabel());
  }
}
