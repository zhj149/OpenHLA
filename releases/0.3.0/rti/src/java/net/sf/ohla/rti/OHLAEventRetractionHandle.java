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

package net.sf.ohla.rti;

import java.io.Serializable;

import hla.rti.EventRetractionHandle;

import hla.rti1516.MessageRetractionHandle;
import hla.rti1516.MessageRetractionReturn;

public class OHLAEventRetractionHandle
  implements EventRetractionHandle, Serializable
{
  protected MessageRetractionHandle messageRetractionHandle;

  public OHLAEventRetractionHandle(
    MessageRetractionHandle messageRetractionHandle)
  {
    this.messageRetractionHandle = messageRetractionHandle;
  }

  public OHLAEventRetractionHandle(
    MessageRetractionReturn messageRetractionReturn)
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
    return rhs instanceof OHLAEventRetractionHandle &&
           messageRetractionHandle.equals(
             ((OHLAEventRetractionHandle) rhs).messageRetractionHandle);
  }
}
