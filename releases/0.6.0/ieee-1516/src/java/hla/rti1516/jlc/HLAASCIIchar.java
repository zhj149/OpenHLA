package hla.rti1516.jlc;

/**
 * Interface for the HLA data type HLAASCIIchar.
 */
public interface HLAASCIIchar
  extends DataElement
{
  int getOctetBoundary();

  void encode(ByteWrapper byteWrapper);

  int getEncodedLength();

  void decode(ByteWrapper byteWrapper);

  /**
   * Returns the ASCII character as a byte.
   *
   * @return value the ASCII character
   */
  byte getValue();
}

