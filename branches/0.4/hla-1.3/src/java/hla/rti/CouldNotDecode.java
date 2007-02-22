package hla.rti;

public final class CouldNotDecode
  extends RTIexception
{
  public CouldNotDecode()
  {
  }

  public CouldNotDecode(String message)
  {
    super(message);
  }

  public CouldNotDecode(String message, Throwable cause)
  {
    super(message, cause);
  }

  public CouldNotDecode(Throwable cause)
  {
    super(cause);
  }

  public CouldNotDecode(String message, int serial)
  {
    super(message, serial);
  }

  public CouldNotDecode(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public CouldNotDecode(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
