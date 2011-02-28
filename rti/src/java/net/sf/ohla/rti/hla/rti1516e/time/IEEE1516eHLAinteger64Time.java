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
import hla.rti1516e.time.HLAinteger64Time;

public class IEEE1516eHLAinteger64Time
  implements HLAinteger64Time
{
  public static final IEEE1516eHLAinteger64Time INITIAL = new IEEE1516eHLAinteger64Time(0L);
  public static final IEEE1516eHLAinteger64Time FINAL = new IEEE1516eHLAinteger64Time(Long.MAX_VALUE);

  private final long time;

  public IEEE1516eHLAinteger64Time(long time)
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

  public HLAinteger64Time add(HLAinteger64Interval interval)
  {
    return new IEEE1516eHLAinteger64Time(time + interval.getValue());
  }

  public HLAinteger64Time subtract(HLAinteger64Interval interval)
  {
    return new IEEE1516eHLAinteger64Time(time - interval.getValue());
  }

  public HLAinteger64Interval distance(HLAinteger64Time time)
  {
    return new IEEE1516eHLAinteger64Interval(this.time - time.getValue());
  }

  public int compareTo(HLAinteger64Time rhs)
  {
    long result = time - rhs.getValue();
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
      throw new CouldNotEncode(I18n.getMessage(ExceptionMessages.ENCODE_BUFFER_IS_NULL));
    }
    else if ((buffer.length - offset) < 8)
    {
      throw new CouldNotEncode(I18n.getMessage(ExceptionMessages.ENCODE_BUFFER_IS_TOO_SHORT));
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

  public long getValue()
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
    return this == rhs || (rhs instanceof HLAinteger64Time && time == ((HLAinteger64Time) rhs).getValue());
  }

  @Override
  public String toString()
  {
    return Long.toString(time);
  }
}
