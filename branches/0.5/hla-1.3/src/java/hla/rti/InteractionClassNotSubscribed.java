package hla.rti;

public final class InteractionClassNotSubscribed
  extends RTIexception
{
  public InteractionClassNotSubscribed()
  {
  }

  public InteractionClassNotSubscribed(String message)
  {
    super(message);
  }

  public InteractionClassNotSubscribed(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InteractionClassNotSubscribed(Throwable cause)
  {
    super(cause);
  }

  public InteractionClassNotSubscribed(String message, int serial)
  {
    super(message, serial);
  }

  public InteractionClassNotSubscribed(String message, Throwable cause,
                                       int serial)
  {
    super(message, cause, serial);
  }

  public InteractionClassNotSubscribed(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
