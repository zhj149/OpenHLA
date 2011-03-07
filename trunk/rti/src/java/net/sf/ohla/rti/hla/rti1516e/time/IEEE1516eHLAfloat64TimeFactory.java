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

import hla.rti1516e.exceptions.CouldNotDecode;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;

public class IEEE1516eHLAfloat64TimeFactory
  implements HLAfloat64TimeFactory
{
  public HLAfloat64Time decodeTime(byte[] buffer, int offset)
    throws CouldNotDecode
  {
    if (buffer == null)
    {
      throw new CouldNotDecode(I18n.getMessage(ExceptionMessages.DECODE_BUFFER_IS_NULL));
    }
    else if ((buffer.length - offset) < IEEE1516eHLAfloat64Time.ENCODED_LENGTH)
    {
      throw new CouldNotDecode(I18n.getMessage(
        ExceptionMessages.DECODE_BUFFER_IS_TOO_SHORT, IEEE1516eHLAfloat64Time.ENCODED_LENGTH, buffer.length - offset));
    }

    long l = ((long) buffer[offset++] & 0xFF) << 56 |
             ((long) buffer[offset++] & 0xFF) << 48 |
             ((long) buffer[offset++] & 0xFF) << 40 |
             ((long) buffer[offset++] & 0xFF) << 32 |
             ((long) buffer[offset++] & 0xFF) << 24 |
             ((long) buffer[offset++] & 0xFF) << 16 |
             ((long) buffer[offset++] & 0xFF) << 8 |
             ((long) buffer[offset] & 0xFF);

    return new IEEE1516eHLAfloat64Time(Double.longBitsToDouble(l));
  }

  public HLAfloat64Interval decodeInterval(byte[] buffer, int offset)
    throws CouldNotDecode
  {
    if (buffer == null)
    {
      throw new CouldNotDecode(I18n.getMessage(ExceptionMessages.DECODE_BUFFER_IS_NULL));
    }
    else if ((buffer.length - offset) < 8)
    {
      throw new CouldNotDecode(I18n.getMessage(
        ExceptionMessages.DECODE_BUFFER_IS_TOO_SHORT, IEEE1516eHLAfloat64Interval.ENCODED_LENGTH, buffer.length - offset));
    }

    long l = ((long) buffer[offset++] & 0xFF) << 56 |
             ((long) buffer[offset++] & 0xFF) << 48 |
             ((long) buffer[offset++] & 0xFF) << 40 |
             ((long) buffer[offset++] & 0xFF) << 32 |
             ((long) buffer[offset++] & 0xFF) << 24 |
             ((long) buffer[offset++] & 0xFF) << 16 |
             ((long) buffer[offset++] & 0xFF) << 8 |
             ((long) buffer[offset] & 0xFF);

    return new IEEE1516eHLAfloat64Interval(Double.longBitsToDouble(l));
  }

  public HLAfloat64Time makeInitial()
  {
    return IEEE1516eHLAfloat64Time.INITIAL;
  }

  public HLAfloat64Time makeFinal()
  {
    return IEEE1516eHLAfloat64Time.FINAL;
  }

  public HLAfloat64Time makeTime(double value)
  {
    return new IEEE1516eHLAfloat64Time(value);
  }

  public HLAfloat64Interval makeZero()
  {
    return IEEE1516eHLAfloat64Interval.ZERO;
  }

  public HLAfloat64Interval makeEpsilon()
  {
    return IEEE1516eHLAfloat64Interval.EPSILON;
  }

  public HLAfloat64Interval makeInterval(double value)
  {
    return new IEEE1516eHLAfloat64Interval(value);
  }

  public String getName()
  {
    return NAME;
  }
}
