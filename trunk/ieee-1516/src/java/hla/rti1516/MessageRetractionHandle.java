package hla.rti1516;

import java.io.Serializable;

public interface MessageRetractionHandle
  extends Serializable
{
  boolean equals(Object otherMessageRetractionHandle);

  int hashCode();

  String toString();
}
