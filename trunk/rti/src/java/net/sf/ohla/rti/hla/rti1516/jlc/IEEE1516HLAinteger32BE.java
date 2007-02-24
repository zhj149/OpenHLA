package net.sf.ohla.rti.hla.rti1516.jlc;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.HLAinteger32BE;

public class IEEE1516HLAinteger32BE
  extends IEEE1516DataElement
  implements HLAinteger32BE
{
  protected int value;

  public IEEE1516HLAinteger32BE()
  {
  }

  public IEEE1516HLAinteger32BE(int value)
  {
    this.value = value;
  }

  public int getOctetBoundary()
  {
    return 4;
  }

  public void encode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(getOctetBoundary());
    byteWrapper.putInt(value);
  }

  public int getEncodedLength()
  {
    return 4;
  }

  public void decode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(getOctetBoundary());
    value = byteWrapper.getInt();
  }

  public int getValue()
  {
    return value;
  }

  @Override
  public int hashCode()
  {
    return value;
  }

  @Override
  public boolean equals(Object rhs)
  {
    return rhs instanceof HLAinteger32BE &&
           value == ((HLAinteger32BE) rhs).getValue();
  }

  @Override
  public String toString()
  {
    return Integer.toString(value);
  }
}
