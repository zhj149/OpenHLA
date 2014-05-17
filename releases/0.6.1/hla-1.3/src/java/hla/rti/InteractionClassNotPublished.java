package hla.rti;

public final class InteractionClassNotPublished
  extends RTIexception
{
  public InteractionClassNotPublished()
  {
  }

  public InteractionClassNotPublished(String message)
  {
    super(message);
  }

  public InteractionClassNotPublished(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InteractionClassNotPublished(Throwable cause)
  {
    super(cause);
  }

  public InteractionClassNotPublished(String message, int serial)
  {
    super(message, serial);
  }

  public InteractionClassNotPublished(String message, Throwable cause,
                                      int serial)
  {
    super(message, cause, serial);
  }

  public InteractionClassNotPublished(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
