package hla.rti;

public final class FederationExecutionAlreadyExists
  extends RTIexception
{
  public FederationExecutionAlreadyExists(String message)
  {
    super(message);
  }

  public FederationExecutionAlreadyExists(String message, Throwable cause)
  {
    super(message, cause);
  }

  public FederationExecutionAlreadyExists(Throwable cause)
  {
    super(cause);
  }
}
