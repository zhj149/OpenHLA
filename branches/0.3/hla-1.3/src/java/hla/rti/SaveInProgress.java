package hla.rti;

public final class SaveInProgress
  extends RTIexception
{
  public SaveInProgress()
  {
  }

  public SaveInProgress(String message)
  {
    super(message);
  }

  public SaveInProgress(String message, Throwable cause)
  {
    super(message, cause);
  }

  public SaveInProgress(Throwable cause)
  {
    super(cause);
  }

  public SaveInProgress(String message, int serial)
  {
    super(message, serial);
  }

  public SaveInProgress(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public SaveInProgress(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
