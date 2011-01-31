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

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eInteractionClassHandle;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.InteractionClassHandle;

public abstract class InteractionClassMessage
  extends AbstractMessage
{
  protected final InteractionClassHandle interactionClassHandle;

  protected InteractionClassMessage(MessageType messageType, InteractionClassHandle interactionClassHandle)
  {
    super(messageType);

    this.interactionClassHandle = interactionClassHandle;

    IEEE1516eInteractionClassHandle.encode(buffer, interactionClassHandle);

    encodingFinished();
  }

  protected InteractionClassMessage(
    MessageType messageType, int capacity, boolean dynamic, InteractionClassHandle interactionClassHandle)
  {
    super(messageType, capacity, dynamic);

    this.interactionClassHandle = interactionClassHandle;

    IEEE1516eInteractionClassHandle.encode(buffer, interactionClassHandle);

    encodingFinished();
  }

  protected InteractionClassMessage(
    MessageType messageType, ChannelBuffer buffer, InteractionClassHandle interactionClassHandle)
  {
    super(messageType, buffer);

    this.interactionClassHandle = interactionClassHandle;

    IEEE1516eInteractionClassHandle.encode(buffer, interactionClassHandle);

    encodingFinished();
  }

  protected InteractionClassMessage(ChannelBuffer buffer)
  {
    super(buffer);

    interactionClassHandle = IEEE1516eInteractionClassHandle.decode(buffer);
  }

  public InteractionClassHandle getInteractionClassHandle()
  {
    return interactionClassHandle;
  }
}
