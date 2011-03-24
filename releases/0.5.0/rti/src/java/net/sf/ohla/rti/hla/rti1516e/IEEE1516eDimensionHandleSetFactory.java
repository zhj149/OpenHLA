/*
 * Copyright (c) 2005-2010, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.hla.rti1516e;

import java.util.Set;

import hla.rti1516e.DimensionHandle;
import hla.rti1516e.DimensionHandleSet;
import hla.rti1516e.DimensionHandleSetFactory;

public class IEEE1516eDimensionHandleSetFactory
  implements DimensionHandleSetFactory
{
  public static final IEEE1516eDimensionHandleSetFactory INSTANCE = new IEEE1516eDimensionHandleSetFactory();

  private IEEE1516eDimensionHandleSetFactory()
  {
  }

  public DimensionHandleSet create()
  {
    return new IEEE1516eDimensionHandleSet();
  }

  public DimensionHandleSet create(Set<DimensionHandle> dimensionHandles)
  {
    return new IEEE1516eDimensionHandleSet(dimensionHandles);
  }

  private Object readResolve()
  {
    return INSTANCE;
  }
}
