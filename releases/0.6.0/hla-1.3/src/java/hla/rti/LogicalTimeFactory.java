package hla.rti;

public interface LogicalTimeFactory
{
  LogicalTime decode(byte[] buffer, int offset)
    throws CouldNotDecode;

  LogicalTime makeInitial();
}
