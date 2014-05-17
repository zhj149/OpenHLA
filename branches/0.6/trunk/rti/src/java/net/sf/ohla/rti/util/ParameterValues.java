/*
 * Copyright (c) 2005-2011, Michael Newcomb
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

package net.sf.ohla.rti.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eParameterHandleValueMap;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.ByteString;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;

public class ParameterValues
{
  public static ParameterHandleValueMap convert(Collection<MessageProtos.ParameterValue> parameterValueProtos)
  {
    ParameterHandleValueMap parameterValues =
      new IEEE1516eParameterHandleValueMap(parameterValueProtos.size());
    for (MessageProtos.ParameterValue parameterValueProto : parameterValueProtos)
    {
      parameterValues.put(ParameterHandles.convert(parameterValueProto.getParameterHandle()),
                          parameterValueProto.getValue().toByteArray());
    }
    return parameterValues;
  }

  public static Collection<MessageProtos.ParameterValue> convert(ParameterHandleValueMap parameterValues)
  {
    List<MessageProtos.ParameterValue> parameterValueProtos = new ArrayList<>(parameterValues.size());
    for (ParameterHandleValueMap.Entry<ParameterHandle, byte[]> entry : parameterValues.entrySet())
    {
      parameterValueProtos.add(
        MessageProtos.ParameterValue.newBuilder().setParameterHandle(
          ParameterHandles.convert(entry.getKey())).setValue(
          ByteString.copyFrom(entry.getValue())).build());
    }
    return parameterValueProtos;
  }
}
