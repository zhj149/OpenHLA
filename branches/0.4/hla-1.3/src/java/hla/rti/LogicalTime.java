package hla.rti;

public interface LogicalTime
{
  void decreaseBy(LogicalTimeInterval lti)
    throws IllegalTimeArithmetic;

  void increaseBy(LogicalTimeInterval lti)
    throws IllegalTimeArithmetic;

  boolean isInitial();

  boolean isFinal();

  boolean isEqualTo(LogicalTime rhs);

  boolean isGreaterThan(LogicalTime rhs);

  boolean isGreaterThanOrEqualTo(LogicalTime rhs);

  boolean isLessThan(LogicalTime rhs);

  boolean isLessThanOrEqualTo(LogicalTime rhs);

  void setFinal();

  void setInitial();

  void setTo(LogicalTime lt);

  LogicalTimeInterval subtract(LogicalTime lt);

  void encode(byte[] buffer, int offset);

  int encodedLength();
}
