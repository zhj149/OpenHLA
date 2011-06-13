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

package net.sf.ohla.rti;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import hla.rti1516e.LogicalTime;
import hla.rti1516e.MessageRetractionHandle;

public class MessageRetractionManager
{
  protected final Lock messageRetractionsLock = new ReentrantLock(true);
  protected final Map<MessageRetractionHandle, MessageRetraction> messageRetractions =
    new HashMap<MessageRetractionHandle, MessageRetraction>();
  protected final Queue<MessageRetraction> messageRetractionsByExpiration = new PriorityQueue<MessageRetraction>();

  @SuppressWarnings("unchecked")
  public void clear(LogicalTime time)
  {
    messageRetractionsLock.lock();
    try
    {
      for (MessageRetraction messageRetraction = messageRetractionsByExpiration.peek();
           messageRetraction != null && messageRetraction.getExpiration().compareTo(time) < 0;
           messageRetraction = messageRetractionsByExpiration.peek())
      {
        messageRetractions.remove(messageRetraction.getMessageRetractionHandle());

        messageRetractionsByExpiration.poll();
      }
    }
    finally
    {
      messageRetractionsLock.unlock();
    }
  }

  protected void add(MessageRetraction messageRetraction)
  {
    messageRetractionsLock.lock();
    try
    {
      messageRetractions.put(messageRetraction.getMessageRetractionHandle(), messageRetraction);
      messageRetractionsByExpiration.offer(messageRetraction);
    }
    finally
    {
      messageRetractionsLock.unlock();
    }
  }

  protected class MessageRetraction
    implements Comparable<MessageRetraction>
  {
    protected final MessageRetractionHandle messageRetractionHandle;

    protected final LogicalTime expiration;

    public MessageRetraction(MessageRetractionHandle messageRetractionHandle, LogicalTime expiration)
    {
      this.messageRetractionHandle = messageRetractionHandle;
      this.expiration = expiration;
    }

    public MessageRetractionHandle getMessageRetractionHandle()
    {
      return messageRetractionHandle;
    }

    public LogicalTime getExpiration()
    {
      return expiration;
    }

    public boolean retract()
    {
      return true;
    }

    @SuppressWarnings("unchecked")
    public int compareTo(MessageRetraction rhs)
    {
      return expiration.compareTo(rhs.expiration);
    }
  }
}
