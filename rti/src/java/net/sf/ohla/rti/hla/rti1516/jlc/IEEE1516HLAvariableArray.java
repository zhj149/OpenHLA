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

package net.sf.ohla.rti.hla.rti1516.jlc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.DataElement;
import hla.rti1516.jlc.DataElementFactory;
import hla.rti1516.jlc.HLAvariableArray;

public class IEEE1516HLAvariableArray
  extends IEEE1516DataElement
  implements HLAvariableArray
{
  protected final List<DataElement> dataElements = new ArrayList<DataElement>();

  protected final DataElementFactory dataElementFactory;

  public IEEE1516HLAvariableArray()
  {
    dataElementFactory = null;
  }

  public IEEE1516HLAvariableArray(DataElement[] momElements)
  {
    this();

    dataElements.addAll(Arrays.asList(momElements));
  }

  public IEEE1516HLAvariableArray(DataElementFactory elementFactory)
  {
    dataElementFactory = elementFactory;
  }

  public void addElement(DataElement dataElement)
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

  public void encode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(4);
    byteWrapper.putInt(dataElements.size());
    for (DataElement dataElement : dataElements)
    {
      dataElement.encode(byteWrapper);
    }
  }

  public void decode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(4);
    int decoded = byteWrapper.getInt();
    if (dataElements.size() != 0)
    {
      for (DataElement dataElement : dataElements)
      {
        dataElement.decode(byteWrapper);
      }
    }
    else
    {
      for (int i = 0; i < decoded; i++)
      {
        dataElements.add(dataElementFactory.createElement(i));
      }
    }
  }

  public int getEncodedLength()
  {
    int size = 4;
    for (DataElement dataElement : dataElements)
    {
      while ((size % dataElement.getOctetBoundary()) != 0)
      {
        size += 1;
      }
      size += dataElement.getEncodedLength();
    }
    return size;
  }

  public int getOctetBoundary()
  {
    int boundary = 4; // Minimum is 4 for the count-element
    for (DataElement dataElement : dataElements)
    {
      boundary = Math.max(boundary, dataElement.getOctetBoundary());
    }
    return boundary;
  }

  @Override
  public int hashCode()
  {
    return dataElements.hashCode();
  }

  @Override
  public boolean equals(Object rhs)
  {
    boolean equals = rhs instanceof HLAvariableArray;
    if (equals)
    {
      for (Iterator<DataElement> i = ((HLAvariableArray) rhs).iterator(),
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
