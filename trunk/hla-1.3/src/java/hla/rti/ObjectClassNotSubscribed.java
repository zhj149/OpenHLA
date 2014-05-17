package hla.rti;

public final class ObjectClassNotSubscribed
  extends RTIexception
{
  public ObjectClassNotSubscribed()
  {
  }

  public ObjectClassNotSubscribed(String message)
  {
    super(message);
  }

  public ObjectClassNotSubscribed(String message, Throwable cause)
  {
    super(message, cause);
  }

  public ObjectClassNotSubscribed(Throwable cause)
  {
    super(cause);
  }

  public ObjectClassNotSubscribed(String message, int serial)
  {
    super(message, serial);
  }

  public ObjectClassNotSubscribed(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public ObjectClassNotSubscribed(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
