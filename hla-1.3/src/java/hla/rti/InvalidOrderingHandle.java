package hla.rti;

public final class InvalidOrderingHandle
  extends RTIexception
{
  public InvalidOrderingHandle()
  {
  }

  public InvalidOrderingHandle(String message)
  {
    super(message);
  }

  public InvalidOrderingHandle(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InvalidOrderingHandle(Throwable cause)
  {
    super(cause);
  }

  public InvalidOrderingHandle(String message, int serial)
  {
    super(message, serial);
  }

  public InvalidOrderingHandle(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public InvalidOrderingHandle(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
