package hla.rti1516;

import java.io.Serializable;

public interface ParameterHandle
  extends Serializable
{
  boolean equals(Object otherParameterHandle);

  int hashCode();

  int encodedLength();

  void encode(byte[] buffer, int offset);

  String toString();
}
