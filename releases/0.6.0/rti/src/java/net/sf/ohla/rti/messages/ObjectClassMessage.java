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

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eObjectClassHandle;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.ObjectClassHandle;

public abstract class ObjectClassMessage
  extends AbstractMessage
{
  protected final ObjectClassHandle objectClassHandle;

  protected ObjectClassMessage(MessageType messageType, ObjectClassHandle objectClassHandle)
  {
    super(messageType);

    this.objectClassHandle = objectClassHandle;

    IEEE1516eObjectClassHandle.encode(buffer, objectClassHandle);
  }

  protected ObjectClassMessage(
    MessageType messageType, int capacity, boolean dynamic, ObjectClassHandle objectClassHandle)
  {
    super(messageType, capacity, dynamic);

    this.objectClassHandle = objectClassHandle;

    IEEE1516eObjectClassHandle.encode(buffer, objectClassHandle);
  }

  protected ObjectClassMessage(MessageType messageType, ChannelBuffer buffer, ObjectClassHandle objectClassHandle)
  {
    super(messageType, buffer);

    this.objectClassHandle = objectClassHandle;

    IEEE1516eObjectClassHandle.encode(buffer, objectClassHandle);
  }

  protected ObjectClassMessage(ChannelBuffer buffer)
  {
    super(buffer);

    objectClassHandle = IEEE1516eObjectClassHandle.decode(buffer);
  }

  public ObjectClassHandle getObjectClassHandle()
  {
    return objectClassHandle;
  }
}
