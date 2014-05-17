package hla.rti1516;

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
}
