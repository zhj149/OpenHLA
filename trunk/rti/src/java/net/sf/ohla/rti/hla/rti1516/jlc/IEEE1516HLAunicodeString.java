package net.sf.ohla.rti.hla.rti1516.jlc;

import java.io.UnsupportedEncodingException;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.HLAunicodeString;

public class IEEE1516HLAunicodeString
  extends IEEE1516DataElement
  implements HLAunicodeString
{
  private static final String CHARSET_NAME = "UTF-16";

  protected String value;

  public IEEE1516HLAunicodeString()
  {
    this("");
  }

  public IEEE1516HLAunicodeString(String s)
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
    return rhs instanceof HLAunicodeString &&
           value.equals(((HLAunicodeString) rhs).getValue());
  }

  @Override
  public String toString()
  {
    return value;
  }
}
