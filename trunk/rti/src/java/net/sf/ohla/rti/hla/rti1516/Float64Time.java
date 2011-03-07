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

import hla.rti1516.IllegalTimeArithmetic;
import hla.rti1516.LogicalTime;
import hla.rti1516.LogicalTimeInterval;

public class Float64Time
  implements LogicalTime
{
  public static final byte ENCODED_LENGTH = Double.SIZE / 8;

  public static final Float64Time INITIAL = new Float64Time(0.0);
  public static final Float64Time FINAL = new Float64Time(Double.MAX_VALUE);

  public final double time;

  public Float64Time(double time)
  {
    this.time = time;
  }

  public boolean isInitial()
  {
    return time == INITIAL.time;
  }

  public boolean isFinal()
  {
    return time == FINAL.time;
  }

  public LogicalTime add(LogicalTimeInterval logicalTimeInterval)
    throws IllegalTimeArithmetic
  {
    double result;
    if (logicalTimeInterval instanceof Float64TimeInterval)
    {
      result = time + ((Float64TimeInterval) logicalTimeInterval).interval;
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

    if (result < INITIAL.time)
    {
      throw new IllegalTimeArithmetic(I18n.getMessage(
        ExceptionMessages.ILLEGAL_TIME_ARITHMETIC_RESULT_LESS_THAN_INITIAL, result, INITIAL));
    }
    else if (result > FINAL.time)
    {
      throw new IllegalTimeArithmetic(I18n.getMessage(
        ExceptionMessages.ILLEGAL_TIME_ARITHMETIC_RESULT_GREATER_THAN_FINAL, result, FINAL));
    }

    return new Float64Time(result);
  }

  public LogicalTime subtract(LogicalTimeInterval logicalTimeInterval)
    throws IllegalTimeArithmetic
  {
    double result;
    if (logicalTimeInterval instanceof Float64TimeInterval)
    {
      result = time - ((Float64TimeInterval) logicalTimeInterval).interval;
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

    if (result < INITIAL.time)
    {
      throw new IllegalTimeArithmetic(I18n.getMessage(
        ExceptionMessages.ILLEGAL_TIME_ARITHMETIC_RESULT_LESS_THAN_INITIAL, result, INITIAL));
    }
    else if (result > FINAL.time)
    {
      throw new IllegalTimeArithmetic(I18n.getMessage(
        ExceptionMessages.ILLEGAL_TIME_ARITHMETIC_RESULT_GREATER_THAN_FINAL, result, FINAL));
    }

    return new Float64Time(result);
  }

  public LogicalTimeInterval distance(LogicalTime logicalTime)
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
      throw new IllegalArgumentException(I18n.getMessage(ExceptionMessages.ENCODE_BUFFER_IS_NULL));
    }
    else if ((buffer.length - offset) < ENCODED_LENGTH)
    {
      throw new IllegalArgumentException(I18n.getMessage(
        ExceptionMessages.ENCODE_BUFFER_IS_TOO_SHORT, ENCODED_LENGTH, buffer.length - offset));
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

  private int compareTo(Float64Time rhs)
  {
    return (int) Math.round(time - rhs.time);
  }
}
