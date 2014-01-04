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

import net.sf.ohla.rti.Protocol;
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandleSet;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.FederateHandleSet;

public class RegisterFederationSynchronizationPoint
  extends StringMessage
  implements FederationExecutionMessage
{
  private final byte[] tag;
  private final FederateHandleSet federateHandles;

  public RegisterFederationSynchronizationPoint(String label, byte[] tag, FederateHandleSet federateHandles)
  {
    super(MessageType.REGISTER_FEDERATION_SYNCHRONIZATION_POINT, label);

    this.tag = tag;
    this.federateHandles = federateHandles;

    Protocol.encodeBytes(buffer, tag);
    IEEE1516eFederateHandleSet.encode(buffer, federateHandles);

    encodingFinished();
  }

  public RegisterFederationSynchronizationPoint(ChannelBuffer buffer)
  {
    super(buffer);

    tag = Protocol.decodeBytes(buffer);
    federateHandles = IEEE1516eFederateHandleSet.decode(buffer);
  }

  public String getLabel()
  {
    return s;
  }

  public byte[] getTag()
  {
    return tag;
  }

  public FederateHandleSet getFederateHandles()
  {
    return federateHandles;
  }

  public MessageType getType()
  {
    return MessageType.REGISTER_FEDERATION_SYNCHRONIZATION_POINT;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.registerFederationSynchronizationPoint(federateProxy, this);
  }
}
