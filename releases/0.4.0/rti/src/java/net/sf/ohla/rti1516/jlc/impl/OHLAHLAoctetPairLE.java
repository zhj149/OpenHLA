package net.sf.ohla.rti1516.jlc.impl;

import net.sf.ohla.rti1516.jlc.impl.OHLADataElement;
import net.sf.ohla.rti1516.jlc.impl.OHLAHLAinteger16LE;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.HLAoctetPairLE;

public class OHLAHLAoctetPairLE
  extends OHLADataElement
  implements HLAoctetPairLE
{
  protected final OHLAHLAinteger16LE value;

  public OHLAHLAoctetPairLE()
  {
    value = new OHLAHLAinteger16LE();
  }

  public OHLAHLAoctetPairLE(short value)
  {
    this.value = new OHLAHLAinteger16LE(value);
  }

  public int getOctetBoundary()
  {
    return value.getOctetBoundary();
  }

  public void encode(ByteWrapper byteWrapper)
  {
    value.encode(byteWrapper);
  }

  public int getEncodedLength()
  {
    return value.getEncodedLength();
  }

  public void decode(ByteWrapper byteWrapper)
  {
    value.decode(byteWrapper);
  }

  public short getValue()
  {
    return value.getValue();
  }

  @Override
  public int hashCode()
  {
    return getValue();
  }

  @Override
  public boolean equals(Object rhs)
  {
    return rhs instanceof HLAoctetPairLE &&
           getValue() == ((HLAoctetPairLE) rhs).getValue();
  }

  @Override
  public String toString()
  {
    return Short.toString(getValue());
  }
}
