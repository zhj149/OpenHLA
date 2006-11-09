package hla.rti1516;

import java.io.Serializable;

public interface LogicalTime
  extends Comparable, Serializable
{
  boolean isInitial();

  boolean isFinal();

  LogicalTime add(LogicalTimeInterval lti)
    throws IllegalTimeArithmetic;

  LogicalTime subtract(LogicalTimeInterval lti)
    throws IllegalTimeArithmetic;

  LogicalTimeInterval distance(LogicalTime lti);

  int compareTo(Object rhs);

  boolean equals(Object rhs);

  int hashCode();

  String toString();

  int encodedLength();

  void encode(byte[] buffer, int offset);
}
