package hla.rti;

public final class RestoreInProgress
  extends RTIexception
{
  public RestoreInProgress()
  {
  }

  public RestoreInProgress(String message)
  {
    super(message);
  }

  public RestoreInProgress(String message, Throwable cause)
  {
    super(message, cause);
  }

  public RestoreInProgress(Throwable cause)
  {
    super(cause);
  }

  public RestoreInProgress(String message, int serial)
  {
    super(message, serial);
  }

  public RestoreInProgress(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public RestoreInProgress(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
