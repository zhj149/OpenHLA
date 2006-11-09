package hla.rti1516;

public final class FederateInternalError
  extends RTIexception
{
  public FederateInternalError(String message)
  {
    super(message);
  }

  public FederateInternalError(String message, Throwable cause)
  {
    super(message, cause);
  }

  public FederateInternalError(Throwable cause)
  {
    super(cause);
  }
}
