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

import net.sf.ohla.rti.util.FederateHandles;
import net.sf.ohla.rti.federate.Callback;
import net.sf.ohla.rti.messages.AbstractMessage;
import net.sf.ohla.rti.messages.proto.FederateMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.FederateRestoreStatus;
import hla.rti1516e.RestoreStatus;
import hla.rti1516e.exceptions.FederateInternalError;

public class FederationRestoreStatusResponse
  extends AbstractMessage<FederateMessageProtos.FederationRestoreStatusResponse, FederateMessageProtos.FederationRestoreStatusResponse.Builder>
  implements Callback
{
  public FederationRestoreStatusResponse(FederateRestoreStatus[] federationRestoreStatus)
  {
    super(FederateMessageProtos.FederationRestoreStatusResponse.newBuilder());

    for (FederateRestoreStatus federateRestoreStatus : federationRestoreStatus)
    {
      FederateMessageProtos.FederateRestoreStatus.Builder federateRestoreStatusProto =
        FederateMessageProtos.FederateRestoreStatus.newBuilder();

      federateRestoreStatusProto.setPreRestoreFederateHandle(
          FederateHandles.convert(federateRestoreStatus.preRestoreHandle));

      if (federateRestoreStatus.postRestoreHandle != null)
      {
        federateRestoreStatusProto.setPostRestoreFederateHandle(
          FederateHandles.convert(federateRestoreStatus.postRestoreHandle));
      }

      federateRestoreStatusProto.setRestoreStatus(
          FederateMessageProtos.RestoreStatus.values()[federateRestoreStatus.status.ordinal()]);

      builder.addFederationRestoreStatus(federateRestoreStatusProto);
    }
  }

  public FederationRestoreStatusResponse(CodedInputStream in)
    throws IOException
  {
    super(FederateMessageProtos.FederationRestoreStatusResponse.newBuilder(), in);
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.FEDERATION_RESTORE_STATUS_RESPONSE;
  }

  @Override
  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    FederateRestoreStatus[] federationRestoreStatus =
      new FederateRestoreStatus[builder.getFederationRestoreStatusCount()];
    int i = 0;
    for (FederateMessageProtos.FederateRestoreStatus federateRestoreStatus : builder.getFederationRestoreStatusList())
    {
      federationRestoreStatus[i++] = new FederateRestoreStatus(
        FederateHandles.convert(federateRestoreStatus.getPreRestoreFederateHandle()),
        FederateHandles.convert(federateRestoreStatus.getPostRestoreFederateHandle()),
        RestoreStatus.values()[federateRestoreStatus.getRestoreStatus().ordinal()]);
    }
    federateAmbassador.federationRestoreStatusResponse(federationRestoreStatus);
  }
}
