package hla.rti;

public final class SaveNotInitiated
  extends RTIexception
{
  public SaveNotInitiated()
  {
  }

  public SaveNotInitiated(String message)
  {
    super(message);
  }

  public SaveNotInitiated(String message, Throwable cause)
  {
    super(message, cause);
  }

  public SaveNotInitiated(Throwable cause)
  {
    super(cause);
  }

  public SaveNotInitiated(String message, int serial)
  {
    super(message, serial);
  }

  public SaveNotInitiated(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public SaveNotInitiated(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
