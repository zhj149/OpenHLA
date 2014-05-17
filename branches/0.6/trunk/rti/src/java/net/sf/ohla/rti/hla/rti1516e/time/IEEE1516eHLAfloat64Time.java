/*
 * Copyright (c) 2005-2011, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.hla.rti1516e.time;

import net.sf.ohla.rti.i18n.ExceptionMessages;
import net.sf.ohla.rti.i18n.I18n;

import hla.rti1516e.exceptions.CouldNotEncode;
import hla.rti1516e.exceptions.IllegalTimeArithmetic;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;

public class IEEE1516eHLAfloat64Time
  implements HLAfloat64Time
{
  public static final byte ENCODED_LENGTH = Double.SIZE / 8;

  public static final IEEE1516eHLAfloat64Time INITIAL = new IEEE1516eHLAfloat64Time(0.0);
  public static final IEEE1516eHLAfloat64Time FINAL = new IEEE1516eHLAfloat64Time(Double.MAX_VALUE);

  private final double time;

  public IEEE1516eHLAfloat64Time(double time)
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

  public HLAfloat64Time add(HLAfloat64Interval interval)
    throws IllegalTimeArithmetic
  {
    double result = time + interval.getValue();

    if (Double.isNaN(result))
    {
      throw new IllegalTimeArithmetic(I18n.getMessage(
        ExceptionMessages.LOGICAL_TIME_ADDITION_IS_NAN, time, interval.getValue()));
    }
    else if (Double.isInfinite(result))
    {
      throw new IllegalTimeArithmetic(I18n.getMessage(
        ExceptionMessages.LOGICAL_TIME_ADDITION_IS_INFINITE, time, interval.getValue()));
    }
    else if (result < INITIAL.time)
    {
      throw new IllegalTimeArithmetic(I18n.getMessage(
        ExceptionMessages.ILLEGAL_TIME_ARITHMETIC_RESULT_LESS_THAN_INITIAL, result, INITIAL));
    }
    else if (result > FINAL.time)
    {
      throw new IllegalTimeArithmetic(I18n.getMessage(
        ExceptionMessages.ILLEGAL_TIME_ARITHMETIC_RESULT_GREATER_THAN_FINAL, result, FINAL));
    }

    return new IEEE1516eHLAfloat64Time(result);
  }

  public HLAfloat64Time subtract(HLAfloat64Interval interval)
    throws IllegalTimeArithmetic
  {
    double result = time - interval.getValue();

    if (Double.isNaN(result))
    {
      throw new IllegalTimeArithmetic(I18n.getMessage(
        ExceptionMessages.LOGICAL_TIME_SUBTRACTION_IS_NAN, time, interval.getValue()));
    }
    else if (Double.isInfinite(result))
    {
      throw new IllegalTimeArithmetic(I18n.getMessage(
        ExceptionMessages.LOGICAL_TIME_SUBTRACTION_IS_INFINITE, time, interval.getValue()));
    }
    else if (result < INITIAL.time)
    {
      throw new IllegalTimeArithmetic(I18n.getMessage(
        ExceptionMessages.ILLEGAL_TIME_ARITHMETIC_RESULT_LESS_THAN_INITIAL, result, INITIAL));
    }
    else if (result > FINAL.time)
    {
      throw new IllegalTimeArithmetic(I18n.getMessage(
        ExceptionMessages.ILLEGAL_TIME_ARITHMETIC_RESULT_GREATER_THAN_FINAL, result, FINAL));
    }

    return new IEEE1516eHLAfloat64Time(result);
  }

  public HLAfloat64Interval distance(HLAfloat64Time interval)
  {
    return new IEEE1516eHLAfloat64Interval(time - interval.getValue());
  }

  public int compareTo(HLAfloat64Time rhs)
  {
    return Double.compare(time, rhs.getValue());
  }

  public int encodedLength()
  {
    return ENCODED_LENGTH;
  }

  public void encode(byte[] buffer, int offset)
    throws CouldNotEncode
  {
    if (buffer == null)
    {
      throw new CouldNotEncode(I18n.getMessage(ExceptionMessages.ENCODE_BUFFER_IS_NULL));
    }
    else if ((buffer.length - offset) < ENCODED_LENGTH)
    {
      throw new CouldNotEncode(I18n.getMessage(
        ExceptionMessages.ENCODE_BUFFER_IS_TOO_SHORT, ENCODED_LENGTH, buffer.length - offset));
    }

    long l = Double.doubleToLongBits(time);

    buffer[offset++] = (byte) (l >>> 56);
    buffer[offset++] = (byte) (l >>> 48);
    buffer[offset++] = (byte) (l >>> 40);
    buffer[offset++] = (byte) (l >>> 32);
    buffer[offset++] = (byte) (l >>> 24);
    buffer[offset++] = (byte) (l >>> 16);
    buffer[offset++] = (byte) (l >>> 8);
    buffer[offset] = (byte) l;
  }

  public double getValue()
  {
    return time;
  }

  @Override
  public int hashCode()
  {
    return (int) time;
  }

  @Override
  public boolean equals(Object rhs)
  {
    return this == rhs || (rhs instanceof HLAfloat64Time && time == ((HLAfloat64Time) rhs).getValue());
  }

  @Override
  public String toString()
  {
    return Double.toString(time);
  }
}
