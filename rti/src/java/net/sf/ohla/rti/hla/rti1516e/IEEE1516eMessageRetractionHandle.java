/*
 * Copyright (c) 2005-2010, Michael Newcomb
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

package net.sf.ohla.rti.hla.rti1516e;

import hla.rti1516e.FederateHandle;
import hla.rti1516e.MessageRetractionHandle;

public class IEEE1516eMessageRetractionHandle
  implements MessageRetractionHandle
{
  private final FederateHandle federateHandle;

  private final long messageRetractionHandle;

  public IEEE1516eMessageRetractionHandle(FederateHandle federateHandle, long messageRetractionHandle)
  {
    this.federateHandle = federateHandle;
    this.messageRetractionHandle = messageRetractionHandle;
  }

  public FederateHandle getFederateHandle()
  {
    return federateHandle;
  }

  public long getMessageRetractionHandle()
  {
    return messageRetractionHandle;
  }

  @Override
  public int hashCode()
  {
    return ((int) messageRetractionHandle) & (((IEEE1516eFederateHandle) federateHandle).getHandle() << 24);
  }

  @Override
  public boolean equals(Object rhs)
  {
    return this == rhs || (rhs instanceof IEEE1516eMessageRetractionHandle &&
                           equals((IEEE1516eMessageRetractionHandle) rhs));
  }

  @Override
  public String toString()
  {
    return new StringBuilder(federateHandle.toString()).append(".").append(messageRetractionHandle).toString();
  }

  private boolean equals(IEEE1516eMessageRetractionHandle rhs)
  {
    return messageRetractionHandle == rhs.messageRetractionHandle && federateHandle.equals(rhs.federateHandle);
  }
}
