package hla.rti1516.jlc;

import java.util.Iterator;

/**
 * Interface for the HLA data type HLAvariableArray.
 */
public interface HLAvariableArray
  extends DataElement
{
  void encode(ByteWrapper byteWrapper);

  void decode(ByteWrapper byteWrapper);

  int getEncodedLength();

  int getOctetBoundary();

  /**
   * Adds an element to this variable array.
   *
   * @param dataElement
   */
  void addElement(DataElement dataElement);

  /**
   * Returns the number of elements in this variable array.
   */
  int size();

  /**
   * Returns element at the specified index.
   *
   * @param index
   */
  DataElement get(int index);

  /**
   * Returns an iterator for the elements in this variable array.
   */
  Iterator<DataElement> iterator();
}

