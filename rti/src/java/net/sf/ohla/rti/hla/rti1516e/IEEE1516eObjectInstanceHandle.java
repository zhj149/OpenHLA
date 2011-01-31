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

import net.sf.ohla.rti.Protocol;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.FederateHandle;
import hla.rti1516e.ObjectInstanceHandle;

public class IEEE1516eObjectInstanceHandle
  implements ObjectInstanceHandle
{
  private final FederateHandle federateHandle;

  private final int objectInstanceHandle;

  public IEEE1516eObjectInstanceHandle(FederateHandle federateHandle, int objectInstanceHandle)
  {
    this.federateHandle = federateHandle;
    this.objectInstanceHandle = objectInstanceHandle;
  }

  public int encodedLength()
  {
    return federateHandle.encodedLength() + Protocol.encodedVarIntSize(objectInstanceHandle);
  }

  public void encode(byte[] buffer, int offset)
  {
    Protocol.encodeVarInt(buffer, offset, objectInstanceHandle);
    federateHandle.encode(buffer, offset + Protocol.encodedVarIntSize(objectInstanceHandle));
  }

  @Override
  public int hashCode()
  {
    return objectInstanceHandle & (((IEEE1516eFederateHandle) federateHandle).getHandle() << 24);
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

  public static void encode(ChannelBuffer buffer, ObjectInstanceHandle objectInstanceHandle)
  {
    if (objectInstanceHandle == null)
    {
      // encode 0 if the handle was null
      //
      Protocol.encodeVarInt(buffer, 0);
    }
    else
    {
      // encode the handle first, saves space when the ObjectInstanceHandle is null
      //
      Protocol.encodeVarInt(buffer, ((IEEE1516eObjectInstanceHandle) objectInstanceHandle).objectInstanceHandle);

      IEEE1516eFederateHandle.encode(
        buffer, ((IEEE1516eObjectInstanceHandle) objectInstanceHandle).federateHandle);
    }
  }

  public static IEEE1516eObjectInstanceHandle decode(ChannelBuffer buffer)
  {
    // decode the handle first
    //
    int objectInstanceHandle = Protocol.decodeVarInt(buffer);

    // a 0 indicates the ObjectInstanceHandle was null
    //
    return objectInstanceHandle == 0 ? null :
      new IEEE1516eObjectInstanceHandle(IEEE1516eFederateHandle.decode(buffer), objectInstanceHandle);
  }

  public static IEEE1516eObjectInstanceHandle decode(byte[] buffer, int offset)
  {
    // decode the handle first
    //
    int objectInstanceHandle = Protocol.decodeVarInt(buffer, offset);

    // a 0 indicates the ObjectInstanceHandle was null
    //
    return objectInstanceHandle == 0 ?
      null : new IEEE1516eObjectInstanceHandle(IEEE1516eFederateHandle.decode(buffer, offset), objectInstanceHandle);
  }
}
