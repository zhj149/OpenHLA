package hla.rti;

public final class ObjectNotKnown
  extends RTIexception
{
  public ObjectNotKnown()
  {
  }

  public ObjectNotKnown(String message)
  {
    super(message);
  }

  public ObjectNotKnown(String message, Throwable cause)
  {
    super(message, cause);
  }

  public ObjectNotKnown(Throwable cause)
  {
    super(cause);
  }

  public ObjectNotKnown(String message, int serial)
  {
    super(message, serial);
  }

  public ObjectNotKnown(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public ObjectNotKnown(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
