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

import java.util.Collection;
import java.util.Iterator;

import net.sf.ohla.rti1516.impl.OHLAFederateHandle;

import hla.rti.FederateHandleSet;
import hla.rti.HandleIterator;

import hla.rti1516.FederateHandle;

public class OHLAFederateHandleSet
  extends net.sf.ohla.rti1516.impl.OHLAFederateHandleSet
  implements FederateHandleSet
{
  public OHLAFederateHandleSet()
  {
  }

  public OHLAFederateHandleSet(int initialCapacity)
  {
    super(initialCapacity);
  }

  public OHLAFederateHandleSet(Collection<? extends FederateHandle> c)
  {
    super(c);
  }

  public OHLAFederateHandleSet(hla.rti1516.FederateHandleSet federateHandles)
  {
    super(federateHandles);
  }

  public void add(int handle)
  {
    add(new OHLAFederateHandle(handle));
  }

  public void remove(int handle)
  {
    remove(new OHLAFederateHandle(handle));
  }

  public boolean isMember(int handle)
  {
    return contains(new OHLAFederateHandle(handle));
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
    protected Iterator<FederateHandle> i = iterator();

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
      return i.next().hashCode();
    }
  }
}
