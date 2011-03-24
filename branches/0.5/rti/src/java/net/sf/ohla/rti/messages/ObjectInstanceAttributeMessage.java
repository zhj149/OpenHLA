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

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandle;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.ObjectInstanceHandle;

public abstract class ObjectInstanceAttributeMessage
  extends ObjectInstanceMessage
{
  protected final AttributeHandle attributeHandle;

  protected ObjectInstanceAttributeMessage(
    MessageType messageType, ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
  {
    super(messageType, objectInstanceHandle);

    this.attributeHandle = attributeHandle;

    IEEE1516eAttributeHandle.encode(buffer, attributeHandle);
  }

  protected ObjectInstanceAttributeMessage(
    MessageType messageType, int capacity, boolean dynamic, ObjectInstanceHandle objectInstanceHandle,
    AttributeHandle attributeHandle)
  {
    super(messageType, capacity, dynamic, objectInstanceHandle);

    this.attributeHandle = attributeHandle;

    IEEE1516eAttributeHandle.encode(buffer, attributeHandle);

    encodingFinished();
  }

  protected ObjectInstanceAttributeMessage(
    MessageType messageType, ChannelBuffer buffer, ObjectInstanceHandle objectInstanceHandle,
    AttributeHandle attributeHandle)
  {
    super(messageType, buffer, objectInstanceHandle);

    this.attributeHandle = attributeHandle;

    IEEE1516eAttributeHandle.encode(buffer, attributeHandle);

    encodingFinished();
  }

  protected ObjectInstanceAttributeMessage(ChannelBuffer buffer)
  {
    super(buffer);

    attributeHandle = IEEE1516eAttributeHandle.decode(buffer);
  }

  public AttributeHandle getAttributeHandle()
  {
    return attributeHandle;
  }
}
