package hla.rti;

public final class UnableToPerformSave
  extends RTIexception
{
  public UnableToPerformSave()
  {
  }

  public UnableToPerformSave(String message)
  {
    super(message);
  }

  public UnableToPerformSave(String message, Throwable cause)
  {
    super(message, cause);
  }

  public UnableToPerformSave(Throwable cause)
  {
    super(cause);
  }

  public UnableToPerformSave(String message, int serial)
  {
    super(message, serial);
  }

  public UnableToPerformSave(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public UnableToPerformSave(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
