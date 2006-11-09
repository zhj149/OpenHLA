package hla.rti1516.jlc;

public interface DataElementFactory
{
  /**
   * Creates an element appropriate for the specified index.
   *
   * @param index position in array that this element will take
   * @return the DataElement for the specified index
   */
  public DataElement createElement(int index);
}

