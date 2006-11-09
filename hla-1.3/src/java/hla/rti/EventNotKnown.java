package hla.rti;

public final class EventNotKnown
  extends RTIexception
{
  public EventNotKnown()
  {
  }

  public EventNotKnown(String message)
  {
    super(message);
  }

  public EventNotKnown(String message, Throwable cause)
  {
    super(message, cause);
  }

  public EventNotKnown(Throwable cause)
  {
    super(cause);
  }

  public EventNotKnown(String message, int serial)
  {
    super(message, serial);
  }

  public EventNotKnown(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public EventNotKnown(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
