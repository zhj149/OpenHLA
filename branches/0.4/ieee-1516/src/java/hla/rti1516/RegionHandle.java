package hla.rti1516;

import java.io.Serializable;

public interface RegionHandle
  extends Serializable
{
  boolean equals(Object rhs);

  int hashCode();

  String toString();
}
