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

import java.io.Serializable;

import hla.rti.EventRetractionHandle;

import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.MessageRetractionReturn;

public class HLA13EventRetractionHandle
  implements EventRetractionHandle, Serializable
{
  private final MessageRetractionHandle messageRetractionHandle;

  public HLA13EventRetractionHandle(MessageRetractionHandle messageRetractionHandle)
  {
    this.messageRetractionHandle = messageRetractionHandle;
  }

  public HLA13EventRetractionHandle(MessageRetractionReturn messageRetractionReturn)
  {
    this(messageRetractionReturn.handle);
  }

  public MessageRetractionHandle getMessageRetractionHandle()
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
    return rhs instanceof HLA13EventRetractionHandle &&
           messageRetractionHandle.equals(((HLA13EventRetractionHandle) rhs).messageRetractionHandle);
  }
}
