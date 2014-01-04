package hla.rti1516.jlc;

/**
 * Interface for the HLA data type HLAinteger64BE.
 */
public interface HLAinteger64BE
  extends DataElement
{
  int getOctetBoundary();

  void encode(ByteWrapper byteWrapper);

  int getEncodedLength();

  void decode(ByteWrapper byteWrapper);

  /**
   * Returns the long value of this element.
   *
   * @return long value
   */
  long getValue();
}

