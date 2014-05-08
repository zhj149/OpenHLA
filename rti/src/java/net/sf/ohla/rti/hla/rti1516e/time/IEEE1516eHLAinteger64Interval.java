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
import hla.rti1516e.time.HLAinteger64Interval;

public class IEEE1516eHLAinteger64Interval
  implements HLAinteger64Interval
{
  public static final byte ENCODED_LENGTH = Long.SIZE / 8;

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
    return Long.compare(interval, rhs.getValue());
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
      throw new IllegalArgumentException(I18n.getMessage(ExceptionMessages.ENCODE_BUFFER_IS_NULL));
    }
    else if ((buffer.length - offset) < ENCODED_LENGTH)
    {
      throw new IllegalArgumentException(I18n.getMessage(
        ExceptionMessages.ENCODE_BUFFER_IS_TOO_SHORT, ENCODED_LENGTH, buffer.length - offset));
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
