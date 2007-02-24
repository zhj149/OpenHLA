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

package net.sf.ohla.rti.hla.rti1516.jlc;

import java.util.Iterator;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.HLAlogicalTime;

public class IEEE1516HLAlogicalTime
  extends IEEE1516DataElement
  implements HLAlogicalTime
{
  protected final IEEE1516HLAopaqueData value;

  public IEEE1516HLAlogicalTime()
  {
    value = new IEEE1516HLAopaqueData();
  }

  public IEEE1516HLAlogicalTime(byte[] bytes)
  {
    value = new IEEE1516HLAopaqueData(bytes);
  }

  public int size()
  {
    return value.size();
  }

  public byte get(int index)
  {
    return value.get(index);
  }

  public Iterator<Byte> iterator()
  {
    return value.iterator();
  }

  public void encode(ByteWrapper byteWrapper)
  {
    value.encode(byteWrapper);
  }

  public void decode(ByteWrapper byteWrapper)
  {
    value.decode(byteWrapper);
  }

  public int getEncodedLength()
  {
    return value.getEncodedLength();
  }

  public int getOctetBoundary()
  {
    return value.getOctetBoundary();
  }

  public byte[] getValue()
  {
    return value.getValue();
  }

  @Override
  public int hashCode()
  {
    return value.hashCode();
  }

  @Override
  public boolean equals(Object rhs)
  {
    return value.equals(rhs);
  }

  @Override
  public String toString()
  {
    return value.toString();
  }
}
