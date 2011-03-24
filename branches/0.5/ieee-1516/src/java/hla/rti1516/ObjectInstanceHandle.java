package hla.rti1516;

import java.io.Serializable;

public interface ObjectInstanceHandle
  extends Serializable
{
  public boolean equals(Object otherObjectInstanceHandle);

  public int hashCode();

  public int encodedLength();

  public void encode(byte[] buffer, int offset);

  public String toString();
}
