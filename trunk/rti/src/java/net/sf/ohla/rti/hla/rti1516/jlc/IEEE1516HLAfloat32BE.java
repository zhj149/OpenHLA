package net.sf.ohla.rti.hla.rti1516.jlc;

import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516DataElement;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.HLAfloat32BE;

public class IEEE1516HLAfloat32BE
  extends IEEE1516DataElement
  implements HLAfloat32BE
{
  protected float value;

  public IEEE1516HLAfloat32BE()
  {
  }

  public IEEE1516HLAfloat32BE(float value)
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
    byteWrapper.putInt(Float.floatToIntBits(value));
  }

  public int getEncodedLength()
  {
    return 4;
  }

  public void decode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(getOctetBoundary());
    value = Float.intBitsToFloat(byteWrapper.getInt());
  }

  public float getValue()
  {
    return value;
  }

  @Override
  public int hashCode()
  {
    return (int) value;
  }

  @Override
  public boolean equals(Object rhs)
  {
    return rhs instanceof HLAfloat32BE &&
           getValue() == ((HLAfloat32BE) rhs).getValue();
  }

  @Override
  public String toString()
  {
    return Float.toString(value);
  }
}