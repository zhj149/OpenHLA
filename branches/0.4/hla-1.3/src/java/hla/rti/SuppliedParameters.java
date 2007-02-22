package hla.rti;

public interface SuppliedParameters
{
  void add(int handle, byte[] value);

  void remove(int handle)
    throws ArrayIndexOutOfBounds;

  void removeAt(int index)
    throws ArrayIndexOutOfBounds;

  int getHandle(int index)
    throws ArrayIndexOutOfBounds;

  byte[] getValue(int index)
    throws ArrayIndexOutOfBounds;

  int getValueLength(int index)
    throws ArrayIndexOutOfBounds;

  byte[] getValueReference(int index)
    throws ArrayIndexOutOfBounds;

  int size();

  void empty();
}
