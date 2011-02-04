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

package net.sf.ohla.rti.messages;

import net.sf.ohla.rti.federate.FederateSave;
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;

import org.jboss.netty.buffer.ChannelBuffer;

public class FederateSaveComplete
  extends AbstractMessage
  implements FederationExecutionMessage
{
  private final FederateSave federateSave;

  public FederateSaveComplete(FederateSave federateSave)
  {
    super(MessageType.FEDERATE_SAVE_COMPLETE);

    this.federateSave = federateSave;

    federateSave.encode(buffer);

    encodingFinished();
  }

  public FederateSaveComplete(ChannelBuffer buffer)
  {
    super(buffer);

    federateSave = new FederateSave(buffer);
  }

  public FederateSave getFederateSave()
  {
    return federateSave;
  }

  public MessageType getType()
  {
    return MessageType.FEDERATE_SAVE_COMPLETE;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.federateSaveComplete(federateProxy, this);
  }
}