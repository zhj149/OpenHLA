package hla.rti1516;

import java.io.Serializable;

public interface ObjectClassHandleFactory
  extends Serializable
{
  ObjectClassHandle decode(byte[] buffer, int offset)
    throws CouldNotDecode, FederateNotExecutionMember;
}
