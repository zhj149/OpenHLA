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
import hla.rti1516e.time.HLAinteger64Interval;

public class IEEE1516eHLAinteger64Interval
  implements HLAinteger64Interval
{
  public static final IEEE1516eHLAinteger64Interval ZERO = new IEEE1516eHLAinteger64Interval(0L);
  public static final IEEE1516eHLAinteger64Interval EPSILON = new IEEE1516eHLAinteger64Interval(1L);

  private final long interval;

  public IEEE1516eHLAinteger64Interval(long interval)
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

  public HLAinteger64Interval add(HLAinteger64Interval interval)
  {
    return new IEEE1516eHLAinteger64Interval(this.interval + interval.getValue());
  }

  public HLAinteger64Interval subtract(HLAinteger64Interval interval)
  {
    return new IEEE1516eHLAinteger64Interval(this.interval - interval.getValue());
  }

  public int compareTo(HLAinteger64Interval rhs)
  {
    long result = interval - rhs.getValue();
    return result < Integer.MIN_VALUE ? -1 : result > Integer.MAX_VALUE ? 1 : (int) result;
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

    buffer[offset++] = (byte) (interval >>> 56);
    buffer[offset++] = (byte) (interval >>> 48);
    buffer[offset++] = (byte) (interval >>> 40);
    buffer[offset++] = (byte) (interval >>> 32);
    buffer[offset++] = (byte) (interval >>> 24);
    buffer[offset++] = (byte) (interval >>> 16);
    buffer[offset++] = (byte) (interval >>> 8);
    buffer[offset] = (byte) interval;
  }

  public long getValue()
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
    return this == rhs || (rhs instanceof HLAinteger64Interval && interval == ((HLAinteger64Interval) rhs).getValue());
  }

  @Override
  public String toString()
  {
    return Long.toString(interval);
  }
}
