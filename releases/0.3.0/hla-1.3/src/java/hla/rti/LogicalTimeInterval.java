package hla.rti;

public interface LogicalTimeInterval
{
  boolean isEpsilon();

  boolean isZero();

  boolean isEqualTo(LogicalTimeInterval value);

  boolean isGreaterThan(LogicalTimeInterval value);

  boolean isGreaterThanOrEqualTo(LogicalTimeInterval value);

  boolean isLessThan(LogicalTimeInterval value);

  boolean isLessThanOrEqualTo(LogicalTimeInterval value);

  void setEpsilon();

  void setZero();

  void setTo(LogicalTimeInterval value);

  void encode(byte[] buffer, int offset);

  int encodedLength();
}
