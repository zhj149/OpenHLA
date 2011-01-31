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
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandle;
import net.sf.ohla.rti.messages.AbstractMessage;
import net.sf.ohla.rti.messages.MessageType;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateHandleSaveStatusPair;
import hla.rti1516e.SaveStatus;
import hla.rti1516e.exceptions.FederateInternalError;

public class FederationSaveStatusResponse
  extends AbstractMessage
  implements Callback
{
  private final FederateHandleSaveStatusPair[] response;

  public FederationSaveStatusResponse(FederateHandleSaveStatusPair[] response)
  {
    super(MessageType.FEDERATION_SAVE_STATUS_RESPONSE);

    this.response = response;

    Protocol.encodeVarInt(buffer, response.length);
    for (FederateHandleSaveStatusPair status : response)
    {
      IEEE1516eFederateHandle.encode(buffer, status.handle);
      Protocol.encodeEnum(buffer, status.status);
    }

    encodingFinished();
  }

  public FederationSaveStatusResponse(ChannelBuffer buffer)
  {
    super(buffer);

    int length = Protocol.decodeVarInt(buffer);
    response = new FederateHandleSaveStatusPair[length];
    for (int i = 0; i < length; i++)
    {
      FederateHandle federateHandle = IEEE1516eFederateHandle.decode(buffer);
      SaveStatus saveStatus = Protocol.decodeEnum(buffer, SaveStatus.values());

      response[i] = new FederateHandleSaveStatusPair(federateHandle, saveStatus);
    }
  }

  public MessageType getType()
  {
    return MessageType.FEDERATION_SAVE_STATUS_RESPONSE;
  }

  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    federateAmbassador.federationSaveStatusResponse(response);
  }
}
