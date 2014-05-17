package hla.rti1516.jlc;

public interface DataElement
{
  /**
   * Returns the octet boundary of this element.
   */
  int getOctetBoundary();

  /**
   * Encodes this element into the specified ByteWrapper.
   *
   * @param byteWrapper the ByteWrapper to encode to
   */
  void encode(ByteWrapper byteWrapper);

  /**
   * Returns the size in bytes of this element's encoding.
   *
   * @return the encoded size of this element
   */
  int getEncodedLength();

  /**
   * Returns a byte array with this element encoded.
   *
   * @return byte array with encoded element
   */
  byte[] toByteArray();

  /**
   * Decodes this element from the ByteWrapper.
   *
   * @param byteWrapper the ByteWrapper to decode from
   */
  void decode(ByteWrapper byteWrapper);
}

