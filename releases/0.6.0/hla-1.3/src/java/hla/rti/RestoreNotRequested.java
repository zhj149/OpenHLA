package hla.rti;

public final class RestoreNotRequested
  extends RTIexception
{
  public RestoreNotRequested()
  {
  }

  public RestoreNotRequested(String message)
  {
    super(message);
  }

  public RestoreNotRequested(String message, Throwable cause)
  {
    super(message, cause);
  }

  public RestoreNotRequested(Throwable cause)
  {
    super(cause);
  }

  public RestoreNotRequested(String message, int serial)
  {
    super(message, serial);
  }

  public RestoreNotRequested(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public RestoreNotRequested(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
