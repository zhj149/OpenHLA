package hla.rti;

public final class ObjectClassNotPublished
  extends RTIexception
{
  public ObjectClassNotPublished()
  {
  }

  public ObjectClassNotPublished(String message)
  {
    super(message);
  }

  public ObjectClassNotPublished(String message, Throwable cause)
  {
    super(message, cause);
  }

  public ObjectClassNotPublished(Throwable cause)
  {
    super(cause);
  }

  public ObjectClassNotPublished(String message, int serial)
  {
    super(message, serial);
  }

  public ObjectClassNotPublished(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public ObjectClassNotPublished(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
