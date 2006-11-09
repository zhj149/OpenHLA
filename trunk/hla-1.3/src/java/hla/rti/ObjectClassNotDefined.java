package hla.rti;

public final class ObjectClassNotDefined
  extends RTIexception
{
  public ObjectClassNotDefined()
  {
  }

  public ObjectClassNotDefined(String message)
  {
    super(message);
  }

  public ObjectClassNotDefined(String message, Throwable cause)
  {
    super(message, cause);
  }

  public ObjectClassNotDefined(Throwable cause)
  {
    super(cause);
  }

  public ObjectClassNotDefined(String message, int serial)
  {
    super(message, serial);
  }

  public ObjectClassNotDefined(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public ObjectClassNotDefined(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
