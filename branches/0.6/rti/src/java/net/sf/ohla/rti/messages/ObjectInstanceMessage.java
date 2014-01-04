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

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eObjectInstanceHandle;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.ObjectInstanceHandle;

public abstract class ObjectInstanceMessage
  extends AbstractMessage
{
  protected final ObjectInstanceHandle objectInstanceHandle;

  protected ObjectInstanceMessage(MessageType messageType, ObjectInstanceHandle objectInstanceHandle)
  {
    super(messageType);

    this.objectInstanceHandle = objectInstanceHandle;

    IEEE1516eObjectInstanceHandle.encode(buffer, objectInstanceHandle);
  }

  protected ObjectInstanceMessage(
    MessageType messageType, int capacity, boolean dynamic, ObjectInstanceHandle objectInstanceHandle)
  {
    super(messageType, capacity, dynamic);

    this.objectInstanceHandle = objectInstanceHandle;

    IEEE1516eObjectInstanceHandle.encode(buffer, objectInstanceHandle);
  }

  protected ObjectInstanceMessage(
    MessageType messageType, ChannelBuffer buffer, ObjectInstanceHandle objectInstanceHandle)
  {
    super(messageType, buffer);

    this.objectInstanceHandle = objectInstanceHandle;

    IEEE1516eObjectInstanceHandle.encode(buffer, objectInstanceHandle);
  }

  protected ObjectInstanceMessage(ChannelBuffer buffer)
  {
    super(buffer);

    objectInstanceHandle = IEEE1516eObjectInstanceHandle.decode(buffer);
  }

  public ObjectInstanceHandle getObjectInstanceHandle()
  {
    return objectInstanceHandle;
  }
}
