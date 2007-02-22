package hla.rti1516.jlc;

/**
 * Interface for the HLA data type HLAfloat64LE.
 */
public interface HLAfloat64LE
  extends DataElement
{
  int getOctetBoundary();

  void encode(ByteWrapper byteWrapper);

  int getEncodedLength();

  void decode(ByteWrapper byteWrapper);

  /**
   * Returns the double value of this element.
   *
   * @return double value
   */
  double getValue();
}
