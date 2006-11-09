package net.sf.ohla.rti1516.jlc;

import net.sf.ohla.rti1516.jlc.OHLADataElement;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.HLAinteger32BE;

public class OHLAHLAinteger32BE
  extends OHLADataElement
  implements HLAinteger32BE
{
  protected int value;

  public OHLAHLAinteger32BE()
  {
  }

  public OHLAHLAinteger32BE(int value)
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
