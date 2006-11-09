package hla.rti;

public final class NameNotFound
  extends RTIexception
{
  public NameNotFound()
  {
  }

  public NameNotFound(String message)
  {
    super(message);
  }

  public NameNotFound(String message, Throwable cause)
  {
    super(message, cause);
  }

  public NameNotFound(Throwable cause)
  {
    super(cause);
  }

  public NameNotFound(String message, int serial)
  {
    super(message, serial);
  }

  public NameNotFound(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public NameNotFound(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
