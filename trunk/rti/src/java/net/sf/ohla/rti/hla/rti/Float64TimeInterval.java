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

import hla.rti.LogicalTimeInterval;

public class Float64TimeInterval
  implements LogicalTimeInterval
{
  public static final byte ENCODED_LENGTH = Long.SIZE / 8;

  public static final double ZERO = 0.0;
  public static final double EPSILON = Double.MIN_NORMAL;

  public double interval;

  public Float64TimeInterval(double interval)
  {
    this.interval = interval;
  }

  public int encodedLength()
  {
    return ENCODED_LENGTH;
  }

  public boolean isEpsilon()
  {
    return interval == EPSILON;
  }

  public boolean isZero()
  {
    return interval == ZERO;
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
    interval = EPSILON;
  }

  public void setZero()
  {
    interval = ZERO;
  }

  public void setTo(LogicalTimeInterval rhs)
  {
    if (rhs instanceof Float64TimeInterval)
    {
      interval = ((Float64TimeInterval) rhs).interval;
    }
    else if (rhs == null)
    {
      throw new IllegalArgumentException(I18n.getMessage(ExceptionMessages.LOGICAL_TIME_INTERVAL_IS_NULL));
    }
    else
    {
      throw new IllegalArgumentException(I18n.getMessage(
        ExceptionMessages.INVALID_LOGICAL_TIME_INTERVAL_TYPE, rhs.getClass()));
    }
  }

  public void encode(byte[] buffer, int offset)
  {
    if (buffer == null)
    {
      throw new IllegalArgumentException(I18n.getMessage(
        ExceptionMessages.ENCODE_BUFFER_IS_NULL));
    }
    else if ((buffer.length - offset) < ENCODED_LENGTH)
    {
      throw new IllegalArgumentException(I18n.getMessage(
        ExceptionMessages.ENCODE_BUFFER_IS_TOO_SHORT, ENCODED_LENGTH, buffer.length - offset));
    }

    long l = Double.doubleToRawLongBits(interval);

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
    return compareTo((Integer64TimeInterval) rhs);
  }

  @Override
  public boolean equals(Object rhs)
  {
    return this == rhs || (rhs instanceof Integer64TimeInterval && interval == ((Integer64TimeInterval) rhs).interval);
  }

  @Override
  public int hashCode()
  {
    return (int) interval;
  }

  @Override
  public String toString()
  {
    return Double.toString(interval);
  }

  private int compareTo(Integer64TimeInterval rhs)
  {
    return Double.compare(interval, rhs.interval);
  }
}
