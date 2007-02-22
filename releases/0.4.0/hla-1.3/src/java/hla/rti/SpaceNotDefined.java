package hla.rti;

public final class SpaceNotDefined
  extends RTIexception
{
  public SpaceNotDefined()
  {
  }

  public SpaceNotDefined(String message)
  {
    super(message);
  }

  public SpaceNotDefined(String message, Throwable cause)
  {
    super(message, cause);
  }

  public SpaceNotDefined(Throwable cause)
  {
    super(cause);
  }

  public SpaceNotDefined(String message, int serial)
  {
    super(message, serial);
  }

  public SpaceNotDefined(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public SpaceNotDefined(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
