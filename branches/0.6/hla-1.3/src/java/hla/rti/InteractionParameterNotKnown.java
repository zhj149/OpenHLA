package hla.rti;

public final class InteractionParameterNotKnown
  extends RTIexception
{
  public InteractionParameterNotKnown()
  {
  }

  public InteractionParameterNotKnown(String message)
  {
    super(message);
  }

  public InteractionParameterNotKnown(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InteractionParameterNotKnown(Throwable cause)
  {
    super(cause);
  }

  public InteractionParameterNotKnown(String message, int serial)
  {
    super(message, serial);
  }

  public InteractionParameterNotKnown(String message, Throwable cause,
                                      int serial)
  {
    super(message, cause, serial);
  }

  public InteractionParameterNotKnown(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
