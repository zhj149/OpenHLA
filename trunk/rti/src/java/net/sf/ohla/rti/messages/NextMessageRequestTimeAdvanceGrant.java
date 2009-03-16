package net.sf.ohla.rti.messages;

import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;

import hla.rti1516.LogicalTime;

public class NextMessageRequestTimeAdvanceGrant
  implements FederationExecutionMessage
{
  protected LogicalTime time;

  public NextMessageRequestTimeAdvanceGrant(LogicalTime time)
  {
    this.time = time;
  }

  public LogicalTime getTime()
  {
    return time;
  }

  public void execute(FederationExecution federationExecution,
                      FederateProxy federateProxy)
  {
    federationExecution.nextMessageRequestTimeAdvanceGrant(
      federateProxy, this);
  }

  @Override
  public String toString()
  {
    return String.format("Next Message Request Advance Grant: %s", time);
  }
}
