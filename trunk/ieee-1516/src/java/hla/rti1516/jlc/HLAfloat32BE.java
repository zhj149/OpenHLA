package hla.rti1516.jlc;

/**
 * Interface for the HLA data type HLAfloat32BE.
 */
public interface HLAfloat32BE
  extends DataElement
{
  int getOctetBoundary();

  void encode(ByteWrapper byteWrapper);

  int getEncodedLength();

  void decode(ByteWrapper byteWrapper);

  /**
   * Returns the float value of this element.
   *
   * @return value the float value
   */
  float getValue();
}
