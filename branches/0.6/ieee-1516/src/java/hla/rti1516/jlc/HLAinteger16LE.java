package hla.rti1516.jlc;

/**
 * Interface for the HLA data type HLAinteger16LE.
 */
public interface HLAinteger16LE
  extends DataElement
{
  int getOctetBoundary();

  void encode(ByteWrapper byteWrapper);

  int getEncodedLength();

  void decode(ByteWrapper byteWrapper);

  short getValue();
}

