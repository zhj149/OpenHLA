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

package net.sf.ohla.rti.messages;

import java.util.Set;

import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleSet;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.ObjectInstanceHandle;

public class UnpublishObjectClassAttributes
  extends ObjectInstancesMessage
  implements FederationExecutionMessage
{
  private final AttributeHandleSet attributeHandles;

  public UnpublishObjectClassAttributes(
    Set<ObjectInstanceHandle> objectInstanceHandles, AttributeHandleSet attributeHandles)
  {
    super(MessageType.UNPUBLISH_OBJECT_CLASS_ATTRIBUTES, objectInstanceHandles);

    this.attributeHandles = attributeHandles;

    IEEE1516eAttributeHandleSet.encode(buffer, attributeHandles);

    encodingFinished();
  }

  public UnpublishObjectClassAttributes(ChannelBuffer buffer)
  {
    super(buffer);

    attributeHandles = IEEE1516eAttributeHandleSet.decode(buffer);
  }

  public AttributeHandleSet getAttributeHandles()
  {
    return attributeHandles;
  }

  public MessageType getType()
  {
    return MessageType.UNPUBLISH_OBJECT_CLASS_ATTRIBUTES;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.unpublishObjectClassAttributes(federateProxy, this);
  }
}