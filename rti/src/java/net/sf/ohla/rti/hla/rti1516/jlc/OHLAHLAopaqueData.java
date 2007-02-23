package net.sf.ohla.rti.hla.rti1516.jlc;

import java.util.Arrays;
import java.util.Iterator;

import net.sf.ohla.rti.hla.rti1516.jlc.OHLADataElement;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.HLAopaqueData;

public class OHLAHLAopaqueData
  extends OHLADataElement
  implements HLAopaqueData
{
  protected byte[] bytes;

  public OHLAHLAopaqueData()
  {
    bytes = new byte[0];
  }

  public OHLAHLAopaqueData(byte[] bytes)
  {
    this.bytes = bytes;
  }

  public int size()
  {
    return bytes.length;
  }

  public byte get(int index)
  {
    return bytes[index];
  }

  public Iterator<Byte> iterator()
  {
    return new Iterator()
    {
      protected int index = 0;

      public void remove()
      {
        throw new UnsupportedOperationException();
      }

      public boolean hasNext()
      {
        return index < bytes.length;
      }

      public Object next()
      {
        return bytes[index++];
      }
    };
  }

  public void encode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(4);
    byteWrapper.putInt(bytes.length);
    byteWrapper.put(bytes);
  }

  public void decode(ByteWrapper byteWrapper)
  {
    byteWrapper.align(4);
    bytes = new byte[byteWrapper.getInt()];
    byteWrapper.get(bytes);
  }

  public int getEncodedLength()
  {
    return 4 + bytes.length;
  }

  public int getOctetBoundary()
  {
    return 4;
  }

  public byte[] getValue()
  {
    return bytes;
  }

  @Override
  public int hashCode()
  {
    return Arrays.hashCode(bytes);
  }

  @Override
  public boolean equals(Object rhs)
  {
    return rhs != null && rhs.getClass().isArray() &&
           rhs.getClass().getComponentType().equals(byte.class) &&
           Arrays.equals(bytes, (byte[]) rhs);
  }

  @Override
  public String toString()
  {
    return Arrays.toString(bytes);
  }
}
