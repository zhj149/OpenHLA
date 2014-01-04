package hla.rti;

public final class CouldNotOpenFED
  extends RTIexception
{
  public CouldNotOpenFED()
  {
  }

  public CouldNotOpenFED(String message)
  {
    super(message);
  }

  public CouldNotOpenFED(String message, Throwable cause)
  {
    super(message, cause);
  }

  public CouldNotOpenFED(Throwable cause)
  {
    super(cause);
  }

  public CouldNotOpenFED(String message, int serial)
  {
    super(message, serial);
  }

  public CouldNotOpenFED(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public CouldNotOpenFED(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
