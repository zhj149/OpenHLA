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

import hla.rti1516.ParameterHandle;

public class IEEE1516ParameterHandle
  implements ParameterHandle
{
  public final hla.rti1516e.ParameterHandle parameterHandle;

  public IEEE1516ParameterHandle(hla.rti1516e.ParameterHandle parameterHandle)
  {
    this.parameterHandle = parameterHandle;
  }

  public hla.rti1516e.ParameterHandle getParameterHandle()
  {
    return parameterHandle;
  }

  public int encodedLength()
  {
    return parameterHandle.encodedLength();
  }

  public void encode(byte[] buffer, int offset)
  {
    parameterHandle.encode(buffer, offset);
  }

  @Override
  public boolean equals(Object rhs)
  {
    return this == rhs ||
           (rhs instanceof IEEE1516ParameterHandle &&
            parameterHandle.equals(((IEEE1516ParameterHandle) rhs).parameterHandle));
  }

  @Override
  public int hashCode()
  {
    return parameterHandle.hashCode();
  }

  @Override
  public String toString()
  {
    return parameterHandle.toString();
  }
}
