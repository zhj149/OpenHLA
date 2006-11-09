package hla.rti1516.jlc;

/**
 * Interface for the HLA data type HLAoctetPairLE.
 */
public interface HLAoctetPairLE
  extends DataElement
{
  int getOctetBoundary();

  void encode(ByteWrapper byteWrapper);

  int getEncodedLength();

  void decode(ByteWrapper byteWrapper);

  short getValue();
}

