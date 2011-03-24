package hla.rti;

public final class InteractionClassNotDefined
  extends RTIexception
{
  public InteractionClassNotDefined()
  {
  }

  public InteractionClassNotDefined(String message)
  {
    super(message);
  }

  public InteractionClassNotDefined(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InteractionClassNotDefined(Throwable cause)
  {
    super(cause);
  }

  public InteractionClassNotDefined(String message, int serial)
  {
    super(message, serial);
  }

  public InteractionClassNotDefined(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public InteractionClassNotDefined(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
