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

package net.sf.ohla.rti.hla.rti;

import net.sf.ohla.rti.i18n.ExceptionMessages;
import net.sf.ohla.rti.i18n.I18n;

import hla.rti.IllegalTimeArithmetic;
import hla.rti.LogicalTime;
import hla.rti.LogicalTimeInterval;

public class Float64Time
  implements LogicalTime
{
  public static final byte ENCODED_LENGTH = Double.SIZE / 8;

  public static final double INITIAL = 0.0;
  public static final double FINAL = Double.MAX_VALUE;

  public double time;

  public Float64Time(double time)
  {
    this.time = time;
  }

  public void decreaseBy(LogicalTimeInterval logicalTimeInterval)
    throws IllegalTimeArithmetic
  {
    if (logicalTimeInterval instanceof Float64TimeInterval)
    {
      time -= ((Float64TimeInterval) logicalTimeInterval).interval;
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
    if (logicalTimeInterval instanceof Float64TimeInterval)
    {
      time += ((Float64TimeInterval) logicalTimeInterval).interval;
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
    if (rhs instanceof Float64Time)
    {
      time = ((Float64Time) rhs).time;
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
    if (logicalTime instanceof Float64Time)
    {
      logicalTimeInterval = new Float64TimeInterval(time - ((Float64Time) logicalTime).time);
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
      throw new IllegalArgumentException(I18n.getMessage(ExceptionMessages.LOGICAL_TIME_ENCODE_BUFFER_IS_NULL));
    }
    else if ((buffer.length - offset) < ENCODED_LENGTH)
    {
      throw new IllegalArgumentException(I18n.getMessage(
        ExceptionMessages.LOGICAL_TIME_ENCODE_BUFFER_NOT_ENOUGH_SPACE, ENCODED_LENGTH, buffer.length - offset));
    }

    long l = Double.doubleToRawLongBits(time);

    buffer[offset++] = (byte) (l >>> 56);
    buffer[offset++] = (byte) (l >>> 48);
    buffer[offset++] = (byte) (l >>> 40);
    buffer[offset++] = (byte) (l >>> 32);
    buffer[offset++] = (byte) (l >>> 24);
    buffer[offset++] = (byte) (l >>> 16);
    buffer[offset++] = (byte) (l >>> 8);
    buffer[offset] = (byte) l;
  }

  public int compareTo(Object rhs)
  {
    return compareTo((Float64Time) rhs);
  }

  public int compareTo(Float64Time rhs)
  {
    double diff = time - rhs.time;
    return diff > 0L ? 1 : diff < 0L ? -1 : 0;
  }

  @Override
  public boolean equals(Object rhs)
  {
    return this == rhs || (rhs instanceof Float64Time && time == ((Float64Time) rhs).time);
  }

  @Override
  public int hashCode()
  {
    return (int) time;
  }

  @Override
  public String toString()
  {
    return Double.toString(time);
  }
}
