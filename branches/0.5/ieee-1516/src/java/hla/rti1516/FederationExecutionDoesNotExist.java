package hla.rti1516;

public final class FederationExecutionDoesNotExist
  extends RTIexception
{
  public FederationExecutionDoesNotExist(String message)
  {
    super(message);
  }

  public FederationExecutionDoesNotExist(String message, Throwable cause)
  {
    super(message, cause);
  }

  public FederationExecutionDoesNotExist(Throwable cause)
  {
    super(cause);
  }
}
