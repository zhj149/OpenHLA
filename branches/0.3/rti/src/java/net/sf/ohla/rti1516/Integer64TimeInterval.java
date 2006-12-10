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
import hla.rti1516.LogicalTimeInterval;

public class Integer64TimeInterval
  implements LogicalTimeInterval
{
  private static final byte ENCODED_LENGTH = Long.SIZE / 8;

  public static final Integer64TimeInterval ZERO =
    new Integer64TimeInterval(0);
  public static final Integer64TimeInterval EPSILON =
    new Integer64TimeInterval(1);

  public final long interval;

  public Integer64TimeInterval(long interval)
  {
    this.interval = interval;
  }

  public Integer64TimeInterval(byte[] buffer, int offset)
    throws CouldNotDecode
  {
    try
    {
      interval = ByteBuffer.wrap(buffer, offset, ENCODED_LENGTH).getLong();
    }
    catch (BufferUnderflowException bue)
    {
      throw new CouldNotDecode(bue);
    }
  }

  public boolean isZero()
  {
    return interval == ZERO.interval;
  }

  public boolean isEpsilon()
  {
    return interval == EPSILON.interval;
  }

  public LogicalTimeInterval subtract(LogicalTimeInterval lti)
  {
    return subtract((Integer64TimeInterval) lti);
  }

  public Integer64TimeInterval subtract(Integer64TimeInterval i64ti)
  {
    return new Integer64TimeInterval(interval - i64ti.interval);
  }

  public int encodedLength()
  {
    return ENCODED_LENGTH;
  }

  public void encode(byte[] buffer, int offset)
  {
    ByteBuffer.wrap(buffer, offset, ENCODED_LENGTH).putLong(interval);
  }

  public int compareTo(Object rhs)
  {
    return compareTo((Integer64TimeInterval) rhs);
  }

  public int compareTo(Integer64TimeInterval rhs)
  {
    return (int) (interval - rhs.interval);
  }

  public boolean equals(Object rhs)
  {
    return rhs instanceof Integer64TimeInterval &&
           equals((Integer64TimeInterval) rhs);
  }

  public boolean equals(Integer64TimeInterval rhs)
  {
    return interval == rhs.interval;
  }

  public int hashCode()
  {
    return (int) interval;
  }

  public String toString()
  {
    return Long.toString(interval);
  }
}