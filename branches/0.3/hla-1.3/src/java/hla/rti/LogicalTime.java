package hla.rti;

public interface LogicalTime
{
  void decreaseBy(LogicalTimeInterval subtrahend)
    throws IllegalTimeArithmetic;

  void increaseBy(LogicalTimeInterval addend)
    throws IllegalTimeArithmetic;

  boolean isInitial();

  boolean isFinal();

  boolean isEqualTo(LogicalTime value);

  boolean isGreaterThan(LogicalTime value);

  boolean isGreaterThanOrEqualTo(LogicalTime value);

  boolean isLessThan(LogicalTime value);

  boolean isLessThanOrEqualTo(LogicalTime value);

  void setFinal();

  void setInitial();

  void setTo(LogicalTime value);

  LogicalTimeInterval subtract(LogicalTime subtrahend);

  void encode(byte[] buffer, int offset);

  int encodedLength();
}
