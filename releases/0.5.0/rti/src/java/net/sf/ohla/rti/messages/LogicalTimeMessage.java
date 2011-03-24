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

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;

public abstract class LogicalTimeMessage
  extends AbstractMessage
{
  protected final LogicalTime time;

  protected LogicalTimeMessage(MessageType messageType, LogicalTime time)
  {
    super(messageType);

    this.time = time;

    Protocol.encodeTime(buffer, time);

    encodingFinished();
  }

  protected LogicalTimeMessage(MessageType messageType, int capacity, boolean dynamic, LogicalTime time)
  {
    super(messageType, capacity, dynamic);

    this.time = time;

    Protocol.encodeTime(buffer, time);

    encodingFinished();
  }

  protected LogicalTimeMessage(MessageType messageType, ChannelBuffer buffer, LogicalTime time)
  {
    super(messageType, buffer);

    this.time = time;

    Protocol.encodeTime(buffer, time);

    encodingFinished();
  }

  protected LogicalTimeMessage(ChannelBuffer buffer, LogicalTimeFactory factory)
  {
    super(buffer);

    time = Protocol.decodeTime(buffer, factory);
  }

  public LogicalTime getTime()
  {
    return time;
  }
}
