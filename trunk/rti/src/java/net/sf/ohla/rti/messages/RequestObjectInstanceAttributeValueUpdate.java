/*
 * Copyright (c) 2005-2010, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.ObjectInstanceHandle;

public class RequestObjectInstanceAttributeValueUpdate
  extends ObjectInstanceAttributesMessage
  implements FederationExecutionMessage
{
  private final byte[] tag;

  public RequestObjectInstanceAttributeValueUpdate(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
  {
    super(MessageType.REQUEST_OBJECT_INSTANCE_ATTRIBUTE_VALUE_UPDATE, objectInstanceHandle, attributeHandles);

    this.tag = tag;

    Protocol.encodeBytes(buffer, tag);

    encodingFinished();
  }

  public RequestObjectInstanceAttributeValueUpdate(ChannelBuffer buffer)
  {
    super(buffer);

    tag = Protocol.decodeBytes(buffer);
  }

  public byte[] getTag()
  {
    return tag;
  }

  public MessageType getType()
  {
    return MessageType.REQUEST_OBJECT_INSTANCE_ATTRIBUTE_VALUE_UPDATE;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.requestObjectInstanceAttributeValueUpdate(federateProxy, this);
  }
}