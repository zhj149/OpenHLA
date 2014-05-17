package hla.rti1516;

import java.io.Serializable;

public interface AttributeHandleFactory
  extends Serializable
{
  AttributeHandle decode(byte[] buffer, int offset)
    throws CouldNotDecode, FederateNotExecutionMember;
}
