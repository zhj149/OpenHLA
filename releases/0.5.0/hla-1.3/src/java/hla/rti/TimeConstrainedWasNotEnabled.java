package hla.rti;

public final class TimeConstrainedWasNotEnabled
  extends RTIexception
{
  public TimeConstrainedWasNotEnabled()
  {
  }

  public TimeConstrainedWasNotEnabled(String message)
  {
    super(message);
  }

  public TimeConstrainedWasNotEnabled(String message, Throwable cause)
  {
    super(message, cause);
  }

  public TimeConstrainedWasNotEnabled(Throwable cause)
  {
    super(cause);
  }

  public TimeConstrainedWasNotEnabled(String message, int serial)
  {
    super(message, serial);
  }

  public TimeConstrainedWasNotEnabled(String message, Throwable cause,
                                      int serial)
  {
    super(message, cause, serial);
  }

  public TimeConstrainedWasNotEnabled(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
