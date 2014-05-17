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

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleValueMap;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.ByteString;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleValueMap;

public class AttributeValues
{
  public static AttributeHandleValueMap convert(Collection<MessageProtos.AttributeValue> attributeValueProtos)
  {
    AttributeHandleValueMap attributeValues =
      new IEEE1516eAttributeHandleValueMap(attributeValueProtos.size());
    for (MessageProtos.AttributeValue attributeValueProto : attributeValueProtos)
    {
      attributeValues.put(AttributeHandles.convert(attributeValueProto.getAttributeHandle()),
                          attributeValueProto.getValue().toByteArray());
    }
    return attributeValues;
  }

  public static Collection<MessageProtos.AttributeValue> convert(AttributeHandleValueMap attributeValues)
  {
    List<MessageProtos.AttributeValue> attributeValueProtos = new ArrayList<>(attributeValues.size());
    for (AttributeHandleValueMap.Entry<AttributeHandle, byte[]> entry : attributeValues.entrySet())
    {
      attributeValueProtos.add(
        MessageProtos.AttributeValue.newBuilder().setAttributeHandle(
          AttributeHandles.convert(entry.getKey())).setValue(
          ByteString.copyFrom(entry.getValue())).build());
    }
    return attributeValueProtos;
  }
}
