package hla.rti1516;

import java.io.Serializable;

public interface ObjectClassHandle
  extends Serializable
{
  boolean equals(Object otherObjectClassHandle);

  int hashCode();

  int encodedLength();

  void encode(byte[] buffer, int offset);

  String toString();
}
