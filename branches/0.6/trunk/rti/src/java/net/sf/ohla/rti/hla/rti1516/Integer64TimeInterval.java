/*
 * Copyright (c) 2006-2007, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

public class Integer64TimeInterval
  implements LogicalTimeInterval
{
  public static final byte ENCODED_LENGTH = Long.SIZE / 8;

  public static final Integer64TimeInterval ZERO = new Integer64TimeInterval(0);
  public static final Integer64TimeInterval EPSILON = new Integer64TimeInterval(1);

  public final long interval;

  public Integer64TimeInterval(long interval)
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

  public LogicalTimeInterval subtract(LogicalTimeInterval lti)
  {
    assert lti instanceof Integer64TimeInterval;

    return new Integer64TimeInterval(interval - ((Integer64TimeInterval) lti).interval);
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

    buffer[offset++] = (byte) (interval >>> 56);
    buffer[offset++] = (byte) (interval >>> 48);
    buffer[offset++] = (byte) (interval >>> 40);
    buffer[offset++] = (byte) (interval >>> 32);
    buffer[offset++] = (byte) (interval >>> 24);
    buffer[offset++] = (byte) (interval >>> 16);
    buffer[offset++] = (byte) (interval >>> 8);
    buffer[offset] = (byte) interval;
  }

  public int compareTo(Object rhs)
  {
    return compareTo((Integer64TimeInterval) rhs);
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
    return Long.toString(interval);
  }

  private int compareTo(Integer64TimeInterval rhs)
  {
    return Long.compare(interval, rhs.interval);
  }
}
