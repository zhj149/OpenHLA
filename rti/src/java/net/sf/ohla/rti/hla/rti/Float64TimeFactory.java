/*
 * Copyright (c) 2005-2011, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.hla.rti;

import hla.rti.CouldNotDecode;
import hla.rti.LogicalTime;
import hla.rti.LogicalTimeFactory;

public class Float64TimeFactory
  implements LogicalTimeFactory
{
  public static final Float64TimeFactory INSTANCE = new Float64TimeFactory();

  public LogicalTime decode(byte[] buffer, int offset)
    throws CouldNotDecode
  {
    if (buffer == null)
    {
      throw new CouldNotDecode();
    }
    else if (buffer.length < Float64Time.ENCODED_LENGTH)
    {
      throw new CouldNotDecode();
    }

    long l = ((long) buffer[offset++] & 0xFF) << 56 |
             ((long) buffer[offset++] & 0xFF) << 48 |
             ((long) buffer[offset++] & 0xFF) << 40 |
             ((long) buffer[offset++] & 0xFF) << 32 |
             ((long) buffer[offset++] & 0xFF) << 24 |
             ((long) buffer[offset++] & 0xFF) << 16 |
             ((long) buffer[offset++] & 0xFF) << 8 |
             ((long) buffer[offset] & 0xFF);

    return new Float64Time(Double.longBitsToDouble(l));
  }

  public LogicalTime makeInitial()
  {
    return new Float64Time(Float64Time.INITIAL);
  }
}
