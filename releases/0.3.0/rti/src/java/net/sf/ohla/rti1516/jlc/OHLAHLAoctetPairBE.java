package net.sf.ohla.rti1516.jlc;

import net.sf.ohla.rti1516.jlc.OHLADataElement;
import net.sf.ohla.rti1516.jlc.OHLAHLAinteger16BE;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.HLAoctetPairBE;

public class OHLAHLAoctetPairBE
  extends OHLADataElement
  implements HLAoctetPairBE
{
  protected final OHLAHLAinteger16BE value;

  public OHLAHLAoctetPairBE()
  {
    value = new OHLAHLAinteger16BE();
  }

  public OHLAHLAoctetPairBE(short value)
  {
    this.value = new OHLAHLAinteger16BE(value);
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
