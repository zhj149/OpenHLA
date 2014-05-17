package hla.rti;

public final class InteractionClassNotKnown
  extends RTIexception
{
  public InteractionClassNotKnown()
  {
  }

  public InteractionClassNotKnown(String message)
  {
    super(message);
  }

  public InteractionClassNotKnown(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InteractionClassNotKnown(Throwable cause)
  {
    super(cause);
  }

  public InteractionClassNotKnown(String message, int serial)
  {
    super(message, serial);
  }

  public InteractionClassNotKnown(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public InteractionClassNotKnown(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
