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
import hla.rti1516.jlc.HLAinteger32LE;

public class IEEE1516HLAinteger32LE
  extends IEEE1516DataElement
  implements HLAinteger32LE
{
  protected int value;

  public IEEE1516HLAinteger32LE()
  {
  }

  public IEEE1516HLAinteger32LE(int value)
  {
    this.value = value;
  }

  public int getOctetBoundary()
  {
    return 4;
  }

  public void encode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(getOctetBoundary());
    final int encoded = value;
    byteWrapper.put(encoded & 0xFF);
    byteWrapper.put((encoded >>> 8) & 0xFF);
    byteWrapper.put((encoded >>> 16) & 0xFF);
    byteWrapper.put((encoded >>> 24) & 0xFF);
  }

  public int getEncodedLength()
  {
    return 4;
  }

  public void decode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(getOctetBoundary());
    int decoded = 0;
    decoded += (short) byteWrapper.get();
    decoded += (short) byteWrapper.get() << 8;
    decoded += (short) byteWrapper.get() << 16;
    decoded += (short) byteWrapper.get() << 24;
    value = decoded;
  }

  public int getValue()
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
    return rhs instanceof HLAinteger32LE &&
           value == ((HLAinteger32LE) rhs).getValue();
  }

  @Override
  public String toString()
  {
    return Integer.toString(value);
  }
}
