package hla.rti1516;

public final class NoAcquisitionPending
  extends RTIexception
{
  public NoAcquisitionPending(String message)
  {
    super(message);
  }

  public NoAcquisitionPending(String message, Throwable cause)
  {
    super(message, cause);
  }

  public NoAcquisitionPending(Throwable cause)
  {
    super(cause);
  }
}
