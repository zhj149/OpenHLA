package hla.rti1516.jlc;

/**
 * Interface for the HLA data type HLAunicodeString.
 */
public interface HLAunicodeString
  extends DataElement
{
  void encode(ByteWrapper byteWrapper);

  void decode(ByteWrapper byteWrapper);

  int getEncodedLength();

  int getOctetBoundary();

  /**
   * Returns the string value of this element.
   *
   * @return string value
   */
  String getValue();
}

