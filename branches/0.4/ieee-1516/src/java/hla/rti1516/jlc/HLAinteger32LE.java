package hla.rti1516.jlc;

/**
 * Interface for the HLA data type HLAinteger32LE.
 */
public interface HLAinteger32LE
  extends DataElement
{
  int getOctetBoundary();

  void encode(ByteWrapper byteWrapper);

  int getEncodedLength();

  void decode(ByteWrapper byteWrapper);

  /**
   * Returns the int value of this element.
   *
   * @return int value
   */
  int getValue();
}

