package hla.rti1516;

import java.io.Serializable;

public interface LogicalTimeFactory
  extends Serializable
{
  LogicalTime decode(byte[] buffer, int offset)
    throws CouldNotDecode;

  LogicalTime makeInitial();

  LogicalTime makeFinal();
}
