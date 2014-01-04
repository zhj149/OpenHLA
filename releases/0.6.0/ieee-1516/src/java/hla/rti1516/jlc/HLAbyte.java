package hla.rti1516.jlc;

/**
 * Interface for the HLA data type HLAbyte.
 */
public interface HLAbyte
  extends DataElement
{
  int getOctetBoundary();

  void encode(ByteWrapper byteWrapper);

  int getEncodedLength();

  void decode(ByteWrapper byteWrapper);

  /**
   * Returns the byte value of this element.
   *
   * @return value the byte value
   */
  byte getValue();
}

