package hla.rti1516;

import java.io.Serializable;

public interface ObjectInstanceHandleFactory
  extends Serializable
{
  public ObjectInstanceHandle decode(byte[] buffer, int offset)
    throws CouldNotDecode, FederateNotExecutionMember;
}
