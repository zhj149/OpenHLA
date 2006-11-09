package hla.rti;

public final class CouldNotRestore
  extends RTIexception
{
  public CouldNotRestore()
  {
  }

  public CouldNotRestore(String message)
  {
    super(message);
  }

  public CouldNotRestore(String message, Throwable cause)
  {
    super(message, cause);
  }

  public CouldNotRestore(Throwable cause)
  {
    super(cause);
  }

  public CouldNotRestore(String message, int serial)
  {
    super(message, serial);
  }

  public CouldNotRestore(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public CouldNotRestore(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
