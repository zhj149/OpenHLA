package hla.rti1516;

import java.io.Serializable;

public final class FederateHandleRestoreStatusPair
  implements Serializable
{
  public FederateHandle handle;
  public RestoreStatus status;

  public FederateHandleRestoreStatusPair(FederateHandle handle,
                                         RestoreStatus status)
  {
    this.handle = handle;
    this.status = status;
  }
}
