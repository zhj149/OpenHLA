package hla.rti;

public final class InteractionParameterNotDefined
  extends RTIexception
{
  public InteractionParameterNotDefined()
  {
  }

  public InteractionParameterNotDefined(String message)
  {
    super(message);
  }

  public InteractionParameterNotDefined(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InteractionParameterNotDefined(Throwable cause)
  {
    super(cause);
  }

  public InteractionParameterNotDefined(String message, int serial)
  {
    super(message, serial);
  }

  public InteractionParameterNotDefined(String message, Throwable cause,
                                        int serial)
  {
    super(message, cause, serial);
  }

  public InteractionParameterNotDefined(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
