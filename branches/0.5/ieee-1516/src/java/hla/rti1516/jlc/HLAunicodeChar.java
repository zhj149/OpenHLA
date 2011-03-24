package hla.rti1516.jlc;

/**
 * Interface for the HLA data type HLAunicodeChar.
 */
public interface HLAunicodeChar
  extends DataElement
{
  int getOctetBoundary();

  void encode(ByteWrapper byteWrapper);

  int getEncodedLength();

  void decode(ByteWrapper byteWrapper);

  short getValue();
}

