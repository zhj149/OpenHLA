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

import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleSetFactory;

public class IEEE1516eAttributeHandleSetFactory
  implements AttributeHandleSetFactory
{
  public static final IEEE1516eAttributeHandleSetFactory INSTANCE = new IEEE1516eAttributeHandleSetFactory();

  private IEEE1516eAttributeHandleSetFactory()
  {
  }

  public AttributeHandleSet create()
  {
    return new IEEE1516eAttributeHandleSet();
  }

  private Object readResolve()
  {
    return INSTANCE;
  }
}
