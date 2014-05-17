package hla.rti;

public interface ReflectedAttributes
{
  int getAttributeHandle(int index)
    throws ArrayIndexOutOfBounds;

  byte[] getValue(int index)
    throws ArrayIndexOutOfBounds;

  int getValueLength(int index)
    throws ArrayIndexOutOfBounds;

  byte[] getValueReference(int index)
    throws ArrayIndexOutOfBounds;

  Region getRegion(int index)
    throws ArrayIndexOutOfBounds;

  int getOrderType(int index)
    throws ArrayIndexOutOfBounds;

  int getTransportType(int index)
    throws ArrayIndexOutOfBounds;

  int size();
}
