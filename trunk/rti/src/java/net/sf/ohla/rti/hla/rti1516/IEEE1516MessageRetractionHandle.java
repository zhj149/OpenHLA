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

import hla.rti1516.MessageRetractionHandle;

public class IEEE1516MessageRetractionHandle
  implements MessageRetractionHandle
{
  private final hla.rti1516e.MessageRetractionHandle messageRetractionHandle;

  public IEEE1516MessageRetractionHandle(hla.rti1516e.MessageRetractionHandle messageRetractionHandle)
  {
    this.messageRetractionHandle = messageRetractionHandle;
  }

  public hla.rti1516e.MessageRetractionHandle getIEEE1516eMessageRetractionHandle()
  {
    return messageRetractionHandle;
  }

  @Override
  public int hashCode()
  {
    return messageRetractionHandle.hashCode();
  }

  @Override
  public boolean equals(Object rhs)
  {
    return this == rhs || (rhs instanceof IEEE1516MessageRetractionHandle &&
                           messageRetractionHandle.equals(((IEEE1516MessageRetractionHandle) rhs).messageRetractionHandle));
  }

  @Override
  public String toString()
  {
    return messageRetractionHandle.toString();
  }
}
