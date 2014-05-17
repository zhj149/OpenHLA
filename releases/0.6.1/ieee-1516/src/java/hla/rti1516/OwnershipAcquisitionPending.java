package hla.rti1516;

public final class OwnershipAcquisitionPending
  extends RTIexception
{
  public OwnershipAcquisitionPending(String message)
  {
    super(message);
  }

  public OwnershipAcquisitionPending(String message, Throwable cause)
  {
    super(message, cause);
  }

  public OwnershipAcquisitionPending(Throwable cause)
  {
    super(cause);
  }
}
