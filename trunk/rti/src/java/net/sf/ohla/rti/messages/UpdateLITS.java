package net.sf.ohla.rti.messages;

import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.federation.FederateProxy;

import hla.rti1516.LogicalTime;

public class UpdateLITS
  implements FederationExecutionMessage
{
  protected LogicalTime lits;

  public UpdateLITS(LogicalTime lits)
  {
    this.lits = lits;
  }

  public LogicalTime getLITS()
  {
    return lits;
  }

  @Override
  public String toString()
  {
    return String.format("update LITS: %s", lits);
  }

  public void execute(FederationExecution federationExecution,
                      FederateProxy federateProxy)
  {
    federationExecution.updateLITS(federateProxy, this);
  }
}
