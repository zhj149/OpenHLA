package hla.rti;

public final class TimeAdvanceWasNotInProgress
  extends RTIexception
{
  public TimeAdvanceWasNotInProgress()
  {
  }

  public TimeAdvanceWasNotInProgress(String message)
  {
    super(message);
  }

  public TimeAdvanceWasNotInProgress(String message, Throwable cause)
  {
    super(message, cause);
  }

  public TimeAdvanceWasNotInProgress(Throwable cause)
  {
    super(cause);
  }

  public TimeAdvanceWasNotInProgress(String message, int serial)
  {
    super(message, serial);
  }

  public TimeAdvanceWasNotInProgress(String message, Throwable cause,
                                     int serial)
  {
    super(message, cause, serial);
  }

  public TimeAdvanceWasNotInProgress(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
