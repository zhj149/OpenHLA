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
import hla.rti1516e.time.HLAinteger64Interval;
import hla.rti1516e.time.HLAinteger64Time;
import hla.rti1516e.time.HLAinteger64TimeFactory;

public class IEEE1516eHLAinteger64TimeFactory
  implements HLAinteger64TimeFactory
{
  public HLAinteger64Time decodeTime(byte[] buffer, int offset)
    throws CouldNotDecode
  {
    if (buffer == null)
    {
      throw new CouldNotDecode(I18n.getMessage(ExceptionMessages.DECODE_BUFFER_IS_NULL));
    }
    else if ((buffer.length - offset) < 8)
    {
      throw new CouldNotDecode(I18n.getMessage(ExceptionMessages.DECODE_BUFFER_IS_TOO_SHORT));
    }

    long l = (((long) buffer[offset++] << 56) +
              ((long) (buffer[offset++] & 255) << 48) +
              ((long) (buffer[offset++] & 255) << 40) +
              ((long) (buffer[offset++] & 255) << 32) +
              ((long) (buffer[offset++] & 255) << 24) +
              ((buffer[offset++] & 255) << 16) +
              ((buffer[offset++] & 255) <<  8) +
              (buffer[offset] & 255));
    return new IEEE1516eHLAinteger64Time(l);
  }

  public HLAinteger64Interval decodeInterval(byte[] buffer, int offset)
    throws CouldNotDecode
  {
    if (buffer == null)
    {
      throw new CouldNotDecode(I18n.getMessage(ExceptionMessages.DECODE_BUFFER_IS_NULL));
    }
    else if ((buffer.length - offset) < 8)
    {
      throw new CouldNotDecode(I18n.getMessage(ExceptionMessages.DECODE_BUFFER_IS_TOO_SHORT));
    }

    long l = (((long) buffer[offset++] << 56) +
              ((long) (buffer[offset++] & 255) << 48) +
              ((long) (buffer[offset++] & 255) << 40) +
              ((long) (buffer[offset++] & 255) << 32) +
              ((long) (buffer[offset++] & 255) << 24) +
              ((buffer[offset++] & 255) << 16) +
              ((buffer[offset++] & 255) <<  8) +
              (buffer[offset] & 255));
    return new IEEE1516eHLAinteger64Interval(l);
  }

  public HLAinteger64Time makeInitial()
  {
    return IEEE1516eHLAinteger64Time.INITIAL;
  }

  public HLAinteger64Time makeFinal()
  {
    return IEEE1516eHLAinteger64Time.FINAL;
  }

  public HLAinteger64Time makeTime(long value)
  {
    return new IEEE1516eHLAinteger64Time(value);
  }

  public HLAinteger64Interval makeZero()
  {
    return IEEE1516eHLAinteger64Interval.ZERO;
  }

  public HLAinteger64Interval makeEpsilon()
  {
    return IEEE1516eHLAinteger64Interval.EPSILON;
  }

  public HLAinteger64Interval makeInterval(long value)
  {
    return new IEEE1516eHLAinteger64Interval(value);
  }

  public String getName()
  {
    return NAME;
  }
}
