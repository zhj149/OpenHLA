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

import hla.rti1516.ParameterHandleValueMap;
import hla.rti1516.ParameterHandleValueMapFactory;

public class IEEE1516ParameterHandleValueMapFactory
  implements ParameterHandleValueMapFactory
{
  public static final IEEE1516ParameterHandleValueMapFactory INSTANCE = new IEEE1516ParameterHandleValueMapFactory();

  private IEEE1516ParameterHandleValueMapFactory()
  {
  }

  public ParameterHandleValueMap create(int capacity)
  {
    return new IEEE1516ParameterHandleValueMap(capacity);
  }

  private Object readResolve()
  {
    return INSTANCE;
  }
}
