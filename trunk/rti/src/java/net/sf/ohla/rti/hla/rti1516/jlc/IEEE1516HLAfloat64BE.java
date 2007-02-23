package net.sf.ohla.rti.hla.rti1516.jlc;

import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516DataElement;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.HLAfloat64BE;

public class IEEE1516HLAfloat64BE
  extends IEEE1516DataElement
  implements HLAfloat64BE
{
  protected double value;

  public IEEE1516HLAfloat64BE()
  {
  }

  public IEEE1516HLAfloat64BE(double value)
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
    final long longBits = Double.doubleToLongBits(value);
    byteWrapper.put((int) (longBits >>> 56) & 0xFF);
    byteWrapper.put((int) (longBits >>> 48) & 0xFF);
    byteWrapper.put((int) (longBits >>> 40) & 0xFF);
    byteWrapper.put((int) (longBits >>> 32) & 0xFF);
    byteWrapper.put((int) (longBits >>> 24) & 0xFF);
    byteWrapper.put((int) (longBits >>> 16) & 0xFF);
    byteWrapper.put((int) (longBits >>> 8) & 0xFF);
    byteWrapper.put((int) longBits & 0xFF);
  }

  public int getEncodedLength()
  {
    return 8;
  }

  public void decode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(getOctetBoundary());
    long longBits = 0l;
    longBits += (long) byteWrapper.get() << 56;
    longBits += (long) byteWrapper.get() << 48;
    longBits += (long) byteWrapper.get() << 40;
    longBits += (long) byteWrapper.get() << 32;
    longBits += (long) byteWrapper.get() << 24;
    longBits += (long) byteWrapper.get() << 16;
    longBits += (long) byteWrapper.get() << 8;
    longBits += (long) byteWrapper.get() << 0;
    value = Double.longBitsToDouble(longBits);
  }

  public double getValue()
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
    return rhs instanceof HLAfloat64BE &&
           getValue() == ((HLAfloat64BE) rhs).getValue();
  }

  @Override
  public String toString()
  {
    return Double.toString(value);
  }
}
