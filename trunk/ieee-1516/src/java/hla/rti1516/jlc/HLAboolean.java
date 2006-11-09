package hla.rti1516.jlc;

/**
 * Interface for the HLA data type HLAboolean.
 */
public interface HLAboolean
  extends DataElement
{
  int getOctetBoundary();

  void encode(ByteWrapper byteWrapper);

  int getEncodedLength();

  void decode(ByteWrapper byteWrapper);

  /**
   * Returns the boolean value of this element.
   *
   * @return value the boolean value
   */
  boolean getValue();
}
