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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.ohla.rti.Protocol;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.encoding.ByteWrapper;

public class IEEE1516eParameterHandleValueMap
  extends HashMap<ParameterHandle, byte[]>
  implements ParameterHandleValueMap
{
  public IEEE1516eParameterHandleValueMap()
  {
  }

  public IEEE1516eParameterHandleValueMap(int initialCapacity)
  {
    super(initialCapacity);
  }

  public IEEE1516eParameterHandleValueMap(Map<ParameterHandle, byte[]> clonee)
  {
    super(clonee);
  }

  public ByteWrapper getValueReference(ParameterHandle parameterHandle)
  {
    byte[] buffer = get(parameterHandle);
    return buffer == null ? null : new ByteWrapper(buffer);
  }

  public ByteWrapper getValueReference(ParameterHandle parameterHandle, ByteWrapper byteWrapper)
  {
    byte[] buffer = get(parameterHandle);
    if (buffer == null)
    {
      byteWrapper = null;
    }
    else
    {
      byteWrapper.put(buffer);
    }
    return byteWrapper;
  }

  @Override
  public boolean equals(Object rhs)
  {
    return this == rhs || (rhs instanceof IEEE1516eParameterHandleValueMap &&
                           equals((IEEE1516eParameterHandleValueMap) rhs));
  }

  private boolean equals(IEEE1516eParameterHandleValueMap rhs)
  {
    boolean equals = size() == rhs.size();
    if (equals)
    {
      for (Iterator<Map.Entry<ParameterHandle, byte[]>> i = entrySet().iterator(); i.hasNext() && equals;)
      {
        Map.Entry<ParameterHandle, byte[]> entry = i.next();
        equals = Arrays.equals(entry.getValue(), rhs.get(entry.getKey()));
      }
    }
    return equals;
  }

  public static void encode(ChannelBuffer buffer, ParameterHandleValueMap parameterHandles)
  {
    if (parameterHandles == null)
    {
      Protocol.encodeVarInt(buffer, 0);
    }
    else
    {
      Protocol.encodeVarInt(buffer, parameterHandles.size());

      for (Map.Entry<ParameterHandle, byte[]> entry : parameterHandles.entrySet())
      {
        IEEE1516eParameterHandle.encode(buffer, entry.getKey());
        Protocol.encodeBytes(buffer, entry.getValue());
      }
    }
  }

  public static IEEE1516eParameterHandleValueMap decode(ChannelBuffer buffer)
  {
    IEEE1516eParameterHandleValueMap parameterHandles;

    int size = Protocol.decodeVarInt(buffer);
    if (size == 0)
    {
      parameterHandles = null;
    }
    else
    {
      parameterHandles = new IEEE1516eParameterHandleValueMap(size);

      for (; size > 0; size--)
      {
        parameterHandles.put(IEEE1516eParameterHandle.decode(buffer), Protocol.decodeBytes(buffer));
      }
    }

    return parameterHandles;
  }
}
