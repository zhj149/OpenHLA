package hla.rti;

public final class TimeAdvanceAlreadyInProgress
  extends RTIexception
{
  public TimeAdvanceAlreadyInProgress()
  {
  }

  public TimeAdvanceAlreadyInProgress(String message)
  {
    super(message);
  }

  public TimeAdvanceAlreadyInProgress(String message, Throwable cause)
  {
    super(message, cause);
  }

  public TimeAdvanceAlreadyInProgress(Throwable cause)
  {
    super(cause);
  }

  public TimeAdvanceAlreadyInProgress(String message, int serial)
  {
    super(message, serial);
  }

  public TimeAdvanceAlreadyInProgress(String message, Throwable cause,
                                      int serial)
  {
    super(message, cause, serial);
  }

  public TimeAdvanceAlreadyInProgress(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
