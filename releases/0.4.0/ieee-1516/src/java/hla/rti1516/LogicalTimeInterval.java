package hla.rti1516;

import java.io.Serializable;

public interface LogicalTimeInterval
  extends Comparable, Serializable
{
  boolean isZero();

  boolean isEpsilon();

  LogicalTimeInterval subtract(LogicalTimeInterval lti);

  int compareTo(Object rhs);

  boolean equals(Object rhs);

  int hashCode();

  String toString();

  int encodedLength();

  void encode(byte[] buffer, int offset);
}
