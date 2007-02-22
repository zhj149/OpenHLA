package hla.rti;

public final class OwnershipAcquisitionPending
  extends RTIexception
{
  public OwnershipAcquisitionPending()
  {
  }

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

  public OwnershipAcquisitionPending(String message, int serial)
  {
    super(message, serial);
  }

  public OwnershipAcquisitionPending(String message, Throwable cause,
                                     int serial)
  {
    super(message, cause, serial);
  }

  public OwnershipAcquisitionPending(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
