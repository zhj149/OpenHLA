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

import hla.rti1516e.exceptions.CouldNotEncode;
import hla.rti1516e.exceptions.IllegalTimeArithmetic;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;

public class IEEE1516eHLAfloat64Time
  implements HLAfloat64Time
{
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
      throw new IllegalTimeArithmetic(time + " + " + interval.getValue() + " is NaN");
    }
    else if (Double.isInfinite(result))
    {
      throw new IllegalTimeArithmetic(time + " + " + interval.getValue() + " is infinite");
    }
    return new IEEE1516eHLAfloat64Time(result);
  }

  public HLAfloat64Time subtract(HLAfloat64Interval interval)
    throws IllegalTimeArithmetic
  {
    double result = time - interval.getValue();
    if (Double.isNaN(result))
    {
      throw new IllegalTimeArithmetic(time + " - " + interval.getValue() + " is NaN");
    }
    else if (Double.isInfinite(result))
    {
      throw new IllegalTimeArithmetic(time + " - " + interval.getValue() + " is infinite");
    }
    else if (result < INITIAL.time)
    {
      throw new IllegalTimeArithmetic(time + " - " + interval.getValue() + " is < " + INITIAL.time);
    }
    return new IEEE1516eHLAfloat64Time(result);
  }

  public HLAfloat64Interval distance(HLAfloat64Time interval)
  {
    return new IEEE1516eHLAfloat64Interval(time - interval.getValue());
  }

  public int compareTo(HLAfloat64Time rhs)
  {
    return (int) Math.round(time - rhs.getValue());
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
      throw new CouldNotEncode("buffer cannot be null");
    }
    else if ((buffer.length - offset) < 8)
    {
      throw new CouldNotEncode("buffer is too short");
    }

    long l = Double.doubleToLongBits(time);
    buffer[offset++] = (byte)(l >>> 56);
    buffer[offset++] = (byte)(l >>> 48);
    buffer[offset++] = (byte)(l >>> 40);
    buffer[offset++] = (byte)(l >>> 32);
    buffer[offset++] = (byte)(l >>> 24);
    buffer[offset++] = (byte)(l >>> 16);
    buffer[offset++] = (byte)(l >>> 8);
    buffer[offset] = (byte)(l >>> 0);
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