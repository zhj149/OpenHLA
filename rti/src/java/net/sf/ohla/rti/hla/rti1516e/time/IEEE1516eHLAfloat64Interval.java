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

public class IEEE1516eHLAfloat64Interval
  implements HLAfloat64Interval
{
  public static final IEEE1516eHLAfloat64Interval ZERO = new IEEE1516eHLAfloat64Interval(0.0);
  public static final IEEE1516eHLAfloat64Interval EPSILON = new IEEE1516eHLAfloat64Interval(Double.MIN_NORMAL);

  private final double interval;

  public IEEE1516eHLAfloat64Interval(double interval)
  {
    this.interval = interval;
  }

  public boolean isZero()
  {
    return interval == ZERO.interval;
  }

  public boolean isEpsilon()
  {
    return interval == EPSILON.interval;
  }

  public HLAfloat64Interval add(HLAfloat64Interval interval)
    throws IllegalTimeArithmetic
  {
    double result = this.interval + interval.getValue();
    if (Double.isNaN(result))
    {
      throw new IllegalTimeArithmetic(I18n.getMessage(
        ExceptionMessages.LOGICAL_TIME_INTERVAL_ADDITION_IS_NAN, this.interval, interval.getValue()));
    }
    else if (Double.isInfinite(result))
    {
      throw new IllegalTimeArithmetic(I18n.getMessage(
        ExceptionMessages.LOGICAL_TIME_INTERVAL_ADDITION_IS_INFINITE, this.interval, interval.getValue()));
    }
    return new IEEE1516eHLAfloat64Interval(result);
  }

  public HLAfloat64Interval subtract(HLAfloat64Interval interval)
    throws IllegalTimeArithmetic
  {
    double result = this.interval - interval.getValue();
    if (Double.isNaN(result))
    {
      throw new IllegalTimeArithmetic(I18n.getMessage(
        ExceptionMessages.LOGICAL_TIME_INTERVAL_SUBTRACTION_IS_NAN, this.interval, interval.getValue()));
    }
    else if (Double.isInfinite(result))
    {
      throw new IllegalTimeArithmetic(I18n.getMessage(
        ExceptionMessages.LOGICAL_TIME_INTERVAL_SUBTRACTION_IS_INFINITE, this.interval, interval.getValue()));
    }
    return new IEEE1516eHLAfloat64Interval(result);
  }

  public int compareTo(HLAfloat64Interval rhs)
  {
    return (int) Math.round(interval - rhs.getValue());
  }

  public int encodedLength()
  {
    return 8;
  }

  public void encode(byte[] buffer, int offset)
    throws CouldNotEncode
  {
    if (buffer == null)
    {
      throw new CouldNotEncode(I18n.getMessage(ExceptionMessages.ENCODE_BUFFER_IS_NULL));
    }
    else if ((buffer.length - offset) < 8)
    {
      throw new CouldNotEncode(I18n.getMessage(ExceptionMessages.ENCODE_BUFFER_IS_TOO_SHORT));
    }

    long l = Double.doubleToLongBits(interval);
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
    return interval;
  }

  @Override
  public int hashCode()
  {
    return (int) interval;
  }

  @Override
  public boolean equals(Object rhs)
  {
    return this == rhs || (rhs instanceof HLAfloat64Interval && interval == ((HLAfloat64Interval) rhs).getValue());
  }

  @Override
  public String toString()
  {
    return Double.toString(interval);
  }
}
