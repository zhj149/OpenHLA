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
import hla.rti1516.jlc.HLAboolean;

public class IEEE1516HLAboolean
  extends IEEE1516DataElement
  implements HLAboolean
{
  protected final IEEE1516HLAinteger32BE value;

  public IEEE1516HLAboolean()
  {
    value = new IEEE1516HLAinteger32BE(0);
  }

  public IEEE1516HLAboolean(boolean value)
  {
    this.value = new IEEE1516HLAinteger32BE(value ? 1 : 0);
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

  public final void decode(ByteWrapper byteWrapper)
  {
    value.decode(byteWrapper);
  }

  public boolean getValue()
  {
    return value.getValue() != 0;
  }

  @Override
  public int hashCode()
  {
    return value.hashCode();
  }

  @Override
  public boolean equals(Object rhs)
  {
    return rhs instanceof HLAboolean &&
           getValue() == ((HLAboolean) rhs).getValue();
  }

  @Override
  public String toString()
  {
    return getValue() ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
  }
}
