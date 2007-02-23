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

package net.sf.ohla.rti.hla.rti1516.jlc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516DataElement;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.DataElement;
import hla.rti1516.jlc.HLAfixedRecord;

public class IEEE1516HLAfixedRecord
  extends IEEE1516DataElement
  implements HLAfixedRecord
{
  protected final List<DataElement> dataElements = new ArrayList<DataElement>();

  public IEEE1516HLAfixedRecord()
  {
  }

  public IEEE1516HLAfixedRecord(DataElement dataElement)
  {
    add(dataElement);
  }

  public IEEE1516HLAfixedRecord(DataElement dataElement1, DataElement dataElement2)
  {
    add(dataElement1);
    add(dataElement2);
  }

  public void add(DataElement dataElement)
  {
    dataElements.add(dataElement);
  }

  public int size()
  {
    return dataElements.size();
  }

  public DataElement get(int index)
  {
    return dataElements.get(index);
  }

  public Iterator<DataElement> iterator()
  {
    return dataElements.iterator();
  }

  public int getOctetBoundary()
  {
    int boundary = 4;
    for (DataElement dataElement : dataElements)
    {
      boundary = Math.max(boundary, dataElement.getOctetBoundary());
    }
    return boundary;
  }

  public void encode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(getOctetBoundary());
    for (DataElement dataElement : dataElements)
    {
      dataElement.encode(byteWrapper);
    }
  }

  public int getEncodedLength()
  {
    int size = 0;
    for (DataElement dataElement : dataElements)
    {
      while (size % dataElement.getOctetBoundary() != 0)
      {
        size += 1;
      }
      size += dataElement.getEncodedLength();
    }
    return size;
  }

  public void decode(ByteWrapper byteWrapper)
  {
    for (DataElement dataElement : dataElements)
    {
      dataElement.decode(byteWrapper);
    }
  }

  @Override
  public int hashCode()
  {
    return dataElements.hashCode();
  }

  @Override
  public boolean equals(Object rhs)
  {
    boolean equals = rhs instanceof HLAfixedRecord;
    if (equals)
    {
      for (Iterator<DataElement> i = ((HLAfixedRecord) rhs).iterator(),
        j = iterator();
           i.hasNext() && j.hasNext() && equals;)
      {
        equals = i.next().equals(j.next());
      }
    }
    return equals;
  }

  @Override
  public String toString()
  {
    return dataElements.toString();
  }
}
