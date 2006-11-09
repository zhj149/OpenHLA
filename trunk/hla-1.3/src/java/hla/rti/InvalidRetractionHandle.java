package hla.rti;

public final class InvalidRetractionHandle
  extends RTIexception
{
  public InvalidRetractionHandle()
  {
  }

  public InvalidRetractionHandle(String message)
  {
    super(message);
  }

  public InvalidRetractionHandle(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InvalidRetractionHandle(Throwable cause)
  {
    super(cause);
  }

  public InvalidRetractionHandle(String message, int serial)
  {
    super(message, serial);
  }

  public InvalidRetractionHandle(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public InvalidRetractionHandle(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
