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

package net.sf.ohla.rti.hla.rti;

import hla.rti.CouldNotDecode;
import hla.rti.LogicalTime;
import hla.rti.LogicalTimeFactory;

public class Integer64TimeFactory
  implements LogicalTimeFactory
{
  public LogicalTime decode(byte[] buffer, int offset)
    throws CouldNotDecode
  {
    return new Integer64Time(buffer, offset);
  }

  public LogicalTime makeInitial()
  {
    return new Integer64Time(Integer64Time.INITIAL.time);
  }
}