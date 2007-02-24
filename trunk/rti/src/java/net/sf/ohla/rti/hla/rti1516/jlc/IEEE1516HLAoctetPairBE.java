package net.sf.ohla.rti.hla.rti1516.jlc;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.HLAoctetPairBE;

public class IEEE1516HLAoctetPairBE
  extends IEEE1516DataElement
  implements HLAoctetPairBE
{
  protected final IEEE1516HLAinteger16BE value;

  public IEEE1516HLAoctetPairBE()
  {
    value = new IEEE1516HLAinteger16BE();
  }

  public IEEE1516HLAoctetPairBE(short value)
  {
    this.value = new IEEE1516HLAinteger16BE(value);
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
    return rhs instanceof HLAoctetPairBE &&
           getValue() == ((HLAoctetPairBE) rhs).getValue();
  }

  @Override
  public String toString()
  {
    return Short.toString(getValue());
  }
}
