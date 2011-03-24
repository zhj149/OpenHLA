package hla.rti;

public interface LogicalTimeIntervalFactory
{
  LogicalTimeInterval decode(byte[] buffer, int offset)
    throws CouldNotDecode;

  LogicalTimeInterval makeZero();
}
