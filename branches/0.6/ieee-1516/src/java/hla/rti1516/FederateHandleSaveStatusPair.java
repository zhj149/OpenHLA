package hla.rti1516;

import java.io.Serializable;

public final class FederateHandleSaveStatusPair
  implements Serializable
{
  public FederateHandle handle;
  public SaveStatus status;

  public FederateHandleSaveStatusPair(FederateHandle handle, SaveStatus status)
  {
    this.handle = handle;
    this.status = status;
  }
}
