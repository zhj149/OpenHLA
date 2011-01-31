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

package net.sf.ohla.rti.hla.rti;

import java.util.Collection;
import java.util.Iterator;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandleSet;

import hla.rti.FederateHandleSet;
import hla.rti.HandleIterator;

import hla.rti1516e.FederateHandle;

public class HLA13FederateHandleSet
  extends IEEE1516eFederateHandleSet
  implements FederateHandleSet
{
  public HLA13FederateHandleSet()
  {
  }

  public HLA13FederateHandleSet(int initialCapacity)
  {
    super(initialCapacity);
  }

  public HLA13FederateHandleSet(Collection<? extends FederateHandle> c)
  {
    super(c);
  }

  public HLA13FederateHandleSet(hla.rti1516e.FederateHandleSet federateHandles)
  {
    super(federateHandles);
  }

  public void add(int handle)
  {
    add(new IEEE1516eFederateHandle(handle));
  }

  public void remove(int handle)
  {
    remove(new IEEE1516eFederateHandle(handle));
  }

  public boolean isMember(int handle)
  {
    return contains(new IEEE1516eFederateHandle(handle));
  }

  public HandleIterator handles()
  {
    return new FederateHandleIterator();
  }

  public void empty()
  {
    clear();
  }

  public class FederateHandleIterator
    implements HandleIterator
  {
    private Iterator<FederateHandle> i = iterator();

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
      return ((IEEE1516eFederateHandle) i.next()).getHandle();
    }
  }
}
