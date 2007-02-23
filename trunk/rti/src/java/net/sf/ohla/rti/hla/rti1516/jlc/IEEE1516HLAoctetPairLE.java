package net.sf.ohla.rti.hla.rti1516.jlc;

import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516DataElement;
import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516HLAinteger16LE;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.HLAoctetPairLE;

public class IEEE1516HLAoctetPairLE
  extends IEEE1516DataElement
  implements HLAoctetPairLE
{
  protected final IEEE1516HLAinteger16LE value;

  public IEEE1516HLAoctetPairLE()
  {
    value = new IEEE1516HLAinteger16LE();
  }

  public IEEE1516HLAoctetPairLE(short value)
  {
    this.value = new IEEE1516HLAinteger16LE(value);
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
