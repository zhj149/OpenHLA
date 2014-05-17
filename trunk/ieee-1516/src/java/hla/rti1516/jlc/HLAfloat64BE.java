package hla.rti1516.jlc;

/**
 * Interface for the HLA data type HLAfloat64BE.
 */
public interface HLAfloat64BE
  extends DataElement
{
  int getOctetBoundary();

  void encode(ByteWrapper byteWrapper);

  int getEncodedLength();

  void decode(ByteWrapper byteWrapper);

  /**
   * Returns the double value of this element.
   *
   * @return value the double value
   */
  double getValue();
}
