package net.sf.ohla.rti.hla.rti1516.jlc;

import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516DataElement;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.HLAinteger64LE;

public class IEEE1516HLAinteger64LE
  extends IEEE1516DataElement
  implements HLAinteger64LE
{
  protected long value;

  public IEEE1516HLAinteger64LE()
  {
  }

  public IEEE1516HLAinteger64LE(long value)
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
    long encoded = value;
    byteWrapper.put((int) encoded & 0xFF);
    byteWrapper.put((int) (encoded >>> 8) & 0xFF);
    byteWrapper.put((int) (encoded >>> 16) & 0xFF);
    byteWrapper.put((int) (encoded >>> 24) & 0xFF);
    byteWrapper.put((int) (encoded >>> 32) & 0xFF);
    byteWrapper.put((int) (encoded >>> 40) & 0xFF);
    byteWrapper.put((int) (encoded >>> 48) & 0xFF);
    byteWrapper.put((int) (encoded >>> 56) & 0xFF);
  }

  public int getEncodedLength()
  {
    return 8;
  }

  public void decode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(getOctetBoundary());
    long decoded = 0L;
    decoded += (long) byteWrapper.get();
    decoded += (long) byteWrapper.get() << 8;
    decoded += (long) byteWrapper.get() << 16;
    decoded += (long) byteWrapper.get() << 24;
    decoded += (long) byteWrapper.get() << 32;
    decoded += (long) byteWrapper.get() << 40;
    decoded += (long) byteWrapper.get() << 48;
    decoded += (long) byteWrapper.get() << 56;
    value = decoded;
  }

  public long getValue()
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
    return rhs instanceof HLAinteger64LE &&
           value == ((HLAinteger64LE) rhs).getValue();
  }

  @Override
  public String toString()
  {
    return Long.toString(value);
  }
}
