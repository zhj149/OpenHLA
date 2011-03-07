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

package net.sf.ohla.rti.hla.rti1516;

import net.sf.ohla.rti.i18n.ExceptionMessages;
import net.sf.ohla.rti.i18n.I18n;

import hla.rti1516.LogicalTimeInterval;

public class Float64TimeInterval
  implements LogicalTimeInterval
{
  public static final byte ENCODED_LENGTH = Long.SIZE / 8;

  public static final Float64TimeInterval ZERO = new Float64TimeInterval(0.0);
  public static final Float64TimeInterval EPSILON = new Float64TimeInterval(Double.MIN_NORMAL);

  public final double interval;

  public Float64TimeInterval(double interval)
  {
    this.interval = interval;
  }

  public boolean isEpsilon()
  {
    return interval == EPSILON.interval;
  }

  public boolean isZero()
  {
    return interval == ZERO.interval;
  }

  public LogicalTimeInterval subtract(LogicalTimeInterval logicalTimeInterval)
  {
    double result;
    if (logicalTimeInterval instanceof Float64TimeInterval)
    {
      result = interval - ((Float64TimeInterval) logicalTimeInterval).interval;
    }
    else if (logicalTimeInterval == null)
    {
      throw new IllegalArgumentException(I18n.getMessage(ExceptionMessages.LOGICAL_TIME_INTERVAL_IS_NULL));
    }
    else
    {
      throw new IllegalArgumentException(I18n.getMessage(ExceptionMessages.INVALID_LOGICAL_TIME_INTERVAL_TYPE));
    }
    return new Float64TimeInterval(result);
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
    return compareTo((Float64TimeInterval) rhs);
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

  private int compareTo(Float64TimeInterval rhs)
  {
    return (int) Math.round(interval - rhs.interval);
  }
}
