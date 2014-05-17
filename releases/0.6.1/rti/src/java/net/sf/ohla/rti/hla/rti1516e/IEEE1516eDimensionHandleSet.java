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

import java.lang.reflect.Array;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sf.ohla.rti.util.EmptyIterator;

import hla.rti1516e.DimensionHandle;
import hla.rti1516e.DimensionHandleSet;

public class IEEE1516eDimensionHandleSet
  extends HashSet<DimensionHandle>
  implements DimensionHandleSet
{
  public static final DimensionHandleSet EMPTY = new EmptyDimensionHandleSet();

  public IEEE1516eDimensionHandleSet()
  {
  }

  public IEEE1516eDimensionHandleSet(int initialCapacity)
  {
    super(initialCapacity);
  }

  public IEEE1516eDimensionHandleSet(Set<DimensionHandle> dimensionHandles)
  {
    super(dimensionHandles);
  }

  public IEEE1516eDimensionHandleSet(IEEE1516eDimensionHandleSet dimensionHandles)
  {
    super(dimensionHandles);
  }

  @Override
  public IEEE1516eDimensionHandleSet clone()
  {
    return new IEEE1516eDimensionHandleSet(this);
  }

  public static class EmptyDimensionHandleSet
    implements DimensionHandleSet
  {
    @Override
    public int size()
    {
      return 0;
    }

    @Override
    public boolean isEmpty()
    {
      return true;
    }

    @Override
    public boolean contains(Object o)
    {
      return false;
    }

    @Override
    public Iterator<DimensionHandle> iterator()
    {
      return EmptyIterator.instance();
    }

    @Override
    public Object[] toArray()
    {
      return new Object[0];
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a)
    {
      return (T[]) Array.newInstance(a.getClass().getComponentType(), 0);
    }

    @Override
    public boolean add(DimensionHandle dimensionHandle)
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o)
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
      return false;
    }

    @Override
    public boolean addAll(Collection<? extends DimensionHandle> c)
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public void clear()
    {
      throw new UnsupportedOperationException();
    }
  }
}
