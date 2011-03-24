package hla.rti1516.jlc;

/**
 * Interface for the HLA data type HLAinteger16BE.
 */
public interface HLAinteger16BE
{
  int getOctetBoundary();

  void encode(ByteWrapper byteWrapper);

  int getEncodedLength();

  void decode(ByteWrapper byteWrapper);

  /**
   * Returns the short value of this element.
   *
   * @return value the short value
   */
  short getValue();
}
