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
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleSet;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.ObjectClassHandle;

public class RequestObjectClassAttributeValueUpdate
  extends ObjectClassMessage
  implements FederationExecutionMessage
{
  private final AttributeHandleSet attributeHandles;
  private final byte[] tag;

  public RequestObjectClassAttributeValueUpdate(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles, byte[] tag)
  {
    super(MessageType.REQUEST_OBJECT_CLASS_ATTRIBUTE_VALUE_UPDATE, objectClassHandle);

    this.attributeHandles = attributeHandles;
    this.tag = tag;

    IEEE1516eAttributeHandleSet.encode(buffer, attributeHandles);
    Protocol.encodeBytes(buffer, tag);

    encodingFinished();
  }

  public RequestObjectClassAttributeValueUpdate(ChannelBuffer buffer)
  {
    super(buffer);

    attributeHandles = IEEE1516eAttributeHandleSet.decode(buffer);
    tag = Protocol.decodeBytes(buffer);
  }

  public AttributeHandleSet getAttributeHandles()
  {
    return attributeHandles;
  }

  public byte[] getTag()
  {
    return tag;
  }

  public MessageType getType()
  {
    return MessageType.REQUEST_OBJECT_CLASS_ATTRIBUTE_VALUE_UPDATE;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.requestObjectClassAttributeValueUpdate(federateProxy, this);
  }
}
