package hla.rti1516.jlc;

/**
 * Interface for the HLA data type HLAoctetPairBE.
 */
public interface HLAoctetPairBE
  extends DataElement
{
  int getOctetBoundary();

  void encode(ByteWrapper byteWrapper);

  int getEncodedLength();

  void decode(ByteWrapper byteWrapper);

  short getValue();
}
