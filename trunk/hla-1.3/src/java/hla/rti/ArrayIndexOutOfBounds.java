package hla.rti;

public final class ArrayIndexOutOfBounds
  extends RTIexception
{
  public ArrayIndexOutOfBounds()
  {
  }

  public ArrayIndexOutOfBounds(String message)
  {
    super(message);
  }

  public ArrayIndexOutOfBounds(String message, Throwable cause)
  {
    super(message, cause);
  }

  public ArrayIndexOutOfBounds(Throwable cause)
  {
    super(cause);
  }

  public ArrayIndexOutOfBounds(String message, int serial)
  {
    super(message, serial);
  }

  public ArrayIndexOutOfBounds(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public ArrayIndexOutOfBounds(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
