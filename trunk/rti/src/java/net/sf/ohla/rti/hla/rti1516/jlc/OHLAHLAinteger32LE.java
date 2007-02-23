package net.sf.ohla.rti.hla.rti1516.jlc;

import net.sf.ohla.rti.hla.rti1516.jlc.OHLADataElement;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.HLAinteger32LE;

public class OHLAHLAinteger32LE
  extends OHLADataElement
  implements HLAinteger32LE
{
  protected int value;

  public OHLAHLAinteger32LE()
  {
  }

  public OHLAHLAinteger32LE(int value)
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
    final int encoded = value;
    byteWrapper.put(encoded & 0xFF);
    byteWrapper.put((encoded >>> 8) & 0xFF);
    byteWrapper.put((encoded >>> 16) & 0xFF);
    byteWrapper.put((encoded >>> 24) & 0xFF);
  }

  public int getEncodedLength()
  {
    return 4;
  }

  public void decode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(getOctetBoundary());
    int decoded = 0;
    decoded += (short) byteWrapper.get();
    decoded += (short) byteWrapper.get() << 8;
    decoded += (short) byteWrapper.get() << 16;
    decoded += (short) byteWrapper.get() << 24;
    value = decoded;
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
    return rhs instanceof HLAinteger32LE &&
           value == ((HLAinteger32LE) rhs).getValue();
  }

  @Override
  public String toString()
  {
    return Integer.toString(value);
  }
}
