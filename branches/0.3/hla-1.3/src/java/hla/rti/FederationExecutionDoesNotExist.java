package hla.rti;

public final class FederationExecutionDoesNotExist
  extends RTIexception
{
  public FederationExecutionDoesNotExist()
  {
  }

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

  public FederationExecutionDoesNotExist(String message, int serial)
  {
    super(message, serial);
  }

  public FederationExecutionDoesNotExist(String message, Throwable cause,
                                         int serial)
  {
    super(message, cause, serial);
  }

  public FederationExecutionDoesNotExist(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
