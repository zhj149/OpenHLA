package hla.rti1516;

import java.io.Serializable;

public interface InteractionClassHandle
  extends Serializable
{
  boolean equals(Object rhs);

  int hashCode();

  int encodedLength();

  void encode(byte[] buffer, int offset);

  String toString();
}
