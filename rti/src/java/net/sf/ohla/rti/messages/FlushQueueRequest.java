package net.sf.ohla.rti.messages;

import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;

import hla.rti1516.LogicalTime;

public class FlushQueueRequest
  implements FederationExecutionMessage
{
  protected LogicalTime time;

  public FlushQueueRequest(LogicalTime time)
  {
    this.time = time;
  }

  public LogicalTime getTime()
  {
    return time;
  }

  @Override
  public String toString()
  {
    return String.format("Flush Queue Request: %s", time);
  }

  public void execute(FederationExecution federationExecution,
                      FederateProxy federateProxy)
  {
    federationExecution.flushQueueRequest(federateProxy, this);
  }
}
