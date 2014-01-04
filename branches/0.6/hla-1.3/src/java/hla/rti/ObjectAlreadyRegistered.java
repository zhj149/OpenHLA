package hla.rti;

public final class ObjectAlreadyRegistered
  extends RTIexception
{
  public ObjectAlreadyRegistered()
  {
  }

  public ObjectAlreadyRegistered(String message)
  {
    super(message);
  }

  public ObjectAlreadyRegistered(String message, Throwable cause)
  {
    super(message, cause);
  }

  public ObjectAlreadyRegistered(Throwable cause)
  {
    super(cause);
  }

  public ObjectAlreadyRegistered(String message, int serial)
  {
    super(message, serial);
  }

  public ObjectAlreadyRegistered(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public ObjectAlreadyRegistered(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
