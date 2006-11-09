package hla.rti;

public final class CouldNotDiscover
  extends RTIexception
{
  public CouldNotDiscover()
  {
  }

  public CouldNotDiscover(String message)
  {
    super(message);
  }

  public CouldNotDiscover(String message, Throwable cause)
  {
    super(message, cause);
  }

  public CouldNotDiscover(Throwable cause)
  {
    super(cause);
  }

  public CouldNotDiscover(String message, int serial)
  {
    super(message, serial);
  }

  public CouldNotDiscover(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public CouldNotDiscover(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
