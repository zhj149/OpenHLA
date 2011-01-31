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

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleSet;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.AttributeHandleSet;

public class AttributeOwnershipDivestitureIfWantedResponse
  extends AbstractResponse
{
  private final AttributeHandleSet attributeHandles;

  public AttributeOwnershipDivestitureIfWantedResponse(long id, AttributeHandleSet attributeHandles)
  {
    super(MessageType.ATTRIBUTE_OWNERSHIP_DIVESTITURE_IF_WANTED_RESPONSE, id);

    this.attributeHandles = attributeHandles;

    IEEE1516eAttributeHandleSet.encode(buffer, attributeHandles);

    encodingFinished();
  }

  public AttributeOwnershipDivestitureIfWantedResponse(ChannelBuffer buffer)
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
    return MessageType.ATTRIBUTE_OWNERSHIP_DIVESTITURE_IF_WANTED_RESPONSE;
  }
}
