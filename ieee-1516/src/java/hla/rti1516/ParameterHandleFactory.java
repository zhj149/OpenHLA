package hla.rti1516;

import java.io.Serializable;

public interface ParameterHandleFactory
  extends Serializable
{
  ParameterHandle decode(byte[] buffer, int offset)
    throws CouldNotDecode, FederateNotExecutionMember;
}
