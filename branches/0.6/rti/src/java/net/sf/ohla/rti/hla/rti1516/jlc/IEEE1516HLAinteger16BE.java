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
import hla.rti1516.jlc.HLAinteger16BE;

public class IEEE1516HLAinteger16BE
  extends IEEE1516DataElement
  implements HLAinteger16BE
{
  protected short value;

  public IEEE1516HLAinteger16BE()
  {
  }

  public IEEE1516HLAinteger16BE(short value)
  {
    this.value = value;
  }

  public int getOctetBoundary()
  {
    return 2;
  }

  public void encode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(getOctetBoundary());
    byteWrapper.put((value >>> 8) & 0xFF);
    byteWrapper.put(value & 0xFF);
  }

  public int getEncodedLength()
  {
    return 2;
  }

  public void decode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(getOctetBoundary());
    short decoded = 0;
    decoded += (short) (byteWrapper.get() << 8);
    decoded += (short) byteWrapper.get();
    this.value = decoded;
  }

  public short getValue()
  {
    return value;
  }

  @Override
  public int hashCode()
  {
    return value;
  }

  @Override
  public boolean equals(Object rhs)
  {
    return rhs instanceof HLAinteger16BE &&
           value == ((HLAinteger16BE) rhs).getValue();
  }

  @Override
  public String toString()
  {
    return Short.toString(value);
  }
}
