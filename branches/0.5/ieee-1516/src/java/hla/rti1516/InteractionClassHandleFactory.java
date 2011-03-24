package hla.rti1516;

import java.io.Serializable;

public interface InteractionClassHandleFactory
  extends Serializable
{
  InteractionClassHandle decode(byte[] buffer, int offset)
    throws CouldNotDecode, FederateNotExecutionMember;
}
