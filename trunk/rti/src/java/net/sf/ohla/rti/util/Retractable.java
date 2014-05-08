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

import hla.rti1516e.LogicalTime;
import hla.rti1516e.MessageRetractionHandle;

public class Retractable
  implements Comparable<Retractable>
{
  protected final MessageRetractionHandle messageRetractionHandle;
  protected final LogicalTime time;

  protected boolean retracted;
  protected boolean expired;

  public Retractable(MessageRetractionHandle messageRetractionHandle, LogicalTime time)
  {
    this.messageRetractionHandle = messageRetractionHandle;
    this.time = time;
  }

  public MessageRetractionHandle getMessageRetractionHandle()
  {
    return messageRetractionHandle;
  }

  public LogicalTime getTime()
  {
    return time;
  }

  public boolean isRetracted()
  {
    return retracted;
  }

  public boolean isNotRetracted()
  {
    return !retracted;
  }

  public void retract()
  {
    assert !retracted;
    assert !expired;

    retracted = true;
  }

  public boolean isExpired()
  {
    return expired;
  }

  public boolean isNotExpired()
  {
    return !expired;
  }

  public void expire()
  {
    assert !retracted;
    assert !expired;

    expired = true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public int compareTo(Retractable rhs)
  {
    return time.compareTo(rhs.time);
  }
}
