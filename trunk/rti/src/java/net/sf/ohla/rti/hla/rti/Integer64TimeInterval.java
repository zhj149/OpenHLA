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

package net.sf.ohla.rti.hla.rti;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import hla.rti.CouldNotDecode;
import hla.rti.LogicalTimeInterval;

public class Integer64TimeInterval
  implements LogicalTimeInterval
{
  private static final byte ENCODED_LENGTH = Long.SIZE / 8;

  public static final Integer64TimeInterval ZERO =
    new Integer64TimeInterval(0);
  public static final Integer64TimeInterval EPSILON =
    new Integer64TimeInterval(1);

  public long interval;

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

  public int encodedLength()
  {
    return ENCODED_LENGTH;
  }

  public boolean isEpsilon()
  {
    return interval == EPSILON.interval;
  }

  public boolean isZero()
  {
    return interval == ZERO.interval;
  }

  public boolean isEqualTo(LogicalTimeInterval rhs)
  {
    return equals(rhs);
  }

  public boolean isGreaterThan(LogicalTimeInterval rhs)
  {
    return compareTo(rhs) > 0;
  }

  public boolean isGreaterThanOrEqualTo(LogicalTimeInterval rhs)
  {
    return compareTo(rhs) >= 0;
  }

  public boolean isLessThan(LogicalTimeInterval rhs)
  {
    return compareTo(rhs) < 0;
  }

  public boolean isLessThanOrEqualTo(LogicalTimeInterval rhs)
  {
    return compareTo(rhs) <= 0;
  }

  public void setEpsilon()
  {
    interval = EPSILON.interval;
  }

  public void setZero()
  {
    interval = ZERO.interval;
  }

  public void setTo(LogicalTimeInterval lti)
  {
    assert lti instanceof Integer64Time : String.format("%s", lti);

    interval = ((Integer64TimeInterval) lti).interval;
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
    long diff = interval - rhs.interval;
    return diff > 0l ? 1 : diff < 0l ? -1 : 0;
  }

  @Override
  public boolean equals(Object rhs)
  {
    return rhs instanceof Integer64TimeInterval &&
           interval == ((Integer64TimeInterval) rhs).interval;
  }

  @Override
  public int hashCode()
  {
    return (int) interval;
  }

  @Override
  public String toString()
  {
    return Long.toString(interval);
  }
}