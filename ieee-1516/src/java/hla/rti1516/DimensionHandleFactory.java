package hla.rti1516;

import java.io.Serializable;

public interface DimensionHandleFactory
  extends Serializable
{
  DimensionHandle decode(byte[] buffer, int offset)
    throws CouldNotDecode, FederateNotExecutionMember;
}
