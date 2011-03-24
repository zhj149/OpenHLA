package hla.rti1516.jlc;

/**
 * Interface for the HLA data type HLAinteger64LE.
 */
public interface HLAinteger64LE
  extends DataElement
{
  int getOctetBoundary();

  void encode(ByteWrapper byteWrapper);

  int getEncodedLength();

  void decode(ByteWrapper byteWrapper);

  /**
   * Returns the long value of this element.
   *
   * @return int value
   */
  long getValue();
}
