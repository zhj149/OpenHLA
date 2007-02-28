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

package net.sf.ohla.rti.federate;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.ohla.rti.hla.rti1516.IEEE1516MessageRetractionHandle;

import hla.rti1516.LogicalTime;
import hla.rti1516.MessageCanNoLongerBeRetracted;
import hla.rti1516.MessageRetractionHandle;

public class FederateMessageRetractionManager
{
  protected Federate federate;

  protected AtomicLong messageRetractionCount = new AtomicLong();

  protected Lock messageRetractionsLock = new ReentrantLock(true);
  protected Map<MessageRetractionHandle, MessageRetraction> messageRetractions =
    new HashMap<MessageRetractionHandle, MessageRetraction>();
  protected Queue<MessageRetraction> messageRetractionsByExpiration =
    new PriorityQueue<MessageRetraction>();

  public FederateMessageRetractionManager(Federate federate)
  {
    this.federate = federate;
  }

  public MessageRetractionHandle add(LogicalTime time)
  {
    return add(time, null);
  }

  public MessageRetractionHandle add(LogicalTime time, Future<?> future)
  {
    return add(time, future, nextMessageRetractionHandle());
  }

  public MessageRetractionHandle add(
    LogicalTime time, Future<?> future,
    MessageRetractionHandle messageRetractionHandle)
  {
    MessageRetraction messageRetraction =
      new MessageRetraction(messageRetractionHandle, time, future);

    messageRetractionsLock.lock();
    try
    {
      messageRetractions.put(messageRetractionHandle, messageRetraction);
      messageRetractionsByExpiration.offer(messageRetraction);
    }
    finally
    {
      messageRetractionsLock.unlock();
    }

    return messageRetractionHandle;
  }

  public void retract(MessageRetractionHandle messageRetractionHandle,
                      LogicalTime time)
    throws MessageCanNoLongerBeRetracted
  {
    messageRetractionsLock.lock();
    try
    {
      MessageRetraction messageRetraction =
        messageRetractions.get(messageRetractionHandle);
      if (messageRetraction == null)
      {
        throw new MessageCanNoLongerBeRetracted(
          String.format("%s", messageRetractionHandle));
      }
      messageRetraction.retract(time);
    }
    finally
    {
      messageRetractionsLock.unlock();
    }
  }

  protected MessageRetractionHandle nextMessageRetractionHandle()
  {
    return new IEEE1516MessageRetractionHandle();
  }

  protected class MessageRetraction
    implements Comparable
  {
    protected MessageRetractionHandle messageRetractionHandle;
    protected LogicalTime expiration;
    protected Future<?> future;

    public MessageRetraction(MessageRetractionHandle messageRetractionHandle,
                             LogicalTime expiration, Future<?> future)
    {
      this.messageRetractionHandle = messageRetractionHandle;
      this.expiration = expiration;
      this.future = future;
    }

    public MessageRetractionHandle getMessageRetractionHandle()
    {
      return messageRetractionHandle;
    }

    public LogicalTime getExpiration()
    {
      return expiration;
    }

    public void retract(LogicalTime time)
      throws MessageCanNoLongerBeRetracted
    {
      if (expiration.compareTo(time) <= 0)
      {
        throw new MessageCanNoLongerBeRetracted(String.format(
          "message retraction expired: %s <= %s", expiration, time));
      }
      else if (future != null && !future.cancel(false))
      {
        throw new MessageCanNoLongerBeRetracted(String.format(
          "message already processed: %s", messageRetractionHandle));
      }
    }

    public int compareTo(Object rhs)
    {
      return compareTo((MessageRetraction) rhs);
    }

    public int compareTo(MessageRetraction rhs)
    {
      return expiration.compareTo(rhs.expiration);
    }
  }
}