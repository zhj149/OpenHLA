package hla.rti1516.jlc;

import java.util.Iterator;

/**
 * Interface for the HLA data type HLAopaqueData.
 */
public interface HLAopaqueData
  extends DataElement
{
  int size();

  byte get(int index);

  Iterator<Byte> iterator();

  void encode(ByteWrapper byteWrapper);

  void decode(ByteWrapper byteWrapper);

  int getEncodedLength();

  int getOctetBoundary();

  byte[] getValue();
}

