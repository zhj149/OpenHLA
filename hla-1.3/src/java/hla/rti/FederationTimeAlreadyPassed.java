package hla.rti;

public final class FederationTimeAlreadyPassed
  extends RTIexception
{
  public FederationTimeAlreadyPassed()
  {
  }

  public FederationTimeAlreadyPassed(String message)
  {
    super(message);
  }

  public FederationTimeAlreadyPassed(String message, Throwable cause)
  {
    super(message, cause);
  }

  public FederationTimeAlreadyPassed(Throwable cause)
  {
    super(cause);
  }

  public FederationTimeAlreadyPassed(String message, int serial)
  {
    super(message, serial);
  }

  public FederationTimeAlreadyPassed(String message, Throwable cause,
                                     int serial)
  {
    super(message, cause, serial);
  }

  public FederationTimeAlreadyPassed(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
