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

package net.sf.ohla.rti.hla.rti1516;

import java.nio.ByteBuffer;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandle;

import hla.rti1516.DimensionHandle;

public class IEEE1516DimensionHandle
  implements DimensionHandle
{
  public final hla.rti1516e.DimensionHandle dimensionHandle;

  public IEEE1516DimensionHandle(hla.rti1516e.DimensionHandle dimensionHandle)
  {
    this.dimensionHandle = dimensionHandle;
  }

  public hla.rti1516e.DimensionHandle getDimensionHandle()
  {
    return dimensionHandle;
  }

  public int encodedLength()
  {
    return dimensionHandle.encodedLength();
  }

  public void encode(byte[] buffer, int offset)
  {
    dimensionHandle.encode(buffer, offset);
  }

  @Override
  public boolean equals(Object rhs)
  {
    return this == rhs ||
           (rhs instanceof IEEE1516DimensionHandle &&
            dimensionHandle.equals(((IEEE1516DimensionHandle) rhs).dimensionHandle));
  }

  @Override
  public int hashCode()
  {
    return dimensionHandle.hashCode();
  }

  @Override
  public String toString()
  {
    return dimensionHandle.toString();
  }
}
