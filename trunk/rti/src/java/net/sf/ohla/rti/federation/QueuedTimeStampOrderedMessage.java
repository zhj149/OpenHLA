/*
 * Copyright (c) 2005-2011, Michael Newcomb
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

package net.sf.ohla.rti.federation;

import net.sf.ohla.rti.messages.TimeStampOrderedMessage;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.Channels;

import hla.rti1516e.LogicalTime;

public class QueuedTimeStampOrderedMessage
  implements Comparable<QueuedTimeStampOrderedMessage>
{
  private final TimeStampOrderedMessage timeStampOrderedMessage;

  private boolean cancelled;

  public QueuedTimeStampOrderedMessage(TimeStampOrderedMessage timeStampOrderedMessage)
  {
    this.timeStampOrderedMessage = timeStampOrderedMessage;
  }

  public TimeStampOrderedMessage getTimeStampOrderedMessage()
  {
    return timeStampOrderedMessage;
  }

  public LogicalTime getTime()
  {
    return timeStampOrderedMessage.getTime();
  }

  public synchronized boolean cancel()
  {
    return !cancelled && (cancelled = true);
  }

  public synchronized ChannelFuture write(Channel channel)
  {
    return cancelled ? Channels.succeededFuture(channel) : channel.write(timeStampOrderedMessage);
  }

  public synchronized ChannelFuture writeReceiveOrder(Channel channel)
  {
    return cancelled ? Channels.succeededFuture(channel) : channel.write(timeStampOrderedMessage.makeReceiveOrdered());
  }

  public int compareTo(QueuedTimeStampOrderedMessage rhs)
  {
    return timeStampOrderedMessage.compareTo(rhs.timeStampOrderedMessage);
  }
}
