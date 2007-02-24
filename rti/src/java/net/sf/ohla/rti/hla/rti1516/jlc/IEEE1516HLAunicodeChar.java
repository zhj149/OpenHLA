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

package net.sf.ohla.rti.hla.rti1516.jlc;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.HLAunicodeChar;

public class IEEE1516HLAunicodeChar
  extends IEEE1516DataElement
  implements HLAunicodeChar
{
  protected final IEEE1516HLAinteger16BE value;

  public IEEE1516HLAunicodeChar()
  {
    value = new IEEE1516HLAinteger16BE();
  }

  public IEEE1516HLAunicodeChar(byte value)
  {
    this.value = new IEEE1516HLAinteger16BE(value);
  }

  public int getOctetBoundary()
  {
    return value.getOctetBoundary();
  }

  public void encode(ByteWrapper byteWrapper)
  {
    value.encode(byteWrapper);
  }

  public int getEncodedLength()
  {
    return value.getEncodedLength();
  }

  public void decode(ByteWrapper byteWrapper)
  {
    value.decode(byteWrapper);
  }

  public short getValue()
  {
    return value.getValue();
  }

  @Override
  public int hashCode()
  {
    return getValue();
  }

  @Override
  public boolean equals(Object rhs)
  {
    return rhs instanceof HLAunicodeChar &&
           getValue() == ((HLAunicodeChar) rhs).getValue();
  }

  @Override
  public String toString()
  {
    return Character.toString((char) getValue());
  }
}
