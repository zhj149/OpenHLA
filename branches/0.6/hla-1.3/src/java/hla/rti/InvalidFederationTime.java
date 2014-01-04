package hla.rti;

public final class InvalidFederationTime
  extends RTIexception
{
  public InvalidFederationTime()
  {
  }

  public InvalidFederationTime(String message)
  {
    super(message);
  }

  public InvalidFederationTime(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InvalidFederationTime(Throwable cause)
  {
    super(cause);
  }

  public InvalidFederationTime(String message, int serial)
  {
    super(message, serial);
  }

  public InvalidFederationTime(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public InvalidFederationTime(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
