package hla.rti;

public interface ReceivedInteraction
{
  int getParameterHandle(int index)
    throws ArrayIndexOutOfBounds;

  byte[] getValue(int index)
    throws ArrayIndexOutOfBounds;

  int getValueLength(int index)
    throws ArrayIndexOutOfBounds;

  byte[] getValueReference(int index)
    throws ArrayIndexOutOfBounds;

  Region getRegion();

  int getOrderType();

  int getTransportType();

  int size();
}
