/*
 * Copyright (c) 2006, Michael Newcomb
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

import net.sf.ohla.rti.hla.rti1516.Integer64TimeInterval;

import hla.rti1516.CouldNotDecode;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.LogicalTimeIntervalFactory;

public class Integer64TimeIntervalFactory
  implements LogicalTimeIntervalFactory
{
  public LogicalTimeInterval decode(byte[] buffer, int offset)
    throws CouldNotDecode
  {
    return new Integer64TimeInterval(buffer, offset);
  }

  public LogicalTimeInterval makeZero()
  {
    return Integer64TimeInterval.ZERO;
  }

  public LogicalTimeInterval makeEpsilon()
  {
    return Integer64TimeInterval.EPSILON;
  }
}
