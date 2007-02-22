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

package net.sf.ohla.rti1516.jlc.impl;

import java.io.UnsupportedEncodingException;

import net.sf.ohla.rti1516.jlc.impl.OHLADataElement;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.HLAASCIIstring;

public class OHLAHLAASCIIstring
  extends OHLADataElement
  implements HLAASCIIstring
{
  private static final String CHARSET_NAME = "ISO-8859-1";

  protected String value;

  public OHLAHLAASCIIstring()
  {
    this("");
  }

  public OHLAHLAASCIIstring(String s)
  {
    value = s != null ? s : "";
  }

  public void encode(ByteWrapper byteWrapper)
  {
    byte[] bytes;
    try
    {
      bytes = value.getBytes(CHARSET_NAME);
    }
    catch (UnsupportedEncodingException uee)
    {
      throw new AssertionError(uee);
    }
    byteWrapper.align(getOctetBoundary());
    byteWrapper.putInt(value.length());
    byteWrapper.put(bytes);
  }

  public void decode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(getOctetBoundary());
    int length = byteWrapper.getInt();
    byte[] bytes = new byte[length];
    byteWrapper.get(bytes);
    try
    {
      value = new String(bytes, CHARSET_NAME);
    }
    catch (UnsupportedEncodingException uee)
    {
      throw new AssertionError(uee);
    }
  }

  public int getEncodedLength()
  {
    return 4 + value.length();
  }

  public int getOctetBoundary()
  {
    return 4;
  }

  public String getValue()
  {
    return value;
  }

  @Override
  public int hashCode()
  {
    return value.hashCode();
  }

  @Override
  public boolean equals(Object rhs)
  {
    return rhs instanceof HLAASCIIstring &&
           value.equals(((HLAASCIIstring) rhs).getValue());
  }

  @Override
  public String toString()
  {
    return value;
  }
}
