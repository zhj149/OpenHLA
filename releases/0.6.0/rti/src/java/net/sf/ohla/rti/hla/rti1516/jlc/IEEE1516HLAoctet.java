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

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.HLAbyte;
import hla.rti1516.jlc.HLAoctet;

public class IEEE1516HLAoctet
  extends IEEE1516DataElement
  implements HLAoctet
{
  protected byte value;

  public IEEE1516HLAoctet()
  {
  }

  public IEEE1516HLAoctet(byte value)
  {
    this.value = value;
  }

  public int getOctetBoundary()
  {
    return 1;
  }

  public void encode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(getOctetBoundary());
    byteWrapper.put(value);
  }

  public int getEncodedLength()
  {
    return 1;
  }

  public final void decode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(getOctetBoundary());
    value = (byte) byteWrapper.get();
  }

  public byte getValue()
  {
    return value;
  }

  @Override
  public int hashCode()
  {
    return getValue();
  }

  @Override
  public boolean equals(Object rhs)
  {
    return rhs instanceof HLAbyte && getValue() == ((HLAbyte) rhs).getValue();
  }

  @Override
  public String toString()
  {
    return Byte.toString(getValue());
  }
}
