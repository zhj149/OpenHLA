package net.sf.ohla.rti1516.jlc.impl;

import java.util.Iterator;

import net.sf.ohla.rti1516.jlc.impl.OHLADataElement;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.HLAhandle;

public class OHLAHLAhandle
  extends OHLADataElement
  implements HLAhandle
{
  protected final OHLAHLAopaqueData value;

  public OHLAHLAhandle()
  {
    value = new OHLAHLAopaqueData();
  }

  public OHLAHLAhandle(byte[] bytes)
  {
    value = new OHLAHLAopaqueData(bytes);
  }

  public int size()
  {
    return value.size();
  }

  public byte get(int index)
  {
    return value.get(index);
  }

  public Iterator<Byte> iterator()
  {
    return value.iterator();
  }

  public void encode(ByteWrapper byteWrapper)
  {
    value.encode(byteWrapper);
  }

  public void decode(ByteWrapper byteWrapper)
  {
    value.decode(byteWrapper);
  }

  public int getEncodedLength()
  {
    return value.getEncodedLength();
  }

  public int getOctetBoundary()
  {
    return value.getOctetBoundary();
  }

  public byte[] getValue()
  {
    return value.getValue();
  }

  @Override
  public int hashCode()
  {
    return value.hashCode();
  }

  @Override
  public boolean equals(Object rhs)
  {
    return value.equals(rhs);
  }

  @Override
  public String toString()
  {
    return value.toString();
  }
}
