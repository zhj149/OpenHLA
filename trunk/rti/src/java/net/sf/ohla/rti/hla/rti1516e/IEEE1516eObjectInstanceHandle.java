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

import java.nio.ByteBuffer;

import hla.rti1516e.FederateHandle;
import hla.rti1516e.ObjectInstanceHandle;

public class IEEE1516eObjectInstanceHandle
  implements ObjectInstanceHandle
{
  private final FederateHandle federateHandle;

  private final long objectInstanceHandle;

  private transient volatile Integer hashCode;

  public IEEE1516eObjectInstanceHandle(FederateHandle federateHandle, long objectInstanceHandle)
  {
    this.federateHandle = federateHandle;
    this.objectInstanceHandle = objectInstanceHandle;
  }

  public FederateHandle getFederateHandle()
  {
    return federateHandle;
  }

  public long getObjectInstanceHandle()
  {
    return objectInstanceHandle;
  }

  public int encodedLength()
  {
    return federateHandle.encodedLength() + 8;
  }

  public void encode(byte[] buffer, int offset)
  {
    ByteBuffer.wrap(buffer, offset, 8).putLong(objectInstanceHandle);
    federateHandle.encode(buffer, offset + 8);
  }

  @Override
  public int hashCode()
  {
    if (hashCode == null)
    {
      hashCode = ((((IEEE1516eFederateHandle) federateHandle).getHandle() << 24) &
                  0xFF000000) | (((int) objectInstanceHandle) & 0xFFFFFF);
    }
    return hashCode;
  }

  @Override
  public boolean equals(Object rhs)
  {
    return this == rhs || (rhs instanceof IEEE1516eObjectInstanceHandle &&
                           equals((IEEE1516eObjectInstanceHandle) rhs));
  }

  @Override
  public String toString()
  {
    return new StringBuilder(federateHandle.toString()).append(".").append(objectInstanceHandle).toString();
  }

  private boolean equals(IEEE1516eObjectInstanceHandle rhs)
  {
    return objectInstanceHandle == rhs.objectInstanceHandle && federateHandle.equals(rhs.federateHandle);
  }
}
