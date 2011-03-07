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

package net.sf.ohla.rti.hla.rti;

import net.sf.ohla.rti.i18n.ExceptionMessages;
import net.sf.ohla.rti.i18n.I18n;

import hla.rti.IllegalTimeArithmetic;
import hla.rti.LogicalTime;
import hla.rti.LogicalTimeInterval;

public class Integer64Time
  implements LogicalTime
{
  public static final byte ENCODED_LENGTH = Long.SIZE / 8;

  public static final long INITIAL = 0L;
  public static final long FINAL = Long.MAX_VALUE;

  public long time;

  public Integer64Time(long time)
  {
    this.time = time;
  }

  public void decreaseBy(LogicalTimeInterval logicalTimeInterval)
    throws IllegalTimeArithmetic
  {
    if (logicalTimeInterval instanceof Integer64TimeInterval)
    {
      time -= ((Integer64TimeInterval) logicalTimeInterval).interval;
    }
    else if (logicalTimeInterval == null)
    {
      throw new IllegalTimeArithmetic(I18n.getMessage(ExceptionMessages.LOGICAL_TIME_INTERVAL_IS_NULL));
    }
    else
    {
      throw new IllegalTimeArithmetic(I18n.getMessage(
        ExceptionMessages.ILLEGAL_TIME_ARITHMETIC_INVALID_LOGICAL_TIME_INTERVAL_TYPE));
    }
  }

  public void increaseBy(LogicalTimeInterval logicalTimeInterval)
    throws IllegalTimeArithmetic
  {
    if (logicalTimeInterval instanceof Integer64TimeInterval)
    {
      time += ((Integer64TimeInterval) logicalTimeInterval).interval;
    }
    else if (logicalTimeInterval == null)
    {
      throw new IllegalTimeArithmetic(I18n.getMessage(ExceptionMessages.LOGICAL_TIME_INTERVAL_IS_NULL));
    }
    else
    {
      throw new IllegalTimeArithmetic(I18n.getMessage(
        ExceptionMessages.ILLEGAL_TIME_ARITHMETIC_INVALID_LOGICAL_TIME_INTERVAL_TYPE, logicalTimeInterval.getClass()));
    }
  }

  public boolean isInitial()
  {
    return time == INITIAL;
  }

  public boolean isFinal()
  {
    return time == FINAL;
  }

  public boolean isEqualTo(LogicalTime rhs)
  {
    return equals(rhs);
  }

  public boolean isGreaterThan(LogicalTime rhs)
  {
    return compareTo(rhs) > 0;
  }

  public boolean isGreaterThanOrEqualTo(LogicalTime rhs)
  {
    return compareTo(rhs) >= 0;
  }

  public boolean isLessThan(LogicalTime rhs)
  {
    return compareTo(rhs) < 0;
  }

  public boolean isLessThanOrEqualTo(LogicalTime rhs)
  {
    return compareTo(rhs) <= 0;
  }

  public void setFinal()
  {
    time = FINAL;
  }

  public void setInitial()
  {
    time = INITIAL;
  }

  public void setTo(LogicalTime rhs)
  {
    if (rhs instanceof Integer64Time)
    {
      time = ((Integer64Time) rhs).time;
    }
    else if (rhs == null)
    {
      throw new IllegalArgumentException(I18n.getMessage(ExceptionMessages.LOGICAL_TIME_IS_NULL));
    }
    else
    {
      throw new IllegalArgumentException(I18n.getMessage(ExceptionMessages.INVALID_LOGICAL_TIME_TYPE, rhs.getClass()));
    }
  }

  public LogicalTimeInterval subtract(LogicalTime logicalTime)
  {
    LogicalTimeInterval logicalTimeInterval;
    if (logicalTime instanceof Integer64Time)
    {
      logicalTimeInterval = new Integer64TimeInterval(time - ((Integer64Time) logicalTime).time);
    }
    else if (logicalTime == null)
    {
      throw new IllegalArgumentException(I18n.getMessage(ExceptionMessages.LOGICAL_TIME_IS_NULL));
    }
    else
    {
      throw new IllegalArgumentException(I18n.getMessage(
        ExceptionMessages.INVALID_LOGICAL_TIME_TYPE, logicalTime.getClass()));
    }
    return logicalTimeInterval;
  }

  public int encodedLength()
  {
    return ENCODED_LENGTH;
  }

  public void encode(byte[] buffer, int offset)
  {
    if (buffer == null)
    {
      throw new IllegalArgumentException(I18n.getMessage(ExceptionMessages.ENCODE_BUFFER_IS_NULL));
    }
    else if ((buffer.length - offset) < ENCODED_LENGTH)
    {
      throw new IllegalArgumentException(I18n.getMessage(
        ExceptionMessages.ENCODE_BUFFER_IS_TOO_SHORT, ENCODED_LENGTH, buffer.length - offset));
    }

    buffer[offset++] = (byte) (time >>> 56);
    buffer[offset++] = (byte) (time >>> 48);
    buffer[offset++] = (byte) (time >>> 40);
    buffer[offset++] = (byte) (time >>> 32);
    buffer[offset++] = (byte) (time >>> 24);
    buffer[offset++] = (byte) (time >>> 16);
    buffer[offset++] = (byte) (time >>> 8);
    buffer[offset] = (byte) time;
  }

  public int compareTo(Object rhs)
  {
    return compareTo((Integer64Time) rhs);
  }

  @Override
  public boolean equals(Object rhs)
  {
    return this == rhs || (rhs instanceof Integer64Time && time == ((Integer64Time) rhs).time);
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

  private int compareTo(Integer64Time rhs)
  {
    return Math.round(time - rhs.time);
  }
}
