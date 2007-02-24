package net.sf.ohla.rti.hla.rti1516.jlc;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.HLAinteger16LE;

public class IEEE1516HLAinteger16LE
  extends IEEE1516DataElement
  implements HLAinteger16LE
{
  protected short value;

  public IEEE1516HLAinteger16LE(short value)
  {
    this.value = value;
  }

  public IEEE1516HLAinteger16LE()
  {
    this.value = 0;
  }

  public int getOctetBoundary()
  {
    return 2;
  }

  public void encode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(getOctetBoundary());
    byteWrapper.put(value & 0xFF);
    byteWrapper.put((value >>> 8) & 0xFF);
  }

  public int getEncodedLength()
  {
    return 2;
  }

  public final void decode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(getOctetBoundary());
    short decoded = 0;
    decoded += (short) byteWrapper.get();
    decoded += (short) byteWrapper.get() << 8;
    value = decoded;
  }

  public short getValue()
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
    return rhs instanceof HLAinteger16LE &&
           value == ((HLAinteger16LE) rhs).getValue();
  }

  @Override
  public String toString()
  {
    return Short.toString(value);
  }
}
