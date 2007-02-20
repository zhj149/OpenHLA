package net.sf.ohla.rti1516.jlc;

import net.sf.ohla.rti1516.jlc.OHLADataElement;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.HLAinteger16BE;

public class OHLAHLAinteger16BE
  extends OHLADataElement
  implements HLAinteger16BE
{
  protected short value;

  public OHLAHLAinteger16BE()
  {
  }

  public OHLAHLAinteger16BE(short value)
  {
    this.value = value;
  }

  public int getOctetBoundary()
  {
    return 2;
  }

  public void encode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(getOctetBoundary());
    byteWrapper.put((value >>> 8) & 0xFF);
    byteWrapper.put(value & 0xFF);
  }

  public int getEncodedLength()
  {
    return 2;
  }

  public void decode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(getOctetBoundary());
    short decoded = 0;
    decoded += (short) (byteWrapper.get() << 8);
    decoded += (short) byteWrapper.get();
    this.value = decoded;
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
    return rhs instanceof HLAinteger16BE &&
           value == ((HLAinteger16BE) rhs).getValue();
  }

  @Override
  public String toString()
  {
    return Short.toString(value);
  }
}