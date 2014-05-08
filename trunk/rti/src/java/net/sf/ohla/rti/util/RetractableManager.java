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

package net.sf.ohla.rti.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;

import hla.rti1516e.LogicalTime;
import hla.rti1516e.MessageRetractionHandle;

public class RetractableManager<R extends Retractable>
{
  protected final PriorityQueue<R> retractables = new PriorityQueue<>();
  protected final Map<MessageRetractionHandle, R> retractablesByMessageRetractionHandle = new HashMap<>();

  public synchronized boolean contains(MessageRetractionHandle messageRetractionHandle)
  {
    return retractablesByMessageRetractionHandle.containsKey(messageRetractionHandle) &&
           retractablesByMessageRetractionHandle.get(messageRetractionHandle).isNotRetracted();
  }

  public synchronized boolean add(R retractable)
  {
    boolean added;
    if (added = !retractablesByMessageRetractionHandle.containsKey(retractable.getMessageRetractionHandle()))
    {
      retractables.add(retractable);
      retractablesByMessageRetractionHandle.put(retractable.getMessageRetractionHandle(), retractable);
    }
    return added;
  }

  public synchronized boolean retract(MessageRetractionHandle messageRetractionHandle)
  {
    boolean retracted;

    R retractable = retractablesByMessageRetractionHandle.remove(messageRetractionHandle);
    if (retracted = retractable != null)
    {
      retractable.retract();
    }

    return retracted;
  }

  @SuppressWarnings("unchecked")
  public synchronized void expire(LogicalTime time)
  {
    for (Retractable retractable = retractables.peek();
         retractable != null && retractable.getTime().compareTo(time) <= 0; retractable = retractables.peek())
    {
      if (retractable.isNotRetracted())
      {
        retractable.expire();
      }

      retractables.poll();
      retractablesByMessageRetractionHandle.remove(retractable.getMessageRetractionHandle());
    }
  }

  public synchronized void expireAll()
  {
    for (Retractable retractable = retractables.poll(); retractable != null; retractable = retractables.poll())
    {
      if (retractable.isNotRetracted())
      {
        retractable.expire();
      }
    }
    retractablesByMessageRetractionHandle.clear();
  }

  public synchronized void clear()
  {
    retractables.clear();
    retractablesByMessageRetractionHandle.clear();
  }

  protected void clearRetracted()
  {
    // clear any retracted messages
    //
    for (Iterator<R> i = retractables.iterator(); i.hasNext(); )
    {
      R retractable = i.next();
      if (retractable.isRetracted())
      {
        i.remove();

        retractablesByMessageRetractionHandle.remove(retractable.getMessageRetractionHandle());
      }
    }
  }
}
