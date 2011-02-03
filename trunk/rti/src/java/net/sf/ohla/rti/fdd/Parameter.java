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

package net.sf.ohla.rti.fdd;

import net.sf.ohla.rti.Protocol;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eParameterHandle;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.ParameterHandle;

public class Parameter
{
  private final ParameterHandle parameterHandle;
  private final String parameterName;

  public Parameter(ParameterHandle parameterHandle, String parameterName)
  {
    this.parameterHandle = parameterHandle;
    this.parameterName = parameterName;
  }

  public Parameter(ChannelBuffer buffer)
  {
    parameterHandle = IEEE1516eParameterHandle.decode(buffer);
    parameterName = Protocol.decodeString(buffer);
  }

  public ParameterHandle getParameterHandle()
  {
    return parameterHandle;
  }

  public String getParameterName()
  {
    return parameterName;
  }

  @Override
  public int hashCode()
  {
    return parameterHandle.hashCode();
  }

  @Override
  public String toString()
  {
    return parameterName;
  }

  public static void encode(ChannelBuffer buffer, Parameter parameter)
  {
    IEEE1516eParameterHandle.encode(buffer, parameter.parameterHandle);
    Protocol.encodeString(buffer, parameter.parameterName);
  }

  public static Parameter decode(ChannelBuffer buffer)
  {
    return new Parameter(buffer);
  }
}
