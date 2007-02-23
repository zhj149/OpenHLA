package net.sf.ohla.rti.hla.rti1516.jlc;

import net.sf.ohla.rti.hla.rti1516.jlc.OHLADataElement;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.HLAoctet;
import hla.rti1516.jlc.HLAbyte;

public class OHLAHLAoctet
  extends OHLADataElement
  implements HLAoctet
{
  protected byte value;

  public OHLAHLAoctet()
  {
  }

  public OHLAHLAoctet(byte value)
  {
    this.value = value;
  }

  public int getOctetBoundary()
  {
    return 1;
  }

  public void encode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(getOctetBoundary());
    byteWrapper.put(value);
  }

  public int getEncodedLength()
  {
    return 1;
  }

  public final void decode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(getOctetBoundary());
    value = (byte) byteWrapper.get();
  }

  public byte getValue()
  {
    return value;
  }

  @Override
  public int hashCode()
  {
    return getValue();
  }

  @Override
  public boolean equals(Object rhs)
  {
    return rhs instanceof HLAbyte && getValue() == ((HLAbyte) rhs).getValue();
  }

  @Override
  public String toString()
  {
    return Byte.toString(getValue());
  }
}
