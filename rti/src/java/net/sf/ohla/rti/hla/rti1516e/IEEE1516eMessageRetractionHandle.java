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

  public int encodedLength()
  {
    return federateHandle.encodedLength() + Protocol.encodedVarLongSize(messageRetractionHandle);
  }

  public void encode(byte[] buffer, int offset)
  {
    federateHandle.encode(buffer, offset);
    Protocol.encodeVarLong(buffer, offset + federateHandle.encodedLength(), messageRetractionHandle);
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

  public static void encode(ChannelBuffer buffer, MessageRetractionHandle messageRetractionHandle)
  {
    if (messageRetractionHandle == null)
    {
      // encode 0 if the handle was null
      //
      Protocol.encodeVarLong(buffer, 0L);
    }
    else
    {
      // encode the handle first, saves space when the MessageRetractionHandle is null
      //
      Protocol.encodeVarLong(buffer, ((IEEE1516eMessageRetractionHandle) messageRetractionHandle).messageRetractionHandle);

      IEEE1516eFederateHandle.encode(
        buffer, ((IEEE1516eMessageRetractionHandle) messageRetractionHandle).federateHandle);
    }
  }

  public static IEEE1516eMessageRetractionHandle decode(ChannelBuffer buffer)
  {
    // decode the handle first
    //
    int handle = Protocol.decodeVarInt(buffer);

    // a 0 indicates the MessageRetractionHandle was null
    //
    return handle == 0 ? null : new IEEE1516eMessageRetractionHandle(IEEE1516eFederateHandle.decode(buffer), handle);
  }
}
