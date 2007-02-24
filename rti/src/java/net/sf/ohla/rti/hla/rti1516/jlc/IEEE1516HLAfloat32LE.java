package net.sf.ohla.rti.hla.rti1516.jlc;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.HLAfloat32LE;

public class IEEE1516HLAfloat32LE
  extends IEEE1516DataElement
  implements HLAfloat32LE
{
  protected float value;

  public IEEE1516HLAfloat32LE()
  {
  }

  public IEEE1516HLAfloat32LE(float value)
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
    final int intBits = Float.floatToIntBits(value);
    byteWrapper.put(intBits & 0xFF);
    byteWrapper.put((intBits >>> 8) & 0xFF);
    byteWrapper.put((intBits >>> 16) & 0xFF);
    byteWrapper.put((intBits >>> 24) & 0xFF);
  }

  public int getEncodedLength()
  {
    return 4;
  }

  public void decode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(getOctetBoundary());
    int intBits = 0;
    intBits += (short) byteWrapper.get();
    intBits += (short) byteWrapper.get() << 8;
    intBits += (short) byteWrapper.get() << 16;
    intBits += (short) byteWrapper.get() << 24;
    value = Float.intBitsToFloat(intBits);
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
    return rhs instanceof HLAfloat32LE &&
           getValue() == ((HLAfloat32LE) rhs).getValue();
  }

  @Override
  public String toString()
  {
    return Float.toString(value);
  }
}
