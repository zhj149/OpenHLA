package hla.rti;

public final class ObjectClassNotKnown
  extends RTIexception
{
  public ObjectClassNotKnown()
  {
  }

  public ObjectClassNotKnown(String message)
  {
    super(message);
  }

  public ObjectClassNotKnown(String message, Throwable cause)
  {
    super(message, cause);
  }

  public ObjectClassNotKnown(Throwable cause)
  {
    super(cause);
  }

  public ObjectClassNotKnown(String message, int serial)
  {
    super(message, serial);
  }

  public ObjectClassNotKnown(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public ObjectClassNotKnown(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
