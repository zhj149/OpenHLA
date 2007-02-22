package hla.rti;

public final class InvalidExtents
  extends RTIexception
{
  public InvalidExtents()
  {
  }

  public InvalidExtents(String message)
  {
    super(message);
  }

  public InvalidExtents(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InvalidExtents(Throwable cause)
  {
    super(cause);
  }

  public InvalidExtents(String message, int serial)
  {
    super(message, serial);
  }

  public InvalidExtents(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public InvalidExtents(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
