package hla.rti;

public final class InvalidRegionContext
  extends RTIexception
{
  public InvalidRegionContext()
  {
  }

  public InvalidRegionContext(String message)
  {
    super(message);
  }

  public InvalidRegionContext(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InvalidRegionContext(Throwable cause)
  {
    super(cause);
  }

  public InvalidRegionContext(String message, int serial)
  {
    super(message, serial);
  }

  public InvalidRegionContext(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public InvalidRegionContext(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
