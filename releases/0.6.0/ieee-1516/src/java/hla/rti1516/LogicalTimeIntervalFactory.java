package hla.rti1516;

import java.io.Serializable;

public interface LogicalTimeIntervalFactory
  extends Serializable
{
  LogicalTimeInterval decode(byte[] buffer, int offset)
    throws CouldNotDecode;

  LogicalTimeInterval makeZero();

  LogicalTimeInterval makeEpsilon();
}
