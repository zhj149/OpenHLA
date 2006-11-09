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

package net.sf.ohla.rti1516.jlc;

import net.sf.ohla.rti1516.jlc.OHLADataElement;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.HLAASCIIchar;

public class OHLAHLAASCIIchar
  extends OHLADataElement
  implements HLAASCIIchar
{
  protected final OHLAHLAoctet value;

  public OHLAHLAASCIIchar()
  {
    value = new OHLAHLAoctet();
  }

  public OHLAHLAASCIIchar(byte value)
  {
    this.value = new OHLAHLAoctet(value);
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

  public byte getValue()
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
    return rhs instanceof HLAASCIIchar &&
           getValue() == ((HLAASCIIchar) rhs).getValue();
  }

  @Override
  public String toString()
  {
    return Character.toString((char) getValue());
  }
}
