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

import hla.rti1516.CouldNotDecode;
import hla.rti1516.LogicalTime;
import hla.rti1516.LogicalTimeFactory;

public class Integer64TimeFactory
  implements LogicalTimeFactory
{
  public static final Integer64TimeFactory INSTANCE = new Integer64TimeFactory();

  public LogicalTime decode(byte[] buffer, int offset)
    throws CouldNotDecode
  {
    if (buffer == null)
    {
      throw new IllegalArgumentException(I18n.getMessage(ExceptionMessages.DECODE_BUFFER_IS_NULL));
    }
    else if ((buffer.length - offset) < Integer64Time.ENCODED_LENGTH)
    {
      throw new IllegalArgumentException(I18n.getMessage(
        ExceptionMessages.DECODE_BUFFER_IS_TOO_SHORT, Integer64Time.ENCODED_LENGTH, buffer.length - offset));
    }

    long l = ((long) buffer[offset++] & 0xFF) << 56 |
             ((long) buffer[offset++] & 0xFF) << 48 |
             ((long) buffer[offset++] & 0xFF) << 40 |
             ((long) buffer[offset++] & 0xFF) << 32 |
             ((long) buffer[offset++] & 0xFF) << 24 |
             ((long) buffer[offset++] & 0xFF) << 16 |
             ((long) buffer[offset++] & 0xFF) << 8 |
             ((long) buffer[offset] & 0xFF);

    return new Integer64Time(l);
  }

  public LogicalTime makeInitial()
  {
    return Integer64Time.INITIAL;
  }

  public LogicalTime makeFinal()
  {
    return Integer64Time.FINAL;
  }
}
