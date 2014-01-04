package hla.rti;

public final class ErrorReadingFED
  extends RTIexception
{
  public ErrorReadingFED()
  {
  }

  public ErrorReadingFED(String message)
  {
    super(message);
  }

  public ErrorReadingFED(String message, Throwable cause)
  {
    super(message, cause);
  }

  public ErrorReadingFED(Throwable cause)
  {
    super(cause);
  }

  public ErrorReadingFED(String message, int serial)
  {
    super(message, serial);
  }

  public ErrorReadingFED(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public ErrorReadingFED(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
