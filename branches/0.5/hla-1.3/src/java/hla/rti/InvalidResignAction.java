package hla.rti;

public final class InvalidResignAction
  extends RTIexception
{
  public InvalidResignAction()
  {
  }

  public InvalidResignAction(String message)
  {
    super(message);
  }

  public InvalidResignAction(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InvalidResignAction(Throwable cause)
  {
    super(cause);
  }

  public InvalidResignAction(String message, int serial)
  {
    super(message, serial);
  }

  public InvalidResignAction(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public InvalidResignAction(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
