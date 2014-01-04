package hla.rti1516;

import java.io.Serializable;

public interface FederateHandleFactory
  extends Serializable
{
  FederateHandle decode(byte[] buffer, int offset)
    throws CouldNotDecode, FederateNotExecutionMember;
}
