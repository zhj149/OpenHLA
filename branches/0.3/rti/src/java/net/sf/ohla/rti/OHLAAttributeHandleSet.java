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

package net.sf.ohla.rti;

import java.util.Iterator;
import java.util.Set;

import net.sf.ohla.rti1516.OHLAAttributeHandle;

import hla.rti.AttributeHandleSet;
import hla.rti.HandleIterator;

import hla.rti1516.AttributeHandle;

public class OHLAAttributeHandleSet
  extends net.sf.ohla.rti1516.OHLAAttributeHandleSet
  implements AttributeHandleSet
{
  public OHLAAttributeHandleSet()
  {
  }

  public OHLAAttributeHandleSet(int initialCapacity)
  {
    super(initialCapacity);
  }

  public OHLAAttributeHandleSet(Set<AttributeHandle> attributeHandles)
  {
    super(attributeHandles);
  }

  public OHLAAttributeHandleSet(hla.rti1516.AttributeHandleSet attributeHandles)
  {
    super(attributeHandles);
  }

  public void add(int handle)
  {
    add(new OHLAAttributeHandle(handle));
  }

  public void remove(int handle)
  {
    remove(new OHLAAttributeHandle(handle));
  }

  public boolean isMember(int handle)
  {
    return contains(new OHLAAttributeHandle(handle));
  }

  public HandleIterator handles()
  {
    return new AttributeHandleIterator();
  }

  public void empty()
  {
    clear();
  }

  public class AttributeHandleIterator
    implements HandleIterator
  {
    protected Iterator i = iterator();

    public int first()
    {
      i = iterator();
      return next();
    }

    public boolean isValid()
    {
      return i.hasNext();
    }

    public int next()
    {
      return ((OHLAAttributeHandle) i.next()).getHandle();
    }
  }
}
