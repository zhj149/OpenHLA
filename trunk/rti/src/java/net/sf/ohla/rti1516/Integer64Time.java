/*
 * Copyright (c) 2006, Michael Newcomb
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

package net.sf.ohla.rti1516;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import hla.rti1516.CouldNotDecode;
import hla.rti1516.IllegalTimeArithmetic;
import hla.rti1516.LogicalTime;
import hla.rti1516.LogicalTimeInterval;

public class Integer64Time
  implements LogicalTime
{
  private static final byte ENCODED_LENGTH = Long.SIZE / 8;

  public static final Integer64Time INITIAL = new Integer64Time(0);
  public static final Integer64Time FINAL = new Integer64Time(Long.MAX_VALUE);

  public final long time;

  public Integer64Time(long time)
  {
    this.time = time;
  }

  public Integer64Time(byte[] buffer, int offset)
    throws CouldNotDecode
  {
    try
    {
      time = ByteBuffer.wrap(buffer, offset, ENCODED_LENGTH).getLong();
    }
    catch (BufferUnderflowException bue)
    {
      throw new CouldNotDecode(bue);
    }
  }

  public boolean isInitial()
  {
    return time == INITIAL.time;
  }

  public boolean isFinal()
  {
    return time == FINAL.time;
  }

  public LogicalTime add(LogicalTimeInterval lti)
    throws IllegalTimeArithmetic
  {
    return add((Integer64TimeInterval) lti);
  }

  public Integer64Time add(Integer64TimeInterval i64ti)
    throws IllegalTimeArithmetic
  {
    return new Integer64Time(time + i64ti.interval);
  }

  public LogicalTime subtract(LogicalTimeInterval lti)
    throws IllegalTimeArithmetic
  {
    return subtract((Integer64TimeInterval) lti);
  }

  public Integer64Time subtract(Integer64TimeInterval i64ti)
    throws IllegalTimeArithmetic
  {
    return new Integer64Time(time - i64ti.interval);
  }

  public LogicalTimeInterval distance(LogicalTime lt)
  {
    return distance((Integer64Time) lt);
  }

  public Integer64TimeInterval distance(Integer64Time i64t)
  {
    return new Integer64TimeInterval(Math.abs(time - i64t.time));
  }

  public int encodedLength()
  {
    return ENCODED_LENGTH;
  }

  public void encode(byte[] buffer, int offset)
  {
    ByteBuffer.wrap(buffer, offset, ENCODED_LENGTH).putLong(time);
  }

  public int compareTo(Object rhs)
  {
    return compareTo((Integer64Time) rhs);
  }

  public int compareTo(Integer64Time rhs)
  {
    long diff = time - rhs.time;
    return diff > 0l ? 1 : diff < 0l ? -1 : 0;
  }

  @Override
  public boolean equals(Object rhs)
  {
    return rhs instanceof Integer64Time && time == ((Integer64Time) rhs).time;
  }

  @Override
  public int hashCode()
  {
    return (int) time;
  }

  @Override
  public String toString()
  {
    return Long.toString(time);
  }
}
