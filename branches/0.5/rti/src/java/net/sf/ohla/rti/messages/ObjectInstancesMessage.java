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

import java.util.HashSet;
import java.util.Set;

import net.sf.ohla.rti.Protocol;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eObjectInstanceHandle;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.ObjectInstanceHandle;

public abstract class ObjectInstancesMessage
  extends AbstractMessage
{
  private final Set<ObjectInstanceHandle> objectInstanceHandles;

  protected ObjectInstancesMessage(MessageType messageType, Set<ObjectInstanceHandle> objectInstanceHandles)
  {
    super(messageType);

    this.objectInstanceHandles = objectInstanceHandles;

    Protocol.encodeVarInt(buffer, objectInstanceHandles.size());
    for (ObjectInstanceHandle objectInstanceHandle : objectInstanceHandles)
    {
      IEEE1516eObjectInstanceHandle.encode(buffer, objectInstanceHandle);
    }
  }

  protected ObjectInstancesMessage(
    MessageType messageType, int capacity, boolean dynamic, Set<ObjectInstanceHandle> objectInstanceHandles)
  {
    super(messageType, capacity, dynamic);

    this.objectInstanceHandles = objectInstanceHandles;

    Protocol.encodeVarInt(buffer, objectInstanceHandles.size());
    for (ObjectInstanceHandle objectInstanceHandle : objectInstanceHandles)
    {
      IEEE1516eObjectInstanceHandle.encode(buffer, objectInstanceHandle);
    }
  }

  protected ObjectInstancesMessage(
    MessageType messageType, ChannelBuffer buffer, Set<ObjectInstanceHandle> objectInstanceHandles)
  {
    super(messageType, buffer);

    this.objectInstanceHandles = objectInstanceHandles;

    Protocol.encodeVarInt(buffer, objectInstanceHandles.size());
    for (ObjectInstanceHandle objectInstanceHandle : objectInstanceHandles)
    {
      IEEE1516eObjectInstanceHandle.encode(buffer, objectInstanceHandle);
    }
  }

  protected ObjectInstancesMessage(ChannelBuffer buffer)
  {
    super(buffer);

    objectInstanceHandles = new HashSet<ObjectInstanceHandle>();
    for (int count = Protocol.decodeVarInt(buffer); count > 0; count--)
    {
      objectInstanceHandles.add(IEEE1516eObjectInstanceHandle.decode(buffer));
    }
  }

  public Set<ObjectInstanceHandle> getObjectInstanceHandles()
  {
    return objectInstanceHandles;
  }
}
