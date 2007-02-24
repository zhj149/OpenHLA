package net.sf.ohla.rti.hla.rti1516.jlc;

import java.util.Iterator;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.HLAhandle;

public class IEEE1516HLAhandle
  extends IEEE1516DataElement
  implements HLAhandle
{
  protected final IEEE1516HLAopaqueData value;

  public IEEE1516HLAhandle()
  {
    value = new IEEE1516HLAopaqueData();
  }

  public IEEE1516HLAhandle(byte[] bytes)
  {
    value = new IEEE1516HLAopaqueData(bytes);
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
