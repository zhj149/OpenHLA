package hla.rti;

public final class TimeConstrainedAlreadyEnabled
  extends RTIexception
{
  public TimeConstrainedAlreadyEnabled()
  {
  }

  public TimeConstrainedAlreadyEnabled(String message)
  {
    super(message);
  }

  public TimeConstrainedAlreadyEnabled(String message, Throwable cause)
  {
    super(message, cause);
  }

  public TimeConstrainedAlreadyEnabled(Throwable cause)
  {
    super(cause);
  }

  public TimeConstrainedAlreadyEnabled(String message, int serial)
  {
    super(message, serial);
  }

  public TimeConstrainedAlreadyEnabled(String message, Throwable cause,
                                       int serial)
  {
    super(message, cause, serial);
  }

  public TimeConstrainedAlreadyEnabled(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
