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

package net.sf.ohla.rti1516.fdd;

import java.io.Serializable;

import java.util.concurrent.atomic.AtomicInteger;

import net.sf.ohla.rti.hla.rti1516.OHLAParameterHandle;

import org.dom4j.Element;

import hla.rti1516.ParameterHandle;

public class Parameter
  implements Serializable
{
  protected ParameterHandle parameterHandle;

  protected String name;

  protected boolean mom;

  public Parameter(String name, AtomicInteger parameterCount)
  {
    this.name = name;

    parameterHandle = new OHLAParameterHandle(parameterCount.incrementAndGet());
  }

  public Parameter(Element parameter, AtomicInteger parameterCount)
  {
    this(((org.dom4j.Attribute) parameter.selectSingleNode("@name")).getValue(),
         parameterCount);
  }

  public ParameterHandle getParameterHandle()
  {
    return parameterHandle;
  }

  public String getName()
  {
    return name;
  }

  public boolean isMOM()
  {
    return mom;
  }

  @Override
  public boolean equals(Object rhs)
  {
    return rhs instanceof Parameter &&
           parameterHandle.equals(((Parameter) rhs).parameterHandle);
  }

  @Override
  public int hashCode()
  {
    return parameterHandle.hashCode();
  }

  @Override
  public String toString()
  {
    return name;
  }
}
