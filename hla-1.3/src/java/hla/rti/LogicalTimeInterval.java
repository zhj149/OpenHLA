package hla.rti;

public interface LogicalTimeInterval
{
  boolean isEpsilon();

  boolean isZero();

  boolean isEqualTo(LogicalTimeInterval rhs);

  boolean isGreaterThan(LogicalTimeInterval rhs);

  boolean isGreaterThanOrEqualTo(LogicalTimeInterval rhs);

  boolean isLessThan(LogicalTimeInterval rhs);

  boolean isLessThanOrEqualTo(LogicalTimeInterval rhs);

  void setEpsilon();

  void setZero();

  void setTo(LogicalTimeInterval lti);

  void encode(byte[] buffer, int offset);

  int encodedLength();
}
