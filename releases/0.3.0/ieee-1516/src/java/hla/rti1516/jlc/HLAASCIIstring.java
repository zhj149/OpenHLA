package hla.rti1516.jlc;

/**
 * Interface for the HLA data type HLAASCIIstring.
 */
public interface HLAASCIIstring
  extends DataElement
{
  void encode(ByteWrapper byteWrapper);

  void decode(ByteWrapper byteWrapper);

  int getEncodedLength();

  int getOctetBoundary();

  /**
   * Returns ASCII string value of this element.
   *
   * @return value the ASCII string
   */
  String getValue();
}
