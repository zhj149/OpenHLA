package net.sf.ohla.rti1516.jlc;

import hla.rti1516.jlc.ByteWrapper;
import hla.rti1516.jlc.DataElement;

public abstract class OHLADataElement
  implements DataElement
{
  public byte[] toByteArray()
  {
    ByteWrapper byteWrapper = new ByteWrapper(getEncodedLength());
    encode(byteWrapper);
    return byteWrapper.array();
  }
}
